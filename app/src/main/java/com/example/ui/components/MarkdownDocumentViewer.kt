package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun MarkdownDocumentViewer(
    markdownText: String,
    modifier: Modifier = Modifier
) {
    val lines = markdownText.lines()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        var tableBuffer = mutableListOf<String>()

        for (line in lines) {
            val trimmed = line.trim()

            // Check if line belongs to a markdown table
            if (trimmed.startsWith("|") && trimmed.endsWith("|")) {
                tableBuffer.add(trimmed)
                continue
            } else {
                if (tableBuffer.isNotEmpty()) {
                    MarkdownTableComponent(tableBuffer)
                    tableBuffer = mutableListOf()
                }
            }

            when {
                trimmed.startsWith("# ") -> {
                    val title = trimmed.removePrefix("# ").trim()
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.5.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(QalamGold)
                        )
                    }
                }
                trimmed.startsWith("## ") -> {
                    val subtitle = trimmed.removePrefix("## ").trim()
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        ),
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 2.dp)
                    )
                }
                trimmed.startsWith("### ") -> {
                    val sub3 = trimmed.removePrefix("### ").trim()
                    Text(
                        text = sub3,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                    )
                }
                trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("• ") -> {
                    val content = trimmed.substring(2).trim()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 4.dp, top = 2.dp, bottom = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .size(7.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(QalamGold)
                        )
                        FormattedInlineText(
                            rawText = content,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                trimmed.matches("^\\d+\\.\\s+.*".toRegex()) -> {
                    val number = trimmed.substringBefore(".")
                    val content = trimmed.replaceFirst("^\\d+\\.\\s+".toRegex(), "")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 4.dp, top = 2.dp, bottom = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = number,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        FormattedInlineText(
                            rawText = content,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                trimmed.isEmpty() -> {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                else -> {
                    FormattedInlineText(
                        rawText = trimmed,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        if (tableBuffer.isNotEmpty()) {
            MarkdownTableComponent(tableBuffer)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FormattedInlineText(
    rawText: String,
    modifier: Modifier = Modifier
) {
    val illegibleTag = "[كلمة غير واضحة]"

    // If text contains the illegible marker, split and highlight
    if (rawText.contains(illegibleTag)) {
        val parts = rawText.split(illegibleTag)
        Column(modifier = modifier) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (i in parts.indices) {
                    if (parts[i].isNotEmpty()) {
                        Text(
                            text = parseBoldMarkdown(parts[i]),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = 24.sp,
                                textAlign = TextAlign.Right
                            )
                        )
                    }
                    if (i < parts.size - 1) {
                        IllegibleBadge()
                    }
                }
            }
        }
    } else {
        Text(
            text = parseBoldMarkdown(rawText),
            style = MaterialTheme.typography.bodyLarge.copy(
                lineHeight = 24.sp,
                textAlign = TextAlign.Right
            ),
            modifier = modifier
        )
    }
}

@Composable
fun IllegibleBadge() {
    Surface(
        color = WarningIllegibleBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, WarningIllegible),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.padding(horizontal = 2.dp, vertical = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = WarningIllegible,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "كلمة غير واضحة",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = WarningIllegible
                )
            )
        }
    }
}

fun parseBoldMarkdown(input: String): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        val regex = Regex("\\*\\*(.*?)\\*\\*")
        val matches = regex.findAll(input)

        for (match in matches) {
            val start = match.range.first
            val end = match.range.last + 1

            if (start > cursor) {
                append(input.substring(cursor, start))
            }

            val boldContent = match.groupValues[1]
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(boldContent)
            }

            cursor = end
        }

        if (cursor < input.length) {
            append(input.substring(cursor))
        }
    }
}

@Composable
fun MarkdownTableComponent(
    tableLines: List<String>,
    modifier: Modifier = Modifier
) {
    if (tableLines.isEmpty()) return

    val horizontalScrollState = rememberScrollState()

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(horizontalScrollState)
        ) {
            Column {
                var isHeader = true
                var rowIndex = 0

                for (line in tableLines) {
                    val trimmed = line.trim()
                    // Skip markdown separator row like |---|---|
                    if (trimmed.replace("|", "").replace("-", "").replace(":", "").replace(" ", "").isEmpty()) {
                        isHeader = false
                        continue
                    }

                    val cells = trimmed.split("|")
                        .filterIndexed { index, _ -> index > 0 && index < trimmed.split("|").size - 1 }
                        .map { it.trim() }

                    if (cells.isEmpty()) continue

                    val rowBg = when {
                        isHeader -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        rowIndex % 2 == 1 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        else -> Color.Transparent
                    }

                    Row(
                        modifier = Modifier
                            .background(rowBg)
                            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        for (cell in cells) {
                            Box(
                                modifier = Modifier
                                    .widthIn(min = 100.dp, max = 220.dp)
                                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = parseBoldMarkdown(cell),
                                    style = if (isHeader) {
                                        MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    if (!isHeader) rowIndex++
                }
            }
        }
    }
}
