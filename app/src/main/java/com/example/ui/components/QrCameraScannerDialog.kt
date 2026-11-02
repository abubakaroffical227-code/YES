package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun QrCameraScannerDialog(
  title: String = "Scan QR Code",
  subtitle: String = "Align the QR code inside the camera frame to mark attendance automatically.",
  onQrCodeScanned: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  var hasCameraPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED
    )
  }
  var permissionDeniedMessage by remember { mutableStateOf(false) }
  var cameraError by remember { mutableStateOf<String?>(null) }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
  ) { granted ->
    hasCameraPermission = granted
    permissionDeniedMessage = !granted
  }

  LaunchedEffect(Unit) {
    if (!hasCameraPermission) {
      permissionLauncher.launch(Manifest.permission.CAMERA)
    }
  }

  val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
  val scannedOnce = remember { AtomicBoolean(false) }

  DisposableEffect(Unit) {
    onDispose {
      analysisExecutor.shutdown()
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Icon(
            Icons.Filled.QrCodeScanner,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
          )
          Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        IconButton(onClick = onDismiss) {
          Icon(Icons.Filled.Close, contentDescription = "Close camera scanner")
        }
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (hasCameraPermission) {
          if (cameraError != null) {
            Card(
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text(
                text = cameraError ?: "Camera unavailable on this device.",
                modifier = Modifier.padding(12.dp),
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodySmall,
              )
            }
          } else {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.Black)
                .testTag("camera_qr_preview_box"),
              contentAlignment = Alignment.Center,
            ) {
              AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                  val previewView = PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                  }
                  val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                  cameraProviderFuture.addListener(
                    {
                      runCatching {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                          it.surfaceProvider = previewView.surfaceProvider
                        }
                        val reader = MultiFormatReader().apply {
                          setHints(
                            mapOf(
                              DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
                              DecodeHintType.TRY_HARDER to true,
                              DecodeHintType.CHARACTER_SET to "UTF-8",
                            )
                          )
                        }
                        val imageAnalysis = ImageAnalysis.Builder()
                          .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                          .build()
                          .also { analysis ->
                            analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                              val decoded = decodeQrFromImageProxy(imageProxy, reader)
                              if (!decoded.isNullOrBlank() && scannedOnce.compareAndSet(false, true)) {
                                ContextCompat.getMainExecutor(ctx).execute {
                                  onQrCodeScanned(decoded.trim())
                                }
                              }
                            }
                          }

                        cameraProvider.unbindAll()
                        val selector = when {
                          cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ->
                            CameraSelector.DEFAULT_BACK_CAMERA
                          cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ->
                            CameraSelector.DEFAULT_FRONT_CAMERA
                          else -> CameraSelector.DEFAULT_BACK_CAMERA
                        }
                        cameraProvider.bindToLifecycle(
                          lifecycleOwner,
                          selector,
                          preview,
                          imageAnalysis,
                        )
                      }.onFailure { err ->
                        cameraError = "Unable to start camera preview: ${err.localizedMessage ?: "No camera hardware found"}"
                      }
                    },
                    ContextCompat.getMainExecutor(ctx),
                  )
                  previewView
                },
              )

              // Crisp emerald targeting reticle overlay
              Box(
                modifier = Modifier
                  .size(190.dp)
                  .border(
                    BorderStroke(2.5.dp, Color(0xFF4ADE80)),
                    RoundedCornerShape(18.dp),
                  ),
              )
              Surface(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp),
                shape = RoundedCornerShape(50),
                color = Color.Black.copy(alpha = 0.65f),
              ) {
                Text(
                  text = "Point camera at QR code",
                  color = Color.White,
                  style = MaterialTheme.typography.labelSmall,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                )
              }
            }
          }
        } else {
          Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Icon(
                Icons.Filled.CameraAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(34.dp),
              )
              Text(
                text = if (permissionDeniedMessage) {
                  "Camera permission is required to scan QR codes with your camera."
                } else {
                  "Requesting camera access for live QR scanning..."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Button(
                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                modifier = Modifier.testTag("grant_camera_permission_btn"),
              ) {
                Text("Allow Camera Access")
              }
            }
          }
        }
      }
    },
    confirmButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Cancel")
      }
    },
  )
}

private fun decodeQrFromImageProxy(
  imageProxy: ImageProxy,
  reader: MultiFormatReader,
): String? {
  try {
    val plane = imageProxy.planes.firstOrNull() ?: return null
    val buffer = plane.buffer
    val width = imageProxy.width
    val height = imageProxy.height
    if (width <= 0 || height <= 0) return null

    val rowStride = plane.rowStride
    val pixelStride = plane.pixelStride
    val yuv = ByteArray(width * height)
    buffer.rewind()
    if (rowStride == width && pixelStride == 1) {
      buffer.get(yuv, 0, width * height)
    } else {
      val rowBuffer = ByteArray(rowStride)
      var offset = 0
      for (row in 0 until height) {
        val bytesToRead = if (row == height - 1) {
          buffer.remaining().coerceAtMost(rowStride)
        } else {
          rowStride.coerceAtMost(buffer.remaining())
        }
        if (bytesToRead <= 0) break
        buffer.get(rowBuffer, 0, bytesToRead)
        for (col in 0 until width) {
          val idx = col * pixelStride
          if (idx < bytesToRead && offset < yuv.size) {
            yuv[offset++] = rowBuffer[idx]
          }
        }
      }
    }

    // 1. Try upright frame
    runCatching {
      val source = PlanarYUVLuminanceSource(yuv, width, height, 0, 0, width, height, false)
      val bitmap = BinaryBitmap(HybridBinarizer(source))
      return reader.decodeWithState(bitmap).text
    }
    reader.reset()

    // 2. Try 90-degree rotated frame (portrait camera sensor alignment)
    val rotated = ByteArray(width * height)
    for (y in 0 until height) {
      for (x in 0 until width) {
        rotated[x * height + height - y - 1] = yuv[y * width + x]
      }
    }
    runCatching {
      val rotSource = PlanarYUVLuminanceSource(rotated, height, width, 0, 0, height, width, false)
      val rotBitmap = BinaryBitmap(HybridBinarizer(rotSource))
      return reader.decodeWithState(rotBitmap).text
    }
    reader.reset()
    return null
  } catch (_: Exception) {
    return null
  } finally {
    imageProxy.close()
  }
}
