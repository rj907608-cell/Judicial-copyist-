package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.WordExportConfig
import com.example.util.DocExporter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordExportBottomSheet(
    title: String,
    content: String,
    initialCaseNumber: String = "",
    initialCourtName: String = "المحكمة العامة",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isJudicialFormat by remember { mutableStateOf(true) }
    var courtName by remember { mutableStateOf(initialCourtName.ifBlank { "المحكمة العامة" }) }
    var circuitName by remember { mutableStateOf("الدائرة الحقوقية الأولى") }
    var caseNumber by remember { mutableStateOf(initialCaseNumber) }
    var judgeName by remember { mutableStateOf("") }
    var clerkName by remember { mutableStateOf("") }

    var selectedFont by remember { mutableStateOf("Traditional Arabic") }
    var fontSizePt by remember { mutableFloatStateOf(16f) } // 16pt is official court standard
    var lineSpacing by remember { mutableFloatStateOf(1.25f) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "تصدير محضر أو قرار إلى Word",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "تنسيق متوافق مع معايير وزارة العدل والمحاكم وهوامش السجلات الرسمية",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider()

            // Judicial Format Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isJudicialFormat = !isJudicialFormat },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "اعتماد التنسيق القضائي الرسمي (وزارة العدل)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "ترويسة رسمية، هوامش التثقيب (3 سم يمين)، وجدول بيانات القضية والتوقيعات",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isJudicialFormat,
                    onCheckedChange = { isJudicialFormat = it }
                )
            }

            if (isJudicialFormat) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "بيانات الترويسة القضائية:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = courtName,
                                onValueChange = { courtName = it },
                                label = { Text("المحكمة") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = circuitName,
                                onValueChange = { circuitName = it },
                                label = { Text("الدائرة القضائية") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = caseNumber,
                                onValueChange = { caseNumber = it },
                                label = { Text("رقم القضية / المعاملة") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = judgeName,
                                onValueChange = { judgeName = it },
                                label = { Text("اسم فضيلة القاضي") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                        OutlinedTextField(
                            value = clerkName,
                            onValueChange = { clerkName = it },
                            label = { Text("اسم كاتب الضبط والناسخ القضائي") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            // Font Selector
            Text(
                text = "نوع الخط القضائي:",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Traditional Arabic", "Amiri", "Simplified Arabic").forEach { font ->
                    FilterChip(
                        selected = selectedFont == font,
                        onClick = { selectedFont = font },
                        label = { Text(font) },
                        leadingIcon = if (selectedFont == font) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            // Font Size
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "حجم الخط الأساسي (المعتمد بالمحاكم 16pt):",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "${fontSizePt.toInt()} pt",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
            Slider(
                value = fontSizePt,
                onValueChange = { fontSizePt = it },
                valueRange = 14f..18f,
                steps = 4
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Action Buttons
            Button(
                onClick = {
                    val config = WordExportConfig(
                        fontName = selectedFont,
                        fontSizePt = fontSizePt.toInt(),
                        lineSpacing = lineSpacing,
                        includeHeaderLogo = true,
                        documentTitle = title,
                        isJudicialFormat = isJudicialFormat,
                        courtName = courtName,
                        circuitName = circuitName,
                        caseNumber = caseNumber,
                        judgeName = judgeName,
                        clerkName = clerkName
                    )
                    com.example.util.DocxExporter.shareDocumentAsDocx(context, title, content, config)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تصدير ومشاركة مستند Word الأصلي (.docx)")
            }

            OutlinedButton(
                onClick = {
                    val config = WordExportConfig(
                        fontName = selectedFont,
                        fontSizePt = fontSizePt.toInt(),
                        lineSpacing = lineSpacing,
                        includeHeaderLogo = true,
                        documentTitle = title,
                        isJudicialFormat = isJudicialFormat,
                        courtName = courtName,
                        circuitName = circuitName,
                        caseNumber = caseNumber,
                        judgeName = judgeName,
                        clerkName = clerkName
                    )
                    DocExporter.shareDocumentAsWord(context, title, content, config)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Description, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تصدير بصيغة Word الكلاسيكية (.doc)")
            }

            OutlinedButton(
                onClick = {
                    DocExporter.copyToClipboard(context, content)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("نسخ النص القضائي للحافظة")
            }
        }
    }
}
