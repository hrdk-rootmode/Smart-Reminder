package com.groupflow.app.data.repository

import com.groupflow.app.data.local.AppDatabase
import com.groupflow.app.data.local.entity.Task
import com.groupflow.app.data.local.entity.TaskStatus
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val database: AppDatabase) {

    fun getGroupTasks(groupId: String): Flow<List<Task>> = database.taskDao().getGroupTasks(groupId)

    fun getUserTasks(userId: String): Flow<List<Task>> = database.taskDao().getUserTasks(userId)

    fun getTasksByDateRange(start: Long, end: Long): Flow<List<Task>> = database.taskDao().getTasksByDateRange(start, end)

    suspend fun insertTask(task: Task) = database.taskDao().insertTask(task)

    suspend fun updateTask(task: Task) = database.taskDao().updateTask(task)

    suspend fun deleteTask(task: Task) = database.taskDao().deleteTask(task)

    suspend fun completeTask(taskId: String, status: TaskStatus, completedAt: Long, completedBy: String) = database.taskDao().completeTask(taskId, status, completedAt, completedBy)
}
