package BankingAssistantChatbot.dto;

public class ChatMessageDTO {
    private String sender;
    private String content;
    private String type; // e.g., "USER" or "BOT"

    public ChatMessageDTO() {}

    public ChatMessageDTO(String sender, String content, String type) {
        this.sender = sender;
        this.content = content;
        this.type = type;
    }

    // getters & setters
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
