package com.groupflow.app.service

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.groupflow.app.data.local.entity.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

/**
 * Firebase Authentication Service
 *
 * NOTE: Firebase integration requires credentials to be set up.
 * To enable Firebase:
 * 1. Add google-services.json to app/ directory
 * 2. Replace WEB_CLIENT_ID with your actual Firebase Web Client ID
 */
class FirebaseAuthService(private val context: Context) {

    companion object {
        // Web Client ID from Firebase Console
        private const val WEB_CLIENT_ID = "380347093634-uvf1etg85i44g192erof0aea44msr4q5.apps.googleusercontent.com"
    }

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: Flow<User?> = _currentUser.asStateFlow()

    /**
     * Initialize Firebase Auth
     * Call this in Application.onCreate()
     */
    fun initialize() {
        firebaseAuth.addAuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                _currentUser.value = User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName ?: "",
                    photoUrl = firebaseUser.photoUrl?.toString(),
                    createdAt = System.currentTimeMillis()
                )
            } else {
                _currentUser.value = null
            }
        }
    }

    /**
     * Get Google Sign-In Intent
     * Call this from your Activity to launch Google Sign-In
     */
    fun getGoogleSignInIntent() = googleSignInClient.signInIntent

    /**
     * Handle Google Sign-In result
     * Call this in onActivityResult after Google Sign-In
     */
    suspend fun handleGoogleSignInResult(idToken: String?): Result<User> {
        return try {
            if (idToken == null) {
                Result.failure(IllegalArgumentException("Invalid ID token"))
            } else {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = firebaseAuth.signInWithCredential(credential).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val user = User(
                        uid = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        displayName = firebaseUser.displayName ?: "",
                        photoUrl = firebaseUser.photoUrl?.toString(),
                        createdAt = System.currentTimeMillis()
                    )
                    _currentUser.value = user
                    Result.success(user)
                } else {
                    Result.failure(IllegalStateException("Firebase user is null"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign in with email and password
     */
    suspend fun signInWithEmail(email: String, password: String): Result<User> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                val user = User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName ?: "",
                    photoUrl = firebaseUser.photoUrl?.toString(),
                    createdAt = System.currentTimeMillis()
                )
                _currentUser.value = user
                Result.success(user)
            } else {
                Result.failure(IllegalStateException("Firebase user is null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign up with email and password
     */
    suspend fun signUpWithEmail(email: String, password: String, name: String): Result<User> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                // Update user profile with name
                firebaseUser.updateProfile(
                    com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()
                ).await()

                val user = User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = name,
                    photoUrl = firebaseUser.photoUrl?.toString(),
                    createdAt = System.currentTimeMillis()
                )
                _currentUser.value = user
                Result.success(user)
            } else {
                Result.failure(IllegalStateException("Firebase user is null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign out
     */
    fun signOut() {
        firebaseAuth.signOut()
        googleSignInClient.signOut()
        _currentUser.value = null
    }

    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    /**
     * Check if user is signed in
     */
    fun isSignedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }
}
