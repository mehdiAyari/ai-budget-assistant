package com.budgetclient.service;

import com.budgetclient.dto.BudgetSummary;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetSummaryService {

    private final List<McpAsyncClient> mcpClients;

    private final ObjectMapper objectMapper;

    public BudgetSummary getTotals(Integer year, Integer month) {
        try {
            log.info("Getting budget totals for {}/{} using direct MCP call", month, year);

            // Vérifier qu'on a au moins un client MCP
            if (mcpClients.isEmpty()) {
                log.error("No MCP clients available");
                return BudgetSummary.empty();
            }

            // Utiliser le premier client MCP (vous en avez 1)
            McpAsyncClient mcpClient = mcpClients.getFirst();

            // Créer les paramètres pour le tool getSummary
            Map<String, Object> arguments = new HashMap<>();
            arguments.put("year", year);
            arguments.put("month", month);

            log.debug("Calling getSummary tool with arguments: {}", arguments);

            // Créer la requête pour appeler le tool
            McpSchema.CallToolRequest request = new McpSchema.CallToolRequest("getSummary", arguments);

            // Appel direct du tool MCP - AUCUN COÛT D'IA
            McpSchema.CallToolResult result = mcpClient.callTool(request).block();
            String jsonResponse = extractJsonFromResult(result);

            BudgetSummary summary = objectMapper.readValue(jsonResponse, BudgetSummary.class);

            return summary;

        } catch (Exception e) {
            log.error("Error calling getSummary tool: {}", e.getMessage(), e);
            return BudgetSummary.empty();
        }
    }

    private String extractJsonFromResult(McpSchema.CallToolResult result) {
        for (McpSchema.Content content : result.content()) {
            if (content instanceof McpSchema.TextContent textContent) {
                return textContent.text();
            }
        }

        log.warn("No TextContent found in CallToolResult");
        return "{}";
    }

}