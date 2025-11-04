package com.budgetserver.repository;

import com.budgetserver.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    Optional<Budget> findByCategoryAndBudgetYearAndBudgetMonthAndIsActive(
            String category, Integer year, Integer month, Boolean isActive);

    @Query("SELECT b FROM Budget b WHERE b.isActive = true ORDER BY b.category")
    List<Budget> findAllActiveBudgets();
}
