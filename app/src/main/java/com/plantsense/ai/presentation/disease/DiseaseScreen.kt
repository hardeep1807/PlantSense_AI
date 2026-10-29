package com.plantsense.ai.presentation.disease

import com.plantsense.ai.core.theme.Dimens
import com.plantsense.ai.core.theme.TextMetrics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.plantsense.ai.R
import com.plantsense.ai.core.theme.spacing
import com.plantsense.ai.domain.model.DiseaseDetectionResult
import com.plantsense.ai.presentation.common.PremiumLoadingScreen
import java.io.File

@Composable
fun DiseaseRoute(
    imagePath: String,
    historyId: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DiseaseViewModel = hiltViewModel()
) {
    val spacing = MaterialTheme.spacing
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(imagePath, historyId) {
        viewModel.loadResult(historyId, imagePath)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when (val state = uiState) {
                is DiseaseUiState.Loading -> {
                    PremiumLoadingScreen()
                }
                is DiseaseUiState.Success -> {
                    DiseaseContent(
                        imagePath = imagePath,
                        result = state.result,
                        onBack = onBack
                    )
                }
                is DiseaseUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(spacing.xxl),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            stringResource(R.string.diagnostic_failed),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(spacing.md))
                        Text(
                            text = state.message.asString(),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(spacing.xxl))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(spacing.md)
                        ) {
                            OutlinedButton(
                                onClick = onBack,
                                shape = RoundedCornerShape(spacing.lg),
                                modifier = Modifier.height(spacing.minTouchTarget)
                            ) {
                                Text(stringResource(R.string.back), fontWeight = FontWeight.Bold)
                            }
                            
                            if (historyId == -1) {
                                Button(
                                    onClick = { viewModel.diagnose(imagePath) },
                                    shape = RoundedCornerShape(spacing.lg),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    ),
                                    modifier = Modifier.height(spacing.minTouchTarget)
                                ) {
                                    Text(stringResource(R.string.retry_diagnostic), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiseaseContent(
    imagePath: String,
    result: DiseaseDetectionResult,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Curved Full-bleed image header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.Dp320)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(File(imagePath))
                    .crossfade(true)
                    .build(),
                contentDescription = result.diseaseName ?: stringResource(R.string.analyzed_leaf_cd),
                contentScale = ContentScale.Crop,
                placeholder = rememberVectorPainter(Icons.Rounded.Spa),
                error = rememberVectorPainter(Icons.Rounded.BrokenImage),
                modifier = Modifier.fillMaxSize()
            )
            
            // Vignette overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                            startY = 200f
                        )
                    )
            )

            // Floating Circular Back Button
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(start = Dimens.Dp16, top = Dimens.Dp16)
                    .size(Dimens.Dp48)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.back_cd),
                    tint = Color.White,
                    modifier = Modifier.size(Dimens.Dp20)
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(Dimens.Dp24)
                .fillMaxWidth()
        ) {
            if (result.isHealthy) {
                // Healthy Banner badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Dimens.Dp8))
                        .background(Color(0xFFE8F5E9))
                        .padding(horizontal = Dimens.Dp10, vertical = Dimens.Dp4)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(Dimens.Dp14)
                        )
                        Spacer(modifier = Modifier.width(Dimens.Dp6))
                        Text(
                            text = stringResource(R.string.healthy).uppercase(),
                            color = Color(0xFF2E7D32),
                            fontSize = TextMetrics.Sp10,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = TextMetrics.Tracking1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.Dp10))

                Text(
                    text = stringResource(R.string.plant_is_healthy),
                    fontSize = TextMetrics.Sp32,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.no_disease_detected),
                    fontSize = TextMetrics.Sp15,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = Dimens.Dp4)
                )

                Spacer(modifier = Modifier.height(Dimens.Dp24))

                // Healthy suggestions custom Card
                Card(
                    shape = RoundedCornerShape(Dimens.Dp24),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(Dimens.Dp1, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = Dimens.Dp2)
                ) {
                    Column(modifier = Modifier.padding(Dimens.Dp24)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(Dimens.Dp36)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Spa,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(Dimens.Dp20)
                                )
                            }
                            Spacer(modifier = Modifier.width(Dimens.Dp12))
                            Text(
                                text = stringResource(R.string.care_suggestions),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(Dimens.Dp12))
                        
                        Text(
                            text = stringResource(R.string.healthy_care_suggestions),
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = TextMetrics.Sp22,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            } else {
                // Pathology detected warning badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Dimens.Dp8))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(horizontal = Dimens.Dp10, vertical = Dimens.Dp4)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(Dimens.Dp14)
                        )
                        Spacer(modifier = Modifier.width(Dimens.Dp6))
                        Text(
                            text = stringResource(
                                R.string.severity_label,
                                result.severity ?: stringResource(R.string.detected)
                            ).uppercase(),
                            color = MaterialTheme.colorScheme.error,
                            fontSize = TextMetrics.Sp10,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = TextMetrics.Tracking1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.Dp10))

                // Disease Header
                Text(
                    text = result.diseaseName ?: stringResource(R.string.leaf_pathology),
                    fontSize = TextMetrics.Sp32,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = TextMetrics.Sp38
                )
                Text(
                    text = stringResource(R.string.pathology_report_compiled),
                    fontSize = TextMetrics.Sp15,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = Dimens.Dp4)
                )

                Spacer(modifier = Modifier.height(Dimens.Dp24))

                // Detailed report sections (structured)
                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimens.Dp16),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Cause
                    result.cause?.let {
                        DetailReportCard(
                            title = stringResource(R.string.probable_cause), 
                            content = it,
                            tagColor = MaterialTheme.colorScheme.secondary
                        )
                    }

                    // Symptoms
                    result.symptoms?.let {
                        DetailReportCard(
                            title = stringResource(R.string.observed_symptoms), 
                            content = it,
                            tagColor = MaterialTheme.colorScheme.tertiary
                        )
                    }

                    // Treatment
                    result.treatment?.let {
                        DetailReportCard(
                            title = stringResource(R.string.recommended_treatment), 
                            content = it,
                            tagColor = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Prevention
                    result.prevention?.let {
                        DetailReportCard(
                            title = stringResource(R.string.prevention_measures), 
                            content = it,
                            tagColor = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.Dp32))

            // Action return to camera Button
            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.Dp52),
                shape = RoundedCornerShape(Dimens.Dp16),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (result.isHealthy) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = stringResource(R.string.back_to_camera),
                    fontWeight = FontWeight.Bold,
                    fontSize = TextMetrics.Sp15,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(Dimens.Dp24))
        }
    }
}

@Composable
fun DetailReportCard(title: String, content: String, tagColor: Color) {
    Card(
        shape = RoundedCornerShape(Dimens.Dp24),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(Dimens.Dp1, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.Dp2),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(Dimens.Dp20)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(Dimens.Dp8)
                        .clip(CircleShape)
                        .background(tagColor)
                )
                Spacer(modifier = Modifier.width(Dimens.Dp8))
                Text(
                    text = title.uppercase(),
                    fontSize = TextMetrics.Sp11,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = TextMetrics.Tracking1
                )
            }
            Spacer(modifier = Modifier.height(Dimens.Dp8))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = TextMetrics.Sp22,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}
