package com.budgetclient.service;

import com.budgetclient.dto.BudgetSummary;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Budget Summary Service Tests")
class BudgetSummaryServiceTest {

    @Mock
    private List<McpAsyncClient> mcpClients;

    @Mock
    private McpAsyncClient mcpClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private BudgetSummaryService budgetSummaryService;

    private BudgetSummary testSummary;

    @BeforeEach
    void setUp() {
        testSummary = BudgetSummary.builder()
                .totalIncome(BigDecimal.valueOf(3000.00))
                .totalExpenses(BigDecimal.valueOf(1500.00))
                .netAmount(BigDecimal.valueOf(1500.00))
                .build();
    }

    @Test
    @DisplayName("Should get totals successfully with valid year and month")
    void getTotals_WithValidYearAndMonth_ShouldReturnBudgetSummary() throws Exception {
        // Given
        Integer year = 2025;
        Integer month = 6;
        String jsonResponse = "{\"totalIncome\":3000.00,\"totalExpenses\":1500.00,\"netAmount\":1500.00}";

        McpSchema.CallToolResult callResult = createMockCallToolResult(jsonResponse);

        when(mcpClients.isEmpty()).thenReturn(false);
        when(mcpClients.getFirst()).thenReturn(mcpClient);
        when(mcpClient.callTool(any(McpSchema.CallToolRequest.class))).thenReturn(Mono.just(callResult));
        when(objectMapper.readValue(jsonResponse, BudgetSummary.class)).thenReturn(testSummary);

        // When
        BudgetSummary summaryResult = budgetSummaryService.getTotals(year, month);

        // Then
        assertThat(summaryResult).isEqualTo(testSummary);
        assertThat(summaryResult.getTotalIncome()).isEqualByComparingTo(BigDecimal.valueOf(3000.00));
        assertThat(summaryResult.getTotalExpenses()).isEqualByComparingTo(BigDecimal.valueOf(1500.00));
        assertThat(summaryResult.getNetAmount()).isEqualByComparingTo(BigDecimal.valueOf(1500.00));

        verify(mcpClient).callTool(argThat(request ->
                request.name().equals("getSummary") &&
                        request.arguments().get("year").equals(year) &&
                        request.arguments().get("month").equals(month)));
        verify(objectMapper).readValue(jsonResponse, BudgetSummary.class);
    }

    @Test
    @DisplayName("Should return empty summary when no MCP clients available")
    void getTotals_WithNoMcpClients_ShouldReturnEmptySummary() {
        // Given
        when(mcpClients.isEmpty()).thenReturn(true);

        // When
        BudgetSummary emptyResult = budgetSummaryService.getTotals(2025, 6);

        // Then
        assertThat(emptyResult).isEqualTo(BudgetSummary.empty());
        verifyNoInteractions(mcpClient);
        verifyNoInteractions(objectMapper);
    }

    @Test
    @DisplayName("Should handle MCP client call failure gracefully")
    void getTotals_WithMcpClientFailure_ShouldReturnEmptySummary() {
        // Given
        when(mcpClients.isEmpty()).thenReturn(false);
        when(mcpClients.getFirst()).thenReturn(mcpClient);
        when(mcpClient.callTool(any(McpSchema.CallToolRequest.class)))
                .thenReturn(Mono.error(new RuntimeException("MCP call failed")));

        // When
        BudgetSummary failureResult = budgetSummaryService.getTotals(2025, 6);

        // Then
        assertThat(failureResult).isEqualTo(BudgetSummary.empty());
        verify(mcpClient).callTool(any(McpSchema.CallToolRequest.class));
        verifyNoInteractions(objectMapper);
    }

    @Test
    @DisplayName("Should handle JSON parsing failure gracefully")
    void getTotals_WithJsonParsingFailure_ShouldReturnEmptySummary() throws Exception {
        // Given
        String invalidJson = "{invalid json}";
        McpSchema.CallToolResult parseResult = createMockCallToolResult(invalidJson);

        when(mcpClients.isEmpty()).thenReturn(false);
        when(mcpClients.getFirst()).thenReturn(mcpClient);
        when(mcpClient.callTool(any(McpSchema.CallToolRequest.class))).thenReturn(Mono.just(parseResult));
        when(objectMapper.readValue(invalidJson, BudgetSummary.class))
                .thenThrow(new RuntimeException("JSON parsing failed"));

        // When
        BudgetSummary parseFailureResult = budgetSummaryService.getTotals(2025, 6);

        // Then
        assertThat(parseFailureResult).isEqualTo(BudgetSummary.empty());
        verify(objectMapper).readValue(invalidJson, BudgetSummary.class);
    }

