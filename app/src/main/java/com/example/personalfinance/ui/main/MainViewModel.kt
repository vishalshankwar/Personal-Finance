package com.example.personalfinance.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.personalfinance.data.FinanceRepository
import com.example.personalfinance.domain.FinanceAnalytics
import com.example.personalfinance.model.FinanceTransaction
import com.example.personalfinance.model.TransactionType

class MainViewModel(
    private val repository: FinanceRepository
) : ViewModel() {

    // These mutable fields are the inputs from which the full screen state is rebuilt.
    private val transactions = repository.loadTransactions()
    private var goalConfig = repository.loadGoalConfig()
    private var currentScreen = MainScreen.DASHBOARD
    private var currentQuery = ""
    private var selectedTransactionFilter: TransactionType? = null

    private val _uiState = MutableLiveData(buildUiState())
    val uiState: LiveData<MainUiState> = _uiState

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    fun onScreenSelected(screen: MainScreen) {
        if (currentScreen == screen) return
        currentScreen = screen
        refreshState()
    }

    fun onSearchQueryChanged(query: String) {
        if (currentQuery == query) return
        currentQuery = query
        refreshState()
    }

    fun onTransactionFilterChanged(filter: TransactionType?) {
        if (selectedTransactionFilter == filter) return
        selectedTransactionFilter = filter
        refreshState()
    }

    fun upsertTransaction(transaction: FinanceTransaction) {
        val index = transactions.indexOfFirst { it.id == transaction.id }
        if (index >= 0) {
            transactions[index] = transaction
            _message.value = "Transaction updated"
        } else {
            transactions.add(transaction)
            _message.value = "Transaction added"
        }
        repository.saveTransactions(transactions)
        refreshState()
    }

    fun deleteTransaction(transaction: FinanceTransaction) {
        transactions.removeAll { it.id == transaction.id }
        repository.saveTransactions(transactions)
        _message.value = "Transaction deleted"
        refreshState()
    }

    fun updateGoalTarget(target: Double) {
        goalConfig = goalConfig.copy(monthlyTarget = target)
        repository.saveGoalConfig(goalConfig)
        refreshState()
    }

    fun consumeMessage() {
        _message.value = null
    }

    private fun refreshState() {
        _uiState.value = buildUiState()
    }

    private fun buildUiState(): MainUiState {
        // A single state builder keeps render behavior predictable after add/edit/delete,
        // search changes, filter changes, and goal updates.
        val sortedTransactions = transactions
            .withIndex()
            .sortedWith(
                compareByDescending<IndexedValue<FinanceTransaction>> { it.value.date }
                    .thenByDescending { it.index }
            )
            .map { it.value }
        return MainUiState(
            currentScreen = currentScreen,
            dashboardSummary = FinanceAnalytics.buildDashboardSummary(sortedTransactions),
            weeklySpend = FinanceAnalytics.buildWeeklySpend(sortedTransactions),
            dashboardCategories = FinanceAnalytics.buildCategoryBreakdown(sortedTransactions).take(4),
            filteredTransactions = FinanceAnalytics.filteredTransactions(
                transactions = sortedTransactions,
                query = currentQuery,
                selectedFilter = selectedTransactionFilter
            ),
            selectedTransactionFilter = selectedTransactionFilter,
            currentQuery = currentQuery,
            goalProgress = FinanceAnalytics.buildGoalProgress(sortedTransactions, goalConfig),
            insights = FinanceAnalytics.buildInsights(sortedTransactions),
            insightCategories = FinanceAnalytics.buildCategoryBreakdown(sortedTransactions)
        )
    }
}
