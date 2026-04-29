package com.groupflow.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.groupflow.app.data.local.entity.Announcement
import com.groupflow.app.data.local.entity.Checklist
import com.groupflow.app.data.local.entity.ChecklistItem
import com.groupflow.app.data.local.entity.Group
import com.groupflow.app.data.local.entity.GroupMember
import com.groupflow.app.data.local.entity.MemberRole
import com.groupflow.app.data.local.entity.Message
import com.groupflow.app.data.local.entity.Reminder
import com.groupflow.app.data.local.entity.ReminderPriority
import com.groupflow.app.data.local.entity.ReminderStatus
import com.groupflow.app.data.local.entity.Task
import com.groupflow.app.data.local.entity.TaskStatus
import com.groupflow.app.data.local.entity.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE uid = :uid")
    suspend fun getUserById(uid: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("UPDATE users SET dailyGeminiRequests = :count, lastGeminiResetDate = :date WHERE uid = :uid")
    suspend fun updateGeminiUsage(uid: String, count: Int, date: String)

    @Query("SELECT dailyGeminiRequests FROM users WHERE uid = :uid")
    suspend fun getGeminiRequestCount(uid: String): Int?
}

@Dao
interface GroupDao {
    @Query("SELECT * FROM groups WHERE isArchived = 0 ORDER BY lastActivity DESC")
    fun getAllActiveGroups(): Flow<List<Group>>

    @Query("SELECT * FROM groups WHERE groupId = :groupId")
    suspend fun getGroupById(groupId: String): Group?

    @Query("SELECT * FROM groups WHERE inviteCode = :code")
    suspend fun getGroupByInviteCode(code: String): Group?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: Group)

    @Update
    suspend fun updateGroup(group: Group)

    @Query("UPDATE groups SET lastActivity = :timestamp WHERE groupId = :groupId")
    suspend fun updateLastActivity(groupId: String, timestamp: Long)

    @Delete
    suspend fun deleteGroup(group: Group)
}

@Dao
interface GroupMemberDao {
    @Query("SELECT * FROM group_members WHERE groupId = :groupId AND isActive = 1")
    fun getGroupMembers(groupId: String): Flow<List<GroupMember>>

