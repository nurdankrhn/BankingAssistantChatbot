package BankingAssistantChatbot.services;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiClientService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String LLAMA_URL = "http://localhost:11434/v1/chat/completions";
    private static final String SYSTEM_PROMPT = """
    You are BankingAssistant, an AI banking customer support agent.
    Your job is to assist customers with balance inquiries, transactions, IBAN help,
    banking concepts, fraud prevention, and general questions.
    
    Rules:
    1. Never guess balances or personal data.
    2. Never ask for PIN, CVV, or password.
    3. If user asks for a transfer: explain steps but DO NOT perform it.
    4. Detect user's language (Turkish or English) and reply in that language.
    5. Keep responses short, polite, and professional.
    6. If user says "sadece Türkçe konuş", use only Turkish.
    7. If user says "sadece İngilizce konuş", use only English.
    """;

    private static final Logger logger = LoggerFactory.getLogger(AiClientService.class);

    public String askModel(String message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "/models/Meta-Llama-3.1-8B-Instruct.Q4_K_M.gguf");
        payload.put("stream", false);
        payload.put("messages", List.of(
                Map.of("role", "system", "content", SYSTEM_PROMPT),
                Map.of("role", "user", "content", message)
        ));
        payload.put("temperature", 0.7);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            // Send the request and get the response
            ResponseEntity<Map> response = restTemplate.exchange(
                    LLAMA_URL, HttpMethod.POST, request, Map.class);

            // Check the response and parse the result
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map> choices = (List<Map>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map messageObj = (Map) choices.get(0).get("message");
                    return messageObj != null ? messageObj.get("content").toString() : "No response content.";
                }
                return "No valid response from the model.";
            } else {
                logger.error("Error: Received a non-OK response: {}", response.getStatusCode());
                return "Failed to get a response from the AI model.";
            }
        } catch (Exception e) {
            logger.error("Error while calling Llama model: ", e);
            return "An error occurred while contacting the AI model.";
        }
    }
}
