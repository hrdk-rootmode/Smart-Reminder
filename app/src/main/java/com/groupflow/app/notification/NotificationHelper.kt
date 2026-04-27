package com.groupflow.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.groupflow.app.MainActivity
import com.groupflow.app.R

object NotificationHelper {
    const val CHANNEL_ID = "reminder_channel"
    const val CHANNEL_NAME = "Reminders"
    const val CHANNEL_DESCRIPTION = "Notification channel for reminders"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d("NotificationHelper", "Notification channel created")
        }
    }

    fun scheduleReminder(
        context: Context,
        reminderId: String,
        title: String,
        description: String,
        priority: String,
        triggerTime: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        
        val intent = Intent(context, com.groupflow.app.receiver.ReminderReceiver::class.java).apply {
            putExtra("reminder_id", reminderId)
            putExtra("reminder_title", title)
            putExtra("reminder_description", description)
            putExtra("reminder_priority", priority)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        Log.d("NotificationHelper", "Scheduling reminder: $title at $triggerTime")
        
        // Schedule main reminder
        try {
            alarmManager.setExactAndAllowWhileIdle(
                android.app.AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            Log.d("NotificationHelper", "Reminder scheduled successfully")
        } catch (e: SecurityException) {
            // Fallback for devices that don't allow exact alarms
            Log.e("NotificationHelper", "Exact alarm not allowed, using fallback", e)
            alarmManager.setAndAllowWhileIdle(
                android.app.AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
        
        // Schedule pre-alert for urgent/high priority if time is more than 6 hours away
        if (priority == "URGENT" || priority == "HIGH") {
            val currentTime = System.currentTimeMillis()
            val hoursUntilReminder = (triggerTime - currentTime) / (1000 * 60 * 60)
            
            if (hoursUntilReminder > 6) {
                val preAlertTime = triggerTime - (5 * 60 * 1000) // 5 minutes before
                
                val preAlertIntent = Intent(context, com.groupflow.app.receiver.ReminderReceiver::class.java).apply {
                    putExtra("reminder_id", reminderId)
                    putExtra("reminder_title", "Upcoming: $title")
                    putExtra("reminder_description", "Reminder in 5 minutes")
                    putExtra("reminder_priority", priority)
                }
                
                val preAlertPendingIntent = PendingIntent.getBroadcast(
                    context,
                    reminderId.hashCode() + 1,
                    preAlertIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                
                try {
                    alarmManager.setExactAndAllowWhileIdle(
                        android.app.AlarmManager.RTC_WAKEUP,
                        preAlertTime,
                        preAlertPendingIntent
                    )
                    Log.d("NotificationHelper", "Pre-alert scheduled for $priority reminder")
                } catch (e: SecurityException) {
                    alarmManager.setAndAllowWhileIdle(
                        android.app.AlarmManager.RTC_WAKEUP,
                        preAlertTime,
                        preAlertPendingIntent
                    )
                }
            }
        }
    }

    fun cancelReminder(context: Context, reminderId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        
        // Cancel main reminder
        val intent = Intent(context, com.groupflow.app.receiver.ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        alarmManager.cancel(pendingIntent)
        
        // Cancel pre-alert reminder
        val preAlertPendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode() + 1,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        alarmManager.cancel(preAlertPendingIntent)
        
        Log.d("NotificationHelper", "Reminder cancelled: $reminderId")
    }
}
