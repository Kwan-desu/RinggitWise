package com.ringgitwise.app.ui.theme

import androidx.compose.ui.graphics.Color

// -------------------------------------------------------------
// RinggitWise "Obsidian Monochrome" palette
// True OLED black & neutral charcoal surfaces + crisp monochrome accent.
// Text pairs are chosen to keep >= 4.5:1 contrast on their surfaces.
// -------------------------------------------------------------

// Accent
val AccentWhite = Color(0xFFFAFAFA)
val Silver300 = Color(0xFFD4D4D8)
val Silver700 = Color(0xFF3F3F46)
val Obsidian900 = Color(0xFF18181B)
val ObsidianBlack = Color(0xFF09090B)

// Semantic
val Danger = Color(0xFFF87171)
val DangerStrong = Color(0xFFDC2626)
val Warning = Color(0xFFFBBF24)
val Info = Color(0xFF38BDF8)

// ---------- Dark ----------
val primaryDark = AccentWhite
val onPrimaryDark = ObsidianBlack
val primaryContainerDark = Color(0xFF27272A)
val onPrimaryContainerDark = Color(0xFFFAFAFA)

val secondaryDark = Color(0xFFA1A1AA)
val onSecondaryDark = ObsidianBlack
val secondaryContainerDark = Color(0xFF242429)
val onSecondaryContainerDark = Color(0xFFE4E4E7)

val tertiaryDark = Silver300
val onTertiaryDark = ObsidianBlack
val tertiaryContainerDark = Color(0xFF27272A)
val onTertiaryContainerDark = Color(0xFFE4E4E7)

val errorDark = Danger
val onErrorDark = Color(0xFF2B0606)
val errorContainerDark = Color(0xFF4C1212)
val onErrorContainerDark = Color(0xFFFECACA)

val backgroundDark = Color(0xFF000000)
val surfaceDark = Color(0xFF000000)
val onSurfaceDark = Color(0xFFFAFAFA)
val surfaceVariantDark = Color(0xFF242429)
val onSurfaceVariantDark = Color(0xFFA1A1AA)
val surfaceContainerLowestDark = Color(0xFF050506)
val surfaceContainerLowDark = Color(0xFF0A0A0C)
val surfaceContainerDark = Color(0xFF111114)
val surfaceContainerHighDark = Color(0xFF18181B)
val surfaceContainerHighestDark = Color(0xFF242429)
val outlineDark = Color(0xFF3F3F46)
val outlineVariantDark = Color(0xFF27272A)
val inverseSurfaceDark = Color(0xFFFAFAFA)
val inverseOnSurfaceDark = Color(0xFF000000)

// ---------- Light ----------
val primaryLight = Obsidian900
val onPrimaryLight = Color(0xFFFFFFFF)
val primaryContainerLight = Color(0xFFE4E4E7)
val onPrimaryContainerLight = ObsidianBlack

val secondaryLight = Color(0xFF52525B)
val onSecondaryLight = Color(0xFFFFFFFF)
val secondaryContainerLight = Color(0xFFE4E4E7)
val onSecondaryContainerLight = ObsidianBlack

val tertiaryLight = Silver700
val onTertiaryLight = Color(0xFFFFFFFF)
val tertiaryContainerLight = Color(0xFFF4F4F5)
val onTertiaryContainerLight = Obsidian900

val errorLight = DangerStrong
val onErrorLight = Color(0xFFFFFFFF)
val errorContainerLight = Color(0xFFFEE2E2)
val onErrorContainerLight = Color(0xFF7F1D1D)

val backgroundLight = Color(0xFFFAFAFA)
val surfaceLight = Color(0xFFFAFAFA)
val onSurfaceLight = ObsidianBlack
val surfaceVariantLight = Color(0xFFE4E4E7)
val onSurfaceVariantLight = Color(0xFF52525B)
val surfaceContainerLowestLight = Color(0xFFFFFFFF)
val surfaceContainerLowLight = Color(0xFFFFFFFF)
val surfaceContainerLight = Color(0xFFFFFFFF)
val surfaceContainerHighLight = Color(0xFFFFFFFF)
val surfaceContainerHighestLight = Color(0xFFF4F4F5)
val outlineLight = Color(0xFFD4D4D8)
val outlineVariantLight = Color(0xFFE4E4E7)
val inverseSurfaceLight = Obsidian900
val inverseOnSurfaceLight = Color(0xFFFAFAFA)
