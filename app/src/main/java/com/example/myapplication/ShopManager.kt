package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth

/**
 * Управление монетами, купленными товарами и выбранными опциями профиля.
 */
class ShopManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val uid: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    fun getCoins(): Int {
        val key = keyCoins()
        if (!prefs.contains(key)) {
            prefs.edit().putInt(key, DEFAULT_COINS).apply()
            return DEFAULT_COINS
        }
        return prefs.getInt(key, DEFAULT_COINS)
    }

    fun deductCoins(amount: Int): Boolean {
        val k = keyCoins()
        val cur = prefs.getInt(k, DEFAULT_COINS)
        if (cur < amount) return false
        prefs.edit().putInt(k, cur - amount).apply()
        return true
    }

    /** Начислить монеты (например, +5 за тренировку). */
    fun addCoins(amount: Int) {
        val k = keyCoins()
        val cur = prefs.getInt(k, DEFAULT_COINS)
        prefs.edit().putInt(k, cur + amount).apply()
    }

    fun getPurchasedIds(): Set<String> {
        val raw = prefs.getString(keyPurchased(), null) ?: return emptySet()
        return if (raw.isEmpty()) emptySet() else raw.split(',').map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    }

    fun isPurchased(itemId: String): Boolean = itemId in getPurchasedIds()

    fun addPurchased(itemId: String) {
        val set = getPurchasedIds().toMutableSet()
        set.add(itemId)
        prefs.edit().putString(keyPurchased(), set.joinToString(",")).apply()
    }

    fun getActiveOption(category: String): String? =
        prefs.getString("active_${category}_${uid ?: ""}", null)

    fun setActiveOption(category: String, itemId: String?) {
        val k = "active_${category}_${uid ?: ""}"
        if (itemId == null) prefs.edit().remove(k).apply()
        else prefs.edit().putString(k, itemId).apply()
    }

    private fun keyCoins() = "coins_${uid ?: "guest"}"
    private fun keyPurchased() = "purchased_${uid ?: "guest"}"

    companion object {
        private const val PREFS_NAME = "shop_prefs"
        private const val DEFAULT_COINS = 100
    }
}
