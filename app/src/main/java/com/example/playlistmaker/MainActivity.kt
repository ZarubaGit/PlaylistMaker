package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

const val FILE_FOR_SAVED = "file_for_saved"
const val DARK_THEME_MODE = "setting_saves"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreference = getSharedPreferences(FILE_FOR_SAVED, MODE_PRIVATE)
        val darkTheme = sharedPreference.getBoolean(DARK_THEME_MODE, false)
        setAppTheme(darkTheme)

        setContentView(R.layout.activity_main)

        val searchButton = findViewById<Button>(R.id.buttonSearch)
        val mediaButton = findViewById<Button>(R.id.buttonMedia)
        val settingButton = findViewById<Button>(R.id.buttonSetting)

        searchButton.setOnClickListener {
            val displaySearch = Intent(this, SearchActivity::class.java)
            startActivity(displaySearch)
        }
        mediaButton.setOnClickListener {
            val displayMedia = Intent(this, MediatekaActivity::class.java)
            startActivity(displayMedia)
        }
        settingButton.setOnClickListener {
            val displaySettings = Intent(this, SettingsActivity::class.java)
            startActivity(displaySettings)
        }
    }

    private fun setAppTheme(darkThemeEnable: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnable) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }
}