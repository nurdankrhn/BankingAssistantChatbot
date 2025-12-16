package BankingAssistantChatbot.services;

import BankingAssistantChatbot.model.Account;
import BankingAssistantChatbot.model.Transaction;
import BankingAssistantChatbot.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import BankingAssistantChatbot.exceptions.InsufficientBalanceException;
import BankingAssistantChatbot.exceptions.InvalidIbanException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BankingService {

    private final AccountService accountService;
    private final TransactionRepository transactionRepository;


    public boolean isValidIban(String iban) {
        // Turkish IBAN format: TRkk BBBB BBBB CCCC CCCC CCCC CCCC
        String ibanPattern = "TR\\d{2}[0-9]{4}[0-9]{4}[0-9]{4}[0-9]{4}[0-9]{4}";
        Pattern pattern = Pattern.compile(ibanPattern);
        Matcher matcher = pattern.matcher(iban);
        return matcher.matches();
    }

    public BankingService(AccountService accountService, TransactionRepository transactionRepository) {
        this.accountService = accountService;
        this.transactionRepository = transactionRepository;
    }

    public Double getBalance(String iban) {
        Account account = accountService.findByIban(iban);
        return (account == null) ? null : account.getBalance().doubleValue();
    }

    public boolean transfer(String fromIban, String toIban, Double amount) throws InvalidIbanException, InsufficientBalanceException {
        if (!isValidIban(fromIban) || !isValidIban(toIban)) {
            throw new InvalidIbanException("Invalid IBAN format");
        }
        if (amount <= 0 || getBalance(fromIban).compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

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
