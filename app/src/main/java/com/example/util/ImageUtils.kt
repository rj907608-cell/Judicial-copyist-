package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

object ImageUtils {

    // High resolution for clear, intricate Arabic handwritten text
    const val MAX_IMAGE_DIMENSION = 2560

    /**
     * Creates a temporary file and Uri for camera photo capture.
     */
    fun createTempPictureUri(context: Context): Uri {
        val imageDir = File(context.cacheDir, "images").apply { mkdirs() }
        val tempFile = File(imageDir, "capture_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
    }

    /**
     * High-speed, high-resolution bitmap loader on Dispatchers.IO.
     * Preserves sharp handwriting clarity while preventing UI freeze.
     */
    suspend fun loadOptimizedBitmapFromUri(
        context: Context,
        uri: Uri,
        maxDimension: Int = MAX_IMAGE_DIMENSION
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            // Step 1: Decode image dimensions without loading full pixel array
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            val origWidth = options.outWidth
            val origHeight = options.outHeight
            if (origWidth <= 0 || origHeight <= 0) return@withContext null

            // Step 2: Compute optimal inSampleSize
            var inSampleSize = 1
            val largestEdge = max(origWidth, origHeight)
            while (largestEdge / (inSampleSize * 2) >= maxDimension) {
                inSampleSize *= 2
            }

            // Step 3: Decode with high-precision ARGB_8888 for crystal clarity
            options.inJustDecodeBounds = false
            options.inSampleSize = inSampleSize
            options.inPreferredConfig = Bitmap.Config.ARGB_8888

            val decodedBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            } ?: return@withContext null

            // Step 4: Extract EXIF rotation
            val rotationDegrees = try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val exif = ExifInterface(stream)
                    val orient = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                    when (orient) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                        else -> 0f
                    }
                } ?: 0f
            } catch (e: Exception) {
                0f
            }

            // Step 5: Scale to exact maxDimension and rotate if needed
            val currentMax = max(decodedBitmap.width, decodedBitmap.height)
            val matrix = Matrix()
            if (rotationDegrees != 0f) {
                matrix.postRotate(rotationDegrees)
            }
            if (currentMax > maxDimension) {
                val scale = maxDimension.toFloat() / currentMax.toFloat()
                matrix.postScale(scale, scale)
            }

            if (!matrix.isIdentity) {
                val transformed = Bitmap.createBitmap(
                    decodedBitmap,
                    0,
                    0,
                    decodedBitmap.width,
                    decodedBitmap.height,
                    matrix,
                    true
                )
                if (transformed != decodedBitmap) {
                    decodedBitmap.recycle()
                }
                transformed
            } else {
                decodedBitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Crops a bitmap according to normalized coordinate fractions (0.0f to 1.0f).
     * Returns the cropped bitmap at full optical resolution.
     */
    fun cropBitmap(
        original: Bitmap,
        leftFraction: Float,
        topFraction: Float,
        rightFraction: Float,
        bottomFraction: Float
    ): Bitmap {
        val safeLeft = leftFraction.coerceIn(0f, 0.95f)
        val safeTop = topFraction.coerceIn(0f, 0.95f)
        val safeRight = rightFraction.coerceIn(safeLeft + 0.05f, 1f)
        val safeBottom = bottomFraction.coerceIn(safeTop + 0.05f, 1f)

        val x = (safeLeft * original.width).toInt().coerceIn(0, original.width - 1)
        val y = (safeTop * original.height).toInt().coerceIn(0, original.height - 1)
        val width = ((safeRight - safeLeft) * original.width).toInt().coerceIn(1, original.width - x)
        val height = ((safeBottom - safeTop) * original.height).toInt().coerceIn(1, original.height - y)

        return Bitmap.createBitmap(original, x, y, width, height)
    }

    /**
     * Backward-compatible helper with background IO execution.
     */
    fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return kotlinx.coroutines.runBlocking(Dispatchers.IO) {
            loadOptimizedBitmapFromUri(context, uri)
        }
    }

    /**
     * Saves bitmap to app internal storage and returns the local file path.
     */
    fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap, fileNamePrefix: String = "doc"): String? {
        return try {
            val dir = File(context.filesDir, "documents").apply { mkdirs() }
            val file = File(dir, "${fileNamePrefix}_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
