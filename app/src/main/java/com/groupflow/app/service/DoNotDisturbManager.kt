package com.groupflow.app.service

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Do Not Disturb Manager
 * Manages DND mode for intelligent reminders
 */
class DoNotDisturbManager(private val context: Context) {
    
    private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    /**
     * Enable DND mode (set phone to vibrate)
     * @param restoreTime Time in milliseconds when to restore normal mode
     */
    fun enableDND(restoreTime: Long) {
        // Save original ringer mode
        val originalRingerMode = audioManager.ringerMode
        saveOriginalRingerMode(originalRingerMode, restoreTime)
        
        // Set to vibrate mode
        audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
        
        // If Android 7.0+, use NotificationManager DND policy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            enableInterruptionFilter()
        }
    }
    
    /**
     * Disable DND mode (restore original ringer mode)
     */
    fun disableDND() {
        val originalRingerMode = getOriginalRingerMode()
        if (originalRingerMode != -1) {
            audioManager.ringerMode = originalRingerMode
            clearSavedRingerMode()
        } else {
            // Default to normal mode
            audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
        }
        
        // If Android 7.0+, disable interruption filter
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            disableInterruptionFilter()
        }
    }
    
    /**
     * Check if DND is enabled
     */
    fun isDNDEnabled(): Boolean {
        return audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE
    }
    
    /**
     * Save original ringer mode to SharedPreferences
     */
    private fun saveOriginalRingerMode(mode: Int, restoreTime: Long) {
        val prefs = context.getSharedPreferences("dnd_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putInt("original_ringer_mode", mode)
            .putLong("dnd_restore_time", restoreTime)
            .apply()
    }
    
    /**
     * Get original ringer mode from SharedPreferences
     */
    private fun getOriginalRingerMode(): Int {
        val prefs = context.getSharedPreferences("dnd_prefs", Context.MODE_PRIVATE)
        return prefs.getInt("original_ringer_mode", -1)
    }
    
    /**
     * Clear saved ringer mode
     */
    private fun clearSavedRingerMode() {
        val prefs = context.getSharedPreferences("dnd_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .remove("original_ringer_mode")
            .remove("dnd_restore_time")
            .apply()
    }
    
    /**
     * Get DND restore time
     */
    fun getDNDRestoreTime(): Long {
        val prefs = context.getSharedPreferences("dnd_prefs", Context.MODE_PRIVATE)
        return prefs.getLong("dnd_restore_time", 0)
    }
    
    /**
     * Enable interruption filter (Android 7.0+)
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun enableInterruptionFilter() {
        if (notificationManager.isNotificationPolicyAccessGranted) {
            notificationManager.setInterruptionFilter(
                NotificationManager.INTERRUPTION_FILTER_PRIORITY
            )
        }
    }
    
    /**
     * Disable interruption filter (Android 7.0+)
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun disableInterruptionFilter() {
        if (notificationManager.isNotificationPolicyAccessGranted) {
            notificationManager.setInterruptionFilter(
                NotificationManager.INTERRUPTION_FILTER_ALL
            )
        }
    }
    
    /**
     * Check if notification policy access is granted
     */
    fun isNotificationPolicyAccessGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            notificationManager.isNotificationPolicyAccessGranted
        } else {
            true
        }
    }
}
