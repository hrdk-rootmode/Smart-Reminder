package com.groupflow.app.service

import android.content.Context
import com.google.ai.client.generativeai.GenerativeModel
import com.groupflow.app.BuildConfig
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Smart Scheduling Service
 * Uses Gemini AI to determine optimal reminder times
 * Considers user patterns, time zones, and calendar context
 */
class SmartSchedulingService(private val context: Context) {
    
    private var generativeModel: GenerativeModel? = null
    
    fun initialize() {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotEmpty()) {
            generativeModel = GenerativeModel(
                modelName = "gemini-pro",
                apiKey = apiKey
            )
        }
    }
    
    /**
     * Suggest optimal time for a reminder
     * @param title Reminder title
     * @param description Reminder description
     * @param userTimeZone User's time zone
     * @param userPreferences User scheduling preferences
     * @return Suggested timestamp in milliseconds
     */
    suspend fun suggestOptimalTime(
        title: String,
        description: String,
        userTimeZone: String = Calendar.getInstance().timeZone.id,
        userPreferences: UserPreferences = UserPreferences()
    ): Result<Long> {
        return try {
            val model = generativeModel ?: return Result.failure(
                IllegalStateException("Gemini AI not initialized")
            )
            
            val currentTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(java.util.Date())
            val prompt = """
            Suggest the optimal time for this reminder:
            
            Title: $title
            Description: $description
            Current time: $currentTime
            User timezone: $userTimeZone
            User preferences:
            - Preferred start hour: ${userPreferences.preferredStartHour}
            - Preferred end hour: ${userPreferences.preferredEndHour}
            - Avoid weekends: ${userPreferences.avoidWeekends}
            
            Consider:
            - Work hours (9 AM - 6 PM) for work-related tasks
            - Morning (8 AM - 10 AM) for health/exercise
            - Evening (6 PM - 9 PM) for personal tasks
            - Weekends for leisure activities
            
            Return ONLY the suggested time in ISO 8601 format (yyyy-MM-dd'T'HH:mm:ss).
            Example: 2024-01-15T09:00:00
            """.trimIndent()
            
            val response = model.generateContent(prompt)
            val timeText = response.text?.trim()
            
            if (timeText != null) {
                val timestamp = parseISO8601(timeText)
                Result.success(timestamp)
            } else {
                // Fallback to default scheduling
                Result.success(calculateDefaultTime(title))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Parse ISO 8601 timestamp to milliseconds
     */
    private fun parseISO8601(isoString: String): Long {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        return try {
            format.parse(isoString)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis() + 3600000 // Default to 1 hour from now
        }
    }
    
    /**
     * Calculate default time based on keyword matching
     * Fallback when AI is unavailable
     */
    private fun calculateDefaultTime(title: String): Long {
        val calendar = Calendar.getInstance()
        val lowerTitle = title.lowercase()
        
        when {
            lowerTitle.contains("morning") -> {
                calendar.set(Calendar.HOUR_OF_DAY, 8)
                calendar.set(Calendar.MINUTE, 0)
            }
            lowerTitle.contains("afternoon") -> {
                calendar.set(Calendar.HOUR_OF_DAY, 14)
                calendar.set(Calendar.MINUTE, 0)
            }
            lowerTitle.contains("evening") -> {
                calendar.set(Calendar.HOUR_OF_DAY, 18)
                calendar.set(Calendar.MINUTE, 0)
            }
            lowerTitle.contains("night") -> {
                calendar.set(Calendar.HOUR_OF_DAY, 20)
                calendar.set(Calendar.MINUTE, 0)
            }
            else -> {
                calendar.add(Calendar.HOUR, 1)
            }
        }
        
        return calendar.timeInMillis
    }
    
    /**
     * User scheduling preferences
     */
    data class UserPreferences(
        val preferredStartHour: Int = 9,
        val preferredEndHour: Int = 18,
        val avoidWeekends: Boolean = false,
        val workDays: List<Int> = listOf(1, 2, 3, 4, 5) // Mon-Fri
    )
}
