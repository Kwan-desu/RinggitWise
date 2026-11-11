package com.ringgitwise.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

enum class AppThemeMode { DARK, LIGHT, LIGHT_GREY }

private val DarkColorScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onSurfaceDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
    surfaceBright = surfaceContainerHighestDark,
    surfaceDim = surfaceContainerLowestDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = Silver700
)

private val LightColorScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onSurfaceLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
    surfaceBright = surfaceContainerLowestLight,
    surfaceDim = surfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = Silver300
)

private val LightGreyColorScheme = lightColorScheme(
    primary = primaryLightGrey,
    onPrimary = onPrimaryLightGrey,
    primaryContainer = primaryContainerLightGrey,
    onPrimaryContainer = onPrimaryContainerLightGrey,
    secondary = secondaryLightGrey,
    onSecondary = onSecondaryLightGrey,
    secondaryContainer = secondaryContainerLightGrey,
    onSecondaryContainer = onSecondaryContainerLightGrey,
    tertiary = tertiaryLightGrey,
    onTertiary = onTertiaryLightGrey,
    tertiaryContainer = tertiaryContainerLightGrey,
    onTertiaryContainer = onTertiaryContainerLightGrey,
    error = errorLightGrey,
    onError = onErrorLightGrey,
    errorContainer = errorContainerLightGrey,
    onErrorContainer = onErrorContainerLightGrey,
    background = backgroundLightGrey,
    onBackground = onSurfaceLightGrey,
    surface = surfaceLightGrey,
    onSurface = onSurfaceLightGrey,
    surfaceVariant = surfaceVariantLightGrey,
    onSurfaceVariant = onSurfaceVariantLightGrey,
    surfaceContainerLowest = surfaceContainerLowestLightGrey,
    surfaceContainerLow = surfaceContainerLowLightGrey,
    surfaceContainer = surfaceContainerLightGrey,
    surfaceContainerHigh = surfaceContainerHighLightGrey,
    surfaceContainerHighest = surfaceContainerHighestLightGrey,
    surfaceBright = surfaceContainerHighestLightGrey,
    surfaceDim = surfaceVariantLightGrey,
    outline = outlineLightGrey,
    outlineVariant = outlineVariantLightGrey,
    inverseSurface = inverseSurfaceLightGrey,
    inverseOnSurface = inverseOnSurfaceLightGrey,
    inversePrimary = Silver300
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/**
 * Obsidian monochrome dark theme with Light and Light Grey alternatives.
 */
@Composable
fun RinggitWiseTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val scheme = when (themeMode) {
        AppThemeMode.DARK -> DarkColorScheme
        AppThemeMode.LIGHT -> LightColorScheme
        AppThemeMode.LIGHT_GREY -> LightGreyColorScheme
    }
    MaterialTheme(
        colorScheme = scheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
