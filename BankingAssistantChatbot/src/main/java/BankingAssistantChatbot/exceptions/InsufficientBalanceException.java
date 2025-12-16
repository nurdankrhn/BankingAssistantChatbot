package BankingAssistantChatbot.exceptions;

public class InsufficientBalanceException extends BusinessException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}