package com.sstechcanada.todo.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.sstechcanada.todo.BuildConfig.VERSION_NAME
import com.sstechcanada.todo.R
import com.sstechcanada.todo.activities.auth.LoginActivity
import com.sstechcanada.todo.utils.Constants
import com.sstechcanada.todo.utils.SaveSharedPreference
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {
    private var TIME_OUT: Long = 2100

    companion object {

        private const val TAG = "SplashActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        setupNotificationChannel()
        Handler().postDelayed({
            fetchIntentData()
        }, TIME_OUT)
        Toasty.success(this, SaveSharedPreference.getAdsEnabled(this).toString(), Toast.LENGTH_SHORT).show()
        clearExistingPrefs()
        textViewVersion.text = "Version: $VERSION_NAME";
    }


    private fun fetchIntentData() {
        if (intent.extras != null) {
            intent.extras?.let {
                for (key in it.keySet()) {
                    val value = intent.extras?.get(Constants.SCREEN)
                    checkUserState(value)
                    Log.d(TAG, "Key: $key Value: $value")
                }
            }
        } else {
            checkUserState()
        }
    }

    private fun checkUserState(value: Any? = null) {
        if (SaveSharedPreference.getUserLogin(this) == true) {
            when (value) {
                Constants.TODO_LISTS_SCREEN -> {
                    startActivity(Intent(this, MasterTodoListActivity::class.java))
                    finish()
                }
                Constants.TODO_BENEFITS_SCREEN -> {
                    startActivity(Intent(this, AddBenefitsActivity::class.java))
                }
                Constants.APP_UPGRADE -> {
                    openGooglePlay()
                }
                Constants.TODO_PROFILE_SCREEN -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                Constants.TODO_RATE_APP -> {
                    val intent = Intent(this, MasterTodoListActivity::class.java)
                    intent.putExtra(Constants.TODO_RATE_APP, true)
                    startActivity(intent)
                    finish()
                }
                Constants.UPGRADE_SCREEN -> {
                    startActivity(Intent(this, AppUpgradeActivity::class.java))
                }
                else -> {
                    startActivity(Intent(this, MasterTodoListActivity::class.java))
                    finish()
                }
            }

        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            val channelId = getString(R.string.default_notification_channel_id)
            val channelName = getString(R.string.default_notification_channel_name)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW
                )
            )
        }
    }

    private fun openGooglePlay() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.sstechcanada.todo")))
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.sstechcanada.todo")))
        }
    }

    private fun clearExistingPrefs() {
        val editor = PreferenceManager.getDefaultSharedPreferences(this)
        if (editor.all.containsKey("user_state")) {
            editor.edit().clear().commit()
        }
        Log.d("Prefs", editor.all.keys.toString())
    }
}