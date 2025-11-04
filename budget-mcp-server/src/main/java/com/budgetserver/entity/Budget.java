package com.budgetserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "budgets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String category;
    
    @Column(name = "monthly_limit", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyLimit;
    
    @Column(name = "budget_year", nullable = false)
    private Integer budgetYear;
    
    @Column(name = "budget_month", nullable = false)
    private Integer budgetMonth;
    
    @Column(length = 500)
    private String notes;
    
    @Column(name = "alert_threshold", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal alertThreshold = BigDecimal.valueOf(80.0);
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
