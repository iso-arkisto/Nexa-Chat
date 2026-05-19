package com.yourname.chat.data.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.yourname.chat.data.model.user.User
import javax.inject.Inject

class UserPresenceManager @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    private val firestore: FirebaseFirestore
) {
    private var presenceListener: ValueEventListener? = null
    private var connectedRef: DatabaseReference? = null
    private var isTracking = false

    fun startTracking() {
        if (isTracking) return

        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        checkBanStatus(userId) { isBanned ->
            if (isBanned) return@checkBanStatus
            setupPresenceTracking(userId)
        }
    }

    private fun checkBanStatus(userId: String, callback: (Boolean) -> Unit) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                val user = doc.toObject(User::class.java)
                val isBanned = user?.moderation?.banInfo?.banned == true
                callback(isBanned)
            }
            .addOnFailureListener { e ->
                Log.e("UserPresence", "Ban check failed", e)
                callback(false)
            }
    }

    private fun setupPresenceTracking(userId: String) {
        val userStatusRef = database.getReference("/status/$userId")
        connectedRef = database.getReference(".info/connected")

        presenceListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: return

                if (connected) {
                    val statusData = mapOf(
                        "state" to "online",
                        "lastSeen" to ServerValue.TIMESTAMP,
                        "typing" to null,
                        "geo" to null
                    )

                    userStatusRef.onDisconnect().setValue(statusData.mapValues {
                        if (it.key == "state") "offline" else it.value
                    })
                    userStatusRef.setValue(statusData)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("UserPresence", "Presence error: ${error.message}")
            }
        }

        connectedRef?.addValueEventListener(presenceListener!!)
        isTracking = true
    }

    fun stopTracking() {
        presenceListener?.let { listener ->
            connectedRef?.removeEventListener(listener)
        }
        presenceListener = null
        connectedRef = null
        isTracking = false
    }
}