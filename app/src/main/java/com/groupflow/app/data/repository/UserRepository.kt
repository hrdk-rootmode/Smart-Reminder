package com.groupflow.app.data.repository

import com.groupflow.app.data.local.AppDatabase
import com.groupflow.app.data.local.entity.User
import kotlinx.coroutines.flow.Flow

class UserRepository(private val database: AppDatabase) {

    suspend fun getUserById(uid: String): User? = database.userDao().getUserById(uid)

    suspend fun insertUser(user: User) = database.userDao().insertUser(user)

    suspend fun updateUser(user: User) = database.userDao().updateUser(user)

    suspend fun updateGeminiUsage(uid: String, count: Int, date: String) = database.userDao().updateGeminiUsage(uid, count, date)

    suspend fun getGeminiRequestCount(uid: String): Int? = database.userDao().getGeminiRequestCount(uid)
}
