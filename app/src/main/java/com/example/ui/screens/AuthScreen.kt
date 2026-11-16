package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.util.Log
import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.ui.theme.FrescutBorderLight
import com.example.ui.theme.FrescutDivider
import com.example.ui.theme.FrescutGold
import com.example.ui.theme.FrescutRedBright
import com.example.ui.theme.FrescutRedDark
import com.example.ui.theme.FrescutRedLightBg
import com.example.ui.theme.FrescutSurface
import com.example.ui.theme.FrescutTextPrimary
import com.example.ui.theme.FrescutTextSecondary
import com.example.ui.theme.FrescutWhite
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private const val AUTH_PREFS_NAME = "frescut_auth_accounts_prefs"
private const val KEY_ACTIVE_SESSION_UID = "active_session_uid"
private const val KEY_ACTIVE_SESSION_NAME = "active_session_name"
private const val KEY_ACTIVE_SESSION_EMAIL = "active_session_email"

data class AuthSessionInfo(
    val uid: String,
    val displayName: String,
    val email: String
)

object LocalAuthSessionManager {
    fun getSavedSession(context: Context): AuthSessionInfo? {
        val prefs = context.applicationContext.getSharedPreferences(AUTH_PREFS_NAME, Context.MODE_PRIVATE)
        val rawUid = prefs.getString(KEY_ACTIVE_SESSION_UID, null) ?: return null
        val uid = when {
            rawUid == "guest_frescut_user" -> {
                val freshGuestUid = "emu_guest_${System.currentTimeMillis()}"
                prefs.edit().putString(KEY_ACTIVE_SESSION_UID, freshGuestUid).apply()
                freshGuestUid
            }
            rawUid.startsWith("user_") -> {
                val migratedUid = "emu_$rawUid"
                prefs.edit().putString(KEY_ACTIVE_SESSION_UID, migratedUid).apply()
                migratedUid
            }
            else -> rawUid
        }
        val name = prefs.getString(KEY_ACTIVE_SESSION_NAME, "FRESCUT Customer") ?: "FRESCUT Customer"
        val email = prefs.getString(KEY_ACTIVE_SESSION_EMAIL, "") ?: ""
        return AuthSessionInfo(uid = uid, displayName = name, email = email)
    }

    fun saveSession(context: Context, session: AuthSessionInfo) {
        val prefs = context.applicationContext.getSharedPreferences(AUTH_PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_ACTIVE_SESSION_UID, session.uid)
            .putString(KEY_ACTIVE_SESSION_NAME, session.displayName)
            .putString(KEY_ACTIVE_SESSION_EMAIL, session.email)
            .apply()
    }

    fun clearSession(context: Context) {
        val prefs = context.applicationContext.getSharedPreferences(AUTH_PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(KEY_ACTIVE_SESSION_UID)
            .remove(KEY_ACTIVE_SESSION_NAME)
            .remove(KEY_ACTIVE_SESSION_EMAIL)
            .apply()
    }

    fun loginWithEmailPassword(
        context: Context,
        rawEmail: String,
        password: String
    ): Result<AuthSessionInfo> {
        val email = rawEmail.trim().lowercase()
        if (email.isEmpty() || password.isEmpty()) {
            return Result.failure(IllegalArgumentException("कृपया ईमेल और पासवर्ड दोनों भरें।"))
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.failure(IllegalArgumentException("कृपया सही ईमेल पता लिखें (जैसे नाम@gmail.com)।"))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("पासवर्ड कमज़ोर है! कृपया कम से कम 6 अक्षरों का पासवर्ड डालें।"))
        }

        val prefs = context.applicationContext.getSharedPreferences(AUTH_PREFS_NAME, Context.MODE_PRIVATE)

        val storedPassword = prefs.getString("pwd_$email", null)
            ?: return Result.failure(
                IllegalArgumentException("इस ईमेल से कोई अकाउंट नहीं मिला। कृपया पहले 'Sign Up' टैब में नया अकाउंट बनाएं।")
            )

        if (storedPassword != password) {
            return Result.failure(IllegalArgumentException("गलत पासवर्ड! कृपया सही पासवर्ड डालें।"))
        }

        val storedName = prefs.getString("name_$email", null)
            ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }
        val uid = "emu_user_${email.hashCode().toUInt()}"
        val session = AuthSessionInfo(uid = uid, displayName = storedName, email = email)
        saveSession(context, session)
        return Result.success(session)
    }

    fun signUpWithEmailPassword(
        context: Context,
        rawName: String,
        rawEmail: String,
        password: String
    ): Result<AuthSessionInfo> {
        val email = rawEmail.trim().lowercase()
        val name = rawName.trim().ifBlank {
            email.substringBefore("@").replaceFirstChar { it.uppercase() }.ifBlank { "FRESCUT Customer" }
        }
        if (email.isEmpty() || password.isEmpty()) {
            return Result.failure(IllegalArgumentException("कृपया ईमेल और पासवर्ड दोनों भरें।"))
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.failure(IllegalArgumentException("कृपया सही ईमेल पता लिखें (जैसे नाम@gmail.com)।"))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("पासवर्ड कमज़ोर है! कृपया कम से कम 6 अक्षरों का पासवर्ड डालें।"))
        }

        val prefs = context.applicationContext.getSharedPreferences(AUTH_PREFS_NAME, Context.MODE_PRIVATE)

        if (prefs.contains("pwd_$email")) {
            return Result.failure(
                IllegalArgumentException("यह ईमेल पहले से इस्तेमाल में है। कृपया 'Login' टैब से लॉगिन करें।")
            )
        }

        val uid = "emu_user_${email.hashCode().toUInt()}"
        val session = AuthSessionInfo(uid = uid, displayName = name, email = email)
        prefs.edit()
            .putString("pwd_$email", password)
            .putString("name_$email", name)
            .apply()
        saveSession(context, session)
        return Result.success(session)
    }

    fun continueAsGuest(context: Context): AuthSessionInfo {
        val session = AuthSessionInfo(
            uid = "emu_guest_${System.currentTimeMillis()}",
            displayName = "Guest Customer",
            email = "guest@frescut.in"
        )
        saveSession(context, session)
        return session
    }
}

