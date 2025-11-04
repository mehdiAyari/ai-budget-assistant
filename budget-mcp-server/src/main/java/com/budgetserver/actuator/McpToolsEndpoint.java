package com.budgetserver.actuator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Endpoint(id = "mcp-tools")  // Creates /actuator/mcp-tools
@RequiredArgsConstructor
@Slf4j
public class McpToolsEndpoint {

    private final ToolCallbackProvider toolCallbackProvider;

    // GET /actuator/mcp-tools
    @ReadOperation
    public Map<String, Object> getAllTools() {
        Map<String, Object> result = new HashMap<>();

        try {
            ToolCallback[] callbacks = toolCallbackProvider.getToolCallbacks();
            List<Map<String, Object>> tools = new ArrayList<>();

            for (ToolCallback callback : callbacks) {
                Map<String, Object> tool = new HashMap<>();

                tool.put("name", callback.getToolDefinition().name());
                tool.put("description", callback.getToolDefinition().description());
                tool.put("inputSchema", callback.getToolDefinition().inputSchema());
                tools.add(tool);
            }

            result.put("status", "available");
            result.put("totalTools", callbacks.length);
            result.put("tools", tools);
            result.put("mcpEndpoint", "/mcp/messages");

            log.debug("MCP Tools actuator endpoint accessed - {} tools", callbacks.length);

        } catch (Exception e) {
            log.error("Error in MCP tools actuator endpoint", e);
            result.put("status", "error");
            result.put("error", e.getMessage());
            result.put("totalTools", 0);
        }

        return result;
    }
}
