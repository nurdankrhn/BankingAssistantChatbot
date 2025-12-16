package BankingAssistantChatbot.controller;

import BankingAssistantChatbot.model.Customer;
import BankingAssistantChatbot.repository.CustomerRepository;
import BankingAssistantChatbot.config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private JwtUtil jwtUtil;  // JWT Utility to generate token

    @Autowired
    private CustomerRepository customerRepository;  // Repository to access customer data

    // Authenticate customer based on email and password
    @PostMapping("/login")
    public String authenticate(@RequestBody LoginRequest loginRequest) {
        // Validate the customer's credentials by looking them up in the database
        Customer customer = customerRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Assuming password is plain text, you should hash passwords in practice
        if (customer.getPassword().equals(loginRequest.getPassword())) {
            // Assuming login is successful, generate JWT token
            String token = jwtUtil.generateToken(loginRequest.getEmail());
            return "Bearer " + token;  // Return the token in a Bearer format
        } else {
            return "Invalid email or password";  // Authentication failure
        }
    }
}
