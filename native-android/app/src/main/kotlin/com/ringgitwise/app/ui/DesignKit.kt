package com.ringgitwise.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.util.Locale

// -------------------------------------------------------------
// Design tokens
// -------------------------------------------------------------
object Space {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}

object Radius {
    val chip = 12.dp
    val card = 24.dp
    val hero = 28.dp
}

object IconSize {
    val sm = 16.dp
    val md = 20.dp
    val lg = 24.dp
}

fun formatRMWhole(amount: Double): String = String.format(Locale.US, "RM %,.0f", amount)
fun formatRMExact(amount: Double): String = String.format(Locale.US, "RM %,.2f", amount)

fun parseHex(hex: String, fallback: Color = Color(0xFF10B981)): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (_: Exception) { fallback }

fun goalIcon(key: String): ImageVector = when (key) {
    "phone" -> Icons.Default.PhoneAndroid
    "travel" -> Icons.Default.Flight
    "car" -> Icons.Default.DirectionsCar
    "home" -> Icons.Default.Home
    "school" -> Icons.Default.School
    "gift" -> Icons.Default.CardGiftcard
    "health" -> Icons.Default.FavoriteBorder
    "laptop" -> Icons.Default.Laptop
    "emergency" -> Icons.Default.Shield
    else -> Icons.Default.Savings
}

// -------------------------------------------------------------
// Surfaces
// -------------------------------------------------------------

@Composable
fun appCardBorder(): BorderStroke =
    glassCardBorder() ?: BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)

/** Standard rounded card used across the redesigned screens. */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    container: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentPadding: PaddingValues = PaddingValues(Space.lg),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(Radius.card)
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            colors = glassCardColors(container),
            border = appCardBorder()
        ) { Column(Modifier.padding(contentPadding), content = content) }
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            colors = glassCardColors(container),
            border = appCardBorder()
        ) { Column(Modifier.padding(contentPadding), content = content) }
    }
}

/** Tinted squircle holding an icon; decorative when placed beside text. */
@Composable
fun IconBadge(
    icon: ImageVector,
    tint: Color,
    size: Dp = 40.dp,
    iconSize: Dp = IconSize.md,
    contentDescription: String? = null
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.32f))
            .background(tint.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(iconSize))
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(top = Space.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction, contentPadding = PaddingValues(horizontal = Space.md)) {
                Text(actionLabel, style = MaterialTheme.typography.labelLarge)
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(IconSize.sm))
            }
        }
    }
}

/** Animated circular progress ring with centre slot. */
@Composable
fun ProgressRing(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    stroke: Dp = 8.dp,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    center: @Composable BoxScope.() -> Unit = {}
) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), tween(700), label = "ring")
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val s = stroke.toPx()
            drawArc(trackColor, -90f, 360f, false, style = Stroke(s, cap = StrokeCap.Round),
                topLeft = androidx.compose.ui.geometry.Offset(s / 2, s / 2),
                size = androidx.compose.ui.geometry.Size(this.size.width - s, this.size.height - s))
            if (animated > 0f) {
                drawArc(color, -90f, 360f * animated, false, style = Stroke(s, cap = StrokeCap.Round),
                    topLeft = androidx.compose.ui.geometry.Offset(s / 2, s / 2),
                    size = androidx.compose.ui.geometry.Size(this.size.width - s, this.size.height - s))
            }
        }
        center()
    }
}

/** Thin rounded linear progress bar. */
@Composable
fun SlimProgress(progress: Float, color: Color, modifier: Modifier = Modifier, height: Dp = 8.dp) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), tween(600), label = "bar")
    LinearProgressIndicator(
        progress = { animated },
        modifier = modifier.fillMaxWidth().height(height).clip(CircleShape),
        color = color,
        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        strokeCap = StrokeCap.Round,
        gapSize = 0.dp,
        drawStopIndicator = {}
    )
}

/** Sub-screen top bar with back navigation. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubScreenTopBar(
    title: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = glassSurfaceColor(MaterialTheme.colorScheme.background))
    )
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = Space.xxl, horizontal = Space.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Space.md)
    ) {
        IconBadge(icon, MaterialTheme.colorScheme.primary, size = 64.dp, iconSize = 30.dp)
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Button(onClick = onAction, shape = CircleShape, modifier = Modifier.heightIn(min = 48.dp)) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(IconSize.md))
                Spacer(Modifier.width(Space.sm))
                Text(actionLabel)
            }
        }
    }
}

/** A tappable settings-style row: icon badge, title, subtitle, trailing slot. */
@Composable
fun ListRow(
    icon: ImageVector,
    tint: Color,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = {
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
) {
    val base = Modifier.fillMaxWidth().heightIn(min = 56.dp)
    val rowModifier = if (onClick != null) {
        base.clip(RoundedCornerShape(Radius.chip)).then(
            Modifier.clickableRole(onClick)
        )
    } else base
    Row(
        modifier = rowModifier.padding(vertical = Space.sm, horizontal = Space.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.md)
    ) {
        IconBadge(icon, tint)
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        trailing?.invoke()
    }
}

private fun Modifier.clickableRole(onClick: () -> Unit): Modifier =
    this.clickable(role = Role.Button, onClick = onClick)
