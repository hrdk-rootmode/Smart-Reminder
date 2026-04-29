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
    const val URGENT_CHANNEL_ID = "urgent_reminder_channel"
    const val URGENT_CHANNEL_NAME = "Urgent Reminders"
    const val URGENT_CHANNEL_DESCRIPTION = "High-priority notifications that cannot be missed"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Delete existing channels first to ensure sound is updated
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.deleteNotificationChannel(CHANNEL_ID)
            notificationManager.deleteNotificationChannel(URGENT_CHANNEL_ID)
            
            // Standard reminder channel with sound
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                enableLights(true)
                // Set default notification sound
                val soundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
                setSound(soundUri, android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build())
                // Ensure sound is enabled
                enableVibration(true)
            }
            
            // Urgent/High priority channel - maximum importance with alarm sound
            val urgentChannel = NotificationChannel(
                URGENT_CHANNEL_ID,
                URGENT_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = URGENT_CHANNEL_DESCRIPTION
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                enableLights(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setBypassDnd(true)
                // Use alarm sound for urgent notifications
                val alarmSoundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
                    ?: android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
                setSound(alarmSoundUri, android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build())
            }
            
            notificationManager.createNotificationChannel(channel)
            notificationManager.createNotificationChannel(urgentChannel)
            Log.d("NotificationHelper", "Notification channels recreated with sound")
        }
    }

    fun scheduleReminder(
        context: Context,
        reminderId: String,
        title: String,
        description: String,
        priority: String,
        triggerTime: Long,
        enableDND: Boolean = false,
        appPackageName: String? = null,
        endTime: Long? = null
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        
        val intent = Intent(context, com.groupflow.app.receiver.ReminderReceiver::class.java).apply {
            putExtra("reminder_id", reminderId)
            putExtra("reminder_title", title)
            putExtra("reminder_description", description)
            putExtra("reminder_priority", priority)
            putExtra("enable_dnd", enableDND)
            putExtra("app_package_name", appPackageName)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        Log.d("NotificationHelper", "Scheduling reminder: $title at $triggerTime, app: $appPackageName")
        
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
        
        // Schedule close reminder if endTime is specified
        if (endTime != null && appPackageName != null) {
            val closeIntent = Intent(context, com.groupflow.app.receiver.ReminderReceiver::class.java).apply {
                putExtra("reminder_id", reminderId)
                putExtra("reminder_title", "Close ${com.groupflow.app.service.AppAutomationHelper(context).getAppName(appPackageName)}")
                putExtra("reminder_description", "Time to close the app")
                putExtra("reminder_priority", "MEDIUM")
                putExtra("is_close_reminder", true)
                putExtra("app_package_name", appPackageName)
            }
            
            val closePendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId.hashCode() + 1000,
                closeIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    endTime,
                    closePendingIntent
                )
                Log.d("NotificationHelper", "Close reminder scheduled at $endTime")
            } catch (e: SecurityException) {
                alarmManager.setAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    endTime,
                    closePendingIntent
                )
            }
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
