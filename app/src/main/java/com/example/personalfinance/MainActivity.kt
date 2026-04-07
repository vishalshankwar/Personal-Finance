package com.example.personalfinance

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.personalfinance.data.FinanceRepository
import com.example.personalfinance.model.FinanceTransaction
import com.example.personalfinance.model.TransactionType
import com.example.personalfinance.model.WeeklySpend
import com.example.personalfinance.ui.CategorySummaryAdapter
import com.example.personalfinance.ui.TransactionAdapter
import com.example.personalfinance.ui.main.MainScreen
import com.example.personalfinance.ui.main.MainUiState
import com.example.personalfinance.ui.main.MainViewModel
import com.example.personalfinance.ui.main.MainViewModelFactory
import com.example.personalfinance.ui.profile.ProfileFragment
import com.example.personalfinance.util.Formatters
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.time.LocalDate
import kotlin.math.max

class MainActivity : AppCompatActivity() {

    private lateinit var dashboardCategoryAdapter: CategorySummaryAdapter
    private lateinit var insightCategoryAdapter: CategorySummaryAdapter
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var viewModel: MainViewModel
    private var latestUiState: MainUiState = MainUiState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        val repository = FinanceRepository(this)
        viewModel = ViewModelProvider(
            this,
            MainViewModelFactory(repository)
        )[MainViewModel::class.java]

        supportFragmentManager.addOnBackStackChangedListener {
             syncProfileOverlay()
        }
        syncProfileOverlay()

