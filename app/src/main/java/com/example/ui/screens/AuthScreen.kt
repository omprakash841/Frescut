package com.example.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.example.ui.theme.FrescutBorderLight
import com.example.ui.theme.FrescutFocusRed
import com.example.ui.theme.FrescutRedBright
import com.example.ui.theme.FrescutRedLightBg
import com.example.ui.theme.FrescutRedPrimary
import com.example.ui.theme.FrescutSurfaceTint
import com.example.ui.theme.FrescutTextMuted
import com.example.ui.theme.FrescutTextPrimary
import com.example.ui.theme.FrescutTextSecondary
import com.example.ui.theme.FrescutWhite
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class AuthSessionInfo(
    val uid: String,
    val displayName: String,
    val email: String,
    val isGuest: Boolean
)

object LocalAuthSessionManager {
    private const val PREFS_NAME = "frescut_auth_session_prefs"
    private const val KEY_UID = "session_uid"
    private const val KEY_NAME = "session_name"
    private const val KEY_EMAIL = "session_email"
    private const val KEY_IS_GUEST = "session_is_guest"
    private const val KEY_PERMANENT_GUEST_ID = "permanent_guest_uid"

    fun getSavedSession(context: Context): AuthSessionInfo? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val uid = prefs.getString(KEY_UID, null)?.takeIf { it.isNotBlank() } ?: return null
        val isGuest = prefs.getBoolean(KEY_IS_GUEST, false)
        val name = prefs.getString(KEY_NAME, if (isGuest) context.getString(R.string.profile_guest_name) else "")
            ?: if (isGuest) context.getString(R.string.profile_guest_name) else ""
        val email = if (isGuest) "" else prefs.getString(KEY_EMAIL, "").orEmpty()
        return AuthSessionInfo(
            uid = uid,
            displayName = if (isGuest) context.getString(R.string.profile_guest_name) else name,
            email = email,
            isGuest = isGuest
        )
    }

    fun startGuestSession(context: Context): AuthSessionInfo {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var guestUid = prefs.getString(KEY_PERMANENT_GUEST_ID, null)
        if (guestUid.isNullOrBlank() || !guestUid.startsWith("emu_guest_")) {
            guestUid = "emu_guest_${UUID.randomUUID().toString().replace("-", "").take(16)}"
            prefs.edit().putString(KEY_PERMANENT_GUEST_ID, guestUid).apply()
        }
        val guestName = context.getString(R.string.profile_guest_name)
        val info = AuthSessionInfo(
            uid = guestUid,
            displayName = guestName,
            email = "",
            isGuest = true
        )
        saveSession(context, info)
        return info
    }

    fun saveGoogleFallbackSession(
        context: Context,
        idTokenSubject: String?,
        displayName: String?,
        email: String?
    ): AuthSessionInfo {
        val cleanId = (email ?: idTokenSubject ?: UUID.randomUUID().toString())
            .replace(Regex("[^A-Za-z0-9_-]"), "_")
            .take(28)
        val uid = "guser_$cleanId"
        val info = AuthSessionInfo(
            uid = uid,
            displayName = displayName?.takeIf { it.isNotBlank() }
                ?: email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
                ?: context.getString(R.string.profile_guest_name),
            email = email?.takeIf { it.isNotBlank() } ?: "",
            isGuest = false
        )
        saveSession(context, info)
        return info
    }

    fun saveSession(context: Context, session: AuthSessionInfo) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_UID, session.uid)
            .putString(KEY_NAME, session.displayName)
            .putString(KEY_EMAIL, if (session.isGuest) "" else session.email)
            .putBoolean(KEY_IS_GUEST, session.isGuest)
            .apply()
    }

    fun clearSession(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(KEY_UID)
            .remove(KEY_NAME)
            .remove(KEY_EMAIL)
            .remove(KEY_IS_GUEST)
            .apply()
    }
}

private fun resolveDefaultWebClientId(context: Context): String {
    val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
    return if (resId != 0) {
        runCatching { context.getString(resId) }.getOrDefault("")
    } else {
        ""
    }
}

