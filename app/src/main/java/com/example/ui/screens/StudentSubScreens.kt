package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Announcement
import com.example.data.model.AttendanceRecord
import com.example.data.model.ExecutiveMember
import com.example.data.model.GalleryItem
import com.example.data.model.IdCardDesignTheme
import com.example.data.model.PaymentAccount
import com.example.data.model.SocietyCertificate
import com.example.data.model.SocietyConstants
import com.example.data.model.SocietyEvent
import com.example.data.model.StudentFeedback
import com.example.data.model.StudentPayment
import com.example.data.model.StudentProfile
import com.example.data.model.UserNotification
import com.example.data.model.normalizeCabinetPositionLabel
import com.example.data.model.toCabinetIdCardProfile
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.FullScreenImagePreviewDialog
import com.example.ui.components.OfficialCertificateCard
import com.example.ui.components.OfficialIdCardPreviewDialog
import com.example.ui.components.QrCameraScannerDialog
import com.example.ui.components.SectionTitleRow
import com.example.ui.components.SocietyImage
import com.example.ui.components.StatMetricCard
import com.example.ui.components.StatusPill
import com.example.ui.components.YesDropdownField
import com.example.util.ImageEncodingUtils
import com.example.util.PdfExportManager
import kotlinx.coroutines.launch

enum class StudentSubPage(val title: String) {
  EXECUTIVE_BODY("Executive Body"),
  FEES_PAYMENTS("Fees & Official Accounts"),
  ATTENDANCE("Attendance & Camera QR Scan"),
  CERTIFICATES("Earned Certificates"),
  GALLERY("Society Gallery"),
  FEEDBACK("Feedback & Suggestions"),
  NOTIFICATIONS("Notifications & Notices"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentSubPageHost(
  page: StudentSubPage,
  student: StudentProfile,
  events: List<SocietyEvent>,
  executiveMembers: List<ExecutiveMember>,
  payments: List<StudentPayment>,
  paymentAccounts: List<PaymentAccount>,
  attendance: List<AttendanceRecord>,
  certificates: List<SocietyCertificate>,
  galleryItems: List<GalleryItem>,
  feedbackList: List<StudentFeedback>,
  notifications: List<UserNotification>,
  announcements: List<Announcement>,
  onBack: () -> Unit,
  onUploadPaymentReceipt: (StudentPayment, String, String, String) -> Unit,
  onDeletePayment: (StudentPayment) -> Unit,
  onQrCheckIn: (String) -> Unit,
  onOpenVerifyCertificate: () -> Unit,
  onSubmitFeedback: (String, String, String, String) -> Unit,
  onDeleteFeedback: (StudentFeedback) -> Unit,
  onMarkNotificationRead: (String) -> Unit,
  onDeleteNotification: (String) -> Unit,
  onMessage: (String) -> Unit,
) {
  BackHandler(onBack = onBack)
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  var pendingCertForPdf by remember { mutableStateOf<SocietyCertificate?>(null) }
  var paymentToDelete by remember { mutableStateOf<StudentPayment?>(null) }
  var feedbackToDelete by remember { mutableStateOf<StudentFeedback?>(null) }
  var notifToDelete by remember { mutableStateOf<UserNotification?>(null) }
  var previewImage by remember { mutableStateOf<Pair<String, String>?>(null) }

  if (previewImage != null) {
    FullScreenImagePreviewDialog(
      title = previewImage!!.first,
      imageSource = previewImage!!.second,
      onDismiss = { previewImage = null },
    )
  }

  if (paymentToDelete != null) {
    val target = paymentToDelete!!
    ConfirmDeleteDialog(
      itemTitle = "${target.purposeTitle} (PKR ${target.amount.toInt()})",
      itemSubtitle = "Delete this unverified fee record?",
      onConfirmDelete = { onDeletePayment(target) },
      onDismiss = { paymentToDelete = null },
    )
  }

  if (feedbackToDelete != null) {
    val target = feedbackToDelete!!
    ConfirmDeleteDialog(
      itemTitle = target.subject,
      itemSubtitle = "Permanently delete this feedback entry?",
      onConfirmDelete = { onDeleteFeedback(target) },
      onDismiss = { feedbackToDelete = null },
    )
  }

  if (notifToDelete != null) {
    val target = notifToDelete!!
    ConfirmDeleteDialog(
      itemTitle = target.title,
      itemSubtitle = "Delete this personal notification?",
      onConfirmDelete = { onDeleteNotification(target.id) },
      onDismiss = { notifToDelete = null },
    )
  }

  val pdfSaveLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.CreateDocument("application/pdf")
  ) { uri: Uri? ->
    val cert = pendingCertForPdf
    if (uri != null && cert != null) {
      PdfExportManager.writeCertificatePdfToUri(context, uri, cert)
        .onSuccess { onMessage("Certificate PDF saved successfully!") }
        .onFailure { onMessage("Failed to save PDF: ${it.localizedMessage}") }
    }
    pendingCertForPdf = null
  }

  var viewingCabinetCardProfile by remember { mutableStateOf<StudentProfile?>(null) }
  var pendingCabinetIdPdfRequest by remember { mutableStateOf<StudentProfile?>(null) }

  val cabinetIdPdfLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.CreateDocument("application/pdf")
  ) { uri: Uri? ->
    val target = pendingCabinetIdPdfRequest
    if (uri != null && target != null) {
      scope.launch {
        PdfExportManager.exportStudentIdCardPdf(context, uri, target)
          .onSuccess { onMessage("Portrait Cabinet ID Card PDF saved for ${target.fullName}!") }
          .onFailure { onMessage("Failed to save ID Card PDF: ${it.localizedMessage}") }
      }
    }
    pendingCabinetIdPdfRequest = null
  }

