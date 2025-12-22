package BankingAssistantChatbot.services;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class AiClientService {

    private static final Logger logger = LoggerFactory.getLogger(AiClientService.class);

    private final RestTemplate restTemplate;
    private final String LLAMA_URL = "http://localhost:11434/api/chat";
    private static final String MODEL = "qwen2.5-3b-instruct-q4_k_m:latest";

    private static final Pattern GREETING =
            Pattern.compile("^(\\s)*(merhaba|selam|salam|hey|hi|hello|sa|slm)(\\s|!|\\.)*$", Pattern.CASE_INSENSITIVE);

    // Optional: keep, but if you want to allow SQL/tech as "banking", remove sql/code here
    private static final Pattern OUT_OF_SCOPE =
            Pattern.compile("\\b(python|java|react|spring|code|compile)\\b", Pattern.CASE_INSENSITIVE);

    private static final Pattern STEP_BY_STEP =
            Pattern.compile("\\b(adım adım|step by step|detaylı anlat|how to|nasıl yapılır)\\b", Pattern.CASE_INSENSITIVE);

    private static final String SYSTEM_PROMPT = """
        You are BankingAssistant, an AI banking customer support agent.
        Your job is to assist customers with balance inquiries, transactions, IBAN help,
        banking concepts, fraud prevention, and general questions.

        STRICT BEHAVIOR:
        - Stay in banking context. If the user asks something unrelated, politely say you can only help with banking topics.
        - Never output code blocks or programming examples.
        - Keep replies short: max 2–3 sentences unless the user explicitly asks for step-by-step instructions.
        - If the user message is only a greeting (e.g., "merhaba", "selam", "hi", "hello"),
          reply with a short greeting + ask what banking help they need.

        Rules:
        1. Never guess balances or personal data.
        2. Never ask for PIN, CVV, or password.
        3. If user asks for a transfer: explain steps but DO NOT perform it.
        4. Detect user's language (Turkish or English) and reply in that language.
        5. Keep responses short, polite, and professional.
        6. If user says "sadece Türkçe konuş", use only Turkish.
        7. If user says "sadece İngilizce konuş", use only English.
        """;

    public AiClientService() {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(10_000);
        f.setReadTimeout(180_000);
        this.restTemplate = new RestTemplate(f);
    }

    public String askModel(String userMessage) {
        String msg = userMessage == null ? "" : userMessage.trim();

        // 1) Hard guardrails (fast + reliable)
        if (GREETING.matcher(msg).matches()) {
            return isTurkish(msg)
                    ? "Merhaba! Bankacılıkla ilgili nasıl yardımcı olabilirim? (Bakiye, IBAN, havale/EFT, güvenlik vb.)"
                    : "Hi! How can I help you with banking today? (balance, IBAN, transfers, security, etc.)";
        }

        if (OUT_OF_SCOPE.matcher(msg).find()) {
            return isTurkish(msg)
                    ? "Ben yalnızca bankacılık konularında yardımcı olabilirim. IBAN, bakiye, transfer veya güvenlik hakkında sorabilir misiniz?"
                    : "I can only help with banking topics. Could you ask about IBAN, balance, transfers, or security?";
        }

        boolean wantsStepByStep = STEP_BY_STEP.matcher(msg).find();

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", MODEL);
        payload.put("stream", false);
        payload.put("messages", List.of(
                Map.of("role", "system", "content", SYSTEM_PROMPT),
                Map.of("role", "user", "content", msg)
        ));

        // 2) Make answers short by default, longer only when requested
        Map<String, Object> options = new HashMap<>();
        options.put("temperature", 0.0);
        options.put("top_p", 0.9);

        if (wantsStepByStep) {
            options.put("num_predict", 220); // allow longer instructions
            options.put("stop", List.of("```")); // still block code fences
        } else {
            options.put("num_predict", 80); // short & faster
            options.put("stop", List.of("```", "\n\n", "User:", "Kullanıcı:", "System:"));
        }

        payload.put("options", options);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    LLAMA_URL, HttpMethod.POST, new HttpEntity<>(payload, headers), Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map body = response.getBody();
                Map messageObj = (Map) body.get("message");
                Object content = messageObj == null ? null : messageObj.get("content");

                String text = content != null ? content.toString().trim() : "No response content from the model.";

                // 3) Final safety: enforce 2–3 sentences unless step-by-step asked
                if (!wantsStepByStep) {
                    text = keepMaxSentences(text, 3);
                }

                // remove accidental code fences if any
                text = text.replace("```", "").trim();

                return text;
            }

            logger.error("Non-OK response: {}", response.getStatusCode());
            return "Failed to get a response from the AI model.";

        } catch (Exception e) {
            logger.error("Error while calling Ollama model:", e);
            return "An error occurred while contacting the AI model.";
        }
    }

    private boolean isTurkish(String s) {
        String lower = s.toLowerCase(Locale.ROOT);
        return lower.contains("merhaba") || lower.contains("selam") || lower.contains("havale") || lower.contains("eft")
                || lower.matches(".*[çğıöşü].*");
    }

    private String keepMaxSentences(String text, int maxSentences) {
        // naive but effective for TR/EN: split by . ! ?
        String[] parts = text.split("(?<=[.!?])\\s+");
        if (parts.length <= maxSentences) return text;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maxSentences; i++) {
            sb.append(parts[i]).append(" ");
        }
        return sb.toString().trim();
    }
}
