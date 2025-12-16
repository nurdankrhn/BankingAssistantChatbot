package BankingAssistantChatbot.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;  // Add password field for authentication

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Account> accounts;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
