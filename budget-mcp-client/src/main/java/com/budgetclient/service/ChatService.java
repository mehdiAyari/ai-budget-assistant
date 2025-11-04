package com.budgetclient.service;

import com.budgetclient.dto.ChatRequest;
import com.budgetclient.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final AsyncMcpToolCallbackProvider toolCallbackProvider;
    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    
    private static final String POC_CONVERSATION_ID = "poc-budget-chat";

    public ChatResponse processMessage(ChatRequest request) {
        try {
            String response = chatClient.prompt()
                    .user(request.getMessage())
                    .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, POC_CONVERSATION_ID))
                    .call()
                    .content();

            return ChatResponse.assistant(response);
        } catch (Exception e) {
            log.error("Error processing chat message", e);
            return ChatResponse.assistant("I encountered an error. Please try again.");
        }
    }

    public List<Message> getChatHistory() {
        return chatMemory.get(POC_CONVERSATION_ID);
    }

    public void clearChatMemory() {
        chatMemory.clear(POC_CONVERSATION_ID);
    }

    public boolean hasMcpTools() {
        return toolCallbackProvider != null && 
               toolCallbackProvider.getToolCallbacks() != null &&
               toolCallbackProvider.getToolCallbacks().length > 0;
    }
}
