package com.budgetserver.actuator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.definition.ToolDefinition;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MCP Tools Endpoint Tests")
class McpToolsEndpointTest {

    @Mock
    private ToolCallbackProvider toolCallbackProvider;

    @Mock
    private ToolCallback toolCallback1;

    @Mock
    private ToolCallback toolCallback2;

    @Mock
    private ToolDefinition toolDefinition1;

    @Mock
    private ToolDefinition toolDefinition2;

    @InjectMocks
    private McpToolsEndpoint mcpToolsEndpoint;

    private void setupMockToolsWithCallbacks() {
        // Setup mock tool definitions
        when(toolDefinition1.name()).thenReturn("createBudget");
        when(toolDefinition1.description()).thenReturn("Create a new budget");
        when(toolDefinition1.inputSchema()).thenReturn("{\"type\":\"object\",\"properties\":{\"category\":{\"type\":\"string\"}}}");

        when(toolDefinition2.name()).thenReturn("addTransaction");
        when(toolDefinition2.description()).thenReturn("Add a new transaction");
        when(toolDefinition2.inputSchema()).thenReturn("{\"type\":\"object\",\"properties\":{\"amount\":{\"type\":\"number\"}}}");

        // Setup mock tool callbacks
        when(toolCallback1.getToolDefinition()).thenReturn(toolDefinition1);
        when(toolCallback2.getToolDefinition()).thenReturn(toolDefinition2);
    }

    @Test
    @DisplayName("Should return all available MCP tools")
    void getAllTools_WithAvailableTools_ShouldReturnToolsInformation() {
        // Given
        setupMockToolsWithCallbacks();
        ToolCallback[] callbacks = {toolCallback1, toolCallback2};
        when(toolCallbackProvider.getToolCallbacks()).thenReturn(callbacks);

        // When
        Map<String, Object> result = mcpToolsEndpoint.getAllTools();

        // Then
        assertThat(result.get("status")).isEqualTo("available");
        assertThat(result.get("totalTools")).isEqualTo(2);
        assertThat(result.get("mcpEndpoint")).isEqualTo("/mcp/messages");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tools = (List<Map<String, Object>>) result.get("tools");
        assertThat(tools).hasSize(2);

        // Verify first tool
        Map<String, Object> tool1 = tools.get(0);
        assertThat(tool1.get("name")).isEqualTo("createBudget");
        assertThat(tool1.get("description")).isEqualTo("Create a new budget");
        assertThat(tool1.get("inputSchema")).isEqualTo("{\"type\":\"object\",\"properties\":{\"category\":{\"type\":\"string\"}}}");

        verify(toolCallbackProvider).getToolCallbacks();
        verify(toolCallback1, atLeast(1)).getToolDefinition();
        verify(toolCallback2, atLeast(1)).getToolDefinition();
    }

    @Test
    @DisplayName("Should handle empty tools array")
    void getAllTools_WithNoTools_ShouldReturnEmptyToolsList() {
        // Given
        ToolCallback[] emptyCallbacks = {};
        when(toolCallbackProvider.getToolCallbacks()).thenReturn(emptyCallbacks);

        // When
        Map<String, Object> result = mcpToolsEndpoint.getAllTools();

        // Then
        assertThat(result.get("status")).isEqualTo("available");
        assertThat(result.get("totalTools")).isEqualTo(0);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tools = (List<Map<String, Object>>) result.get("tools");
        assertThat(tools).isEmpty();

        verify(toolCallbackProvider).getToolCallbacks();
        // Note: No interactions with tool callbacks or definitions since array is empty
    }

    @Test
    @DisplayName("Should handle exception gracefully")
    void getAllTools_WithException_ShouldReturnErrorStatus() {
        // Given
        when(toolCallbackProvider.getToolCallbacks()).thenThrow(new RuntimeException("Tool provider error"));

        // When
        Map<String, Object> result = mcpToolsEndpoint.getAllTools();

        // Then
        assertThat(result.get("status")).isEqualTo("error");
        assertThat(result.get("error")).isEqualTo("Tool provider error");
        assertThat(result.get("totalTools")).isEqualTo(0);

        verify(toolCallbackProvider).getToolCallbacks();
        // Note: No interactions with tool callbacks or definitions due to exception
    }
}
