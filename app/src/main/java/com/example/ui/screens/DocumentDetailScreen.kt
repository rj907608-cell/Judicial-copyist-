package com.example.ui.screens

import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScannedDocument
import com.example.ui.components.ArabicTtsPlayer
import com.example.ui.components.MarkdownDocumentViewer
import com.example.ui.components.WordExportBottomSheet
import com.example.ui.theme.JudicialGold
import com.example.ui.theme.JudicialNavy
import com.example.ui.theme.WordDocBlue
import com.example.util.DocExporter
import com.example.util.DocxExporter
import com.example.viewmodel.DocumentViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentDetailScreen(
    documentId: Long,
    viewModel: DocumentViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentDoc by viewModel.currentDocument.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showExportSheet by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var editableText by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(documentId) {
        viewModel.selectDocumentById(documentId)
    }

    LaunchedEffect(currentDoc) {
        currentDoc?.let {
            if (!isEditing) {
                editableText = it.extractedText
            }
        }
    }

    val doc = currentDoc

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = doc?.title ?: "عرض المستند",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        doc?.let {
                            Text(
                                text = "${it.category} • ${it.wordCount} كلمة",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    doc?.let { d ->
                        IconButton(onClick = { viewModel.toggleFavorite(d) }) {
                            Icon(
                                imageVector = if (d.isFavorite) Icons.Default.Star else Icons.Outlined.StarOutline,
                                contentDescription = "المفضلة",
                                tint = if (d.isFavorite) JudicialGold else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { showExportSheet = true }) {
                            Icon(Icons.Default.Description, contentDescription = "تصدير إلى Word")
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف المستند", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            doc?.let { d ->
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                DocxExporter.shareDocumentAsDocx(context, d.title, d.extractedText)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_word_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WordDocBlue)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("مشاركة Word للحاسوب", color = Color.White)
                        }

                        OutlinedButton(
                            onClick = {
                                DocExporter.copyToClipboard(context, d.extractedText)
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("نسخ")
                        }

                        OutlinedButton(
                            onClick = { showExportSheet = true },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (doc == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Direct PC Share Banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = WordDocBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "تم حفظ المستند محلياً وتجهيز ملف Word الرسمي",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = JudicialNavy
                            )
                        }

                        Button(
                            onClick = {
                                DocxExporter.shareDocumentAsDocx(context, doc.title, doc.extractedText)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WordDocBlue)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "مشاركة ملف Word للحاسوب الآن (.docx)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }

                // Audio Reader Toolbar
                ArabicTtsPlayer(
                    textToRead = doc.extractedText,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                // View Tabs
                PrimaryTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("المستند المنسق") },
                        icon = { Icon(Icons.AutoMirrored.Filled.FormatAlignRight, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = {
                            selectedTabIndex = 1
                            editableText = doc.extractedText
                        },
                        text = { Text("تحرير النص") },
                        icon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTabIndex == 2,
                        onClick = { selectedTabIndex = 2 },
                        text = { Text("الأصل اليدوي") },
                        icon = { Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }

                // Tab Content
                when (selectedTabIndex) {
                    0 -> {
                        // Rendered Word-ready View
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            MarkdownDocumentViewer(
                                markdownText = doc.extractedText,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    1 -> {
                        // In-App Editor
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Quick formatting toolbar
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "أدوات التنسيق السريعة:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    FilterChip(
                                        selected = false,
                                        onClick = { editableText = "# $editableText" },
                                        label = { Text("عنوان #") }
                                    )
                                    FilterChip(
                                        selected = false,
                                        onClick = { editableText = "## $editableText" },
                                        label = { Text("فرعي ##") }
                                    )
                                    FilterChip(
                                        selected = false,
                                        onClick = { editableText = "$editableText\n- " },
                                        label = { Text("قائمة نقطية") }
                                    )
                                    FilterChip(
                                        selected = false,
                                        onClick = {
                                            editableText = "$editableText\n\n| البند | البيان | القيمة |\n|---|---|---|\n| 1 |  |  |\n"
                                        },
                                        label = { Text("جدول") }
                                    )
                                    FilterChip(
                                        selected = false,
                                        onClick = { editableText = "$editableText [كلمة غير واضحة] " },
                                        label = { Text("[كلمة غير واضحة]") }
                                    )
                                    FilterChip(
                                        selected = false,
                                        onClick = { editableText = "$editableText\n\n---page-break---\n\n" },
                                        label = { Text("فاصل صفحة جديدة") }
                                    )
                                }

                                Text(
                                    text = "كليشات قضائية متكررة (شريط أدوات الناسخ):",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    SuggestionChip(
                                        onClick = { editableText = "$editableText\n\n**وبسؤال المدعي عن دعواه قرر قائلاً:** " },
                                        label = { Text("أقوال المدعي") }
                                    )
                                    SuggestionChip(
                                        onClick = { editableText = "$editableText\n\n**وبمواجهة المدعى عليه بدعوى المدعي أجاب قائلاً:** " },
                                        label = { Text("إجابة المدعى عليه") }
                                    )
                                    SuggestionChip(
                                        onClick = { editableText = "$editableText\n\nطلب وكيل المدعى عليه مهلة لتقديم مذكرته الجوابية." },
                                        label = { Text("طلب مهلة للرد") }
                                    )
                                    SuggestionChip(
                                        onClick = { editableText = "$editableText\n\n**لذلك كله؛ حكمت الدائرة بما يلي:**\n- **أولاً:** " },
                                        label = { Text("حكمت الدائرة") }
                                    )
                                    SuggestionChip(
                                        onClick = { editableText = "$editableText\n\nحلف بالله العظيم قائلاً: «أقسم بالله العظيم أن أقول الحق ولا شيء غير الحق»." },
                                        label = { Text("حلف اليمين") }
                                    )
                                    SuggestionChip(
                                        onClick = { editableText = "$editableText\n\nورفعت الجلسة في تمام الساعة [....] وأثبت ما تقدم." },
                                        label = { Text("رفع الجلسة") }
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = editableText,
                                onValueChange = { editableText = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .testTag("document_text_editor"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Button(
                                onClick = {
                                    viewModel.updateCurrentDocumentText(editableText)
                                    Toast.makeText(context, "تم حفظ التعديلات بنجاح", Toast.LENGTH_SHORT).show()
                                    selectedTabIndex = 0
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("حفظ التعديلات على المستند")
                            }
                        }
                    }
                    2 -> {
                        // Original Handwritten Image
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val imgPath = doc.localImagePath
                            val bitmap = remember(imgPath) {
                                if (imgPath != null && File(imgPath).exists()) {
                                    BitmapFactory.decodeFile(imgPath)
                                } else {
                                    null
                                }
                            }

                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "صورة المستند اليدوي الأصلية",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.BrokenImage, contentDescription = null, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("الصورة الأصلية غير متوفرة لهذا المستند")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Export Word Bottom Sheet
    if (showExportSheet && doc != null) {
        WordExportBottomSheet(
            title = doc.title,
            content = doc.extractedText,
            initialCaseNumber = doc.caseNumber,
            initialCourtName = doc.courtName,
            onDismiss = { showExportSheet = false }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirm && doc != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("حذف المستند") },
            text = { Text("هل أنت متأكد من رغبتك في حذف مستند '${doc.title}' نهائياً؟") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteDocument(doc)
                        onNavigateBack()
                    }
                ) {
                    Text("حذف", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
