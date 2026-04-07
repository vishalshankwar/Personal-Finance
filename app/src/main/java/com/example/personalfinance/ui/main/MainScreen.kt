package com.example.personalfinance.ui.main

enum class MainScreen(
    val title: String,
    val subtitle: String
) {
    DASHBOARD(
        title = "Overview",
        subtitle = "Your daily money snapshot"
    ),
    TRANSACTIONS(
        title = "Transactions",
        subtitle = "Search, edit, and stay in control"
    ),
    GOALS(
        title = "Goals",
        subtitle = "Savings target and challenge momentum"
    ),
    INSIGHTS(
        title = "Insights",
        subtitle = "See where your money patterns are heading"
    )
}
