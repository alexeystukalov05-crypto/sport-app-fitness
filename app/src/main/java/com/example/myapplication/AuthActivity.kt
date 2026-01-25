package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AuthActivity : AppCompatActivity() {

    private lateinit var userManager: UserManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_auth)
        ThemeHelper.apply(this)

        userManager = UserManager(this)

        val loginEditText = findViewById<EditText>(R.id.loginEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerTextView = findViewById<TextView>(R.id.registerTextView)

        // Проверяем, не авторизован ли пользователь
        checkCurrentUser()

        loginButton.setOnClickListener {
            val login = loginEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (login.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Показываем прогресс
            Toast.makeText(this, "Вход...", Toast.LENGTH_SHORT).show()

            // Авторизация через Firebase
            userManager.loginUser(login, password) { success, message ->
                if (success) {
                    Toast.makeText(this, "Успешный вход! Добро пожаловать", Toast.LENGTH_SHORT).show()

                    // Переход на главный экран
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Ошибка входа: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }

        registerTextView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkCurrentUser() {
        val currentUser = userManager.getCurrentUser()
        if (currentUser != null) {
            // Пользователь уже авторизован, переходим на главный экран
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}