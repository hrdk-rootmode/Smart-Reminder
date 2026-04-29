package com.groupflow.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.groupflow.app.data.local.dao.AnnouncementDao
import com.groupflow.app.data.local.dao.ChecklistDao
import com.groupflow.app.data.local.dao.GroupDao
import com.groupflow.app.data.local.dao.GroupMemberDao
import com.groupflow.app.data.local.dao.MessageDao
import com.groupflow.app.data.local.dao.ReminderDao
import com.groupflow.app.data.local.dao.TaskDao
import com.groupflow.app.data.local.dao.UserDao
import com.groupflow.app.data.local.entity.Announcement
import com.groupflow.app.data.local.entity.Checklist
import com.groupflow.app.data.local.entity.ChecklistItem
import com.groupflow.app.data.local.entity.Group
import com.groupflow.app.data.local.entity.GroupMember
import com.groupflow.app.data.local.entity.Message
import com.groupflow.app.data.local.entity.Reminder
import com.groupflow.app.data.local.entity.Task
import com.groupflow.app.data.local.entity.User

@Database(
    entities = [
        User::class,
        Group::class,
        GroupMember::class,
        Message::class,
        Task::class,
        Checklist::class,
        ChecklistItem::class,
        Announcement::class,
        Reminder::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun groupDao(): GroupDao
    abstract fun groupMemberDao(): GroupMemberDao
    abstract fun messageDao(): MessageDao
    abstract fun taskDao(): TaskDao
    abstract fun checklistDao(): ChecklistDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "groupflow_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
