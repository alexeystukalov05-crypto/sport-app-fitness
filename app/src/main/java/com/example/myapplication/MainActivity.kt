package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {

    private lateinit var welcomeText: TextView
    private lateinit var badgeText: TextView
    private lateinit var currentStreakText: TextView
    private lateinit var totalWorkoutsText: TextView
    private lateinit var logoutButton: Button
    private lateinit var firebaseManager: FirebaseManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)
        ThemeHelper.apply(this)

        firebaseManager = FirebaseManager()
        initViews()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        ThemeHelper.apply(this)
        setupUserInfo()
    }

    private fun initViews() {
        welcomeText = findViewById(R.id.welcomeText)
        badgeText = findViewById(R.id.badgeText)
        currentStreakText = findViewById(R.id.currentStreakText)
        totalWorkoutsText = findViewById(R.id.totalWorkoutsText)
        logoutButton = findViewById(R.id.logoutButton)
    }

    private fun setupUserInfo() {
        val userManager = UserManager(this)
        val currentUser = userManager.getCurrentUser()

        val userName = currentUser?.login ?: "Спортсмен"
        welcomeText.text = "Добро пожаловать, $userName!"

        val badge = ThemeHelper.getBadgeEmoji(this)
        badgeText.visibility = if (badge != null) View.VISIBLE else View.GONE
        if (badge != null) badgeText.text = badge

        currentStreakText.text = "0"
        totalWorkoutsText.text = "0"
        firebaseManager.getTotalWorkoutCount { count ->
            runOnUiThread {
                totalWorkoutsText.text = count.toString()
            }
        }
    }

    private fun setupClickListeners() {
        // Календарь
        findViewById<CardView>(R.id.calendarCard).setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
        }

        // Тренировки
        findViewById<CardView>(R.id.workoutsCard).setOnClickListener {
            val intent = Intent(this, WorkoutsActivity::class.java)
            startActivity(intent)
        }

        // Статистика
        findViewById<CardView>(R.id.statsCard2).setOnClickListener {
            val intent = Intent(this, StatsActivity::class.java)
            startActivity(intent)
        }

        // Магазин
        findViewById<CardView>(R.id.shopCard).setOnClickListener {
            val intent = Intent(this, ShopActivity::class.java)
            startActivity(intent)
        }

        // Настройки профиля
        findViewById<CardView>(R.id.profileSettingsCard).setOnClickListener {
            val intent = Intent(this, ProfileSettingsActivity::class.java)
            startActivity(intent)
        }

        // Выход
        logoutButton.setOnClickListener {
            val userManager = UserManager(this)
            userManager.logout()
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}