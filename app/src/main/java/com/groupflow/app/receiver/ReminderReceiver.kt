package com.groupflow.app.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.groupflow.app.MainActivity
import com.groupflow.app.service.DoNotDisturbManager
import com.groupflow.app.service.AppAutomationHelper
import com.groupflow.app.notification.NotificationHelper
import java.util.concurrent.TimeUnit

class ReminderReceiver : BroadcastReceiver() {
    
    companion object {
        private var mediaPlayer: MediaPlayer? = null
        private val handler = Handler(Looper.getMainLooper())
        private var stopSoundRunnable: Runnable? = null
        
        fun stopCustomSound() {
            try {
                mediaPlayer?.let {
                    if (it.isPlaying) {
                        it.stop()
                    }
                    it.release()
                }
                mediaPlayer = null
                
                stopSoundRunnable?.let {
                    handler.removeCallbacks(it)
                    stopSoundRunnable = null
                }
                
                Log.d("ReminderReceiver", "Custom sound stopped")
            } catch (e: Exception) {
                Log.e("ReminderReceiver", "Error stopping custom sound", e)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ReminderReceiver", "Reminder received! Action: ${intent.getStringExtra("action")}")
        
        // Check if this is a dismiss or postpone action
        val action = intent.getStringExtra("action")
        val notificationId = intent.getIntExtra("notification_id", -1)
        val reminderIdForAction = intent.getStringExtra("reminder_id")
        val reminderTitleForAction = intent.getStringExtra("reminder_title")
        val reminderDescriptionForAction = intent.getStringExtra("reminder_description") ?: ""
        val reminderPriorityForAction = intent.getStringExtra("reminder_priority") ?: "MEDIUM"
        
        if (action == "dismiss" && reminderIdForAction != null) {
            // Stop any playing sound
            stopCustomSound()
            // Cancel the notification using the correct ID
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.cancel(reminderIdForAction.hashCode())
            Log.d("ReminderReceiver", "Notification dismissed: $reminderIdForAction")
            return
        }

        if (action == "postpone_10m" && reminderIdForAction != null && reminderTitleForAction != null) {
            stopCustomSound()
            // Cancel the notification using the correct ID
            NotificationManagerCompat.from(context).cancel(reminderIdForAction.hashCode())
            
            // Reschedule for 10 minutes later
            NotificationHelper.scheduleReminder(
                context = context,
                reminderId = reminderIdForAction,
                title = reminderTitleForAction,
                description = reminderDescriptionForAction,
                priority = reminderPriorityForAction,
                triggerTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10)
            )
            
            // Show toast to confirm postpone
            android.widget.Toast.makeText(
                context,
                "Reminder postponed for 10 minutes",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            
            Log.d("ReminderReceiver", "Reminder postponed by 10 minutes: $reminderIdForAction")
            return
        }
        
        val reminderId = intent.getStringExtra("reminder_id") ?: return
        val reminderTitle = intent.getStringExtra("reminder_title") ?: return
        val reminderDescription = intent.getStringExtra("reminder_description") ?: ""
        val reminderPriority = intent.getStringExtra("reminder_priority") ?: "MEDIUM"
        val enableDND = intent.getBooleanExtra("enable_dnd", false)
        val appPackageName = intent.getStringExtra("app_package_name")
        val isCloseReminder = intent.getBooleanExtra("is_close_reminder", false)
        val isPreAlert = intent.getBooleanExtra("is_pre_alert", false)

        Log.d("ReminderReceiver", "Processing reminder: $reminderTitle, Priority: $reminderPriority, DND: $enableDND, App: $appPackageName, Close: $isCloseReminder, PreAlert: $isPreAlert")

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

        // Get user preferences
        val sharedPreferences = context.getSharedPreferences("reminder_prefs", MODE_PRIVATE)
        val alarmSoundEnabled = sharedPreferences.getBoolean("alarm_sound_enabled", true)
        val forceRingUrgent = sharedPreferences.getBoolean("force_ring_urgent", true)
        val customSoundUri = sharedPreferences.getString("custom_sound_uri", null)
        val useSystemDefault = sharedPreferences.getBoolean("use_system_default_sound", true)

        // Check audio mode
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val ringerMode = audioManager.ringerMode
        val isSilentOrVibrate = ringerMode == AudioManager.RINGER_MODE_SILENT || ringerMode == AudioManager.RINGER_MODE_VIBRATE

        val isUrgentOrHigh = reminderPriority == "URGENT" || reminderPriority == "HIGH"
        val channelId = when (reminderPriority) {
            "URGENT" -> NotificationHelper.URGENT_CHANNEL_ID
            "HIGH" -> NotificationHelper.HIGH_CHANNEL_ID
            else -> NotificationHelper.CHANNEL_ID
        }

        // Create notification with appropriate channel and settings
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(
                when (reminderPriority) {
                    "URGENT" -> "🚨 URGENT: $reminderTitle"
                    "HIGH" -> "⚠️ HIGH: $reminderTitle"
                    else -> if (isPreAlert) "⏰ Upcoming: $reminderTitle" else reminderTitle
                }
            )
            .setContentText(if (reminderDescription.isNotEmpty()) reminderDescription else "Reminder")
            .setPriority(getNotificationPriority(reminderPriority))
            .setContentIntent(createPendingIntent(context, reminderId))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setOngoing(false)
        
        // Add actions for main reminders (not pre-alerts)
        if (!isPreAlert) {
            val dismissIntent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra("action", "dismiss")
                putExtra("reminder_id", reminderId)
                putExtra("reminder_title", reminderTitle)
                putExtra("reminder_description", reminderDescription)
                putExtra("reminder_priority", reminderPriority)
            }
            val dismissPendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId.hashCode() + 100,
                dismissIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            builder.addAction(0, "Stop", dismissPendingIntent)

            val postponeIntent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra("action", "postpone_10m")
                putExtra("reminder_id", reminderId)
                putExtra("reminder_title", reminderTitle)
                putExtra("reminder_description", reminderDescription)
                putExtra("reminder_priority", reminderPriority)
            }
            val postponePendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId.hashCode() + 101,
                postponeIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            builder.addAction(0, "Postpone 10m", postponePendingIntent)
        }

        // Handle urgent/high priority notifications
        if (isUrgentOrHigh && !isPreAlert) {
            builder.setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(createPendingIntent(context, reminderId), true)
        }

        // Handle sound and vibration
        val shouldPlaySound = alarmSoundEnabled && (!isSilentOrVibrate || (reminderPriority == "URGENT" && forceRingUrgent))
        
        if (shouldPlaySound) {
            // Play custom sound for urgent reminders that should force ring
            if (reminderPriority == "URGENT" && forceRingUrgent) {
                playCustomSound(context, customSoundUri, useSystemDefault, true)
            }
        }

        // Always vibrate (respecting system settings)
        builder.setVibrate(
            when (reminderPriority) {
                "URGENT" -> longArrayOf(0, 1000, 300, 1000, 300, 1000)
                "HIGH" -> longArrayOf(0, 500, 200, 500, 200, 500)
                else -> longArrayOf(0, 300, 200, 300)
            }
        )

        val notification = builder.build()

        // For urgent reminders, use FLAG_INSISTENT to keep ringing
        if (reminderPriority == "URGENT" && !isPreAlert) {
            notification.flags = notification.flags or android.app.Notification.FLAG_INSISTENT
        }

        // Show notification
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(reminderId.hashCode(), notification)
        
        // Force vibration for urgent reminders even in silent mode
        if (reminderPriority == "URGENT" && isSilentOrVibrate) {
            forceVibration(context)
        }
        
        // Restore DND if it was enabled
        if (enableDND) {
            val dndManager = DoNotDisturbManager(context)
            dndManager.disableDND()
            Log.d("ReminderReceiver", "DND disabled after reminder")
        }
        
        Log.d("ReminderReceiver", "Notification shown for priority: $reminderPriority, ringer mode: $ringerMode, force ring: $forceRingUrgent")
    }

