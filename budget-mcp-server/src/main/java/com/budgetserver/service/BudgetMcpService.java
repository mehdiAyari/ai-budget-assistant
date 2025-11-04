package com.budgetserver.service;


import com.budgetserver.dto.BudgetSummary;
import com.budgetserver.entity.Budget;
import com.budgetserver.entity.Transaction;
import com.budgetserver.entity.TransactionType;
import com.budgetserver.repository.BudgetRepository;
import com.budgetserver.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetMcpService {
    
    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    
    @Tool(description = "Create a new budget for a category with monthly limit and alert threshold")
    @Transactional
    public String createBudget(
            @ToolParam(description = "Budget category name (e.g., Food, Transportation)")
            String category,
            @ToolParam(description = "Monthly budget limit amount") 
            BigDecimal monthlyLimit,
            @ToolParam(description = "Budget year (default: current year)") 
            Integer year,
            @ToolParam(description = "Budget month (default: current month)") 
            Integer month,
            @ToolParam(description = "Optional notes about the budget") 
            String notes,
            @ToolParam(description = "Alert threshold percentage (default: 80%)") 
            BigDecimal alertThreshold) {
        
        try {
            // Set defaults
            if (year == null) year = LocalDate.now().getYear();
            if (month == null) month = LocalDate.now().getMonthValue();
            if (alertThreshold == null) alertThreshold = BigDecimal.valueOf(80);
            
            // Validate inputs
            if (monthlyLimit.compareTo(BigDecimal.ZERO) <= 0) {
                return "❌ Monthly limit must be greater than 0";
            }
            
            if (alertThreshold.compareTo(BigDecimal.ZERO) <= 0 || 
                alertThreshold.compareTo(BigDecimal.valueOf(100)) > 0) {
                return "❌ Alert threshold must be between 1 and 100";
            }
            
            // Check if budget already exists
            var existingBudget = budgetRepository.findByCategoryAndBudgetYearAndBudgetMonthAndIsActive(
                category, year, month, true);
            
            if (existingBudget.isPresent()) {
                return String.format("❌ Budget for %s already exists for %d/%d. Current limit: $%.2f", 
                    category, month, year, existingBudget.get().getMonthlyLimit());
            }
            
            // Create new budget
            Budget budget = Budget.builder()
                .category(category)
                .monthlyLimit(monthlyLimit)
                .budgetYear(year)
                .budgetMonth(month)
                .notes(notes)
                .alertThreshold(alertThreshold)
                .isActive(true)
                .build();
            
            budgetRepository.save(budget);

            log.info("🤖 AI calls tool: createBudget - Created budget for {} with limit ${}", category, monthlyLimit);
            
            return String.format("""
                ✅ Budget created successfully!
                📋 Category: %s
                💰 Monthly Limit: $%.2f
                📅 Period: %d/%d
                ⚠️ Alert Threshold: %.1f%%
                📝 Notes: %s
                """,
                category, monthlyLimit, month, year, alertThreshold, 
                notes != null ? notes : "None");
                
        } catch (Exception e) {
            log.error("🤖 AI tool call failed: createBudget - Error: {}", e.getMessage(), e);
            return "❌ Error creating budget: " + e.getMessage();
        }
    }
    
    @Tool(description = "Add a new income or expense transaction")
    @Transactional
    public String addTransaction(
            @ToolParam(description = "Transaction amount (positive number)") 
            BigDecimal amount,
            @ToolParam(description = "Description of the transaction") 
            String description,
            @ToolParam(description = "Transaction category") 
            String category,
            @ToolParam(description = "Transaction type: INCOME or EXPENSE") 
            String type,
            @ToolParam(description = "Transaction date in YYYY-MM-DD format (default: today)") 
            String date) {
        
        try {
            // Validate inputs
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return "❌ Amount must be greater than 0";
            }
            
            LocalDate transactionDate = date != null 
                ? LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
                : LocalDate.now();
                
            TransactionType transactionType = TransactionType.valueOf(type.toUpperCase());
            
            // Create transaction
            Transaction transaction = Transaction.builder()
                .amount(amount)
                .description(description)
                .category(category)
                .type(transactionType)
                .date(transactionDate)
                .build();
            
            transactionRepository.save(transaction);

            log.info("🤖 AI calls tool: addTransaction - Added {} transaction: {} ${} in {}", type, description, amount, category);
            
            String emoji = transactionType == TransactionType.INCOME ? "💰" : "💸";
            return String.format("""
                %s Transaction added successfully!
                💵 Amount: $%.2f
                📝 Description: %s
                🏷️ Category: %s
                📅 Date: %s
                🔄 Type: %s
                """,
                emoji, amount, description, category, 
                transactionDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")), type);
                
        } catch (IllegalArgumentException e) {
            log.warn("🤖 AI tool call rejected: addTransaction - Invalid transaction type: {}", e.getMessage());
            return "❌ Invalid transaction type. Use INCOME or EXPENSE";
        } catch (Exception e) {
            log.error("🤖 AI tool call failed: addTransaction - Error: {}", e.getMessage(), e);
            return "❌ Error adding transaction: " + e.getMessage();
        }
    }
    
    @Tool(description = "Get all active budgets with current spending status")
    public String getAllBudgets() {
        try {
            log.info("🤖 AI calls tool: getAllBudgets - Retrieving all active budgets");
            List<Budget> budgets = budgetRepository.findAllActiveBudgets();
            
            if (budgets.isEmpty()) {
                return "📋 No active budgets found. Create your first budget to get started!";
            }
            
            StringBuilder response = new StringBuilder("📋 **Current Active Budgets:**\n\n");
            
            for (Budget budget : budgets) {
                LocalDate startDate = LocalDate.of(budget.getBudgetYear(), budget.getBudgetMonth(), 1);
                LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
                
                BigDecimal spent = transactionRepository.sumExpensesByCategoryAndDateBetween(
                    budget.getCategory(), startDate, endDate);
                
                BigDecimal remaining = budget.getMonthlyLimit().subtract(spent);
                BigDecimal percentUsed = spent.divide(budget.getMonthlyLimit(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
                
                String status = percentUsed.compareTo(budget.getAlertThreshold()) >= 0 ? "⚠️" : "✅";
                
                response.append(String.format("""
                    %s **%s**
                      💰 Budget: $%.2f
                      💸 Spent: $%.2f (%.1f%%)
                      💵 Remaining: $%.2f
                      📅 Period: %d/%d
                    """, 
                    status, budget.getCategory(), budget.getMonthlyLimit(), 
                    spent, percentUsed, remaining, budget.getBudgetMonth(), budget.getBudgetYear()));
                
                if (budget.getNotes() != null && !budget.getNotes().trim().isEmpty()) {
                    response.append(String.format("      📝 Notes: %s\n", budget.getNotes()));
                }
                response.append("\n");
            }
            
            return response.toString();

        } catch (Exception e) {
            log.error("🤖 AI tool call failed: getAllBudgets - Error: {}", e.getMessage(), e);
            return "❌ Error retrieving budgets: " + e.getMessage();
        }
    }

    @Tool(description = "Get spending summary for a specific month or category")
    public String getSpendingSummary(
            @ToolParam(description = "Category to filter by (optional)")
            String category,
            @ToolParam(description = "Year (default: current year)")
            Integer year,
            @ToolParam(description = "Month (default: current month)")
            Integer month) {

        try {
            log.info("🤖 AI calls tool: getSpendingSummary - Category: {}, Period: {}/{}",
                    category != null ? category : "All",
                    month != null ? month : LocalDate.now().getMonthValue(),
                    year != null ? year : LocalDate.now().getYear());
            if (year == null) year = LocalDate.now().getYear();
            if (month == null) month = LocalDate.now().getMonthValue();
            
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
            
            if (category != null && !category.trim().isEmpty()) {
                // Category-specific spending
                BigDecimal spent = transactionRepository.sumExpensesByCategoryAndDateBetween(
                    category, startDate, endDate);
                    
                List<Transaction> transactions = transactionRepository.findByCategoryAndDateBetween(
                    category, startDate, endDate);
                
                return String.format("""
                    💳 **%s Spending for %d/%d:**
                    
                    💸 Total Spent: $%.2f
                    📊 Number of Transactions: %d
                    """,
                    category, month, year, spent, transactions.size());
            } else {
                // Overall spending summary
                BigDecimal totalIncome = transactionRepository.sumByTypeAndDateBetween(
                    TransactionType.INCOME, startDate, endDate);
                BigDecimal totalExpenses = transactionRepository.sumByTypeAndDateBetween(
                    TransactionType.EXPENSE, startDate, endDate);
                BigDecimal netAmount = totalIncome.subtract(totalExpenses);
                
                return String.format("""
                    📊 **Monthly Summary for %d/%d:**
                    
                    💰 Total Income: $%.2f
                    💸 Total Expenses: $%.2f
                    💵 Net Amount: $%.2f
                    📈 Status: %s
                    """,
                    month, year, totalIncome, totalExpenses, netAmount,
                    netAmount.compareTo(BigDecimal.ZERO) >= 0 ? "Positive ✅" : "Negative ⚠️");
            }

        } catch (Exception e) {
            log.error("🤖 AI tool call failed: getSpendingSummary - Error: {}", e.getMessage(), e);
            return "❌ Error getting spending summary: " + e.getMessage();
        }
    }

    @Tool(description = "Get recent transactions (last 10)")
    public String getRecentTransactions() {
        try {
            log.info("🤖 AI calls tool: getRecentTransactions - Fetching last 10 transactions");
            List<Transaction> transactions = transactionRepository.findTop10ByOrderByCreatedAtDesc();
            
            if (transactions.isEmpty()) {
                return "📝 No transactions found. Add your first transaction to get started!";
            }
            
            StringBuilder response = new StringBuilder("📝 **Recent Transactions:**\n\n");
            
            for (Transaction transaction : transactions) {
                String emoji = transaction.getType() == TransactionType.INCOME ? "💰" : "💸";
                response.append(String.format("""
                    %s $%.2f - %s
                      🏷️ %s | 📅 %s
                    """,
                    emoji, transaction.getAmount(), transaction.getDescription(),
                    transaction.getCategory(),
                    transaction.getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))));
            }
            
            return response.toString();

        } catch (Exception e) {
            log.error("🤖 AI tool call failed: getRecentTransactions - Error: {}", e.getMessage(), e);
            return "❌ Error getting recent transactions: " + e.getMessage();
        }
    }

    @Tool(description = "Get budget summary with totals as structured data")
    public BudgetSummary getSummary(
            @ToolParam(description = "Year (default: current year)")
            Integer year,
            @ToolParam(description = "Month (default: current month)")
            Integer month) {

        try {
            if (year == null) year = LocalDate.now().getYear();
            if (month == null) month = LocalDate.now().getMonthValue();

            log.info("🤖 AI calls tool: getSummary - Getting structured budget summary for {}/{}", month, year);

            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

            // Get totals from database
            BigDecimal totalIncome = transactionRepository.sumByTypeAndDateBetween(
                    TransactionType.INCOME, startDate, endDate);

            BigDecimal totalExpenses = transactionRepository.sumByTypeAndDateBetween(
                    TransactionType.EXPENSE, startDate, endDate);

            // Handle null values (when no transactions exist)
            if (totalIncome == null) totalIncome = BigDecimal.ZERO;
            if (totalExpenses == null) totalExpenses = BigDecimal.ZERO;

            BigDecimal netAmount = totalIncome.subtract(totalExpenses);

            BudgetSummary summary = new BudgetSummary(totalIncome, totalExpenses, netAmount);

            log.info("🤖 AI tool result: getSummary - Income=${}, Expenses=${}, Net=${} for {}/{}",
                    totalIncome, totalExpenses, netAmount, month, year);

            return summary;

        } catch (Exception e) {
            log.error("🤖 AI tool call failed: getSummary - Error: {}", e.getMessage(), e);
            return BudgetSummary.empty();
        }
    }
}