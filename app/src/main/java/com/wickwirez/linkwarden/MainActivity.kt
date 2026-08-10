package com.wickwirez.linkwarden

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedUrl = extractUrlFromIntent(intent)

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
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
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { showPreview = false }) { Text("← Back") }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("JS", modifier = Modifier.padding(end = 4.dp))
                    Switch(checked = allowJs, onCheckedChange = { allowJs = it })
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

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("LinkWarden", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = urlInput,
            onValueChange = { urlInput = it },
            label = { Text("Paste a link") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                if (urlInput.isNotBlank()) {
                    isLoading = true
                    result = null
                    scope.launch {
                        val analysis = withContext(Dispatchers.IO) {
                            UrlAnalyzer.analyze(urlInput.trim())
                        }
                        result = analysis
                        isLoading = false
                    }
                }
            },
            enabled = urlInput.isNotBlank() && !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoading) "Checking..." else "Check link")
        }

        Spacer(Modifier.height(20.dp))

        result?.let { r ->
            val (verdictColor, verdictLabel) = when (r.verdict) {
                Verdict.SAFE -> Color(0xFF2ECC71) to "SAFE"
                Verdict.CAUTION -> Color(0xFFF1C40F) to "CAUTION"
                Verdict.DANGEROUS -> Color(0xFFE74C3C) to "DANGEROUS"
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(verdictLabel, color = verdictColor, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text("Final destination:", style = MaterialTheme.typography.labelMedium)
                    Text(r.finalUrl, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    r.reasons.forEach { reason ->
                        Text("• $reason", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(onClick = { showPreview = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Sandbox preview")
            }
        }
    }
}
