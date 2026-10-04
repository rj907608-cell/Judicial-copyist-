package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DocumentCategory
import com.example.data.model.SampleDocument
import com.example.data.model.ScannedDocument
import com.example.ui.components.WordExportBottomSheet
import com.example.ui.theme.JudicialGold
import com.example.ui.theme.JudicialNavy
import com.example.ui.theme.WordDocBlue
import com.example.util.DocExporter
import com.example.util.DocxExporter
import com.example.util.ImageUtils
import com.example.util.SampleDataHelper
import com.example.viewmodel.DocumentViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: DocumentViewModel,
    onNavigateToScan: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val documents by viewModel.documents.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showSampleDialog by remember { mutableStateOf(false) }
    var exportDocTarget by remember { mutableStateOf<ScannedDocument?>(null) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Android Zero-permission Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.addPageFromUri(uri) { success ->
                if (success) {
                    onNavigateToScan()
                } else {
                    Toast.makeText(context, "فشل تحميل الصورة المحددة", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Camera Capture Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && capturedImageUri != null) {
            viewModel.addPageFromUri(capturedImageUri!!) { loaded ->
                if (loaded) {
                    onNavigateToScan()
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = JudicialNavy,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "⚖️",
                                    fontSize = 20.sp
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "الناسخ الذكي",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = JudicialNavy
                                )
                            )
                            Text(
                                text = "تحويل الخط اليدوي إلى Word ومشاركته للحاسوب",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "الإعدادات")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val uri = ImageUtils.createTempPictureUri(context)
                    capturedImageUri = uri
                    cameraLauncher.launch(uri)
                },
                icon = { Icon(Icons.Default.CameraAlt, contentDescription = null) },
                text = { Text("مسح بالكاميرا") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("camera_scan_fab")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Action Banner
            item {
                HeroScannerCard(
                    onCameraClick = {
                        val uri = ImageUtils.createTempPictureUri(context)
                        capturedImageUri = uri
                        cameraLauncher.launch(uri)
                    },
                    onGalleryClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onSampleClick = {
                        showSampleDialog = true
                    }
                )
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("بحث في المستندات والنصوص المستخرجة...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "مسح")
                            }
                        }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("search_input")
                )
            }

            // Category Filter Chips
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(DocumentCategory.entries) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { viewModel.setSelectedCategory(category) },
                            label = { Text(category.titleArabic) },
                            leadingIcon = if (selectedCategory == category) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }
            }

            // Documents Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "المستندات المحفوظة (${documents.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Document Items
            if (documents.isEmpty()) {
                item {
                    EmptyDocumentsPlaceholder(
                        onSelectSample = { showSampleDialog = true }
                    )
                }
            } else {
                items(documents, key = { it.id }) { document ->
                    DocumentCardItem(
                        document = document,
                        onClick = { onNavigateToDetail(document.id) },
                        onToggleFavorite = { viewModel.toggleFavorite(document) },
                        onExportWord = { exportDocTarget = document },
                        onDelete = { viewModel.deleteDocument(document) },
                        onCopyText = { DocExporter.copyToClipboard(context, document.extractedText) }
                    )
                }
            }
        }
    }

    // Sample Document Selection Dialog
    if (showSampleDialog) {
        SampleSelectionDialog(
            onDismiss = { showSampleDialog = false },
            onSelectSample = { sample ->
                showSampleDialog = false
                viewModel.loadSample(sample)
                viewModel.currentDocument.value?.let {
                    onNavigateToDetail(it.id)
                } ?: run {
                    onNavigateToScan()
                }
            }
        )
    }

    // Export Word Bottom Sheet
    exportDocTarget?.let { doc ->
        WordExportBottomSheet(
            title = doc.title,
            content = doc.extractedText,
            initialCaseNumber = doc.caseNumber,
            initialCourtName = doc.courtName,
            onDismiss = { exportDocTarget = null }
        )
    }
}

@Composable
fun HeroScannerCard(
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onSampleClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "الناسخ الذكي • تحويل خط اليد لملف Word",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = JudicialNavy
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ارفع أو التقط صورة لأي محضر أو قرار، واحصل فوراً على ملف Word رسمي جاهز للمشاركة للحاسوب",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = JudicialGold,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "⚖️",
                            fontSize = 22.sp
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onCameraClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = JudicialNavy)
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("الكاميرا")
                }

                FilledTonalButton(
                    onClick = onGalleryClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("الاستوديو")
                }

                OutlinedButton(
                    onClick = onSampleClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("نماذج جاهزة")
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DocumentCardItem(
    document: ScannedDocument,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onExportWord: () -> Unit,
    onDelete: () -> Unit,
    onCopyText: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val dateStr = SimpleDateFormat("yyyy/MM/dd", Locale.forLanguageTag("ar")).format(Date(document.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("document_card_${document.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = document.category,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    if (document.caseNumber.isNotBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "قضية ${document.caseNumber}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    if (document.pageCount > 1) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${document.pageCount} صفحات",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (document.isFavorite) Icons.Default.Star else Icons.Outlined.StarOutline,
                            contentDescription = "المفضلة",
                            tint = if (document.isFavorite) JudicialGold else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "خيارات إضافية")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("تصدير إلى Word") },
                                onClick = { showMenu = false; onExportWord() },
                                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("نسخ النص") },
                                onClick = { showMenu = false; onCopyText() },
                                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("حذف المستند", color = MaterialTheme.colorScheme.error) },
                                onClick = { showMenu = false; onDelete() },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                            )
                        }
                    }
                }
            }

            Text(
                text = document.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Snippet preview of extracted text
            Text(
                text = document.extractedText.lines().filter { it.isNotBlank() && !it.startsWith("#") }.joinToString(" "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${document.wordCount} كلمة",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onExportWord,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WordDocBlue)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مشاركة Word للحاسوب", fontSize = 12.sp, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = onCopyText,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("نسخ", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyDocumentsPlaceholder(
    onSelectSample: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Outlined.Description,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "لا توجد مستندات بعد",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "قم بمسح ورقة مكتوبة بخط اليد عبر الكاميرا، أو اختر صورة من المعرض، أو جرب أحد النماذج المخطوطة الجاهزة فوراً.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onSelectSample,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("تجربة النماذج الجاهزة")
            }
        }
    }
}

@Composable
fun SampleSelectionDialog(
    onDismiss: () -> Unit,
    onSelectSample: (SampleDocument) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "اختر نموذجاً للمعاينة والمسح",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "نماذج بخط اليد مصممة خصيصاً لاختبار دقة استخراج العناوين، الجداول والقوائم:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 340.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SampleDataHelper.SAMPLES) { sample ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectSample(sample) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = sample.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = sample.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
