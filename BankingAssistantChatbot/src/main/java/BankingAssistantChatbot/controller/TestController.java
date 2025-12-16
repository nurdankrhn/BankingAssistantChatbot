package BankingAssistantChatbot.controller;

import BankingAssistantChatbot.dto.ChatMessageDTO;
import BankingAssistantChatbot.services.AiClientService;
import BankingAssistantChatbot.services.ChatbotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TestController {
    private final ChatbotService chatbotService;

    public TestController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/chat")
    public ResponseEntity<String> getChatbotResponse(@RequestBody ChatMessageDTO message) {
        // Assume message.getSender() contains the IBAN, and message.getContent() contains the user message
        String response = chatbotService.generateResponse(message.getSender(), message.getContent());
        return ResponseEntity.ok(response);
    }
}
