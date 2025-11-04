package com.budgetserver.integration;

import com.budgetserver.repository.BudgetRepository;
import com.budgetserver.repository.TransactionRepository;
import com.budgetserver.service.BudgetMcpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Budget MCP Server Integration Tests")
class BudgetMcpServerIntegrationTest {

    @Autowired
    private BudgetMcpService budgetMcpService;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        budgetRepository.deleteAll();
        transactionRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create budget and track spending end-to-end")
    void createBudgetAndTrackSpending_EndToEnd_ShouldWorkCorrectly() {
        // Given - Create a budget
        String createBudgetResult = budgetMcpService.createBudget(
                "Food", BigDecimal.valueOf(500.00), 2025, 6, "Monthly food budget", BigDecimal.valueOf(80.0));

        // Then - Verify budget creation
        assertThat(createBudgetResult).contains("✅ Budget created successfully!");
        assertThat(budgetRepository.findAll()).hasSize(1);

        // When - Add some transactions
        String transaction1 = budgetMcpService.addTransaction(
                BigDecimal.valueOf(50.00), "Grocery shopping", "Food", "EXPENSE", "2025-06-15");
        String transaction2 = budgetMcpService.addTransaction(
                BigDecimal.valueOf(25.00), "Restaurant", "Food", "EXPENSE", "2025-06-20");

        // Then - Verify transactions were added
        assertThat(transaction1).contains("💸 Transaction added successfully!");
        assertThat(transaction2).contains("💸 Transaction added successfully!");
        assertThat(transactionRepository.findAll()).hasSize(2);

        // When - Check budget status
        String budgetStatus = budgetMcpService.getAllBudgets();

        // Then - Verify spending tracking
        assertThat(budgetStatus).contains("**Food**");
        assertThat(budgetStatus).contains("$500.00"); // Budget limit
        assertThat(budgetStatus).contains("$75.00"); // Total spent
        assertThat(budgetStatus).contains("15.0%"); // Percentage used
        assertThat(budgetStatus).contains("$425.00"); // Remaining
    }

    @Test
    @DisplayName("Should handle monthly summary with mixed transactions")
    void getMonthlySummary_WithMixedTransactions_ShouldCalculateCorrectly() {
        // Given - Add various transactions
        budgetMcpService.addTransaction(
                BigDecimal.valueOf(3000.00), "Salary", "Income", "INCOME", "2025-06-01");
        budgetMcpService.addTransaction(
                BigDecimal.valueOf(500.00), "Freelance", "Income", "INCOME", "2025-06-15");
        budgetMcpService.addTransaction(
                BigDecimal.valueOf(200.00), "Groceries", "Food", "EXPENSE", "2025-06-05");
        budgetMcpService.addTransaction(
                BigDecimal.valueOf(50.00), "Gas", "Transportation", "EXPENSE", "2025-06-10");

        // When - Get spending summary
        String summary = budgetMcpService.getSpendingSummary(null, 2025, 6);

        // Then - Verify calculations
        assertThat(summary).contains("$3500.00"); // Total income
        assertThat(summary).contains("$250.00"); // Total expenses
        assertThat(summary).contains("$3250.00"); // Net amount
        assertThat(summary).contains("Positive ✅"); // Status
    }

    @Test
    @DisplayName("Should prevent duplicate budgets for same category and period")
    void createBudget_DuplicateCategoryAndPeriod_ShouldPreventCreation() {
        // Given - Create initial budget
        String firstBudget = budgetMcpService.createBudget(
                "Food", BigDecimal.valueOf(500.00), 2025, 6, null, null);
        assertThat(firstBudget).contains("✅ Budget created successfully!");

        // When - Try to create duplicate budget
        String duplicateBudget = budgetMcpService.createBudget(
                "Food", BigDecimal.valueOf(600.00), 2025, 6, null, null);

        // Then - Should prevent duplicate
        assertThat(duplicateBudget).contains("❌ Budget for Food already exists");
        assertThat(budgetRepository.findAll()).hasSize(1); // Still only one budget
    }

    @Test
    @DisplayName("Should handle structured summary with proper data types")
    void getSummary_ShouldReturnStructuredData() {
        // Given - Add some transactions
        budgetMcpService.addTransaction(
                BigDecimal.valueOf(2000.00), "Salary", "Income", "INCOME", "2025-06-01");
        budgetMcpService.addTransaction(
                BigDecimal.valueOf(300.00), "Groceries", "Food", "EXPENSE", "2025-06-10");

        // When - Get structured summary
        var summary = budgetMcpService.getSummary(2025, 6);

        // Then - Verify structured data
        assertThat(summary.getTotalIncome()).isEqualByComparingTo(BigDecimal.valueOf(2000.00));
        assertThat(summary.getTotalExpenses()).isEqualByComparingTo(BigDecimal.valueOf(300.00));
        assertThat(summary.getNetAmount()).isEqualByComparingTo(BigDecimal.valueOf(1700.00));
    }
}
