package com.budgetserver.repository;

import com.budgetserver.entity.Transaction;
import com.budgetserver.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByCategoryAndDateBetween(String category, LocalDate startDate, LocalDate endDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.type = :type AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumByTypeAndDateBetween(
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.category = :category AND t.type = 'EXPENSE' " +
            "AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumExpensesByCategoryAndDateBetween(
            @Param("category") String category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    List<Transaction> findTop10ByOrderByCreatedAtDesc();
}
