package com.sstechcanada.todo.broadcast_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sstechcanada.todo.services.DueCheckIntentService;

public class DailyAlarmReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 1201;
    public static final String ACTION = "com.sstechcanada.todo.alarm";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent dueCheckIntent = new Intent(context, DueCheckIntentService.class);
        dueCheckIntent.putExtra("foo", "bar");
        context.startService(dueCheckIntent);
    }
}
