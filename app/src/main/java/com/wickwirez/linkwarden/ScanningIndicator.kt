package com.wickwirez.linkwarden

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wickwirez.linkwarden.ui.theme.AccentCyan
import com.wickwirez.linkwarden.ui.theme.BorderSubtle

@Composable
fun ScanningIndicator(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "scan")
    val sweep by transition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )

    Canvas(modifier = modifier.fillMaxWidth().height(2.dp)) {
        drawLine(
            color = BorderSubtle,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = size.height
        )
        val center = sweep * size.width
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, AccentCyan, Color.Transparent),
                startX = center - 120f,
                endX = center + 120f
            )
        )
    }
}
