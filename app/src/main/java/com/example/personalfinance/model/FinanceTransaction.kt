package com.example.personalfinance.model

import java.time.LocalDate
import java.util.UUID

enum class TransactionType {
    INCOME,
    EXPENSE
}

data class FinanceTransaction(
    val id: String = UUID.randomUUID().toString(),
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val date: LocalDate,
    val notes: String =""
)
