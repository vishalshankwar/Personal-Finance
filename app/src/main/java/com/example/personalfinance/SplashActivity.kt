package com.example.personalfinance

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.personalfinance.data.AuthStorage

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        val authStorage = AuthStorage(this)
        android.os.Handler(Looper.getMainLooper()).postDelayed({
            val nextScreen = if (authStorage.hasCredentials()) {
                MainActivity::class.java
            } else {
                LoginActivity::class.java
            }
            startActivity(Intent(this, nextScreen))
            finish()
        }, 1500)

    }
}
