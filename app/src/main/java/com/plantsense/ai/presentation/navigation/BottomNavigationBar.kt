package com.plantsense.ai.presentation.navigation

import com.plantsense.ai.core.theme.Dimens
import com.plantsense.ai.core.theme.TextMetrics

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.plantsense.ai.R

@Composable
fun BottomNavigationBar(
    selectedRoute: AppRoute?,
    onNavigate: (AppRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = Dimens.Dp24, vertical = Dimens.Dp12),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(Dimens.Dp28),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            tonalElevation = Dimens.Dp8,
            shadowElevation = Dimens.Dp10,
            border = BorderStroke(
                width = Dimens.Dp1,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.Dp12, vertical = Dimens.Dp8),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Tab
                NavigationTabItem(
                    isSelected = selectedRoute == AppRoute.Home,
                    onClick = { onNavigate(AppRoute.Home) },
                    icon = Icons.Rounded.Home,
                    labelText = stringResource(R.string.home_label),
                    contentDescription = stringResource(R.string.home_cd)
                )

                // History Tab
                NavigationTabItem(
                    isSelected = selectedRoute == AppRoute.History,
                    onClick = { onNavigate(AppRoute.History) },
                    icon = Icons.Rounded.History,
                    labelText = stringResource(R.string.history_label),
                    contentDescription = stringResource(R.string.scan_history_cd)
                )

                // Settings Tab
                NavigationTabItem(
                    isSelected = selectedRoute == AppRoute.Profile,
                    onClick = { onNavigate(AppRoute.Profile) },
                    icon = Icons.Rounded.Settings,
                    labelText = stringResource(R.string.profile_label),
                    contentDescription = stringResource(R.string.settings_cd)
                )
            }
        }
    }
}

@Composable
private fun RowScope.NavigationTabItem(
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    labelText: String,
    contentDescription: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .weight(1f)
            .height(Dimens.Dp48)
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(Dimens.Dp24))
                .background(
                    if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        Color.Transparent
                    }
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null, // Disable default grey ripple for premium touch feel
                    onClick = onClick
                )
                .padding(horizontal = Dimens.Dp16, vertical = Dimens.Dp8)
                .animateContentSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                },
                modifier = Modifier.size(Dimens.Dp24)
            )
            
            if (isSelected) {
                Spacer(modifier = Modifier.width(Dimens.Dp8))
                Text(
                    text = labelText,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}
