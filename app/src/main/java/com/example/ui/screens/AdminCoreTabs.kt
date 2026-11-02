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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.AdminAccount
import com.example.data.model.Announcement
import com.example.data.model.AttendanceRecord
import com.example.data.model.BlockedAccount
import com.example.data.model.EventRegistration
import com.example.data.model.ExecutiveMember
import com.example.data.model.FinanceTransaction
import com.example.data.model.IdCardDesignTheme
import com.example.data.model.MembershipApplication
import com.example.data.model.SocietyConstants
import com.example.data.model.SocietyEvent
import com.example.data.model.StudentProfile
import com.example.data.model.UserAccount
import com.example.data.model.allocatedRoleOrNull
import com.example.data.model.buildAdministrativeCabinetIdCards
import com.example.data.model.hasAllocatedRole
import com.example.data.model.hasPermission
import com.example.data.model.isCabinetMember
import com.example.data.model.isCabinetOfficerRole
import com.example.data.model.isIdCardActive
import com.example.data.model.isVerifiedMember
import com.example.data.model.resolvedDisplayDesignation
import com.example.data.model.resolvedRoleLabel
import com.example.data.model.toCabinetIdCardProfile
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.DigitalMembershipCard
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.FullScreenImagePreviewDialog
import com.example.ui.components.IdCardDesignThemeSelector
import com.example.ui.components.OfficialIdCardPreviewDialog
import com.example.ui.components.QrCameraScannerDialog
import com.example.ui.components.SectionTitleRow
import com.example.ui.components.SocietyImage
import com.example.ui.components.StatMetricCard
import com.example.ui.components.StatusPill
import com.example.ui.components.UafLogoEmblem
import com.example.ui.components.YesDropdownField
import com.example.ui.components.YesLogoEmblem
import com.example.ui.theme.YesHeroEnd
import com.example.ui.theme.YesHeroStart
import com.example.util.ImageEncodingUtils
import com.example.util.PdfExportManager
import com.example.util.YesQrCodeBox
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminOverviewTab(
  admin: AdminAccount,
  students: List<StudentProfile>,
  @Suppress("UNUSED_PARAMETER") executiveBody: List<ExecutiveMember>,
  @Suppress("UNUSED_PARAMETER") admins: List<AdminAccount>,
  membershipApps: List<MembershipApplication>,
  events: List<SocietyEvent>,
  registrations: List<EventRegistration>,
  announcements: List<Announcement>,
  attendance: List<AttendanceRecord>,
  myAttendance: List<AttendanceRecord> = emptyList(),
  financeTransactions: List<FinanceTransaction>,
  onQrCheckIn: (String) -> Unit = {},
  onOpenCabinetIdCardsTab: () -> Unit = {},
  onOpenModule: (AdminSubModule) -> Unit,
  @Suppress("UNUSED_PARAMETER") onMessage: (String) -> Unit,
) {
  val verifiedMembers = students.count { it.isVerifiedMember() && it.active }
  val pendingApps = membershipApps.count { it.status == "PENDING" }
  val upcomingEventsCount = events.count { it.upcoming }
  val presentCount = attendance.count { it.status == "PRESENT" }
  val personalPresentCount = myAttendance.count { it.status == "PRESENT" }
  val totalIncome = financeTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
  val totalExpense = financeTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
  val netBalance = totalIncome - totalExpense
  val canSeeFinance = admin.hasPermission("FINANCE_MANAGEMENT")

  var showQrCameraScanner by remember { mutableStateOf(false) }
  var qrTokenInput by remember { mutableStateOf("") }

  if (showQrCameraScanner) {
    QrCameraScannerDialog(
      title = "Cabinet & Executive QR Attendance",
      subtitle = "Scan the official venue Event QR code with your camera to record your personal event attendance.",
      onQrCodeScanned = { code ->
        showQrCameraScanner = false
        onQrCheckIn(code)
      },
      onDismiss = { showQrCameraScanner = false },
    )
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    // Hero Role Banner featuring Society Logo on LEFT & University Logo on RIGHT
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
              // Society Logo on LEFT
              YesLogoEmblem(size = 48.dp)
              Column(
                modifier = Modifier.weight(1f).padding(horizontal = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
              ) {
                Text(
                  text = "YOUNG ENTOMOLOGIST SOCIETY",
                  style = MaterialTheme.typography.labelMedium,
                  color = Color(0xFFFFE59E),
                  fontWeight = FontWeight.ExtraBold,
                  textAlign = TextAlign.Center,
                )
                Text(
                  text = "Department of Entomology • UAF",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color.White.copy(alpha = 0.90f),
                  textAlign = TextAlign.Center,
                )
              }
              // University Logo on RIGHT
              UafLogoEmblem(size = 48.dp)
            }
            HorizontalDivider(color = Color.White.copy(alpha = 0.18f))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = admin.fullName,
                  style = MaterialTheme.typography.headlineSmall,
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                )
                Text(
                  text = admin.resolvedRoleLabel(),
                  style = MaterialTheme.typography.bodySmall,
                  color = Color.White.copy(alpha = 0.90f),
                )
              }
              StatusPill(label = admin.resolvedDisplayDesignation())
            }
          }
        }
      }
    }

    // Section Title: Society Overview
    item {
      SectionTitleRow(title = "Society Overview")
    }

    // Society Overview Options: QR Camera, Attendance & Check-In, Event Calendar, Cabinet ID Cards
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
              .clickable { showQrCameraScanner = true }
              .testTag("admin_overview_qr_camera_card"),
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
              Text("Scan venue event QR codes with camera", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }

          // 2. Attendance & Check-In
          Card(
            modifier = Modifier
              .weight(1f)
              .clickable { onOpenModule(AdminSubModule.ATTENDANCE) }
              .testTag("admin_overview_checkin_card"),
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
              Text("Token check-in & records ($personalPresentCount Present)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
              .clickable { onOpenModule(AdminSubModule.EVENT_CALENDAR) }
              .testTag("admin_overview_event_calendar_card"),
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
              Text("Meetings, seminars & field trips ($upcomingEventsCount upcoming)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }

          // 4. Cabinet ID Cards
          Card(
            modifier = Modifier
              .weight(1f)
              .clickable { onOpenCabinetIdCardsTab() }
              .testTag("admin_overview_cabinet_id_cards_card"),
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
                  Icon(Icons.Filled.Badge, contentDescription = "Cabinet ID Cards", tint = MaterialTheme.colorScheme.tertiary)
                }
              }
              Text("Cabinet ID Cards", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
              Text("View & download officer identity cards", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }
      }
    }

    // 8 Overview KPI Metrics
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          StatMetricCard(
            title = "Registered Students",
            value = "${students.size}",
            icon = Icons.Filled.Groups,
            modifier = Modifier.weight(1f),
          )
          StatMetricCard(
            title = "Verified Members",
            value = "$verifiedMembers",
            icon = Icons.Filled.Badge,
            modifier = Modifier.weight(1f),
            onClick = { onOpenModule(AdminSubModule.MEMBERSHIP) },
          )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          StatMetricCard(
            title = "Pending Applications",
            value = "$pendingApps",
            icon = Icons.Filled.PendingActions,
            accentColor = Color(0xFFB46914),
            modifier = Modifier.weight(1f),
            onClick = { onOpenModule(AdminSubModule.MEMBERSHIP) },
          )
          StatMetricCard(
            title = "Upcoming Events",
            value = "$upcomingEventsCount",
            icon = Icons.Filled.Event,
            modifier = Modifier.weight(1f),
            onClick = { onOpenModule(AdminSubModule.EVENT_CALENDAR) },
          )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          StatMetricCard(
            title = "Event Registrations",
            value = "${registrations.size}",
            icon = Icons.Filled.HowToReg,
            modifier = Modifier.weight(1f),
            onClick = { onOpenModule(AdminSubModule.EVENT_REGISTRATIONS) },
          )
          StatMetricCard(
            title = "Attendance Present",
            value = "$presentCount",
            icon = Icons.Filled.CheckCircle,
            modifier = Modifier.weight(1f),
            onClick = { onOpenModule(AdminSubModule.ATTENDANCE) },
          )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          StatMetricCard(
            title = "Announcements",
            value = "${announcements.size}",
            icon = Icons.Filled.Campaign,
            modifier = Modifier.weight(1f),
            onClick = { onOpenModule(AdminSubModule.ANNOUNCEMENTS) },
          )
          StatMetricCard(
            title = if (canSeeFinance) "Net Balance (PKR)" else "Role Access",
            value = if (canSeeFinance) "${netBalance.toInt()}" else "Scoped",
            icon = Icons.Filled.AccountBalanceWallet,
            accentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
            onClick = if (canSeeFinance) ({ onOpenModule(AdminSubModule.FINANCE) }) else null,
          )
        }
      }
    }

    // Recent Announcements
    item {
      SectionTitleRow(
        title = "Recent Announcements",
        actionLabel = "Manage Notices",
        onAction = { onOpenModule(AdminSubModule.ANNOUNCEMENTS) },
      )
    }
    if (announcements.isEmpty()) {
      item {
        EmptyStateCard(
          title = "No Announcements Published",
          subtitle = "Open Notices & Announcements in Modules to broadcast schedules and deadlines.",
        )
      }
    } else {
      items(announcements.take(3), key = { it.id }) { ann ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              StatusPill(label = ann.priority)
              StatusPill(label = ann.targetGroup)
            }
            Text(ann.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(ann.message, style = MaterialTheme.typography.bodySmall, maxLines = 2)
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminStudentsTab(
  admin: AdminAccount,
  students: List<StudentProfile>,
  users: List<UserAccount> = emptyList(),
  blockedAccounts: List<BlockedAccount>,
  onSaveStudent: (
    StudentProfile?,
    String,
    String,
    String,
    String,
    String,
    String,
    String,
    String,
    String,
    String,
    String,
    String,
    String,
    Boolean,
    Boolean,
    String,
  ) -> Unit,
  onVerifyOrRejectStudent: (StudentProfile, Boolean, String?, String, String) -> Unit,
  onSetIdCardIssued: (StudentProfile, Boolean, String?, String) -> Unit,
  onToggleStudentLock: (StudentProfile, Boolean) -> Unit,
  onDeleteStudent: (StudentProfile) -> Unit,
  onPurgeAndBlockStudent: (StudentProfile, String) -> Unit,
  onUnblockAccount: (BlockedAccount) -> Unit,
  onMessage: (String) -> Unit,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  
  val canManageStudents = admin.hasPermission("REGISTRATION_MANAGEMENT")
  val canApproveStudent = admin.hasPermission("STUDENT_REGISTRATION_APPROVAL")
  val canManageIdCards = admin.hasPermission("ID_CARD_MANAGEMENT")
  val canAdminAccess = admin.hasPermission("ADMINISTRATOR_ACCESS")

  var searchQuery by remember { mutableStateOf("") }
  var filterStatus by remember { mutableStateOf("ALL") }
  var editingStudent by remember { mutableStateOf<StudentProfile?>(null) }
  var viewingStudent by remember { mutableStateOf<StudentProfile?>(null) }
  var showEditor by remember { mutableStateOf(false) }
  var studentToDelete by remember { mutableStateOf<StudentProfile?>(null) }
  var studentToVerify by remember { mutableStateOf<StudentProfile?>(null) }
  var studentToReject by remember { mutableStateOf<StudentProfile?>(null) }
  var studentToIssueIdCard by remember { mutableStateOf<StudentProfile?>(null) }
  var studentToPurgeAndBlock by remember { mutableStateOf<StudentProfile?>(null) }
  var previewIdCardStudent by remember { mutableStateOf<StudentProfile?>(null) }
  var idCardPdfStudent by remember { mutableStateOf<Pair<StudentProfile, IdCardDesignTheme>?>(null) }
  var zoomImageTitleAndSrc by remember { mutableStateOf<Pair<String, String>?>(null) }

  fun enrichStudentPhoto(st: StudentProfile): StudentProfile {
    if (st.photoUri.isNotBlank()) return st
    val userPhoto = users.firstOrNull { u ->
      (st.studentUid.isNotBlank() && (u.userId == st.studentUid || u.id == st.studentUid)) ||
        (st.email.isNotBlank() && u.email.equals(st.email.trim(), ignoreCase = true))
    }?.photoUri.orEmpty()
    return if (userPhoto.isNotBlank()) st.copy(photoUri = userPhoto) else st
  }

  val idCardPdfLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.CreateDocument("application/pdf")
  ) { uri: Uri? ->
    val target = idCardPdfStudent
    if (uri != null && target != null) {
      val (profile, theme) = target
      scope.launch {
        PdfExportManager.exportStudentIdCardPdf(context, uri, enrichStudentPhoto(profile), theme)
          .onSuccess { onMessage("Student ID Card PDF saved for ${profile.fullName}!") }
          .onFailure { onMessage("Failed to save ID Card PDF: ${it.localizedMessage}") }
      }
    }
    idCardPdfStudent = null
  }

  val statusFilterOptions = listOf(
    "ALL",
    "VERIFIED",
    "PENDING",
    "REJECTED",
    "ID_ISSUED",
    "ID_REVOKED",
    "LOCKED",
    "BLOCKED_ACCOUNTS",
  )

  val filtered = remember(students, searchQuery, filterStatus) {
    students.filter { st ->
      val q = searchQuery.trim().lowercase()
      val matchesQuery = q.isEmpty() ||
        st.fullName.lowercase().contains(q) ||
        st.registrationNumber.lowercase().contains(q) ||
        st.department.lowercase().contains(q) ||
        st.email.lowercase().contains(q)
      val matchesStatus = when (filterStatus) {
        "VERIFIED" -> st.isVerifiedMember()
        "PENDING" -> st.membershipStatus == "PENDING" || st.membershipStatus == "NONE"
        "REJECTED" -> st.membershipStatus == "REJECTED"
        "ID_ISSUED" -> st.isIdCardActive()
        "ID_REVOKED" -> st.idCardStatus == "REVOKED"
        "LOCKED" -> st.profileLocked
        else -> true
      }
      matchesQuery && matchesStatus
    }
  }

  if (zoomImageTitleAndSrc != null) {
    val (title, src) = zoomImageTitleAndSrc!!
    FullScreenImagePreviewDialog(
      title = title,
      imageSource = src,
      onDismiss = { zoomImageTitleAndSrc = null },
    )
  }

  if (studentToDelete != null) {
    val target = studentToDelete!!
    ConfirmDeleteDialog(
      itemTitle = "${target.fullName} (${target.registrationNumber})",
      itemSubtitle = "Delete this student profile record only (without blocking login)? Use 'Delete All Data & Block Login' to purge all records and permanently block account access.",
      onConfirmDelete = { onDeleteStudent(target) },
      onDismiss = { studentToDelete = null },
    )
  }

  // Verify Student Dialog (chooses General Member vs Cabinet Position + Validity)
  if (studentToVerify != null) {
    val target = studentToVerify!!
    val positionChoices = remember { listOf("General Member") + SocietyConstants.EXECUTIVE_DESIGNATIONS }
    var selectedPosition by remember(target) {
      mutableStateOf(
        if (target.isCabinetMember()) target.position else "General Member"
      )
    }
    var validUntil by remember(target) { mutableStateOf(target.membershipValidUntil.ifBlank { "2027-06-30" }) }
    var notes by remember(target) { mutableStateOf("Application verified by YES Administration. Profile locked and Official ID Card issued.") }

    AlertDialog(
      onDismissRequest = { studentToVerify = null },
      title = { Text("Mark Student Application VERIFIED") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("${target.fullName} (${target.registrationNumber})", fontWeight = FontWeight.Bold)
          YesDropdownField(
            label = "Assign Position & Card Type",
            options = positionChoices,
            selectedOption = selectedPosition,
            onOptionSelected = { selectedPosition = it },
            optionLabel = { pos ->
              if (pos == "General Member") "General Member (Standard Member ID Card)"
              else "$pos (Executive Cabinet ID Card)"
            },
          )
          OutlinedTextField(
            value = validUntil,
            onValueChange = { validUntil = it },
            label = { Text("ID Card Valid Until (YYYY-MM-DD)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
          OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Verification Notes") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onVerifyOrRejectStudent(target, true, selectedPosition, validUntil, notes)
            studentToVerify = null
          },
          modifier = Modifier.testTag("confirm_verify_student_btn"),
        ) {
          Text("Mark Verified & Issue ID")
        }
      },
      dismissButton = {
        TextButton(onClick = { studentToVerify = null }) { Text("Cancel") }
      },
    )
  }

  // Reject Student Dialog
  if (studentToReject != null) {
    val target = studentToReject!!
    var rejectNotes by remember(target) { mutableStateOf("Application marked REJECTED by YES Administration. Please review your details and university student card.") }
    AlertDialog(
      onDismissRequest = { studentToReject = null },
      title = { Text("Mark Student Application REJECTED") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("${target.fullName} (${target.registrationNumber})", fontWeight = FontWeight.Bold)
          Text(
            "Marking this student application as REJECTED will revoke any active ID card and unlock their profile so they can correct details if needed.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          OutlinedTextField(
            value = rejectNotes,
            onValueChange = { rejectNotes = it },
            label = { Text("Rejection Reason") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onVerifyOrRejectStudent(target, false, null, "", rejectNotes)
            studentToReject = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
          modifier = Modifier.testTag("confirm_reject_student_btn"),
        ) {
          Text("Mark Rejected")
        }
      },
      dismissButton = {
        TextButton(onClick = { studentToReject = null }) { Text("Cancel") }
      },
    )
  }

  // Issue ID Card Dialog (General Member Identity Card vs Executive Cabinet Credential)
  if (studentToIssueIdCard != null) {
    val target = studentToIssueIdCard!!
    val positionChoices = remember { listOf("General Member") + SocietyConstants.EXECUTIVE_DESIGNATIONS }
    var selectedPosition by remember(target) {
      mutableStateOf(
        if (target.isCabinetMember()) target.position else "General Member"
      )
    }
    var validUntil by remember(target) { mutableStateOf(target.membershipValidUntil.ifBlank { "2027-06-30" }) }

    AlertDialog(
      onDismissRequest = { studentToIssueIdCard = null },
      title = { Text("Issue Official YES ID Card") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("${target.fullName} (${target.registrationNumber})", fontWeight = FontWeight.Bold)
          Text(
            "Cabinet positions receive the Royal Emerald & Gold Executive Cabinet Credential with Cabinet Position plaque. General Members receive the Botanical Ivory General Member Identity Card.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          YesDropdownField(
            label = "Card Designation / Role",
            options = positionChoices,
            selectedOption = selectedPosition,
            onOptionSelected = { selectedPosition = it },
            optionLabel = { pos ->
              if (pos == "General Member") "General Member (General ID Card)"
              else "$pos (Cabinet Member ID Card)"
            },
          )
          OutlinedTextField(
            value = validUntil,
            onValueChange = { validUntil = it },
            label = { Text("Valid Until (YYYY-MM-DD)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onSetIdCardIssued(target, true, selectedPosition, validUntil)
            studentToIssueIdCard = null
          },
          modifier = Modifier.testTag("confirm_issue_id_card_btn"),
        ) {
          Text("Issue ID Card")
        }
      },
      dismissButton = {
        TextButton(onClick = { studentToIssueIdCard = null }) { Text("Cancel") }
      },
    )
  }

  // Delete Whole Student Data & Permanently Block Login Access Dialog
  if (studentToPurgeAndBlock != null) {
    val target = studentToPurgeAndBlock!!
    var blockReason by remember(target) {
      mutableStateOf("Account permanently blocked and all society records deleted by YES Administration.")
    }
    AlertDialog(
      onDismissRequest = { studentToPurgeAndBlock = null },
      title = {
        Text(
          "Delete Whole Student Data & Block Account",
          color = MaterialTheme.colorScheme.error,
          fontWeight = FontWeight.Bold,
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("${target.fullName} (${target.registrationNumber} • ${target.email})", fontWeight = FontWeight.Bold)
          Text(
            "WARNING: This permanently deletes ALL records belonging to this student (profile, membership applications, event registrations, attendance, payments, certificates, feedback & notifications) AND blocks their account so they CANNOT access the portal or re-register when trying to log in.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
          )
          OutlinedTextField(
            value = blockReason,
            onValueChange = { blockReason = it },
            label = { Text("Official Block Reason") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onPurgeAndBlockStudent(target, blockReason)
            if (viewingStudent?.id == target.id) viewingStudent = null
            studentToPurgeAndBlock = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
          modifier = Modifier.testTag("confirm_purge_and_block_student_btn"),
        ) {
          Text("Delete All Data & Block Login")
        }
      },
      dismissButton = {
        TextButton(onClick = { studentToPurgeAndBlock = null }) { Text("Cancel") }
      },
    )
  }

  // Full Student Registration, Photo, Student Card, Verification, ID Card & Block Inspector
  if (viewingStudent != null) {
    val liveTarget = enrichStudentPhoto(students.firstOrNull { it.id == viewingStudent!!.id } ?: viewingStudent!!)
    StudentFullDetailAdminDialog(
      admin = admin,
      student = liveTarget,
      canManageStudents = canManageStudents,
      onMarkVerified = { studentToVerify = liveTarget },
      onMarkRejected = { studentToReject = liveTarget },
      onIssueIdCard = { studentToIssueIdCard = liveTarget },
      onRevokeIdCard = { onSetIdCardIssued(liveTarget, false, null, "") },
      onPurgeAndBlock = { studentToPurgeAndBlock = liveTarget },
      onToggleLock = { locked -> onToggleStudentLock(liveTarget, locked) },
      onOpenImageZoom = { title, src -> zoomImageTitleAndSrc = title to src },
      onExportIdCardPdf = { theme ->
        idCardPdfStudent = liveTarget to theme
        idCardPdfLauncher.launch("YES_ID_${liveTarget.registrationNumber.ifBlank { liveTarget.fullName }}.pdf")
      },
      onEdit = {
        viewingStudent = null
        editingStudent = liveTarget
        showEditor = true
      },
      onDismiss = { viewingStudent = null },
    )
  }

  if (previewIdCardStudent != null) {
    val cardTarget = enrichStudentPhoto(students.firstOrNull { it.id == previewIdCardStudent!!.id } ?: previewIdCardStudent!!)
    OfficialIdCardPreviewDialog(
      student = cardTarget,
      onDownloadPdf = { theme ->
        idCardPdfStudent = cardTarget to theme
        idCardPdfLauncher.launch("YES_ID_${cardTarget.registrationNumber.ifBlank { "Member" }}.pdf")
      },
      onDismiss = { previewIdCardStudent = null },
    )
  }

  Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      item {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          label = { Text("Search by Name, Reg No. (2023-ag-...) or Email") },
          leadingIcon = { Icon(Icons.Filled.PersonSearch, contentDescription = null) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("admin_student_search_input"),
        )
      }
      item {
        YesDropdownField(
          label = "Filter Student & Account Status",
          options = statusFilterOptions,
          selectedOption = filterStatus,
          onOptionSelected = { filterStatus = it },
          optionLabel = { opt ->
            when (opt) {
              "ALL" -> "All Registered Students (${students.size})"
              "VERIFIED" -> "Verified Students (${students.count { it.isVerifiedMember() }})"
              "PENDING" -> "Pending / Unverified (${students.count { it.membershipStatus == "PENDING" || it.membershipStatus == "NONE" }})"
              "REJECTED" -> "Rejected Applications (${students.count { it.membershipStatus == "REJECTED" }})"
              "ID_ISSUED" -> "ID Card Issued (${students.count { it.isIdCardActive() }})"
              "ID_REVOKED" -> "ID Card Revoked (${students.count { it.idCardStatus == "REVOKED" }})"
              "LOCKED" -> "Locked Profiles (${students.count { it.profileLocked }})"
              "BLOCKED_ACCOUNTS" -> "Blocked Student Accounts (${blockedAccounts.size})"
              else -> opt
            }
          },
          testTag = "admin_student_filter_dropdown",
        )
      }

      if (filterStatus == "BLOCKED_ACCOUNTS") {
        if (blockedAccounts.isEmpty()) {
          item {
            EmptyStateCard(
              title = "No Blocked Student Accounts",
              subtitle = "Students whose data was purged and blocked from logging in appear here.",
              icon = Icons.Filled.Block,
            )
          }
        } else {
          items(blockedAccounts, key = { it.userId.ifBlank { it.id } }) { blocked ->
            Card(
              modifier = Modifier.fillMaxWidth(),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.28f)),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
            ) {
              Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(blocked.fullName.ifBlank { "Blocked User" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Reg No: ${blocked.registrationNumber.ifBlank { "N/A" }} • ${blocked.email}", style = MaterialTheme.typography.bodySmall)
                  }
                  StatusPill(label = "BLOCKED")
                }
                Text("Reason: ${blocked.reason}", style = MaterialTheme.typography.bodySmall)
                Text("Blocked by: ${blocked.blockedByName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (canAdminAccess) {
                  OutlinedButton(
                    onClick = { onUnblockAccount(blocked) },
                    modifier = Modifier.testTag("unblock_student_${blocked.userId}"),
                  ) {
                    Icon(Icons.Filled.LockReset, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Unblock Account Access")
                  }
                }
              }
            }
          }
        }
      } else if (filtered.isEmpty()) {
        item {
          EmptyStateCard(
            title = "No Matching Students",
            subtitle = "Add students manually or adjust your search and dropdown filter above.",
            icon = Icons.Filled.Groups,
          )
        }
      } else {
        items(filtered, key = { it.id }) { st ->
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
          ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Surface(
                  modifier = Modifier
                    .size(54.dp)
                    .clickable(enabled = st.photoUri.isNotBlank()) {
                      if (st.photoUri.isNotBlank()) {
                        zoomImageTitleAndSrc = "${st.fullName} — Photograph" to st.photoUri
                      }
                    },
                  shape = CircleShape,
                  color = MaterialTheme.colorScheme.primaryContainer,
                  border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                ) {
                  if (st.photoUri.isNotBlank()) {
                    SocietyImage(
                      imageSource = st.photoUri,
                      contentDescription = st.fullName,
                      modifier = Modifier.fillMaxSize(),
                      contentScale = ContentScale.Crop,
                    )
                  } else {
                    Box(contentAlignment = Alignment.Center) {
                      Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                  }
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                  Text(st.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                  Text(
                    "Reg No: ${st.registrationNumber} • ${st.position.ifBlank { "General Member" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                  )
                  Text("${st.degreeLevel} • ${st.department} (${st.semester})", style = MaterialTheme.typography.bodySmall)
                }
              }

              FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                StatusPill(label = st.membershipStatus)
                StatusPill(label = if (st.isIdCardActive()) "ISSUED" else st.idCardStatus.ifBlank { "NOT_ISSUED" })
                if (st.isCabinetMember()) StatusPill(label = "CABINET")
                StatusPill(label = if (st.profileLocked) "LOCKED" else "UNLOCKED")
                if (st.studentCardUri.isNotBlank()) StatusPill(label = "CARD UPLOADED")
              }

              if (st.membershipId.isNotBlank()) {
                Text(
                  "Membership ID: ${st.membershipId} (Valid: ${st.membershipValidUntil.ifBlank { "2027-06-30" }})",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.primary,
                )
              }

              // Administrator Verification, Issue/Revoke ID Card & Block Controls
              if (canManageStudents) {
                FlowRow(
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalArrangement = Arrangement.spacedBy(6.dp),
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  if (canApproveStudent) {
                    Button(
                      onClick = { studentToVerify = st },
                      modifier = Modifier.testTag("verify_student_${st.id}"),
                    ) {
                      Icon(Icons.Filled.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                      Spacer(Modifier.width(4.dp))
                      Text("Verified")
                    }
                    OutlinedButton(
                      onClick = { studentToReject = st },
                      colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                      modifier = Modifier.testTag("reject_student_${st.id}"),
                    ) {
                      Icon(Icons.Filled.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                      Spacer(Modifier.width(4.dp))
                      Text("Rejected")
                    }
                  }
                  if (canManageIdCards) {
                    if (st.isIdCardActive()) {
                      OutlinedButton(
                        onClick = { onSetIdCardIssued(st, false, null, "") },
                        modifier = Modifier.testTag("revoke_id_student_${st.id}"),
                      ) {
                        Icon(Icons.Filled.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Revoke ID Card")
                      }
                    } else {
                      OutlinedButton(
                        onClick = { studentToIssueIdCard = st },
                        modifier = Modifier.testTag("issue_id_student_${st.id}"),
                      ) {
                        Icon(Icons.Filled.Badge, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Issue ID Card")
                      }
                    }
                  }
                }
              }

              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth(),
              ) {
                OutlinedButton(
                  onClick = { viewingStudent = st },
                  modifier = Modifier.testTag("view_student_${st.id}"),
                ) {
                  Icon(Icons.Filled.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(Modifier.width(6.dp))
                  Text("View Full Info & ID")
                }

                if (st.isCabinetMember() || st.isIdCardActive()) {
                  OutlinedButton(
                    onClick = { previewIdCardStudent = st },
                    modifier = Modifier.testTag("view_id_card_student_${st.id}"),
                  ) {
                    Icon(Icons.Filled.Badge, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("View ID Card")
                  }
                }
              }

              Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
              ) {

                if (canManageStudents) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                      onClick = { onToggleStudentLock(st, !st.profileLocked) },
                      modifier = Modifier.testTag("toggle_lock_student_${st.id}"),
                    ) {
                      Icon(
                        imageVector = if (st.profileLocked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                        contentDescription = if (st.profileLocked) "Unlock Student Profile" else "Lock Student Profile",
                        tint = if (st.profileLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                      )
                    }
                    IconButton(onClick = {
                      editingStudent = st
                      showEditor = true
                    }) {
                      Icon(Icons.Filled.Edit, contentDescription = "Edit Student")
                    }
                    if (canAdminAccess) {
                      IconButton(
                        onClick = { studentToPurgeAndBlock = st },
                        modifier = Modifier.testTag("purge_block_student_${st.id}"),
                      ) {
                        Icon(
                          Icons.Filled.DeleteForever,
                          contentDescription = "Delete All Student Data & Block Login",
                          tint = MaterialTheme.colorScheme.error,
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    if (canManageStudents) {
      FloatingActionButton(
        onClick = {
          editingStudent = null
          showEditor = true
        },
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(18.dp)
          .testTag("admin_add_student_fab"),
      ) {
        Icon(Icons.Filled.Add, contentDescription = "Add Student")
      }
    }
  }

  if (showEditor) {
    val st = editingStudent
    var fullName by remember(st) { mutableStateOf(st?.fullName ?: "") }
    var email by remember(st) { mutableStateOf(st?.email ?: "") }
    var regNo by remember(st) { mutableStateOf(st?.registrationNumber ?: "") }
    var degreeLevel by remember(st) { mutableStateOf(st?.degreeLevel?.ifBlank { SocietyConstants.DEGREE_LEVELS.first() } ?: SocietyConstants.DEGREE_LEVELS.first()) }
    var dept by remember(st) { mutableStateOf(st?.department?.ifBlank { "Department of Entomology" } ?: "Department of Entomology") }
    var sem by remember(st) { mutableStateOf(st?.semester?.ifBlank { "1st Semester" } ?: "1st Semester") }
    var phone by remember(st) { mutableStateOf(st?.phone ?: "") }
    val positionOptions = remember { listOf("General Member") + SocietyConstants.EXECUTIVE_DESIGNATIONS }
    var position by remember(st) {
      mutableStateOf(
        if (st != null && st.isCabinetMember()) st.position else "General Member"
      )
    }
    var memStatus by remember(st) {
      mutableStateOf(
        when (st?.membershipStatus) {
          "ACTIVE" -> "VERIFIED"
          null, "" -> "VERIFIED"
          else -> st.membershipStatus
        }
      )
    }
    var idCardStatus by remember(st) {
      mutableStateOf(
        when {
          st == null -> "ISSUED"
          st.isIdCardActive() -> "ISSUED"
          st.idCardStatus == "REVOKED" -> "REVOKED"
          else -> "NOT_ISSUED"
        }
      )
    }
    var memId by remember(st) { mutableStateOf(st?.membershipId?.ifBlank { "YES-UAF-2026-${(1000..9999).random()}" } ?: "YES-UAF-2026-${(1000..9999).random()}") }
    var validUntil by remember(st) { mutableStateOf(st?.membershipValidUntil?.ifBlank { "2027-06-30" } ?: "2027-06-30") }
    var photoUri by remember(st) { mutableStateOf(st?.photoUri ?: "") }
    var studentCardUri by remember(st) { mutableStateOf(st?.studentCardUri ?: "") }
    var profileLocked by remember(st) { mutableStateOf(st?.profileLocked ?: true) }
    var active by remember(st) { mutableStateOf(st?.active ?: true) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
      if (uri != null) {
        photoUri = ImageEncodingUtils.encodeUriToDataUrl(context, uri, maxDimension = 420, quality = 68) ?: uri.toString()
      }
    }
    val cardPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
      if (uri != null) {
        studentCardUri = ImageEncodingUtils.encodeUriToDataUrl(context, uri, maxDimension = 640, quality = 68) ?: uri.toString()
      }
    }

    AlertDialog(
      onDismissRequest = { showEditor = false },
      title = { Text(if (st == null) "Add Verified Student Record" else "Edit Student Record") },
      text = {
        Column(
          modifier = Modifier.verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = regNo, onValueChange = { regNo = it }, label = { Text("Registration No *") }, singleLine = true, modifier = Modifier.fillMaxWidth())

          YesDropdownField(
            label = "Degree Level",
            options = SocietyConstants.DEGREE_LEVELS,
            selectedOption = degreeLevel,
            onOptionSelected = { degreeLevel = it },
          )
          YesDropdownField(
            label = "Department",
            options = SocietyConstants.DEPARTMENTS,
            selectedOption = dept,
            onOptionSelected = { dept = it },
          )
          YesDropdownField(
            label = "Semester",
            options = SocietyConstants.SEMESTERS,
            selectedOption = sem,
            onOptionSelected = { sem = it },
          )
          OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, singleLine = true, modifier = Modifier.fillMaxWidth())

          YesDropdownField(
            label = "Society Position / Cabinet Role",
            options = positionOptions,
            selectedOption = position,
            onOptionSelected = { position = it },
          )
          YesDropdownField(
            label = "Verification Status",
            options = listOf("NONE", "PENDING", "VERIFIED", "REJECTED", "INACTIVE"),
            selectedOption = memStatus,
            onOptionSelected = {
              memStatus = it
              if (it == "VERIFIED") {
                profileLocked = true
                idCardStatus = "ISSUED"
              } else if (it == "REJECTED") {
                idCardStatus = "REVOKED"
              }
            },
          )
          YesDropdownField(
            label = "Official ID Card Status",
            options = listOf("NOT_ISSUED", "ISSUED", "REVOKED"),
            selectedOption = idCardStatus,
            onOptionSelected = { idCardStatus = it },
          )

          OutlinedTextField(value = memId, onValueChange = { memId = it }, label = { Text("Membership ID") }, singleLine = true, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = validUntil, onValueChange = { validUntil = it }, label = { Text("Valid Until (YYYY-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
              onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
              modifier = Modifier.weight(1f),
            ) {
              Text(if (photoUri.isNotBlank()) "Photo Ready ✓" else "Upload Photo")
            }
            OutlinedButton(
              onClick = { cardPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
              modifier = Modifier.weight(1f),
            ) {
              Text(if (studentCardUri.isNotBlank()) "Uni Card ✓" else "Uni Card")
            }
          }

          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Lock Student Profile")
            Switch(checked = profileLocked, onCheckedChange = { profileLocked = it })
          }
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Account Active")
            Switch(checked = active, onCheckedChange = { active = it })
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onSaveStudent(
              st,
              fullName,
              email.ifBlank { "student@uaf.edu.pk" },
              regNo,
              degreeLevel,
              dept,
              sem,
              phone,
              position,
              memStatus,
              memId,
              validUntil,
              photoUri,
              studentCardUri,
              profileLocked,
              active,
              idCardStatus,
            )
            showEditor = false
          },
          enabled = fullName.trim().length >= 2 && regNo.trim().length >= 2,
        ) {
          Text("Save")
        }
      },
      dismissButton = {
        TextButton(onClick = { showEditor = false }) { Text("Cancel") }
      },
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StudentFullDetailAdminDialog(
  admin: AdminAccount,
  student: StudentProfile,
  canManageStudents: Boolean,
  onMarkVerified: () -> Unit,
  onMarkRejected: () -> Unit,
  onIssueIdCard: () -> Unit,
  onRevokeIdCard: () -> Unit,
  onPurgeAndBlock: () -> Unit,
  onToggleLock: (Boolean) -> Unit,
  onOpenImageZoom: (String, String) -> Unit,
  onExportIdCardPdf: (IdCardDesignTheme) -> Unit,
  onEdit: () -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Registered Student Profile & Verification", fontWeight = FontWeight.Bold) },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Student Photo & Academic Details
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Surface(
            modifier = Modifier
              .size(width = 78.dp, height = 92.dp)
              .clickable(enabled = student.photoUri.isNotBlank()) {
                if (student.photoUri.isNotBlank()) {
                  onOpenImageZoom("${student.fullName} — Student Photo", student.photoUri)
                }
              },
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
          ) {
            if (student.photoUri.isNotBlank()) {
              SocietyImage(
                imageSource = student.photoUri,
                contentDescription = student.fullName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
              )
            } else {
              Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
              }
            }
          }

          Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(student.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Reg No: ${student.registrationNumber}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            Text("Email: ${student.email}", style = MaterialTheme.typography.bodySmall)
            Text("Phone: ${student.phone.ifBlank { "Not provided" }}", style = MaterialTheme.typography.bodySmall)
            Text("Position: ${student.position.ifBlank { "General Member" }}", style = MaterialTheme.typography.labelMedium)
          }
        }

        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Degree Level: ${student.degreeLevel}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            Text("Department: ${student.department}", style = MaterialTheme.typography.bodySmall)
            Text("Semester: ${student.semester}", style = MaterialTheme.typography.bodySmall)
            Text("Verification Status: ${student.membershipStatus} (${student.membershipId.ifBlank { "No ID yet" }})", style = MaterialTheme.typography.bodySmall)
            Text("ID Card Status: ${if (student.isIdCardActive()) "ISSUED" else student.idCardStatus.ifBlank { "NOT_ISSUED" }}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
          }
        }

        // Administrator Verification, ID Card Issue/Revoke & Purge/Block Controls
        if (canManageStudents) {
          Text("Application Verification & ID Card Controls", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            if (admin.hasPermission("MEMBERSHIP_FORM_APPROVAL") || admin.hasPermission("STUDENT_REGISTRATION_APPROVAL")) {
              Button(onClick = onMarkVerified) {
                Icon(Icons.Filled.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Mark Verified")
              }
              OutlinedButton(
                onClick = onMarkRejected,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
              ) {
                Icon(Icons.Filled.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Mark Rejected")
              }
            }
            if (admin.hasPermission("ID_CARD_MANAGEMENT")) {
              Button(onClick = onIssueIdCard) {
                Icon(Icons.Filled.Badge, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Issue ID Card")
              }
              if (student.isIdCardActive()) {
                OutlinedButton(onClick = onRevokeIdCard) {
                  Icon(Icons.Filled.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(Modifier.width(4.dp))
                  Text("Revoke ID Card")
                }
              }
            }
          }
        }

        // Uploaded University Student Card Preview
        Text("Uploaded University Student Card", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        if (student.studentCardUri.isNotBlank()) {
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .height(170.dp)
              .clip(RoundedCornerShape(14.dp))
              .clickable {
                onOpenImageZoom("${student.fullName} — University Student Card", student.studentCardUri)
              },
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
          ) {
            SocietyImage(
              imageSource = student.studentCardUri,
              contentDescription = "University Student Card of ${student.fullName}",
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop,
            )
          }
          Text("Tap image to enlarge full student card", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        } else {
          Text("No university student card image uploaded yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // Profile Lock Control
        if (canManageStudents) {
          Card(
            colors = CardDefaults.cardColors(
              containerColor = if (student.profileLocked) Color(0xFFE7F6EC) else Color(0xFFFFF4E0)
            ),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = if (student.profileLocked) "Profile Locked (Admin Protected)" else "Profile Unlocked (Student Editable)",
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.Bold,
                )
                Text(
                  text = if (student.profileLocked) "Student cannot alter verified details unless unlocked here."
                  else "Student can currently edit their profile.",
                  style = MaterialTheme.typography.bodySmall,
                )
              }
              Spacer(Modifier.width(8.dp))
              OutlinedButton(onClick = { onToggleLock(!student.profileLocked) }) {
                Icon(
                  imageVector = if (student.profileLocked) Icons.Filled.LockOpen else Icons.Filled.Lock,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(if (student.profileLocked) "Unlock" else "Lock")
              }
            }
          }

          if (admin.hasPermission("ADMINISTRATOR_ACCESS") || admin.hasPermission("DATA_MANAGEMENT")) {
            Button(
              onClick = onPurgeAndBlock,
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
              modifier = Modifier.fillMaxWidth().testTag("detail_purge_block_btn"),
            ) {
              Icon(Icons.Filled.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(Modifier.width(6.dp))
              Text("Delete Whole Student Data & Block Login")
            }
          }
        }

        HorizontalDivider()

        // Official Dual-Logo ID Card Preview (Cabinet vs General Member)
        Text(
          text = if (student.isCabinetMember()) "Executive Cabinet Credential Preview" else "General Member Identity Card Preview",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
        )
        DigitalMembershipCard(
          student = student,
          onDownloadPdf = onExportIdCardPdf,
        )
      }
    },
    confirmButton = {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (canManageStudents) {
          OutlinedButton(onClick = onEdit) {
            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Edit")
          }
        }
        Button(onClick = onDismiss) { Text("Close") }
      }
    },
  )
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminEventsTab(
  admin: AdminAccount,
  events: List<SocietyEvent>,
  registrations: List<EventRegistration>,
  onSaveEvent: (
    SocietyEvent?,
    String,
    String,
    String,
    String,
    String,
    String,
    String,
    String,
    Double,
    Long,
    Boolean,
    Boolean,
  ) -> Unit,
  onDeleteEvent: (SocietyEvent) -> Unit,
  onOpenEventRegistrations: (String?) -> Unit,
  onOpenEventCalendar: () -> Unit = {},
) {
  val context = LocalContext.current
  val canManageEvents = admin.hasPermission("EVENT_MANAGEMENT")
  val canManageRegistrations = admin.hasPermission("REGISTRATION_MANAGEMENT")

  var selectedCategoryFilter by remember { mutableStateOf("ALL") }
  var selectedStatusFilter by remember { mutableStateOf("ALL") }
  var editingEvent by remember { mutableStateOf<SocietyEvent?>(null) }
  var showEventDialog by remember { mutableStateOf(false) }
  var qrModalEvent by remember { mutableStateOf<SocietyEvent?>(null) }
  var eventToDelete by remember { mutableStateOf<SocietyEvent?>(null) }
  var posterZoomEvent by remember { mutableStateOf<SocietyEvent?>(null) }

  val filteredEvents = remember(events, selectedCategoryFilter, selectedStatusFilter) {
    events.filter { ev ->
      val catOk = selectedCategoryFilter == "ALL" || ev.category == selectedCategoryFilter
      val statusOk = when (selectedStatusFilter) {
        "UPCOMING" -> ev.upcoming
        "PAST" -> !ev.upcoming
        else -> true
      }
      catOk && statusOk
    }
  }

  if (posterZoomEvent != null) {
    val target = posterZoomEvent!!
    FullScreenImagePreviewDialog(
      title = target.title,
      imageSource = target.posterUri,
      onDismiss = { posterZoomEvent = null },
    )
  }

  if (eventToDelete != null) {
    val target = eventToDelete!!
    ConfirmDeleteDialog(
      itemTitle = target.title,
      itemSubtitle = "Permanently delete this society event (${target.eventDate})?",
      onConfirmDelete = { onDeleteEvent(target) },
      onDismiss = { eventToDelete = null },
    )
  }

  Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Button(
            onClick = onOpenEventCalendar,
            modifier = Modifier.weight(1f).testTag("admin_events_open_calendar_btn"),
          ) {
            Icon(Icons.Filled.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Event Calendar")
          }
          OutlinedButton(
            onClick = { onOpenEventRegistrations(null) },
            modifier = Modifier.weight(1f).testTag("open_separate_event_registrations_btn"),
          ) {
            Icon(Icons.Filled.HowToReg, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Registrations (${registrations.size})")
          }
        }
      }

      // Event Dropdown Filters
      item {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
          Box(modifier = Modifier.weight(1.2f)) {
            YesDropdownField(
              label = "Event Category",
              options = listOf("ALL") + SocietyConstants.EVENT_CATEGORIES,
              selectedOption = selectedCategoryFilter,
              onOptionSelected = { selectedCategoryFilter = it },
              optionLabel = { if (it == "ALL") "All Categories (${events.size})" else it },
              testTag = "admin_events_category_dropdown",
            )
          }
          Box(modifier = Modifier.weight(0.8f)) {
            YesDropdownField(
              label = "Schedule",
              options = listOf("ALL", "UPCOMING", "PAST"),
              selectedOption = selectedStatusFilter,
              onOptionSelected = { selectedStatusFilter = it },
              optionLabel = {
                when (it) {
                  "ALL" -> "All Events"
                  "UPCOMING" -> "Upcoming"
                  "PAST" -> "Past"
                  else -> it
                }
              },
              testTag = "admin_events_status_dropdown",
            )
          }
        }
      }

      if (filteredEvents.isEmpty()) {
        item {
          EmptyStateCard(title = "No Events Found", subtitle = "Tap + to schedule seminars, Society Meetings, Study Tours, Sports Gala, or Annual Dinner.")
        }
      } else {
        items(filteredEvents, key = { it.id }) { ev ->
          val evRegCount = registrations.count { it.eventId == ev.id }
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
          ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              if (ev.posterUri.isNotBlank()) {
                Surface(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { posterZoomEvent = ev },
                ) {
                  SocietyImage(
                    imageSource = ev.posterUri,
                    contentDescription = "Poster for ${ev.title}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                  )
                }
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                StatusPill(label = ev.category)
                StatusPill(label = if (ev.upcoming) "UPCOMING" else "PAST")
              }
              Text(ev.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
              Text("${ev.eventDate} • ${ev.eventTime} • ${ev.venue}", style = MaterialTheme.typography.bodySmall)
              Text(
                "Fee: PKR ${ev.registrationFee.toInt()} • Seats: $evRegCount/${ev.maxParticipants} • QR: ${ev.qrCodeToken}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
              )

              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
              ) {
                Button(
                  onClick = { onOpenEventRegistrations(ev.id) },
                  modifier = Modifier.testTag("event_registrations_btn_${ev.id}"),
                ) {
                  Icon(Icons.Filled.HowToReg, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(Modifier.width(4.dp))
                  Text("Registrations ($evRegCount)")
                }
                OutlinedButton(onClick = { qrModalEvent = ev }) {
                  Icon(Icons.Filled.QrCode2, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(Modifier.width(4.dp))
                  Text("QR")
                }
                if (canManageEvents) {
                  OutlinedButton(
                    onClick = {
                      editingEvent = ev
                      showEventDialog = true
                    },
                    modifier = Modifier.testTag("edit_event_${ev.id}"),
                  ) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit Event", modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Edit")
                  }
                  OutlinedButton(
                    onClick = { eventToDelete = ev },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
                    modifier = Modifier.testTag("delete_event_${ev.id}"),
                  ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete Event", modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Delete")
                  }
                }
              }
            }
          }
        }
      }
    }

    if (canManageEvents) {
      FloatingActionButton(
        onClick = {
          editingEvent = null
          showEventDialog = true
        },
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(18.dp)
          .testTag("admin_add_event_fab"),
      ) {
        Icon(Icons.Filled.Add, contentDescription = "Create Event")
      }
    }
  }

  if (qrModalEvent != null) {
    val ev = qrModalEvent!!
    AlertDialog(
      onDismissRequest = { qrModalEvent = null },
      title = { Text("Event Check-In QR Code") },
      text = {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
          Text(ev.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          YesQrCodeBox(payload = ev.qrCodeToken, boxSize = 180.dp)
          Text("Check-In Token: ${ev.qrCodeToken}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
          Text("Display this QR code at the venue so students can scan it with their camera to mark attendance.", style = MaterialTheme.typography.bodySmall)
        }
      },
      confirmButton = {
        Button(onClick = { qrModalEvent = null }) { Text("Done") }
      },
    )
  }

  if (showEventDialog) {
    val ev = editingEvent
    var title by remember(ev) { mutableStateOf(ev?.title ?: "") }
    var desc by remember(ev) { mutableStateOf(ev?.description ?: "") }
    var category by remember(ev) { mutableStateOf(ev?.category ?: SocietyConstants.EVENT_CATEGORIES.first()) }
    var date by remember(ev) { mutableStateOf(ev?.eventDate ?: "2026-11-25") }
    var time by remember(ev) { mutableStateOf(ev?.eventTime ?: "10:00 AM - 01:00 PM") }
    var venue by remember(ev) { mutableStateOf(ev?.venue ?: "Department of Entomology Auditorium, UAF") }
    var deadline by remember(ev) { mutableStateOf(ev?.registrationDeadline ?: "2026-11-22") }
    var feeStr by remember(ev) { mutableStateOf((ev?.registrationFee ?: 0.0).toInt().toString()) }
    var maxStr by remember(ev) { mutableStateOf((ev?.maxParticipants ?: 120L).toString()) }
    var posterUri by remember(ev) { mutableStateOf(ev?.posterUri ?: "") }
    var upcoming by remember(ev) { mutableStateOf(ev?.upcoming ?: true) }
    var published by remember(ev) { mutableStateOf(ev?.published ?: true) }

    val posterPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
      if (uri != null) {
        posterUri = ImageEncodingUtils.encodeUriToDataUrl(context, uri, maxDimension = 720, quality = 70) ?: uri.toString()
      }
    }

    AlertDialog(
      onDismissRequest = { showEventDialog = false },
      title = { Text(if (ev == null) "Create Society Event" else "Edit Event") },
      text = {
        Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Event Title *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
          YesDropdownField(
            label = "Event Category *",
            options = SocietyConstants.EVENT_CATEGORIES,
            selectedOption = category,
            onOptionSelected = { category = it },
            testTag = "event_dialog_category_dropdown",
          )
          OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Event Description *") }, minLines = 2, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Event Date (YYYY-MM-DD) *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Timings *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = venue, onValueChange = { venue = it }, label = { Text("Venue *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = deadline, onValueChange = { deadline = it }, label = { Text("Registration Deadline *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = feeStr, onValueChange = { feeStr = it }, label = { Text("Fee (PKR)") }, singleLine = true, modifier = Modifier.weight(1f))
            OutlinedTextField(value = maxStr, onValueChange = { maxStr = it }, label = { Text("Max Seats") }, singleLine = true, modifier = Modifier.weight(1f))
          }
          OutlinedButton(
            onClick = { posterPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            modifier = Modifier.fillMaxWidth(),
          ) {
            Icon(Icons.Outlined.AddAPhoto, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(if (posterUri.isNotBlank()) "Change Event Poster ✓" else "Upload Event Poster (Visible to Students)")
          }
          if (posterUri.isNotBlank()) {
            Surface(
              modifier = Modifier.fillMaxWidth().height(130.dp).clip(RoundedCornerShape(12.dp)),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
            ) {
              SocietyImage(
                imageSource = posterUri,
                contentDescription = "Selected Event Poster",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
              )
            }
          }
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Mark as Upcoming Event")
            Switch(checked = upcoming, onCheckedChange = { upcoming = it })
          }
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Publish to Student Portal & Calendar")
            Switch(checked = published, onCheckedChange = { published = it })
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onSaveEvent(
              ev,
              title,
              desc.ifBlank { title },
              category,
              date,
              time,
              venue,
              posterUri,
              deadline,
              feeStr.toDoubleOrNull() ?: 0.0,
              maxStr.toLongOrNull() ?: 100L,
              upcoming,
              published,
            )
            showEventDialog = false
          },
          enabled = title.trim().length >= 3 && venue.trim().length >= 2,
        ) {
          Text("Publish Event")
        }
      },
      dismissButton = {
        TextButton(onClick = { showEventDialog = false }) { Text("Cancel") }
      },
    )
  }
}

@Composable
fun AdminAnnouncementsTab(
  canManageAnnouncements: Boolean,
  announcements: List<Announcement>,
  onSaveAnnouncement: (Announcement?, String, String, String, String, String, String, Boolean) -> Unit,
  onDeleteAnnouncement: (Announcement) -> Unit,
) {
  var categoryFilter by remember { mutableStateOf("ALL") }
  var editingAnn by remember { mutableStateOf<Announcement?>(null) }
  var showDialog by remember { mutableStateOf(false) }
  var annToDelete by remember { mutableStateOf<Announcement?>(null) }

  val categories = listOf("GENERAL", "MEETING", "DEADLINE", "URGENT", "EVENT_REMINDER")
  val filteredAnnouncements = remember(announcements, categoryFilter) {
    if (categoryFilter == "ALL") announcements
    else announcements.filter { it.category == categoryFilter }
  }

  if (annToDelete != null) {
    val target = annToDelete!!
    ConfirmDeleteDialog(
      itemTitle = target.title,
      itemSubtitle = "Permanently delete this announcement from society notices?",
      onConfirmDelete = { onDeleteAnnouncement(target) },
      onDismiss = { annToDelete = null },
    )
  }

  Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      item {
        YesDropdownField(
          label = "Filter Notices by Category",
          options = listOf("ALL") + categories,
          selectedOption = categoryFilter,
          onOptionSelected = { categoryFilter = it },
          optionLabel = { if (it == "ALL") "All Categories (${announcements.size})" else it.replace("_", " ") },
          testTag = "admin_announcements_filter_dropdown",
        )
      }

      if (filteredAnnouncements.isEmpty()) {
        item {
          EmptyStateCard(
            title = "No Announcements Published",
            subtitle = "Share meeting schedules, registration deadlines, and urgent notices with students.",
            icon = Icons.Filled.Campaign,
          )
        }
      } else {
        items(filteredAnnouncements, key = { it.id }) { ann ->
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
          ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StatusPill(label = ann.priority)
                StatusPill(label = ann.category)
                StatusPill(label = ann.targetGroup)
              }
              Text(ann.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
              Text(ann.message, style = MaterialTheme.typography.bodyMedium)
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text("By ${ann.authorName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (canManageAnnouncements) {
                  Row {
                    IconButton(onClick = {
                      editingAnn = ann
                      showDialog = true
                    }) {
                      Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                    IconButton(
                      onClick = { annToDelete = ann },
                      modifier = Modifier.testTag("delete_announcement_${ann.id}"),
                    ) {
                      Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    if (canManageAnnouncements) {
      FloatingActionButton(
        onClick = {
          editingAnn = null
          showDialog = true
        },
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(18.dp)
          .testTag("admin_add_announcement_fab"),
      ) {
        Icon(Icons.Filled.Add, contentDescription = "Publish Announcement")
      }
    }
  }

  if (showDialog) {
    val ann = editingAnn
    var title by remember(ann) { mutableStateOf(ann?.title ?: "") }
    var message by remember(ann) { mutableStateOf(ann?.message ?: "") }
    var category by remember(ann) { mutableStateOf(ann?.category ?: "GENERAL") }
    var targetGroup by remember(ann) { mutableStateOf(ann?.targetGroup ?: "ALL_STUDENTS") }
    var priority by remember(ann) { mutableStateOf(ann?.priority ?: "NORMAL") }
    var scheduledDate by remember(ann) { mutableStateOf(ann?.scheduledDate ?: "2026-11-10") }
    var published by remember(ann) { mutableStateOf(ann?.published ?: true) }

    AlertDialog(
      onDismissRequest = { showDialog = false },
      title = { Text(if (ann == null) "Publish Announcement / Alert" else "Edit Announcement") },
      text = {
        Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = message, onValueChange = { message = it }, label = { Text("Message *") }, minLines = 3, modifier = Modifier.fillMaxWidth())
          YesDropdownField(
            label = "Notice Category",
            options = categories,
            selectedOption = category,
            onOptionSelected = { category = it },
            optionLabel = { it.replace("_", " ") },
          )
          YesDropdownField(
            label = "Target Audience",
            options = listOf("ALL_STUDENTS", "ACTIVE_MEMBERS", "EXECUTIVE_BODY"),
            selectedOption = targetGroup,
            onOptionSelected = { targetGroup = it },
            optionLabel = { it.replace("_", " ") },
          )
          YesDropdownField(
            label = "Priority Level",
            options = listOf("NORMAL", "IMPORTANT", "URGENT"),
            selectedOption = priority,
            onOptionSelected = { priority = it },
          )
          OutlinedTextField(value = scheduledDate, onValueChange = { scheduledDate = it }, label = { Text("Schedule / Deadline Date") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onSaveAnnouncement(ann, title, message, category, targetGroup, priority, scheduledDate, published)
            showDialog = false
          },
          enabled = title.trim().length >= 3 && message.trim().length >= 3,
        ) {
          Text("Publish")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDialog = false }) { Text("Cancel") }
      },
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminCabinetIdCardsTab(
  admin: AdminAccount,
  admins: List<AdminAccount>,
  students: List<StudentProfile>,
  executiveBody: List<ExecutiveMember>,
  users: List<UserAccount>,
  currentPhotoUri: String = "",
  onMessage: (String) -> Unit,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  var searchQuery by remember { mutableStateOf("") }
  var roleFilter by remember { mutableStateOf("ALL") }
  var showFullCards by remember { mutableStateOf(true) }
  var previewOfficer by remember { mutableStateOf<StudentProfile?>(null) }
  var pdfExportTarget by remember { mutableStateOf<StudentProfile?>(null) }

  val pdfLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.CreateDocument("application/pdf")
  ) { uri: Uri? ->
    val profile = pdfExportTarget
    if (uri != null && profile != null) {
      scope.launch {
        PdfExportManager.exportStudentIdCardPdf(context, uri, profile)
          .onSuccess { onMessage("Cabinet ID Card PDF saved for ${profile.fullName}!") }
          .onFailure { onMessage("Failed to save ID Card PDF: ${it.localizedMessage}") }
      }
    }
    pdfExportTarget = null
  }

  if (previewOfficer != null) {
    val officer = previewOfficer!!
    OfficialIdCardPreviewDialog(
      student = officer,
      onDownloadPdf = {
        pdfExportTarget = officer
        val safeName = officer.fullName.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        pdfLauncher.launch("YES_Cabinet_ID_${safeName}.pdf")
      },
      onDismiss = { previewOfficer = null },
    )
  }

  val allCabinetCards = remember(admins, students, executiveBody, users, admin, currentPhotoUri) {
    buildAdministrativeCabinetIdCards(
      admins = admins,
      students = students,
      executiveBody = executiveBody,
      users = users,
      currentAdminUid = admin.uid.ifBlank { admin.id },
      currentPhotoUri = currentPhotoUri,
    )
  }

  val myCabinetProfile = remember(admin, students, users, executiveBody, currentPhotoUri) {
    if (admin.isCabinetOfficerRole()) {
      admin.toCabinetIdCardProfile(
        students = students,
        users = users,
        executiveBody = executiveBody,
        fallbackPhotoUri = currentPhotoUri,
      )
    } else {
      null
    }
  }

  val roleOptions = remember(allCabinetCards) {
    listOf("ALL") + allCabinetCards.map { it.position }.filter { it.isNotBlank() }.distinct()
  }

  val filteredCards = remember(allCabinetCards, searchQuery, roleFilter) {
    val q = searchQuery.trim().lowercase()
    allCabinetCards.filter { card ->
      val roleOk = roleFilter == "ALL" || card.position.equals(roleFilter, ignoreCase = true)
      val queryOk = q.isEmpty() ||
        card.fullName.lowercase().contains(q) ||
        card.position.lowercase().contains(q) ||
        card.registrationNumber.lowercase().contains(q) ||
        card.department.lowercase().contains(q)
      roleOk && queryOk
    }
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize().testTag("admin_cabinet_id_cards_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    // Header & Global Theme Switcher
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              Icon(Icons.Filled.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Column {
                Text(
                  text = "Cabinet & Administrative ID Cards",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.ExtraBold,
                )
                Text(
                  text = "Official ID cards of all cabinet officers & secretaries (${allCabinetCards.size})",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }
        }
      }
    }

    // Logged-in Cabinet Officer's Own ID Card
    if (myCabinetProfile != null) {
      item {
        SectionTitleRow(title = "My Cabinet Officer ID Card (${myCabinetProfile.position})")
        DigitalMembershipCard(
          student = myCabinetProfile,
          showThemeSelector = false,
          onDownloadPdf = {
            pdfExportTarget = myCabinetProfile
            val safeName = myCabinetProfile.fullName.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
            pdfLauncher.launch("YES_Cabinet_ID_${safeName}.pdf")
          },
        )
      }
    }

    // Search & Role Filter
    item {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        label = { Text("Search by Name, Role (Secretary, President...) or Reg No") },
        leadingIcon = { Icon(Icons.Filled.PersonSearch, contentDescription = null) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("cabinet_id_search_input"),
      )
    }

    item {
      YesDropdownField(
        label = "Filter by Cabinet / Administrative Role",
        options = roleOptions,
        selectedOption = roleFilter,
        onOptionSelected = { roleFilter = it },
        optionLabel = { if (it == "ALL") "All Cabinet & Administrative Roles (${allCabinetCards.size})" else it },
        testTag = "cabinet_id_role_dropdown",
      )
    }

    item {
      SectionTitleRow(title = "All Cabinet & Administrative ID Cards (${filteredCards.size})")
    }

    if (filteredCards.isEmpty()) {
      item {
        EmptyStateCard(
          title = "No Cabinet ID Cards Found",
          subtitle = "Appoint students to Cabinet / Secretary roles in Admin Roles, Executive Body, or Students to display their official ID cards here.",
          icon = Icons.Filled.Badge,
        )
      }
    } else {
      items(filteredCards, key = { "cab_card_${it.studentUid}_${it.position}" }) { officer ->
        if (showFullCards) {
          DigitalMembershipCard(
            student = officer,
            showThemeSelector = false,
            onDownloadPdf = {
              pdfExportTarget = officer
              val safeName = officer.fullName.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
              pdfLauncher.launch("YES_Cabinet_ID_${safeName}.pdf")
            },
          )
        } else {
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
          ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Surface(
                  modifier = Modifier.size(52.dp),
                  shape = CircleShape,
                  color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                  if (officer.photoUri.isNotBlank()) {
                    SocietyImage(
                      imageSource = officer.photoUri,
                      contentDescription = officer.fullName,
                      modifier = Modifier.fillMaxSize().clip(CircleShape),
                      contentScale = ContentScale.Crop,
                    )
                  } else {
                    Box(contentAlignment = Alignment.Center) {
                      Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                  }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                  if (officer.hasAllocatedRole()) {
                    StatusPill(label = officer.allocatedRoleOrNull()!!)
                  }
                  Text(officer.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                  Text(
                    "${officer.registrationNumber} • ${officer.department}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Button(
                  onClick = { previewOfficer = officer },
                  modifier = Modifier.weight(1f).testTag("cabinet_tab_view_id_${officer.studentUid}"),
                ) {
                  Icon(Icons.Filled.Badge, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(Modifier.width(6.dp))
                  Text("View ID Card")
                }
                OutlinedButton(
                  onClick = {
                    pdfExportTarget = officer
                    val safeName = officer.fullName.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
                    pdfLauncher.launch("YES_Cabinet_ID_${safeName}.pdf")
                  },
                  modifier = Modifier.weight(1f).testTag("cabinet_tab_download_id_${officer.studentUid}"),
                ) {
                  Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(Modifier.width(6.dp))
                  Text("Download PDF")
                }
              }
            }
          }
        }
      }
    }
  }
}

