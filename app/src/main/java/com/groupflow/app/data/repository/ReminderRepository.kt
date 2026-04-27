package com.groupflow.app.data.repository

import com.groupflow.app.data.local.AppDatabase
import com.groupflow.app.data.local.entity.Reminder
import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val database: AppDatabase) {

    fun getUserReminders(userId: String): Flow<List<Reminder>> = database.reminderDao().getUserReminders(userId)

    fun getUpcomingReminders(userId: String, startTime: Long, endTime: Long): Flow<List<Reminder>> = database.reminderDao().getUpcomingReminders(userId, startTime, endTime)

    fun getOverdueReminders(userId: String, currentTime: Long): Flow<List<Reminder>> = database.reminderDao().getOverdueReminders(userId, currentTime)

    fun getRemindersByPriority(userId: String, priority: com.groupflow.app.data.local.entity.ReminderPriority): Flow<List<Reminder>> = database.reminderDao().getRemindersByPriority(userId, priority)

    fun getGroupReminders(groupId: String): Flow<List<Reminder>> = database.reminderDao().getGroupReminders(groupId)

    suspend fun getReminderById(reminderId: String): Reminder? = database.reminderDao().getReminderById(reminderId)

    suspend fun insertReminder(reminder: Reminder) = database.reminderDao().insertReminder(reminder)

    suspend fun updateReminder(reminder: Reminder) = database.reminderDao().updateReminder(reminder)

    suspend fun deleteReminder(reminder: Reminder) = database.reminderDao().deleteReminder(reminder)

    suspend fun markAsCompleted(reminderId: String, completedAt: Long) = database.reminderDao().markAsCompleted(reminderId, completedAt)

    suspend fun snoozeReminder(reminderId: String, snoozeUntil: Long) = database.reminderDao().snoozeReminder(reminderId, snoozeUntil)

    suspend fun cancelReminder(reminderId: String) = database.reminderDao().cancelReminder(reminderId)

    suspend fun markAsSynced(reminderId: String) = database.reminderDao().markAsSynced(reminderId)
}