        setupLists()
        setupNavigation()
        setupInteractions()
        observeViewModel()
    }

    private fun setupLists() {
        dashboardCategoryAdapter = CategorySummaryAdapter()
        insightCategoryAdapter = CategorySummaryAdapter()
        transactionAdapter = TransactionAdapter(
            onEdit = { showTransactionDialog(it) },
            onDelete = { confirmDelete(it) }
        )

        findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.dashboardCategoryList).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = dashboardCategoryAdapter
            isNestedScrollingEnabled = false
        }

        findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.insightCategoryList).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = insightCategoryAdapter
            isNestedScrollingEnabled = false
        }

        findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.transactionList).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = transactionAdapter
        }
    }

    private fun setupNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_dashboard
        bottomNavigation.setOnItemSelectedListener { item ->
            val screen = when (item.itemId) {
                R.id.nav_transactions -> MainScreen.TRANSACTIONS
                R.id.nav_goals -> MainScreen.GOALS
                R.id.nav_insights -> MainScreen.INSIGHTS
                else -> MainScreen.DASHBOARD
            }
            viewModel.onScreenSelected(screen)
            true
        }
    }

    private fun setupInteractions() {
        findViewById<FloatingActionButton>(R.id.addTransactionFab).setOnClickListener {
            showTransactionDialog()
        }
        findViewById<MaterialButton>(R.id.editGoalButton).setOnClickListener {
            showGoalDialog()
        }
        findViewById<View>(R.id.profileButton).setOnClickListener {
            openProfile()
        }

        findViewById<EditText>(R.id.searchInput).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                viewModel.onSearchQueryChanged(s?.toString().orEmpty())
            }
        })

        findViewById<Chip>(R.id.chipAll).setOnClickListener {
            viewModel.onTransactionFilterChanged(null)
        }
        findViewById<Chip>(R.id.chipIncome).setOnClickListener {
            viewModel.onTransactionFilterChanged(TransactionType.INCOME)
        }
        findViewById<Chip>(R.id.chipExpense).setOnClickListener {
            viewModel.onTransactionFilterChanged(TransactionType.EXPENSE)
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            latestUiState = state
            render(state)
        }
        viewModel.message.observe(this) { message ->
            if (message.isNullOrBlank()) return@observe
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            viewModel.consumeMessage()
        }
    }

    private fun openProfile() {
        if (supportFragmentManager.findFragmentById(R.id.overlayContainer) != null) return

        findViewById<View>(R.id.overlayContainer).visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.overlayContainer, ProfileFragment())
            .addToBackStack("profile")
            .commit()
    }

    private fun syncProfileOverlay() {
        val overlay = findViewById<View>(R.id.overlayContainer)
        val isProfileVisible = supportFragmentManager.findFragmentById(R.id.overlayContainer) != null
        overlay.visibility = if (isProfileVisible) View.VISIBLE else View.GONE
        updateFabVisibility(latestUiState.currentScreen, isProfileVisible)
    }

    private fun render(state: MainUiState) {
        updateScreenVisibility(state.currentScreen)
        renderDashboard(state)
        renderTransactions(state)
        renderGoals(state)
        renderInsights(state)
    }

    private fun updateScreenVisibility(screen: MainScreen) {
        findViewById<View>(R.id.dashboardScreen).visibility = if (screen == MainScreen.DASHBOARD) View.VISIBLE else View.GONE
        findViewById<View>(R.id.transactionsScreen).visibility = if (screen == MainScreen.TRANSACTIONS) View.VISIBLE else View.GONE
        findViewById<View>(R.id.goalsScreen).visibility = if (screen == MainScreen.GOALS) View.VISIBLE else View.GONE
        findViewById<View>(R.id.insightsScreen).visibility = if (screen == MainScreen.INSIGHTS) View.VISIBLE else View.GONE
        updateFabVisibility(
            screen = screen,
            isOverlayVisible = supportFragmentManager.findFragmentById(R.id.overlayContainer) != null
        )

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val selectedItemId = when (screen) {
            MainScreen.DASHBOARD -> R.id.nav_dashboard
            MainScreen.TRANSACTIONS -> R.id.nav_transactions
            MainScreen.GOALS -> R.id.nav_goals
            MainScreen.INSIGHTS -> R.id.nav_insights
        }
        if (bottomNavigation.selectedItemId != selectedItemId) {
            bottomNavigation.selectedItemId = selectedItemId
        }

        val titleView = findViewById<TextView>(R.id.toolbarTitle)
        val subtitleView = findViewById<TextView>(R.id.toolbarSubtitle)
        titleView.text = screen.title
        subtitleView.text = screen.subtitle
    }

    private fun updateFabVisibility(screen: MainScreen, isOverlayVisible: Boolean) {
        findViewById<FloatingActionButton>(R.id.addTransactionFab).visibility =
            if (screen == MainScreen.DASHBOARD && !isOverlayVisible) View.VISIBLE else View.GONE
    }

    private fun renderDashboard(state: MainUiState) {
        val summary = state.dashboardSummary
        findViewById<TextView>(R.id.currentBalanceText).text = Formatters.currency(summary.currentBalance)
        findViewById<TextView>(R.id.totalIncomeText).text = Formatters.currency(summary.totalIncome)
        findViewById<TextView>(R.id.totalExpenseText).text = Formatters.currency(summary.totalExpenses)
        findViewById<TextView>(R.id.savingsRateText).text = "Savings rate ${summary.savingsRate}%"
        findViewById<ProgressBar>(R.id.savingsProgressBar).progress = summary.savingsRate

        renderWeeklyTrend(state.weeklySpend)
        dashboardCategoryAdapter.submitList(state.dashboardCategories)
    }

    private fun renderTransactions(state: MainUiState) {
        transactionAdapter.submitList(state.filteredTransactions)
        findViewById<TextView>(R.id.transactionHelperText).text = state.transactionHelperText
        findViewById<TextView>(R.id.emptyTransactionsText).visibility =
            if (state.showEmptyTransactions) View.VISIBLE else View.GONE

        val searchInput = findViewById<EditText>(R.id.searchInput)
        if (searchInput.text?.toString() != state.currentQuery) {
            searchInput.setText(state.currentQuery)
            searchInput.setSelection(searchInput.text?.length ?: 0)
        }

        val chipGroup = findViewById<ChipGroup>(R.id.filterChipGroup)
        val checkedChipId = when (state.selectedTransactionFilter) {
            TransactionType.INCOME -> R.id.chipIncome
            TransactionType.EXPENSE -> R.id.chipExpense
            null -> R.id.chipAll
        }
        if (chipGroup.checkedChipId != checkedChipId) {
            chipGroup.check(checkedChipId)
        }
    }

    private fun renderGoals(state: MainUiState) {
        val progress = state.goalProgress
        findViewById<TextView>(R.id.goalTargetText).text = Formatters.currency(progress.target)
        findViewById<TextView>(R.id.goalSavedText).text = "Saved this month: ${Formatters.currency(progress.savedThisMonth)}"
        findViewById<ProgressBar>(R.id.goalProgressBar).progress = progress.progressPercent
        findViewById<TextView>(R.id.goalPaceText).text = progress.paceMessage
        findViewById<TextView>(R.id.goalRemainingText).text = "Remaining: ${Formatters.currency(progress.remainingAmount)}"
        findViewById<TextView>(R.id.noSpendDaysText).text = progress.noSpendDaysThisMonth.toString()
        findViewById<TextView>(R.id.noSpendStreakText).text = "${progress.currentStreak} day${if (progress.currentStreak == 1) "" else "s"}"

        val challengeMessage = if (progress.currentStreak >= 3) {
            "Challenge complete. You already have a ${progress.currentStreak}-day no-spend streak."
        } else {
            val remainingDays = 3 - progress.currentStreak
            "Aim for $remainingDays more no-spend day${if (remainingDays == 1) "" else "s"} to hit the mini challenge."
        }
        findViewById<TextView>(R.id.challengeHelperText).text = challengeMessage
    }

    private fun renderInsights(state: MainUiState) {
        val insights = state.insights
        findViewById<TextView>(R.id.topCategoryText).text = insights.topCategory
        findViewById<TextView>(R.id.topCategoryAmountText).text = "${Formatters.currency(insights.topCategoryAmount)} this month"
        findViewById<TextView>(R.id.busiestDayText).text = insights.busiestExpenseDayLabel
        findViewById<TextView>(R.id.busiestDayAmountText).text = Formatters.currency(insights.busiestExpenseDayAmount)

        val weeklyDiff = insights.thisWeekSpend - insights.lastWeekSpend
        val direction = when {
            weeklyDiff > 0 -> "up"
            weeklyDiff < 0 -> "down"
            else -> "flat"
        }
        val comparisonText = if (direction == "flat") {
            "This week matched last week at ${Formatters.currency(insights.thisWeekSpend)}."
        } else {
            val difference = Formatters.currency(kotlin.math.abs(weeklyDiff))
            "This week you spent ${Formatters.currency(insights.thisWeekSpend)} vs ${Formatters.currency(insights.lastWeekSpend)} last week, $difference $direction week over week."
        }
        findViewById<TextView>(R.id.weekComparisonText).text = comparisonText

        insightCategoryAdapter.submitList(state.insightCategories)
    }

    private fun renderWeeklyTrend(data: List<WeeklySpend>) {
        val container = findViewById<LinearLayout>(R.id.weeklyTrendContainer)
        container.removeAllViews()
        val maxAmount = max(data.maxOfOrNull { it.amount } ?: 0.0, 1.0)

        data.forEach { item ->
            val row = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, container, false) as ViewGroup
            val title = row.findViewById<TextView>(android.R.id.text1)
            val subtitle = row.findViewById<TextView>(android.R.id.text2)
            title.text = item.label
            title.setTextColor(getColor(R.color.text_primary))
            subtitle.text = Formatters.currency(item.amount)
            subtitle.setTextColor(getColor(R.color.text_secondary))

            val progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
                max = 100
                progress = ((item.amount / maxAmount) * 100).toInt()
                progressTintList = getColorStateList(R.color.accent)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(8)
                ).apply { topMargin = dpToPx(6) }
            }

            val wrapper = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dpToPx(10) }
                addView(row)
                addView(progress)
            }
            container.addView(wrapper)
        }
    }

    private fun showTransactionDialog(existing: FinanceTransaction? = null) {
        // The activity gathers input here, while state changes and persistence stay in the ViewModel/repository path.
        val dialogView = layoutInflater.inflate(R.layout.dialog_transaction, null)
        val amountInput = dialogView.findViewById<TextInputEditText>(R.id.amountInput)
        val typeInput = dialogView.findViewById<AutoCompleteTextView>(R.id.typeInput)
        val categoryInput = dialogView.findViewById<AutoCompleteTextView>(R.id.categoryInput)
        val dateInput = dialogView.findViewById<TextInputEditText>(R.id.dateInput)
        val dateInputLayout = dialogView.findViewById<TextInputLayout>(R.id.dateInputLayout)
        val notesInput = dialogView.findViewById<TextInputEditText>(R.id.notesInput)

        var selectedDate = existing?.date ?: LocalDate.now()
        val typeItems = listOf("Expense", "Income")
        typeInput.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, typeItems))

        fun categoryOptionsFor(type: TransactionType): List<String> {
            val categories = if (type == TransactionType.INCOME) {
                listOf("Salary", "Freelance", "Bonus", "Refund", "Interest", "Other")
            } else {
                listOf("Food", "Transport", "Bills", "Shopping", "Rent", "Coffee", "Health", "Entertainment", "Other")
            }
            val existingCategory = existing?.category?.takeIf { it.isNotBlank() }
            return if (existingCategory != null && existingCategory !in categories) {
                categories + existingCategory
            } else {
                categories
            }
        }

        fun updateCategoryOptions(type: TransactionType, preserveSelection: Boolean) {
            val categories = categoryOptionsFor(type)
            categoryInput.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, categories))

            val currentCategory = categoryInput.text?.toString().orEmpty().trim()
            val nextCategory = when {
                preserveSelection && currentCategory in categories -> currentCategory
                preserveSelection && existing?.category in categories -> existing?.category.orEmpty()
                else -> ""
            }

            categoryInput.setText(nextCategory, false)
            categoryInput.error = null
        }

        val initialType = existing?.type ?: TransactionType.EXPENSE
        typeInput.setText(initialType.name.lowercase().replaceFirstChar { it.uppercase() }, false)
        updateCategoryOptions(initialType, preserveSelection = existing != null)
        amountInput.setText(existing?.amount?.toString().orEmpty())
        notesInput.setText(existing?.notes.orEmpty())
        dateInput.setText(Formatters.compactDate(selectedDate))

        typeInput.setOnItemClickListener { _, _, position, _ ->
            updateCategoryOptions(
                type = if (position == 0) TransactionType.EXPENSE else TransactionType.INCOME,
                preserveSelection = false
            )
        }

        categoryInput.setOnClickListener {
            categoryInput.showDropDown()
        }
        categoryInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                categoryInput.showDropDown()
            }
        }

        val openDatePicker = View.OnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                    dateInput.setText(Formatters.compactDate(selectedDate))
                },
                selectedDate.year,
                selectedDate.monthValue - 1,
                selectedDate.dayOfMonth
            ).show()
        }
        dateInput.setOnClickListener(openDatePicker)
        dateInputLayout.setEndIconOnClickListener { openDatePicker.onClick(it) }

        MaterialAlertDialogBuilder(this)
            .setTitle(if (existing == null) "Add transaction" else "Edit transaction")
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .setPositiveButton(if (existing == null) "Save" else "Update", null)
            .show()
            .apply {
                getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val amount = amountInput.text?.toString()?.toDoubleOrNull()
                    val type = if (typeInput.text.toString() == "Income") TransactionType.INCOME else TransactionType.EXPENSE
                    val category = categoryInput.text?.toString().orEmpty().trim()
                    val notes = notesInput.text?.toString().orEmpty().trim()

                    if (amount == null || amount <= 0.0) {
                        amountInput.error = "Enter a valid amount"
                        return@setOnClickListener
                    }
                    if (category.isBlank()) {
                        categoryInput.error = "Choose a category"
                        return@setOnClickListener
                    }

                    val transaction = (existing ?: FinanceTransaction(
                        amount = amount,
                        type = type,
                        category = category,
                        date = selectedDate,
                        notes = notes
                    )).copy(
                        amount = amount,
                        type = type,
                        category = category,
                        date = selectedDate,
                        notes = notes
                    )

                    viewModel.upsertTransaction(transaction)
                    dismiss()
                }
            }
    }

    private fun showGoalDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_goal, null)
        val amountInput = dialogView.findViewById<TextInputEditText>(R.id.goalAmountInput)
        amountInput.setText(latestUiState.goalProgress.target.toString())

        MaterialAlertDialogBuilder(this)
            .setTitle("Adjust savings goal")
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save", null)
            .show()
            .apply {
                getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val target = amountInput.text?.toString()?.toDoubleOrNull()
                    if (target == null || target <= 0.0) {
                        amountInput.error = "Enter a valid target"
                        return@setOnClickListener
                    }
                    viewModel.updateGoalTarget(target)
                    dismiss()
                }
            }
    }

    private fun confirmDelete(transaction: FinanceTransaction) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete transaction")
            .setMessage("Remove ${transaction.category} from your history?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteTransaction(transaction)
            }
            .show()
    }

    private fun dpToPx(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
