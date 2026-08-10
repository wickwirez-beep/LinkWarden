package com.wickwirez.linkwarden

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.wickwirez.linkwarden.ui.theme.SignalCaution
import com.wickwirez.linkwarden.ui.theme.SignalDanger
import com.wickwirez.linkwarden.ui.theme.SignalSafe

@Composable
fun VerdictStamp(verdict: Verdict, modifier: Modifier = Modifier) {
    val (color, label) = when (verdict) {
        Verdict.SAFE -> SignalSafe to "◆ SAFE ◆"
        Verdict.CAUTION -> SignalCaution to "▲ CAUTION ▲"
        Verdict.DANGEROUS -> SignalDanger to "✕ DANGEROUS ✕"
    }

    Text(
        text = label,
        color = color,
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier
            .rotate(-2.5f)
            .border(BorderStroke(2.dp, color))
            .background(color.copy(alpha = 0.08f))
            .padding(horizontal = 20.dp, vertical = 10.dp)
    )
}
