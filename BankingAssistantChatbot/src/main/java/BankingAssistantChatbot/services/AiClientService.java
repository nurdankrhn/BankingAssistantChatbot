package BankingAssistantChatbot.services;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@Service
public class AiClientService {

    private static final Logger logger = LoggerFactory.getLogger(AiClientService.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final String AI_URL = "http://localhost:11434/v1/chat/completions";

    public String askModel(String userMessage) {
        try {
            // TinyLlama expects messages: [{"role":"user","content":"..."}]
            Map<String, Object> body = Map.of(
                    "model", "tinyllama-1.1b-chat-v1.0.Q4_K_M",
                    "messages", List.of(Map.of("role", "user", "content", userMessage))
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    AI_URL, HttpMethod.POST, request,
                    new ParameterizedTypeReference<>() {}
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) {
                logger.warn("Empty response from TinyLlama");
                return "Sorry, I could not get a response from the AI.";
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                if (message != null && message.get("content") != null) {
                    return message.get("content").toString();
                }
            }

            logger.warn("Unexpected response structure from TinyLlama: {}", responseBody);
            return "Sorry, I could not understand the AI response.";

        } catch (Exception e) {
            logger.error("Error communicating with TinyLlama", e);
            return "Sorry, there was an error connecting to the AI service.";
        }
    }
}
