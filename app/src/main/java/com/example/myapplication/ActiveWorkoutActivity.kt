package com.example.myapplication

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class ActiveWorkoutActivity : AppCompatActivity() {

    private var selectedEmotion: String = "neutral"
    private lateinit var firebaseManager: FirebaseManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_active_workout)
        ThemeHelper.apply(this)

        firebaseManager = FirebaseManager()

        val sportSpinner = findViewById<Spinner>(R.id.sportSpinner)
        val durationEditText = findViewById<EditText>(R.id.durationEditText)
        val notesEditText = findViewById<EditText>(R.id.notesEditText)
        val saveWorkoutButton = findViewById<Button>(R.id.saveWorkoutButton)
        val backButton = findViewById<Button>(R.id.backButton)

        // Настройка спиннера с видами спорта
        val sports = arrayOf("Бег", "Тренажерный зал", "Йога", "Плавание", "Велосипед", "Ходьба")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sports)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sportSpinner.adapter = adapter

        // Настройка обработчиков эмоций
        setupEmotionButtons()

        saveWorkoutButton.setOnClickListener {
            val sport = sportSpinner.selectedItem.toString()
            val durationText = durationEditText.text.toString()
            val notes = notesEditText.text.toString()

            val duration = durationText.toIntOrNull()
            if (duration == null || duration <= 0) {
                Toast.makeText(this, "Введите корректную длительность тренировки", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            firebaseManager.saveWorkoutSession(
                sportName = sport,
                duration = duration,
                notes = notes,
                emotion = selectedEmotion
            ) { success, message ->
                if (success) {
                    ShopManager(this).addCoins(5)
                    Toast.makeText(
                        this,
                        "Тренировка сохранена! +5 монет",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "Ошибка сохранения тренировки: ${message ?: "неизвестная ошибка"}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupEmotionButtons() {
        val emotion1 = findViewById<Button>(R.id.emotion1Button)
        val emotion2 = findViewById<Button>(R.id.emotion2Button)
        val emotion3 = findViewById<Button>(R.id.emotion3Button)
        val emotion4 = findViewById<Button>(R.id.emotion4Button)
        val emotion5 = findViewById<Button>(R.id.emotion5Button)

        val emotionButtons = listOf(emotion1, emotion2, emotion3, emotion4, emotion5)
        val options = ThemeHelper.getEmotionPickerOptions(this)
        val emotions = listOf("sad", "neutral", "happy", "very_happy", "excellent")

        emotionButtons.forEachIndexed { index, button ->
            if (index < options.size) button.text = options[index].second
            button.setOnClickListener {
                selectedEmotion = emotions[index]
                Toast.makeText(this, "Выбрана эмоция: ${button.text}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}