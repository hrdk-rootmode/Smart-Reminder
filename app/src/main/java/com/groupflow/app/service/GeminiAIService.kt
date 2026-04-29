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
            val model = generativeModel
            if (model == null) {
                return Result.failure(
                    IllegalStateException("Gemini AI not initialized. Add GEMINI_API_KEY to local.properties")
                )
            }

            val currentTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
            val prompt = """
            Parse this reminder request in JSON format:
            
            Input: "$input"
            
            Extract the following information and return ONLY valid JSON:
            {
                "title": "The main task or reminder title",
                "description": "Additional details or empty string if none",
                "priority": "URGENT, HIGH, MEDIUM, or LOW based on urgency",
                "triggerTime": "The time in 'HH:mm' 24-hour format, or empty if not specified",
                "isRecurring": "true if this is a recurring reminder (daily, weekly, monthly, quarterly, yearly), otherwise false",
                "recurrencePattern": "DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY, or empty if not recurring"
            }
            
            Rules:
            - If no time is mentioned, set triggerTime to empty
            - If priority is not specified, use MEDIUM
            - If no description, use empty string
            - For recurring reminders, detect patterns like "every day", "daily", "every week", "weekly", "every month", "monthly", "every quarter", "quarterly", "every year", "yearly"
            - Return ONLY the JSON, no other text
            """.trimIndent()
            
            val response = model.generateContent(prompt)
            val text = response.text ?: ""
            
            android.util.Log.d("GeminiAIService", "Response: $text")
            
            // Extract JSON from response
            val jsonText = extractJsonFromResponse(text)
            
            val title = extractField(jsonText, "title") ?: input
            val description = extractField(jsonText, "description") ?: ""
            val priority = extractField(jsonText, "priority") ?: "MEDIUM"
            val timeStr = extractField(jsonText, "triggerTime") ?: ""
            val isRecurring = extractField(jsonText, "isRecurring")?.toBoolean() ?: false
            val recurrencePattern = extractField(jsonText, "recurrencePattern") ?: ""
            
            // Calculate trigger time
            val triggerTime = if (timeStr.isNotEmpty()) {
                calculateTriggerTimeFromTimeStr(timeStr, input)
            } else {
                calculateTriggerTime(input, "")
            }
            
            // Check for DND
            val enableDND = input.lowercase().contains("do not disturb") || 
                           input.lowercase().contains("dnd") || 
                           input.lowercase().contains("silent")
            
            Result.success(
                ParsedReminder(
                    title = title,
                    description = description,
                    priority = priority,
                    triggerTime = triggerTime,
                    isRecurring = isRecurring,
                    recurrencePattern = recurrencePattern,
                    enableDND = enableDND
                )
            )
        } catch (e: Exception) {
            android.util.Log.e("GeminiAIService", "Parse error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    private fun extractJsonFromResponse(response: String): String {
        // Try to extract JSON from the response
        val jsonPattern = "\\{.*\\}".toRegex(RegexOption.DOT_MATCHES_ALL)
        val match = jsonPattern.find(response)
        return match?.value ?: "{}"
    }
    
    private fun extractField(json: String, fieldName: String): String? {
        val pattern = "\"$fieldName\"\\s*:\\s*\"([^\"]*)\"".toRegex()
        val match = pattern.find(json)
        return match?.groupValues?.get(1)
    }
    
    private fun calculateTriggerTimeFromTimeStr(timeStr: String, input: String): Long {
        val calendar = Calendar.getInstance()
        try {
            val parts = timeStr.split(":")
            if (parts.size == 2) {
                val hour = parts[0].toIntOrNull() ?: 0
                val minute = parts[1].toIntOrNull() ?: 0
                
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                
                // If the time has already passed today, set it for tomorrow
                if (calendar.timeInMillis < System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("GeminiAIService", "Error parsing time string: $timeStr", e)
            // Fallback to calculateTriggerTime
            return calculateTriggerTime(input, "")
        }
        return calendar.timeInMillis
    }
    
    private fun calculateTriggerTime(input: String, currentTime: String): Long {
        val calendar = Calendar.getInstance()
        val lowerInput = input.lowercase()
        
        android.util.Log.d("GeminiAIService", "Calculating trigger time for input: $input")
        
        // Handle relative time expressions
        when {
            // Minutes - more flexible pattern matching
            lowerInput.matches(Regex("(after|in)\\s+(\\d+)\\s*minutes?")) -> {
                val match = Regex("(after|in)\\s+(\\d+)\\s*minutes?").find(lowerInput)
                val minutes = match?.groupValues?.get(2)?.toIntOrNull() ?: 2
                calendar.add(Calendar.MINUTE, minutes)
                android.util.Log.d("GeminiAIService", "Matched $minutes minutes")
            }
            lowerInput.matches(Regex("(after|in)\\s+(\\d+)\\s*min")) -> {
                val match = Regex("(after|in)\\s+(\\d+)\\s*min").find(lowerInput)
                val minutes = match?.groupValues?.get(2)?.toIntOrNull() ?: 2
                calendar.add(Calendar.MINUTE, minutes)
                android.util.Log.d("GeminiAIService", "Matched $minutes min")
            }
            // Specific minute patterns
            lowerInput.contains("after 5 minute") || lowerInput.contains("in 5 minute") -> {
                calendar.add(Calendar.MINUTE, 5)
            }
            lowerInput.contains("after 10 minute") || lowerInput.contains("in 10 minute") -> {
                calendar.add(Calendar.MINUTE, 10)
            }
            lowerInput.contains("after 15 minute") || lowerInput.contains("in 15 minute") -> {
                calendar.add(Calendar.MINUTE, 15)
            }
            lowerInput.contains("after 30 minute") || lowerInput.contains("in 30 minute") -> {
                calendar.add(Calendar.MINUTE, 30)
            }
            // Hours
            lowerInput.matches(Regex("(after|in)\\s+(\\d+)\\s*hours?")) -> {
                val match = Regex("(after|in)\\s+(\\d+)\\s*hours?").find(lowerInput)
                val hours = match?.groupValues?.get(2)?.toIntOrNull() ?: 1
                calendar.add(Calendar.HOUR_OF_DAY, hours)
                android.util.Log.d("GeminiAIService", "Matched $hours hours")
            }
            lowerInput.contains("after 1 hour") || lowerInput.contains("in 1 hour") -> {
                calendar.add(Calendar.HOUR_OF_DAY, 1)
            }
            lowerInput.contains("after 2 hour") || lowerInput.contains("in 2 hour") -> {
                calendar.add(Calendar.HOUR_OF_DAY, 2)
            }
            lowerInput.contains("after 3 hour") || lowerInput.contains("in 3 hour") -> {
                calendar.add(Calendar.HOUR_OF_DAY, 3)
            }
            // Days
            lowerInput.matches(Regex("(after|in)\\s+(\\d+)\\s*days?")) -> {
                val match = Regex("(after|in)\\s+(\\d+)\\s*days?").find(lowerInput)
                val days = match?.groupValues?.get(2)?.toIntOrNull() ?: 1
                calendar.add(Calendar.DAY_OF_YEAR, days)
                calendar.set(Calendar.HOUR_OF_DAY, 9)
                calendar.set(Calendar.MINUTE, 0)
                android.util.Log.d("GeminiAIService", "Matched $days days")
            }
            lowerInput.contains("after 1 day") || lowerInput.contains("in 1 day") || lowerInput.contains("tomorrow") -> {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 9)
                calendar.set(Calendar.MINUTE, 0)
            }
            lowerInput.contains("after 2 day") || lowerInput.contains("in 2 day") -> {
                calendar.add(Calendar.DAY_OF_YEAR, 2)
                calendar.set(Calendar.HOUR_OF_DAY, 9)
                calendar.set(Calendar.MINUTE, 0)
            }
            lowerInput.contains("after 3 day") || lowerInput.contains("in 3 day") -> {
                calendar.add(Calendar.DAY_OF_YEAR, 3)
                calendar.set(Calendar.HOUR_OF_DAY, 9)
                calendar.set(Calendar.MINUTE, 0)
            }
            lowerInput.contains("after 7 day") || lowerInput.contains("in 7 day") || lowerInput.contains("next week") -> {
                calendar.add(Calendar.DAY_OF_YEAR, 7)
                calendar.set(Calendar.HOUR_OF_DAY, 9)
                calendar.set(Calendar.MINUTE, 0)
            }
            // Days of week
            lowerInput.contains("next monday") -> {
                calendar.add(Calendar.DAY_OF_YEAR, getDaysUntil(Calendar.MONDAY))
                calendar.set(Calendar.HOUR_OF_DAY, 9)
                calendar.set(Calendar.MINUTE, 0)
            }
            lowerInput.contains("next tuesday") -> {
                calendar.add(Calendar.DAY_OF_YEAR, getDaysUntil(Calendar.TUESDAY))
                calendar.set(Calendar.HOUR_OF_DAY, 9)
                calendar.set(Calendar.MINUTE, 0)
            }
            lowerInput.contains("next wednesday") -> {
                calendar.add(Calendar.DAY_OF_YEAR, getDaysUntil(Calendar.WEDNESDAY))
                calendar.set(Calendar.HOUR_OF_DAY, 9)
                calendar.set(Calendar.MINUTE, 0)
            }
            lowerInput.contains("next thursday") -> {
                calendar.add(Calendar.DAY_OF_YEAR, getDaysUntil(Calendar.THURSDAY))
                calendar.set(Calendar.HOUR_OF_DAY, 9)
                calendar.set(Calendar.MINUTE, 0)
            }
            lowerInput.contains("next friday") -> {
                calendar.add(Calendar.DAY_OF_YEAR, getDaysUntil(Calendar.FRIDAY))
                calendar.set(Calendar.HOUR_OF_DAY, 9)
                calendar.set(Calendar.MINUTE, 0)
            }
            lowerInput.contains("next saturday") -> {
                calendar.add(Calendar.DAY_OF_YEAR, getDaysUntil(Calendar.SATURDAY))
                calendar.set(Calendar.HOUR_OF_DAY, 10)
                calendar.set(Calendar.MINUTE, 0)
            }
            lowerInput.contains("next sunday") -> {
                calendar.add(Calendar.DAY_OF_YEAR, getDaysUntil(Calendar.SUNDAY))
                calendar.set(Calendar.HOUR_OF_DAY, 10)
                calendar.set(Calendar.MINUTE, 0)
            }
            // Time of day
            lowerInput.contains("morning") -> {
                calendar.set(Calendar.HOUR_OF_DAY, 8)
                calendar.set(Calendar.MINUTE, 0)
            }
            lowerInput.contains("afternoon") -> {
                calendar.set(Calendar.HOUR_OF_DAY, 14)
                calendar.set(Calendar.MINUTE, 0)
            }
            lowerInput.contains("evening") -> {
                calendar.set(Calendar.HOUR_OF_DAY, 18)
                calendar.set(Calendar.MINUTE, 0)
            }
            lowerInput.contains("night") -> {
                calendar.set(Calendar.HOUR_OF_DAY, 20)
                calendar.set(Calendar.MINUTE, 0)
            }
            lowerInput.contains("wakeup") || lowerInput.contains("wake up") -> {
                calendar.set(Calendar.HOUR_OF_DAY, 7)
                calendar.set(Calendar.MINUTE, 0)
            }
            // Urgent
            lowerInput.contains("urgent") -> {
                calendar.add(Calendar.MINUTE, 30)
            }
            // Default - 2 minutes instead of 1 hour
            else -> {
                calendar.add(Calendar.MINUTE, 2)
                android.util.Log.d("GeminiAIService", "Using default: 2 minutes")
            }
        }
        
        return calendar.timeInMillis
    }
    
    private fun getDaysUntil(targetDay: Int): Int {
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val daysUntil = targetDay - currentDay
        return if (daysUntil <= 0) daysUntil + 7 else daysUntil
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
        val recurrencePattern: String = "",
        val detectedLanguage: String = "en-US",
        val enableDND: Boolean = false
    )
}
