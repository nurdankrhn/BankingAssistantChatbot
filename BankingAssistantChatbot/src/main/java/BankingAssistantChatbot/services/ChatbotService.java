package BankingAssistantChatbot.services;

import org.springframework.stereotype.Service;

@Service
public class ChatbotService {

    private final AiClientService aiClientService;
    private final BankingService bankingService;

    public ChatbotService(AiClientService aiClientService, BankingService bankingService) {
        this.aiClientService = aiClientService;
        this.bankingService = bankingService;
    }

    public String generateResponse(String userIban, String userMessage) {

        if (userMessage == null || userMessage.isBlank()) {
            return "Please type a message.";
        }

        String msg = userMessage.toLowerCase();

        // Balance inquiry
        if (msg.contains("balance")) {
            Double balance = bankingService.getBalance(userIban);
            if (balance == null) return "I could not find an account for your IBAN.";
            return "Your current balance is $" + balance;
        }

        // Transfer
        if (msg.contains("transfer")) {
            String[] parts = msg.split(" ");

            Double amount = null;
            String targetIban = null;

            for (String p : parts) {
                if (p.matches("\\d+(\\.\\d+)?")) {
                    amount = Double.parseDouble(p);
                } else if (p.startsWith("tr")) {
                    targetIban = p.toUpperCase();
                }
            }

            if (amount != null && targetIban != null) {
                boolean ok = bankingService.transfer(userIban, targetIban, amount);
                return ok
                        ? "Successfully transferred $" + amount + " to " + targetIban + "."
                        : "Transfer failed. Please check balance or IBAN.";
            }

            return "Please use format: Transfer <amount> to <IBAN>";
        }

        // AI fallback
        return aiClientService.askModel(userMessage);
    }
}
