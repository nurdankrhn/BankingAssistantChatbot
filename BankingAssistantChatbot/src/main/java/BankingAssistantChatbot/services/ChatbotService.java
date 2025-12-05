package BankingAssistantChatbot.services;

import org.springframework.stereotype.Service;

@Service
public class ChatbotService {

    private final AiClientService aiClientService;

    public ChatbotService(AiClientService aiClientService) {
        this.aiClientService = aiClientService;
    }

    public String generateResponse(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            return "Please type a message.";
        }

        // Simple rule-based responses for common banking questions
        String lowerMsg = userMessage.toLowerCase();
        if (lowerMsg.contains("balance")) {
            return "You can check your account balance via our mobile app or internet banking.";
        } else if (lowerMsg.contains("transfer")) {
            return "To make a transfer, please provide the recipient details in the app.";
        } else if (lowerMsg.contains("hours") || lowerMsg.contains("working time")) {
            return "Our branch hours are Mon-Fri, 9:00 AM to 5:00 PM.";
        }

        // Otherwise, delegate to AI
        return aiClientService.askModel(userMessage);
    }
}
