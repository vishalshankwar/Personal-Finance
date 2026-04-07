package com.example.personalfinance.ui.main

import com.example.personalfinance.model.CategorySummary
import com.example.personalfinance.model.DashboardSummary
import com.example.personalfinance.model.FinanceTransaction
import com.example.personalfinance.model.GoalProgress
import com.example.personalfinance.model.InsightsSummary
import com.example.personalfinance.model.TransactionType
import com.example.personalfinance.model.WeeklySpend

data class MainUiState(
    val currentScreen: MainScreen = MainScreen.DASHBOARD,
    val dashboardSummary: DashboardSummary = DashboardSummary(
        currentBalance = 0.0,
        totalIncome = 0.0,
        totalExpenses = 0.0,
        savingsRate = 0
    ),
    val weeklySpend: List<WeeklySpend> = emptyList(),
    val dashboardCategories: List<CategorySummary> = emptyList(),
    val filteredTransactions: List<FinanceTransaction> = emptyList(),
    val selectedTransactionFilter: TransactionType? = null,
    val currentQuery: String = "",
    val goalProgress: GoalProgress = GoalProgress(
        target = 0.0,
        savedThisMonth = 0.0,
        progressPercent = 0,
        remainingAmount = 0.0,
        paceMessage = "",
        noSpendDaysThisMonth = 0,
        currentStreak = 0
    ),
    val insights: InsightsSummary = InsightsSummary(
        topCategory = "No expense data yet",
        topCategoryAmount = 0.0,
        thisWeekSpend = 0.0,
        lastWeekSpend = 0.0,
        busiestExpenseDayLabel = "N/A",
        busiestExpenseDayAmount = 0.0,
        noSpendDaysThisMonth = 0,
        currentStreak = 0
    ),
    val insightCategories: List<CategorySummary> = emptyList()
) {
    val transactionHelperText: String
        get() = "${filteredTransactions.size} item${if (filteredTransactions.size == 1) "" else "s"} shown"

    val showEmptyTransactions: Boolean
        get() = filteredTransactions.isEmpty()
}
