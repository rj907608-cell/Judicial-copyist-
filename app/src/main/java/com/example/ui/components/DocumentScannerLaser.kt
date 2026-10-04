package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.QalamEmerald
import com.example.ui.theme.QalamGold

@Composable
fun DocumentScannerLaser(
    isScanning: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isScanning) return

    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_pos"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val yPos = size.height * progress
            val beamHeight = 40f

            // Laser beam gradient
            val laserBrush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    QalamEmerald.copy(alpha = 0.25f),
                    QalamGold.copy(alpha = 0.85f),
                    QalamEmerald.copy(alpha = 0.25f),
                    Color.Transparent
                ),
                startY = yPos - beamHeight,
                endY = yPos + beamHeight
            )

            drawRect(
                brush = laserBrush,
                topLeft = Offset(0f, yPos - beamHeight),
                size = Size(size.width, beamHeight * 2)
            )

            // Sharp center line
            drawLine(
                color = QalamGold,
                start = Offset(0f, yPos),
                end = Offset(size.width, yPos),
                strokeWidth = 3f
            )

            // Target corners
            val cornerLen = 32f
            val cornerStroke = 4f
            val cornerColor = QalamGold

            // Top-Left
            drawLine(cornerColor, Offset(16f, 16f), Offset(16f + cornerLen, 16f), cornerStroke)
            drawLine(cornerColor, Offset(16f, 16f), Offset(16f, 16f + cornerLen), cornerStroke)

            // Top-Right
            drawLine(cornerColor, Offset(size.width - 16f, 16f), Offset(size.width - 16f - cornerLen, 16f), cornerStroke)
            drawLine(cornerColor, Offset(size.width - 16f, 16f), Offset(size.width - 16f, 16f + cornerLen), cornerStroke)

            // Bottom-Left
            drawLine(cornerColor, Offset(16f, size.height - 16f), Offset(16f + cornerLen, size.height - 16f), cornerStroke)
            drawLine(cornerColor, Offset(16f, size.height - 16f), Offset(16f, size.height - 16f - cornerLen), cornerStroke)

            // Bottom-Right
            drawLine(cornerColor, Offset(size.width - 16f, size.height - 16f), Offset(size.width - 16f - cornerLen, size.height - 16f), cornerStroke)
            drawLine(cornerColor, Offset(size.width - 16f, size.height - 16f), Offset(size.width - 16f, size.height - 16f - cornerLen), cornerStroke)
        }
    }
}
