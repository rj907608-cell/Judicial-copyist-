package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CaseMetadata
import com.example.data.model.JudicialTemplate
import com.example.data.model.OcrProcessingState
import com.example.ui.theme.JudicialGold
import com.example.ui.theme.JudicialNavy
import com.example.ui.theme.WordDocBlue
import com.example.util.ImageUtils
import com.example.viewmodel.DocumentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    viewModel: DocumentViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scannedPages by viewModel.scannedPages.collectAsState()
    val ocrState by viewModel.ocrState.collectAsState()
    val currentDoc by viewModel.currentDocument.collectAsState()
    val selectedTemplate by viewModel.selectedTemplate.collectAsState()

    var activePageIndex by remember { mutableIntStateOf(0) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.addPageFromUri(uri) { success ->
                if (success) {
                    activePageIndex = scannedPages.size
                }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && capturedImageUri != null) {
            viewModel.addPageFromUri(capturedImageUri!!) { success ->
                if (success) {
                    activePageIndex = scannedPages.size
                }
            }
        }
    }

    // Automatically navigate to detail view when OCR succeeds
    LaunchedEffect(ocrState) {
        if (ocrState is OcrProcessingState.Success) {
            currentDoc?.let { doc ->
                onNavigateToDetail(doc.id)
            }
        }
    }

    // Keep active page index in valid range
    LaunchedEffect(scannedPages.size) {
        if (activePageIndex >= scannedPages.size && scannedPages.isNotEmpty()) {
            activePageIndex = scannedPages.size - 1
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "تحويل صورة لملف Word",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (scannedPages.isEmpty()) "ارفع الصورة واختر نوع المستند" else "${scannedPages.size} صفحات محددة",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    if (scannedPages.isNotEmpty() && ocrState !is OcrProcessingState.Processing) {
                        TextButton(onClick = { viewModel.clearPages() }) {
                            Text("مسح الكل", color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // STEP 1: Image Preview & Management
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "1. صور المستند المكتوب بخط اليد:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = JudicialNavy
                        )
                        if (scannedPages.isNotEmpty()) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "${scannedPages.size} صفحات",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    if (scannedPages.isEmpty()) {
                        // Empty upload state
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = null,
                                    tint = JudicialNavy,
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(
                                    text = "التقط صورة بكاميرا الهاتف أو اختر من الاستوديو",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        // Active image preview
                        val currentBitmap = scannedPages.getOrNull(activePageIndex)
                        if (currentBitmap != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0F172A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = currentBitmap.asImageBitmap(),
                                    contentDescription = "الصفحة ${activePageIndex + 1}",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )

                                // Page badge
                                Surface(
                                    color = Color.Black.copy(alpha = 0.75f),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "صفحة ${activePageIndex + 1} من ${scannedPages.size}",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                // Delete page button
                                if (ocrState !is OcrProcessingState.Processing) {
                                    IconButton(
                                        onClick = { viewModel.removePageAt(activePageIndex) },
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(8.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                            .size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "حذف الصفحة",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Thumbnail carousel for multiple pages
                        if (scannedPages.size > 1) {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(scannedPages) { index, bmp ->
                                    val isSelected = index == activePageIndex
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) JudicialNavy else MaterialTheme.colorScheme.outlineVariant,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { activePageIndex = index }
                                    ) {
                                        Image(
                                            bitmap = bmp.asImageBitmap(),
                                            contentDescription = "صفحة ${index + 1}",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Add more buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val uri = ImageUtils.createTempPictureUri(context)
                                capturedImageUri = uri
                                cameraLauncher.launch(uri)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (scannedPages.isEmpty()) "تصوير بالكاميرا" else "+ إضافة بالكاميرا", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (scannedPages.isEmpty()) "من الاستوديو" else "+ إضافة من الاستوديو", fontSize = 12.sp)
                        }
                    }
                }
            }

            // STEP 2: Document Type Selection (Pure & Direct!)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "2. اختر نوع المستند لتنسيق ملف Word الرسمي:",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = JudicialNavy
                    )

                    val templateList = listOf(
                        Triple(JudicialTemplate.SESSION_MINUTES, "محضر جلسة قضائية", "تنسيق حضور، أقوال، دفوع، وقرار الجلسة"),
                        Triple(JudicialTemplate.JUDICIAL_DECISION, "قرار قضائي / صك حكم", "تنسيق الديباجة، الأسباب والتسبيب، ومنطوق الحكم"),
                        Triple(JudicialTemplate.WITNESS_TESTIMONY, "محضر ضبط أقوال وشهادة", "تنسيق بيانات الشاهد، اليمين، والشهادة المضبوطة"),
                        Triple(JudicialTemplate.GENERAL_JUDICIAL_NOTE, "مذكرة ولائحة دعوى", "تنسيق الوقائع، الأسانيد النظامية، والطلبات الختامية")
                    )

                    templateList.forEach { (template, title, desc) ->
                        val isSelected = selectedTemplate == template
                        Surface(
                            onClick = { viewModel.setJudicialTemplate(template) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) JudicialNavy else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.setJudicialTemplate(template) },
                                    colors = RadioButtonDefaults.colors(selectedColor = JudicialNavy)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) JudicialNavy else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = JudicialNavy,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Processing State Indicator
            if (ocrState is OcrProcessingState.Processing) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(color = JudicialNavy, modifier = Modifier.size(32.dp))
                        Text(
                            text = "جارٍ تفريغ الخط بالذكاء الاصطناعي وتجهيز ملف Word...",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = JudicialNavy
                        )
                        Text(
                            text = "يتم الحفظ تلقائياً في السجل المحلي للتطبيق",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (ocrState is OcrProcessingState.Error) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = (ocrState as OcrProcessingState.Error).errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        OutlinedButton(
                            onClick = onNavigateToSettings,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("التحقق من مفتاح Gemini API في الإعدادات")
                        }
                    }
                }
            }

            // STEP 3: Execution Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = JudicialNavy,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "التطبيق يحفظ المستندات محلياً، ويلزم الإنترنت فقط أثناء المعالجة",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = {
                    if (scannedPages.isEmpty()) {
                        Toast.makeText(context, "يرجى التقاط صورة أو اختيارها أولاً", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    viewModel.analyzeJudicialPages(
                        pages = scannedPages,
                        template = selectedTemplate,
                        customInstruction = null,
                        metadata = CaseMetadata()
                    )
                },
                enabled = scannedPages.isNotEmpty() && ocrState !is OcrProcessingState.Processing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("start_ocr_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = JudicialNavy)
            ) {
                Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = JudicialGold)
                Spacer(modifier = Modifier.width(10.dp))
                val pageText = if (scannedPages.size > 1) " (${scannedPages.size} صفحات)" else ""
                Text(
                    text = "تفريغ فوري وتوليد مستند Word رسمي$pageText",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }
    }
}