  if (viewingCabinetCardProfile != null) {
    val cardProfile = viewingCabinetCardProfile!!
    OfficialIdCardPreviewDialog(
      student = cardProfile,
      onDownloadPdf = {
        pendingCabinetIdPdfRequest = cardProfile
        val safeName = cardProfile.fullName.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        cabinetIdPdfLauncher.launch("YES_Cabinet_ID_${safeName}.pdf")
      },
      onDismiss = { viewingCabinetCardProfile = null },
    )
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(page.title) },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("student_subpage_back")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
      )
    },
  ) { innerPadding ->
    when (page) {
      StudentSubPage.EXECUTIVE_BODY -> {
        var wingFilter by remember { mutableStateOf("All Designations") }
        val wingOptions = listOf(
          "All Designations",
          "Convener & Advisory Board",
          "Leadership & Secretaries",
          "Associates & Executive Members",
        )
        val filteredExec = remember(executiveMembers, wingFilter) {
          executiveMembers.filter { m ->
            when (wingFilter) {
              "Convener & Advisory Board" ->
                m.designation.contains("Convener", ignoreCase = true) ||
                  m.designation.contains("Advisory", ignoreCase = true) ||
                  m.designation.contains("Patron", ignoreCase = true)
              "Leadership & Secretaries" ->
                !m.designation.contains("Associate", ignoreCase = true) &&
                  !m.designation.contains("Executive Member", ignoreCase = true)
              "Associates & Executive Members" ->
                m.designation.contains("Associate", ignoreCase = true) ||
                  m.designation.contains("Executive Member", ignoreCase = true)
              else -> true
            }
          }
        }

        val presidentMember = remember(executiveMembers) {
          executiveMembers.firstOrNull {
            normalizeCabinetPositionLabel(it.designation).equals("President", ignoreCase = true)
          }
        }

        LazyColumn(
          modifier = Modifier.fillMaxSize().padding(innerPadding),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          item {
            Card(
              modifier = Modifier.fillMaxWidth().testTag("student_exec_president_card"),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
            ) {
              Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                  Text(
                    text = "Executive Cabinet — President",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                  )
                }
                if (presidentMember != null) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      StatusPill(label = "President")
                      Text(
                        text = presidentMember.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                      )
                      Text(
                        text = "${presidentMember.department} • Tenure ${presidentMember.tenure}",
                        style = MaterialTheme.typography.bodySmall,
                      )
                    }
                    OutlinedButton(
                      onClick = {
                        viewingCabinetCardProfile = presidentMember.toCabinetIdCardProfile(listOf(student))
                      },
                      modifier = Modifier.testTag("view_president_id_card_btn"),
                    ) {
                      Icon(Icons.Filled.Badge, contentDescription = null, modifier = Modifier.size(16.dp))
                      Spacer(Modifier.width(6.dp))
                      Text("View ID Card")
                    }
                  }
                } else {
                  StatusPill(label = "President: Pending Cabinet Publication")
                }
              }
            }
          }

          item {
            YesDropdownField(
              label = "Filter Executive Directory",
              options = wingOptions,
              selectedOption = wingFilter,
              onOptionSelected = { wingFilter = it },
              testTag = "student_exec_filter_dropdown",
            )
          }

          if (filteredExec.isEmpty()) {
            item {
              EmptyStateCard(
                title = "Executive Body Directory",
                subtitle = "Current office bearers will appear here once published by society administration.",
                icon = Icons.Filled.Groups,
              )
            }
          } else {
            items(filteredExec, key = { it.id }) { member ->
              Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
              ) {
                Column(
                  modifier = Modifier.padding(16.dp),
                  verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                  ) {
                    if (member.photoUri.isNotBlank()) {
                      SocietyImage(
                        imageSource = member.photoUri,
                        contentDescription = member.fullName,
                        modifier = Modifier.size(56.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop,
                      )
                    } else {
                      Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(56.dp),
                      ) {
                        Box(contentAlignment = Alignment.Center) {
                          Text(
                            text = member.fullName.take(2).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                          )
                        }
                      }
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                      StatusPill(label = normalizeCabinetPositionLabel(member.designation))
                      Text(member.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                      Text(member.department, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                      Text("Tenure: ${member.tenure}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                  }
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                  ) {
                    OutlinedButton(
                      onClick = {
                        viewingCabinetCardProfile = member.toCabinetIdCardProfile(listOf(student))
                      },
                      modifier = Modifier.testTag("view_cabinet_id_card_${member.id}"),
                    ) {
                      Icon(Icons.Filled.Badge, contentDescription = null, modifier = Modifier.size(16.dp))
                      Spacer(Modifier.width(6.dp))
                      Text("View ID Card")
                    }
                  }
                }
              }
            }
          }
        }
      }

      StudentSubPage.FEES_PAYMENTS -> {
        var selectedPaymentForReceipt by remember { mutableStateOf<StudentPayment?>(null) }
        var statusFilter by remember { mutableStateOf("ALL") }
        val filteredPayments = remember(payments, statusFilter) {
          if (statusFilter == "ALL") payments else payments.filter { it.status == statusFilter }
        }

        LazyColumn(
          modifier = Modifier.fillMaxSize().padding(innerPadding),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          // Official Society Payment Accounts Card
          item {
            SectionTitleRow(title = "Official Society Account Details")
            if (paymentAccounts.isEmpty()) {
              Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
              ) {
                Text(
                  text = "No official bank/wallet accounts published yet. Contact the Finance Secretary for fee payment instructions.",
                  style = MaterialTheme.typography.bodySmall,
                  modifier = Modifier.padding(14.dp),
                )
              }
            } else {
              Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                paymentAccounts.forEach { acc ->
                  Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                  ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Text("${acc.bankOrWalletName} (${acc.accountType})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                      }
                      Text("Account Title: ${acc.accountTitle}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                      Text("Account Number: ${acc.accountNumber}", style = MaterialTheme.typography.bodyMedium)
                      if (acc.ibanOrBranch.isNotBlank()) {
                        Text("IBAN / Branch: ${acc.ibanOrBranch}", style = MaterialTheme.typography.bodySmall)
                      }
                      if (acc.instructions.isNotBlank()) {
                        Text(acc.instructions, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                      }
                    }
                  }
                }
              }
            }
          }

          item {
            YesDropdownField(
              label = "Filter Fee Status",
              options = listOf("ALL", "UNPAID", "PENDING_VERIFICATION", "VERIFIED", "REJECTED"),
              selectedOption = statusFilter,
              onOptionSelected = { statusFilter = it },
              testTag = "student_fee_status_dropdown",
            )
          }

          if (filteredPayments.isEmpty()) {
            item {
              EmptyStateCard(
                title = "No Fee Records Found",
                subtitle = "When you register for a paid YES event or submit a fee receipt, payment verification status appears here.",
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
              )
            }
          } else {
            items(filteredPayments, key = { it.id }) { payment ->
              Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
              ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Text(payment.purposeTitle, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      StatusPill(label = payment.status)
                      if (payment.status != "VERIFIED") {
                        IconButton(
                          onClick = { paymentToDelete = payment },
                          modifier = Modifier.testTag("delete_student_payment_${payment.id}"),
                        ) {
                          Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete Payment Record",
                            tint = MaterialTheme.colorScheme.error,
                          )
                        }
                      }
                    }
                  }
                  Text("Amount: PKR ${payment.amount.toInt()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                  if (payment.transactionRef.isNotBlank()) {
                    Text("Reference: ${payment.transactionRef} • Date: ${payment.paymentDate}", style = MaterialTheme.typography.bodySmall)
                  }
                  if (payment.receiptUri.isNotBlank()) {
                    SocietyImage(
                      imageSource = payment.receiptUri,
                      contentDescription = "Payment Receipt",
                      modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { previewImage = payment.purposeTitle to payment.receiptUri },
                      contentScale = ContentScale.Crop,
                    )
                  }
                  if (payment.status != "VERIFIED") {
                    OutlinedButton(
                      onClick = { selectedPaymentForReceipt = payment },
                      modifier = Modifier.testTag("upload_receipt_${payment.id}"),
                    ) {
                      Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                      Spacer(Modifier.width(6.dp))
                      Text("Submit Payment Receipt / Reference")
                    }
                  }
                }
              }
            }
          }
        }

        if (selectedPaymentForReceipt != null) {
          val target = selectedPaymentForReceipt!!
          var txRef by remember(target) { mutableStateOf(target.transactionRef) }
          var payDate by remember(target) { mutableStateOf(target.paymentDate.ifBlank { "2026-11-01" }) }
          var receiptUri by remember(target) { mutableStateOf(target.receiptUri) }
          val receiptPicker = rememberLauncherForActivityResult(
            ActivityResultContracts.PickVisualMedia()
          ) { uri ->
            if (uri != null) {
              receiptUri = ImageEncodingUtils.encodeUriToDataUrl(context, uri, maxDimension = 640, quality = 70)
                ?: uri.toString()
            }
          }

          AlertDialog(
            onDismissRequest = { selectedPaymentForReceipt = null },
            title = { Text("Upload Fee Receipt") },
            text = {
              Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("${target.purposeTitle} (PKR ${target.amount.toInt()})", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                  value = txRef,
                  onValueChange = { txRef = it },
                  label = { Text("Bank / JazzCash / EasyPaisa Ref *") },
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                  value = payDate,
                  onValueChange = { payDate = it },
                  label = { Text("Payment Date (YYYY-MM-DD)") },
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth(),
                )
                OutlinedButton(
                  onClick = { receiptPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  Text(if (receiptUri.isNotBlank()) "Receipt Image Attached ✓" else "Attach Receipt Screenshot")
                }
              }
            },
            confirmButton = {
              Button(
                onClick = {
                  onUploadPaymentReceipt(target, payDate, txRef, receiptUri)
                  selectedPaymentForReceipt = null
                },
                enabled = txRef.isNotBlank(),
              ) {
                Text("Submit")
              }
            },
            dismissButton = {
              TextButton(onClick = { selectedPaymentForReceipt = null }) { Text("Cancel") }
            },
          )
        }
      }

      StudentSubPage.ATTENDANCE -> {
        var qrInput by remember { mutableStateOf("") }
        var showQrCameraScanner by remember { mutableStateOf(false) }
        var statusFilter by remember { mutableStateOf("ALL") }

        val presentCount = attendance.count { it.status == "PRESENT" }
        val rate = if (attendance.isNotEmpty()) (presentCount * 100) / attendance.size else 100
        val filteredAttendance = remember(attendance, statusFilter) {
          if (statusFilter == "ALL") attendance else attendance.filter { it.status == statusFilter }
        }

        if (showQrCameraScanner) {
          QrCameraScannerDialog(
            title = "Scan Event QR Code",
            subtitle = "Point your camera at the official Event QR Code displayed by the Administrator to mark attendance.",
            onQrCodeScanned = { scannedValue ->
              showQrCameraScanner = false
              onQrCheckIn(scannedValue)
            },
            onDismiss = { showQrCameraScanner = false },
          )
        }

        LazyColumn(
          modifier = Modifier.fillMaxSize().padding(innerPadding),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
              StatMetricCard(
                title = "Events Attended",
                value = "$presentCount",
                icon = Icons.Filled.CheckCircle,
                modifier = Modifier.weight(1f),
              )
              StatMetricCard(
                title = "Attendance Rate",
                value = "$rate%",
                icon = Icons.Filled.Verified,
                modifier = Modifier.weight(1f),
              )
            }
          }

          item {
            Card(
              modifier = Modifier.fillMaxWidth(),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            ) {
              Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Icon(Icons.Filled.QrCodeScanner, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                  Text("Camera QR Scan & Event Check-In", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Text(
                  "Open the live camera scanner to capture the Event QR Code at the venue, or enter the QR token manually below.",
                  style = MaterialTheme.typography.bodySmall,
                )
                Button(
                  onClick = { showQrCameraScanner = true },
                  modifier = Modifier.fillMaxWidth().height(50.dp).testTag("open_student_qr_camera_btn"),
                ) {
                  Icon(Icons.Filled.CameraAlt, contentDescription = null)
                  Spacer(Modifier.width(8.dp))
                  Text("Open Camera to Scan Event QR Code", fontWeight = FontWeight.Bold)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                  OutlinedTextField(
                    value = qrInput,
                    onValueChange = { qrInput = it },
                    label = { Text("Or Enter Event QR Token") },
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("event_qr_checkin_input"),
                  )
                  OutlinedButton(
                    onClick = {
                      onQrCheckIn(qrInput)
                      qrInput = ""
                    },
                    enabled = qrInput.isNotBlank(),
                    modifier = Modifier.testTag("event_qr_checkin_button"),
                  ) {
                    Text("Check In")
                  }
                }
              }
            }
          }

          item {
            YesDropdownField(
              label = "Filter Attendance Status",
              options = listOf("ALL", "PRESENT", "ABSENT", "EXCUSED"),
              selectedOption = statusFilter,
              onOptionSelected = { statusFilter = it },
              testTag = "student_attendance_filter_dropdown",
            )
          }

          if (filteredAttendance.isEmpty()) {
            item {
              EmptyStateCard(
                title = "No Attendance Records Yet",
                subtitle = "Scan Event QR codes at YES seminars, field trips, or society meetings to build your participation history.",
              )
            }
          } else {
            items(filteredAttendance, key = { it.id }) { rec ->
              Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              ) {
                Row(
                  modifier = Modifier.padding(16.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(rec.eventTitle, style = MaterialTheme.typography.titleMedium)
                    Text("Date: ${rec.eventDate} • Method: ${rec.method}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  }
                  StatusPill(label = rec.status)
                }
              }
            }
          }
        }
      }

      StudentSubPage.CERTIFICATES -> {
        var certTypeFilter by remember { mutableStateOf("ALL") }
        val filteredCerts = remember(certificates, certTypeFilter) {
          if (certTypeFilter == "ALL") certificates else certificates.filter { it.certificateType == certTypeFilter }
        }

        LazyColumn(
          modifier = Modifier.fillMaxSize().padding(innerPadding),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              YesDropdownField(
                label = "Certificate Type",
                options = listOf("ALL", "PARTICIPATION", "ORGANIZER", "MERIT", "MEMBERSHIP"),
                selectedOption = certTypeFilter,
                onOptionSelected = { certTypeFilter = it },
                modifier = Modifier.weight(1f),
                testTag = "student_cert_filter_dropdown",
              )
            }
          }

          item {
            OutlinedButton(
              onClick = onOpenVerifyCertificate,
              modifier = Modifier.fillMaxWidth().testTag("student_verify_certificate_btn"),
            ) {
              Icon(Icons.Filled.Verified, contentDescription = null)
              Spacer(Modifier.width(8.dp))
              Text("Verify Any Certificate by ID / QR Code")
            }
          }

          if (filteredCerts.isEmpty()) {
            item {
              EmptyStateCard(
                title = "No Certificates Issued Yet",
                subtitle = "Earned participation and merit certificates featuring UAF & YES crests will appear here ready for PDF download.",
                icon = Icons.Outlined.WorkspacePremium,
              )
            }
          } else {
            items(filteredCerts, key = { it.id }) { cert ->
              OfficialCertificateCard(
                certificate = cert,
                onDownloadPdf = {
                  pendingCertForPdf = cert
                  pdfSaveLauncher.launch("${cert.certificateCode}.pdf")
                },
              )
            }
          }
        }
      }

      StudentSubPage.GALLERY -> {
        var selectedAlbum by remember { mutableStateOf("All") }
        val filtered = remember(galleryItems, selectedAlbum) {
          if (selectedAlbum == "All") galleryItems else galleryItems.filter { it.albumCategory == selectedAlbum }
        }

        LazyColumn(
          modifier = Modifier.fillMaxSize().padding(innerPadding),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          item {
            YesDropdownField(
              label = "Select Gallery Album",
              options = listOf("All") + SocietyConstants.GALLERY_ALBUMS,
              selectedOption = selectedAlbum,
              onOptionSelected = { selectedAlbum = it },
              testTag = "student_gallery_album_dropdown",
            )
          }

          if (filtered.isEmpty()) {
            item {
              EmptyStateCard(
                title = "No Gallery Photos Yet",
                subtitle = "Approved photographs from Annual Dinner, Sports Gala, Study Tours, and Workshops appear here.",
                icon = Icons.Filled.PhotoLibrary,
              )
            }
          } else {
            items(filtered, key = { it.id }) { item ->
              val gradient = when (item.accentTheme) {
                "GOLD" -> listOf(Color(0xFF6B3A00), Color(0xFFB46914))
                "FOREST" -> listOf(Color(0xFF062916), Color(0xFF1A7B49))
                else -> listOf(Color(0xFF0E5A34), Color(0xFF2E8B57))
              }
              Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              ) {
                Column {
                  if (item.imageUri.isNotBlank()) {
                    SocietyImage(
                      imageSource = item.imageUri,
                      contentDescription = item.title,
                      modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clickable { previewImage = item.title to item.imageUri },
                      contentScale = ContentScale.Crop,
                    )
                  } else {
                    Box(
                      modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(Brush.linearGradient(gradient))
                        .padding(16.dp),
                      contentAlignment = Alignment.BottomStart,
                    ) {
                      Column {
                        StatusPill(label = item.albumCategory)
                        Spacer(Modifier.height(6.dp))
                        Text(item.title, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                      }
                    }
                  }
                  Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (item.imageUri.isNotBlank()) {
                      StatusPill(label = item.albumCategory)
                      Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    if (item.caption.isNotBlank()) {
                      Text(item.caption, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text("Event Date: ${item.eventDate}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  }
                }
              }
            }
          }
        }
      }

      StudentSubPage.FEEDBACK -> {
        var category by remember { mutableStateOf("SUGGESTION") }
        var subject by remember { mutableStateOf("") }
        var message by remember { mutableStateOf("") }
        var relatedEvent by remember { mutableStateOf("None / General") }
        val eventChoices = remember(events) { listOf("None / General") + events.map { it.title } }

        LazyColumn(
          modifier = Modifier.fillMaxSize().padding(innerPadding),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          item {
            Card(
              modifier = Modifier.fillMaxWidth(),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
            ) {
              Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Submit Suggestion, Feedback or Complaint", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                YesDropdownField(
                  label = "Submission Category",
                  options = listOf("SUGGESTION", "EVENT_FEEDBACK", "COMPLAINT", "GENERAL_MESSAGE"),
                  selectedOption = category,
                  onOptionSelected = { category = it },
                  optionLabel = { it.replace("_", " ") },
                  testTag = "feedback_category_dropdown",
                )
                YesDropdownField(
                  label = "Related Event (Optional)",
                  options = eventChoices,
                  selectedOption = relatedEvent,
                  onOptionSelected = { relatedEvent = it },
                  testTag = "feedback_event_dropdown",
                )
                OutlinedTextField(
                  value = subject,
                  onValueChange = { subject = it },
                  label = { Text("Subject *") },
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth().testTag("feedback_subject_input"),
                )
                OutlinedTextField(
                  value = message,
                  onValueChange = { message = it },
                  label = { Text("Details / Message *") },
                  minLines = 3,
                  modifier = Modifier.fillMaxWidth().testTag("feedback_message_input"),
                )
                Button(
                  onClick = {
                    val eventPayload = if (relatedEvent == "None / General") "" else relatedEvent
                    onSubmitFeedback(category, subject, message, eventPayload)
                    subject = ""
                    message = ""
                    relatedEvent = "None / General"
                  },
                  enabled = subject.trim().length >= 3 && message.trim().length >= 5,
                  modifier = Modifier.fillMaxWidth().testTag("submit_feedback_button"),
                ) {
                  Text("Send to YES Administration")
                }
              }
            }
          }

          if (feedbackList.isEmpty()) {
            item {
              EmptyStateCard(
                title = "No Previous Submissions",
                subtitle = "Your feedback status and responses from YES office bearers will appear here.",
                icon = Icons.Outlined.Feedback,
              )
            }
          } else {
            items(feedbackList, key = { it.id }) { fb ->
              Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
              ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Text(fb.subject, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      StatusPill(label = fb.status)
                      IconButton(
                        onClick = { feedbackToDelete = fb },
                        modifier = Modifier.testTag("delete_my_feedback_${fb.id}"),
                      ) {
                        Icon(
                          imageVector = Icons.Filled.Delete,
                          contentDescription = "Delete Feedback",
                          tint = MaterialTheme.colorScheme.error,
                        )
                      }
                    }
                  }
                  Text(fb.message, style = MaterialTheme.typography.bodyMedium)
                  if (fb.adminResponse.isNotBlank()) {
                    Surface(
                      color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                      shape = RoundedCornerShape(10.dp),
                      modifier = Modifier.fillMaxWidth(),
                    ) {
                      Text(
                        text = "Admin Response: ${fb.adminResponse}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp),
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }

      StudentSubPage.NOTIFICATIONS -> {
        LazyColumn(
          modifier = Modifier.fillMaxSize().padding(innerPadding),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          item {
            SectionTitleRow(title = "Personal Alerts & Updates")
          }
          if (notifications.isEmpty()) {
            item {
              EmptyStateCard(
                title = "All Caught Up",
                subtitle = "Membership approvals, payment confirmations, and certificate alerts appear here.",
                icon = Icons.Filled.Notifications,
              )
            }
          } else {
            items(notifications, key = { it.id }) { notif ->
              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { if (!notif.read) onMarkNotificationRead(notif.id) },
                colors = CardDefaults.cardColors(
                  containerColor = if (notif.read) MaterialTheme.colorScheme.surface
                  else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
              ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                      StatusPill(label = notif.type)
                      if (!notif.read) {
                        Text("NEW • Tap to mark read", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                      }
                    }
                    IconButton(
                      onClick = { notifToDelete = notif },
                      modifier = Modifier.testTag("delete_notification_${notif.id}"),
                    ) {
                      Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete Notification",
                        tint = MaterialTheme.colorScheme.error,
                      )
                    }
                  }
                  Text(notif.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                  Text(notif.body, style = MaterialTheme.typography.bodyMedium)
                }
              }
            }
          }

          item {
            Spacer(Modifier.height(4.dp))
            SectionTitleRow(title = "Society Announcements & Deadlines")
          }
          items(announcements, key = { "ann_${it.id}" }) { ann ->
            Card(
              modifier = Modifier.fillMaxWidth(),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
            ) {
              Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  StatusPill(label = ann.category)
                  StatusPill(label = ann.priority)
                }
                Text(ann.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(ann.message, style = MaterialTheme.typography.bodyMedium)
              }
            }
          }
        }
      }
    }
  }
}
