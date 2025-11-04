package com.budgetclient.controller;

import com.budgetclient.dto.BudgetSummary;
import com.budgetclient.dto.ChatRequest;
import com.budgetclient.dto.ChatResponse;
import com.budgetclient.service.BudgetSummaryService;
import com.budgetclient.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpAsyncClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
@ActiveProfiles("test")
@DisplayName("Chat Controller Tests")
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @MockBean
    private BudgetSummaryService budgetSummaryService;

    // Mock the dependencies that ChatService and BudgetSummaryService need
    @MockBean
    private AsyncMcpToolCallbackProvider mcpToolCallbackProvider;

    @MockBean
    private ChatClient chatClient;

    @MockBean
    private ChatMemory chatMemory;

    @MockBean
    private List<McpAsyncClient> mcpClients;

    // Use the real ObjectMapper, don't mock it
    @Autowired
    private ObjectMapper objectMapper;

    private ChatRequest validChatRequest;
    private ChatResponse successChatResponse;
    private BudgetSummary testBudgetSummary;

    @BeforeEach
    void setUp() {
        validChatRequest = new ChatRequest();
        validChatRequest.setMessage("Create a budget for food with $500 limit");

        successChatResponse = ChatResponse.assistant("✅ Budget created successfully!");

        testBudgetSummary = BudgetSummary.builder()
                .totalIncome(BigDecimal.valueOf(3000.00))
                .totalExpenses(BigDecimal.valueOf(1500.00))
                .netAmount(BigDecimal.valueOf(1500.00))
                .build();
    }

    @Test
    @DisplayName("Should process chat message successfully")
    void chat_WithValidRequest_ShouldReturnSuccessResponse() throws Exception {
        // Given
        when(chatService.processMessage(any(ChatRequest.class))).thenReturn(successChatResponse);

        // When & Then
        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validChatRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").value("✅ Budget created successfully!"))
                .andExpect(jsonPath("$.role").value("assistant"));

        verify(chatService).processMessage(any(ChatRequest.class));
    }

    @Test
    @DisplayName("Should clear chat memory successfully")
    void clearChatMemory_ShouldReturnSuccessMessage() throws Exception {
        // Given
        doNothing().when(chatService).clearChatMemory();

        // When & Then
        mockMvc.perform(delete("/api/chat/memory"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Chat memory cleared successfully"));

        verify(chatService).clearChatMemory();
    }

    @Test
    @DisplayName("Should get chat history successfully")
    void getChatHistory_ShouldReturnMessageList() throws Exception {
        // Given
        List<Message> mockHistory = Arrays.asList(
                new UserMessage("Hello"),
                new UserMessage("How are you?")
        );
        when(chatService.getChatHistory()).thenReturn(mockHistory);

        // When & Then
        mockMvc.perform(get("/api/chat/history"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(chatService).getChatHistory();
    }

    @Test
    @DisplayName("Should get budget totals successfully")
    void getTotals_WithValidYearAndMonth_ShouldReturnBudgetSummary() throws Exception {
        // Given
        when(budgetSummaryService.getTotals(2025, 6)).thenReturn(testBudgetSummary);

        // When & Then
        mockMvc.perform(get("/api/transactions/totals/2025/6"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalIncome").value(3000.00))
                .andExpect(jsonPath("$.totalExpenses").value(1500.00))
                .andExpect(jsonPath("$.netAmount").value(1500.00));

        verify(budgetSummaryService).getTotals(2025, 6);
    }

    @Test
    @DisplayName("Should return health status")
    void health_ShouldReturnHealthMessage() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Budget Chat Client is running!"));
    }

    @Test
    @DisplayName("Should return MCP status")
    void mcpStatus_ShouldReturnMcpStatusMessage() throws Exception {
        // Given
        when(chatService.hasMcpTools()).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/mcp/status"))
                .andExpect(status().isOk())
                .andExpect(content().string("MCP Tools Available: false"));

        verify(chatService).hasMcpTools();
    }

    @Test
    @DisplayName("Should handle validation errors for invalid chat request")
    void chat_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given
        ChatRequest invalidRequest = new ChatRequest();
        invalidRequest.setMessage(""); // Empty message should trigger validation

        // When & Then
        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle null message in chat request")
    void chat_WithNullMessage_ShouldReturnBadRequest() throws Exception {
        // Given
        ChatRequest invalidRequest = new ChatRequest();
        invalidRequest.setMessage(null);

        // When & Then
        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
