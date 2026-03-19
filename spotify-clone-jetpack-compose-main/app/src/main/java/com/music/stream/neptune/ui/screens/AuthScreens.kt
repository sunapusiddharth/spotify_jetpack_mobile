package com.music.stream.neptune.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.stream.neptune.ui.viewmodel.AuthUiState

@Composable
fun AuthLoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF081F2A), Color(0xFF101520), Color.Black)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Color(0xFF39D98A))
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Restoring your listening session...",
                color = Color.White.copy(alpha = 0.86f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun AuthLoginScreen(
    state: AuthUiState,
    onLogin: () -> Unit,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF081F2A), Color(0xFF101520), Color.Black)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Color(0x201A2633),
            tonalElevation = 6.dp,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "SidFlix Music",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (state.bypassEnabled)
                        "Auth bypass mode is enabled. The app will use the test user from gradle.properties."
                    else
                        "Sign in with Auth0 to continue your songs, stations, and podcasts.",
                    color = Color.White.copy(alpha = 0.78f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(18.dp))

                if (!state.configured) {
                    Text(
                        text = "Auth0 configuration is missing. Add AUTH0_DOMAIN and AUTH0_CLIENT_ID in gradle.properties.",
                        color = Color(0xFFFFB4AB),
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D8CFF))
                    ) {
                        Text("Retry Config")
                    }
                } else {
                    Button(
                        onClick = onLogin,
                        enabled = !state.loading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF39D98A))
                    ) {
                        Text(
                            if (state.loading) "Opening Auth0..."
                            else if (state.bypassEnabled) "Continue In Test Mode"
                            else "Continue With Auth0"
                        )
                    }
                }

                if (!state.errorMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = state.errorMessage,
                        color = Color(0xFFFFB4AB),
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
