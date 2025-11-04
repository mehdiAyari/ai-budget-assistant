package com.budgetserver.repository;

import com.budgetserver.entity.Budget;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Budget Repository Tests")
class BudgetRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BudgetRepository budgetRepository;

    @MockitoBean
    private ToolCallbackProvider toolCallbackProvider;

    private Budget testBudget;

    @BeforeEach
    void setUp() {
        testBudget = Budget.builder()
                .category("Food")
                .monthlyLimit(BigDecimal.valueOf(500.00))
                .budgetYear(2025)
                .budgetMonth(6)
                .alertThreshold(BigDecimal.valueOf(80.0))
                .isActive(true)
                .notes("Test budget for food")
                .build();
    }

    @Test
    @DisplayName("Should save and retrieve budget")
    void saveBudget_ShouldPersistAndRetrieve() {
        // When
        Budget savedBudget = budgetRepository.save(testBudget);

        // Then
        assertThat(savedBudget.getId()).isNotNull();
        assertThat(savedBudget.getCategory()).isEqualTo("Food");
        assertThat(savedBudget.getMonthlyLimit()).isEqualByComparingTo(BigDecimal.valueOf(500.00));
        assertThat(savedBudget.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Should find budget by category, year, month and active status")
    void findByCategoryAndBudgetYearAndBudgetMonthAndIsActive_ShouldReturnBudget() {
        // Given
        entityManager.persistAndFlush(testBudget);

        // When
        Optional<Budget> foundBudget = budgetRepository.findByCategoryAndBudgetYearAndBudgetMonthAndIsActive(
                "Food", 2025, 6, true);

        // Then
        assertThat(foundBudget).isPresent();
        assertThat(foundBudget.get().getCategory()).isEqualTo("Food");
        assertThat(foundBudget.get().getBudgetYear()).isEqualTo(2025);
        assertThat(foundBudget.get().getBudgetMonth()).isEqualTo(6);
        assertThat(foundBudget.get().getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Should not find budget when not active")
    void findByCategoryAndBudgetYearAndBudgetMonthAndIsActive_WithInactiveBudget_ShouldReturnEmpty() {
        // Given
        testBudget.setIsActive(false);
        entityManager.persistAndFlush(testBudget);

        // When
        Optional<Budget> foundBudget = budgetRepository.findByCategoryAndBudgetYearAndBudgetMonthAndIsActive(
                "Food", 2025, 6, true);

        // Then
        assertThat(foundBudget).isEmpty();
    }

    @Test
    @DisplayName("Should find all active budgets")
    void findAllActiveBudgets_ShouldReturnOnlyActiveBudgets() {
        // Given
        Budget activeBudget1 = Budget.builder()
                .category("Food")
                .monthlyLimit(BigDecimal.valueOf(500.00))
                .budgetYear(2025)
                .budgetMonth(6)
                .alertThreshold(BigDecimal.valueOf(80.0))
                .isActive(true)
                .build();

        Budget activeBudget2 = Budget.builder()
                .category("Transportation")
                .monthlyLimit(BigDecimal.valueOf(300.00))
                .budgetYear(2025)
                .budgetMonth(6)
                .alertThreshold(BigDecimal.valueOf(75.0))
                .isActive(true)
                .build();

        Budget inactiveBudget = Budget.builder()
                .category("Entertainment")
                .monthlyLimit(BigDecimal.valueOf(200.00))
                .budgetYear(2025)
                .budgetMonth(6)
                .alertThreshold(BigDecimal.valueOf(90.0))
                .isActive(false)
                .build();

        entityManager.persist(activeBudget1);
        entityManager.persist(activeBudget2);
        entityManager.persist(inactiveBudget);
        entityManager.flush();

        // When
        List<Budget> activeBudgets = budgetRepository.findAllActiveBudgets();

        // Then
        assertThat(activeBudgets).hasSize(2);
        assertThat(activeBudgets).extracting(Budget::getCategory)
                .containsExactlyInAnyOrder("Food", "Transportation");
        assertThat(activeBudgets).allMatch(Budget::getIsActive);
    }

    @Test
    @DisplayName("Should return empty list when no active budgets exist")
    void findAllActiveBudgets_WithNoActiveBudgets_ShouldReturnEmptyList() {
        // Given
        testBudget.setIsActive(false);
        entityManager.persistAndFlush(testBudget);

        // When
        List<Budget> activeBudgets = budgetRepository.findAllActiveBudgets();

        // Then
        assertThat(activeBudgets).isEmpty();
    }

    @Test
    @DisplayName("Should handle budget with null notes")
    void saveBudget_WithNullNotes_ShouldSaveSuccessfully() {
        // Given
        testBudget.setNotes(null);

        // When
        Budget savedBudget = budgetRepository.save(testBudget);

        // Then
        assertThat(savedBudget.getNotes()).isNull();
        assertThat(savedBudget.getId()).isNotNull();
    }

    @Test
    @DisplayName("Should handle budget with decimal alert threshold")
    void saveBudget_WithDecimalAlertThreshold_ShouldSaveCorrectly() {
        // Given
        testBudget.setAlertThreshold(BigDecimal.valueOf(85.5));

        // When
        Budget savedBudget = budgetRepository.save(testBudget);

        // Then
        assertThat(savedBudget.getAlertThreshold()).isEqualByComparingTo(BigDecimal.valueOf(85.5));
    }

    @Test
    @DisplayName("Should find budget by exact category match (case sensitive)")
    void findByCategoryAndBudgetYearAndBudgetMonthAndIsActive_CaseSensitive_ShouldNotMatch() {
        // Given
        entityManager.persistAndFlush(testBudget);

        // When
        Optional<Budget> foundBudget = budgetRepository.findByCategoryAndBudgetYearAndBudgetMonthAndIsActive(
                "food", 2025, 6, true); // lowercase

        // Then
        assertThat(foundBudget).isEmpty();
    }

    @Test
    @DisplayName("Should support multiple budgets for same category in different months")
    void findByCategoryAndBudgetYearAndBudgetMonthAndIsActive_DifferentMonths_ShouldFindCorrectBudget() {
        // Given
        Budget juneBudget = Budget.builder()
                .category("Food")
                .monthlyLimit(BigDecimal.valueOf(500.00))
                .budgetYear(2025)
                .budgetMonth(6)
                .alertThreshold(BigDecimal.valueOf(80.0))
                .isActive(true)
                .build();

        Budget julyBudget = Budget.builder()
                .category("Food")
                .monthlyLimit(BigDecimal.valueOf(600.00))
                .budgetYear(2025)
                .budgetMonth(7)
                .alertThreshold(BigDecimal.valueOf(85.0))
                .isActive(true)
                .build();

        entityManager.persist(juneBudget);
        entityManager.persist(julyBudget);
        entityManager.flush();

        // When
        Optional<Budget> juneFound = budgetRepository.findByCategoryAndBudgetYearAndBudgetMonthAndIsActive(
                "Food", 2025, 6, true);
        Optional<Budget> julyFound = budgetRepository.findByCategoryAndBudgetYearAndBudgetMonthAndIsActive(
                "Food", 2025, 7, true);

        // Then
        assertThat(juneFound).isPresent();
        assertThat(julyFound).isPresent();
        assertThat(juneFound.get().getMonthlyLimit()).isEqualByComparingTo(BigDecimal.valueOf(500.00));
        assertThat(julyFound.get().getMonthlyLimit()).isEqualByComparingTo(BigDecimal.valueOf(600.00));
    }
}
