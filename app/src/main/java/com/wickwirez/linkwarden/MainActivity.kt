package com.wickwirez.linkwarden

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wickwirez.linkwarden.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedUrl = extractUrlFromIntent(intent)
        setContent {
            LinkWardenTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = VoidBlack) {
                    LinkWardenScreen(initialUrl = sharedUrl)
                }
            }
        }
    }

    private fun extractUrlFromIntent(intent: Intent?): String {
        return when (intent?.action) {
            Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
            Intent.ACTION_VIEW -> intent.dataString ?: ""
            else -> ""
        }
    }
}

@Composable
fun LinkWardenScreen(initialUrl: String) {
    var urlInput by remember { mutableStateOf(initialUrl) }
    var isLoading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<AnalysisResult?>(null) }
    var showPreview by remember { mutableStateOf(false) }
    var allowJs by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (showPreview && result != null) {
        Column(Modifier.fillMaxSize().background(VoidBlack)) {
            Row(
                Modifier.fillMaxWidth()
                    .background(PanelDark)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { showPreview = false }) {
                    Text("‹ BACK", color = AccentCyan, style = MaterialTheme.typography.labelMedium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("JS", color = TextMuted, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(end = 8.dp))
                    Switch(
                        checked = allowJs,
                        onCheckedChange = { allowJs = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = AccentCyan)
                    )
                }
            }
            SandboxWebView(
                url = result!!.finalUrl,
                allowJs = allowJs,
                modifier = Modifier.weight(1f).fillMaxWidth()
            )
        }
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(VoidBlack)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(8.dp)
                    .background(if (isLoading) SignalCaution else SignalSafe, shape = CircleShape)
            )
            Spacer(Modifier.width(8.dp))
            Text("LINKWARDEN", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
        }
        Text(
            "// SANDBOX SCANNER",
            style = MaterialTheme.typography.labelMedium,
            color = TextMuted,
            modifier = Modifier.padding(start = 16.dp)
        )

        Spacer(Modifier.height(28.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("❯", color = AccentCyan, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(end = 8.dp))
            TextField(
                value = urlInput,
                onValueChange = { urlInput = it },
                placeholder = { Text("paste a link to scan", color = TextMuted) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = AccentCyan,
                    unfocusedIndicatorColor = BorderSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = AccentCyan
                ),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, fontFamily = MonoFamily),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                if (urlInput.isNotBlank()) {
                    isLoading = true
                    result = null
                    scope.launch {
                        val analysis = withContext(Dispatchers.IO) { UrlAnalyzer.analyze(urlInput.trim()) }
                        result = analysis
                        isLoading = false
                    }
                }
            },
            enabled = urlInput.isNotBlank() && !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = VoidBlack),
            shape = RoundedCornerShape(2.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text(
                if (isLoading) "SCANNING..." else "▶ RUN SCAN",
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp),
                fontWeight = FontWeight.Bold
            )
        }

        AnimatedVisibility(visible = isLoading) {
            Column(Modifier.padding(top = 16.dp)) {
                ScanningIndicator(modifier = Modifier.padding(bottom = 6.dp))
                Text("resolving redirects · scoring heuristics", color = TextMuted, style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(Modifier.height(24.dp))

        result?.let { r ->
            Column {
                VerdictStamp(verdict = r.verdict, modifier = Modifier.align(Alignment.CenterHorizontally))

                Spacer(Modifier.height(20.dp))

                Text("TRACE", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                Spacer(Modifier.height(6.dp))
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(PanelDark)
                        .padding(12.dp)
                ) {
                    r.hops.forEachIndexed { i, hop ->
                        Text(
                            "$i → $hop",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = MonoFamily),
                            color = if (i == r.hops.lastIndex) AccentCyan else TextMuted
                        )
                        if (i != r.hops.lastIndex) Spacer(Modifier.height(4.dp))
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text("FINDINGS", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                Spacer(Modifier.height(6.dp))
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(PanelDark)
                        .padding(12.dp)
                ) {
                    r.reasons.forEach { reason ->
                        Text("· $reason", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                        Spacer(Modifier.height(4.dp))
                    }
                }

                Spacer(Modifier.height(20.dp))

                OutlinedButton(
                    onClick = { showPreview = true },
                    border = BorderStroke(1.dp, AccentCyan),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("❯ open --sandbox", style = MaterialTheme.typography.bodyMedium.copy(fontFamily = MonoFamily))
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Text(
            "built by wickwirez",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 12.dp)
        )
    }
}
