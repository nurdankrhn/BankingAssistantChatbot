package BankingAssistantChatbot.services;

import BankingAssistantChatbot.model.Account;
import BankingAssistantChatbot.model.Transaction;
import BankingAssistantChatbot.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class BankingService {

    private final AccountService accountService;
    private final TransactionRepository transactionRepository;

    public BankingService(AccountService accountService, TransactionRepository transactionRepository) {
        this.accountService = accountService;
        this.transactionRepository = transactionRepository;
    }

    public Double getBalance(String iban) {
        Account account = accountService.findByIban(iban);
        return (account == null) ? null : account.getBalance().doubleValue();
    }

    public boolean transfer(String fromIban, String toIban, Double amount) {

        if (amount <= 0) return false;

        Account from = accountService.findByIban(fromIban);
        Account to = accountService.findByIban(toIban);

        if (from == null || to == null) return false;

        boolean ok = accountService.transfer(from, to, BigDecimal.valueOf(amount));
        if (!ok) return false;

        // Transaction records
        transactionRepository.save(new Transaction(
                from, -amount, "TRANSFER_OUT", LocalDateTime.now()
        ));

        transactionRepository.save(new Transaction(
                to, amount, "TRANSFER_IN", LocalDateTime.now()
        ));

        return true;
    }
}
