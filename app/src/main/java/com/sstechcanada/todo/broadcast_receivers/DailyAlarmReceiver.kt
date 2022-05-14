package com.sstechcanada.todo.broadcast_receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sstechcanada.todo.services.DueCheckIntentService

class DailyAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val dueCheckIntent = Intent(context, DueCheckIntentService::class.java)
        dueCheckIntent.putExtra("foo", "bar")
        context.startService(dueCheckIntent)
    }

    companion object {
        const val REQUEST_CODE = 1201
        const val ACTION = "com.sstechcanada.todo.alarm"
    }
}