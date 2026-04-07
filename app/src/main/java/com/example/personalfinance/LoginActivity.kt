package com.example.personalfinance

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.personalfinance.data.AuthStorage
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity() {

    private lateinit var authStorage: AuthStorage
    private lateinit var emailLayout: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                systemBars.top,
                view.paddingRight,
                systemBars.bottom
            )
            insets
        }

        authStorage = AuthStorage(this)
        if (authStorage.hasCredentials()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        emailLayout = findViewById(R.id.emailInputLayout)
        val emailInput = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.emailInput)

        emailInput.setText(authStorage.getSavedEmail())

        findViewById<MaterialButton>(R.id.loginButton).setOnClickListener {
            handleLogin(emailInput.text?.toString().orEmpty().trim())
        }
    }

    private fun handleLogin(email: String) {
        emailLayout.error = null

        if (!isValidEmail(email)) {
            emailLayout.error = getString(R.string.error_email_invalid)
            return
        }

        authStorage.saveCredentials(email, "")
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
