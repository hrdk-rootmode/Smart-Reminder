package com.groupflow.app.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.groupflow.app.data.local.entity.GroupType
import com.groupflow.app.data.local.entity.MemberRole
import com.groupflow.app.data.local.entity.MessageType
import com.groupflow.app.data.local.entity.RecurrenceFrequency
import com.groupflow.app.data.local.entity.RecurrencePattern
import com.groupflow.app.data.local.entity.ReminderPriority
import com.groupflow.app.data.local.entity.ReminderStatus
import com.groupflow.app.data.local.entity.ReminderType
import com.groupflow.app.data.local.entity.TaskPriority
import com.groupflow.app.data.local.entity.TaskStatus

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromGroupType(value: GroupType): String = value.name

    @TypeConverter
    fun toGroupType(value: String): GroupType = GroupType.valueOf(value)

    @TypeConverter
    fun fromMemberRole(value: MemberRole): String = value.name

    @TypeConverter
    fun toMemberRole(value: String): MemberRole = MemberRole.valueOf(value)

    @TypeConverter
    fun fromMessageType(value: MessageType): String = value.name

    @TypeConverter
    fun toMessageType(value: String): MessageType = MessageType.valueOf(value)

    @TypeConverter
    fun fromTaskPriority(value: TaskPriority): String = value.name

    @TypeConverter
    fun toTaskPriority(value: String): TaskPriority = TaskPriority.valueOf(value)

    @TypeConverter
    fun fromTaskStatus(value: TaskStatus): String = value.name

    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus = TaskStatus.valueOf(value)

    @TypeConverter
    fun fromRecurrencePattern(value: RecurrencePattern?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun toRecurrencePattern(value: String?): RecurrencePattern? =
        value?.let { gson.fromJson(it, RecurrencePattern::class.java) }

    @TypeConverter
    fun fromReminderPriority(value: ReminderPriority): String = value.name

    @TypeConverter
    fun toReminderPriority(value: String): ReminderPriority = ReminderPriority.valueOf(value)

    @TypeConverter
    fun fromReminderStatus(value: ReminderStatus): String = value.name

    @TypeConverter
    fun toReminderStatus(value: String): ReminderStatus = ReminderStatus.valueOf(value)

    @TypeConverter
    fun fromReminderType(value: ReminderType): String = value.name

    @TypeConverter
    fun toReminderType(value: String): ReminderType = ReminderType.valueOf(value)

    @TypeConverter
    fun fromRecurrenceFrequency(value: RecurrenceFrequency): String = value.name

    @TypeConverter
    fun toRecurrenceFrequency(value: String): RecurrenceFrequency = RecurrenceFrequency.valueOf(value)
}
