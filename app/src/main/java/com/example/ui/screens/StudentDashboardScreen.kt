package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Announcement
import com.example.data.model.AttendanceRecord
import com.example.data.model.EventRegistration
import com.example.data.model.ExecutiveMember
import com.example.data.model.GalleryItem
import com.example.data.model.IdCardDesignTheme
import com.example.data.model.MembershipApplication
import com.example.data.model.PaymentAccount
import com.example.data.model.SocietyCertificate
import com.example.data.model.SocietyConstants
import com.example.data.model.SocietyEvent
import com.example.data.model.StudentFeedback
import com.example.data.model.StudentPayment
import com.example.data.model.StudentProfile
import com.example.data.model.UserNotification
import com.example.data.model.allocatedRoleOrNull
import com.example.data.model.hasAllocatedRole
import com.example.data.model.isCabinetMember
import com.example.data.model.isIdCardActive
import com.example.data.model.isVerifiedMember
import com.example.ui.components.AppFontSizeSelector
import com.example.ui.components.CertificateVerificationDialog
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.DigitalMembershipCard
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.FullScreenImagePreviewDialog
import com.example.ui.components.OfficialIdCardPreviewDialog
import com.example.ui.components.QrCameraScannerDialog
import com.example.ui.components.SectionTitleRow
import com.example.ui.components.SocietyImage
import com.example.ui.components.StatusPill
import com.example.ui.components.UafLogoEmblem
import com.example.ui.components.YesDropdownField
import com.example.ui.components.YesLogoEmblem
import com.example.ui.theme.AppFontSize
import com.example.ui.theme.YesHeroEnd
import com.example.ui.theme.YesHeroStart
import com.example.util.ImageEncodingUtils
import com.example.util.PdfExportManager
import kotlinx.coroutines.launch

