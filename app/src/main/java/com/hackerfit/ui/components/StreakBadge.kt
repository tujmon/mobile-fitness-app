package com.hackerfit.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp

@Composable
fun StreakBadge(
    streakCount: Int,
    freezesBanked: Int
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (streakCount >= 7) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val colorProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (streakCount >= 7) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "colorProgress"
    )

    val baseTertiary = MaterialTheme.colorScheme.tertiary
    val basePrimary = MaterialTheme.colorScheme.primary
    val fireTint by animateColorAsState(
        targetValue = if (streakCount >= 7) {
            lerp(baseTertiary, basePrimary, colorProgress)
        } else {
            baseTertiary
        },
        animationSpec = tween(600),
        label = "fireTint"
    )

    Row(
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AssistChip(
            onClick = {},
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = fireTint,
                    modifier = Modifier.scale(scale)
                )
            },
            label = { Text("$streakCount ${if (streakCount == 1) "dia" else "dias"}") },
            colors = if (streakCount >= 7) {
                AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    labelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    leadingIconContentColor = MaterialTheme.colorScheme.tertiary
                )
            } else {
                AssistChipDefaults.assistChipColors()
            }
        )
        if (freezesBanked > 0) {
            AssistChip(
                onClick = {},
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.AcUnit,
                        contentDescription = null
                    )
                },
                label = { Text("$freezesBanked") }
            )
        }
    }
}

private fun lerp(start: androidx.compose.ui.graphics.Color, end: androidx.compose.ui.graphics.Color, fraction: Float): androidx.compose.ui.graphics.Color {
    return androidx.compose.ui.graphics.Color(
        red = start.red + (end.red - start.red) * fraction,
        green = start.green + (end.green - start.green) * fraction,
        blue = start.blue + (end.blue - start.blue) * fraction,
        alpha = start.alpha + (end.alpha - start.alpha) * fraction
    )
}
