package com.sstechcanada.todo.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sstechcanada.todo.R

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.action_settings)
    }
}