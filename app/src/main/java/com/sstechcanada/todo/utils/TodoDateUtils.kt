package com.sstechcanada.todo.utils

import android.content.Context
import android.text.format.DateUtils
import android.util.Log
import java.util.*

object TodoDateUtils {
    private val TAG = TodoDateUtils::class.java.simpleName

    // zeroing out all the time info because I only want to compare due/overdue dates in terms
    // of which day it is right now
    val todaysDateInMillis: Long
        get() {
            val calendar = Calendar.getInstance(Locale.getDefault())
            // zeroing out all the time info because I only want to compare due/overdue dates in terms
            // of which day it is right now
            calendar[Calendar.HOUR_OF_DAY] = 0
            calendar[Calendar.MINUTE] = 0
            calendar[Calendar.SECOND] = 0
            calendar[Calendar.MILLISECOND] = 0
            val today = calendar.timeInMillis
            Log.d(TAG, "millis = $today")
            return today
        }

    fun formatDueDate(
        context: Context?,
        dueDateInMillis: Long
    ): String {
        return DateUtils.formatDateTime(
            context, dueDateInMillis,
            DateUtils.FORMAT_SHOW_DATE or
                    DateUtils.FORMAT_ABBREV_MONTH or
                    DateUtils.FORMAT_SHOW_YEAR or
                    DateUtils.FORMAT_ABBREV_WEEKDAY or
                    DateUtils.FORMAT_SHOW_WEEKDAY
        )
    }
}