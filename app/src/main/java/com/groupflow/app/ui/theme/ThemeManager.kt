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
        context: Context? = null
    ): ColorScheme {
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
    
    /**
     * Premium mode theme - Dark gradient
     * Primary: Gradient (#6366F1 → #A855F7)
     * Secondary: Gradient (#EC4899 → #F59E0B)
     * Background: Dark gradient (#0F172A → #1E293B)
     * Surface: Glassmorphic cards
     * OnSurface: #FFFFFF
     * Accent: Gold (#FFD700)
     */
    private fun premiumTheme(themeId: String?): ColorScheme {
        val themes = mapOf(
            "midnight" to darkColorScheme(
                primary = Color(0xFF6366F1),
                secondary = Color(0xFFA855F7),
                tertiary = Color(0xFFEC4899),
                background = Color(0xFF0F172A),
                surface = Color(0xFF1E293B),
                onSurface = Color.White,
                onSurfaceVariant = Color(0xFFCBD5E1),
                primaryContainer = Color(0xFF312E81),
                onPrimaryContainer = Color(0xFFE0E7FF)
            ),
            "sunset" to darkColorScheme(
                primary = Color(0xFFFF6B6B),
                secondary = Color(0xFFFD7E14),
                tertiary = Color(0xFF6366F1),
                background = Color(0xFF1A0A0A),
                surface = Color(0xFF2D1515),
                onSurface = Color.White,
                onSurfaceVariant = Color(0xFFCBD5E1),
                primaryContainer = Color(0xFF991B1B),
                onPrimaryContainer = Color(0xFFFFE5E5)
            ),
            "ocean" to darkColorScheme(
                primary = Color(0xFF0EA5E9),
                secondary = Color(0xFF06B6D4),
                tertiary = Color(0xFF6366F1),
                background = Color(0xFF0A1929),
                surface = Color(0xFF1B2A41),
                onSurface = Color.White,
                onSurfaceVariant = Color(0xFFCBD5E1),
                primaryContainer = Color(0xFF164E63),
                onPrimaryContainer = Color(0xFFE0F2FE)
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
            // For testing: set to true to see premium theme
            val isPremium = false // Change to true to test premium dark gradient theme
            if (isPremium) UserTier.PREMIUM else UserTier.LOGGED_IN
        } else {
            UserTier.GUEST
        }
    }
}
