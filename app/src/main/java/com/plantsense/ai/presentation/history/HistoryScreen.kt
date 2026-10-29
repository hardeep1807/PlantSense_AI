package com.plantsense.ai.presentation.history

import com.plantsense.ai.core.theme.Dimens
import com.plantsense.ai.core.theme.TextMetrics

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.plantsense.ai.R
import com.plantsense.ai.core.theme.spacing
import com.plantsense.ai.domain.model.ScanType
import com.plantsense.ai.presentation.model.HistoryFilter
import com.plantsense.ai.presentation.model.ScanCardUiModel
import com.plantsense.ai.presentation.util.localizedDateTime
import java.io.File
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryRoute(
    onNavigateToHome: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToIdentify: (String, Int) -> Unit,
    onNavigateToDisease: (String, Int) -> Unit,
    onBack: () -> Unit,
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    HistoryScreen(
        state = state,
        onQueryChange = viewModel::updateSearchQuery,
        onFilterChange = viewModel::updateFilter,
        onFavoriteToggle = viewModel::toggleFavorite,
        onDelete = viewModel::deleteItem,
        onNavigateToHome = onNavigateToHome,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToIdentify = onNavigateToIdentify,
        onNavigateToDisease = onNavigateToDisease,
        onBack = onBack,
        bottomBar = bottomBar,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    state: HistoryUiState,
    onQueryChange: (String) -> Unit,
    onFilterChange: (HistoryFilter) -> Unit,
    onFavoriteToggle: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToIdentify: (String, Int) -> Unit,
    onNavigateToDisease: (String, Int) -> Unit,
    onBack: () -> Unit,
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.spacing
    var itemToDelete by remember { mutableStateOf<ScanCardUiModel?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.scan_history_title), fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack, 
                            contentDescription = stringResource(R.string.back_cd)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = bottomBar,
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Premium Search Bar
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                placeholder = { Text(stringResource(R.string.search_placeholder), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) },
                leadingIcon = { Icon(imageVector = Icons.Rounded.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.clear_search_cd)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(spacing.cardRadiusMd),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.xl, vertical = Dimens.Dp6)
            )

            // Horizontal Filter Chips Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                contentPadding = PaddingValues(horizontal = spacing.xl)
            ) {
                items(
                    items = HistoryFilter.entries,
                    key = { it.name }
                ) { filter ->
                    val isSelected = state.selectedFilter == filter
                    
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterChange(filter) },
                        label = { Text(text = stringResource(filter.labelRes), fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                            selectedBorderColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(Dimens.Dp12)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (state.items.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(Dimens.Dp24)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(Dimens.Dp110)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (state.selectedFilter == HistoryFilter.FAVORITES) Icons.Rounded.Favorite else Icons.Rounded.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(Dimens.Dp52)
                                )
                            }
                            Spacer(modifier = Modifier.height(Dimens.Dp20))
                            Text(
                                text = stringResource(state.selectedFilter.emptyTitleRes),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(Dimens.Dp8))
                            Text(
                                text = stringResource(state.selectedFilter.emptyDescriptionRes),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center,
                                lineHeight = TextMetrics.Sp18,
                                modifier = Modifier.width(Dimens.Dp280)
                            )
                            Spacer(modifier = Modifier.height(Dimens.Dp24))
                            Button(
                                onClick = onNavigateToHome,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(Dimens.Dp16),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = Dimens.Dp2)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(horizontal = Dimens.Dp8)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CameraAlt,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(Dimens.Dp18)
                                    )
                                    Spacer(modifier = Modifier.width(Dimens.Dp8))
                                    Text(
                                        text = stringResource(R.string.start_scanning),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Lazy List with Cascading Animation Wrapper
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = Dimens.Dp20, vertical = Dimens.Dp6),
                        verticalArrangement = Arrangement.spacedBy(Dimens.Dp12),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(items = state.items, key = { it.id }) { item ->
                            HistoryCard(
                                item = item,
                                isFavorited = item.id in state.favoriteIds,
                                onFavoriteToggle = { onFavoriteToggle(item.id) },
                                onClick = {
                                    if (item.type == ScanType.IDENTIFICATION) {
                                        onNavigateToIdentify(item.imagePath, item.id)
                                    } else {
                                        onNavigateToDisease(item.imagePath, item.id)
                                    }
                                },
                                onDelete = { itemToDelete = item },
                                modifier = Modifier.animateItem()
                            )
                        }
                        
                        // Bottom spacer padding for the bottom bar pill overlay
                        item {
                            Spacer(modifier = Modifier.height(Dimens.Dp90))
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text(stringResource(R.string.delete_history_title), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.delete_history_confirmation)) },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(item.id)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(Dimens.Dp12)
                ) {
                    Text(stringResource(R.string.delete_label), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text(stringResource(R.string.cancel), fontWeight = FontWeight.SemiBold)
                }
            },
            shape = RoundedCornerShape(Dimens.Dp24)
        )
    }
}

