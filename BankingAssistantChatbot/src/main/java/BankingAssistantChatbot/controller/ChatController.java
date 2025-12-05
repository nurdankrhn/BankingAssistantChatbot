package BankingAssistantChatbot.controller;

import BankingAssistantChatbot.dto.ChatMessageDTO;
import BankingAssistantChatbot.services.ChatbotService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    private final ChatbotService chatbotService;

    public ChatController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @MessageMapping("/chat.send")
    @SendTo("/topic/public")
    public ChatMessageDTO sendMessage(ChatMessageDTO message) {
        if (message == null || message.getContent() == null || message.getContent().isBlank()) {
            return new ChatMessageDTO("BANK-BOT", "Please send a valid message.", "BOT");
        }

        String userIban = message.getSender(); // sender holds IBAN
        String botResponse = chatbotService.generateResponse(userIban, message.getContent());

        return new ChatMessageDTO("BANK-BOT", botResponse, "BOT");
    }
}
