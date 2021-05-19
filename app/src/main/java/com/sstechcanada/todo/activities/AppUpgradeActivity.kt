package com.sstechcanada.todo.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sstechcanada.todo.R
import kotlinx.android.synthetic.main.activity_app_upgrade.*

class AppUpgradeActivity : AppCompatActivity() {
    val premium_app = "com.sstechcanada.todo_plus"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_upgrade)

        buttonUpgrade.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$premium_app")))
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$premium_app")))
            }
        }

        fabBack.setOnClickListener { super.onBackPressed() }
    }
}