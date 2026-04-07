package com.example.personalfinance.data

import android.content.Context
import com.example.personalfinance.data.local.AppDatabase
import com.example.personalfinance.data.local.GoalEntity
import com.example.personalfinance.data.local.TransactionEntity
import com.example.personalfinance.model.FinanceTransaction
import com.example.personalfinance.model.SavingsGoalConfig
import com.example.personalfinance.model.TransactionType
import org.json.JSONArray
import java.time.LocalDate

class FinanceRepository(context: Context) {

    private val legacyPreferences = context.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
    private val financeDao = AppDatabase.getInstance(context).financeDao()

    fun loadTransactions(): MutableList<FinanceTransaction> {
        // Room is the current source of truth. SharedPreferences is read only for migration.
        migrateLegacyTransactionsIfNeeded()
        return financeDao.getTransactions().map { it.toModel() }.toMutableList()
    }

    fun saveTransactions(transactions: List<FinanceTransaction>) {
        val existingIds = financeDao.getTransactions().map { it.id }.toSet()
        val incomingIds = transactions.map { it.id }.toSet()
        existingIds.subtract(incomingIds).forEach(financeDao::deleteTransaction)
        transactions.forEach { financeDao.upsertTransaction(it.toEntity()) }
    }

    fun loadGoalConfig(): SavingsGoalConfig {
        migrateLegacyGoalIfNeeded()
        val goal = financeDao.getGoal()
        if (goal == null) {
            val defaultGoal = SavingsGoalConfig()
            saveGoalConfig(defaultGoal)
            return defaultGoal
        }
        return SavingsGoalConfig(monthlyTarget = goal.monthlyTarget)
    }

    fun saveGoalConfig(config: SavingsGoalConfig) {
        financeDao.upsertGoal(
            GoalEntity(
                monthlyTarget = config.monthlyTarget
            )
        )
    }

    private fun FinanceTransaction.toEntity(): TransactionEntity {
        return TransactionEntity(
            id = id,
            amount = amount,
            type = type,
            category = category,
            date = date,
            notes = notes
        )
    }

    private fun TransactionEntity.toModel(): FinanceTransaction {
        return FinanceTransaction(
            id = id,
            amount = amount,
            type = type,
            category = category,
            date = date,
            notes = notes
        )
    }

    private fun migrateLegacyTransactionsIfNeeded() {
        if (financeDao.getTransactionCount() > 0) return

        val raw = legacyPreferences.getString(KEY_TRANSACTIONS, null)
        if (raw.isNullOrBlank()) return

        val array = JSONArray(raw)
        val transactions = MutableList(array.length()) { index ->
            val item = array.getJSONObject(index)
            TransactionEntity(
                id = item.getString("id"),
                amount = item.getDouble("amount"),
                type = TransactionType.valueOf(item.getString("type")),
                category = item.getString("category"),
                date = LocalDate.parse(item.getString("date")),
                notes = item.optString("notes")
            )
        }
        financeDao.insertTransactions(transactions)
    }

    private fun migrateLegacyGoalIfNeeded() {
        if (financeDao.getGoal() != null) return

        val raw = legacyPreferences.getString(KEY_GOAL_CONFIG, null) ?: return
        financeDao.upsertGoal(
            GoalEntity(
                monthlyTarget = org.json.JSONObject(raw).optDouble("monthlyTarget", 12000.0)
            )
        )
    }

    companion object {
        private const val LEGACY_PREFS_NAME = "finance_companion"
        private const val KEY_TRANSACTIONS = "transactions"
        private const val KEY_GOAL_CONFIG = "goal_config"
    }
}
