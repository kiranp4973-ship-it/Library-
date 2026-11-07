package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ForbiddenLibraryColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = ObsidianBlack,
    primaryContainer = DeepArchiveBlue,
    onPrimaryContainer = GoldBright,
    secondary = AmberPrimary,
    onSecondary = ObsidianBlack,
    secondaryContainer = CosmicViolet,
    onSecondaryContainer = IvoryWhite,
    tertiary = VioletGlow,
    onTertiary = IvoryWhite,
    background = ObsidianBlack,
    onBackground = IvoryWhite,
    surface = VoidSurface,
    onSurface = IvoryWhite,
    surfaceVariant = GlassPanelElevated,
    onSurfaceVariant = ParchmentMuted,
    outline = BorderHairlineGold,
    outlineVariant = BorderHairlineSubtle,
    error = CrimsonCensored,
    onError = IvoryWhite
)

@Composable
fun ForbiddenLibraryTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ForbiddenLibraryColorScheme,
        typography = Typography,
        content = content
    )
}
