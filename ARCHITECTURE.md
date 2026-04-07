# Architecture

This project uses a lightweight layered Android architecture built on native Views.

Runtime flow:

`Application -> Splash/Login/MainActivity -> ViewModel -> Repository -> Room DAO -> Database`

Derived screen data flow:

`raw transactions + goal config -> FinanceAnalytics -> MainUiState -> UI render`

## Entry Flow

- [`PersonalFinanceApp`](E:\zorvyn\app\src\main\java\com\example\personalfinance\PersonalFinanceApp.kt) applies the saved theme on app start.
- [`SplashActivity`](E:\zorvyn\app\src\main\java\com\example\personalfinance\SplashActivity.kt) is the launcher activity.
- `SplashActivity` checks [`AuthStorage`](E:\zorvyn\app\src\main\java\com\example\personalfinance\data\AuthStorage.kt) and routes to:
  - [`LoginActivity`](E:\zorvyn\app\src\main\java\com\example\personalfinance\LoginActivity.kt)
  - [`MainActivity`](E:\zorvyn\app\src\main\java\com\example\personalfinance\MainActivity.kt)

## Layer Responsibilities

### UI Layer

Main files:

- [`MainActivity.kt`](E:\zorvyn\app\src\main\java\com\example\personalfinance\MainActivity.kt)
- [`MainScreen.kt`](E:\zorvyn\app\src\main\java\com\example\personalfinance\ui\main\MainScreen.kt)
- [`MainUiState.kt`](E:\zorvyn\app\src\main\java\com\example\personalfinance\ui\main\MainUiState.kt)
- [`TransactionAdapter.kt`](E:\zorvyn\app\src\main\java\com\example\personalfinance\ui\TransactionAdapter.kt)
- [`CategorySummaryAdapter.kt`](E:\zorvyn\app\src\main\java\com\example\personalfinance\ui\CategorySummaryAdapter.kt)
- [`ProfileFragment.kt`](E:\zorvyn\app\src\main\java\com\example\personalfinance\ui\profile\ProfileFragment.kt)

Responsibilities:

- render layouts and lists
- handle clicks, dialogs, and navigation
- observe ViewModel state
- forward user actions to the ViewModel

### Presentation Layer

Main files:

- [`MainViewModel.kt`](E:\zorvyn\app\src\main\java\com\example\personalfinance\ui\main\MainViewModel.kt)
- [`MainViewModelFactory.kt`](E:\zorvyn\app\src\main\java\com\example\personalfinance\ui\main\MainViewModelFactory.kt)
- [`MainUiState.kt`](E:\zorvyn\app\src\main\java\com\example\personalfinance\ui\main\MainUiState.kt)

Responsibilities:

- hold current screen state in memory
- store active filters and query
- save edits through the repository
- rebuild a single `MainUiState` after every meaningful change

Key idea:

- `MainViewModel.buildUiState()` is the central place where raw data becomes dashboard cards, filtered transactions, goal progress, and insight summaries.

### Domain Layer

Main files:

- [`FinanceAnalytics.kt`](E:\zorvyn\app\src\main\java\com\example\personalfinance\domain\FinanceAnalytics.kt)
- [`AnalyticsModels.kt`](E:\zorvyn\app\src\main\java\com\example\personalfinance\model\AnalyticsModels.kt)

Responsibilities:

- pure calculations
- transaction filtering
- weekly summaries
- category breakdowns
- goal and insight derivation

This layer has no Android UI code and no Room code.

### Data Layer

Main files:

- [`FinanceRepository.kt`](E:\zorvyn\app\src\main\java\com\example\personalfinance\data\FinanceRepository.kt)
- [`AuthStorage.kt`](E:\zorvyn\app\src\main\java\com\example\personalfinance\data\AuthStorage.kt)
- [`ThemeStorage.kt`](E:\zorvyn\app\src\main\java\com\example\personalfinance\data\ThemeStorage.kt)

Responsibilities:

- read and write app data
- map between Room entities and app models
- migrate older SharedPreferences data into Room

Important:

- Room is the active persistence layer.
- SharedPreferences is only used as a legacy migration source.

### Persistence Layer

Main files:

- [`AppDatabase.kt`](E:\zorvyn\app\src\main\java\com\example\personalfinance\data\local\AppDatabase.kt)
- [`FinanceDao.kt`](E:\zorvyn\app\src\main\java\com\example\personalfinance\data\local\FinanceDao.kt)
- [`TransactionEntity.kt`](E:\zorvyn\app\src\main\java\com\example\personalfinance\data\local\TransactionEntity.kt)
- [`GoalEntity.kt`](E:\zorvyn\app\src\main\java\com\example\personalfinance\data\local\GoalEntity.kt)
- [`AuthEntity.kt`](E:\zorvyn\app\src\main\java\com\example\personalfinance\data\local\AuthEntity.kt)
- [`ThemePreferenceEntity.kt`](E:\zorvyn\app\src\main\java\com\example\personalfinance\data\local\ThemePreferenceEntity.kt)
- [`Converters.kt`](E:\zorvyn\app\src\main\java\com\example\personalfinance\data\local\Converters.kt)

Responsibilities:

- define tables
- define SQL queries
- configure the Room database

## End-to-End Flows

### Add transaction flow

1. User taps add in [`MainActivity`](E:\zorvyn\app\src\main\java\com\example\personalfinance\MainActivity.kt).
2. `showTransactionDialog()` collects form input.
3. `MainActivity` calls `viewModel.upsertTransaction(...)`.
4. [`MainViewModel`](E:\zorvyn\app\src\main\java\com\example\personalfinance\ui\main\MainViewModel.kt) updates its in-memory list.
5. ViewModel calls [`FinanceRepository.saveTransactions()`](E:\zorvyn\app\src\main\java\com\example\personalfinance\data\FinanceRepository.kt).
6. Repository writes through [`FinanceDao`](E:\zorvyn\app\src\main\java\com\example\personalfinance\data\local\FinanceDao.kt).
7. ViewModel rebuilds `MainUiState`.
8. [`FinanceAnalytics`](E:\zorvyn\app\src\main\java\com\example\personalfinance\domain\FinanceAnalytics.kt) recomputes summaries.
9. `MainActivity` observes the new state and re-renders the UI.

### Search or filter flow

1. Search input or chip selection changes in `MainActivity`.
2. ViewModel stores `currentQuery` or `selectedTransactionFilter`.
3. ViewModel rebuilds `MainUiState`.
4. `FinanceAnalytics.filteredTransactions()` produces the visible transaction list.
5. `TransactionAdapter` displays the filtered result.

### Goal update flow

1. Goal dialog submits a new target.
2. ViewModel updates goal state and saves it through the repository.
3. `FinanceAnalytics.buildGoalProgress()` recalculates progress.
4. Goals and insights UI refresh from the new `MainUiState`.

## Best Reading Order

If you want to understand the project quickly, read in this order:

1. [`AndroidManifest.xml`](E:\zorvyn\app\src\main\AndroidManifest.xml)
2. [`PersonalFinanceApp.kt`](E:\zorvyn\app\src\main\java\com\example\personalfinance\PersonalFinanceApp.kt)
3. [`SplashActivity.kt`](E:\zorvyn\app\src\main\java\com\example\personalfinance\SplashActivity.kt)
4. [`LoginActivity.kt`](E:\zorvyn\app\src\main\java\com\example\personalfinance\LoginActivity.kt)
5. [`MainActivity.kt`](E:\zorvyn\app\src\main\java\com\example\personalfinance\MainActivity.kt)
6. [`MainViewModel.kt`](E:\zorvyn\app\src\main\java\com\example\personalfinance\ui\main\MainViewModel.kt)
7. [`FinanceRepository.kt`](E:\zorvyn\app\src\main\java\com\example\personalfinance\data\FinanceRepository.kt)
8. [`FinanceAnalytics.kt`](E:\zorvyn\app\src\main\java\com\example\personalfinance\domain\FinanceAnalytics.kt)
9. [`AppDatabase.kt`](E:\zorvyn\app\src\main\java\com\example\personalfinance\data\local\AppDatabase.kt)
10. [`FinanceDao.kt`](E:\zorvyn\app\src\main\java\com\example\personalfinance\data\local\FinanceDao.kt)

## Current Tradeoffs

- [`MainActivity`](E:\zorvyn\app\src\main\java\com\example\personalfinance\MainActivity.kt) is still a large orchestration file.
- [`AppDatabase`](E:\zorvyn\app\src\main\java\com\example\personalfinance\data\local\AppDatabase.kt) uses `allowMainThreadQueries()`, which is acceptable for this small local app but not ideal for production.
- The app is intentionally lightweight, so the architecture is layered but not split into feature modules.