    @Test
    @DisplayName("Should handle null year and month parameters")
    void getTotals_WithNullParameters_ShouldCallMcpWithNullValues() throws Exception {
        // Given
        String jsonResponse = "{}";
        McpSchema.CallToolResult nullParamResult = createMockCallToolResult(jsonResponse);

        when(mcpClients.isEmpty()).thenReturn(false);
        when(mcpClients.getFirst()).thenReturn(mcpClient);
        when(mcpClient.callTool(any(McpSchema.CallToolRequest.class))).thenReturn(Mono.just(nullParamResult));
        when(objectMapper.readValue(jsonResponse, BudgetSummary.class)).thenReturn(BudgetSummary.empty());

        // When
        BudgetSummary nullParamsResult = budgetSummaryService.getTotals(null, null);

        // Then
        assertThat(nullParamsResult).isEqualTo(BudgetSummary.empty());
        verify(mcpClient).callTool(argThat(request ->
                request.arguments().get("year") == null &&
                        request.arguments().get("month") == null));
    }

    @Test
    @DisplayName("Should extract JSON from TextContent correctly")
    void getTotals_WithTextContent_ShouldExtractJsonCorrectly() throws Exception {
        // Given
        String expectedJson = "{\"totalIncome\":1000.00}";
        McpSchema.CallToolResult textContentResult = createMockCallToolResult(expectedJson);

        when(mcpClients.isEmpty()).thenReturn(false);
        when(mcpClients.getFirst()).thenReturn(mcpClient);
        when(mcpClient.callTool(any(McpSchema.CallToolRequest.class))).thenReturn(Mono.just(textContentResult));
        when(objectMapper.readValue(expectedJson, BudgetSummary.class)).thenReturn(testSummary);

        // When
        BudgetSummary textContentJsonResult = budgetSummaryService.getTotals(2025, 6);

        // Then
        assertThat(textContentJsonResult).isEqualTo(testSummary);
        verify(objectMapper).readValue(expectedJson, BudgetSummary.class);
    }

    @Test
    @DisplayName("Should handle CallToolResult without TextContent")
    void getTotals_WithoutTextContent_ShouldUseEmptyJson() throws Exception {
        // Given
        McpSchema.CallToolResult noTextContentResult = mock(McpSchema.CallToolResult.class);
        when(noTextContentResult.content()).thenReturn(Collections.emptyList());

        when(mcpClients.isEmpty()).thenReturn(false);
        when(mcpClients.getFirst()).thenReturn(mcpClient);
        when(mcpClient.callTool(any(McpSchema.CallToolRequest.class))).thenReturn(Mono.just(noTextContentResult));
        when(objectMapper.readValue("{}", BudgetSummary.class)).thenReturn(BudgetSummary.empty());

        // When
        BudgetSummary noTextResult = budgetSummaryService.getTotals(2025, 6);

        // Then
        assertThat(noTextResult).isEqualTo(BudgetSummary.empty());
        verify(objectMapper).readValue("{}", BudgetSummary.class);
    }

    @Test
    @DisplayName("Should pass correct arguments to MCP client")
    void getTotals_ShouldPassCorrectArgumentsToMcpClient() throws Exception {
        // Given
        Integer year = 2024;
        Integer month = 12;
        String jsonResponse = "{}";
        McpSchema.CallToolResult argumentsResult = createMockCallToolResult(jsonResponse);

        when(mcpClients.isEmpty()).thenReturn(false);
        when(mcpClients.getFirst()).thenReturn(mcpClient);
        when(mcpClient.callTool(any(McpSchema.CallToolRequest.class))).thenReturn(Mono.just(argumentsResult));
        when(objectMapper.readValue(jsonResponse, BudgetSummary.class)).thenReturn(BudgetSummary.empty());

        // When
        budgetSummaryService.getTotals(year, month);

        // Then
        verify(mcpClient).callTool(argThat(request -> {
            assertThat(request.name()).isEqualTo("getSummary");
            assertThat(request.arguments()).containsEntry("year", year);
            assertThat(request.arguments()).containsEntry("month", month);
            assertThat(request.arguments()).hasSize(2);
            return true;
        }));
    }

    private McpSchema.CallToolResult createMockCallToolResult(String jsonContent) {
        McpSchema.TextContent textContent = mock(McpSchema.TextContent.class);
        when(textContent.text()).thenReturn(jsonContent);

        McpSchema.CallToolResult callToolResult = mock(McpSchema.CallToolResult.class);
        when(callToolResult.content()).thenReturn(List.of(textContent));

        return callToolResult;
    }
}
