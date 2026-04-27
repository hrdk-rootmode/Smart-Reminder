package com.groupflow.app.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.groupflow.app.MainActivity
import com.groupflow.app.R
import com.groupflow.app.service.DoNotDisturbManager

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ReminderReceiver", "Reminder received!")
        
        val reminderId = intent.getStringExtra("reminder_id") ?: return
        val reminderTitle = intent.getStringExtra("reminder_title") ?: return
        val reminderDescription = intent.getStringExtra("reminder_description") ?: ""
        val reminderPriority = intent.getStringExtra("reminder_priority") ?: "MEDIUM"
        val enableDND = intent.getBooleanExtra("enable_dnd", false)

        Log.d("ReminderReceiver", "Reminder: $reminderTitle, Priority: $reminderPriority, DND: $enableDND")

        // Check if alarm sound is enabled
        val sharedPreferences = context.getSharedPreferences("reminder_prefs", MODE_PRIVATE)
        val alarmSoundEnabled = sharedPreferences.getBoolean("alarm_sound_enabled", true)

        // Create notification
        val notification = NotificationCompat.Builder(context, "reminder_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(reminderTitle)
            .setContentText(if (reminderDescription.isNotEmpty()) reminderDescription else "Reminder")
            .setPriority(getNotificationPriority(reminderPriority))
            .setAutoCancel(true)
            .setContentIntent(createPendingIntent(context, reminderId))
            .build()

        // Play sound if enabled
        if (alarmSoundEnabled) {
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            notification.sound = defaultSoundUri
        }

        // Show notification
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(reminderId.hashCode(), notification)
        
        // Restore DND if it was enabled
        if (enableDND) {
            val dndManager = DoNotDisturbManager(context)
            dndManager.disableDND()
            Log.d("ReminderReceiver", "DND disabled after reminder")
        }
        
        Log.d("ReminderReceiver", "Notification shown")
    }

    private fun getNotificationPriority(priority: String): Int {
        return when (priority) {
            "URGENT" -> NotificationCompat.PRIORITY_MAX
            "HIGH" -> NotificationCompat.PRIORITY_HIGH
            "MEDIUM" -> NotificationCompat.PRIORITY_DEFAULT
            "LOW" -> NotificationCompat.PRIORITY_LOW
            else -> NotificationCompat.PRIORITY_DEFAULT
        }
    }

    private fun createPendingIntent(context: Context, reminderId: String): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("reminder_id", reminderId)
        }
        return PendingIntent.getActivity(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}
