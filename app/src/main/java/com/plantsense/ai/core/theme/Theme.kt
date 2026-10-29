package com.plantsense.ai.core.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PSLightPrimary,
    onPrimary = PSLightOnPrimary,
    primaryContainer = PSLightPrimaryContainer,
    onPrimaryContainer = PSLightOnPrimaryContainer,
    secondary = PSLightSecondary,
    onSecondary = PSLightOnSecondary,
    secondaryContainer = PSLightSecondaryContainer,
    onSecondaryContainer = PSLightOnSecondaryContainer,
    tertiary = PSLightTertiary,
    onTertiary = PSLightOnTertiary,
    tertiaryContainer = PSLightTertiaryContainer,
    onTertiaryContainer = PSLightOnTertiaryContainer,
    error = PSLightError,
    onError = PSLightOnError,
    errorContainer = PSLightErrorContainer,
    onErrorContainer = PSLightOnErrorContainer,
    background = PSLightBackground,
    onBackground = PSLightOnBackground,
    surface = PSLightSurface,
    onSurface = PSLightOnSurface,
    surfaceVariant = PSLightSurfaceVariant,
    onSurfaceVariant = PSLightOnSurfaceVariant,
    outline = PSLightOutline,
    outlineVariant = PSLightOutline,
    scrim = Color.Black,
    inversePrimary = PSDarkPrimary,
    inverseSurface = PSDarkSurface,
    inverseOnSurface = PSDarkOnSurface,
    surfaceTint = PSLightPrimary
)

private val DarkColorScheme = darkColorScheme(
    primary = PSDarkPrimary,
    onPrimary = PSDarkOnPrimary,
    primaryContainer = PSDarkPrimaryContainer,
    onPrimaryContainer = PSDarkOnPrimaryContainer,
    secondary = PSDarkSecondary,
    onSecondary = PSDarkOnSecondary,
    secondaryContainer = PSDarkSecondaryContainer,
    onSecondaryContainer = PSDarkOnSecondaryContainer,
    tertiary = PSDarkTertiary,
    onTertiary = PSDarkOnTertiary,
    tertiaryContainer = PSDarkTertiaryContainer,
    onTertiaryContainer = PSDarkOnTertiaryContainer,
    error = PSDarkError,
    onError = PSDarkOnError,
    errorContainer = PSDarkErrorContainer,
    onErrorContainer = PSDarkOnErrorContainer,
    background = PSDarkBackground,
    onBackground = PSDarkOnBackground,
    surface = PSDarkSurface,
    onSurface = PSDarkOnSurface,
    surfaceVariant = PSDarkSurfaceVariant,
    onSurfaceVariant = PSDarkOnSurfaceVariant,
    outline = PSDarkOutline,
    outlineVariant = PSDarkOutline,
    scrim = Color.Black,
    inversePrimary = PSLightPrimary,
    inverseSurface = PSLightSurface,
    inverseOnSurface = PSLightOnSurface,
    surfaceTint = PSDarkPrimary
)

@Composable
fun PlantSenseAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to keep our curated green palette consistent!
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalSpacing provides Spacing()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