@Composable
fun AuthScreen(
    onAuthSuccess: (AuthSessionInfo?) -> Unit = {},
    onSignInSuccess: (AuthSessionInfo?) -> Unit = onAuthSuccess
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val cancelledErrorText = stringResource(R.string.auth_error_cancelled)
    val noCredentialErrorText = stringResource(R.string.auth_error_no_credential)
    val genericErrorText = stringResource(R.string.auth_error_generic)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FrescutSurfaceTint)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .testTag("auth_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = FrescutWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(FrescutRedLightBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "FC",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = FrescutRedPrimary,
                            letterSpacing = 1.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.auth_brand_title),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = FrescutRedPrimary,
                        letterSpacing = 1.2.sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(R.string.auth_brand_subtitle),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = FrescutTextPrimary
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.auth_welcome_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = FrescutTextPrimary
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(R.string.auth_welcome_subtitle),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = FrescutTextSecondary,
                        lineHeight = 20.sp
                    ),
                    textAlign = TextAlign.Center
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorMessage.orEmpty(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = FrescutFocusRed,
                            fontWeight = FontWeight.Medium
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("auth_error_text")
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        errorMessage = null
                        isLoading = true
                        coroutineScope.launch {
                            try {
                                val webClientId = resolveDefaultWebClientId(context)
                                if (webClientId.isBlank()) {
                                    errorMessage = genericErrorText
                                    return@launch
                                }
                                val googleOption = GetSignInWithGoogleOption.Builder(webClientId).build()
                                val request = GetCredentialRequest.Builder()
                                    .addCredentialOption(googleOption)
                                    .build()
                                val result = credentialManager.getCredential(context, request)
                                val credential = result.credential
                                if (credential is CustomCredential &&
                                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                                ) {
                                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                    val firebaseCred = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                                    try {
                                        val authResult = Firebase.auth.signInWithCredential(firebaseCred).await()
                                        val fbUser = authResult.user
                                        val session = AuthSessionInfo(
                                            uid = fbUser?.uid ?: "guser_${UUID.randomUUID().toString().take(12)}",
                                            displayName = fbUser?.displayName
                                                ?: googleIdTokenCredential.displayName
                                                ?: context.getString(R.string.profile_guest_name),
                                            email = fbUser?.email ?: googleIdTokenCredential.id,
                                            isGuest = false
                                        )
                                        LocalAuthSessionManager.saveSession(context, session)
                                        onSignInSuccess(session)
                                    } catch (firebaseEx: Exception) {
                                        Log.w("AuthScreen", "Firebase token exchange fallback used", firebaseEx)
                                        val fallbackSession = LocalAuthSessionManager.saveGoogleFallbackSession(
                                            context = context,
                                            idTokenSubject = googleIdTokenCredential.id,
                                            displayName = googleIdTokenCredential.displayName,
                                            email = googleIdTokenCredential.id
                                        )
                                        onSignInSuccess(fallbackSession)
                                    }
                                } else {
                                    errorMessage = genericErrorText
                                }
                            } catch (e: GetCredentialCancellationException) {
                                Log.w("AuthScreen", "User cancelled Google sign-in", e)
                                errorMessage = cancelledErrorText
                            } catch (e: NoCredentialException) {
                                Log.w("AuthScreen", "No Google credentials on device", e)
                                errorMessage = noCredentialErrorText
                            } catch (e: Exception) {
                                Log.e("AuthScreen", "Google sign-in failed", e)
                                errorMessage = genericErrorText
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("google_sign_in_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FrescutRedBright,
                        contentColor = FrescutWhite
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = FrescutWhite,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.auth_signing_in),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = stringResource(R.string.auth_continue_with_google),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.auth_continue_with_google),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = FrescutBorderLight)
                    Text(
                        text = "  ${stringResource(R.string.auth_or_divider)}  ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = FrescutTextMuted,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = FrescutBorderLight)
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = {
                        errorMessage = null
                        val guestSession = LocalAuthSessionManager.startGuestSession(context)
                        onSignInSuccess(guestSession)
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("continue_as_guest_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, FrescutBorderLight),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = FrescutTextPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.PersonOutline,
                        contentDescription = stringResource(R.string.auth_continue_as_guest),
                        tint = FrescutRedPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.auth_continue_as_guest),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = FrescutTextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.auth_footer_note),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = FrescutTextMuted
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

suspend fun signOutFromFirebase(context: Context, credentialManager: CredentialManager) {
    LocalAuthSessionManager.clearSession(context)
    Firebase.auth.signOut()
    try {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
    } catch (e: Exception) {
        Log.w("AuthScreen", "Failed to clear credential manager state", e)
    }
}

fun signOutFromFirebase(
    context: Context,
    credentialManager: CredentialManager,
    scope: kotlinx.coroutines.CoroutineScope,
    onSignOutComplete: () -> Unit = {}
) {
    scope.launch {
        signOutFromFirebase(context, credentialManager)
        onSignOutComplete()
    }
}
