package com.example.personalfinance.data

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.example.personalfinance.data.local.AppDatabase
import com.example.personalfinance.data.local.ThemePreferenceEntity

class ThemeStorage(context: Context) {

    private val legacyPreferences = context.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
    private val financeDao = AppDatabase.getInstance(context).financeDao()

    fun isDarkModeEnabled(): Boolean {
        migrateLegacyThemeIfNeeded()
        return financeDao.getThemePreference()?.darkModeEnabled ?: false
    }

    fun setDarkModeEnabled(enabled: Boolean) {
        financeDao.upsertThemePreference(
            ThemePreferenceEntity(
                darkModeEnabled = enabled
            )
        )
    }

    fun applyTheme() {
        val mode = if (isDarkModeEnabled()) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun migrateLegacyThemeIfNeeded() {
        if (financeDao.getThemePreference() != null) return
        if (!legacyPreferences.contains(KEY_DARK_MODE)) return

        financeDao.upsertThemePreference(
            ThemePreferenceEntity(
                darkModeEnabled = legacyPreferences.getBoolean(KEY_DARK_MODE, false)
            )
        )
    }

    companion object {
        private const val LEGACY_PREFS_NAME = "theme_prefs"
        private const val KEY_DARK_MODE = "dark_mode_enabled"
    }
}
