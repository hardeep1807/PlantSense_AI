package com.plantsense.ai.presentation.common

import com.plantsense.ai.core.theme.Dimens
import com.plantsense.ai.core.theme.TextMetrics

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plantsense.ai.core.theme.spacing
import com.plantsense.ai.R
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.delay

import androidx.compose.foundation.isSystemInDarkTheme

@Composable
fun PremiumLoadingScreen(
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.spacing
    // Sequenced checklist logic
    var activeStep by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        delay(600)
        activeStep = 1
        delay(1000)
        activeStep = 2
        delay(1000)
        activeStep = 3
        delay(1000)
        activeStep = 4
    }

    // Infinite pulse animations for plant icon
    val infiniteTransition = rememberInfiniteTransition(label = "icon_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val glowRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glow_rotation"
    )

    // Infinite Shimmer Gradient translation
    val shimmerTransition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by shimmerTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val isDark = isSystemInDarkTheme()
    val shimmerColors = remember(isDark) {
        if (isDark) {
            listOf(
                Color.White.copy(alpha = 0.04f),
                Color.White.copy(alpha = 0.12f),
                Color.White.copy(alpha = 0.04f),
            )
        } else {
            listOf(
                Color.Black.copy(alpha = 0.03f),
                Color.Black.copy(alpha = 0.08f),
                Color.Black.copy(alpha = 0.03f),
            )
        }
    }

    val shimmerModifier = Modifier.drawWithContent {
        drawContent()
        val brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = translateAnim, y = translateAnim)
        )
        drawRect(brush = brush)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = Dimens.Dp24),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(Dimens.Dp24))

            // Pulse Plant Circle with outer gradient ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(Dimens.Dp150)
            ) {
                // Glow Ring
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationZ = glowRotation }
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Central pulsing icon
                Box(
                    modifier = Modifier
                        .size(Dimens.Dp80)
                        .graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                        }
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Spa,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(Dimens.Dp40)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.Dp24))

            Text(
                text = stringResource(R.string.analyzing_your_plant),
                fontSize = TextMetrics.Sp24,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Dimens.Dp24))

            // Steps Card
            Card(
                shape = RoundedCornerShape(Dimens.Dp24),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(Dimens.Dp1, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)),
                elevation = CardDefaults.cardElevation(defaultElevation = Dimens.Dp2),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(Dimens.Dp20),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Dp14)
                ) {
                    LoadingStepRow(
                        label = stringResource(R.string.loading_identifying_species),
                        status = getStepStatus(0, activeStep)
                    )
                    LoadingStepRow(
                        label = stringResource(R.string.loading_detecting_diseases),
                        status = getStepStatus(1, activeStep)
                    )
                    LoadingStepRow(
                        label = stringResource(R.string.loading_evaluating_vigor),
                        status = getStepStatus(2, activeStep)
                    )
                    LoadingStepRow(
                        label = stringResource(R.string.loading_compiling_care),
                        status = getStepStatus(3, activeStep)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.Dp32))

            // Shimmer Result skeleton placeholder card
            Card(
                shape = RoundedCornerShape(Dimens.Dp24),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                border = BorderStroke(Dimens.Dp1, MaterialTheme.colorScheme.outline.copy(alpha = 0.04f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(0.6f)
            ) {
                Column(modifier = Modifier.padding(Dimens.Dp20)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(Dimens.Dp22)
                            .clip(RoundedCornerShape(Dimens.Dp8))
                            .then(shimmerModifier)
                    )
                    Spacer(modifier = Modifier.height(Dimens.Dp8))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.3f)
                            .height(Dimens.Dp14)
                            .clip(RoundedCornerShape(Dimens.Dp8))
                            .then(shimmerModifier)
                    )
                    Spacer(modifier = Modifier.height(Dimens.Dp24))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.Dp10)
                    ) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(Dimens.Dp72)
                                    .clip(RoundedCornerShape(Dimens.Dp14))
                                    .then(shimmerModifier)
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class StepStatus {
    PENDING,
    ACTIVE,
    COMPLETED
}

private fun getStepStatus(stepIndex: Int, activeStep: Int): StepStatus {
    return when {
        activeStep > stepIndex -> StepStatus.COMPLETED
        activeStep == stepIndex -> StepStatus.ACTIVE
        else -> StepStatus.PENDING
    }
}

@Composable
private fun LoadingStepRow(
    label: String,
    status: StepStatus
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (status == StepStatus.PENDING) 0.4f else 1.0f)
    ) {
        Box(
            modifier = Modifier.size(Dimens.Dp24),
            contentAlignment = Alignment.Center
        ) {
            when (status) {
                StepStatus.COMPLETED -> {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = stringResource(R.string.step_complete),
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier
                            .size(Dimens.Dp22)
                            .scale(1.1f)
                    )
                }
                StepStatus.ACTIVE -> {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = Dimens.Dp2,
                        modifier = Modifier.size(Dimens.Dp16)
                    )
                }
                StepStatus.PENDING -> {
                    Icon(
                        imageVector = Icons.Rounded.RadioButtonUnchecked,
                        contentDescription = stringResource(R.string.step_pending),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(Dimens.Dp18)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(Dimens.Dp12))
        
        Text(
            text = label,
            fontSize = TextMetrics.Sp14,
            fontWeight = if (status == StepStatus.ACTIVE) FontWeight.Bold else FontWeight.Medium,
            color = if (status == StepStatus.ACTIVE) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}
