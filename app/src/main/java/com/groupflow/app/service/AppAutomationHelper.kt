package com.groupflow.app.service

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log

/**
 * Helper class to handle app automation - opening apps at scheduled times
 * Note: Direct app closing is restricted by Android for security reasons.
 * We can only open apps and show notifications to remind users to close them.
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: android.graphics.drawable.Drawable? = null
)

class AppAutomationHelper(private val context: Context) {
    
    companion object {
        private const val TAG = "AppAutomation"
    }
    
    /**
     * Open an app by its package name
     * @param packageName The package name of the app to open (e.g., "com.spotify.music")
     * @return true if app was opened successfully, false otherwise
     */
    fun openApp(packageName: String): Boolean {
        return try {
            val packageManager = context.packageManager
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                Log.d(TAG, "Successfully opened app: $packageName")
                true
            } else {
                Log.e(TAG, "Could not find launch intent for package: $packageName")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening app $packageName: ${e.message}", e)
            false
        }
    }
    
    /**
     * Check if an app is installed on the device
     * @param packageName The package name to check
     * @return true if app is installed, false otherwise
     */
    fun isAppInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    
    /**
     * Get the app name from package name
     * @param packageName The package name
     * @return The app display name, or the package name if not found
     */
    fun getAppName(packageName: String): String {
        return try {
            val packageManager = context.packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting app name for $packageName: ${e.message}", e)
            packageName
        }
    }
    
    /**
     * Get list of installed music apps on the device
     * @return List of package names for music apps
     */
    fun getMusicApps(): List<String> {
        val musicApps = mutableListOf<String>()
        val packageManager = context.packageManager
        
        // Common music app package names
        val commonMusicApps = listOf(
            "com.spotify.music",
            "com.google.android.music",
            "com.apple.android.music",
            "com.amazon.mp3",
            "com.miui.player",
            "com.sec.android.app.music",
            "com.tidal",
            "com.deezer.android.app",
            "com.soundcloud.android",
            "com.yandex.music",
            "com.rhapsody",
            "com.pandora.android",
            "com.audials"
        )
        
        for (packageName in commonMusicApps) {
            if (isAppInstalled(packageName)) {
                musicApps.add(packageName)
            }
        }
        
        Log.d(TAG, "Found ${musicApps.size} music apps: $musicApps")
        return musicApps
    }
    
    /**
     * Get all installed apps on the device
     * @return List of AppInfo with package names, app names, and icons
     */
    fun getAllInstalledApps(): List<AppInfo> {
        val apps = mutableListOf<AppInfo>()
        val packageManager = context.packageManager
        
        try {
            val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            
            for (packageInfo in packages) {
                // Filter out system apps if needed
                if (packageInfo.packageName.startsWith("com.android") || 
                    packageInfo.packageName.startsWith("com.google.android")) {
                    continue
                }
                
                try {
                    val appInfo = packageManager.getApplicationInfo(packageInfo.packageName, 0)
                    val appName = packageManager.getApplicationLabel(appInfo).toString()
                    val icon = packageManager.getApplicationIcon(appInfo)
                    
                    apps.add(AppInfo(packageInfo.packageName, appName, icon))
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting info for ${packageInfo.packageName}: ${e.message}")
                }
            }
            
            // Sort alphabetically by app name
            apps.sortBy { it.appName.lowercase() }
            
            Log.d(TAG, "Found ${apps.size} installed apps")
        } catch (e: Exception) {
            Log.e(TAG, "Error getting installed apps: ${e.message}", e)
        }
        
        return apps
    }
    
    /**
     * Search apps by name
     * @param query Search query
     * @return List of matching apps
     */
    fun searchApps(query: String): List<AppInfo> {
        val allApps = getAllInstalledApps()
        return allApps.filter { 
            it.appName.lowercase().contains(query.lowercase()) ||
            it.packageName.lowercase().contains(query.lowercase())
        }
    }
}
