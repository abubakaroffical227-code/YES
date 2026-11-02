package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Base64
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Encodes picked local media URIs into compact, cross-device Firestore-safe JPEG Base64 data URLs
 * (`data:image/jpeg;base64,...`) and decodes any image source (`data:image`, `https://`, `content://`,
 * `file://`) into software `ARGB_8888` [Bitmap]s so Student Photos, University Student Cards,
 * Event Posters, and PDF ID Cards render reliably on every device and downloaded PDF.
 */
object ImageEncodingUtils {

  fun encodeUriToDataUrl(
    context: Context,
    uri: Uri,
    maxDimension: Int = 520,
    quality: Int = 68,
  ): String? = runCatching {
    val rawBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
      ?: return null
    if (rawBytes.isEmpty()) return null

    val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, boundsOpts)
    val srcW = boundsOpts.outWidth
    val srcH = boundsOpts.outHeight
    if (srcW <= 0 || srcH <= 0) return null

    var sampleSize = 1
    while (max(srcW / sampleSize, srcH / sampleSize) > maxDimension * 2) {
      sampleSize *= 2
    }

    val decodeOpts = BitmapFactory.Options().apply {
      inSampleSize = sampleSize
      inPreferredConfig = Bitmap.Config.ARGB_8888
    }
    val decoded = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, decodeOpts)
      ?: return null

    val largestSide = max(decoded.width, decoded.height)
    val scaled = if (largestSide > maxDimension) {
      val ratio = maxDimension.toFloat() / largestSide.toFloat()
      val targetW = (decoded.width * ratio).toInt().coerceAtLeast(1)
      val targetH = (decoded.height * ratio).toInt().coerceAtLeast(1)
      Bitmap.createScaledBitmap(decoded, targetW, targetH, true)
    } else {
      decoded
    }

    fun compressWithQuality(q: Int): String {
      val out = ByteArrayOutputStream()
      scaled.compress(Bitmap.CompressFormat.JPEG, q, out)
      val b64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
      return "data:image/jpeg;base64,$b64"
    }

    var dataUrl = compressWithQuality(quality)
    if (dataUrl.length > 440_000) {
      dataUrl = compressWithQuality(45)
    }
    if (dataUrl.length > 480_000) {
      val smaller = Bitmap.createScaledBitmap(
        scaled,
        (scaled.width * 0.7f).toInt().coerceAtLeast(1),
        (scaled.height * 0.7f).toInt().coerceAtLeast(1),
        true,
      )
      val out = ByteArrayOutputStream()
      smaller.compress(Bitmap.CompressFormat.JPEG, 40, out)
      val b64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
      dataUrl = "data:image/jpeg;base64,$b64"
    }
    dataUrl
  }.getOrNull()

  fun ensureSoftwareArgbBitmap(bitmap: Bitmap): Bitmap {
    return if (bitmap.config == Bitmap.Config.ARGB_8888 && !bitmap.isRecycled) {
      bitmap
    } else {
      bitmap.copy(Bitmap.Config.ARGB_8888, false) ?: bitmap
    }
  }

  /**
   * Produces a center-cropped software [Bitmap.Config.ARGB_8888] bitmap suitable for
   * Android [android.graphics.pdf.PdfDocument] software canvas rendering without distortion.
   */
  fun prepareCenterCroppedSoftwareBitmap(
    source: Bitmap,
    targetWidth: Int,
    targetHeight: Int,
  ): Bitmap {
    val safeSource = ensureSoftwareArgbBitmap(source)
    val outW = targetWidth.coerceAtLeast(1)
    val outH = targetHeight.coerceAtLeast(1)
    val output = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    val srcW = safeSource.width.coerceAtLeast(1)
    val srcH = safeSource.height.coerceAtLeast(1)
    val srcAspect = srcW.toFloat() / srcH.toFloat()
    val dstAspect = outW.toFloat() / outH.toFloat()

    val srcRect = if (srcAspect > dstAspect) {
      val cropW = (srcH * dstAspect).toInt().coerceIn(1, srcW)
      val left = ((srcW - cropW) / 2).coerceAtLeast(0)
      Rect(left, 0, left + cropW, srcH)
    } else {
      val cropH = (srcW / dstAspect).toInt().coerceIn(1, srcH)
      val top = ((srcH - cropH) / 2).coerceAtLeast(0)
      Rect(0, top, srcW, top + cropH)
    }

    canvas.drawBitmap(safeSource, srcRect, RectF(0f, 0f, outW.toFloat(), outH.toFloat()), paint)
    return output
  }

  fun decodeDataUrlOrNull(source: String): Bitmap? {
    val trimmed = source.trim()
    if (trimmed.isEmpty()) return null

    val base64Payload = when {
      trimmed.startsWith("data:image", ignoreCase = true) -> {
        val commaIdx = trimmed.indexOf(',')
        if (commaIdx < 0 || commaIdx >= trimmed.length - 1) return null
        trimmed.substring(commaIdx + 1).trim()
      }
      trimmed.startsWith("/9j/") || trimmed.startsWith("iVBOR") -> trimmed
      else -> return null
    }

    return runCatching {
      val bytes = Base64.decode(base64Payload, Base64.DEFAULT)
      if (bytes.isEmpty()) return@runCatching null
      val opts = BitmapFactory.Options().apply {
        inPreferredConfig = Bitmap.Config.ARGB_8888
      }
      BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)?.let(::ensureSoftwareArgbBitmap)
    }.getOrNull()
  }

  /**
   * Resolves any student photograph source (`data:image/...`, `https://...` Google profile URL,
   * `content://...`, or `file://...`) into a software `ARGB_8888` center-cropped portrait [Bitmap]
   * so downloaded ID Card PDFs always display the student's photograph.
   */
  suspend fun loadBitmapForPdf(
    context: Context,
    imageSource: String,
    targetWidth: Int = 220,
    targetHeight: Int = 256,
  ): Bitmap? = withContext(Dispatchers.IO) {
    val trimmed = imageSource.trim()
    if (trimmed.isEmpty()) return@withContext null

    // 1. Fast path: Base64 data URL or raw Base64 JPEG/PNG
    decodeDataUrlOrNull(trimmed)?.let { bmp ->
      return@withContext prepareCenterCroppedSoftwareBitmap(bmp, targetWidth, targetHeight)
    }

    // 2. Local content:// or file:// URI via ContentResolver
    if (trimmed.startsWith("content://", ignoreCase = true) ||
      trimmed.startsWith("file://", ignoreCase = true) ||
      trimmed.startsWith("android.resource://", ignoreCase = true)
    ) {
      val localBmp = runCatching {
        val uri = Uri.parse(trimmed)
        context.contentResolver.openInputStream(uri)?.use { stream ->
          val bytes = stream.readBytes()
          val opts = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
          }
          BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
        }
      }.getOrNull()
      if (localBmp != null) {
        return@withContext prepareCenterCroppedSoftwareBitmap(localBmp, targetWidth, targetHeight)
      }
    }

    // 3. Coil ImageLoader (handles https:// Google Sign-In avatars, cached URLs, and URIs)
    val coilBmp = runCatching {
      val loader = ImageLoader(context)
      val request = ImageRequest.Builder(context)
        .data(trimmed)
        .allowHardware(false)
        .bitmapConfig(Bitmap.Config.ARGB_8888)
        .build()
      val result = loader.execute(request)
      val drawable = (result as? SuccessResult)?.drawable
      when (drawable) {
        is BitmapDrawable -> drawable.bitmap
        null -> null
        else -> {
          val w = drawable.intrinsicWidth.takeIf { it > 0 } ?: targetWidth
          val h = drawable.intrinsicHeight.takeIf { it > 0 } ?: targetHeight
          val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
          val c = Canvas(bmp)
          drawable.setBounds(0, 0, w, h)
          drawable.draw(c)
          bmp
        }
      }
    }.getOrNull()

    if (coilBmp != null) {
      return@withContext prepareCenterCroppedSoftwareBitmap(coilBmp, targetWidth, targetHeight)
    }

    // 4. Direct HTTP(S) stream fallback for remote URLs
    if (trimmed.startsWith("http://", ignoreCase = true) ||
      trimmed.startsWith("https://", ignoreCase = true)
    ) {
      val networkBmp = runCatching {
        val conn = (URL(trimmed).openConnection() as HttpURLConnection).apply {
          connectTimeout = 7000
          readTimeout = 7000
          doInput = true
          instanceFollowRedirects = true
        }
        conn.inputStream.use { stream ->
          val bytes = stream.readBytes()
          val opts = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
          }
          BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
        }
      }.getOrNull()
      if (networkBmp != null) {
        return@withContext prepareCenterCroppedSoftwareBitmap(networkBmp, targetWidth, targetHeight)
      }
    }

    null
  }
}
