package com.groupflow.app.service

import android.content.Context
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.groupflow.app.data.local.entity.Reminder
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Firebase Sync Service
 * Syncs reminders with Firebase Realtime Database
 * Supports offline mode with local cache
 */
class FirebaseSyncService(private val context: Context) {
    
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    
    /**
     * Get reference to user's reminders
     */
    private fun getRemindersRef(userId: String): DatabaseReference {
        return database.reference.child("users").child(userId).child("reminders")
    }
    
    /**
     * Sync reminder to Firebase
     */
    suspend fun syncReminder(userId: String, reminder: Reminder): Result<Unit> {
        return suspendCancellableCoroutine { continuation ->
            val ref = getRemindersRef(userId).child(reminder.reminderId)
            
            ref.setValue(reminder)
                .addOnSuccessListener {
                    continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { e ->
                    continuation.resume(Result.failure(e))
                }
        }
    }
    
    /**
     * Delete reminder from Firebase
     */
    suspend fun deleteReminder(userId: String, reminderId: String): Result<Unit> {
        return suspendCancellableCoroutine { continuation ->
            val ref = getRemindersRef(userId).child(reminderId)
            
            ref.removeValue()
                .addOnSuccessListener {
                    continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { e ->
                    continuation.resume(Result.failure(e))
                }
        }
    }
    
    /**
     * Listen to reminders from Firebase (real-time sync)
     */
    fun observeReminders(userId: String): Flow<List<Reminder>> {
        return callbackFlow {
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val reminders = mutableListOf<Reminder>()
                    for (childSnapshot in snapshot.children) {
                        val reminder = childSnapshot.getValue(Reminder::class.java)
                        if (reminder != null) {
                            reminders.add(reminder)
                        }
                    }
                    trySend(reminders)
                }
                
                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            }
            
            getRemindersRef(userId).addValueEventListener(listener)
            
            awaitClose {
                getRemindersRef(userId).removeEventListener(listener)
            }
        }
    }
    
    /**
     * Sync all local reminders to Firebase (for initial sync)
     */
    suspend fun syncAllReminders(userId: String, reminders: List<Reminder>): Result<Unit> {
        return suspendCancellableCoroutine { continuation ->
            val ref = getRemindersRef(userId)
            
            ref.setValue(reminders)
                .addOnSuccessListener {
                    continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { e ->
                    continuation.resume(Result.failure(e))
                }
        }
    }
    
    /**
     * Clear all reminders from Firebase
     */
    suspend fun clearAllReminders(userId: String): Result<Unit> {
        return suspendCancellableCoroutine { continuation ->
            val ref = getRemindersRef(userId)
            
            ref.removeValue()
                .addOnSuccessListener {
                    continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { e ->
                    continuation.resume(Result.failure(e))
                }
        }
    }
}
