package com.groupflow.app.data.local.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
@Entity(tableName = "users")
data class User(
    @PrimaryKey val uid: String,
    val email: String,
    val displayName: String,
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastSyncTime: Long = 0,
    val dailyGeminiRequests: Int = 0,
    val lastGeminiResetDate: String = "",
    val isPremium: Boolean = false,
    val premiumExpiryDate: Long? = null,
) : Parcelable

@Parcelize
@Entity(tableName = "groups")
data class Group(
    @PrimaryKey val groupId: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val iconUrl: String? = null,
    val type: GroupType = GroupType.PROJECT,
    val createdBy: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastActivity: Long = System.currentTimeMillis(),
    val inviteCode: String = generateInviteCode(),
    val isArchived: Boolean = false,
) : Parcelable

enum class GroupType {
    CLASSROOM,
    PROJECT,
    TRIP,
    BUSINESS,
    EVENT,
    OTHER,
}

fun generateInviteCode(): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return (1..6).map { chars.random() }.joinToString("")
}

@Entity(
    tableName = "group_members",
    primaryKeys = ["groupId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = Group::class,
            parentColumns = ["groupId"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["uid"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class GroupMember(
    val groupId: String,
    val userId: String,
    val role: MemberRole = MemberRole.MEMBER,
    val joinedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
)

enum class MemberRole {
    ADMIN,
    MODERATOR,
    MEMBER,
    VIEWER,
}

@Parcelize
@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = Group::class,
            parentColumns = ["groupId"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("groupId"), Index("timestamp")],
)
data class Message(
    @PrimaryKey val messageId: String = UUID.randomUUID().toString(),
    val groupId: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val type: MessageType = MessageType.TEXT,
    val timestamp: Long = System.currentTimeMillis(),
    val fileUrl: String? = null,
    val fileName: String? = null,
    val fileSize: Long? = null,
    val isEdited: Boolean = false,
    val editedAt: Long? = null,
    val replyToMessageId: String? = null,
    val reactions: String = "",
    val isSynced: Boolean = false,
    val isDeletedFromFirebase: Boolean = false,
) : Parcelable

enum class MessageType {
    TEXT,
    IMAGE,
    FILE,
    AUDIO,
    VIDEO,
    SYSTEM,
}

@Parcelize
@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Group::class,
            parentColumns = ["groupId"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("groupId"), Index("dueDate")],
)
data class Task(
    @PrimaryKey val taskId: String = UUID.randomUUID().toString(),
    val groupId: String,
    val title: String,
    val description: String = "",
    val createdBy: String,
    val assignedTo: String = "",
    val dueDate: Long? = null,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val status: TaskStatus = TaskStatus.TODO,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val completedBy: String? = null,
    val tags: String = "",
    val attachments: String = "",
    val reminderTime: Long? = null,
    val isSynced: Boolean = false,
) : Parcelable

enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL,
}

enum class TaskStatus {
    TODO,
    IN_PROGRESS,
    REVIEW,
    COMPLETED,
    CANCELLED,
}

@Parcelize
@Entity(
    tableName = "checklists",
    foreignKeys = [
        ForeignKey(
            entity = Group::class,
            parentColumns = ["groupId"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class Checklist(
    @PrimaryKey val checklistId: String = UUID.randomUUID().toString(),
    val groupId: String,
    val title: String,
    val createdBy: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
) : Parcelable

@Parcelize
@Entity(
    tableName = "checklist_items",
    foreignKeys = [
        ForeignKey(
            entity = Checklist::class,
            parentColumns = ["checklistId"],
            childColumns = ["checklistId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class ChecklistItem(
    @PrimaryKey val itemId: String = UUID.randomUUID().toString(),
    val checklistId: String,
    val text: String,
    val assignedTo: String? = null,
    val isCompleted: Boolean = false,
    val completedBy: String? = null,
    val completedAt: Long? = null,
    val order: Int = 0,
) : Parcelable

@Parcelize
@Entity(
    tableName = "announcements",
    foreignKeys = [
        ForeignKey(
            entity = Group::class,
            parentColumns = ["groupId"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class Announcement(
    @PrimaryKey val announcementId: String = UUID.randomUUID().toString(),
    val groupId: String,
    val title: String,
    val content: String,
    val createdBy: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val readBy: String = "",
    val isSynced: Boolean = false,
) : Parcelable

@Parcelize
@Entity(
    tableName = "reminders",
    indices = [Index("userId"), Index("groupId"), Index("triggerTime"), Index("status")],
)
data class Reminder(
    @PrimaryKey val reminderId: String = UUID.randomUUID().toString(),
    val userId: String,
    val groupId: String? = null,
    val title: String,
    val description: String = "",
    val triggerTime: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val isRecurring: Boolean = false,
    val recurrencePattern: RecurrencePattern? = null,
    val recurrenceEndDate: Long? = null,
    val priority: ReminderPriority = ReminderPriority.MEDIUM,
    val status: ReminderStatus = ReminderStatus.ACTIVE,
    val completedAt: Long? = null,
    val snoozeUntil: Long? = null,
    val snoozeCount: Int = 0,
    val tags: String = "",
    val location: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val radius: Int? = null,
    val attachments: String = "",
    val notificationSoundUri: String? = null,
    val isVibrate: Boolean = true,
    val isSilent: Boolean = false,
    val customRepeatInterval: Long? = null,
    val reminderType: ReminderType = ReminderType.TIME_BASED,
    val linkedTaskId: String? = null,
    val linkedMessageId: String? = null,
    val isSynced: Boolean = false,
    val lastModified: Long = System.currentTimeMillis(),
    // App automation fields
    val appPackageName: String? = null,  // Package name of app to open (e.g., "com.spotify.music")
    val endTime: Long? = null,  // Time to remind user to close the app
) : Parcelable

enum class ReminderPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT,
}

enum class ReminderStatus {
    ACTIVE,
    COMPLETED,
    SNOOZED,
    CANCELLED,
    EXPIRED,
}

enum class ReminderType {
    TIME_BASED,
    LOCATION_BASED,
    EVENT_BASED,
    CONTACT_BASED,
}

@Parcelize
data class RecurrencePattern(
    val frequency: RecurrenceFrequency,
    val interval: Int = 1,
    val daysOfWeek: List<Int> = emptyList(),
    val dayOfMonth: Int? = null,
    val monthOfYear: Int? = null,
    val customPattern: String? = null,
) : Parcelable

enum class RecurrenceFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY,
    CUSTOM,
}
