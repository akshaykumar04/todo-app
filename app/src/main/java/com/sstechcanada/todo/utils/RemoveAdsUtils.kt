package com.sstechcanada.todo.utils

object RemoveAdsUtils {

    fun getTimeStampOfNextWeek(): String {
        val now = System.currentTimeMillis() / 1000
        val afterAWeek = now + 60//604800
        return afterAWeek.toString()
    }
}