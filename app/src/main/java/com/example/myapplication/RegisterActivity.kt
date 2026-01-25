package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var userManager: UserManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_register)
        ThemeHelper.apply(this)

        userManager = UserManager(this)

        val regLoginEditText = findViewById<EditText>(R.id.regLoginEditText)
        val regPasswordEditText = findViewById<EditText>(R.id.regPasswordEditText)
        val createAccountButton = findViewById<Button>(R.id.createAccountButton)
        val loginTextView = findViewById<TextView>(R.id.loginTextView)

        createAccountButton.setOnClickListener {
            val login = regLoginEditText.text.toString().trim()
            val password = regPasswordEditText.text.toString().trim()

            if (login.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Пароль должен содержать минимум 6 символов", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Валидация логина
            if (!isValidLogin(login)) {
                Toast.makeText(this, "Логин должен содержать только английские буквы, цифры, точки и дефисы (3-20 символов)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Показываем прогресс
            Toast.makeText(this, "Регистрация...", Toast.LENGTH_SHORT).show()

            // Блокируем кнопку на время регистрации
            createAccountButton.isEnabled = false

            // Регистрация через Firebase
            userManager.registerUser(login, password) { success, message ->
                // Разблокируем кнопку
                createAccountButton.isEnabled = true

                if (success) {
                    Toast.makeText(this, "Аккаунт успешно создан!", Toast.LENGTH_SHORT).show()

                    // Проверяем, авторизован ли пользователь
                    val currentUser = userManager.getCurrentUser()
                    if (currentUser != null) {
                        // Если авторизован, переходим сразу в приложение
                        Toast.makeText(this, "Добро пожаловать, ${currentUser.login}!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // Если не авторизован, переходим на авторизацию
                        Toast.makeText(this, "Теперь войдите в систему", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, AuthActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    Toast.makeText(this, "Ошибка регистрации: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }

        loginTextView.setOnClickListener {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Валидация логина
    private fun isValidLogin(login: String): Boolean {
        return login.matches(Regex("^[a-zA-Z0-9._-]{3,20}$"))
    }
}