# Personal Finance Companion

A native Android finance tracker built in Kotlin with a single-activity UI, Room persistence, and a separate analytics layer for derived screen data.

## What's included

- Home dashboard with current balance, income, expenses, savings rate, weekly spending trend, and top category breakdown
- Full transaction tracking flow with add, edit, delete, search, and income/expense filters
- Goal and challenge screen with a monthly savings target plus a no-spend streak concept
- Insights screen with week-over-week comparisons, busiest spend day, and category analysis
- Local persistence using Room, with legacy SharedPreferences migration for older saved data

## Tech stack

- Kotlin
- Native Android Views + Material 3 components
- Single-activity architecture with screen-level sections
- ViewModel + repository + analytics layer for state, persistence, and derived insights
- Room database for transactions, goal config, auth, and theme preference
- RecyclerView for transaction and analytics lists

## Architecture At A Glance

- Entry flow: `PersonalFinanceApp -> SplashActivity -> LoginActivity/MainActivity`
- Main UI flow: `MainActivity -> MainViewModel -> MainUiState`
- Data flow: `MainViewModel -> FinanceRepository -> FinanceDao -> Room`
- Derived data flow: `transactions + goal -> FinanceAnalytics -> MainUiState -> UI`

## Project Structure

- [`MainActivity.kt`](app/src/main/java/com/example/personalfinance/MainActivity.kt): main screen orchestration, navigation, dialogs, and rendering
- [`MainViewModel.kt`](app/src/main/java/com/example/personalfinance/ui/main/MainViewModel.kt): in-memory state owner and event handler
- [`FinanceRepository.kt`](app/src/main/java/com/example/personalfinance/data/FinanceRepository.kt): persistence, mapping, and legacy migration
- [`FinanceAnalytics.kt`](app/src/main/java/com/example/personalfinance/domain/FinanceAnalytics.kt): pure summary, trend, and filtering logic
- [`AppDatabase.kt`](app/src/main/java/com/example/personalfinance/data/local/AppDatabase.kt): Room database entry point
- [`FinanceDao.kt`](app/src/main/java/com/example/personalfinance/data/local/FinanceDao.kt): Room queries
- [`ARCHITECTURE.md`](ARCHITECTURE.md): detailed runtime flow and layer explanation

## Read Order

If you want to understand the project quickly, read these files in order:

1. [`AndroidManifest.xml`](app/src/main/AndroidManifest.xml)
2. [`PersonalFinanceApp.kt`](app/src/main/java/com/example/personalfinance/PersonalFinanceApp.kt)
3. [`SplashActivity.kt`](app/src/main/java/com/example/personalfinance/SplashActivity.kt)
4. [`LoginActivity.kt`](app/src/main/java/com/example/personalfinance/LoginActivity.kt)
5. [`MainActivity.kt`](app/src/main/java/com/example/personalfinance/MainActivity.kt)
6. [`MainViewModel.kt`](app/src/main/java/com/example/personalfinance/ui/main/MainViewModel.kt)
7. [`FinanceRepository.kt`](app/src/main/java/com/example/personalfinance/data/FinanceRepository.kt)
8. [`FinanceAnalytics.kt`](app/src/main/java/com/example/personalfinance/domain/FinanceAnalytics.kt)

## How to run

1. Open the project in Android Studio.
2. Sync Gradle.
3. Run the `app` configuration on an emulator or Android device with API 28+.

## Verification

- `./gradlew.bat assembleDebug`

## Notes

- No external image assets are required for this submission.
- The app stays lightweight, but responsibilities are still separated into UI, ViewModel, domain, data, and Room layers.
