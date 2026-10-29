package com.plantsense.ai.presentation.splash

import com.plantsense.ai.core.theme.Dimens
import com.plantsense.ai.core.theme.TextMetrics

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plantsense.ai.core.theme.spacing
import com.plantsense.ai.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.spacing
    // Animation states
    val scale = remember { Animatable(0.6f) }
    val alpha = remember { Animatable(0f) }
    val leafScale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Fade in entire content
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing)
            )
        }
        
        // Bouncy logo entry scale
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        
        // Bouncy leaf growth animation (blooms after scale starts)
        launch {
            delay(350)
            leafScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        
        // Exact 2-second splash screen duration
        delay(2000)
        onNavigateToHome()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Animated centered container
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .alpha(alpha.value)
                .scale(scale.value)
        ) {
            // Large circular gradient background behind logo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(Dimens.Dp170)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
            ) {
                // Centered Logo Circle
                Box(
                    modifier = Modifier
                        .size(Dimens.Dp90)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        )
                        .scale(leafScale.value),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Spa,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(Dimens.Dp52)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.Dp28))

            // App Name Title
            Text(
                text = stringResource(R.string.app_name),
                fontSize = TextMetrics.Sp32,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = TextMetrics.TrackingNegativeHalf
            )

            Spacer(modifier = Modifier.height(Dimens.Dp6))

            // Subtitle
            Text(
                text = stringResource(R.string.splash_subtitle),
                fontSize = TextMetrics.Sp14,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }

        // Animated footer text (Powered by Gemini AI)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = Dimens.Dp36)
                .alpha(alpha.value)
        ) {
            Text(
                text = stringResource(R.string.powered_by_gemini).uppercase(),
                fontSize = TextMetrics.Sp11,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = TextMetrics.Tracking2
            )
        }
    }
}
