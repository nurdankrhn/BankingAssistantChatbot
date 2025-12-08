package BankingAssistantChatbot.model;

import jakarta.persistence.*;

@Entity
@Table(name = "chat_logs")
public class ChatLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    private String sender;

}
