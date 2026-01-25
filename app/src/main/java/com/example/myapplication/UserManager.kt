package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.util.Locale

class UserManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    private val firebaseManager = FirebaseManager()

    companion object {
        private const val TAG = "UserManager"
        private const val KEY_CURRENT_USER_LOGIN = "current_user_login"
        private const val KEY_CURRENT_USER_EMAIL = "current_user_email"
    }

    // Регистрация через Firebase
    fun registerUser(login: String, password: String, callback: (Boolean, String?) -> Unit) {
        Log.d(TAG, "Register user: $login")

        // Создаем правильный email адрес
        val email = createValidEmail(login)

        firebaseManager.registerUser(email, password, login) { success, message ->
            if (success) {
                // После успешной регистрации пользователь автоматически авторизован в Firebase
                // Сохраняем информацию о пользователе локально
                saveCurrentUser(login, email)
                Log.d(TAG, "User registered and logged in successfully")
                callback(true, null)
            } else {
                Log.e(TAG, "Registration failed: $message")
                callback(false, message)
            }
        }
    }

    // Авторизация через Firebase
    fun loginUser(login: String, password: String, callback: (Boolean, String?) -> Unit) {
        Log.d(TAG, "Login user: $login")

        val email = createValidEmail(login)

        firebaseManager.loginUser(email, password) { success, message ->
            if (success) {
                // Сохраняем информацию о пользователе локально
                saveCurrentUser(login, email)
                callback(true, null)
            } else {
                Log.e(TAG, "Login failed: $message")
                callback(false, message)
            }
        }
    }

    // Создание валидного email адреса
    private fun createValidEmail(login: String): String {
        // Убираем все недопустимые символы для email
        val cleanLogin = login
            .lowercase(Locale.getDefault())
            .replace("[^a-z0-9._-]".toRegex(), "")
            .replace("\\.{2,}".toRegex(), ".")
            .trim()

        // Если после очистки логин пустой, используем дефолтное значение
        val finalLogin = if (cleanLogin.isEmpty()) "user${System.currentTimeMillis()}" else cleanLogin

        return "$finalLogin@makesports.com"
    }

    // Получение текущего пользователя
    fun getCurrentUser(): User? {
        // Сначала проверяем Firebase
        val firebaseUser = firebaseManager.getCurrentUser()
        if (firebaseUser != null) {
            val login = firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@") ?: "user"
            val email = firebaseUser.email ?: "$login@makesports.com"
            Log.d(TAG, "Firebase user found: $login")
            return User(login, email)
        }

        // Если Firebase пользователя нет, проверяем локальное хранилище
        val login = sharedPreferences.getString(KEY_CURRENT_USER_LOGIN, null)
        val email = sharedPreferences.getString(KEY_CURRENT_USER_EMAIL, null)

        return if (login != null && email != null) {
            Log.d(TAG, "Local user found: $login")
            User(login, email)
        } else {
            Log.d(TAG, "No user found")
            null
        }
    }

    // Выход пользователя
    fun logout() {
        Log.d(TAG, "Logout user")
        firebaseManager.logout()
        sharedPreferences.edit()
            .remove(KEY_CURRENT_USER_LOGIN)
            .remove(KEY_CURRENT_USER_EMAIL)
            .apply()
    }

    // Проверка существования пользователя (локальная)
    fun userExists(login: String): Boolean {
        return false
    }

    // Сохранение текущего пользователя локально
    private fun saveCurrentUser(login: String, email: String) {
        sharedPreferences.edit()
            .putString(KEY_CURRENT_USER_LOGIN, login)
            .putString(KEY_CURRENT_USER_EMAIL, email)
            .apply()
        Log.d(TAG, "User saved locally: $login, $email")
    }
}

data class User(
    val login: String,
    val email: String
)