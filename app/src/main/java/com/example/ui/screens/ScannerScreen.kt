package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import com.example.ui.components.DocumentScannerLaser
import com.example.ui.theme.QalamEmerald
import com.example.ui.theme.QalamGold
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
    val caseMetadata by viewModel.caseMetadata.collectAsState()

    var activePageIndex by remember { mutableIntStateOf(0) }
    var caseNumber by remember { mutableStateOf(caseMetadata.caseNumber) }
    var courtName by remember { mutableStateOf(caseMetadata.courtName) }
    var circuitName by remember { mutableStateOf(caseMetadata.circuitName) }
    var judgeName by remember { mutableStateOf(caseMetadata.judgeName) }
    var clerkName by remember { mutableStateOf(caseMetadata.clerkName) }
    var customNotes by remember { mutableStateOf("") }

    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val bitmap = ImageUtils.loadBitmapFromUri(context, uri)
            if (bitmap != null) {
                viewModel.addPage(bitmap)
                activePageIndex = scannedPages.size // Focus newly added page
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && capturedImageUri != null) {
            val bitmap = ImageUtils.loadBitmapFromUri(context, capturedImageUri!!)
            if (bitmap != null) {
                viewModel.addPage(bitmap)
                activePageIndex = scannedPages.size
            }
        }
    }

    // React to success
    LaunchedEffect(ocrState) {
        if (ocrState is OcrProcessingState.Success) {
            currentDoc?.let { doc ->
                onNavigateToDetail(doc.id)
            }
        }
    }

    // Keep active page index in range
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
                            text = "المسح والنسخ القضائي",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (scannedPages.isEmpty()) "تصوير أوراق الجلسة" else "${scannedPages.size} أوراق جاهزة للتحويل لـ Word",
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
            // Court Template Selector Chips
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "نوع المستند القضائي المراد نسخه:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        JudicialTemplate.entries.forEach { template ->
                            FilterChip(
                                selected = selectedTemplate == template,
                                onClick = { viewModel.setJudicialTemplate(template) },
                                label = { Text(template.titleArabic, fontSize = 11.sp) },
                                leadingIcon = if (selectedTemplate == template) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null
                            )
                        }
                    }
                }
            }

            // Image Preview Container with Scanner Laser Effect
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E2221))
                    .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                val currentBitmap = scannedPages.getOrNull(activePageIndex)

                if (currentBitmap != null) {
                    Image(
                        bitmap = currentBitmap.asImageBitmap(),
                        contentDescription = "الصفحة ${activePageIndex + 1}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                    // Page Number badge
                    Surface(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "صفحة ${activePageIndex + 1} من ${scannedPages.size}",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Delete current page icon
                    if (ocrState !is OcrProcessingState.Processing) {
                        IconButton(
                            onClick = { viewModel.removePageAt(activePageIndex) },
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(6.dp)
                        ) {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.errorContainer) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "حذف هذه الصفحة",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                        }
                    }

                    // Animated scanning beam
                    DocumentScannerLaser(
                        isScanning = ocrState is OcrProcessingState.Processing
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Icon(
                            Icons.Default.DocumentScanner,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(54.dp)
                        )
                        Text(
                            text = "صور أوراق الجلسة القضائية أو القرار",
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "يمكنك تصوير أكثر من ورقة متتالية لنفس القضية ليتم دمجها في ملف Word واحد",
                            color = Color.LightGray,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    val uri = ImageUtils.createTempPictureUri(context)
                                    capturedImageUri = uri
                                    cameraLauncher.launch(uri)
                                }
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تصوير بالكاميرا")
                            }
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("من المعرض")
                            }
                        }
                    }
                }
            }

            // Multi-Page Thumbnails Strip
            if (scannedPages.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "أوراق ومحاضر القضية (${scannedPages.size}):",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "انقر على الصفحة لمعاينتها",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(scannedPages) { index, bitmap ->
                            Card(
                                modifier = Modifier
                                    .size(width = 75.dp, height = 95.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { activePageIndex = index }
                                    .border(
                                        width = if (activePageIndex == index) 2.5.dp else 1.dp,
                                        color = if (activePageIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "ورقة ${index + 1}",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(bottomStart = 6.dp),
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Add More Pages Button
                        item {
                            OutlinedCard(
                                modifier = Modifier
                                    .size(width = 85.dp, height = 95.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        val uri = ImageUtils.createTempPictureUri(context)
                                        capturedImageUri = uri
                                        cameraLauncher.launch(uri)
                                    },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.AddAPhoto,
                                        contentDescription = "إضافة ورقة تالية",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "+ ورقة تالية",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Processing Indicator Card
            AnimatedVisibility(visible = ocrState is OcrProcessingState.Processing) {
                val state = ocrState as? OcrProcessingState.Processing
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 3.dp
                            )
                            Column {
                                Text(
                                    text = "جاري النسخ والتدقيق القضائي بالذكاء الاصطناعي...",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = state?.stageMessage ?: "قيد المعالجة...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // Error Display
            AnimatedVisibility(visible = ocrState is OcrProcessingState.Error) {
                val errorState = ocrState as? OcrProcessingState.Error
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Text(
                                text = "تعذر نسخ الأوراق",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Text(
                            text = errorState?.errorMessage ?: "خطأ غير معروف",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    val meta = CaseMetadata(
                                        courtName = courtName,
                                        circuitName = circuitName,
                                        caseNumber = caseNumber,
                                        judgeName = judgeName,
                                        clerkName = clerkName
                                    )
                                    viewModel.analyzeJudicialPages(scannedPages, selectedTemplate, customNotes, meta)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("إعادة المحاولة")
                            }
                            OutlinedButton(onClick = onNavigateToSettings) {
                                Text("فحص مفتاح API")
                            }
                        }
                    }
                }
            }

            // Case Quick Metadata Form (رقم القضية، المحكمة، الدائرة)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "بيانات القضية والمحكمة (لتصدير الترويسة لـ Word):",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = caseNumber,
                            onValueChange = { caseNumber = it },
                            label = { Text("رقم القضية (اختياري)") },
                            placeholder = { Text("مثال: 461028392") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = circuitName,
                            onValueChange = { circuitName = it },
                            label = { Text("الدائرة القضائية") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = courtName,
                            onValueChange = { courtName = it },
                            label = { Text("المحكمة") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = judgeName,
                            onValueChange = { judgeName = it },
                            label = { Text("اسم فضيلة القاضي") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    OutlinedTextField(
                        value = customNotes,
                        onValueChange = { customNotes = it },
                        label = { Text("توجيهات إضافية للناسخ (مثل: ركز على دفوع المدعى عليه...)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Primary OCR Trigger Button
            Button(
                onClick = {
                    if (scannedPages.isEmpty()) {
                        Toast.makeText(context, "يرجى تصوير أو اختيار ورقة جلسة أولاً", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val metadata = CaseMetadata(
                        courtName = courtName,
                        circuitName = circuitName,
                        caseNumber = caseNumber,
                        judgeName = judgeName,
                        clerkName = clerkName
                    )
                    viewModel.updateCaseMetadata(metadata)
                    viewModel.analyzeJudicialPages(scannedPages, selectedTemplate, customNotes, metadata)
                },
                enabled = scannedPages.isNotEmpty() && ocrState !is OcrProcessingState.Processing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("start_ocr_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.AutoFixHigh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                val pageText = if (scannedPages.size > 1) " (${scannedPages.size} صفحات)" else ""
                Text(
                    text = "بدء النسخ القضائي الذكي والتحويل لـ Word$pageText",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