    private fun playCustomSound(context: Context, customSoundUri: String?, useSystemDefault: Boolean, forcePlay: Boolean) {
        try {
            stopCustomSound() // Stop any existing sound
            
            val soundUri = when {
                !customSoundUri.isNullOrEmpty() -> Uri.parse(customSoundUri)
                useSystemDefault -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                else -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, soundUri)
                
                if (forcePlay) {
                    // Force play at maximum volume for urgent reminders
                    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    val originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
                    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
                    
                    setAudioStreamType(AudioManager.STREAM_ALARM)
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0)
                    
                    // Restore original volume after 30 seconds
                    stopSoundRunnable = Runnable {
                        stopCustomSound()
                        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, originalVolume, 0)
                    }
                    handler.postDelayed(stopSoundRunnable!!, 30000) // 30 seconds
                } else {
                    setAudioStreamType(AudioManager.STREAM_NOTIFICATION)
                }
                
                isLooping = forcePlay // Loop for urgent reminders
                prepare()
                start()
                
                Log.d("ReminderReceiver", "Custom sound started, force play: $forcePlay")
            }
        } catch (e: Exception) {
            Log.e("ReminderReceiver", "Failed to play custom sound", e)
        }
    }

    private fun forceVibration(context: Context) {
        try {
            val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            
            // Strong vibration pattern for urgent reminders
            vibrator.vibrate(longArrayOf(0, 1000, 300, 1000, 300, 1000), -1)
            Log.d("ReminderReceiver", "Force vibration triggered")
        } catch (e: Exception) {
            Log.e("ReminderReceiver", "Failed to force vibration", e)
        }
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