    @Query("SELECT * FROM group_members WHERE userId = :userId AND isActive = 1")
    fun getUserGroups(userId: String): Flow<List<GroupMember>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: GroupMember)

    @Query("DELETE FROM group_members WHERE groupId = :groupId AND userId = :userId")
    suspend fun removeMember(groupId: String, userId: String)

    @Query("UPDATE group_members SET role = :role WHERE groupId = :groupId AND userId = :userId")
    suspend fun updateMemberRole(groupId: String, userId: String, role: MemberRole)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE groupId = :groupId ORDER BY timestamp ASC")
    fun getGroupMessages(groupId: String): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE groupId = :groupId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastMessage(groupId: String): Message?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<Message>)

    @Update
    suspend fun updateMessage(message: Message)

    @Delete
    suspend fun deleteMessage(message: Message)

    @Query("UPDATE messages SET isSynced = 1 WHERE messageId = :messageId")
    suspend fun markAsSynced(messageId: String)

    @Query("SELECT * FROM messages WHERE isSynced = 0")
    suspend fun getUnsyncedMessages(): List<Message>
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE groupId = :groupId ORDER BY dueDate ASC")
    fun getGroupTasks(groupId: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE assignedTo LIKE '%' || :userId || '%' AND status != 'COMPLETED' ORDER BY dueDate ASC")
    fun getUserTasks(userId: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE dueDate BETWEEN :start AND :end")
    fun getTasksByDateRange(start: Long, end: Long): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("UPDATE tasks SET status = :status, completedAt = :completedAt, completedBy = :completedBy WHERE taskId = :taskId")
    suspend fun completeTask(taskId: String, status: TaskStatus, completedAt: Long, completedBy: String)
}

@Dao
interface ChecklistDao {
    @Query("SELECT * FROM checklists WHERE groupId = :groupId")
    fun getGroupChecklists(groupId: String): Flow<List<Checklist>>

    @Query("SELECT * FROM checklist_items WHERE checklistId = :checklistId ORDER BY `order` ASC")
    fun getChecklistItems(checklistId: String): Flow<List<ChecklistItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklist(checklist: Checklist): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklistItem(item: ChecklistItem)

    @Update
    suspend fun updateChecklistItem(item: ChecklistItem)

    @Delete
    suspend fun deleteChecklist(checklist: Checklist)

    @Delete
    suspend fun deleteChecklistItem(item: ChecklistItem)
}

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements WHERE groupId = :groupId ORDER BY isPinned DESC, createdAt DESC")
    fun getGroupAnnouncements(groupId: String): Flow<List<Announcement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: Announcement)

    @Update
    suspend fun updateAnnouncement(announcement: Announcement)

    @Delete
    suspend fun deleteAnnouncement(announcement: Announcement)

    @Query("UPDATE announcements SET readBy = :readBy WHERE announcementId = :announcementId")
    suspend fun markAsRead(announcementId: String, readBy: String)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE userId = :userId AND status != 'CANCELLED' AND status != 'EXPIRED' ORDER BY triggerTime ASC")
    fun getUserReminders(userId: String): Flow<List<Reminder>>

    @Query(
        """
        SELECT * FROM reminders
        WHERE userId = :userId
        AND status = 'ACTIVE'
        AND triggerTime BETWEEN :startTime AND :endTime
        ORDER BY triggerTime ASC
        """
    )
    fun getUpcomingReminders(userId: String, startTime: Long, endTime: Long): Flow<List<Reminder>>

    @Query(
        """
        SELECT * FROM reminders
        WHERE userId = :userId
        AND status = 'ACTIVE'
        AND triggerTime < :currentTime
        ORDER BY triggerTime DESC
        """
    )
    fun getOverdueReminders(userId: String, currentTime: Long): Flow<List<Reminder>>

    @Query(
        """
        SELECT * FROM reminders
        WHERE userId = :userId
        AND status = 'ACTIVE'
        AND priority = :priority
        ORDER BY triggerTime ASC
        """
    )
    fun getRemindersByPriority(userId: String, priority: ReminderPriority): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE groupId = :groupId AND status = 'ACTIVE' ORDER BY triggerTime ASC")
    fun getGroupReminders(groupId: String): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE reminderId = :reminderId")
    suspend fun getReminderById(reminderId: String): Reminder?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    @Query(
        """
        UPDATE reminders
        SET status = 'COMPLETED', completedAt = :completedAt
        WHERE reminderId = :reminderId
        """
    )
    suspend fun markAsCompleted(reminderId: String, completedAt: Long)

    @Query(
        """
        UPDATE reminders
        SET status = :status, lastModified = :lastModified
        WHERE reminderId = :reminderId
        """
    )
    suspend fun updateReminderStatus(reminderId: String, status: ReminderStatus, lastModified: Long)

    @Query(
        """
        UPDATE reminders
        SET status = 'SNOOZED', snoozeUntil = :snoozeUntil, snoozeCount = snoozeCount + 1
        WHERE reminderId = :reminderId
        """
    )
    suspend fun snoozeReminder(reminderId: String, snoozeUntil: Long)

    @Query("UPDATE reminders SET status = 'CANCELLED' WHERE reminderId = :reminderId")
    suspend fun cancelReminder(reminderId: String)

    @Query("SELECT * FROM reminders WHERE userId = :userId AND tags LIKE '%' || :tag || '%' ORDER BY triggerTime ASC")
    fun getRemindersByTag(userId: String, tag: String): Flow<List<Reminder>>

    @Query(
        """
        SELECT * FROM reminders
        WHERE userId = :userId
        AND (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
        ORDER BY triggerTime DESC
        """
    )
    fun searchReminders(userId: String, query: String): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE userId = :userId AND reminderType = 'LOCATION_BASED' AND status = 'ACTIVE'")
    suspend fun getLocationReminders(userId: String): List<Reminder>

    @Query("SELECT * FROM reminders WHERE isSynced = 0")
    suspend fun getUnsyncedReminders(): List<Reminder>

    @Query("UPDATE reminders SET isSynced = 1 WHERE reminderId = :reminderId")
    suspend fun markAsSynced(reminderId: String)
}
