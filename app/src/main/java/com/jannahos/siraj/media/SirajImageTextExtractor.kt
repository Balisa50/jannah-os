package com.jannahos.siraj.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * On-device OCR for selected images. The original image is never uploaded.
 * Large images are downsampled before recognition to bound memory use.
 */
object SirajImageTextExtractor {
    private const val MAX_FILE_BYTES = 25L * 1024L * 1024L
    private const val MAX_PIXELS = 100_000_000L
    private const val MAX_DIMENSION = 2400
    private const val MAX_TEXT_CHARS = 6000

    suspend fun extractText(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        val resolver = context.applicationContext.contentResolver
        val fileSize = resolver.openFileDescriptor(uri, "r")?.use { it.statSize }
            ?: error("Android could not open the selected image.")
        require(fileSize <= MAX_FILE_BYTES) { "Image exceeds the 25 MB processing limit." }

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        } ?: error("Could not read the selected image.")
        require(bounds.outWidth > 0 && bounds.outHeight > 0) { "The selected file is not a readable image." }
        require(bounds.outWidth.toLong() * bounds.outHeight.toLong() <= MAX_PIXELS) {
            "Image dimensions are too large to process safely."
        }

        var sampleSize = 1
        while (maxOf(bounds.outWidth / sampleSize, bounds.outHeight / sampleSize) > MAX_DIMENSION) {
            sampleSize *= 2
        }
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val bitmap = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: error("Could not decode the selected image.")

        var oriented: Bitmap = bitmap
        try {
            val orientation = resolver.openInputStream(uri)?.use { stream ->
                ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    matrix.postScale(-1f, 1f)
                    matrix.postRotate(90f)
                }
                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    matrix.postScale(-1f, 1f)
                    matrix.postRotate(270f)
                }
            }
            if (!matrix.isIdentity) {
                oriented = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            }

            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            try {
                val result = Tasks.await(recognizer.process(InputImage.fromBitmap(oriented, 0)))
                result.text.trim().take(MAX_TEXT_CHARS)
            } finally {
                recognizer.close()
            }
        } finally {
            if (oriented !== bitmap) oriented.recycle()
            bitmap.recycle()
        }
    }
}
