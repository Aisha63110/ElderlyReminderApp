
package com.example.elderlyreminderapp

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.elderlyreminderapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment =
            supportFragmentManager.findFragmentById(
                R.id.nav_host_fragment_content_main
            ) as NavHostFragment

        val navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(navController.graph)

        setupActionBarWithNavController(
            navController,
            appBarConfiguration
        )

        // Refresh toolbar menu whenever the user changes screens
        navController.addOnDestinationChangedListener {
                _, _, _ ->
            invalidateOptionsMenu()
        }

        binding.fab.setOnClickListener {
            // Keep existing FAB behaviour here if needed.
        }
    }

    // Create the Options Menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // Show or hide menu items depending on the current screen
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {

        val navController =
            findNavController(R.id.nav_host_fragment_content_main)

        val currentScreen = navController.currentDestination?.id

        // Hide Add Reminder while already on the Add/Edit screen
        menu.findItem(R.id.action_add)?.isVisible =
            currentScreen != R.id.SecondFragment

        // Hide Settings while already on Settings
        menu.findItem(R.id.action_settings)?.isVisible =
            currentScreen != R.id.SettingsFragment

        // Hide About while already on About
        menu.findItem(R.id.action_about)?.isVisible =
            currentScreen != R.id.AboutFragment

        return super.onPrepareOptionsMenu(menu)
    }

    // Handle menu clicks
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val navController =
            findNavController(R.id.nav_host_fragment_content_main)

        return when (item.itemId) {

            R.id.action_add -> {
                if (navController.currentDestination?.id != R.id.SecondFragment) {
                    navController.navigate(R.id.SecondFragment)
                }
                true
            }

            R.id.action_settings -> {
                if (navController.currentDestination?.id != R.id.SettingsFragment) {
                    navController.navigate(R.id.SettingsFragment)
                }
                true
            }

            R.id.action_about -> {
                if (navController.currentDestination?.id != R.id.AboutFragment) {
                    navController.navigate(R.id.AboutFragment)
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    // Handle toolbar Back navigation
    override fun onSupportNavigateUp(): Boolean {

        val navController =
            findNavController(R.id.nav_host_fragment_content_main)

        return navController.navigateUp(appBarConfiguration) ||
                super.onSupportNavigateUp()
    }
}
