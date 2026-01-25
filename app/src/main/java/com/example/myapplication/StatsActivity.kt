package com.example.myapplication

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.data.WorkoutSession
import java.text.SimpleDateFormat
import java.util.*

class StatsActivity : AppCompatActivity() {

    private val firebaseManager = FirebaseManager()
    private val monthData = mutableListOf<Pair<Int, Int>>() // (year, month)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_stats)
        ThemeHelper.apply(this)

        val monthSpinner = findViewById<Spinner>(R.id.monthSpinner)
        val backButton = findViewById<Button>(R.id.backButton)

        // Формируем список последних 12 месяцев
        val labels = mutableListOf<String>()
        val cal = Calendar.getInstance()
        for (i in 0 until 12) {
            val c = cal.clone() as Calendar
            c.add(Calendar.MONTH, -i)
            monthData.add(Pair(c.get(Calendar.YEAR), c.get(Calendar.MONTH)))
            c.set(Calendar.DAY_OF_MONTH, 1)
            labels.add(SimpleDateFormat("MMMM yyyy", Locale("ru")).format(c.time))
        }
        labels.reverse()
        monthData.reverse()

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, labels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        monthSpinner.adapter = adapter
        monthSpinner.setSelection(monthData.size - 1) // по умолчанию текущий месяц

        updateStats(monthSpinner.selectedItemPosition)

        monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                updateStats(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        backButton.setOnClickListener { finish() }
    }

    private fun updateStats(monthIndex: Int) {
        if (monthIndex < 0 || monthIndex >= monthData.size) return
        val (year, month) = monthData[monthIndex]

        firebaseManager.getWorkoutSessionsForMonth(year, month) { sessions ->
            runOnUiThread {
                applyStats(sessions)
            }
        }
    }

    private fun applyStats(sessions: List<WorkoutSession>) {
        val happyStats = findViewById<TextView>(R.id.happyStats)
        val neutralStats = findViewById<TextView>(R.id.neutralStats)
        val sadStats = findViewById<TextView>(R.id.sadStats)
        val totalWorkouts = findViewById<TextView>(R.id.totalWorkouts)
        val totalDuration = findViewById<TextView>(R.id.totalDuration)
        val currentStreak = findViewById<TextView>(R.id.currentStreak)

        val total = sessions.size
        val totalMinutes = sessions.sumOf { it.duration }
        var positive = 0
        var neutral = 0
        var negative = 0
        for (s in sessions) {
            when (s.emotion) {
                "happy", "very_happy", "excellent" -> positive++
                "sad" -> negative++
                else -> neutral++
            }
        }

        val streak = computeStreak(sessions)

        totalWorkouts.text = total.toString()
        totalDuration.text = totalMinutes.toString()
        currentStreak.text = streak.toString()
        happyStats.text = "😊 Положительные: $positive"
        neutralStats.text = "😐 Нейтральные: $neutral"
        sadStats.text = "😢 Отрицательные: $negative"
    }

    private fun computeStreak(sessions: List<WorkoutSession>): Int {
        if (sessions.isEmpty()) return 0
        val dayMs = 24 * 60 * 60 * 1000
        val sorted = sessions.map { it.date.toDate().time / dayMs }.distinct().sortedDescending()
        var streak = 1
        for (i in 1 until sorted.size) {
            if (sorted[i - 1] - sorted[i] == 1L) streak++ else break
        }
        return streak
    }
}