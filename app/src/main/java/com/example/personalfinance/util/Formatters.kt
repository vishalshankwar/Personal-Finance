package com.example.personalfinance.util

import com.example.personalfinance.model.TransactionType
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object Formatters {
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))
    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)

    fun currency(amount: Double): String = currencyFormatter.format(amount)

    fun compactDate(date: LocalDate): String = date.format(dateFormatter)

    fun signedAmount(type: TransactionType, amount: Double): String {
        val prefix = if (type == TransactionType.INCOME) "+ " else "- "
        return prefix + currency(amount)
    }
}
