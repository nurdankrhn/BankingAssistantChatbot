package BankingAssistantChatbot.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    private Double amount;

    private String transactionType; // for example, "DEPOSIT", "WITHDRAWAL", etc.

    private LocalDateTime createdAt;

    public Transaction() {
        // Default constructor
    }

    public Transaction(Account account, Double amount, String transactionType, LocalDateTime createdAt) {
        this.account = account;
        this.amount = amount;
        this.transactionType = transactionType;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Override toString() for better logging and debugging
    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", account=" + account +
                ", amount=" + amount +
                ", transactionType='" + transactionType + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}

