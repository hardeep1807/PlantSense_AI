package com.plantsense.ai.presentation.home

import com.plantsense.ai.core.theme.Dimens
import com.plantsense.ai.core.theme.TextMetrics

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.plantsense.ai.R
import com.plantsense.ai.domain.model.ScanType
import androidx.compose.ui.platform.LocalContext
import com.plantsense.ai.core.theme.spacing
import com.plantsense.ai.presentation.model.ScanCardUiModel
import java.io.File
import java.util.*
import androidx.compose.ui.res.pluralStringResource
import com.plantsense.ai.presentation.util.localizedDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeRoute(
    onNavigateToCamera: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToIdentify: (String, Int) -> Unit,
    onNavigateToDisease: (String, Int) -> Unit,
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isCopying by viewModel.isCopying.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HomeEffect.NavigateToIdentify -> {
                    onNavigateToIdentify(effect.imagePath, -1)
                }
                is HomeEffect.NavigateToDisease -> {
                    onNavigateToDisease(effect.imagePath, -1)
                }
                is HomeEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message.asString(context))
                }
            }
        }
    }

    HomeScreen(
        uiState = uiState,
        isCopying = isCopying,
        onEvent = viewModel::onEvent,
        onNavigateToCamera = onNavigateToCamera,
        onNavigateToHistory = onNavigateToHistory,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToIdentify = onNavigateToIdentify,
        onNavigateToDisease = onNavigateToDisease,
        bottomBar = bottomBar,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    isCopying: Boolean,
    onEvent: (HomeEvent) -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToIdentify: (String, Int) -> Unit,
    onNavigateToDisease: (String, Int) -> Unit,
    bottomBar: @Composable () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.spacing
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showAnalysisDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            showAnalysisDialog = true
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = bottomBar,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .statusBarsPadding()
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(spacing.lg))
            
            // Greeting & Title Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.xl),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = getGreeting().uppercase(Locale.getDefault()),
                        fontSize = TextMetrics.Sp11,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = TextMetrics.TrackingOneAndHalf
                    )
                    Text(
                        text = stringResource(R.string.app_name),
                        fontSize = TextMetrics.Sp30,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(R.string.home_tagline),
                        fontSize = TextMetrics.Sp12,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = Dimens.Dp1)
                    )
                }
                
                // Profile Avatar Icon
                IconButton(
                    onClick = onNavigateToProfile,
                    modifier = Modifier
                        .size(Dimens.Dp48)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Spa,
                        contentDescription = stringResource(R.string.open_profile_settings_cd),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimens.Dp24)
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.xl))

            // Premium Hero Gradient Card
            Surface(
                shape = RoundedCornerShape(spacing.cardRadiusLg),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.xl)
                    .height(Dimens.Dp200),
                shadowElevation = Dimens.Dp8,
                tonalElevation = Dimens.Dp2
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        )
                ) {
                    // Decorative sweep curves inside card background
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = Dimens.Dp20, start = Dimens.Dp160)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Dimens.Dp24),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(Dimens.Dp8))
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .padding(horizontal = Dimens.Dp8, vertical = Dimens.Dp4)
                            ) {
                                Text(
                                    text = stringResource(R.string.botanical_scanner).uppercase(Locale.getDefault()),
                                    fontSize = TextMetrics.Sp9,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = TextMetrics.Tracking1
                                )
                            }
                            Spacer(modifier = Modifier.height(Dimens.Dp8))
                            Text(
                                text = stringResource(R.string.analyze_plant_vigor),
                                color = Color.White,
                                fontSize = TextMetrics.Sp22,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = stringResource(R.string.hero_scan_description),
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = TextMetrics.Sp12,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Hero controls: Large Scan button + Gallery button + Camera icon
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Dimens.Dp10)
                        ) {
                            // Large Scan Plant Button
                            Button(
                                onClick = onNavigateToCamera,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(Dimens.Dp16),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = Dimens.Dp2),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(Dimens.Dp46)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Camera,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(Dimens.Dp18)
                                    )
                                    Spacer(modifier = Modifier.width(Dimens.Dp8))
                                    Text(
                                        text = stringResource(R.string.scan_plant),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = TextMetrics.Sp14
                                    )
                                }
                            }

                            // Gallery icon button trigger
                            IconButton(
                                onClick = {
                                    imagePickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier
                                    .size(Dimens.Dp48)
                                    .clip(RoundedCornerShape(Dimens.Dp16))
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PhotoLibrary,
                                    contentDescription = stringResource(R.string.upload_gallery_cd),
                                    tint = Color.White,
                                    modifier = Modifier.size(Dimens.Dp20)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.Dp20))

            // Resolve real-time statistics
            val totalScansVal: String
            val vigorScoreVal: String
            val vigorRankVal: String
            
            when (val state = uiState) {
                is HomeUiState.Success -> {
                    totalScansVal = state.totalScans.toString()
                    vigorScoreVal = stringResource(R.string.percent_value, state.vigorScore)
                    vigorRankVal = stringResource(state.vigorRank.labelRes)
                }
                is HomeUiState.Empty -> {
                    totalScansVal = 0.toString()
                    vigorScoreVal = stringResource(R.string.percent_value, 0)
                    vigorRankVal = stringResource(R.string.rank_novice)
                }
                is HomeUiState.Loading -> {
                    totalScansVal = stringResource(R.string.loading_placeholder)
                    vigorScoreVal = stringResource(R.string.loading_placeholder)
                    vigorRankVal = stringResource(R.string.loading_placeholder)
                }
            }

            // Statistics Row Section (Premium Stats)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.Dp20),
                horizontalArrangement = Arrangement.spacedBy(Dimens.Dp10)
            ) {
                StatCard(
                    title = stringResource(R.string.vigor_score_label).uppercase(Locale.getDefault()),
                    value = vigorScoreVal,
                    icon = Icons.Rounded.Spa,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = stringResource(R.string.total_scans_label).uppercase(Locale.getDefault()),
                    value = totalScansVal,
                    icon = Icons.Rounded.History,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = stringResource(R.string.ai_vigor_rank_label).uppercase(Locale.getDefault()),
                    value = vigorRankVal,
                    icon = Icons.Rounded.Spa,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(Dimens.Dp20))

            // Today's Tip Card Section
            Card(
                shape = RoundedCornerShape(Dimens.Dp24),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                ),
                border = BorderStroke(Dimens.Dp1, MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.Dp20)
                    .padding(bottom = Dimens.Dp20) // Offset for the floating bottom navigation bar
            ) {
                Row(
                    modifier = Modifier.padding(Dimens.Dp20),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(Dimens.Dp40)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Spa,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(Dimens.Dp22)
                        )
                    }
                    Spacer(modifier = Modifier.width(Dimens.Dp16))
                    Column {
                        Text(
                            text = stringResource(R.string.todays_care_advice),
                            fontSize = TextMetrics.Sp13,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(Dimens.Dp2))
                        Text(
                            text = stringResource(R.string.todays_care_advice_body),
                            fontSize = TextMetrics.Sp12,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            lineHeight = TextMetrics.Sp16
                        )
                    }
                }
            }


            // Recent Scans Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.Dp20),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.recent_scans_label),
                    fontSize = TextMetrics.Sp18,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = onNavigateToHistory) {
                    Text(
                        text = stringResource(R.string.see_all),
                        fontSize = TextMetrics.Sp13,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.Dp10))

            // Horizontal Recent Scans List
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.Dp180),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is HomeUiState.Empty -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.Dp20)
                            .height(Dimens.Dp140)
                            .clip(RoundedCornerShape(Dimens.Dp24))
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Rounded.Spa,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                                modifier = Modifier.size(Dimens.Dp48)
                            )
                            Spacer(modifier = Modifier.height(Dimens.Dp8))
                            Text(
                                text = stringResource(R.string.no_scans_found),
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                                fontSize = TextMetrics.Sp13,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                is HomeUiState.Success -> {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.Dp14),
                        contentPadding = PaddingValues(horizontal = Dimens.Dp20),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(state.scans, key = { it.id }) { item ->
                            HorizontalRecentScanItem(
                                item = item,
                                onClick = {
                                    if (item.type == ScanType.IDENTIFICATION) {
                                        onNavigateToIdentify(item.imagePath, item.id)
                                    } else {
                                        onNavigateToDisease(item.imagePath, item.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.Dp20))

        }
    }

    // Analysis Type Dialog for Picked Images
    if (showAnalysisDialog) {
        Dialog(onDismissRequest = { if (!isCopying) showAnalysisDialog = false }) {
            Card(
                shape = RoundedCornerShape(Dimens.Dp24),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(Dimens.Dp1, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.Dp16),
                elevation = CardDefaults.cardElevation(defaultElevation = Dimens.Dp8)
            ) {
                Column(
                    modifier = Modifier.padding(Dimens.Dp24),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(Dimens.Dp56)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Spa,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(Dimens.Dp28)
                        )
                    }
                    Spacer(modifier = Modifier.height(Dimens.Dp16))
                    Text(
                        text = stringResource(R.string.select_analysis_type),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(Dimens.Dp8))
                    Text(
                        text = stringResource(R.string.choose_how_to_analyze),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(Dimens.Dp24))

                    if (isCopying) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = Dimens.Dp8)
                        )
                    } else {
                        // Identify option
                        Button(
                            onClick = {
                                selectedImageUri?.let { uri ->
                                    onEvent(HomeEvent.ImagePicked(uri.toString()))
                                    showAnalysisDialog = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Dimens.Dp48),
                            shape = RoundedCornerShape(Dimens.Dp16)
                        ) {
                            Text(stringResource(R.string.identify_plant), fontWeight = FontWeight.Bold, fontSize = TextMetrics.Sp15)
                        }
                        Spacer(modifier = Modifier.height(Dimens.Dp8))

                        // Diagnose Option
                        Button(
                            onClick = {
                                selectedImageUri?.let { uri ->
                                    onEvent(HomeEvent.DiseaseImagePicked(uri.toString()))
                                    showAnalysisDialog = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Dimens.Dp48),
                            shape = RoundedCornerShape(Dimens.Dp16),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text(stringResource(R.string.diagnose_disease), fontWeight = FontWeight.Bold, fontSize = TextMetrics.Sp15)
                        }
                        Spacer(modifier = Modifier.height(Dimens.Dp12))

                        // Cancel
                        TextButton(onClick = { showAnalysisDialog = false }) {
                            Text(
                                stringResource(R.string.cancel),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(Dimens.Dp20),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(Dimens.Dp1, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.Dp2),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(Dimens.Dp14),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(Dimens.Dp30)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(Dimens.Dp16)
                )
            }
            Spacer(modifier = Modifier.height(Dimens.Dp10))
            Text(
                text = value,
                fontSize = TextMetrics.Sp18,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(Dimens.Dp1))
            Text(
                text = title,
                fontSize = TextMetrics.Sp9,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                letterSpacing = TextMetrics.TrackingHalf
            )
        }
    }
}

@Composable
private fun HorizontalRecentScanItem(
    item: ScanCardUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(Dimens.Dp24),
        border = BorderStroke(Dimens.Dp1, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.Dp3),
        modifier = modifier
            .width(Dimens.Dp150)
            .height(Dimens.Dp180)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Full crop background image
            AsyncImage(
                model = File(item.imagePath),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dark gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.82f)),
                            startY = 180f
                        )
                    )
            )

            // Overlaid texts
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimens.Dp14),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top tag chip
                val tagColor = if (item.type == ScanType.IDENTIFICATION) Color(0xFF4CAF50) else Color(0xFFFFB300)
                val label = stringResource(
                    if (item.type == ScanType.IDENTIFICATION) R.string.species_label
                    else R.string.pathology_label
                )
                
                Box(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(Dimens.Dp6))
                        .padding(horizontal = Dimens.Dp6, vertical = Dimens.Dp2)
                ) {
                    Text(
                        text = label.uppercase(Locale.getDefault()),
                        color = tagColor,
                        fontSize = TextMetrics.Sp8,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = TextMetrics.TrackingHalf
                    )
                }

                // Bottom title block
                Column {
                    Text(
                        text = item.title.asString(),
                        color = Color.White,
                        fontSize = TextMetrics.Sp14,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    
                    Text(
                        text = localizedDateTime(item.timestamp, dateStyle = java.text.DateFormat.SHORT, timeStyle = null),
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = TextMetrics.Sp11,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = Dimens.Dp1)
                    )
                }
            }
        }
    }
}

@Composable
private fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 0..11 -> stringResource(R.string.greeting_morning)
        in 12..16 -> stringResource(R.string.greeting_afternoon)
        else -> stringResource(R.string.greeting_evening)
    }
}
