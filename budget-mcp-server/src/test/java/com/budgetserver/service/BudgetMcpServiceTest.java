package com.budgetserver.service;

import com.budgetserver.dto.BudgetSummary;
import com.budgetserver.entity.Budget;
import com.budgetserver.entity.Transaction;
import com.budgetserver.entity.TransactionType;
import com.budgetserver.repository.BudgetRepository;
import com.budgetserver.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Budget MCP Service Tests")
class BudgetMcpServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private BudgetMcpService budgetMcpService;

    private Budget testBudget;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testBudget = Budget.builder()
                .id(1L)
                .category("Food")
                .monthlyLimit(BigDecimal.valueOf(500.00))
                .budgetYear(2025)
                .budgetMonth(6)
                .alertThreshold(BigDecimal.valueOf(80.0))
                .isActive(true)
                .notes("Test budget")
                .build();

        testTransaction = Transaction.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(45.50))
                .description("Grocery shopping")
                .category("Food")
                .type(TransactionType.EXPENSE)
                .date(LocalDate.now())
                .build();
    }

    @Test
    @DisplayName("Should create budget successfully with valid parameters")
    void createBudget_WithValidParameters_ShouldReturnSuccessMessage() {
        // Given
        String category = "Food";
        BigDecimal monthlyLimit = BigDecimal.valueOf(500.00);
        Integer year = 2025;
        Integer month = 6;
        String notes = "Test budget";
        BigDecimal alertThreshold = BigDecimal.valueOf(80.0);

        when(budgetRepository.findByCategoryAndBudgetYearAndBudgetMonthAndIsActive(
                category, year, month, true)).thenReturn(Optional.empty());
        when(budgetRepository.save(any(Budget.class))).thenReturn(testBudget);

        // When
        String result = budgetMcpService.createBudget(category, monthlyLimit, year, month, notes, alertThreshold);

        // Then
        assertThat(result).contains("✅ Budget created successfully!");
        assertThat(result).contains("Food");
        assertThat(result).contains("$500.00");
        verify(budgetRepository).save(any(Budget.class));
    }

    @Test
    @DisplayName("Should use current date when year and month are null")
    void createBudget_WithNullYearAndMonth_ShouldUseCurrentDate() {
        // Given
        String category = "Transportation";
        BigDecimal monthlyLimit = BigDecimal.valueOf(300.00);
        LocalDate now = LocalDate.now();

        when(budgetRepository.findByCategoryAndBudgetYearAndBudgetMonthAndIsActive(
                eq(category), eq(now.getYear()), eq(now.getMonthValue()), eq(true)))
                .thenReturn(Optional.empty());
        when(budgetRepository.save(any(Budget.class))).thenReturn(testBudget);

        // When
        String result = budgetMcpService.createBudget(category, monthlyLimit, null, null, null, null);

        // Then
        assertThat(result).contains("✅ Budget created successfully!");
        verify(budgetRepository).findByCategoryAndBudgetYearAndBudgetMonthAndIsActive(
                category, now.getYear(), now.getMonthValue(), true);
    }

    @Test
    @DisplayName("Should return error when budget already exists")
    void createBudget_WhenBudgetExists_ShouldReturnErrorMessage() {
        // Given
        String category = "Food";
        BigDecimal monthlyLimit = BigDecimal.valueOf(500.00);
        Integer year = 2025;
        Integer month = 6;

        when(budgetRepository.findByCategoryAndBudgetYearAndBudgetMonthAndIsActive(
                category, year, month, true)).thenReturn(Optional.of(testBudget));

        // When
        String result = budgetMcpService.createBudget(category, monthlyLimit, year, month, null, null);

        // Then
        assertThat(result).contains("❌ Budget for Food already exists");
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    @DisplayName("Should return error for invalid monthly limit")
    void createBudget_WithInvalidMonthlyLimit_ShouldReturnErrorMessage() {
        // Given
        BigDecimal invalidLimit = BigDecimal.valueOf(-100.00);

        // When
        String result = budgetMcpService.createBudget("Food", invalidLimit, 2025, 6, null, null);

        // Then
        assertThat(result).contains("❌ Monthly limit must be greater than 0");
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    @DisplayName("Should return error for invalid alert threshold")
    void createBudget_WithInvalidAlertThreshold_ShouldReturnErrorMessage() {
        // Given
        BigDecimal invalidThreshold = BigDecimal.valueOf(150.0);

        // When
        String result = budgetMcpService.createBudget("Food", BigDecimal.valueOf(500), 2025, 6, null, invalidThreshold);

        // Then
        assertThat(result).contains("❌ Alert threshold must be between 1 and 100");
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    @DisplayName("Should add transaction successfully with valid parameters")
    void addTransaction_WithValidParameters_ShouldReturnSuccessMessage() {
        // Given
        BigDecimal amount = BigDecimal.valueOf(45.50);
        String description = "Grocery shopping";
        String category = "Food";
        String type = "EXPENSE";
        String date = "2025-06-08";

        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // When
        String result = budgetMcpService.addTransaction(amount, description, category, type, date);

        // Then
        assertThat(result).contains("💸 Transaction added successfully!");
        assertThat(result).contains("$45.50");
        assertThat(result).contains("Grocery shopping");
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should use current date when date is null")
    void addTransaction_WithNullDate_ShouldUseCurrentDate() {
        // Given
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // When
        String result = budgetMcpService.addTransaction(
                BigDecimal.valueOf(100), "Test", "Food", "INCOME", null);

        // Then
        assertThat(result).contains("💰 Transaction added successfully!");
        verify(transactionRepository).save(argThat(transaction -> 
                transaction.getDate().equals(LocalDate.now())));
    }

    @Test
    @DisplayName("Should return error for invalid transaction amount")
    void addTransaction_WithInvalidAmount_ShouldReturnErrorMessage() {
        // Given
        BigDecimal invalidAmount = BigDecimal.valueOf(-50.00);

        // When
        String result = budgetMcpService.addTransaction(invalidAmount, "Test", "Food", "EXPENSE", null);

        // Then
        assertThat(result).contains("❌ Amount must be greater than 0");
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should return error for invalid transaction type")
    void addTransaction_WithInvalidType_ShouldReturnErrorMessage() {
        // When
        String result = budgetMcpService.addTransaction(
                BigDecimal.valueOf(100), "Test", "Food", "INVALID_TYPE", null);

        // Then
        assertThat(result).contains("❌ Invalid transaction type. Use INCOME or EXPENSE");
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should get all budgets with spending status")
    void getAllBudgets_WithActiveBudgets_ShouldReturnFormattedBudgets() {
        // Given
        List<Budget> budgets = Arrays.asList(testBudget);
        BigDecimal spent = BigDecimal.valueOf(200.00);

        when(budgetRepository.findAllActiveBudgets()).thenReturn(budgets);
        when(transactionRepository.sumExpensesByCategoryAndDateBetween(
                anyString(), any(LocalDate.class), any(LocalDate.class))).thenReturn(spent);

        // When
        String result = budgetMcpService.getAllBudgets();

        // Then
        assertThat(result).contains("📋 **Current Active Budgets:**");
        assertThat(result).contains("**Food**");
        assertThat(result).contains("$500.00");
        assertThat(result).contains("$200.00");
        assertThat(result).contains("40.0%");
    }

    @Test
    @DisplayName("Should return no budgets message when no active budgets")
    void getAllBudgets_WithNoBudgets_ShouldReturnNoBudgetsMessage() {
        // Given
        when(budgetRepository.findAllActiveBudgets()).thenReturn(Collections.emptyList());

        // When
        String result = budgetMcpService.getAllBudgets();

        // Then
        assertThat(result).contains("📋 No active budgets found");
    }

    @Test
    @DisplayName("Should get spending summary for specific category")
    void getSpendingSummary_WithCategory_ShouldReturnCategorySpending() {
        // Given
        String category = "Food";
        Integer year = 2025;
        Integer month = 6;
        BigDecimal spent = BigDecimal.valueOf(150.00);
        List<Transaction> transactions = Arrays.asList(testTransaction);

        when(transactionRepository.sumExpensesByCategoryAndDateBetween(
                anyString(), any(LocalDate.class), any(LocalDate.class))).thenReturn(spent);
        when(transactionRepository.findByCategoryAndDateBetween(
                anyString(), any(LocalDate.class), any(LocalDate.class))).thenReturn(transactions);

        // When
        String result = budgetMcpService.getSpendingSummary(category, year, month);

        // Then
        assertThat(result).contains("💳 **Food Spending for 6/2025:**");
        assertThat(result).contains("$150.00");
        assertThat(result).contains("1");
    }

    @Test
    @DisplayName("Should get overall spending summary when no category specified")
    void getSpendingSummary_WithoutCategory_ShouldReturnOverallSummary() {
        // Given
        Integer year = 2025;
        Integer month = 6;
        BigDecimal totalIncome = BigDecimal.valueOf(3000.00);
        BigDecimal totalExpenses = BigDecimal.valueOf(1500.00);

        when(transactionRepository.sumByTypeAndDateBetween(
                eq(TransactionType.INCOME), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(totalIncome);
        when(transactionRepository.sumByTypeAndDateBetween(
                eq(TransactionType.EXPENSE), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(totalExpenses);

        // When
        String result = budgetMcpService.getSpendingSummary(null, year, month);

        // Then
        assertThat(result).contains("📊 **Monthly Summary for 6/2025:**");
        assertThat(result).contains("$3000.00");
        assertThat(result).contains("$1500.00");
        assertThat(result).contains("$1500.00");
        assertThat(result).contains("Positive ✅");
    }

    @Test
    @DisplayName("Should get recent transactions")
    void getRecentTransactions_WithTransactions_ShouldReturnFormattedTransactions() {
        // Given
        List<Transaction> transactions = Arrays.asList(testTransaction);
        when(transactionRepository.findTop10ByOrderByCreatedAtDesc()).thenReturn(transactions);

        // When
        String result = budgetMcpService.getRecentTransactions();

        // Then
        assertThat(result).contains("📝 **Recent Transactions:**");
        assertThat(result).contains("💸 $45.50 - Grocery shopping");
        assertThat(result).contains("🏷️ Food");
    }

    @Test
    @DisplayName("Should return no transactions message when no transactions exist")
    void getRecentTransactions_WithNoTransactions_ShouldReturnNoTransactionsMessage() {
        // Given
        when(transactionRepository.findTop10ByOrderByCreatedAtDesc()).thenReturn(Collections.emptyList());

        // When
        String result = budgetMcpService.getRecentTransactions();

        // Then
        assertThat(result).contains("📝 No transactions found");
    }

    @Test
    @DisplayName("Should get structured budget summary")
    void getSummary_WithValidData_ShouldReturnBudgetSummary() {
        // Given
        Integer year = 2025;
        Integer month = 6;
        BigDecimal totalIncome = BigDecimal.valueOf(3000.00);
        BigDecimal totalExpenses = BigDecimal.valueOf(1500.00);

        when(transactionRepository.sumByTypeAndDateBetween(
                eq(TransactionType.INCOME), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(totalIncome);
        when(transactionRepository.sumByTypeAndDateBetween(
                eq(TransactionType.EXPENSE), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(totalExpenses);

        // When
        BudgetSummary result = budgetMcpService.getSummary(year, month);

        // Then
        assertThat(result.getTotalIncome()).isEqualTo(totalIncome);
        assertThat(result.getTotalExpenses()).isEqualTo(totalExpenses);
        assertThat(result.getNetAmount()).isEqualTo(BigDecimal.valueOf(1500.00));
    }

    @Test
    @DisplayName("Should handle null values in summary calculation")
    void getSummary_WithNullValues_ShouldReturnZeroValues() {
        // Given
        when(transactionRepository.sumByTypeAndDateBetween(
                any(TransactionType.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(null);

        // When
        BudgetSummary result = budgetMcpService.getSummary(2025, 6);

        // Then
        assertThat(result.getTotalIncome()).isEqualTo(BigDecimal.ZERO);
        assertThat(result.getTotalExpenses()).isEqualTo(BigDecimal.ZERO);
        assertThat(result.getNetAmount()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should use current date when year and month are null in summary")
    void getSummary_WithNullYearAndMonth_ShouldUseCurrentDate() {
        // Given
        LocalDate now = LocalDate.now();
        when(transactionRepository.sumByTypeAndDateBetween(
                any(TransactionType.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(BigDecimal.valueOf(1000));

        // When
        BudgetSummary result = budgetMcpService.getSummary(null, null);

        // Then
        assertThat(result).isNotNull();
        verify(transactionRepository, times(2)).sumByTypeAndDateBetween(
                any(TransactionType.class), 
                eq(LocalDate.of(now.getYear(), now.getMonth(), 1)),
                any(LocalDate.class));
    }

    @Test
    @DisplayName("Should handle exceptions gracefully in createBudget")
    void createBudget_WithException_ShouldReturnErrorMessage() {
        // Given
        when(budgetRepository.findByCategoryAndBudgetYearAndBudgetMonthAndIsActive(
                anyString(), anyInt(), anyInt(), anyBoolean()))
                .thenThrow(new RuntimeException("Database error"));

        // When
        String result = budgetMcpService.createBudget("Food", BigDecimal.valueOf(500), 2025, 6, null, null);

        // Then
        assertThat(result).contains("❌ Error creating budget");
        assertThat(result).contains("Database error");
    }

    @Test
    @DisplayName("Should handle exceptions gracefully in getSummary")
    void getSummary_WithException_ShouldReturnEmptySummary() {
        // Given
        when(transactionRepository.sumByTypeAndDateBetween(
                any(TransactionType.class), any(LocalDate.class), any(LocalDate.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When
        BudgetSummary result = budgetMcpService.getSummary(2025, 6);

        // Then
        assertThat(result).isEqualTo(BudgetSummary.empty());
    }
}
