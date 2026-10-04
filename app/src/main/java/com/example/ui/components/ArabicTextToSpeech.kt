package com.example.ui.components

import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun ArabicTtsPlayer(
    textToRead: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var ttsInstance by remember { mutableStateOf<TextToSpeech?>(null) }
    var isSpeaking by remember { mutableStateOf(false) }
    var isTtsReady by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Try Arabic locale first
                val result = ttsInstance?.setLanguage(Locale.forLanguageTag("ar"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    ttsInstance?.setLanguage(Locale.getDefault())
                }
                isTtsReady = true
            }
        }
        ttsInstance = tts

        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "قارئ النص العربي صوتياً",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            IconButton(
                onClick = {
                    if (isSpeaking) {
                        ttsInstance?.stop()
                        isSpeaking = false
                    } else {
                        if (!isTtsReady || ttsInstance == null) {
                            Toast.makeText(context, "محرك الصوت قيد التهيئة...", Toast.LENGTH_SHORT).show()
                            return@IconButton
                        }
                        // Clean markdown formatting characters for natural audio speech
                        val cleanSpeech = textToRead
                            .replace("#", "")
                            .replace("*", "")
                            .replace("|", " ")
                            .replace("-", "")
                            .replace("[كلمة غير واضحة]", "كلمة غير واضحة")
                            .take(4000)

                        ttsInstance?.speak(cleanSpeech, TextToSpeech.QUEUE_FLUSH, null, "doc_tts")
                        isSpeaking = true
                    }
                }
            ) {
                Icon(
                    imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isSpeaking) "إيقاف القراءة" else "تشغيل القراءة الصوتية",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
