package com.sstechcanada.todo.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.sstechcanada.todo.R
import com.sstechcanada.todo.activities.auth.LoginActivity
import com.sstechcanada.todo.utils.SaveSharedPreference

class SplashActivity : AppCompatActivity() {
    private var TIME_OUT: Long = 2100

    companion object {

        private const val TAG = "SplashActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Handler().postDelayed({
            checkUserState()
        }, TIME_OUT)

        setupNotificationChannel()
        fetchIntentData()

    }

    private fun checkUserState() {
        if (SaveSharedPreference.getUserLogin(this).equals("true")) {
            startActivity(Intent(this, MasterTodoListActivity::class.java))
            finish()
        } else {
            startActivity(Intent(this, LoginActivity::class.java))

            finish()
        }
    }

    private fun fetchIntentData() {
        intent.extras?.let {
            for (key in it.keySet()) {
                val value = intent.extras?.get(key)
                Log.d(TAG, "Key: $key Value: $value")
            }
        }
    }

    private fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            val channelId = getString(R.string.default_notification_channel_id)
            val channelName = getString(R.string.default_notification_channel_name)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(
                NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_LOW)
            )
        }
    }
}