private enum class StudentNavTab(
  val label: String,
  val filledIcon: ImageVector,
  val outlinedIcon: ImageVector,
  val tag: String,
) {
  HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, "student_nav_home"),
  CALENDAR("Calendar", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth, "student_nav_calendar"),
  EVENTS("Events", Icons.Filled.Event, Icons.Outlined.Event, "student_nav_events"),
  MEMBERSHIP("ID Card", Icons.Filled.Badge, Icons.Outlined.Badge, "student_nav_membership"),
  PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person, "student_nav_profile"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboardScreen(
  student: StudentProfile,
  events: List<SocietyEvent>,
  registrations: List<EventRegistration>,
  membershipApps: List<MembershipApplication>,
  announcements: List<Announcement>,
  notifications: List<UserNotification>,
  executiveBody: List<ExecutiveMember>,
  payments: List<StudentPayment>,
  paymentAccounts: List<PaymentAccount>,
  attendance: List<AttendanceRecord>,
  certificates: List<SocietyCertificate>,
  galleryItems: List<GalleryItem>,
  feedbackList: List<StudentFeedback>,
  verifiedCertificate: SocietyCertificate?,
  verificationChecked: Boolean,
  appFontSize: AppFontSize,
  onAppFontSizeChange: (AppFontSize) -> Unit,
  snackbarHostState: SnackbarHostState,
  onMessage: (String) -> Unit,
  onApplyMembership: (String) -> Unit,
  onDeleteMembershipApplication: (MembershipApplication) -> Unit,
  onRegisterEvent: (SocietyEvent, String, String) -> Unit,
  onCancelRegistration: (EventRegistration, SocietyEvent) -> Unit,
  onUploadPaymentReceipt: (StudentPayment, String, String, String) -> Unit,
  onDeletePayment: (StudentPayment) -> Unit,
  onQrCheckIn: (String) -> Unit,
  onVerifyCertificate: (String) -> Unit,
  onClearVerification: () -> Unit,
  onSubmitFeedback: (String, String, String, String) -> Unit,
  onDeleteFeedback: (StudentFeedback) -> Unit,
  onMarkNotificationRead: (String) -> Unit,
  onDeleteNotification: (String) -> Unit,
  onUpdateProfile: (String, String, String, String, String, String, String, String) -> Unit,
  onSignOut: () -> Unit,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  var currentTab by rememberSaveable { mutableStateOf(StudentNavTab.HOME) }
  var activeSubPage by rememberSaveable { mutableStateOf<StudentSubPage?>(null) }
  var showVerifyDialog by remember { mutableStateOf(false) }
  var eventToRegister by remember { mutableStateOf<SocietyEvent?>(null) }
  val idCardPdfLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.CreateDocument("application/pdf")
  ) { uri: Uri? ->
    if (uri != null) {
      scope.launch {
        PdfExportManager.exportStudentIdCardPdf(context, uri, student)
          .onSuccess { onMessage("Official YES ID Card PDF saved!") }
          .onFailure { onMessage("Failed to save ID Card PDF: ${it.localizedMessage}") }
      }
    }
  }

  val unreadCount = notifications.count { !it.read }

  if (showVerifyDialog) {
    CertificateVerificationDialog(
      verifiedCertificate = verifiedCertificate,
      checked = verificationChecked,
      onVerify = onVerifyCertificate,
      onDismiss = {
        showVerifyDialog = false
        onClearVerification()
      },
    )
  }

  if (eventToRegister != null) {
    val ev = eventToRegister!!
    var txRef by remember(ev) { mutableStateOf("") }
    var receiptUri by remember(ev) { mutableStateOf("") }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
      if (uri != null) {
        receiptUri = ImageEncodingUtils.encodeUriToDataUrl(context, uri, maxDimension = 640, quality = 70)
          ?: uri.toString()
      }
    }
    AlertDialog(
      onDismissRequest = { eventToRegister = null },
      title = { Text("Separate Event Registration") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(ev.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Text("Category: ${ev.category} • Date: ${ev.eventDate}", style = MaterialTheme.typography.bodySmall)
          Text("Venue: ${ev.venue}", style = MaterialTheme.typography.bodySmall)
          Text(
            text = if (ev.registrationFee <= 0.0) "Registration Fee: FREE" else "Registration Fee: PKR ${ev.registrationFee.toInt()}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
          )
          if (ev.registrationFee > 0.0) {
            if (paymentAccounts.isNotEmpty()) {
              Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp),
              ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Filled.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Text("Official Society Account Details", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                  }
                  paymentAccounts.take(2).forEach { acc ->
                    Text(
                      "${acc.bankOrWalletName} (${acc.accountType}): ${acc.accountNumber} — ${acc.accountTitle}",
                      style = MaterialTheme.typography.bodySmall,
                    )
                  }
                }
              }
            }
            OutlinedTextField(
              value = txRef,
              onValueChange = { txRef = it },
              label = { Text("Transaction Ref (Optional now, or submit later)") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
            )
            OutlinedButton(
              onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text(if (receiptUri.isNotBlank()) "Payment Receipt Attached ✓" else "Attach Fee Receipt Image (Optional)")
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onRegisterEvent(ev, txRef, receiptUri)
            eventToRegister = null
          },
          modifier = Modifier.testTag("confirm_event_registration_btn"),
        ) {
          Text("Confirm Registration")
        }
      },
      dismissButton = {
        TextButton(onClick = { eventToRegister = null }) { Text("Cancel") }
      },
    )
  }

  if (activeSubPage != null) {
    StudentSubPageHost(
      page = activeSubPage!!,
      student = student,
      events = events,
      executiveMembers = executiveBody,
      payments = payments,
      paymentAccounts = paymentAccounts,
      attendance = attendance,
      certificates = certificates,
      galleryItems = galleryItems,
      feedbackList = feedbackList,
      notifications = notifications,
      announcements = announcements,
      onBack = { activeSubPage = null },
      onUploadPaymentReceipt = onUploadPaymentReceipt,
      onDeletePayment = onDeletePayment,
      onQrCheckIn = onQrCheckIn,
      onOpenVerifyCertificate = { showVerifyDialog = true },
      onSubmitFeedback = onSubmitFeedback,
      onDeleteFeedback = onDeleteFeedback,
      onMarkNotificationRead = onMarkNotificationRead,
      onDeleteNotification = onDeleteNotification,
      onMessage = onMessage,
    )
    return
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val isExpanded = maxWidth >= 700.dp

    Scaffold(
      snackbarHost = { SnackbarHost(snackbarHostState) },
      topBar = {
        TopAppBar(
          title = {
            Row(
              modifier = Modifier.fillMaxWidth().padding(end = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f),
              ) {
                YesLogoEmblem(size = 36.dp)
                Column {
                  Text("YES Student Portal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                  Text("Department of Entomology", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
              }
              UafLogoEmblem(size = 36.dp)
            }
          },
          actions = {
            IconButton(
              onClick = { activeSubPage = StudentSubPage.NOTIFICATIONS },
              modifier = Modifier.testTag("student_nav_notifications"),
            ) {
              if (unreadCount > 0) {
                BadgedBox(badge = { Badge { Text("$unreadCount") } }) {
                  Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                }
              } else {
                Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
              }
            }
          },
        )
      },
      bottomBar = {
        if (!isExpanded) {
          NavigationBar {
            StudentNavTab.entries.forEach { tab ->
              val selected = currentTab == tab
              NavigationBarItem(
                selected = selected,
                onClick = { currentTab = tab },
                icon = { Icon(if (selected) tab.filledIcon else tab.outlinedIcon, contentDescription = tab.label) },
                label = { Text(tab.label, maxLines = 1) },
                modifier = Modifier.testTag(tab.tag),
              )
            }
          }
        }
      },
    ) { innerPadding ->
      Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        if (isExpanded) {
          NavigationRail(modifier = Modifier.fillMaxHeight()) {
            StudentNavTab.entries.forEach { tab ->
              val selected = currentTab == tab
              NavigationRailItem(
                selected = selected,
                onClick = { currentTab = tab },
                icon = { Icon(if (selected) tab.filledIcon else tab.outlinedIcon, contentDescription = tab.label) },
                label = { Text(tab.label) },
                modifier = Modifier.testTag(tab.tag),
              )
            }
          }
        }

        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
          when (currentTab) {
            StudentNavTab.HOME -> StudentHomeTabContent(
              student = student,
              events = events,
              registrations = registrations,
              announcements = announcements,
              attendance = attendance,
              onQrCheckIn = onQrCheckIn,
              onOpenSubPage = { activeSubPage = it },
              onOpenCalendarTab = { currentTab = StudentNavTab.CALENDAR },
              onOpenEventsTab = { currentTab = StudentNavTab.EVENTS },
              onOpenMembershipTab = { currentTab = StudentNavTab.MEMBERSHIP },
              onQuickRegister = { eventToRegister = it },
            )
            StudentNavTab.CALENDAR -> StudentEventCalendarContent(
              events = events,
              registrations = registrations,
              onRegisterClick = { eventToRegister = it },
              onCancelRegistration = onCancelRegistration,
            )
            StudentNavTab.EVENTS -> StudentEventsTabContent(
              events = events,
              registrations = registrations,
              onRegisterClick = { eventToRegister = it },
              onCancelRegistration = onCancelRegistration,
            )
            StudentNavTab.MEMBERSHIP -> StudentMembershipTabContent(
              student = student,
              applications = membershipApps,
              onApply = onApplyMembership,
              onDeleteApplication = onDeleteMembershipApplication,
              onDownloadIdCardPdf = {
                val safeReg = student.registrationNumber.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
                idCardPdfLauncher.launch("YES_ID_Card_${safeReg.ifBlank { "Member" }}.pdf")
              },
            )
            StudentNavTab.PROFILE -> StudentProfileTabContent(
              student = student,
              appFontSize = appFontSize,
              onAppFontSizeChange = onAppFontSizeChange,
              onUpdateProfile = onUpdateProfile,
              onDownloadIdCardPdf = {
                val safeReg = student.registrationNumber.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
                idCardPdfLauncher.launch("YES_ID_Card_${safeReg.ifBlank { "Member" }}.pdf")
              },
              onSignOut = onSignOut,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun StudentHomeTabContent(
  student: StudentProfile,
  events: List<SocietyEvent>,
  registrations: List<EventRegistration>,
  announcements: List<Announcement>,
  attendance: List<AttendanceRecord>,
  onQrCheckIn: (String) -> Unit,
  onOpenSubPage: (StudentSubPage) -> Unit,
  onOpenCalendarTab: () -> Unit,
  onOpenEventsTab: () -> Unit,
  onOpenMembershipTab: () -> Unit,
  onQuickRegister: (SocietyEvent) -> Unit,
) {
  val registeredEventIds = remember(registrations) { registrations.map { it.eventId }.toSet() }
  val upcomingEvents = remember(events) { events.filter { it.upcoming }.take(4) }
  val presentCount = remember(attendance) { attendance.count { it.status == "PRESENT" } }
  val isCabinetOfficer = student.isCabinetMember()
  var showMainQrCameraScanner by remember { mutableStateOf(false) }
  var manualQrToken by remember { mutableStateOf("") }
  var posterPreview by remember { mutableStateOf<Pair<String, String>?>(null) }

  if (showMainQrCameraScanner) {
    QrCameraScannerDialog(
      title = if (isCabinetOfficer) "Cabinet & Executive QR Attendance" else "Scan Event QR Code",
      subtitle = "Point your camera at the official Event QR Code to mark your society attendance.",
      onQrCodeScanned = { scannedCode ->
        showMainQrCameraScanner = false
        onQrCheckIn(scannedCode)
      },
      onDismiss = { showMainQrCameraScanner = false },
    )
  }

  if (posterPreview != null) {
    FullScreenImagePreviewDialog(
      title = posterPreview!!.first,
      imageSource = posterPreview!!.second,
      onDismiss = { posterPreview = null },
    )
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    // Welcome Hero Banner: Society Logo on LEFT side, University Logo on RIGHT side
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.linearGradient(
                listOf(YesHeroStart, YesHeroEnd)
              )
            )
            .padding(20.dp),
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              YesLogoEmblem(size = 46.dp)
              Column(
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
              ) {
                Text(
                  text = "YOUNG ENTOMOLOGIST SOCIETY",
                  style = MaterialTheme.typography.labelLarge,
                  color = Color.White,
                  fontWeight = FontWeight.ExtraBold,
                )
                Text(
                  text = "Department of Entomology • UAF",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color(0xFFFFE5B4),
                )
              }
              UafLogoEmblem(size = 46.dp)
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = student.fullName,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
              )
              StatusPill(label = if (student.isVerifiedMember()) "VERIFIED" else student.membershipStatus)
            }
            Text(
              text = "${student.registrationNumber} • ${student.degreeLevel} • ${student.department}",
              style = MaterialTheme.typography.bodySmall,
              color = Color.White.copy(alpha = 0.88f),
            )

            if (!student.isVerifiedMember()) {
              Button(
                onClick = onOpenMembershipTab,
                colors = ButtonDefaults.buttonColors(
                  containerColor = Color(0xFFFFE5C4),
                  contentColor = Color(0xFF3A1E00),
                ),
              ) {
                Text(
                  if (student.membershipStatus == "PENDING") "View Pending Verification Status"
                  else "Apply for Society Verification"
                )
              }
            } else if (student.hasAllocatedRole()) {
              Text(
                text = "Verified Role: ${student.allocatedRoleOrNull()!!}",
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFFFFDFB3),
              )
            }
          }
        }
      }
    }

    // Section Title: Society Overview
    if (student.isCabinetMember()) {
      item {
        SectionTitleRow(title = "Society Overview")
      }
      // Society Overview Options: QR Camera, Attendance & Check-In, Event Calendar, ID Card
      item {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
          // 1. QR Camera Scanner
          Card(
            modifier = Modifier
              .weight(1f)
              .clickable { showMainQrCameraScanner = true }
              .testTag("student_overview_qr_camera_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.40f)),
          ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(38.dp),
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(Icons.Filled.CameraAlt, contentDescription = "QR Camera Scanner", tint = MaterialTheme.colorScheme.onPrimary)
                }
              }
              Text("QR Camera Scanner", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
              Text("Scan event QR codes with camera", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }

          // 2. Attendance & Check-In
          Card(
            modifier = Modifier
              .weight(1f)
              .clickable { onOpenSubPage(StudentSubPage.ATTENDANCE) }
              .testTag("student_overview_checkin_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.40f)),
          ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(38.dp),
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(Icons.Filled.CheckCircle, contentDescription = "Attendance & Check-In", tint = MaterialTheme.colorScheme.onSecondary)
                }
              }
              Text("Attendance & Check-In", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
              Text("Token check-in & history ($presentCount Present)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          // 3. Event Calendar
          Card(
            modifier = Modifier
              .weight(1f)
              .clickable { onOpenCalendarTab() }
              .testTag("student_overview_event_calendar_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
          ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(38.dp),
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(Icons.Filled.CalendarMonth, contentDescription = "Event Calendar", tint = MaterialTheme.colorScheme.primary)
                }
              }
              Text("Event Calendar", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
              Text("Meetings, seminars & field trips (${upcomingEvents.size} upcoming)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }

          // 4. Cabinet / Digital ID Card
          Card(
            modifier = Modifier
              .weight(1f)
              .clickable { onOpenMembershipTab() }
              .testTag("student_overview_id_card_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
          ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.size(38.dp),
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(Icons.Filled.Badge, contentDescription = "Official ID Card", tint = MaterialTheme.colorScheme.tertiary)
                }
              }
              Text(if (isCabinetOfficer) "Cabinet ID Card" else "Digital ID Card", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
              Text("View & download official identity credential", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }
      }
    }
    }

    // Student Quick Services Grid
    item {
      SectionTitleRow(title = "Society Services")
      val tiles = listOf(
        Triple(StudentSubPage.EXECUTIVE_BODY, Icons.Filled.Groups, "Executive Body"),
        Triple(StudentSubPage.FEES_PAYMENTS, Icons.Filled.Payments, "Fees & Accounts"),
        Triple(StudentSubPage.ATTENDANCE, Icons.Filled.QrCodeScanner, "Camera QR Scan"),
        Triple(StudentSubPage.CERTIFICATES, Icons.Filled.WorkspacePremium, "Certificates"),
        Triple(StudentSubPage.GALLERY, Icons.Filled.PhotoLibrary, "Society Gallery"),
        Triple(StudentSubPage.FEEDBACK, Icons.Filled.Feedback, "Feedback"),
      )
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        tiles.chunked(3).forEach { rowItems ->
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            rowItems.forEach { (page, icon, label) ->
              Card(
                modifier = Modifier
                  .weight(1f)
                  .clickable { onOpenSubPage(page) }
                  .testTag("tile_${page.name.lowercase()}"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
              ) {
                Column(
                  modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 8.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                  Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(42.dp),
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    }
                  }
                  Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                }
              }
            }
          }
        }
      }
    }

    // Upcoming Events with Poster Preview
    item {
      SectionTitleRow(title = "Separate Event Registrations", actionLabel = "All Events", onAction = onOpenEventsTab)
    }
    if (upcomingEvents.isEmpty()) {
      item {
        EmptyStateCard(
          title = "No Upcoming Events Scheduled",
          subtitle = "Stay tuned for upcoming YES society meetings, field trips, seminars, and workshops.",
          icon = Icons.Filled.CalendarMonth,
        )
      }
    } else {
      items(upcomingEvents, key = { it.id }) { ev ->
        val alreadyRegistered = registeredEventIds.contains(ev.id)
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (ev.posterUri.isNotBlank()) {
              SocietyImage(
                imageSource = ev.posterUri,
                contentDescription = "${ev.title} poster",
                modifier = Modifier
                  .fillMaxWidth()
                  .height(175.dp)
                  .clip(RoundedCornerShape(14.dp))
                  .clickable { posterPreview = ev.title to ev.posterUri },
                contentScale = ContentScale.Crop,
              )
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              StatusPill(label = ev.category)
              Text(
                text = if (ev.registrationFee <= 0.0) "FREE" else "PKR ${ev.registrationFee.toInt()}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
              )
            }
            Text(ev.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("${ev.eventDate} • ${ev.eventTime} • ${ev.venue}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                "Deadline: ${ev.registrationDeadline}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary,
              )
              if (alreadyRegistered) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                  Text("Registered", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
              } else {
                Button(
                  onClick = { onQuickRegister(ev) },
                  modifier = Modifier.testTag("quick_register_${ev.id}"),
                ) {
                  Text("Register")
                }
              }
            }
          }
        }
      }
    }

    // Latest Announcements
    item {
      SectionTitleRow(title = "Latest Announcements")
    }
    if (announcements.isEmpty()) {
      item {
        EmptyStateCard(
          title = "No Announcements Yet",
          subtitle = "Official notices and meeting schedules from the Department of Entomology & YES Executive Body appear here.",
        )
      }
    } else {
      items(announcements.take(4), key = { it.id }) { ann ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
              StatusPill(label = ann.priority)
              StatusPill(label = ann.category)
            }
            Text(ann.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(ann.message, style = MaterialTheme.typography.bodyMedium)
            Text(
              "By ${ann.authorName}" + if (ann.scheduledDate.isNotBlank()) " • Date: ${ann.scheduledDate}" else "",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun StudentEventsTabContent(
  events: List<SocietyEvent>,
  registrations: List<EventRegistration>,
  onRegisterClick: (SocietyEvent) -> Unit,
  onCancelRegistration: (EventRegistration, SocietyEvent) -> Unit,
) {
  var mode by remember { mutableStateOf("UPCOMING") }
  var selectedCategory by remember { mutableStateOf("All") }
  var regToCancel by remember { mutableStateOf<Pair<EventRegistration, SocietyEvent>?>(null) }
  var posterPreview by remember { mutableStateOf<Pair<String, String>?>(null) }

  val regMap = remember(registrations) { registrations.associateBy { it.eventId } }

  if (posterPreview != null) {
    FullScreenImagePreviewDialog(
      title = posterPreview!!.first,
      imageSource = posterPreview!!.second,
      onDismiss = { posterPreview = null },
    )
  }

  if (regToCancel != null) {
    val (reg, ev) = regToCancel!!
    ConfirmDeleteDialog(
      itemTitle = ev.title,
      itemSubtitle = "Cancel and delete your registration for this event?",
      onConfirmDelete = { onCancelRegistration(reg, ev) },
      onDismiss = { regToCancel = null },
    )
  }

  val modeOptions = listOf("UPCOMING", "PREVIOUS", "REGISTERED", "ALL")

  val filtered = remember(events, mode, selectedCategory, regMap) {
    events.filter { ev ->
      val modeMatch = when (mode) {
        "UPCOMING" -> ev.upcoming
        "PREVIOUS" -> !ev.upcoming
        "REGISTERED" -> regMap.containsKey(ev.id)
        else -> true
      }
      val catMatch = selectedCategory == "All" || ev.category == selectedCategory
      modeMatch && catMatch
    }
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        YesDropdownField(
          label = "Event View",
          options = modeOptions,
          selectedOption = mode,
          onOptionSelected = { mode = it },
          optionLabel = {
            when (it) {
              "UPCOMING" -> "Upcoming Events"
              "PREVIOUS" -> "Previous Events"
              "REGISTERED" -> "My Registrations (${registrations.size})"
              else -> "All Events"
            }
          },
          modifier = Modifier.weight(1f),
          testTag = "student_events_mode_dropdown",
        )
        YesDropdownField(
          label = "Category",
          options = listOf("All") + SocietyConstants.EVENT_CATEGORIES,
          selectedOption = selectedCategory,
          onOptionSelected = { selectedCategory = it },
          modifier = Modifier.weight(1f),
          testTag = "student_events_category_dropdown",
        )
      }
    }

    if (filtered.isEmpty()) {
      item {
        EmptyStateCard(
          title = "No Matching Events",
          subtitle = "Use the dropdown menus above to browse seminars, field trips, society meetings, or workshops.",
          icon = Icons.Filled.Event,
        )
      }
    } else {
      items(filtered, key = { it.id }) { ev ->
        val myReg = regMap[ev.id]
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (ev.posterUri.isNotBlank()) {
              SocietyImage(
                imageSource = ev.posterUri,
                contentDescription = "${ev.title} poster",
                modifier = Modifier
                  .fillMaxWidth()
                  .height(190.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .clickable { posterPreview = ev.title to ev.posterUri },
                contentScale = ContentScale.Crop,
              )
              Text(
                text = "Tap event poster to view full size",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
              )
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              StatusPill(label = ev.category)
              Text(
                text = if (ev.registrationFee <= 0.0) "FREE" else "PKR ${ev.registrationFee.toInt()}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
              )
            }
            Text(ev.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(ev.description, style = MaterialTheme.typography.bodyMedium)
            Text("${ev.eventDate} • ${ev.eventTime}", style = MaterialTheme.typography.bodySmall)
            Text("${ev.venue} • Seats: ${ev.registeredCount}/${ev.maxParticipants}", style = MaterialTheme.typography.bodySmall)
            Text("Registration Deadline: ${ev.registrationDeadline}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)

            if (myReg != null) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                  StatusPill(label = "REG: ${myReg.registrationStatus}")
                  StatusPill(label = "FEE: ${myReg.paymentStatus}")
                }
                IconButton(
                  onClick = { regToCancel = myReg to ev },
                  modifier = Modifier.testTag("cancel_event_reg_${ev.id}"),
                ) {
                  Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete Event Registration",
                    tint = MaterialTheme.colorScheme.error,
                  )
                }
              }
            } else if (ev.upcoming && ev.registeredCount < ev.maxParticipants) {
              Button(
                onClick = { onRegisterClick(ev) },
                modifier = Modifier.fillMaxWidth().testTag("register_event_${ev.id}"),
              ) {
                Text("Register for ${ev.title}")
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun StudentMembershipTabContent(
  student: StudentProfile,
  applications: List<MembershipApplication>,
  onApply: (String) -> Unit,
  onDeleteApplication: (MembershipApplication) -> Unit,
  onDownloadIdCardPdf: (IdCardDesignTheme) -> Unit,
) {
  var committeePreference by remember { mutableStateOf("Seminars & Academic Research") }
  var motivation by remember {
    mutableStateOf("I wish to actively contribute to the Department of Entomology and Young Entomologist Society academic events, field trips, and community initiatives.")
  }
  var appToDelete by remember { mutableStateOf<MembershipApplication?>(null) }

  if (appToDelete != null) {
    val target = appToDelete!!
    ConfirmDeleteDialog(
      itemTitle = "Application #${target.id.takeLast(6)}",
      itemSubtitle = "Permanently delete this membership application record?",
      onConfirmDelete = { onDeleteApplication(target) },
      onDismiss = { appToDelete = null },
    )
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      DigitalMembershipCard(
        student = student,
        onDownloadPdf = if (student.isIdCardActive() || student.isCabinetMember()) onDownloadIdCardPdf else null,
      )
    }

    if (!student.isVerifiedMember() && student.membershipStatus != "PENDING") {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        ) {
          Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Apply for Official YES Verification & ID Card", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
              "Once verified by an Administrator, your official dual-crest ID Card (Society logo left, University logo right) is issued and your profile is locked for security.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            YesDropdownField(
              label = "Preferred Wing / Committee",
              options = listOf(
                "Seminars & Academic Research",
                "Field Trips & Insect Collection",
                "Event Management",
                "IT & Media Cell",
                "Sports & Co-Curricular",
              ),
              selectedOption = committeePreference,
              onOptionSelected = { committeePreference = it },
              testTag = "membership_wing_dropdown",
            )
            OutlinedTextField(
              value = motivation,
              onValueChange = { motivation = it },
              label = { Text("Statement of Interest *") },
              minLines = 3,
              modifier = Modifier.fillMaxWidth().testTag("membership_motivation_input"),
            )
            Button(
              onClick = { onApply("[$committeePreference] $motivation") },
              enabled = motivation.trim().length >= 5,
              modifier = Modifier.fillMaxWidth().testTag("apply_membership_button"),
            ) {
              Text("Submit Membership Application")
            }
          }
        }
      }
    }

    if (applications.isNotEmpty()) {
      item {
        SectionTitleRow(title = "Application History")
      }
      items(applications, key = { it.id }) { app ->
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
              Text("Application #${app.id.takeLast(6)}", style = MaterialTheme.typography.titleMedium)
              Row(verticalAlignment = Alignment.CenterVertically) {
                StatusPill(label = app.status)
                IconButton(
                  onClick = { appToDelete = app },
                  modifier = Modifier.testTag("delete_my_membership_app_${app.id}"),
                ) {
                  Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete Membership Application",
                    tint = MaterialTheme.colorScheme.error,
                  )
                }
              }
            }
            Text(app.motivation, style = MaterialTheme.typography.bodyMedium)
            if (app.assignedMembershipId.isNotBlank()) {
              Text("Assigned ID: ${app.assignedMembershipId}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            if (app.reviewNotes.isNotBlank()) {
              Text("Remarks: ${app.reviewNotes}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun StudentProfileTabContent(
  student: StudentProfile,
  appFontSize: AppFontSize,
  onAppFontSizeChange: (AppFontSize) -> Unit,
  onUpdateProfile: (String, String, String, String, String, String, String, String) -> Unit,
  onDownloadIdCardPdf: (IdCardDesignTheme) -> Unit,
  onSignOut: () -> Unit,
) {
  val context = LocalContext.current
  val isLocked = student.profileLocked

  var fullName by remember(student.fullName) { mutableStateOf(student.fullName) }
  var regNo by remember(student.registrationNumber) { mutableStateOf(student.registrationNumber) }
  var degreeLevel by remember(student.degreeLevel) {
    mutableStateOf(student.degreeLevel.ifBlank { SocietyConstants.DEGREE_LEVELS.first() })
  }
  var department by remember(student.department) {
    mutableStateOf(student.department.ifBlank { SocietyConstants.DEPARTMENTS.first() })
  }
  var semester by remember(student.semester) {
    mutableStateOf(student.semester.ifBlank { SocietyConstants.SEMESTERS.first() })
  }
  var phone by remember(student.phone) { mutableStateOf(student.phone) }
  var photoUri by remember(student.photoUri) { mutableStateOf(student.photoUri) }
  var studentCardUri by remember(student.studentCardUri) { mutableStateOf(student.studentCardUri) }
  var showSecurityInfo by remember { mutableStateOf(false) }
  var showProfileIdCardDialog by remember { mutableStateOf(false) }
  var previewDoc by remember { mutableStateOf<Pair<String, String>?>(null) }

  val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
    if (uri != null && !isLocked) {
      photoUri = ImageEncodingUtils.encodeUriToDataUrl(context, uri, maxDimension = 420, quality = 70)
        ?: uri.toString()
    }
  }

  val cardPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
    if (uri != null && !isLocked) {
      studentCardUri = ImageEncodingUtils.encodeUriToDataUrl(context, uri, maxDimension = 640, quality = 70)
        ?: uri.toString()
    }
  }

  if (previewDoc != null) {
    FullScreenImagePreviewDialog(
      title = previewDoc!!.first,
      imageSource = previewDoc!!.second,
      onDismiss = { previewDoc = null },
    )
  }

  if (showProfileIdCardDialog) {
    OfficialIdCardPreviewDialog(
      student = student,
      onDownloadPdf = if (student.isIdCardActive() || student.isCabinetMember()) onDownloadIdCardPdf else null,
      onDismiss = { showProfileIdCardDialog = false },
    )
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    // Locked Banner when profile is approved by Administrator
    if (isLocked) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth().testTag("profile_locked_banner"),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E5)),
          border = BorderStroke(1.2.dp, Color(0xFFB8860B)),
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Icon(Icons.Filled.Lock, contentDescription = "Locked", tint = Color(0xFF8A6100), modifier = Modifier.size(28.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text(
                text = "Profile Approved & Locked by Administrator",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4E3500),
              )
              Text(
                text = "Your verified academic profile is locked to protect your issued ID Card. Only a YES Administrator can unlock it for changes.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF634600),
              )
            }
          }
        }
      }
    }

    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
      ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            if (photoUri.isNotBlank()) {
              SocietyImage(
                imageSource = photoUri,
                contentDescription = fullName,
                modifier = Modifier
                  .size(72.dp)
                  .clip(CircleShape)
                  .clickable { previewDoc = "Profile Photograph" to photoUri },
                contentScale = ContentScale.Crop,
              )
            } else {
              Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(72.dp)) {
                Box(contentAlignment = Alignment.Center) {
                  Text(fullName.take(2).uppercase(), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
              }
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(student.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(
                "Position: ${student.position.ifBlank { "Member" }} • ID: ${student.membershipId.ifBlank { "Not Assigned" }}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
              )
              if (!isLocked) {
                TextButton(
                  onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                  contentPadding = PaddingValues(0.dp),
                ) {
                  Icon(Icons.Outlined.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(Modifier.width(6.dp))
                  Text("Update Photograph")
                }
              }
            }
          }

          if (student.isCabinetMember() || student.isIdCardActive()) {
            OutlinedButton(
              onClick = { showProfileIdCardDialog = true },
              modifier = Modifier.fillMaxWidth().testTag("profile_view_id_card_btn"),
            ) {
              Icon(Icons.Filled.Badge, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(Modifier.width(8.dp))
              Text(
                if (student.isCabinetMember()) "View Cabinet Member ID Card"
                else "View Official Member ID Card"
              )
            }
          }

          // University Student Card Section
          if (studentCardUri.isNotBlank() || !isLocked) {
            Card(
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
              shape = RoundedCornerShape(12.dp),
            ) {
              Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Uploaded University Student Card", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                if (studentCardUri.isNotBlank()) {
                  SocietyImage(
                    imageSource = studentCardUri,
                    contentDescription = "University Student Card",
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(130.dp)
                      .clip(RoundedCornerShape(10.dp))
                      .clickable { previewDoc = "University Student Card" to studentCardUri },
                    contentScale = ContentScale.Crop,
                  )
                }
                if (!isLocked) {
                  OutlinedButton(
                    onClick = { cardPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    modifier = Modifier.fillMaxWidth(),
                  ) {
                    Text(if (studentCardUri.isNotBlank()) "Replace Student Card Image" else "Upload University Student Card")
                  }
                }
              }
            }
          }

          OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            enabled = !isLocked,
            label = { Text("Full Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
          OutlinedTextField(
            value = regNo,
            onValueChange = { regNo = it },
            enabled = !isLocked,
            label = { Text("Registration Number") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
          YesDropdownField(
            label = "Degree Level",
            options = SocietyConstants.DEGREE_LEVELS,
            selectedOption = degreeLevel,
            onOptionSelected = { degreeLevel = it },
            enabled = !isLocked,
            testTag = "profile_degree_dropdown",
          )
          YesDropdownField(
            label = "Academic Department",
            options = SocietyConstants.DEPARTMENTS,
            selectedOption = department,
            onOptionSelected = { department = it },
            enabled = !isLocked,
            testTag = "profile_department_dropdown",
          )
          YesDropdownField(
            label = "Current Semester",
            options = SocietyConstants.SEMESTERS,
            selectedOption = semester,
            onOptionSelected = { semester = it },
            enabled = !isLocked,
            testTag = "profile_semester_dropdown",
          )
          OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            enabled = !isLocked,
            label = { Text("Phone Number") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )

          if (!isLocked) {
            Button(
              onClick = {
                onUpdateProfile(
                  fullName,
                  regNo,
                  degreeLevel,
                  department,
                  semester,
                  phone,
                  photoUri,
                  studentCardUri,
                )
              },
              enabled = fullName.trim().length >= 2 && regNo.trim().length >= 2,
              modifier = Modifier.fillMaxWidth().testTag("save_student_profile_btn"),
            ) {
              Text("Save Profile Updates")
            }
          }
        }
      }
    }

    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
      ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
          AppFontSizeSelector(
            appFontSize = appFontSize,
            onAppFontSizeChange = onAppFontSizeChange,
          )

          OutlinedButton(
            onClick = { showSecurityInfo = true },
            modifier = Modifier.fillMaxWidth(),
          ) {
            Icon(Icons.Outlined.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Google Account & Security")
          }
          Button(
            onClick = onSignOut,
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.errorContainer,
              contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ),
            modifier = Modifier.fillMaxWidth().testTag("logout_button"),
          ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Logout")
          }
        }
      }
    }
  }

  if (showSecurityInfo) {
    AlertDialog(
      onDismissRequest = { showSecurityInfo = false },
      title = { Text("Account Security") },
      text = {
        Text("Your YES Student account is authenticated via Google Credential Manager (${student.email}). Manage your Google password and 2-Step Verification directly at myaccount.google.com/security.")
      },
      confirmButton = {
        Button(onClick = { showSecurityInfo = false }) { Text("Done") }
      },
    )
  }
}
