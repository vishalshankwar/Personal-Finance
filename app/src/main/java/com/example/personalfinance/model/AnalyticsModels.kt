package com.example.personalfinance.model

data class DashboardSummary(
    val currentBalance: Double,
    val totalIncome: Double,
    val totalExpenses: Double,
    val savingsRate: Int
)

data class WeeklySpend(
    val label: String,
    val amount: Double
)

data class CategorySummary(
    val category: String,
    val amount: Double,
    val percentage: Int
)

data class InsightsSummary(
    val topCategory: String,
    val topCategoryAmount: Double,
    val thisWeekSpend: Double,
    val lastWeekSpend: Double,
    val busiestExpenseDayLabel: String,
    val busiestExpenseDayAmount: Double,
    val noSpendDaysThisMonth: Int,
    val currentStreak: Int
)

data class GoalProgress(
    val target: Double,
    val savedThisMonth: Double,
    val progressPercent: Int,
    val remainingAmount: Double,
    val paceMessage: String,
    val noSpendDaysThisMonth: Int,
    val currentStreak: Int
)
