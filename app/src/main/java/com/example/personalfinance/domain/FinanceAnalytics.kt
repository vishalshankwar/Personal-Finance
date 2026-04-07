package com.example.personalfinance.domain

import com.example.personalfinance.model.CategorySummary
import com.example.personalfinance.model.DashboardSummary
import com.example.personalfinance.model.FinanceTransaction
import com.example.personalfinance.model.GoalProgress
import com.example.personalfinance.model.InsightsSummary
import com.example.personalfinance.model.SavingsGoalConfig
import com.example.personalfinance.model.TransactionType
import com.example.personalfinance.model.WeeklySpend
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.abs
import kotlin.math.roundToInt

object FinanceAnalytics {

    fun buildDashboardSummary(transactions: List<FinanceTransaction>): DashboardSummary {
        val income = totalByType(transactions, TransactionType.INCOME)
        val expenses = totalByType(transactions, TransactionType.EXPENSE)
        val balance = income - expenses
        val savingsRate = if (income > 0) {
            (((balance / income) * 100).coerceAtLeast(0.0)).roundToInt()
        } else {
            0
        }
        return DashboardSummary(
            currentBalance = balance,
            totalIncome = income,
            totalExpenses = expenses,
            savingsRate = savingsRate
        )
    }

    fun buildWeeklySpend(transactions: List<FinanceTransaction>, today: LocalDate = LocalDate.now()): List<WeeklySpend> {
        return (6 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            val amount = transactions
                .filter { it.type == TransactionType.EXPENSE && it.date == date }
                .sumOf { it.amount }
            WeeklySpend(label = date.dayOfWeek.shortLabel(), amount = amount)
        }
    }

    fun buildCategoryBreakdown(
        transactions: List<FinanceTransaction>,
        month: YearMonth = YearMonth.now()
    ): List<CategorySummary> {
        val monthlyExpenses = transactions.filter {
            it.type == TransactionType.EXPENSE && YearMonth.from(it.date) == month
        }
        val total = monthlyExpenses.sumOf { it.amount }
        if (total <= 0.0) return emptyList()

        return monthlyExpenses
            .groupBy { it.category }
            .map { (category, entries) ->
                val amount = entries.sumOf { it.amount }
                CategorySummary(
                    category = category,
                    amount = amount,
                    percentage = ((amount / total) * 100).roundToInt()
                )
            }
            .sortedByDescending { it.amount }
    }

    fun buildGoalProgress(
        transactions: List<FinanceTransaction>,
        goalConfig: SavingsGoalConfig,
        today: LocalDate = LocalDate.now()
    ): GoalProgress {
        val currentMonth = YearMonth.from(today)
        val monthlyTransactions = transactions.filter { YearMonth.from(it.date) == currentMonth }
        val monthlyIncome = totalByType(monthlyTransactions, TransactionType.INCOME)
        val monthlyExpenses = totalByType(monthlyTransactions, TransactionType.EXPENSE)
        val savedThisMonth = (monthlyIncome - monthlyExpenses).coerceAtLeast(0.0)
        val progressPercent = if (goalConfig.monthlyTarget > 0) {
            ((savedThisMonth / goalConfig.monthlyTarget) * 100).coerceIn(0.0, 100.0).roundToInt()
        } else {
            0
        }
        val remaining = (goalConfig.monthlyTarget - savedThisMonth).coerceAtLeast(0.0)
        val elapsedRatio = today.dayOfMonth.toDouble() / currentMonth.lengthOfMonth().toDouble()
        val expectedSoFar = goalConfig.monthlyTarget * elapsedRatio
        val paceMessage = when {
            savedThisMonth >= goalConfig.monthlyTarget -> "Goal achieved for this month"
            savedThisMonth >= expectedSoFar -> "On pace for your monthly target"
            else -> "Behind pace by ${currencyCompact(expectedSoFar - savedThisMonth)}"
        }

        return GoalProgress(
            target = goalConfig.monthlyTarget,
            savedThisMonth = savedThisMonth,
            progressPercent = progressPercent,
            remainingAmount = remaining,
            paceMessage = paceMessage,
            noSpendDaysThisMonth = noSpendDaysThisMonth(transactions, today),
            currentStreak = currentNoSpendStreak(transactions, today)
        )
    }

