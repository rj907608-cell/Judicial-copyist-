package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageUtils {

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
     * Decodes bitmap from Uri and correctly rotates it based on EXIF orientation.
     */
    fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) return null

            // Read EXIF orientation
            val exifStream: InputStream? = context.contentResolver.openInputStream(uri)
            val orientation = exifStream?.let {
                val exif = ExifInterface(it)
                val orient = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                it.close()
                orient
            } ?: ExifInterface.ORIENTATION_NORMAL

            val rotationDegrees = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }

            if (rotationDegrees != 0f) {
                val matrix = Matrix().apply { postRotate(rotationDegrees) }
                Bitmap.createBitmap(
                    originalBitmap,
                    0,
                    0,
                    originalBitmap.width,
                    originalBitmap.height,
                    matrix,
                    true
                )
            } else {
                originalBitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
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
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
