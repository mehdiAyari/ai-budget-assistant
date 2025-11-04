package com.budgetclient.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
@Slf4j
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(
            ChatClient.Builder chatClientBuilder,
            @Autowired(required = false) AsyncMcpToolCallbackProvider toolCallbackProvider,
            ChatMemory chatMemory) {

        String currentDate = LocalDate.now().toString();
        String currentYear = String.valueOf(LocalDate.now().getYear());
        String currentMonth = String.valueOf(LocalDate.now().getMonthValue());

        String systemPrompt = String.format("""
                You are a budget management assistant with conversation memory and access to MCP tools.

                Current date: %s (year: %s, month: %s)
                When users say "today" or don't specify a date, use: %s

                Remember conversation context and reference previous messages naturally.
                When users say "that category" or "that transaction", use context to understand.
                """, currentDate, currentYear, currentMonth, currentDate);

        var builder = chatClientBuilder
                .defaultSystem(systemPrompt)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                );

        if (toolCallbackProvider != null) {
            builder = builder.defaultToolCallbacks(toolCallbackProvider.getToolCallbacks());
            log.info("✅ Configured {} MCP tools", toolCallbackProvider.getToolCallbacks().length);
        }

        return builder.build();
    }
}