    fun buildInsights(
        transactions: List<FinanceTransaction>,
        today: LocalDate = LocalDate.now()
    ): InsightsSummary {
        val breakdown = buildCategoryBreakdown(transactions)
        val topCategory = breakdown.firstOrNull()
        val thisWeek = expenseTotalBetween(transactions, today.minusDays(6), today)
        val lastWeek = expenseTotalBetween(transactions, today.minusDays(13), today.minusDays(7))
        val busiestDay = (0..6)
            .map { today.minusDays(it.toLong()) }
            .associateWith { date ->
                transactions
                    .filter { it.type == TransactionType.EXPENSE && it.date == date }
                    .sumOf { it.amount }
            }
            .maxByOrNull { it.value }

        return InsightsSummary(
            topCategory = topCategory?.category ?: "No expense data yet",
            topCategoryAmount = topCategory?.amount ?: 0.0,
            thisWeekSpend = thisWeek,
            lastWeekSpend = lastWeek,
            busiestExpenseDayLabel = busiestDay?.key?.dayOfWeek?.shortLabel() ?: "N/A",
            busiestExpenseDayAmount = busiestDay?.value ?: 0.0,
            noSpendDaysThisMonth = noSpendDaysThisMonth(transactions, today),
            currentStreak = currentNoSpendStreak(transactions, today)
        )
    }

    fun filteredTransactions(
        transactions: List<FinanceTransaction>,
        query: String,
        selectedFilter: TransactionType?
    ): List<FinanceTransaction> {
        return transactions
            .withIndex()
            .filter {
                (selectedFilter == null || it.value.type == selectedFilter) &&
                    (query.isBlank() ||
                        it.value.category.contains(query, ignoreCase = true) ||
                        it.value.notes.contains(query, ignoreCase = true))
            }
            .sortedWith(
                compareByDescending<IndexedValue<FinanceTransaction>> { it.value.date }
                    .thenByDescending { it.index }
            )
            .map { it.value }
    }

    private fun totalByType(
        transactions: List<FinanceTransaction>,
        type: TransactionType
    ): Double = transactions.filter { it.type == type }.sumOf { it.amount }

    private fun expenseTotalBetween(
        transactions: List<FinanceTransaction>,
        startInclusive: LocalDate,
        endInclusive: LocalDate
    ): Double {
        return transactions
            .filter { it.type == TransactionType.EXPENSE && !it.date.isBefore(startInclusive) && !it.date.isAfter(endInclusive) }
            .sumOf { it.amount }
    }

    private fun noSpendDaysThisMonth(transactions: List<FinanceTransaction>, today: LocalDate): Int {
        val month = YearMonth.from(today)
        val expenseDates = transactions
            .filter { it.type == TransactionType.EXPENSE && YearMonth.from(it.date) == month }
            .map { it.date }
            .toSet()
        return (1..today.dayOfMonth)
            .map { month.atDay(it) }
            .count { it !in expenseDates }
    }

    private fun currentNoSpendStreak(transactions: List<FinanceTransaction>, today: LocalDate): Int {
        val expenseDates = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .map { it.date }
            .toSet()
        val earliestTransactionDate = transactions.minOfOrNull { it.date } ?: today
        var streak = 0
        var cursor = today
        while (!cursor.isBefore(earliestTransactionDate) && cursor !in expenseDates) {
            streak += 1
            cursor = cursor.minusDays(1)
        }
        return streak
    }

    private fun DayOfWeek.shortLabel(): String = name.take(3).lowercase().replaceFirstChar { it.uppercase() }

    private fun currencyCompact(amount: Double): String {
        val absolute = abs(amount)
        return when {
            absolute >= 1000 -> "â‚¹${(absolute / 1000.0).roundToInt()}k"
            else -> "â‚¹${absolute.roundToInt()}"
        }
    }
}
