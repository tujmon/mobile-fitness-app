package com.hackerfit.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment

@Composable
fun StreakBadge(
    streakCount: Int,
    freezesBanked: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AssistChip(
            onClick = {},
            label = { Text("\ud83d\udd25 $streakCount dias") }
        )
        if (freezesBanked > 0) {
            AssistChip(
                onClick = {},
                label = { Text("\u2744\ufe0f $freezesBanked") }
            )
        }
    }
}
