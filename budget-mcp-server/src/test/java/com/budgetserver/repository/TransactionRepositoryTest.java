package com.budgetserver.repository;

import com.budgetserver.entity.Transaction;
import com.budgetserver.entity.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Transaction Repository Tests")
class TransactionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TransactionRepository transactionRepository;

    @MockitoBean
    private ToolCallbackProvider toolCallbackProvider;

    private Transaction expenseTransaction;
    private Transaction incomeTransaction;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.of(2025, 6, 1);
        endDate = LocalDate.of(2025, 6, 30);

        expenseTransaction = Transaction.builder()
                .amount(BigDecimal.valueOf(45.50))
                .description("Grocery shopping")
                .category("Food")
                .type(TransactionType.EXPENSE)
                .date(LocalDate.of(2025, 6, 15))
                .build();

        incomeTransaction = Transaction.builder()
                .amount(BigDecimal.valueOf(3000.00))
                .description("Salary")
                .category("Income")
                .type(TransactionType.INCOME)
                .date(LocalDate.of(2025, 6, 1))
                .build();
    }

    @Test
    @DisplayName("Should save and retrieve transaction")
    void saveTransaction_ShouldPersistAndRetrieve() {
        // When
        Transaction savedTransaction = transactionRepository.save(expenseTransaction);

        // Then
        assertThat(savedTransaction.getId()).isNotNull();
        assertThat(savedTransaction.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(45.50));
        assertThat(savedTransaction.getDescription()).isEqualTo("Grocery shopping");
        assertThat(savedTransaction.getCategory()).isEqualTo("Food");
        assertThat(savedTransaction.getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(savedTransaction.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should sum expenses by category and date range")
    void sumExpensesByCategoryAndDateBetween_ShouldReturnCorrectSum() {
        // Given
        Transaction expense1 = Transaction.builder()
                .amount(BigDecimal.valueOf(45.50))
                .description("Grocery shopping")
                .category("Food")
                .type(TransactionType.EXPENSE)
                .date(LocalDate.of(2025, 6, 15))
                .build();

        Transaction expense2 = Transaction.builder()
                .amount(BigDecimal.valueOf(25.00))
                .description("Restaurant")
                .category("Food")
                .type(TransactionType.EXPENSE)
                .date(LocalDate.of(2025, 6, 20))
                .build();

        entityManager.persist(expense1);
        entityManager.persist(expense2);
        entityManager.flush();

        // When
        BigDecimal sum = transactionRepository.sumExpensesByCategoryAndDateBetween(
                "Food", startDate, endDate);

        // Then
        assertThat(sum).isEqualByComparingTo(BigDecimal.valueOf(70.50));
    }

    @Test
    @DisplayName("Should find top 10 transactions ordered by created date desc")
    void findTop10ByOrderByCreatedAtDesc_ShouldReturnRecentTransactions() {
        // Given
        for (int i = 1; i <= 15; i++) {
            Transaction transaction = Transaction.builder()
                    .amount(BigDecimal.valueOf(i * 10))
                    .description("Transaction " + i)
                    .category("Test")
                    .type(TransactionType.EXPENSE)
                    .date(LocalDate.of(2025, 6, i))
                    .build();
            entityManager.persist(transaction);
            entityManager.flush();
        }

        // When
        List<Transaction> recentTransactions = transactionRepository.findTop10ByOrderByCreatedAtDesc();

        // Then
        assertThat(recentTransactions).hasSize(10);
        // Most recent should be first
        assertThat(recentTransactions.get(0).getDescription()).isEqualTo("Transaction 15");
    }

    @Test
    @DisplayName("Should sum transactions by type and date range")
    void sumByTypeAndDateBetween_ShouldReturnCorrectSum() {
        // Given
        entityManager.persist(expenseTransaction);
        entityManager.persist(incomeTransaction);
        
        Transaction anotherExpense = Transaction.builder()
                .amount(BigDecimal.valueOf(100.00))
                .description("Another expense")
                .category("Food")
                .type(TransactionType.EXPENSE)
                .date(LocalDate.of(2025, 6, 20))
                .build();
        entityManager.persist(anotherExpense);
        entityManager.flush();

        // When
        BigDecimal expenseSum = transactionRepository.sumByTypeAndDateBetween(
                TransactionType.EXPENSE, startDate, endDate);
        BigDecimal incomeSum = transactionRepository.sumByTypeAndDateBetween(
                TransactionType.INCOME, startDate, endDate);

        // Then
        assertThat(expenseSum).isEqualByComparingTo(BigDecimal.valueOf(145.50));
        assertThat(incomeSum).isEqualByComparingTo(BigDecimal.valueOf(3000.00));
    }

    @Test
    @DisplayName("Should find transactions by category and date range")
    void findByCategoryAndDateBetween_ShouldReturnMatchingTransactions() {
        // Given
        entityManager.persist(expenseTransaction);
        
        Transaction anotherFoodExpense = Transaction.builder()
                .amount(BigDecimal.valueOf(30.00))
                .description("Fast food")
                .category("Food")
                .type(TransactionType.EXPENSE)
                .date(LocalDate.of(2025, 6, 25))
                .build();
        entityManager.persist(anotherFoodExpense);
        
        Transaction transportExpense = Transaction.builder()
                .amount(BigDecimal.valueOf(50.00))
                .description("Gas")
                .category("Transportation")
                .type(TransactionType.EXPENSE)
                .date(LocalDate.of(2025, 6, 10))
                .build();
        entityManager.persist(transportExpense);
        entityManager.flush();

        // When
        List<Transaction> foodTransactions = transactionRepository.findByCategoryAndDateBetween(
                "Food", startDate, endDate);

        // Then
        assertThat(foodTransactions).hasSize(2);
        assertThat(foodTransactions).extracting(Transaction::getDescription)
                .containsExactlyInAnyOrder("Grocery shopping", "Fast food");
    }

    @Test
    @DisplayName("Should return null when no transactions found for sum operations")
    void sumOperations_WithNoTransactions_ShouldReturnNull() {
        // When
        BigDecimal expenseSum = transactionRepository.sumExpensesByCategoryAndDateBetween(
                "NonExistent", startDate, endDate);
        BigDecimal typeSum = transactionRepository.sumByTypeAndDateBetween(
                TransactionType.EXPENSE, startDate, endDate);

        // Then
        assertThat(expenseSum).isEqualTo(BigDecimal.valueOf(0));
        assertThat(typeSum).isEqualTo(BigDecimal.valueOf(0));
    }

    @Test
    @DisplayName("Should handle transactions on boundary dates")
    void transactionQueries_WithBoundaryDates_ShouldIncludeBoundaryTransactions() {
        // Given
        Transaction startDateTransaction = Transaction.builder()
                .amount(BigDecimal.valueOf(100.00))
                .description("Start date transaction")
                .category("Food")
                .type(TransactionType.EXPENSE)
                .date(startDate) // 2025-06-01
                .build();
        
        Transaction endDateTransaction = Transaction.builder()
                .amount(BigDecimal.valueOf(200.00))
                .description("End date transaction")
                .category("Food")
                .type(TransactionType.EXPENSE)
                .date(endDate) // 2025-06-30
                .build();

        entityManager.persist(startDateTransaction);
        entityManager.persist(endDateTransaction);
        entityManager.flush();

        // When
        BigDecimal sum = transactionRepository.sumExpensesByCategoryAndDateBetween(
                "Food", startDate, endDate);
        List<Transaction> transactions = transactionRepository.findByCategoryAndDateBetween(
                "Food", startDate, endDate);

        // Then
        assertThat(sum).isEqualByComparingTo(BigDecimal.valueOf(300.00));
        assertThat(transactions).hasSize(2);
    }

    @Test
    @DisplayName("Should exclude transactions outside date range")
    void transactionQueries_WithTransactionsOutsideDateRange_ShouldExcludeThem() {
        // Given
        Transaction beforeRange = Transaction.builder()
                .amount(BigDecimal.valueOf(100.00))
                .description("Before range")
                .category("Food")
                .type(TransactionType.EXPENSE)
                .date(LocalDate.of(2025, 5, 31)) // Day before start
                .build();
        
        Transaction afterRange = Transaction.builder()
                .amount(BigDecimal.valueOf(200.00))
                .description("After range")
                .category("Food")
                .type(TransactionType.EXPENSE)
                .date(LocalDate.of(2025, 7, 1)) // Day after end
                .build();

        Transaction inRange = Transaction.builder()
                .amount(BigDecimal.valueOf(50.00))
                .description("In range")
                .category("Food")
                .type(TransactionType.EXPENSE)
                .date(LocalDate.of(2025, 6, 15))
                .build();

        entityManager.persist(beforeRange);
        entityManager.persist(afterRange);
        entityManager.persist(inRange);
        entityManager.flush();

        // When
        BigDecimal sum = transactionRepository.sumExpensesByCategoryAndDateBetween(
                "Food", startDate, endDate);
        List<Transaction> transactions = transactionRepository.findByCategoryAndDateBetween(
                "Food", startDate, endDate);

        // Then
        assertThat(sum).isEqualByComparingTo(BigDecimal.valueOf(50.00));
        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0).getDescription()).isEqualTo("In range");
    }
}
