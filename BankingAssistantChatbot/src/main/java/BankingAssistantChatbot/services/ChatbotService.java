package BankingAssistantChatbot.services;

import BankingAssistantChatbot.model.Account;
import BankingAssistantChatbot.model.Transaction;
import BankingAssistantChatbot.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ChatbotService {

    private final AiClientService aiClientService;
    private final BankingService bankingService;
    private final TransactionService transactionService;
    private final AccountRepository accountRepository;

    public ChatbotService(AiClientService aiClientService, BankingService bankingService,
                          TransactionService transactionService, AccountRepository accountRepository) {
        this.aiClientService = aiClientService;
        this.bankingService = bankingService;
        this.transactionService = transactionService;
        this.accountRepository = accountRepository;
    }

    public String generateResponse(String userIban, String userMessage) {

        Account account = accountRepository
                .findByIban(userIban)
                .orElse(null);

        if (account == null) {
            return "Account not found.";
        }

        // Check if the user is authenticated (role-based)
        if (!isUserAuthenticated(userIban)) {
            return getResponseInUserLanguage(userIban, "You need to log in first.");
        }

        if (userMessage == null || userMessage.isBlank()) {
            return getResponseInUserLanguage(userIban, "Please type a message.");
        }

        String msg = userMessage.toLowerCase();

        // Detect language of the message
        String userLanguage = detectLanguage(userMessage);

        // 1. IBAN format inquiry
        if (msg.contains("iban format") || msg.contains("iban nasıl")) {
            return getResponseInUserLanguage(userLanguage,
                    "IBAN is a unique International Bank Account Number used for international payments. " +
                            "In Turkey, IBAN is of the format: TRkk BBBB BBBB CCCC CCCC CCCC CCCC, where 'TR' is the country code, " +
                            "and the rest is a unique account number.");
        }

        // 2. How to make a transfer
        if (msg.contains("how to transfer") || msg.contains("transfer nasıl yapılır")) {
            return getResponseInUserLanguage(userLanguage,
                    "To make a transfer, you need to provide the IBAN of the recipient and the amount to transfer. " +
                            "For example, 'Please send 100 TL to IBAN: TR12345678901234567890123456'.");
        }

        // 3. IBAN inquiry
        if (msg.contains("iban") && msg.contains("send")) {
            return getResponseInUserLanguage(userLanguage,
                    "Please provide the IBAN of the recipient, along with the amount you'd like to transfer.");
        }

        // 4. **Balance Inquiry**: If the message mentions 'balance', check the balance.
        if (msg.contains("balance")) {
            Double balance = bankingService.getBalance(userIban);
            if (balance == null) return getResponseInUserLanguage(userLanguage, "I could not find an account for your IBAN.");
            return getResponseInUserLanguage(userLanguage, "Your current balance is " + balance + " TL.");
        }

        // 5. **Transfer Request**: If the message mentions 'transfer', extract amount and IBAN
        if (msg.contains("transfer")) {
            String[] parts = msg.split(" ");
            Double amount = null;
            String targetIban = null;

            // Extract amount and IBAN
            for (String p : parts) {
                if (p.matches("\\d+(\\.\\d+)?")) {  // Check if the part is a number (amount)
                    amount = Double.parseDouble(p);
                } else if (p.startsWith("tr")) {  // IBAN should start with 'TR' (Turkish IBAN)
                    targetIban = p.toUpperCase();
                }
            }

            // 5.1 **Check if IBAN is valid**
            if (targetIban == null || !isValidIban(targetIban)) {
                return getResponseInUserLanguage(userLanguage, "The IBAN format you provided is incorrect. Please provide a valid IBAN.");
            }

            // 5.2 **Check if balance is sufficient**
            Double balance = bankingService.getBalance(userIban);
            if (balance == null || balance < amount) {
                return getResponseInUserLanguage(userLanguage, "Your balance is insufficient for this transfer.");
            }

            // 5.3 **Perform the transfer**
            boolean success = bankingService.transfer(userIban, targetIban, amount);
            if (success) {
                return getResponseInUserLanguage(userLanguage, "Successfully transferred " + amount + " TL to IBAN " + targetIban + ".");
            } else {
                return getResponseInUserLanguage(userLanguage, "Transfer failed. Please check the IBAN or try again later.");
            }
        }

        if (msg.contains("last 10 transactions")) {
            List<Transaction> transactions =
                    transactionService.getLast10Transactions(account);

            if (transactions.isEmpty()) {
                return "No recent transactions found.";
            }
            return "Your last 10 transactions: " + transactions;
        }

        // Default AI response for general inquiries
        return aiClientService.askModel(userMessage);
    }

    // Helper method to validate IBAN format (for Turkish IBANs)
    private boolean isValidIban(String iban) {
        // Turkish IBAN format: TRkk BBBB BBBB CCCC CCCC CCCC CCCC
        String ibanPattern = "TR\\d{2}[0-9]{4}[0-9]{4}[0-9]{4}[0-9]{4}[0-9]{4}";
        Pattern pattern = Pattern.compile(ibanPattern);
        Matcher matcher = pattern.matcher(iban);
        return matcher.matches();
    }

    // Example authentication check (mocked for now)
    private boolean isUserAuthenticated(String userIban) {
        // Normally, you'd check the IBAN against a list of authenticated users
        // For now, just assume the user is authenticated
        return userIban != null && !userIban.isEmpty();
    }

    // Method to detect the user's language based on the message
    private String detectLanguage(String message) {
        // Basic language detection (you can refine this or use a library)
        if (message.matches(".*[şŞçÇıİğĞöÖüÜ].*")) {
            return "tr"; // Turkish
        } else {
            return "en"; // English
        }
    }

    // Method to return responses in the detected language
    private String getResponseInUserLanguage(String userLanguage, String message) {
        if ("tr".equals(userLanguage)) {
            // Return the response in Turkish
            return message; // You can refine this by translating if needed
        } else {
            // Default is English
            return message;
        }
    }
}
