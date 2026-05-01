package com.groupflow.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
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
    const val HIGH_CHANNEL_ID = "high_reminder_channel"
    const val HIGH_CHANNEL_NAME = "High Priority Reminders"
    const val HIGH_CHANNEL_DESCRIPTION = "High-priority reminders with enhanced notifications"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Delete existing channels to ensure proper sound configuration
            notificationManager.deleteNotificationChannel(CHANNEL_ID)
            notificationManager.deleteNotificationChannel(URGENT_CHANNEL_ID)
            notificationManager.deleteNotificationChannel(HIGH_CHANNEL_ID)
            
            // Get user's preferred sound settings
            val prefs = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
            val customSoundUri = prefs.getString("custom_sound_uri", null)
            val useSystemDefault = prefs.getBoolean("use_system_default_sound", true)
            
            // Determine sound URI
            val defaultSoundUri = when {
                !customSoundUri.isNullOrEmpty() -> Uri.parse(customSoundUri)
                useSystemDefault -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                else -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
            
            val alarmSoundUri = when {
                !customSoundUri.isNullOrEmpty() -> Uri.parse(customSoundUri)
                useSystemDefault -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) 
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                else -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
            
            // Standard reminder channel
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                enableLights(true)
                setSound(defaultSoundUri, AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build())
            }
            
            // High priority channel
            val highChannel = NotificationChannel(
                HIGH_CHANNEL_ID,
                HIGH_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = HIGH_CHANNEL_DESCRIPTION
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                enableLights(true)
                setSound(alarmSoundUri, AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build())
            }
            
            // Urgent channel - maximum priority with alarm sound
            val urgentChannel = NotificationChannel(
                URGENT_CHANNEL_ID,
                URGENT_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = URGENT_CHANNEL_DESCRIPTION
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 300, 1000, 300, 1000)
                enableLights(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setBypassDnd(true) // Bypass Do Not Disturb for urgent reminders
                setSound(alarmSoundUri, AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                    .build())
            }
            
            notificationManager.createNotificationChannel(channel)
            notificationManager.createNotificationChannel(highChannel)
            notificationManager.createNotificationChannel(urgentChannel)
            Log.d("NotificationHelper", "Notification channels created with custom sound settings")
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
        
        Log.d("NotificationHelper", "Scheduling reminder: $title at $triggerTime (${java.util.Date(triggerTime)}), priority: $priority")
        
        // Schedule main reminder
        try {
            alarmManager.setExactAndAllowWhileIdle(
                android.app.AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            Log.d("NotificationHelper", "Reminder scheduled successfully with exact alarm")
        } catch (e: SecurityException) {
            // Fallback for devices that don't allow exact alarms
            Log.w("NotificationHelper", "Exact alarm not allowed, using fallback", e)
            alarmManager.setAndAllowWhileIdle(
                android.app.AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
        
        // Schedule close reminder if endTime is specified
        if (endTime != null && appPackageName != null) {
            val closeIntent = Intent(context, com.groupflow.app.receiver.ReminderReceiver::class.java).apply {
                putExtra("reminder_id", "${reminderId}_close")
                putExtra("reminder_title", "Close ${getAppName(context, appPackageName)}")
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
        
        // Schedule pre-alert for urgent/high priority if time is more than 5 minutes away
        if (priority == "URGENT" || priority == "HIGH") {
            val currentTime = System.currentTimeMillis()
            val minutesUntilReminder = (triggerTime - currentTime) / (1000 * 60)
            
            if (minutesUntilReminder > 5) {
                val preAlertTime = triggerTime - (5 * 60 * 1000) // 5 minutes before
                
                val preAlertIntent = Intent(context, com.groupflow.app.receiver.ReminderReceiver::class.java).apply {
                    putExtra("reminder_id", "${reminderId}_prealert")
                    putExtra("reminder_title", "Upcoming: $title")
                    putExtra("reminder_description", "Reminder in 5 minutes")
                    putExtra("reminder_priority", priority)
                    putExtra("is_pre_alert", true)
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
                    Log.d("NotificationHelper", "Pre-alert scheduled for $priority reminder at ${java.util.Date(preAlertTime)}")
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
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
        
        // Cancel pre-alert reminder
        val preAlertPendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode() + 1,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        
        if (preAlertPendingIntent != null) {
            alarmManager.cancel(preAlertPendingIntent)
            preAlertPendingIntent.cancel()
        }
        
        // Cancel close reminder
        val closePendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode() + 1000,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        
        if (closePendingIntent != null) {
            alarmManager.cancel(closePendingIntent)
            closePendingIntent.cancel()
        }
        
        Log.d("NotificationHelper", "All reminders cancelled for: $reminderId")
    }
    
    private fun getAppName(context: Context, packageName: String): String {
        return try {
            val packageManager = context.packageManager
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }
}
