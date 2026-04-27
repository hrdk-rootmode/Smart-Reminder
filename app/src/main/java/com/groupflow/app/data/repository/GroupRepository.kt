package com.groupflow.app.data.repository

import com.groupflow.app.data.local.AppDatabase
import com.groupflow.app.data.local.entity.Group
import kotlinx.coroutines.flow.Flow

class GroupRepository(private val database: AppDatabase) {

    fun getAllActiveGroups(): Flow<List<Group>> = database.groupDao().getAllActiveGroups()

    suspend fun getGroupById(groupId: String): Group? = database.groupDao().getGroupById(groupId)

    suspend fun getGroupByInviteCode(code: String): Group? = database.groupDao().getGroupByInviteCode(code)

    suspend fun insertGroup(group: Group) = database.groupDao().insertGroup(group)

    suspend fun updateGroup(group: Group) = database.groupDao().updateGroup(group)

    suspend fun deleteGroup(group: Group) = database.groupDao().deleteGroup(group)

    suspend fun updateLastActivity(groupId: String, timestamp: Long) = database.groupDao().updateLastActivity(groupId, timestamp)
}
