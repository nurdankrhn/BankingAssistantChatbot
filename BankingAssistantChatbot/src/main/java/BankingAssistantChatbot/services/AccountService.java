package BankingAssistantChatbot.services;

import BankingAssistantChatbot.model.Account;
import BankingAssistantChatbot.model.Customer;
import BankingAssistantChatbot.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public List<Account> getCustomerAccounts(Customer customer) {
        return accountRepository.findByCustomer(customer);
    }

    public Optional<Account> findByIban(String iban) {
        return accountRepository.findByIban(iban);
    }

    public Account save(Account account) {
        return accountRepository.save(account);
    }
}

