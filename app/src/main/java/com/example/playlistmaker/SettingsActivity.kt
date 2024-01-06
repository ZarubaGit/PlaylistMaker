package com.example.playlistmaker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class SettingsActivity : AppCompatActivity() {


    private lateinit var backButton: ImageView
    private lateinit var shareButton: ImageView
    private lateinit var switchTheme: Switch
    private lateinit var supportSend: ImageView
    private lateinit var userAccepted: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        backButton = findViewById(R.id.backInMain)
        shareButton = findViewById(R.id.buttonShare)
        switchTheme = findViewById(R.id.switchTheme)
        supportSend = findViewById(R.id.sendInSupport)
        userAccepted = findViewById(R.id.userAccepted)

        // Загрузка сохраненной темы
        val sharedPreference = getSharedPreferences(FILE_FOR_SAVED, MODE_PRIVATE)
        val darkTheme = sharedPreference.getBoolean(DARK_THEME_MODE, false)
        switchTheme.isChecked = darkTheme
        setAppTheme(darkTheme)

        shareButton.setOnClickListener {
            val shareButton = Intent(Intent.ACTION_SEND)
            shareButton.type = "text/plain"
            shareButton.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.shareSub))
            shareButton.putExtra(Intent.EXTRA_TEXT, getString(R.string.linkOnAndDevol))
            startActivity(Intent.createChooser(shareButton, getString(R.string.titleForShareIcon)))
        }

        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            switchTheme(isChecked)
            savedThemePreference(isChecked)
        }

        supportSend.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SENDTO)
            val emailStudent = getString(R.string.emailMy)
            shareIntent.data = Uri.parse("mailto: $emailStudent")
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.messageForEmailDevolp))
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.messageForEmail))
            startActivity(shareIntent)
        }

        userAccepted.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(getString(R.string.offerta))
            startActivity(intent)
        }

        backButton.setOnClickListener {
            finish()
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

    private fun savedThemePreference(darkThemeEnable: Boolean) {
        val sharedPref = getSharedPreferences(FILE_FOR_SAVED, MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean(DARK_THEME_MODE, darkThemeEnable)
            apply()
        }
    }

    private fun switchTheme(darkThemeEnable: Boolean) {
        setAppTheme(darkThemeEnable)
        savedThemePreference(darkThemeEnable)
    }
}