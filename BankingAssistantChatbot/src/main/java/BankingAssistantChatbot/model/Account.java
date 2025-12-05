package BankingAssistantChatbot.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // CHECKING, SAVINGS, CREDIT etc.
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    private BigDecimal balance;

    @Column(unique = true, nullable = false)
    private String iban;

    @Column(nullable = false)
    private String status; // ACTIVE / BLOCKED etc.

    private LocalDateTime createdAt;

    // Relation to customer
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    // Relation to transactions
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<Transaction> transactions;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
