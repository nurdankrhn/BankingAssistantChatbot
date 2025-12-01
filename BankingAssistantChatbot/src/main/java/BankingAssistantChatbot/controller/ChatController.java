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

    @MessageMapping("/chat.send") // client sends message to /app/chat.send
    @SendTo("/topic/public") // bot response broadcast to /topic/public
    public ChatMessageDTO sendMessage(
            ChatMessageDTO message) {
        String botResponse = chatbotService.getResponse(message.getContent());
        return new ChatMessageDTO("BANK-BOT", botResponse, "BOT");
    }
}
