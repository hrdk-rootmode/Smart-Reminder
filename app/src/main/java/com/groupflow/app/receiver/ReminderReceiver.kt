package com.groupflow.app.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.groupflow.app.MainActivity
import com.groupflow.app.R
import com.groupflow.app.service.DoNotDisturbManager
import com.groupflow.app.service.AppAutomationHelper
import com.groupflow.app.notification.NotificationHelper

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ReminderReceiver", "Reminder received!")
        
        // Check if this is a dismiss action
        val action = intent.getStringExtra("action")
        val notificationId = intent.getIntExtra("notification_id", -1)
        
        if (action == "dismiss" && notificationId != -1) {
            // Cancel the notification
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.cancel(notificationId)
            Log.d("ReminderReceiver", "Notification dismissed: $notificationId")
            return
        }
        
        val reminderId = intent.getStringExtra("reminder_id") ?: return
        val reminderTitle = intent.getStringExtra("reminder_title") ?: return
        val reminderDescription = intent.getStringExtra("reminder_description") ?: ""
        val reminderPriority = intent.getStringExtra("reminder_priority") ?: "MEDIUM"
        val enableDND = intent.getBooleanExtra("enable_dnd", false)
        val appPackageName = intent.getStringExtra("app_package_name")
        val isCloseReminder = intent.getBooleanExtra("is_close_reminder", false)

        Log.d("ReminderReceiver", "Reminder: $reminderTitle, Priority: $reminderPriority, DND: $enableDND, App: $appPackageName, Close: $isCloseReminder")

        // Open app if package name is provided and this is not a close reminder
        if (appPackageName != null && !isCloseReminder) {
            val appAutomationHelper = AppAutomationHelper(context)
            if (appAutomationHelper.isAppInstalled(appPackageName)) {
                val appName = appAutomationHelper.getAppName(appPackageName)
                val opened = appAutomationHelper.openApp(appPackageName)
                if (opened) {
                    Log.d("ReminderReceiver", "Successfully opened app: $appName")
                } else {
                    Log.e("ReminderReceiver", "Failed to open app: $appName")
                }
            } else {
                Log.w("ReminderReceiver", "App not installed: $appPackageName")
            }
        }

        // Check if alarm sound is enabled
        val sharedPreferences = context.getSharedPreferences("reminder_prefs", MODE_PRIVATE)
        val alarmSoundEnabled = sharedPreferences.getBoolean("alarm_sound_enabled", true)
        val forceRing = sharedPreferences.getBoolean("force_ring", false)  // User preference to force ring

        // Check audio mode - if silent or vibrate, we should still vibrate
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val ringerMode = audioManager.ringerMode
        val isSilentOrVibrate = ringerMode == AudioManager.RINGER_MODE_SILENT || ringerMode == AudioManager.RINGER_MODE_VIBRATE

        val isUrgentOrHigh = reminderPriority == "URGENT" || reminderPriority == "HIGH"
        val channelId = if (isUrgentOrHigh) NotificationHelper.URGENT_CHANNEL_ID else NotificationHelper.CHANNEL_ID

        // Create notification with appropriate channel and settings
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(
                when (reminderPriority) {
                    "URGENT" -> "🚨 URGENT: $reminderTitle"
                    "HIGH" -> "⚠️ HIGH: $reminderTitle"
                    else -> reminderTitle
                }
            )
            .setContentText(if (reminderDescription.isNotEmpty()) reminderDescription else "Reminder")
            .setPriority(getNotificationPriority(reminderPriority))
            .setContentIntent(createPendingIntent(context, reminderId))
            .setCategory(NotificationCompat.CATEGORY_ALARM)  // Important for heads-up display
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)  // Allow dismissal by swipe
            .setOngoing(false)  // Don't make it ongoing by default
        
        // Urgent/High priority: make persistent but allow dismissal
        if (isUrgentOrHigh) {
            builder.setOngoing(false)  // Allow dismissal
                .setAutoCancel(true)  // Allow swipe to dismiss
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(createPendingIntent(context, reminderId), true)
            
            // Add dismiss action
            val dismissIntent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra("action", "dismiss")
                putExtra("notification_id", reminderId.hashCode())
            }
            val dismissPendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId.hashCode() + 100,
                dismissIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            builder.addAction(0, "Dismiss", dismissPendingIntent)
        } else {
            builder.setAutoCancel(true)
        }

        // Handle sound and vibration based on audio mode and user preferences
        // All priorities vibrate by default
        builder.setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
        
        if (alarmSoundEnabled) {
            if (forceRing) {
                // User wants to force ring regardless of silent mode
                val alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                builder.setSound(alarmSoundUri)
            } else if (isSilentOrVibrate) {
                // Silent or vibrate mode - vibrate only (no sound)
                builder.setSound(null)
            } else if (isUrgentOrHigh) {
                // Urgent/High priority in normal mode - compulsory ring
                val alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                builder.setSound(alarmSoundUri)
            } else {
                // Normal priority in normal mode - notification sound
                val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                builder.setSound(defaultSoundUri)
            }
        } else {
            // Sound disabled - still vibrate
            builder.setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
        }

        val notification = builder.build()

        // For urgent/high, use FLAG_INSISTENT to keep ringing until dismissed
        if (isUrgentOrHigh) {
            notification.flags = notification.flags or
                android.app.Notification.FLAG_INSISTENT
        }

        // Show notification
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(reminderId.hashCode(), notification)
        
        // Also vibrate directly if in silent mode (ensure it works even if notification vibration is blocked)
        if (isSilentOrVibrate && alarmSoundEnabled) {
            val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            vibrator.vibrate(longArrayOf(0, 500, 200, 500, 200, 500), -1)
        }
        
        // Restore DND if it was enabled
        if (enableDND) {
            val dndManager = DoNotDisturbManager(context)
            dndManager.disableDND()
            Log.d("ReminderReceiver", "DND disabled after reminder")
        }
        
        Log.d("ReminderReceiver", "Notification shown for priority: $reminderPriority, ringer mode: $ringerMode")
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
