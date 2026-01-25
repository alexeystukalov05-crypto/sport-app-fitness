package com.example.myapplication

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.view.ViewGroup

/**
 * Применяет выбранные в Настройках профиля: тему (фон), эмодзи, значок, акцент карточек.
 */
object ThemeHelper {

    private const val DEFAULT_BG = "#F8C2BC"   // светлый розовый (бежевый)
    private const val THEME_BLUE = "#B3D4FC"
    private const val THEME_GREEN = "#C8E6C9"
    private const val THEME_ORANGE = "#FFE0B2"
    private const val THEME_PURPLE = "#E1BEE7"
    private const val THEME_LAVENDER = "#D1C4E9"
    private const val THEME_MINT = "#B2DFDB"
    private const val THEME_CORAL = "#FFCCBC"
    private const val THEME_PEACH = "#FFECB3"
    private const val THEME_SKY = "#B3E5FC"

    fun getBackgroundColor(context: android.content.Context): Int {
        val id = ShopManager(context).getActiveOption("theme") ?: return Color.parseColor(DEFAULT_BG)
        return when (id) {
            "theme_blue" -> Color.parseColor(THEME_BLUE)
            "theme_green" -> Color.parseColor(THEME_GREEN)
            "theme_orange" -> Color.parseColor(THEME_ORANGE)
            "theme_purple" -> Color.parseColor(THEME_PURPLE)
            "theme_lavender" -> Color.parseColor(THEME_LAVENDER)
            "theme_mint" -> Color.parseColor(THEME_MINT)
            "theme_coral" -> Color.parseColor(THEME_CORAL)
            "theme_peach" -> Color.parseColor(THEME_PEACH)
            "theme_sky" -> Color.parseColor(THEME_SKY)
            else -> Color.parseColor(DEFAULT_BG)
        }
    }

    /** Применить цвет фона к корневому view активности. */
    fun apply(activity: Activity) {
        val content = activity.window.decorView.findViewById<View>(android.R.id.content) as? ViewGroup
        val root = content?.getChildAt(0) ?: return
        root.setBackgroundColor(getBackgroundColor(activity))
    }

    /** Эмодзи для эмоции в зависимости от выбранного набора. */
    fun getEmojiForEmotion(context: android.content.Context, emotion: String): String {
        val set = ShopManager(context).getActiveOption("emojis")
        return when (set) {
            "emojis_sports" -> when (emotion) {
                "sad" -> "😔"
                "neutral" -> "😐"
                "happy" -> "💪"
                "very_happy" -> "🏆"
                "excellent" -> "🔥"
                else -> "💪"
            }
            else -> when (emotion) {
                "sad" -> "😢"
                "neutral" -> "😐"
                "happy" -> "😊"
                "very_happy" -> "😄"
                "excellent" -> "🤩"
                else -> "😊"
            }
        }
    }

    /** Список эмоций с эмодзи для выбора (эмодзи зависят от купленного набора). */
    fun getEmotionPickerOptions(context: android.content.Context): List<Pair<String, String>> {
        val keys = listOf("sad", "neutral", "happy", "very_happy", "excellent")
        return keys.map { it to getEmojiForEmotion(context, it) }
    }

    /** Значок для отображения рядом с именем. */
    fun getBadgeEmoji(context: android.content.Context): String? {
        val id = ShopManager(context).getActiveOption("badge") ?: return null
        return when (id) {
            "badge_star" -> "⭐"
            else -> null
        }
    }

    /** Цвет акцентной полоски на карточках тренировок (null = не показывать). */
    fun getCardAccentColor(context: android.content.Context): Int? {
        val id = ShopManager(context).getActiveOption("accent") ?: return null
        return when (id) {
            "accent_cards" -> Color.parseColor("#FF9800")
            else -> null
        }
    }
}
