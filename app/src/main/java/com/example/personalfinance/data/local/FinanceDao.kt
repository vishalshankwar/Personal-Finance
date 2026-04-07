package com.example.personalfinance.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface FinanceDao {

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getTransactions(): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTransactions(transactions: List<TransactionEntity>)

    @Query("DELETE FROM transactions WHERE id = :transactionId")
    fun deleteTransaction(transactionId: String)

    @Query("SELECT COUNT(*) FROM transactions")
    fun getTransactionCount(): Int

    @Query("SELECT * FROM goal_config WHERE id = 1")
    fun getGoal(): GoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertGoal(goal: GoalEntity)

    @Query("SELECT * FROM auth WHERE id = 1")
    fun getAuth(): AuthEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAuth(auth: AuthEntity)

    @Query("SELECT * FROM theme_preferences WHERE id = 1")
    fun getThemePreference(): ThemePreferenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertThemePreference(preference: ThemePreferenceEntity)
}
