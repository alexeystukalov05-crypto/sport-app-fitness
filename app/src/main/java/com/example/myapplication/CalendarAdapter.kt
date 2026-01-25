package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.myapplication.data.WorkoutSession

class CalendarAdapter(
    private val days: List<CalendarDayItem>,
    private val workoutDays: Map<String, List<WorkoutSession>>,
    private val onDayClick: (String) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = days.size

    override fun getItem(position: Int): CalendarDayItem = days[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(parent.context)
            .inflate(R.layout.calendar_day_item, parent, false)

        val dayItem = getItem(position)
        val dayButton = view.findViewById<TextView>(R.id.dayButton)

        when (dayItem) {
            is CalendarDayItem.Empty -> {
                // Пустая ячейка в начале месяца
                dayButton.text = ""
                dayButton.isEnabled = false
                dayButton.setBackgroundResource(R.drawable.day_button_empty)
            }
            is CalendarDayItem.Day -> {
                // Число дня
                dayButton.isEnabled = true

                // #region agent log
                try {
                    val logData = """
{"sessionId":"debug-session","runId":"pre-fix","hypothesisId":"H3","location":"CalendarAdapter.kt:40","message":"render_day","data":{"date":"${dayItem.date}","hasWorkout":${dayItem.hasWorkout}},"timestamp":${System.currentTimeMillis()}}
""".trimIndent()
                    java.io.File("c:\\Users\\1\\Desktop\\sport_app-master\\.cursor\\debug.log").appendText(logData + "\n")
                } catch (_: Exception) {
                }
                // #endregion

                if (dayItem.hasWorkout) {
                    dayButton.text = if (dayItem.emoji.isNotEmpty()) "${dayItem.dayNumber} ${dayItem.emoji}" else "${dayItem.dayNumber} •"
                    dayButton.setBackgroundResource(R.drawable.day_button_workout)
                } else {
                    dayButton.text = dayItem.dayNumber.toString()
                    dayButton.setBackgroundResource(R.drawable.day_button_normal)
                }

                dayButton.setOnClickListener {
                    onDayClick(dayItem.date)
                }
            }
        }

        return view
    }
}

// Модель для отображения дней в календаре
sealed class CalendarDayItem {
    object Empty : CalendarDayItem()
    data class Day(val dayNumber: Int, val date: String, val hasWorkout: Boolean, val emoji: String = "") : CalendarDayItem()
}