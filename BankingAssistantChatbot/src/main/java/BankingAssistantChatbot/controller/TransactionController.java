package BankingAssistantChatbot.controller;

import BankingAssistantChatbot.model.Account;
import BankingAssistantChatbot.model.Transaction;
import BankingAssistantChatbot.repository.AccountRepository;
import BankingAssistantChatbot.services.TransactionService;
import BankingAssistantChatbot.services.AccountService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final AccountRepository accountRepository;

    public TransactionController(TransactionService transactionService, AccountRepository accountRepository) {
        this.transactionService = transactionService;
        this.accountRepository = accountRepository;
    }

    @GetMapping("/history/{iban}")
    public List<Transaction> getTransactionHistory(@PathVariable String iban) {
        Account account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new RuntimeException("Account not found with IBAN: " + iban)); // Throws exception if not found
        return transactionService.getTransactions(account);
    }

    @GetMapping("/last10/{iban}")
    public List<Transaction> getLast10Transactions(@PathVariable String iban) {
        Account account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new RuntimeException("Account not found with IBAN: " + iban)); // Throws exception if not found
        return transactionService.getLast10Transactions(account);
    }
}


