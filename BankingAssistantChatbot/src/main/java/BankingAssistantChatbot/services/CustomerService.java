package BankingAssistantChatbot.services;

import BankingAssistantChatbot.model.Customer;
import BankingAssistantChatbot.repository.CustomerRepository;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer createCustomer(String firstName, String lastName, String email) {
        if (customerRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Customer with this email already exists.");
        }

        Customer customer = Customer.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .build();

        return customerRepository.save(customer);
    }

    public Customer getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
    }
}
