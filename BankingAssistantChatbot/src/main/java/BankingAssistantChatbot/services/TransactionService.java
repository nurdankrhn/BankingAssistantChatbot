package BankingAssistantChatbot.services;

import BankingAssistantChatbot.model.Account;
import BankingAssistantChatbot.model.Transaction;
import BankingAssistantChatbot.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<Transaction> getTransactions(Account account) {
        return transactionRepository.findByAccountOrderByCreatedAtDesc(account);
    }

    public List<Transaction> getLast10Transactions(Account account) {
        return transactionRepository.findTop10ByAccountOrderByCreatedAtDesc(account);
    }

    public Transaction save(Transaction transaction) {
        return transactionRepository.save(transaction);
    }
}
