package com.groupflow.app.data.repository

import com.groupflow.app.data.local.AppDatabase
import com.groupflow.app.data.local.entity.Message
import kotlinx.coroutines.flow.Flow

class MessageRepository(private val database: AppDatabase) {

    fun getGroupMessages(groupId: String): Flow<List<Message>> = database.messageDao().getGroupMessages(groupId)

    suspend fun getLastMessage(groupId: String): Message? = database.messageDao().getLastMessage(groupId)

    suspend fun insertMessage(message: Message) = database.messageDao().insertMessage(message)

    suspend fun insertMessages(messages: List<Message>) = database.messageDao().insertMessages(messages)

    suspend fun updateMessage(message: Message) = database.messageDao().updateMessage(message)

    suspend fun deleteMessage(message: Message) = database.messageDao().deleteMessage(message)

    suspend fun markAsSynced(messageId: String) = database.messageDao().markAsSynced(messageId)

    suspend fun getUnsyncedMessages(): List<Message> = database.messageDao().getUnsyncedMessages()
}
