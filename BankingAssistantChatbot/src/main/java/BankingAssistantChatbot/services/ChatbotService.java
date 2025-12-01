package BankingAssistantChatbot.services;

import org.springframework.stereotype.Service;

@Service
public class ChatbotService {

    public String getResponse(String message) {
        message = message.toLowerCase();
        if (message.contains("balance")) {
            return "Your current balance is 1000 USD.";
        } else if (message.contains("transfer")) {
            return "To make a transfer, please provide recipient IBAN and amount.";
        } else if (message.contains("hello")) {
            return "Hello! How can I assist you today?";
        } else {
            return "Sorry, I did not understand. Can you please rephrase?";
        }
    }
}
