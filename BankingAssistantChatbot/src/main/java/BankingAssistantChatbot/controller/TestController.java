package BankingAssistantChatbot.controller;

import BankingAssistantChatbot.services.AiClientService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {
    private final AiClientService ai;

    public TestController(AiClientService ai) { this.ai = ai; }

    @GetMapping("/ping-llm")
    public String ping() {
        return ai.askModel("IBAN nedir? 2 cümleyle açıkla.");
    }
}
