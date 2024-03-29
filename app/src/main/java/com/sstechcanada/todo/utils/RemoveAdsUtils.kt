package com.sstechcanada.todo.utils

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

object RemoveAdsUtils {

    fun getTimeStampOfNextWeek(previousTimeStamp: String?= null): String {
        return if (previousTimeStamp == null){
            (getServerTime() + 604800000).toString()
//            (getServerTime() + 100000).toString()
        } else {
            (previousTimeStamp.toLong() + 604800000).toString()
//            (previousTimeStamp.toLong() + 100000).toString()
        }
    }

    fun getServerTime(): Long {
        var serverOffset = 0L
        FirebaseDatabase.getInstance().goOffline()
        FirebaseDatabase.getInstance().goOnline()
        val ref = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                serverOffset = (snapshot.value as Long).toLong()
            }
        })

        return (Date().time + serverOffset)
    }

}