@Composable
fun HistoryCard(
    item: ScanCardUiModel,
    isFavorited: Boolean,
    onFavoriteToggle: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(Dimens.Dp24),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(Dimens.Dp1, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.Dp2),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.Dp14),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Large Crop Thumbnail
            Box(
                modifier = Modifier
                    .size(Dimens.Dp80)
                    .clip(RoundedCornerShape(Dimens.Dp16))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                AsyncImage(
                    model = File(item.imagePath),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(Dimens.Dp16))

            // Information Column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Header tags row (Type badge + Vigor/Confidence indicators)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.Dp6),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Dp4)
                ) {
                    val tagColor = if (item.type == ScanType.IDENTIFICATION) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                    val label = stringResource(
                        if (item.type == ScanType.IDENTIFICATION) R.string.species_label
                        else R.string.pathology_label
                    )
                    
                    // Type Tag
                    Box(
                        modifier = Modifier
                            .background(tagColor.copy(alpha = 0.12f), RoundedCornerShape(Dimens.Dp6))
                            .padding(horizontal = Dimens.Dp6, vertical = Dimens.Dp2)
                    ) {
                        Text(
                            text = label.uppercase(Locale.getDefault()),
                            color = tagColor,
                            fontSize = TextMetrics.Sp8,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = TextMetrics.TrackingHalf,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    // Confidence / Healthy Badges
                    if (item.type == ScanType.IDENTIFICATION) {
                        val percent = item.confidencePercent ?: 85
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), RoundedCornerShape(Dimens.Dp6))
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
                                    text = stringResource(R.string.confidence_match, percent).uppercase(Locale.getDefault()),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = TextMetrics.Sp8,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = TextMetrics.TrackingHalf,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    } else {
                        val isHealthy = item.isHealthy
                        val isDark = isSystemInDarkTheme()
                        val badgeBg = if (isHealthy) {
                            Color(0xFF2E7D32).copy(alpha = 0.1f)
                        } else {
                            if (isDark) Color(0xFFFFB300).copy(alpha = 0.15f) else Color(0xFFE65100).copy(alpha = 0.1f)
                        }
                        val badgeTint = if (isHealthy) {
                            if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
                        } else {
                            if (isDark) Color(0xFFFFD54F) else Color(0xFFD84315)
                        }
                        val badgeLabel = stringResource(
                            if (isHealthy) R.string.healthy_vigor else R.string.disease_alert
                        ).uppercase(Locale.getDefault())
                        val badgeIcon = if (isHealthy) Icons.Rounded.Spa else Icons.Rounded.Warning
                        
                        Box(
                            modifier = Modifier
                                .background(badgeBg, RoundedCornerShape(Dimens.Dp6))
                                .padding(horizontal = Dimens.Dp6, vertical = Dimens.Dp2)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = badgeIcon,
                                    contentDescription = null,
                                    tint = badgeTint,
                                    modifier = Modifier.size(Dimens.Dp10)
                                )
                                Spacer(modifier = Modifier.width(Dimens.Dp3))
                                Text(
                                    text = badgeLabel,
                                    color = badgeTint,
                                    fontSize = TextMetrics.Sp8,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = TextMetrics.TrackingHalf,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.Dp6))

                // Plant / Disease Title
                Text(
                    text = item.title.asString(),
                    fontSize = TextMetrics.Sp16,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(Dimens.Dp2))

                // Formatted Date
                Text(
                    text = localizedDateTime(item.timestamp),
                    fontSize = TextMetrics.Sp11,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(Dimens.Dp8))

            // Action Heart Toggle + Delete Icons + Navigation Chevron (48dp touch target compliant)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.Dp2)
            ) {
                // Favorites heart toggle
                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier.size(Dimens.Dp48)
                ) {
                    Icon(
                        imageVector = if (isFavorited) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = stringResource(
                            if (isFavorited) R.string.remove_favorite_cd else R.string.add_favorite_cd
                        ),
                        tint = if (isFavorited) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(Dimens.Dp22)
                    )
                }

                // Delete icon
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(Dimens.Dp48)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = stringResource(R.string.delete_from_history_cd),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.65f),
                        modifier = Modifier.size(Dimens.Dp20)
                    )
                }

                Spacer(modifier = Modifier.width(Dimens.Dp2))

                // Nav Chevron (decorative)
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    modifier = Modifier.size(Dimens.Dp18)
                )
            }
        }
    }
}
