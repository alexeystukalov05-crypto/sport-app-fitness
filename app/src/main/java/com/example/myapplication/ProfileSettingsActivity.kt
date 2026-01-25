package com.example.myapplication

import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class ProfileSettingsActivity : AppCompatActivity() {

    private lateinit var shopManager: ShopManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_profile_settings)
        ThemeHelper.apply(this)

        shopManager = ShopManager(this)
        val purchased = shopManager.getPurchasedIds()

        val themeSpinner = findViewById<Spinner>(R.id.themeSpinner)
        val emojisSpinner = findViewById<Spinner>(R.id.emojisSpinner)
        val badgeSpinner = findViewById<Spinner>(R.id.badgeSpinner)
        val accentSpinner = findViewById<Spinner>(R.id.accentSpinner)

        setupSpinner("theme", themeSpinner, purchased)
        setupSpinner("emojis", emojisSpinner, purchased)
        setupSpinner("badge", badgeSpinner, purchased)
        setupSpinner("accent", accentSpinner, purchased)

        findViewById<Button>(R.id.backButton).setOnClickListener { finish() }
    }

    private fun setupSpinner(category: String, spinner: Spinner, purchased: Set<String>) {
        val options = mutableListOf<Pair<String, String?>>()
        options.add("По умолчанию" to null)
        ShopCatalog.items
            .filter { it.category == category && it.id in purchased }
            .forEach { options.add(it.name to it.id) }

        val labels = options.map { it.first }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, labels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        val active = shopManager.getActiveOption(category)
        val idx = options.indexOfFirst { it.second == active }.let { if (it < 0) 0 else it }
        spinner.setSelection(idx)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, pos: Int, id: Long) {
                val selectedId = options.getOrNull(pos)?.second
                shopManager.setActiveOption(category, selectedId)
                if (category == "theme") ThemeHelper.apply(this@ProfileSettingsActivity)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
}
