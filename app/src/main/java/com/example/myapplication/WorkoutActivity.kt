package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.data.WorkoutSession
import java.text.SimpleDateFormat
import java.util.Locale

class WorkoutsActivity : AppCompatActivity() {

    private val firebaseManager = FirebaseManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_workouts)
        ThemeHelper.apply(this)

        val startWorkoutButton = findViewById<Button>(R.id.startWorkoutButton)
        val workoutHistoryButton = findViewById<Button>(R.id.workoutHistoryButton)
        val backButton = findViewById<Button>(R.id.backButton)
        val recentWorkoutsList = findViewById<ListView>(R.id.recentWorkoutsList)

        startWorkoutButton.setOnClickListener {
            startActivity(Intent(this, ActiveWorkoutActivity::class.java))
        }

        workoutHistoryButton.setOnClickListener {
            startActivity(Intent(this, WorkoutHistoryActivity::class.java))
        }

        backButton.setOnClickListener { finish() }

        loadRecentWorkouts(recentWorkoutsList)
    }

    override fun onResume() {
        super.onResume()
        loadRecentWorkouts(findViewById(R.id.recentWorkoutsList))
    }

    private fun loadRecentWorkouts(list: ListView) {
        firebaseManager.getAllWorkoutSessionsOrderedByDate { sessions ->
            runOnUiThread {
                val items = sessions.take(10).map { s -> toWorkoutItem(s) }
                list.adapter = SimpleWorkoutListAdapter(this, items)
            }
        }
    }

    private fun toWorkoutItem(s: WorkoutSession) = WorkoutItem(
        sport = s.sportName,
        duration = "${s.duration} мин",
        emotion = ThemeHelper.getEmojiForEmotion(this, s.emotion),
        date = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(s.date.toDate())
    )
}