private fun resolveDefaultWebClientId(context: Context): String? {
    val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
    if (resId != 0) {
        val value = runCatching { context.getString(resId) }.getOrNull()
        if (!value.isNullOrBlank()) return value
    }
    return "246774246452-afr9mvgb41q3um7doun3kua88250a42t.apps.googleusercontent.com"
}

fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope
) {
    if (Firebase.auth.currentUser != null) {
        onAuthSuccess()
        return
    }
    val clientId = resolveDefaultWebClientId(context)
    if (clientId.isNullOrBlank()) {
        onUnauthenticated()
        return
    }

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onUnauthenticated()
            }
        } catch (e: Exception) {
            onUnauthenticated()
        }
    }
}

fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {}
) {
    val clientId = resolveDefaultWebClientId(context)
    if (clientId.isNullOrBlank()) {
        onAuthError("Google Sign-In कॉन्फ़िगरेशन उपलब्ध नहीं है।")
        return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(signInOption)
        .build()

    scope.launch {
        try {
            val activityContext = context as? Activity ?: context
            val result = credentialManager.getCredential(activityContext, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onAuthError("Google लॉगिन पूरा नहीं हो सका।")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In cancelled or dismissed: ${e.message}", e)
            onAuthCancelled()
        } catch (e: Exception) {
            Log.w("Auth", "Google Sign-In unavailable on this device/emulator: ${e.message}")
            onAuthError("एमुलेटर में Google अकाउंट नहीं मिला। कृपया ऊपर Email/Password या नीचे 'Continue as Guest' का उपयोग करें।")
        }
    }
}

fun signOutFromFirebase(
    context: Context,
    credentialManager: CredentialManager,
    scope: CoroutineScope,
    onSignOutComplete: () -> Unit = {}
) {
    scope.launch {
        withContext(NonCancellable) {
            try {
                LocalAuthSessionManager.clearSession(context)
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w("Auth", "Failed to clear credential state: ${e.message}")
            } finally {
                Firebase.auth.signOut()
                onSignOutComplete()
            }
        }
    }
}

private enum class AuthTab {
    LOGIN,
    SIGN_UP
}

@Composable
fun AuthScreen(
    onAuthSuccess: (AuthSessionInfo?) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var selectedTab by remember { mutableStateOf(AuthTab.LOGIN) }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = { onAuthSuccess(null) },
            onUnauthenticated = {},
            scope = coroutineScope
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FrescutWhite)
            .windowInsetsPadding(WindowInsets.statusBars)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .testTag("auth_screen")
    ) {
        // Compact Brand Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(175.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(FrescutRedDark, FrescutRedBright)
                    )
                )
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_hero_farm_to_home_1794299674996),
                contentDescription = "FRESCUT Fresh Meat & Fish",
                contentScale = ContentScale.Crop,
                alpha = 0.22f,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.16f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = null,
                            tint = FrescutGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "0–4°C COLD-CHAIN • 30 MIN DELIVERY",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.5.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "FRESCUT",
                    color = Color.White,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp,
                        letterSpacing = 1.5.sp
                    )
                )

                Text(
                    text = "Fresh Meat & Fish Delivery",
                    color = Color.White.copy(alpha = 0.92f),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp
                    )
                )
            }
        }

        // Main Auth Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Login / Sign Up Two-Tab Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(FrescutSurface)
                    .border(1.dp, FrescutBorderLight, RoundedCornerShape(14.dp))
                    .padding(4.dp)
            ) {
                AuthTabButton(
                    title = "Login",
                    selected = selectedTab == AuthTab.LOGIN,
                    onClick = {
                        selectedTab = AuthTab.LOGIN
                        errorMessage = null
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("auth_tab_login")
                )
                AuthTabButton(
                    title = "Sign Up",
                    selected = selectedTab == AuthTab.SIGN_UP,
                    onClick = {
                        selectedTab = AuthTab.SIGN_UP
                        errorMessage = null
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("auth_tab_signup")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error Message Banner (Simple Hindi)
            if (errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("auth_error_banner"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = FrescutRedLightBg)
                ) {
                    Text(
                        text = errorMessage.orEmpty(),
                        color = FrescutRedBright,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        ),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Full Name Field (Sign Up tab only)
            if (selectedTab == AuthTab.SIGN_UP) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = {
                        fullName = it
                        errorMessage = null
                    },
                    label = { Text("आपका नाम (Full Name)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = FrescutRedBright
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FrescutRedBright,
                        unfocusedBorderColor = FrescutBorderLight,
                        focusedLabelColor = FrescutRedBright
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_name_input")
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null
                },
                label = { Text("ईमेल (Email Address)") },
                placeholder = { Text("user@example.com") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = null,
                        tint = FrescutRedBright
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FrescutRedBright,
                    unfocusedBorderColor = FrescutBorderLight,
                    focusedLabelColor = FrescutRedBright
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_email_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                label = { Text("पासवर्ड (कम से कम 6 अक्षर)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = FrescutRedBright
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) {
                                Icons.Filled.VisibilityOff
                            } else {
                                Icons.Filled.Visibility
                            },
                            contentDescription = "Toggle Password Visibility",
                            tint = FrescutTextSecondary
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FrescutRedBright,
                    unfocusedBorderColor = FrescutBorderLight,
                    focusedLabelColor = FrescutRedBright
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_password_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Email + Password Submit Button
            Button(
                onClick = {
                    errorMessage = null
                    val result = if (selectedTab == AuthTab.LOGIN) {
                        LocalAuthSessionManager.loginWithEmailPassword(
                            context = context,
                            rawEmail = email,
                            password = password
                        )
                    } else {
                        LocalAuthSessionManager.signUpWithEmailPassword(
                            context = context,
                            rawName = fullName,
                            rawEmail = email,
                            password = password
                        )
                    }
                    result.onSuccess { session ->
                        onAuthSuccess(session)
                    }.onFailure { err ->
                        errorMessage = err.message ?: "लॉगिन में त्रुटि हुई, कृपया दोबारा कोशिश करें।"
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("auth_email_submit_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FrescutRedBright,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (selectedTab == AuthTab.LOGIN) {
                        "Login with Email"
                    } else {
                        "Create Account (Sign Up)"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = FrescutDivider)
                Text(
                    text = "  OR  ",
                    color = FrescutTextSecondary,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = FrescutDivider)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. "Sign in with Google" Button (kept for real phone)
            OutlinedButton(
                onClick = {
                    errorMessage = null
                    isLoading = true
                    onGoogleSignInClicked(
                        context = context,
                        credentialManager = credentialManager,
                        onAuthSuccess = {
                            isLoading = false
                            onAuthSuccess(null)
                        },
                        onAuthError = { msg ->
                            isLoading = false
                            errorMessage = msg
                        },
                        scope = coroutineScope,
                        onAuthCancelled = {
                            isLoading = false
                        }
                    )
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("google_sign_in_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = FrescutTextPrimary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = FrescutRedBright,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Sign in with Google",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.5.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. "Continue as Guest" Button (for instant testing in Emulator)
            OutlinedButton(
                onClick = {
                    errorMessage = null
                    val guestSession = LocalAuthSessionManager.continueAsGuest(context)
                    onAuthSuccess(guestSession)
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("continue_as_guest_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = FrescutRedLightBg.copy(alpha = 0.45f),
                    contentColor = FrescutRedBright
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.PersonOutline,
                    contentDescription = null,
                    tint = FrescutRedBright,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Continue as Guest (Testing)",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun AuthTabButton(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) FrescutRedBright else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (selected) Color.White else FrescutTextPrimary,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
            )
        )
    }
}
