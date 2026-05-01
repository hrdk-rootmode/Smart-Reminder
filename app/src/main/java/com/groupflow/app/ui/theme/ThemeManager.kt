package com.groupflow.app.ui.theme

import android.content.Context
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.groupflow.app.data.local.entity.User
import java.util.Calendar

object ThemeManager {
    
    enum class UserTier {
        GUEST, LOGGED_IN, PREMIUM
    }
    
    /**
     * Get theme for user based on their tier
     */
    fun getThemeForUser(
        tier: UserTier,
        selectedThemeId: String? = null,
        context: Context? = null,
        forceDark: Boolean = false
    ): ColorScheme {
        if (forceDark) {
            return when (tier) {
                UserTier.GUEST -> guestDarkTheme()
                UserTier.LOGGED_IN -> loggedInDarkTheme(context)
                UserTier.PREMIUM -> premiumTheme(selectedThemeId)
            }
        }
        return when (tier) {
            UserTier.GUEST -> guestTheme()
            UserTier.LOGGED_IN -> loggedInTheme(context)
            UserTier.PREMIUM -> premiumTheme(selectedThemeId)
        }
    }
    
    /**
     * Guest mode theme - Minimalist
     * Primary: #607D8B (Blue Grey)
     * Secondary: #90A4AE (Light Blue Grey)
     */
    private fun guestTheme(): ColorScheme {
        return lightColorScheme(
            primary = Color(0xFF607D8B),
            secondary = Color(0xFF90A4AE),
            background = Color.White,
            surface = Color(0xFFF5F5F5),
            onSurface = Color(0xFF212121),
            primaryContainer = Color(0xFFBBDEFB),
            onPrimaryContainer = Color(0xFF0D47A1)
        )
    }
    
    /**
     * Logged-in mode theme - Dynamic based on user profile
     * Extracts color from user's email hash or uses time-based accent
     */
    private fun loggedInTheme(context: Context?): ColorScheme {
        val userProfileColor = getUserProfileColor(context)
        val timeBasedAccent = getTimeBasedAccent()
        
        return lightColorScheme(
            primary = userProfileColor,
            secondary = getComplementaryColor(userProfileColor),
            background = Color(0xFFFAFAFA),
            surface = Color.White,
            onSurface = Color(0xFF1A1A1A),
            tertiary = timeBasedAccent,
            primaryContainer = userProfileColor.copy(alpha = 0.12f),
            onPrimaryContainer = userProfileColor
        )
    }

    private fun guestDarkTheme(): ColorScheme {
        return darkColorScheme(
            primary = Color(0xFF90A4AE),
            secondary = Color(0xFF607D8B),
            background = Color(0xFF0F1214),
            surface = Color(0xFF161A1D),
            onSurface = Color(0xFFE8EAED),
            primaryContainer = Color(0xFF1E2930),
            onPrimaryContainer = Color(0xFFD5E3EA)
        )
    }

    private fun loggedInDarkTheme(context: Context?): ColorScheme {
        val userProfileColor = getUserProfileColor(context)
        val accent = getTimeBasedAccent()
        return darkColorScheme(
            primary = userProfileColor.copy(alpha = 0.9f),
            secondary = getComplementaryColor(userProfileColor).copy(alpha = 0.85f),
            background = Color(0xFF0B1020),
            surface = Color(0xFF131B2C),
            onSurface = Color(0xFFE6ECFF),
            tertiary = accent,
            primaryContainer = userProfileColor.copy(alpha = 0.25f),
            onPrimaryContainer = Color(0xFFE6ECFF)
        )
    }
    
