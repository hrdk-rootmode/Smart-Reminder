package com.groupflow.app.service

import android.content.Context
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.groupflow.app.BuildConfig

/**
 * Gemini AI Service
 *
 * NOTE: Gemini AI integration requires API key from Google AI Studio.
 * To enable Gemini AI:
 * 1. Get API key from https://makersuite.google.com/app/apikey
 * 2. Add API key to local.properties: GEMINI_API_KEY=your-api-key-here
 * 3. Sync Gradle to rebuild project
 */
class GeminiAIService(private val context: Context) {

    private var generativeModel: GenerativeModel? = null

    /**
     * Initialize Gemini AI with API key from BuildConfig
     * Call this in Application.onCreate()
     */
    fun initialize() {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotEmpty()) {
            generativeModel = GenerativeModel(
                modelName = "gemini-pro",
                apiKey = apiKey,
                generationConfig = generationConfig {
                    temperature = 0.7f
                    topK = 40
                    topP = 0.95f
                    maxOutputTokens = 8192
                }
            )
        }
    }

    /**
     * Generate a summary of tasks and reminders
     * @param tasks List of task descriptions
     * @param reminders List of reminder descriptions
     * @return AI-generated summary
     */
    suspend fun generateDailySummary(tasks: List<String>, reminders: List<String>): Result<String> {
        return try {
            val model = generativeModel ?: return Result.failure(
                IllegalStateException("Gemini AI not initialized. Add GEMINI_API_KEY to local.properties")
            )

            val prompt = buildString {
                appendLine("Generate a concise daily summary for the following tasks and reminders:")
                appendLine("Tasks:")
                tasks.forEach { appendLine("- $it") }
                appendLine("Reminders:")
                reminders.forEach { appendLine("- $it") }
                appendLine("Provide a brief summary focusing on priorities and deadlines.")
            }

            val response = model.generateContent(prompt)
            Result.success(response.text ?: "No response generated")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Suggest task priorities based on deadlines and descriptions
     * @param tasks List of tasks with descriptions and due dates
     * @return List of suggested priorities
     */
    suspend fun suggestTaskPriorities(tasks: List<TaskInfo>): Result<List<String>> {
        return try {
            val model = generativeModel ?: return Result.failure(
                IllegalStateException("Gemini AI not initialized. Add GEMINI_API_KEY to local.properties")
            )

            val prompt = buildString {
                appendLine("Suggest priorities (High/Medium/Low) for these tasks:")
                tasks.forEach { appendLine("- ${it.title} (Due: ${it.dueDate}): ${it.description}") }
                appendLine("Return a JSON array with task titles and their suggested priorities.")
                appendLine("Format: [{\"title\": \"Task Name\", \"priority\": \"High\"}]")
            }

            val response = model.generateContent(prompt)
            Result.success(listOf(response.text ?: "No response generated"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Smart reminder suggestions based on user patterns
     * @param userActivity User's recent activity data
     * @return Suggested reminders
     */
    suspend fun suggestReminders(userActivity: UserActivity): Result<List<String>> {
        return try {
            val model = generativeModel ?: return Result.failure(
                IllegalStateException("Gemini AI not initialized. Add GEMINI_API_KEY to local.properties")
            )

            val prompt = buildString {
                appendLine("Based on the user's activity patterns, suggest helpful reminders:")
                appendLine("Recent tasks: ${userActivity.recentTasks.joinToString(", ")}")
                appendLine("Recent reminders: ${userActivity.recentReminders.joinToString(", ")}")
                appendLine("Completion patterns: ${userActivity.completionPatterns}")
                appendLine("Suggest 3-5 reminders that would be helpful based on these patterns.")
            }

            val response = model.generateContent(prompt)
            val suggestions = response.text?.lines()?.filter { it.isNotBlank() } ?: emptyList()
            Result.success(suggestions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generate a checklist from a task description
     * @param taskDescription Task description
     * @return Generated checklist items
     */
    suspend fun generateChecklist(taskDescription: String): Result<List<String>> {
        return try {
            val model = generativeModel ?: return Result.failure(
                IllegalStateException("Gemini AI not initialized. Add GEMINI_API_KEY to local.properties")
            )

            val prompt = buildString {
                appendLine("Generate a checklist of 3-7 subtasks for: $taskDescription")
                appendLine("Return each subtask on a new line.")
                appendLine("Make subtasks actionable and specific.")
            }

            val response = model.generateContent(prompt)
            val checklist = response.text?.lines()?.filter { it.isNotBlank() } ?: emptyList()
            Result.success(checklist)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Task information for AI processing
     */
    data class TaskInfo(
        val title: String,
        val description: String,
        val dueDate: String
    )

    /**
     * User activity data for smart suggestions
     */
    data class UserActivity(
        val recentTasks: List<String>,
        val recentReminders: List<String>,
        val completionPatterns: Map<String, Int>
    )
}
