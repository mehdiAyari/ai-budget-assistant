package com.budgetclient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetSummary {
    
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netAmount;
    
    public static BudgetSummary empty() {
        return new BudgetSummary(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }
}