    /**
     * Premium mode theme - Dark gradient
     */
    private fun premiumTheme(themeId: String?): ColorScheme {
        val themes = mapOf(
            "midnight" to darkColorScheme(
                primary = Color(0xFF6366F1),
                secondary = Color(0xFFA855F7),
                background = Color(0xFF0F172A),
                surface = Color(0xFF1E293B).copy(alpha = 0.7f),
                onSurface = Color.White,
                onSurfaceVariant = Color(0xFFCBD5E1)
            ),
            "sunset" to darkColorScheme(
                primary = Color(0xFFFF6B6B),
                secondary = Color(0xFFFD7E14),
                background = Color(0xFF1A0A0A),
                surface = Color(0xFF2D1515).copy(alpha = 0.7f),
                onSurface = Color.White,
                onSurfaceVariant = Color(0xFFCBD5E1)
            ),
            "ocean" to darkColorScheme(
                primary = Color(0xFF0EA5E9),
                secondary = Color(0xFF06B6D4),
                background = Color(0xFF0A1929),
                surface = Color(0xFF1B2A41).copy(alpha = 0.7f),
                onSurface = Color.White,
                onSurfaceVariant = Color(0xFFCBD5E1)
            )
        )
        
        return themes[themeId] ?: themes["midnight"]!!
    }
    
    /**
     * Extract user profile color from email hash
     * In production, this would extract from Gmail profile picture dominant color
     */
    private fun getUserProfileColor(context: Context?): Color {
        // For now, use a hash of the email to generate a consistent color
        // In production, extract from user's Gmail profile picture dominant color
        val email = context?.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            ?.getString("user_email", "") ?: ""
        
        if (email.isEmpty()) return Color(0xFF6366F1) // Default indigo
        
        val hash = email.hashCode()
        val hue = Math.abs(hash % 360)
        
        return Color.hsv(hue.toFloat(), 0.7f, 0.6f)
    }
    
    /**
     * Calculate complementary color
     */
    private fun getComplementaryColor(color: Color): Color {
        return Color(
            red = 1f - color.red,
            green = 1f - color.green,
            blue = 1f - color.blue
        )
    }
    
    /**
     * Get time-based accent color
     * Morning (6AM-12PM): Warm Orange
     * Afternoon (12PM-6PM): Sky Blue
     * Evening (6PM-10PM): Purple
     * Night (10PM-6AM): Cyan
     */
    private fun getTimeBasedAccent(): Color {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        
        return when (hour) {
            in 6..11 -> Color(0xFFFFB74D) // Morning - Warm Orange
            in 12..17 -> Color(0xFF64B5F6) // Afternoon - Sky Blue
            in 18..21 -> Color(0xFF9575CD) // Evening - Purple
            else -> Color(0xFF4FC3F7) // Night - Cyan
        }
    }
    
    /**
     * Get user tier from Firebase auth state
     */
    fun getUserTier(currentUser: User?): UserTier {
        return if (currentUser != null) {
            // Check if user is premium (would check Firebase database or Play Billing)
            val isPremium = currentUser.isPremium
            if (isPremium) UserTier.PREMIUM else UserTier.LOGGED_IN
        } else {
            UserTier.GUEST
        }
    }
    
    /**
     * Get available theme options for user tier
     */
    fun getAvailableThemes(tier: UserTier): List<ThemeOption> {
        return when (tier) {
            UserTier.GUEST -> listOf(
                ThemeOption("light", "Light", "Clean and minimal"),
                ThemeOption("dark", "Dark", "Easy on the eyes")
            )
            UserTier.LOGGED_IN -> listOf(
                ThemeOption("light", "Light", "Clean and minimal"),
                ThemeOption("dark", "Dark", "Easy on the eyes"),
                ThemeOption("dynamic", "Dynamic", "Based on your profile")
            )
            UserTier.PREMIUM -> listOf(
                ThemeOption("light", "Light", "Clean and minimal"),
                ThemeOption("dark", "Dark", "Easy on the eyes"),
                ThemeOption("dynamic", "Dynamic", "Based on your profile"),
                ThemeOption("midnight", "Midnight", "Premium dark gradient"),
                ThemeOption("sunset", "Sunset", "Warm premium theme"),
                ThemeOption("ocean", "Ocean", "Cool premium theme")
            )
        }
    }
    
    data class ThemeOption(
        val id: String,
        val name: String,
        val description: String
    )
}
