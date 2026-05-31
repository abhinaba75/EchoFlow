package com.echoflow.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.echoflow.R
import com.echoflow.ui.theme.Spacing
import com.echoflow.ui.theme.rememberMorphProgress

/**
 * Shared Material 3 Expressive building blocks used across every screen. Centralising these is the
 * key to visual cohesion — the home, drawer, and settings all speak the same shape/containment
 * language instead of each reinventing rows, headers, and the brand mark.
 */

/**
 * Corner shape for an item at [index] within a group of [count] items, producing the "grouped,
 * connected container" look from Android 16 / M3 Expressive: the group reads as one rounded slab,
 * with only the outer corners rounded large.
 */
fun groupedItemShape(
    index: Int,
    count: Int,
    large: Dp = 22.dp,
    small: Dp = 6.dp,
): Shape {
    val top = if (index == 0) large else small
    val bottom = if (index == count - 1) large else small
    return RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom)
}

/** Section label in the expressive style — short, bold, primary-tinted, generous spacing. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(start = Spacing.base, bottom = Spacing.s, top = Spacing.xs),
    )
}

/**
 * The app's brand mark — the EchoFlow logo rendered as a tintable silhouette. It is tinted with the
 * [MaterialTheme.colorScheme.primary] role, so it adapts automatically to dark mode and to Material
 * You wallpaper-derived dynamic color. The monogram reads through as transparency. When [animated]
 * it gently breathes. Reused for the drawer header, empty-state hero, assistant avatar and model
 * rows so the identity is consistent everywhere.
 */
@Composable
fun BrandMark(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    animated: Boolean = false,
    @Suppress("UNUSED_PARAMETER") iconScale: Float = 0.5f,
) {
    val pulse = if (animated) {
        val p by rememberMorphProgress(1600)
        1f + 0.05f * p
    } else 1f
    Image(
        painter = painterResource(R.drawable.logo),
        contentDescription = null,
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
        modifier = modifier.size(size).scale(pulse),
    )
}

/** Vertical gap between tiles in a grouped section so they read as one connected slab. */
val GroupedItemGap = 3.dp
