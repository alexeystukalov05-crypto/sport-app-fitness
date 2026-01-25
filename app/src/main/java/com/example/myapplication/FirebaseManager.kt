package com.example.myapplication

import android.util.Log
import com.example.myapplication.data.WorkoutSession
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.Calendar
import java.util.Date

class FirebaseManager {
    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "FirebaseManager"
    }

    // Регистрация пользователя - УПРОЩЕННАЯ ВЕРСИЯ
    fun registerUser(email: String, password: String, username: String, callback: (Boolean, String?) -> Unit) {
        Log.d(TAG, "Starting registration: email=$email, username=$username")

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Registration successful")
                    val user = auth.currentUser

                    if (user != null) {
                        // Обновляем профиль пользователя
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(username)
                            .build()

                        user.updateProfile(profileUpdates)
                            .addOnCompleteListener { profileTask ->
                                if (profileTask.isSuccessful) {
                                    Log.d(TAG, "Profile updated successfully")
                                    // Сохраняем в Firestore (но не блокируем на этом)
                                    saveUserToFirestore(user.uid, email, username) { firestoreSuccess, firestoreMessage ->
                                        if (firestoreSuccess) {
                                            Log.d(TAG, "User fully registered and saved to Firestore")
                                            callback(true, null)
                                        } else {
                                            Log.w(TAG, "User registered but Firestore save failed: $firestoreMessage")
                                            // ВСЕ РАВНО возвращаем успех, так как пользователь зарегистрирован
                                            callback(true, null)
                                        }
                                    }
                                } else {
                                    val errorMsg = profileTask.exception?.message ?: "Unknown profile update error"
                                    Log.e(TAG, "Profile update failed: $errorMsg")
                                    // ВСЕ РАВНО возвращаем успех, так как пользователь зарегистрирован
                                    callback(true, null)
                                }
                            }
                    } else {
                        Log.e(TAG, "User is null after registration")
                        callback(false, "User registration failed")
                    }
                } else {
                    val errorMsg = task.exception?.message ?: "Unknown registration error"
                    Log.e(TAG, "Registration failed: $errorMsg")
                    callback(false, errorMsg)
                }
            }
    }

    // Авторизация пользователя
    fun loginUser(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        Log.d(TAG, "Starting login: email=$email")

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Login successful")
                    callback(true, null)
                } else {
                    val errorMsg = task.exception?.message ?: "Unknown login error"
                    Log.e(TAG, "Login failed: $errorMsg")
                    callback(false, errorMsg)
                }
            }
    }

    // Сохранение пользователя в Firestore (асинхронно)
    private fun saveUserToFirestore(userId: String, email: String, username: String, callback: (Boolean, String?) -> Unit) {
        Log.d(TAG, "Saving user to Firestore: userId=$userId")

        val user = hashMapOf(
            "email" to email,
            "username" to username,
            "createdAt" to com.google.firebase.Timestamp.now(),
            "fitnessGoal" to "maintain_fitness",
            "lastLogin" to com.google.firebase.Timestamp.now()
        )

        db.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                Log.d(TAG, "User saved to Firestore successfully")
                callback(true, null)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to save user to Firestore: ${e.message}")
                callback(false, e.message)
            }
    }

    // Получение текущего пользователя
    fun getCurrentUser() = auth.currentUser

    // Выход пользователя
    fun logout() {
        Log.d(TAG, "Logging out user")
        auth.signOut()
    }

    // Проверка, авторизован ли пользователь
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
    // Сохранение тренировки
    fun saveWorkoutSession(
        sportName: String,
        duration: Int,
        notes: String,
        emotion: String,
        date: Date = Date(),
        callback: (Boolean, String?) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // #region agent log
            try {
                val logData = """
{"sessionId":"debug-session","runId":"pre-fix","hypothesisId":"H1","location":"FirebaseManager.kt:136","message":"saveWorkoutSession_user_null","data":{"userLoggedIn":false},"timestamp":${System.currentTimeMillis()}}
""".trimIndent()
                java.io.File("c:\\Users\\1\\Desktop\\sport_app-master\\.cursor\\debug.log").appendText(logData + "\n")
            } catch (_: Exception) {
            }
            // #endregion
            callback(false, "Пользователь не авторизован")
            return
        }

        val workoutSession = WorkoutSession(
            userId = currentUser.uid,
            sportName = sportName,
            date = Timestamp(date),
            duration = duration,
            notes = notes,
            emotion = emotion,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )

        db.collection("workout_sessions")
            .add(workoutSession)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "Workout session saved with ID: ${documentReference.id}")

                // #region agent log
                try {
                    val logData = """
{"sessionId":"debug-session","runId":"pre-fix","hypothesisId":"H1","location":"FirebaseManager.kt:155","message":"saveWorkoutSession_success","data":{"duration":$duration},"timestamp":${System.currentTimeMillis()}}
""".trimIndent()
                    java.io.File("c:\\Users\\1\\Desktop\\sport_app-master\\.cursor\\debug.log").appendText(logData + "\n")
                } catch (_: Exception) {
                }
                // #endregion

                callback(true, null)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error saving workout session", e)

                // #region agent log
                try {
                    val logData = """
{"sessionId":"debug-session","runId":"pre-fix","hypothesisId":"H1","location":"FirebaseManager.kt:159","message":"saveWorkoutSession_failure","data":{"hasError":true},"timestamp":${System.currentTimeMillis()}}
""".trimIndent()
                    java.io.File("c:\\Users\\1\\Desktop\\sport_app-master\\.cursor\\debug.log").appendText(logData + "\n")
                } catch (_: Exception) {
                }
                // #endregion

                callback(false, e.message)
            }
    }

    // Получение тренировок за определенный месяц
    fun getWorkoutSessionsForMonth(
        year: Int,
        month: Int,
        callback: (List<WorkoutSession>) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            callback(emptyList())
            return
        }

        // Создаем диапазон дат для месяца
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1, 0, 0, 0)
        val startDate = calendar.time

        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endDate = calendar.time

        // ВАЖНО: избегаем сложного составного индекса (userId + диапазон date),
        // поэтому фильтруем по дате в памяти, а в Firestore запрашиваем только по userId.
        db.collection("workout_sessions")
            .whereEqualTo("userId", currentUser.uid)
            .get()
            .addOnSuccessListener { documents ->
                val sessions = mutableListOf<WorkoutSession>()
                for (document in documents) {
                    val session = document.toObject(WorkoutSession::class.java)
                    val sessionDate = session.date.toDate()
                    if (!sessionDate.before(startDate) && !sessionDate.after(endDate)) {
                        sessions.add(session.copy(id = document.id))
                    }
                }
                Log.d(TAG, "Loaded ${sessions.size} workout sessions for month")
                callback(sessions)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading workout sessions", e)
                callback(emptyList())
            }
    }

    // Получение тренировок за конкретный день
    fun getWorkoutSessionsForDate(
        date: Date,
        callback: (List<WorkoutSession>) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            callback(emptyList())
            return
        }

        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startDate = calendar.time

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endDate = calendar.time

        db.collection("workout_sessions")
            .whereEqualTo("userId", currentUser.uid)
            .get()
            .addOnSuccessListener { documents ->
                val sessions = mutableListOf<WorkoutSession>()
                for (document in documents) {
                    val session = document.toObject(WorkoutSession::class.java)
                    val sessionDate = session.date.toDate()
                    if (!sessionDate.before(startDate) && !sessionDate.after(endDate)) {
                        sessions.add(session.copy(id = document.id))
                    }
                }
                callback(sessions)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading workout sessions for date", e)
                callback(emptyList())
            }
    }

    // Удаление тренировки
    fun deleteWorkoutSession(sessionId: String, callback: (Boolean, String?) -> Unit) {
        db.collection("workout_sessions")
            .document(sessionId)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "Workout session deleted: $sessionId")
                callback(true, null)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error deleting workout session", e)
                callback(false, e.message)
            }
    }

    // Общее количество тренировок пользователя
    fun getTotalWorkoutCount(callback: (Int) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            callback(0)
            return
        }

        db.collection("workout_sessions")
            .whereEqualTo("userId", currentUser.uid)
            .get()
            .addOnSuccessListener { documents ->
                callback(documents.size())
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading total workout count", e)
                callback(0)
            }
    }

    /** Все тренировки пользователя, отсортированные по дате (новые первые). */
    fun getAllWorkoutSessionsOrderedByDate(callback: (List<WorkoutSession>) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            callback(emptyList())
            return
        }
        db.collection("workout_sessions")
            .whereEqualTo("userId", currentUser.uid)
            .get()
            .addOnSuccessListener { documents ->
                val list = documents.map { doc ->
                    doc.toObject(WorkoutSession::class.java).copy(id = doc.id)
                }.sortedByDescending { it.date.toDate().time }
                callback(list)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading workout sessions", e)
                callback(emptyList())
            }
    }
}


// Модель тренировки
data class Workout(
    val sport: String,
    val duration: Int,
    val notes: String,
    val emotion: String
)