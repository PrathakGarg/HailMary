package com.arise.habitquest.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arise.habitquest.data.local.datastore.OnboardingDataStore
import com.arise.habitquest.ui.theme.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToHome: () -> Unit,
    dataStore: OnboardingDataStore = androidx.hilt.navigation.compose.hiltViewModel<SplashViewModel>().let {
        it.dataStore
    }
) {
    val viewModel = androidx.hilt.navigation.compose.hiltViewModel<SplashViewModel>()

    LaunchedEffect(Unit) {
        viewModel.checkAndNavigate(onNavigateToOnboarding, onNavigateToHome)
    }

    SplashContent()
}

@Composable
fun SplashContent() {
    var bootText by remember { mutableStateOf("") }
    val fullText = "SYSTEM INITIALISING..."

    val bgAlpha by rememberInfiniteTransition(label = "bg").animateFloat(
        initialValue = 0.3f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = "bg_alpha"
    )

    LaunchedEffect(Unit) {
        fullText.forEachIndexed { i, _ ->
            delay(60)
            bootText = fullText.substring(0, i + 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        PurpleCore.copy(alpha = bgAlpha * 0.15f),
                        BackgroundDeep
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ARISE logo
            Text(
                text = "▲",
                style = AriseTypography.displayLarge.copy(
                    color = PurpleCore,
                    fontSize = 80.sp
                )
            )

            Text(
                text = "ARISE",
                style = AriseTypography.displaySmall.copy(
                    color = TextPrimary,
                    letterSpacing = 12.sp
                )
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = bootText,
                style = SystemTextStyle.copy(color = PurpleLight, fontSize = 12.sp),
                textAlign = TextAlign.Center
            )
        }
    }
}
