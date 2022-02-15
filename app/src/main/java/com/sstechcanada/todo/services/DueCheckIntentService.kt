package com.sstechcanada.todo.services

import android.app.IntentService
import android.content.Intent
import android.util.Log

class DueCheckIntentService : IntentService("DueCheckIntentService") {

    companion object {
        private const val TAG = "DueCheckIntentService"
    }

    override fun onHandleIntent(intent: Intent?) {
        Log.d(TAG, "Service running")
    }
}