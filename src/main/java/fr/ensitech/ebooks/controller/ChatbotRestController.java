package fr.ensitech.ebooks.controller;

import fr.ensitech.ebooks.dto.ChatRequest;
import fr.ensitech.ebooks.dto.ChatResponse;
import fr.ensitech.ebooks.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rest/chat")
@RequiredArgsConstructor
public class ChatbotRestController {

    private final ChatbotService chatbotService;

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        ChatResponse response = chatbotService.chat(request.getMessage());
        return ResponseEntity.ok(response);
    }
}