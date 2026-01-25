package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.WorkoutSession
import java.text.SimpleDateFormat
import java.util.Locale

class WorkoutHistoryActivity : AppCompatActivity() {

    private val firebaseManager = FirebaseManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_workout_history)
        ThemeHelper.apply(this)

        val workoutsRecyclerView = findViewById<RecyclerView>(R.id.workoutsRecyclerView)
        val backButton = findViewById<Button>(R.id.backButton)
        workoutsRecyclerView.layoutManager = LinearLayoutManager(this)

        loadHistory()

        backButton.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        loadHistory()
    }

    private fun loadHistory() {
        firebaseManager.getAllWorkoutSessionsOrderedByDate { sessions ->
            runOnUiThread {
                val items = sessions.map { s -> toWorkoutItem(s) }
                findViewById<RecyclerView>(R.id.workoutsRecyclerView).adapter = WorkoutHistoryAdapter(items)
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

data class WorkoutItem(
    val sport: String,
    val duration: String,
    val emotion: String,
    val date: String
)

class WorkoutHistoryAdapter(private val workouts: List<WorkoutItem>) :
    androidx.recyclerview.widget.RecyclerView.Adapter<WorkoutHistoryAdapter.ViewHolder>() {

    class ViewHolder(view: android.view.View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val sportText: TextView = view.findViewById(R.id.sportText)
        val durationText: TextView = view.findViewById(R.id.durationText)
        val emotionText: TextView = view.findViewById(R.id.emotionText)
        val dateText: TextView = view.findViewById(R.id.dateText)
        val accentBar: View = view.findViewById(R.id.accentBar)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.workout_history_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workout = workouts[position]
        holder.sportText.text = workout.sport
        holder.durationText.text = workout.duration
        holder.emotionText.text = workout.emotion
        holder.dateText.text = workout.date
        val acc = ThemeHelper.getCardAccentColor(holder.itemView.context)
        holder.accentBar.visibility = if (acc != null) View.VISIBLE else View.GONE
        if (acc != null) holder.accentBar.setBackgroundColor(acc)
    }

    override fun getItemCount() = workouts.size
}

/** Адаптер для ListView последних тренировок. */
class SimpleWorkoutListAdapter(
    private val context: Context,
    private val items: List<WorkoutItem>
) : BaseAdapter() {

    private val inflater = LayoutInflater.from(context)

    override fun getCount() = items.size
    override fun getItem(position: Int) = items[position]
    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v = convertView ?: inflater.inflate(R.layout.workout_history_item, parent, false)
        val w = items[position]
        v.findViewById<TextView>(R.id.sportText).text = w.sport
        v.findViewById<TextView>(R.id.durationText).text = w.duration
        v.findViewById<TextView>(R.id.emotionText).text = w.emotion
        v.findViewById<TextView>(R.id.dateText).text = w.date
        val bar = v.findViewById<View>(R.id.accentBar)
        val acc = ThemeHelper.getCardAccentColor(context)
        bar.visibility = if (acc != null) View.VISIBLE else View.GONE
        if (acc != null) bar.setBackgroundColor(acc)
        return v
    }
}