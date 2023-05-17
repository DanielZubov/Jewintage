package com.stato.jewintage

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stato.jewintage.adapters.CategoryAdapter
import com.stato.jewintage.model.Category
import com.stato.jewintage.viewmodel.FirebaseViewModel

class SettingsActivity : AppCompatActivity(), CategoryAdapter.OnCommissionChangeListener,
    SharedPreferences.OnSharedPreferenceChangeListener,
    FirebaseViewModel.OnCategoryDeletedListener {

    private lateinit var sharedPreferences: SharedPreferences
    private val viewModel: FirebaseViewModel by viewModels()
    private lateinit var adapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        adapter = CategoryAdapter(listOf(), this, viewModel) // Initialize adapter here

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        viewModel.liveCategoryData.observe(this) { categories ->
            adapter.updateCategories(categories)
        }
        viewModel.onCategoryDeletedListener = this
        viewModel.loadAllCategories()

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onCommissionChange(category: Category, commission: Float) {
        category.commission = commission
        viewModel.updateCategory(category)
    }

    override fun onCategoryDeleted(position: Int) {
        adapter.notifyItemRemoved(position)
    }

    override fun onDestroy() {
        super.onDestroy()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        viewModel.onCategoryDeletedListener = null
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            "commissionCard" -> {
                val commissionCard = sharedPreferences.getString(key, "0")?.toFloatOrNull() ?: 0f
                viewModel.updateCommissionCard(commissionCard)
            }

            "commissionCash" -> {
                val commissionCash = sharedPreferences.getString(key, "0")?.toFloatOrNull() ?: 0f
                viewModel.updateCommissionCash(commissionCash)
            }

            "theme" -> {
                val theme = sharedPreferences.getString(key, "")
                if (theme != null) {
                    setAppTheme(theme)
                    recreate()
                }
            }
        }
    }

    private fun setAppTheme(theme: String) {
        when (theme) {
            "auto" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {


            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }
}
