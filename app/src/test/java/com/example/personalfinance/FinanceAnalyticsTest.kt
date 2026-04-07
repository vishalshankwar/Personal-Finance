package com.example.personalfinance

import com.example.personalfinance.domain.FinanceAnalytics
import com.example.personalfinance.model.FinanceTransaction
import com.example.personalfinance.model.SavingsGoalConfig
import com.example.personalfinance.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class FinanceAnalyticsTest {

    @Test
    fun dashboardSummary_calculatesBalanceIncomeExpenses() {
        val transactions = listOf(
            FinanceTransaction(amount = 20000.0, type = TransactionType.INCOME, category = "Salary", date = LocalDate.of(2026, 4, 1)),
            FinanceTransaction(amount = 5000.0, type = TransactionType.INCOME, category = "Freelance", date = LocalDate.of(2026, 4, 2)),
            FinanceTransaction(amount = 7000.0, type = TransactionType.EXPENSE, category = "Rent", date = LocalDate.of(2026, 4, 2)),
            FinanceTransaction(amount = 3000.0, type = TransactionType.EXPENSE, category = "Food", date = LocalDate.of(2026, 4, 3))
        )

        val summary = FinanceAnalytics.buildDashboardSummary(transactions)

        assertEquals(25000.0, summary.totalIncome, 0.0)
        assertEquals(10000.0, summary.totalExpenses, 0.0)
        assertEquals(15000.0, summary.currentBalance, 0.0)
        assertEquals(60, summary.savingsRate)
    }

    @Test
    fun goalProgress_tracksMonthlySavingsAgainstTarget() {
        val today = LocalDate.of(2026, 4, 10)
        val transactions = listOf(
            FinanceTransaction(amount = 30000.0, type = TransactionType.INCOME, category = "Salary", date = LocalDate.of(2026, 4, 1)),
            FinanceTransaction(amount = 4000.0, type = TransactionType.EXPENSE, category = "Rent", date = LocalDate.of(2026, 4, 3)),
            FinanceTransaction(amount = 1500.0, type = TransactionType.EXPENSE, category = "Food", date = LocalDate.of(2026, 4, 5))
        )

        val progress = FinanceAnalytics.buildGoalProgress(
            transactions = transactions,
            goalConfig = SavingsGoalConfig(monthlyTarget = 20000.0),
            today = today
        )

        assertEquals(24500.0, progress.savedThisMonth, 0.0)
        assertEquals(100, progress.progressPercent)
        assertEquals(0.0, progress.remainingAmount, 0.0)
    }
}
