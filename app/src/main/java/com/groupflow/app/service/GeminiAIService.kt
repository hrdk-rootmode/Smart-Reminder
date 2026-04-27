package com.groupflow.app.service

import android.content.Context
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.groupflow.app.BuildConfig
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
     * Parse natural language reminder input
     * @param input Natural language input (e.g., "Remind me to call mom tomorrow at 6 PM")
     * @param userId User ID for tracking
     * @return ParsedReminder with extracted information
     */
    suspend fun parseReminder(input: String, userId: String): Result<ParsedReminder> {
        return try {
            val model = generativeModel ?: return Result.failure(
                IllegalStateException("Gemini AI not initialized. Add GEMINI_API_KEY to local.properties")
            )

            val currentTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
            val prompt = """
            Parse this reminder request in JSON format:
            
            Input: "$input"
            
            Extract:
            {
              "title": "Brief title",
              "description": "Optional details",
              "triggerTime": "ISO 8601 datetime (yyyy-MM-dd'T'HH:mm:ss)",
              "priority": "URGENT|HIGH|MEDIUM|LOW",
              "isRecurring": true/false,
              "recurrencePattern": {
                "frequency": "DAILY|WEEKLY|MONTHLY",
                "daysOfWeek": [1,2,3]
              },
              "detectedLanguage": "en|hi|es"
            }
            
            Current time: $currentTime
            User timezone: ${Calendar.getInstance().timeZone.displayName}
            
            Examples:
            - "कल सुबह 7 बजे व्यायाम करने की याद दिलाओ" → Hindi, tomorrow 7 AM, title: "व्यायाम करना"
            - "Remind me to call mom tomorrow at 6 PM" → English, tomorrow 6 PM, title: "Call mom"
            - "Urgente: pagar factura de electricidad" → Spanish, URGENT priority
            
            Return ONLY JSON, no markdown.
            """.trimIndent()

            val response = model.generateContent(prompt)
            val jsonText = response.text?.trim()
            
            if (jsonText != null) {
                // Parse JSON (simplified parsing for now)
                val title = extractField(jsonText, "title") ?: "Untitled Reminder"
                val description = extractField(jsonText, "description") ?: ""
                val priorityStr = extractField(jsonText, "priority") ?: "MEDIUM"
                val detectedLanguage = extractField(jsonText, "detectedLanguage") ?: "en"
                
                // Calculate trigger time based on input (simplified)
                val triggerTime = calculateTriggerTime(input, currentTime)
                
                Result.success(ParsedReminder(
                    title = title,
                    description = description,
                    triggerTime = triggerTime,
                    priority = priorityStr,
                    isRecurring = false,
                    detectedLanguage = detectedLanguage
                ))
            } else {
                Result.failure(Exception("No response from Gemini AI"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun extractField(json: String, fieldName: String): String? {
        val pattern = "\"$fieldName\"\\s*:\\s*\"([^\"]*)\"".toRegex()
        val match = pattern.find(json)
        return match?.groupValues?.get(1)
    }
    
    private fun calculateTriggerTime(input: String, currentTime: String): Long {
        // Simplified time calculation - in production, use proper NLP
        val calendar = Calendar.getInstance()
        
        when {
            input.contains("tomorrow", ignoreCase = true) -> {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 9)
                calendar.set(Calendar.MINUTE, 0)
            }
            input.contains("morning", ignoreCase = true) -> {
                calendar.set(Calendar.HOUR_OF_DAY, 8)
                calendar.set(Calendar.MINUTE, 0)
            }
            input.contains("evening", ignoreCase = true) -> {
                calendar.set(Calendar.HOUR_OF_DAY, 18)
                calendar.set(Calendar.MINUTE, 0)
            }
            input.contains("urgent", ignoreCase = true) -> {
                calendar.add(Calendar.MINUTE, 30)
            }
            else -> {
                calendar.add(Calendar.HOUR, 1)
            }
        }
        
        return calendar.timeInMillis
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
    
    /**
     * Parsed reminder from natural language input
     */
    data class ParsedReminder(
        val title: String,
        val description: String,
        val triggerTime: Long,
        val priority: String,
        val isRecurring: Boolean,
        val detectedLanguage: String
    )
}
