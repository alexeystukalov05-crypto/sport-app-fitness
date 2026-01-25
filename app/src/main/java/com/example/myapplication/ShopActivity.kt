package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ShopActivity : AppCompatActivity() {

    private lateinit var shopManager: ShopManager
    private lateinit var balanceText: TextView
    private lateinit var adapter: ShopAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_shop)
        ThemeHelper.apply(this)

        shopManager = ShopManager(this)
        balanceText = findViewById(R.id.balanceText)
        val shopRecyclerView = findViewById<RecyclerView>(R.id.shopRecyclerView)
        val backButton = findViewById<Button>(R.id.backButton)

        val shopItems = ShopCatalog.items

        adapter = ShopAdapter(shopItems, shopManager.getPurchasedIds()) { item ->
            if (shopManager.isPurchased(item.id)) {
                Toast.makeText(this, "Уже куплено", Toast.LENGTH_SHORT).show()
                return@ShopAdapter
            }
            if (!shopManager.deductCoins(item.price)) {
                Toast.makeText(this, "Недостаточно монет", Toast.LENGTH_SHORT).show()
                return@ShopAdapter
            }
            shopManager.addPurchased(item.id)
            balanceText.text = "${shopManager.getCoins()} монет"
            adapter.updatePurchased(shopManager.getPurchasedIds())
            Toast.makeText(this, "Куплено: ${item.name}", Toast.LENGTH_SHORT).show()
        }
        shopRecyclerView.layoutManager = GridLayoutManager(this, 2)
        shopRecyclerView.adapter = adapter

        updateBalance()
        backButton.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        updateBalance()
    }

    private fun updateBalance() {
        balanceText.text = "${shopManager.getCoins()} монет"
    }
}

data class ShopItem(
    val id: String,
    val category: String,
    val name: String,
    val description: String,
    val price: Int,
    val icon: String
)

object ShopCatalog {
    val items = listOf(
        // Темы
        ShopItem("theme_blue", "theme", "Голубая тема", "Светло-голубой фон", 50, "🔵"),
        ShopItem("theme_green", "theme", "Зелёная тема", "Светло-зелёный фон", 40, "🌿"),
        ShopItem("theme_orange", "theme", "Оранжевая тема", "Тёплый оранжевый фон", 45, "🟠"),
        ShopItem("theme_purple", "theme", "Фиолетовая тема", "Светло-фиолетовый фон", 55, "🟣"),
        ShopItem("theme_lavender", "theme", "Лавандовая тема", "Нежно-лавандовый фон", 42, "💜"),
        ShopItem("theme_mint", "theme", "Мятная тема", "Свежий мятный фон", 38, "🌱"),
        ShopItem("theme_coral", "theme", "Коралловая тема", "Коралловый фон", 48, "🪸"),
        ShopItem("theme_peach", "theme", "Персиковая тема", "Мягкий персиковый фон", 44, "🍑"),
        ShopItem("theme_sky", "theme", "Небесная тема", "Голубое небо", 52, "☁️"),
        // Эмодзи
        ShopItem("emojis_sports", "emojis", "Спортивные смайлы", "💪🏆🔥 и др. для эмоций", 30, "💪"),
        // Значок
        ShopItem("badge_star", "badge", "Золотая звезда", "Значок рядом с именем", 75, "⭐"),
        // Акцент карточек
        ShopItem("accent_cards", "accent", "Акцент карточек", "Цветная полоска на карточках", 35, "📌")
    )
}

class ShopAdapter(
    private val shopItems: List<ShopItem>,
    private var purchasedIds: Set<String>,
    private val onBuyClick: (ShopItem) -> Unit
) : RecyclerView.Adapter<ShopAdapter.ViewHolder>() {

    fun updatePurchased(ids: Set<String>) {
        purchasedIds = ids
        notifyDataSetChanged()
    }

    class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.nameText)
        val descText: TextView = view.findViewById(R.id.descText)
        val priceText: TextView = view.findViewById(R.id.priceText)
        val iconText: TextView = view.findViewById(R.id.iconText)
        val buyButton: Button = view.findViewById(R.id.buyButton)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.shop_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = shopItems[position]
        holder.nameText.text = item.name
        holder.descText.text = item.description
        holder.priceText.text = "${item.price} монет"
        holder.iconText.text = item.icon

        val bought = item.id in purchasedIds
        holder.buyButton.isEnabled = !bought
        holder.buyButton.text = if (bought) "Куплено" else "Купить"
        holder.buyButton.setOnClickListener {
            if (!bought) onBuyClick(item)
        }
    }

    override fun getItemCount() = shopItems.size
}