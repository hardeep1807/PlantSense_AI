package com.plantsense.ai.presentation.identification

import com.plantsense.ai.core.theme.Dimens
import com.plantsense.ai.core.theme.TextMetrics

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.annotation.StringRes
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.plantsense.ai.R
import com.plantsense.ai.core.theme.spacing
import com.plantsense.ai.domain.model.PlantIdentificationResult
import com.plantsense.ai.presentation.common.PremiumLoadingScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdentificationRoute(
    imagePath: String,
    historyId: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: IdentificationViewModel = hiltViewModel()
) {
    val spacing = MaterialTheme.spacing
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = imagePath, key2 = historyId) {
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
                is IdentificationUiState.Loading -> {
                    PremiumLoadingScreen()
                }
                is IdentificationUiState.Success -> {
                    IdentificationContent(
                        imagePath = imagePath,
                        result = state.result,
                        onBack = onBack
                    )
                }
                is IdentificationUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(spacing.xxl),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(Dimens.Dp64)
                            )
                            Spacer(modifier = Modifier.height(spacing.lg))
                            Text(
                                text = state.message.asString(),
                                style = MaterialTheme.typography.titleMedium,
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
                                        onClick = { viewModel.identify(imagePath) },
                                        shape = RoundedCornerShape(spacing.lg),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier.height(Dimens.Dp48)
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
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun IdentificationContent(
    imagePath: String,
    result: PlantIdentificationResult,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showFullExplanation by remember { mutableStateOf(false) }
    val plantName = result.plantName.ifBlank { stringResource(R.string.unknown_plant) }
    val botanicalName = result.botanicalName.ifBlank { stringResource(R.string.unknown_value) }
    val description = result.description.ifBlank { stringResource(R.string.no_description_available) }
    val careLight = result.careLight.ifBlank { stringResource(R.string.unknown_sunlight_requirements) }
    val careWater = result.careWater.ifBlank { stringResource(R.string.unknown_water_requirements) }
    val careTemp = result.careTemp.ifBlank { stringResource(R.string.unknown_temperature_requirements) }

    val factsResource = remember(plantName) {
        when {
            plantName.contains("tomato", ignoreCase = true) -> R.array.tomato_facts
            plantName.contains("monstera", ignoreCase = true) -> R.array.monstera_facts
            else -> R.array.generic_plant_facts
        }
    }
    val interestingFacts = stringArrayResource(factsResource)

    val fullExplanation = stringResource(
        R.string.full_botanical_explanation,
        plantName,
        botanicalName,
        (result.confidence * 100).toInt(),
        careWater.lowercase(),
        careLight.lowercase(),
        careTemp.lowercase()
    )
    val shareSubject = stringResource(R.string.share_report_subject, plantName)
    val shareBody = stringResource(
        R.string.share_report_body,
        plantName,
        botanicalName,
        (result.confidence * 100).toInt(),
        description,
        careWater,
        careLight,
        careTemp
    )
    val shareChooserTitle = stringResource(R.string.share_report_chooser)
    val savedMessage = stringResource(R.string.report_saved)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Sticky Bottom Actions Row
            Surface(
                shape = RoundedCornerShape(topStart = Dimens.Dp28, topEnd = Dimens.Dp28),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = Dimens.Dp8,
                shadowElevation = Dimens.Dp8,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(Dimens.Dp16)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.Dp10)
                ) {
                    // Share Intent
                    OutlinedButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, shareSubject)
                                putExtra(Intent.EXTRA_TEXT, shareBody)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, shareChooserTitle))
                        },
                        shape = RoundedCornerShape(Dimens.Dp16),
                        modifier = Modifier
                            .weight(1f)
                            .height(Dimens.Dp48)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = null,
                            modifier = Modifier.size(Dimens.Dp16)
                        )
                        Spacer(modifier = Modifier.width(Dimens.Dp6))
                        Text(stringResource(R.string.share), fontWeight = FontWeight.Bold, fontSize = TextMetrics.Sp13)
                    }
                    
                    // Save Confirmation
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(savedMessage)
                            }
                        },
                        shape = RoundedCornerShape(Dimens.Dp16),
                        modifier = Modifier
                            .weight(1f)
                            .height(Dimens.Dp48)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.BookmarkAdd,
                            contentDescription = null,
                            modifier = Modifier.size(Dimens.Dp16)
                        )
                        Spacer(modifier = Modifier.width(Dimens.Dp6))
                        Text(stringResource(R.string.save), fontWeight = FontWeight.Bold, fontSize = TextMetrics.Sp13)
                    }

                    // Scan Again Button
                    Button(
                        onClick = onBack,
                        shape = RoundedCornerShape(Dimens.Dp16),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(Dimens.Dp48)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.QrCodeScanner,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(Dimens.Dp16)
                        )
                        Spacer(modifier = Modifier.width(Dimens.Dp6))
                        Text(stringResource(R.string.scan_again), fontWeight = FontWeight.Bold, color = Color.White, fontSize = TextMetrics.Sp13)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Header image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.Dp300)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(File(imagePath))
                        .crossfade(true)
                        .build(),
                    contentDescription = plantName,
                    contentScale = ContentScale.Crop,
                    placeholder = rememberVectorPainter(Icons.Rounded.Spa),
                    error = rememberVectorPainter(Icons.Rounded.BrokenImage),
                    modifier = Modifier.fillMaxSize()
                )
                
                // Overlay vignette
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                                startY = 180f
                            )
                        )
                )

                // Back Button
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
                    .padding(Dimens.Dp20)
                    .fillMaxWidth()
            ) {
                // Header details: Name, Scientific Name, Circular Confidence Progress
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(Dimens.Dp6))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                    .padding(horizontal = Dimens.Dp6, vertical = Dimens.Dp2)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(Dimens.Dp10)
                                    )
                                    Spacer(modifier = Modifier.width(Dimens.Dp3))
                                    Text(
                                        text = stringResource(R.string.verified_species).uppercase(),
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = TextMetrics.Sp8,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = TextMetrics.TrackingHalf
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(Dimens.Dp6))

                        Text(
                            text = plantName,
                            fontSize = TextMetrics.Sp28,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = TextMetrics.Sp34
                        )
                        Text(
                            text = botanicalName,
                            fontSize = TextMetrics.Sp16,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            modifier = Modifier.padding(top = Dimens.Dp2)
                        )
                    }

                    Spacer(modifier = Modifier.width(Dimens.Dp16))

                    // Circular Confidence Gauge
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(Dimens.Dp60)
                    ) {
                        CircularProgressIndicator(
                            progress = { result.confidence.toFloat() },
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = Dimens.Dp5,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxSize()
                        )
                        Text(
                            text = stringResource(R.string.percent_value, (result.confidence * 100).toInt()),
                            fontSize = TextMetrics.Sp13,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.Dp24))

                // Card 1: About Card
                AnimatedResultCard(index = 0) {
                    Card(
                        shape = RoundedCornerShape(Dimens.Dp24),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(Dimens.Dp1, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.Dp2),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(Dimens.Dp20)) {
                            Text(
                                text = stringResource(R.string.about_species),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(Dimens.Dp8))
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                lineHeight = TextMetrics.Sp20
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.Dp14))

                // Card 2: Disease Analysis Status Check
                AnimatedResultCard(index = 1) {
                    Card(
                        shape = RoundedCornerShape(Dimens.Dp24),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(Dimens.Dp1, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.Dp2),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(Dimens.Dp20),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(Dimens.Dp38)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2E7D32).copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(Dimens.Dp20)
                                )
                            }
                            Spacer(modifier = Modifier.width(Dimens.Dp16))
                            Column {
                                Text(
                                    text = stringResource(R.string.disease_diagnostics_check),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF2E7D32)
                                )
                                Spacer(modifier = Modifier.height(Dimens.Dp4))
                                Text(
                                    text = stringResource(R.string.healthy_leaf_diagnostic),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    lineHeight = TextMetrics.Sp18
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.Dp14))

                // Card 3: Care Guide Card (Water, Light, Temp, Humidity, Fertilizer)
                AnimatedResultCard(index = 2) {
                    Card(
                        shape = RoundedCornerShape(Dimens.Dp24),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(Dimens.Dp1, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.Dp2),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(Dimens.Dp20)) {
                            Text(
                                text = stringResource(R.string.care_guide),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.height(Dimens.Dp16))

                            // Grid Row 1 (Water, Sunlight, Temperature)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Dimens.Dp8)
                            ) {
                                CareParamItem(
                                    icon = Icons.Rounded.WaterDrop,
                                    title = stringResource(R.string.water),
                                    value = getShortCareLabel(careWater, R.string.weekly),
                                    tint = Color(0xFF2196F3),
                                    modifier = Modifier.weight(1f)
                                )
                                CareParamItem(
                                    icon = Icons.Rounded.LightMode,
                                    title = stringResource(R.string.sunlight),
                                    value = getShortCareLabel(careLight, R.string.indirect_light),
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.weight(1f)
                                )
                                CareParamItem(
                                    icon = Icons.Rounded.Thermostat,
                                    title = stringResource(R.string.temperature),
                                    value = getShortCareLabel(careTemp, R.string.moderate),
                                    tint = Color(0xFFE57373),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(Dimens.Dp8))

                            // Grid Row 2 (Humidity, Fertilizer)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Dimens.Dp8)
                            ) {
                                CareParamItem(
                                    icon = Icons.Rounded.Air,
                                    title = stringResource(R.string.humidity),
                                    value = stringResource(R.string.moderate),
                                    tint = Color(0xFF00ACC1),
                                    modifier = Modifier.weight(1f)
                                )
                                CareParamItem(
                                    icon = Icons.Rounded.Grass,
                                    title = stringResource(R.string.fertilizer),
                                    value = stringResource(R.string.monthly),
                                    tint = Color(0xFF7CB342),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.Dp14))

                // Card 4: Interesting Facts
                if (interestingFacts.isNotEmpty()) {
                    AnimatedResultCard(index = 3) {
                        Card(
                            shape = RoundedCornerShape(Dimens.Dp24),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(Dimens.Dp1, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = Dimens.Dp2),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(Dimens.Dp20)) {
                                Text(
                                    text = stringResource(R.string.interesting_facts),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(Dimens.Dp12))
                                
                                interestingFacts.forEachIndexed { i, fact ->
                                    Row(
                                        modifier = Modifier.padding(vertical = Dimens.Dp4),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Eco,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier
                                                .size(Dimens.Dp16)
                                                .padding(top = Dimens.Dp2)
                                        )
                                        Spacer(modifier = Modifier.width(Dimens.Dp10))
                                        Text(
                                            text = fact,
                                            fontSize = TextMetrics.Sp13,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                            lineHeight = TextMetrics.Sp18
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(Dimens.Dp14))
                }

                // Card 5: Expandable AI Explanation Accordion Card
                AnimatedResultCard(index = 4) {
                    Card(
                        shape = RoundedCornerShape(Dimens.Dp24),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(Dimens.Dp1, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.Dp2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .clickable { showFullExplanation = !showFullExplanation }
                                .padding(Dimens.Dp20)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.full_botanical_explanation_title),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Icon(
                                    imageVector = if (showFullExplanation) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                    contentDescription = stringResource(
                                        if (showFullExplanation) R.string.collapse_explanation_cd
                                        else R.string.expand_explanation_cd
                                    ),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (showFullExplanation) {
                                Spacer(modifier = Modifier.height(Dimens.Dp12))
                                Text(
                                    text = fullExplanation,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    lineHeight = TextMetrics.Sp20
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(Dimens.Dp48))
            }
        }
    }
}

// Sequential cascading slide + fade animations
@Composable
private fun AnimatedResultCard(
    index: Int,
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        // Sequenced entry cascade delays
        delay(index * 100L)
        isVisible = true
    }
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(durationMillis = 400)) + 
                slideInVertically(
                    animationSpec = tween(durationMillis = 400, easing = EaseOutBack),
                    initialOffsetY = { it / 3 }
                ),
        exit = fadeOut(animationSpec = tween(durationMillis = 200)),
        modifier = Modifier.fillMaxWidth()
    ) {
        content()
    }
}

@Composable
private fun CareParamItem(
    icon: ImageVector,
    title: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(Dimens.Dp16),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(Dimens.Dp1, MaterialTheme.colorScheme.outline.copy(alpha = 0.05f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(Dimens.Dp10),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(Dimens.Dp32)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(Dimens.Dp16)
                )
            }
            Spacer(modifier = Modifier.height(Dimens.Dp6))
            Text(
                text = title,
                fontSize = TextMetrics.Sp10,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(Dimens.Dp2))
            Text(
                text = value,
                fontSize = TextMetrics.Sp11,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun getShortCareLabel(careText: String, @StringRes defaultRes: Int): String {
    val lower = careText.lowercase()
    return when {
        careText.isBlank() -> stringResource(defaultRes)
        lower.contains("twice a week") -> stringResource(R.string.twice_weekly)
        lower.contains("weekly") || lower.contains("once a week") -> stringResource(R.string.weekly)
        lower.contains("indirect") -> stringResource(R.string.indirect_light)
        lower.contains("direct") -> stringResource(R.string.direct_light)
        lower.contains("shade") -> stringResource(R.string.shade)
        lower.contains("dry") -> stringResource(R.string.dry_soil)
        lower.contains("moist") -> stringResource(R.string.moist_soil)
        else -> if (careText.length > 10) careText.take(8) + "…" else careText
    }
}
