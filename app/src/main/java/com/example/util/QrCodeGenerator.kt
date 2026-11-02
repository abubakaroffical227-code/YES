package com.example.util

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * Generates authentic ISO/IEC 18004 QR Codes via ZXing [QRCodeWriter] for YES Student ID Cards,
 * Event Attendance Check-in Codes, and Certificate Verification Seals, scannable by CameraX
 * and standard mobile QR readers.
 */
object QrCodeGenerator {

  fun generateMatrix(payload: String): Array<BooleanArray> {
    val safePayload = payload.ifBlank { "YES-UAF" }
    return runCatching {
      val hints = mapOf(
        EncodeHintType.MARGIN to 0,
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
        EncodeHintType.CHARACTER_SET to "UTF-8",
      )
      val bitMatrix = QRCodeWriter().encode(safePayload, BarcodeFormat.QR_CODE, 0, 0, hints)
      val h = bitMatrix.height
      val w = bitMatrix.width
      Array(h) { r ->
        BooleanArray(w) { c -> bitMatrix.get(c, r) }
      }
    }.getOrElse {
      Array(21) { r -> BooleanArray(21) { c -> (r + c) % 2 == 0 } }
    }
  }

  fun createBitmap(
    payload: String,
    pixelSize: Int = 300,
    darkColor: Int = AndroidColor.rgb(10, 61, 34),
  ): Bitmap {
    val matrix = generateMatrix(payload)
    val matrixSize = matrix.size.coerceAtLeast(1)
    val bmp = Bitmap.createBitmap(pixelSize, pixelSize, Bitmap.Config.ARGB_8888)
    bmp.eraseColor(AndroidColor.WHITE)
    val quietZone = 2
    val totalModules = matrixSize + quietZone * 2
    val cellSize = pixelSize.toFloat() / totalModules

    for (r in matrix.indices) {
      for (c in matrix[r].indices) {
        if (matrix[r][c]) {
          val startX = ((c + quietZone) * cellSize).toInt().coerceIn(0, pixelSize)
          val endX = ((c + quietZone + 1) * cellSize).toInt().coerceIn(0, pixelSize)
          val startY = ((r + quietZone) * cellSize).toInt().coerceIn(0, pixelSize)
          val endY = ((r + quietZone + 1) * cellSize).toInt().coerceIn(0, pixelSize)
          for (y in startY until endY) {
            for (x in startX until endX) {
              bmp.setPixel(x, y, darkColor)
            }
          }
        }
      }
    }
    return bmp
  }
}

@Composable
fun YesQrCodeBox(
  payload: String,
  modifier: Modifier = Modifier,
  boxSize: Dp = 164.dp,
  moduleColor: Color = Color(0xFF0A3D22),
  description: String = "QR Code for $payload",
) {
  val matrix = remember(payload) { QrCodeGenerator.generateMatrix(payload) }
  Box(
    modifier = modifier
      .size(boxSize)
      .clip(RoundedCornerShape(14.dp))
      .background(Color.White)
      .padding(10.dp)
      .semantics { contentDescription = description },
    contentAlignment = Alignment.Center,
  ) {
    Canvas(modifier = Modifier.size(boxSize - 20.dp)) {
      val rows = matrix.size.coerceAtLeast(1)
      val cell = size.width / rows
      for (r in matrix.indices) {
        for (c in matrix[r].indices) {
          if (matrix[r][c]) {
            drawRect(
              color = moduleColor,
              topLeft = Offset(c * cell, r * cell),
              size = Size(cell + 0.45f, cell + 0.45f),
            )
          }
        }
      }
    }
  }
}
