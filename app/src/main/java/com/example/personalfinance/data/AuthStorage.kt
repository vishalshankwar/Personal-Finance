package com.example.personalfinance.data

import android.content.Context
import com.example.personalfinance.data.local.AppDatabase
import com.example.personalfinance.data.local.AuthEntity

class AuthStorage(context: Context) {

    private val legacyPreferences = context.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
    private val financeDao = AppDatabase.getInstance(context).financeDao()

    fun getSavedEmail(): String {
        migrateLegacyAuthIfNeeded()
        return financeDao.getAuth()?.email.orEmpty()
    }

    fun hasCredentials(): Boolean {
        migrateLegacyAuthIfNeeded()
        return financeDao.getAuth()?.email?.isNotBlank() == true
    }

    fun isValidLogin(email: String, password: String): Boolean {
        migrateLegacyAuthIfNeeded()
        val auth = financeDao.getAuth() ?: return false
        return email == auth.email && (auth.password.isBlank() || password == auth.password)
    }

    fun saveCredentials(email: String, password: String) {
        financeDao.upsertAuth(
            AuthEntity(
                email = email,
                password = password
            )
        )
    }

    private fun getSavedPassword(): String = financeDao.getAuth()?.password.orEmpty()

    private fun migrateLegacyAuthIfNeeded() {
        if (financeDao.getAuth() != null) return

        val email = legacyPreferences.getString(KEY_EMAIL, "").orEmpty()
        val password = legacyPreferences.getString(KEY_PASSWORD, "").orEmpty()
        if (email.isBlank() || password.isBlank()) return

        financeDao.upsertAuth(
            AuthEntity(
                email = email,
                password = password
            )
        )
    }

    companion object {
        private const val LEGACY_PREFS_NAME = "auth_prefs"
        private const val KEY_EMAIL = "saved_email"
        private const val KEY_PASSWORD = "saved_password"
    }
}
