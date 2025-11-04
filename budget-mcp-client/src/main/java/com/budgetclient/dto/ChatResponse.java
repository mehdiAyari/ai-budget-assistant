package com.budgetclient.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    
    private String role;
    private String content;
    private long timestamp;
    
    public static ChatResponse assistant(String content) {
        return new ChatResponse("assistant", content, System.currentTimeMillis());
    }
    
    public static ChatResponse user(String content) {
        return new ChatResponse("user", content, System.currentTimeMillis());
    }
    
    public static ChatResponse error(String errorMessage) {
        return new ChatResponse("assistant", "❌ " + errorMessage, System.currentTimeMillis());
    }
}