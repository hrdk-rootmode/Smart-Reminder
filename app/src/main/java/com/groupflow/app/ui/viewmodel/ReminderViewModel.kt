package com.groupflow.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.groupflow.app.data.local.AppDatabase
import com.groupflow.app.data.local.entity.Reminder
import com.groupflow.app.data.local.entity.ReminderPriority
import com.groupflow.app.data.local.entity.ReminderStatus
import com.groupflow.app.data.local.entity.ReminderType
import com.groupflow.app.notification.NotificationHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ReminderViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val reminderDao = database.reminderDao()
    private val context = application

    // For guest users, use a fixed ID; for logged-in users, this will be updated
    private val _currentUserId = MutableStateFlow("guest_user")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    fun setUserId(userId: String) {
        _currentUserId.value = userId
    }

    // Get all reminders for current user
    fun getUserReminders(): Flow<List<Reminder>> {
        return reminderDao.getUserReminders(_currentUserId.value)
    }

    // Get upcoming reminders
    fun getUpcomingReminders(startTime: Long, endTime: Long): Flow<List<Reminder>> {
        return reminderDao.getUpcomingReminders(_currentUserId.value, startTime, endTime)
    }

    // Get overdue reminders
    fun getOverdueReminders(currentTime: Long): Flow<List<Reminder>> {
        return reminderDao.getOverdueReminders(_currentUserId.value, currentTime)
    }

    // Get reminders by priority
    fun getRemindersByPriority(priority: ReminderPriority): Flow<List<Reminder>> {
        return reminderDao.getRemindersByPriority(_currentUserId.value, priority)
    }

    // Create a new reminder
    fun createReminder(
        title: String,
        description: String = "",
        triggerTime: Long,
        priority: ReminderPriority = ReminderPriority.MEDIUM,
        isRecurring: Boolean = false,
        appPackageName: String? = null,
        endTime: Long? = null
    ) {
        viewModelScope.launch {
            val reminderId = UUID.randomUUID().toString()
            val reminder = Reminder(
                reminderId = reminderId,
                userId = _currentUserId.value,
                title = title,
                description = description,
                triggerTime = triggerTime,
                priority = priority,
                isRecurring = isRecurring,
                reminderType = ReminderType.TIME_BASED,
                appPackageName = appPackageName,
                endTime = endTime
            )
            reminderDao.insertReminder(reminder)
            
            // Schedule notification
            NotificationHelper.scheduleReminder(
                context,
                reminderId,
                title,
                description,
                priority.name,
                triggerTime,
                appPackageName = appPackageName,
                endTime = endTime
            )
        }
    }

    // Update an existing reminder
    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch {
            reminderDao.updateReminder(reminder.copy(lastModified = System.currentTimeMillis()))
            
            // Reschedule notification
            NotificationHelper.cancelReminder(context, reminder.reminderId)
            NotificationHelper.scheduleReminder(
                context,
                reminder.reminderId,
                reminder.title,
                reminder.description,
                reminder.priority.name,
                reminder.triggerTime,
                appPackageName = reminder.appPackageName,
                endTime = reminder.endTime
            )
        }
    }

    // Delete a reminder
    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            reminderDao.deleteReminder(reminder)
            
            // Cancel notification
            NotificationHelper.cancelReminder(context, reminder.reminderId)
        }
    }

    // Mark reminder as completed
    fun markAsCompleted(reminderId: String) {
        viewModelScope.launch {
            reminderDao.markAsCompleted(reminderId, System.currentTimeMillis())
            
            // Cancel notification
            NotificationHelper.cancelReminder(context, reminderId)
        }
    }
    
    // Unmark reminder as completed (restore to pending)
    fun unmarkCompleted(reminderId: String) {
        viewModelScope.launch {
            reminderDao.updateReminderStatus(reminderId, ReminderStatus.ACTIVE, System.currentTimeMillis())
            
            // Reschedule notification
            val reminder = reminderDao.getReminderById(reminderId)
            if (reminder != null && reminder.triggerTime > System.currentTimeMillis()) {
                NotificationHelper.scheduleReminder(
                    context,
                    reminder.reminderId,
                    reminder.title,
                    reminder.description,
                    reminder.priority.name,
                    reminder.triggerTime
                )
            }
        }
    }

    // Snooze reminder
    fun snoozeReminder(reminderId: String, snoozeUntil: Long) {
        viewModelScope.launch {
            reminderDao.snoozeReminder(reminderId, snoozeUntil)
            
            // Reschedule notification
            val reminder = reminderDao.getReminderById(reminderId)
            if (reminder != null) {
                NotificationHelper.cancelReminder(context, reminderId)
                NotificationHelper.scheduleReminder(
                    context,
                    reminderId,
                    reminder.title,
                    reminder.description,
                    reminder.priority.name,
                    snoozeUntil
                )
            }
        }
    }

    // Get reminder by ID
    suspend fun getReminderById(reminderId: String): Reminder? {
        return reminderDao.getReminderById(reminderId)
    }
}
