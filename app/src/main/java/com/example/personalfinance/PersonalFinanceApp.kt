package com.example.personalfinance

import android.app.Application
import com.example.personalfinance.data.ThemeStorage

class PersonalFinanceApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemeStorage(this).applyTheme()
    }
}
