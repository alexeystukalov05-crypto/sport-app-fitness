package com.example.myapplication

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.data.CalendarDay
import com.example.myapplication.data.WorkoutSession
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : AppCompatActivity() {

    private lateinit var calendarGridView: GridView
    private lateinit var monthYearText: TextView
    private lateinit var firebaseManager: FirebaseManager

    private val calendar = Calendar.getInstance()
    private val workoutDays = mutableMapOf<String, List<WorkoutSession>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_calendar)
        ThemeHelper.apply(this)

        firebaseManager = FirebaseManager()

        calendarGridView = findViewById(R.id.calendarGridView)
        monthYearText = findViewById(R.id.monthYearText)
        val prevMonthButton = findViewById<Button>(R.id.prevMonthButton)
        val nextMonthButton = findViewById<Button>(R.id.nextMonthButton)
        val backButton = findViewById<Button>(R.id.backButton)

        // Загружаем данные для текущего месяца
        loadWorkoutSessionsForCurrentMonth()

        // Навигация по месяцам
        prevMonthButton.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            loadWorkoutSessionsForCurrentMonth()
        }

        nextMonthButton.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            loadWorkoutSessionsForCurrentMonth()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun loadWorkoutSessionsForCurrentMonth() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        firebaseManager.getWorkoutSessionsForMonth(year, month) { sessions ->
            workoutDays.clear()

            // Группируем тренировки по дням
            sessions.forEach { session ->
                val dateString = CalendarDay.fromTimestamp(session.date)
                if (workoutDays.containsKey(dateString)) {
                    val existingSessions = workoutDays[dateString]!!.toMutableList()
                    existingSessions.add(session)
                    workoutDays[dateString] = existingSessions
                } else {
                    workoutDays[dateString] = listOf(session)
                }
            }

            updateCalendar()
        }
    }

    private fun updateCalendar() {
        updateMonthYearText()

        val days = getDaysInMonth()
        val adapter = CalendarAdapter(days, workoutDays) { date ->
            showWorkoutDialog(date)
        }
        calendarGridView.adapter = adapter
    }

    private fun updateMonthYearText() {
        val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale("ru"))
        monthYearText.text = monthYearFormat.format(calendar.time)
    }

    private fun getDaysInMonth(): List<CalendarDayItem> {
        val days = mutableListOf<CalendarDayItem>()

        val tempCalendar = calendar.clone() as Calendar
        tempCalendar.set(Calendar.DAY_OF_MONTH, 1)

        // Пустые дни для выравнивания
        val firstDayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK)
        val emptyDays = (firstDayOfWeek - Calendar.MONDAY + 7) % 7
        repeat(emptyDays) {
            days.add(CalendarDayItem.Empty)
        }

        val daysInMonth = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (day in 1..daysInMonth) {
            tempCalendar.set(Calendar.DAY_OF_MONTH, day)
            val dateString = CalendarDay.fromDate(tempCalendar.time)
            val sessions = workoutDays[dateString]
            val hasWorkout = sessions != null
            val emoji = if (hasWorkout) ThemeHelper.getEmojiForEmotion(this, sessions!!.firstOrNull()?.emotion ?: "") else ""
            days.add(CalendarDayItem.Day(day, dateString, hasWorkout, emoji))
        }

        return days
    }

    private fun showWorkoutDialog(date: String) {
        val sessions = workoutDays[date] ?: emptyList()

        if (sessions.isEmpty()) {
            showAddWorkoutDialog(date)
        } else {
            showWorkoutDetailsDialog(date, sessions)
        }
    }

    private fun showAddWorkoutDialog(date: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_workout, null)
        val sportSpinner = dialogView.findViewById<Spinner>(R.id.sportSpinner)
        val durationEditText = dialogView.findViewById<EditText>(R.id.durationEditText)
        val notesEditText = dialogView.findViewById<EditText>(R.id.notesEditText)
        val emotionSpinner = dialogView.findViewById<Spinner>(R.id.emotionSpinner)

        val sports = arrayOf("Бег", "Тренажерный зал", "Йога", "Плавание", "Велосипед", "Ходьба")
        val sportAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sports)
        sportAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sportSpinner.adapter = sportAdapter

        val options = ThemeHelper.getEmotionPickerOptions(this)
        val labels = listOf("Грустно", "Обычно", "Хорошо", "Отлично", "Превосходно")
        val emotionStrings = options.mapIndexed { i, (_, em) -> "$em ${labels.getOrElse(i) { "" }}" }
        val emotionAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, emotionStrings)
        emotionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        emotionSpinner.adapter = emotionAdapter
        emotionSpinner.setSelection(2) // "Хорошо" по умолчанию

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Добавить тренировку")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { _, _ ->
                val sport = sportSpinner.selectedItem.toString()
                val duration = durationEditText.text.toString().toIntOrNull() ?: 0
                val notes = notesEditText.text.toString()
                val emotion = options.getOrNull(emotionSpinner.selectedItemPosition)?.first ?: "happy"

                if (duration > 0) {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val d = inputFormat.parse(date)!!
                    val cal = Calendar.getInstance()
                    cal.time = d
                    cal.set(Calendar.HOUR_OF_DAY, 12)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)

                    saveWorkout(sport, duration, notes, emotion, cal.time)
                } else {
                    Toast.makeText(this, "Введите длительность тренировки", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .create()
        dialog.show()
    }

    private fun showWorkoutDetailsDialog(date: String, sessions: List<WorkoutSession>) {
        val sessionText = sessions.joinToString("\n") { session ->
            "• ${session.sportName} - ${session.duration} мин ${ThemeHelper.getEmojiForEmotion(this, session.emotion)}"
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Тренировки за ${formatDateForDisplay(date)}")
            .setMessage(sessionText)
            .setPositiveButton("Добавить еще") { _, _ ->
                showAddWorkoutDialog(date)
            }
            .setNeutralButton("Удалить все") { _, _ ->
                deleteAllWorkoutsForDate(date, sessions)
            }
            .setNegativeButton("Закрыть", null)
            .create()
        dialog.show()
    }

    private fun saveWorkout(sport: String, duration: Int, notes: String, emotion: String, date: Date) {
        firebaseManager.saveWorkoutSession(sport, duration, notes, emotion, date) { success, message ->
            if (success) {
                ShopManager(this).addCoins(5)
                Toast.makeText(this, "Тренировка сохранена! +5 монет", Toast.LENGTH_SHORT).show()
                loadWorkoutSessionsForCurrentMonth()
            } else {
                Toast.makeText(this, "Ошибка: $message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteAllWorkoutsForDate(date: String, sessions: List<WorkoutSession>) {
        sessions.forEach { session ->
            firebaseManager.deleteWorkoutSession(session.id) { success, message ->
                if (!success) {
                    Toast.makeText(this, "Ошибка удаления: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
        Toast.makeText(this, "Тренировки удалены", Toast.LENGTH_SHORT).show()
        loadWorkoutSessionsForCurrentMonth() // Перезагружаем данные
    }

    private fun formatDateForDisplay(date: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return outputFormat.format(inputFormat.parse(date)!!)
    }
}