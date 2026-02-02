package com.hydration.tracker.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.hydration.tracker.R
import com.hydration.tracker.databinding.ActivityMainBinding
import com.hydration.tracker.utils.fadeIn
import dagger.hilt.android.AndroidEntryPoint
import com.hydration.tracker.utils.LocaleHelper
import android.content.Context // Ensure this is also imported


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Get saved language from preferences
        val prefs = newBase.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        val languageCode = prefs.getString("language", "en") ?: "en"

        val context = LocaleHelper.setLocale(newBase, languageCode)
        super.attachBaseContext(context)
    }


    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        animateEntrance()
    }


    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)

        // Animate icon selection
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            navController.navigate(item.itemId)
            true
        }
    }

    private fun animateEntrance() {
        binding.bottomNavigation.translationY = 200f
        binding.bottomNavigation.animate()
            .translationY(0f)
            .setDuration(400)
            .start()
    }
}