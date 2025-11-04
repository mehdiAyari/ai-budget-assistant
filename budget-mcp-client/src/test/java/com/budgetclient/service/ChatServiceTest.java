package com.budgetclient.service;

import com.budgetclient.dto.ChatRequest;
import com.budgetclient.dto.ChatResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Chat Service Tests")
class ChatServiceTest {

    @Mock
    private AsyncMcpToolCallbackProvider toolCallbackProvider;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec responseSpec;

    @Mock
    private ChatMemory chatMemory;

    @InjectMocks
    private ChatService chatService;

    private void setupChatClientMocks() {
        // Mock the chat client call chain
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.advisors(any(Consumer.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
    }

    @Test
    @DisplayName("Should process chat message and return response")
    void processMessage_WithValidMessage_ShouldReturnResponse() {
        // Given
        setupChatClientMocks();
        ChatRequest userMessage = new ChatRequest("Create a budget for food with $500 limit");
        String expectedResponse = "✅ Budget created successfully!";

        when(responseSpec.content()).thenReturn(expectedResponse);

        // When
        ChatResponse result = chatService.processMessage(userMessage);

        // Then
        assertThat(result.getContent()).isEqualTo(expectedResponse);
        assertThat(result.getRole()).isEqualTo("assistant");
        verify(chatClient).prompt();
        verify(requestSpec).call();
        verify(responseSpec).content();
    }

    @Test
    @DisplayName("Should handle empty message gracefully")
    void processMessage_WithEmptyMessage_ShouldHandleGracefully() {
        // Given
        setupChatClientMocks();
        ChatRequest userMessage = new ChatRequest("");
        String expectedResponse = "Please provide a message about your budget.";

        when(responseSpec.content()).thenReturn(expectedResponse);

        // When
        ChatResponse response = chatService.processMessage(userMessage);

        // Then
        assertThat(response.getContent()).isNotNull();
        assertThat(response.getRole()).isEqualTo("assistant");
        verify(chatClient).prompt();
    }

    @Test
    @DisplayName("Should handle exception gracefully")
    void processMessage_WithException_ShouldReturnErrorResponse() {
        // Given
        setupChatClientMocks();
        ChatRequest userMessage = new ChatRequest("Test message");

        when(responseSpec.content()).thenThrow(new RuntimeException("Test exception"));

        // When
        ChatResponse errorResponse = chatService.processMessage(userMessage);

        // Then
        assertThat(errorResponse.getContent()).isEqualTo("I encountered an error. Please try again.");
        assertThat(errorResponse.getRole()).isEqualTo("assistant");
    }

    @Test
    @DisplayName("Should clear chat memory successfully")
    void clearChatMemory_ShouldCallChatMemoryClear() {
        // When
        chatService.clearChatMemory();

        // Then
        verify(chatMemory).clear("poc-budget-chat");
    }

    @Test
    @DisplayName("Should get chat history successfully")
    void getChatHistory_ShouldReturnMessageList() {
        // Given
        List<Message> expectedMessages = new ArrayList<>();
        when(chatMemory.get("poc-budget-chat")).thenReturn(expectedMessages);

        // When
        List<Message> historyResult = chatService.getChatHistory();

        // Then
        assertThat(historyResult).isEqualTo(expectedMessages);
        verify(chatMemory).get("poc-budget-chat");
    }

    @Test
    @DisplayName("Should check MCP tools availability")
    void hasMcpTools_WithNullProvider_ShouldReturnFalse() {
        // Given - toolCallbackProvider is already mocked as null by default

        // When
        boolean mcpResult = chatService.hasMcpTools();

        // Then
        assertThat(mcpResult).isFalse();
    }

    @Test
    @DisplayName("Should check MCP tools availability with valid provider")
    void hasMcpTools_WithValidProvider_ShouldReturnTrue() {
        // Given
        ToolCallback[] mockCallbacks = new ToolCallback[1];
        when(toolCallbackProvider.getToolCallbacks()).thenReturn(mockCallbacks);

        // When
        boolean mcpResultWithTools = chatService.hasMcpTools();

        // Then
        assertThat(mcpResultWithTools).isTrue();
    }

    @Test
    @DisplayName("Should handle various budget-related queries")
    void processMessage_WithDifferentBudgetQueries_ShouldProcessCorrectly() {
        // Given
        setupChatClientMocks();

        // Test data for different types of queries
        String[] queries = {
                "Show me all my budgets",
                "Add expense of $25 for lunch",
                "How much have I spent this month?",
                "Create a budget for transportation with $300 limit"
        };

        String[] expectedResponses = {
                "📋 Current Active Budgets: ...",
                "💸 Transaction added successfully! ...",
                "📊 Monthly Summary: ...",
                "✅ Budget created successfully! ..."
        };

        for (int i = 0; i < queries.length; i++) {
            // Given
            when(responseSpec.content()).thenReturn(expectedResponses[i]);
            ChatRequest request = new ChatRequest(queries[i]);

            // When
            ChatResponse queryResponse = chatService.processMessage(request);

            // Then
            assertThat(queryResponse.getContent()).isEqualTo(expectedResponses[i]);
            assertThat(queryResponse.getRole()).isEqualTo("assistant");
        }

        // Verify all interactions
        verify(chatClient, times(queries.length)).prompt();
        verify(requestSpec, times(queries.length)).call();
    }

    @Test
    @DisplayName("Should handle long messages correctly")
    void processMessage_WithLongMessage_ShouldProcessCorrectly() {
        // Given
        setupChatClientMocks();
        String longMessage = "I want to create a comprehensive budget for my monthly expenses including " +
                "food which should be around $500, transportation costs of about $200, entertainment " +
                "budget of $150, and utilities budget of $300. Please help me set up these budgets " +
                "with appropriate alert thresholds.";
        String expectedResponse = "I'll help you create multiple budgets...";
        ChatRequest request = new ChatRequest(longMessage);

        when(responseSpec.content()).thenReturn(expectedResponse);

        // When
        ChatResponse longMessageResponse = chatService.processMessage(request);

        // Then
        assertThat(longMessageResponse.getContent()).isEqualTo(expectedResponse);
        verify(requestSpec).user(longMessage);
    }

    @Test
    @DisplayName("Should handle special characters in message")
    void processMessage_WithSpecialCharacters_ShouldProcessCorrectly() {
        // Given
        setupChatClientMocks();
        String messageWithSpecialChars = "Add expense of $45.50 for \"grocery shopping\" at Trader Joe's";
        String expectedResponse = "💸 Transaction added successfully!";
        ChatRequest request = new ChatRequest(messageWithSpecialChars);

        when(responseSpec.content()).thenReturn(expectedResponse);

        // When
        ChatResponse specialCharResponse = chatService.processMessage(request);

        // Then
        assertThat(specialCharResponse.getContent()).isEqualTo(expectedResponse);
        verify(requestSpec).user(messageWithSpecialChars);
    }

    @Test
    @DisplayName("Should handle unicode characters in message")
    void processMessage_WithUnicodeCharacters_ShouldProcessCorrectly() {
        // Given
        setupChatClientMocks();
        String unicodeMessage = "Add expense of €45.50 for café visit 🍕";
        String expectedResponse = "💸 Transaction added successfully!";
        ChatRequest request = new ChatRequest(unicodeMessage);

        when(responseSpec.content()).thenReturn(expectedResponse);

        // When
        ChatResponse unicodeResponse = chatService.processMessage(request);

        // Then
        assertThat(unicodeResponse.getContent()).isEqualTo(expectedResponse);
        verify(requestSpec).user(unicodeMessage);
    }

    @Test
    @DisplayName("Should maintain conversation context through memory")
    void processMessage_WithContextualMessages_ShouldMaintainContext() {
        // Given
        setupChatClientMocks();

        // Multiple related messages
        String[] contextualMessages = {
                "Create a budget for food with $500 limit",
                "Add expense of $50 for groceries",
                "How much is left in my food budget?"
        };

        String[] responses = {
                "✅ Budget created successfully!",
                "💸 Transaction added successfully!",
                "You have $450 remaining in your food budget."
        };

        // When & Then - Process each message
        for (int i = 0; i < contextualMessages.length; i++) {
            when(responseSpec.content()).thenReturn(responses[i]);
            ChatRequest request = new ChatRequest(contextualMessages[i]);

            ChatResponse contextualResponse = chatService.processMessage(request);

            assertThat(contextualResponse.getContent()).isEqualTo(responses[i]);
            assertThat(contextualResponse.getRole()).isEqualTo("assistant");
        }

        // Verify memory is being used (advisor should be called for each interaction)
        verify(chatClient, times(contextualMessages.length)).prompt();
        verify(requestSpec, times(contextualMessages.length)).advisors(any(Consumer.class));
    }
}
