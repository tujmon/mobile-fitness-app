package com.hackerfit.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RepCounter(
    currentReps: Int,
    targetReps: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    var lastReps by remember { mutableIntStateOf(currentReps) }
    val scale by animateFloatAsState(
        targetValue = if (currentReps != lastReps) 1.15f else 1f,
        animationSpec = tween(150),
        label = "scale"
    )

    LaunchedEffect(currentReps) {
        lastReps = currentReps
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Alvo: $targetReps",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            FilledTonalButton(
                onClick = onDecrement,
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(32.dp)
            ) {
                Text("-", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$currentReps",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .width(120.dp)
                        .scale(scale)
                )
                if (currentReps >= targetReps) {
                    Text(
                        text = "Meta batida!",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            FilledTonalButton(
                onClick = onIncrement,
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(32.dp)
            ) {
                Text("+", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
