package com.yourname.chat.data.manager

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

object ServerTimeManager {
    var serverOffset by mutableLongStateOf(0L)
        private set
    private val db = Firebase.database
    private val offsetRef = db.getReference(".info/serverTimeOffset")



    fun startSync() {
        offsetRef
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    serverOffset = snapshot.getValue(Long::class.java) ?: 0L
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun getNow() = System.currentTimeMillis() + serverOffset

}