package com.budgetclient.controller;

import com.budgetclient.dto.BudgetSummary;
import com.budgetclient.dto.ChatRequest;
import com.budgetclient.dto.ChatResponse;
import com.budgetclient.service.BudgetSummaryService;
import com.budgetclient.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}", maxAge = 3600)
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final BudgetSummaryService budgetSummaryService;

    @PostMapping("/chat/message")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        try {
            log.info("Received chat request: {}", request.getMessage());

            ChatResponse response = chatService.processMessage(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ChatResponse.error("Invalid request: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error in chat endpoint", e);
            return ResponseEntity.internalServerError()
                .body(ChatResponse.error("Internal server error: " + e.getMessage()));
        }
    }

    @DeleteMapping("/chat/memory")
    public ResponseEntity<Map<String, String>> clearChatMemory() {
        try {
            chatService.clearChatMemory();
            log.info("Cleared POC chat memory");
            return ResponseEntity.ok(Map.of("message", "Chat memory cleared successfully"));
        } catch (Exception e) {
            log.error("Error clearing chat memory", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to clear chat memory: " + e.getMessage()));
        }
    }

    @GetMapping("/chat/history")
    public ResponseEntity<List<Message>> getChatHistory() {
        try {
            List<Message> history = chatService.getChatHistory();
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error getting chat history", e);
            return ResponseEntity.internalServerError().body(List.of());
        }
    }

    @GetMapping("/transactions/totals/{year}/{month}")
    public ResponseEntity<BudgetSummary> getTotals(
            @PathVariable Integer year,
            @PathVariable Integer month) {
        try {
            log.info("Getting totals for {}/{}", month, year);
            BudgetSummary summary = budgetSummaryService.getTotals(year, month);
            return ResponseEntity.ok(summary);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid year/month: {}", e.getMessage());
            return ResponseEntity.badRequest().body(BudgetSummary.empty());
        } catch (Exception e) {
            log.error("Error getting totals", e);
            return ResponseEntity.internalServerError().body(BudgetSummary.empty());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Budget Chat Client is running!");
    }

    @GetMapping("/mcp/status")
    public ResponseEntity<String> mcpStatus() {
        boolean hasMcp = chatService.hasMcpTools();
        return ResponseEntity.ok("MCP Tools Available: " + hasMcp);
    }
}