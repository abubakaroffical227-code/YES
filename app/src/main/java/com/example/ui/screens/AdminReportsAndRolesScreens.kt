package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.AddModerator
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableView
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AdminAccount
import com.example.data.model.AdminAuditLog
import com.example.data.model.AdminRoleType
import com.example.data.model.AttendanceRecord
import com.example.data.model.BOOTSTRAPPED_SUPER_ADMIN_EMAIL
import com.example.data.model.EventRegistration
import com.example.data.model.ExecutiveMember
import com.example.data.model.FinanceTransaction
import com.example.data.model.IdCardDesignTheme
import com.example.data.model.MembershipApplication
import com.example.data.model.SocietyEvent
import com.example.data.model.StudentFeedback
import com.example.data.model.StudentProfile
import com.example.data.model.UserAccount
import com.example.data.model.hasPermission
import com.example.data.model.hasSuperAdminAccess
import com.example.data.model.isCabinetOfficerRole
import com.example.data.model.isIdCardActive
import com.example.data.model.isVerifiedMember
import com.example.data.model.resolvedDisplayDesignation
import com.example.data.model.toCabinetIdCardProfile
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.OfficialIdCardPreviewDialog
import com.example.ui.components.SectionTitleRow
import com.example.ui.components.SocietyImage
import com.example.ui.components.StatMetricCard
import com.example.ui.components.StatusPill
import com.example.ui.components.YesDropdownField
import com.example.util.PdfExportManager
import kotlinx.coroutines.launch

@Composable
fun AdminFeedbackModuleScreen(
  feedbackList: List<StudentFeedback>,
  onRespond: (String, String, String) -> Unit,
  onDeleteFeedback: (StudentFeedback) -> Unit,
) {
  var statusFilter by remember { mutableStateOf("ALL") }
  var replyingItem by remember { mutableStateOf<StudentFeedback?>(null) }
  var fbToDelete by remember { mutableStateOf<StudentFeedback?>(null) }

  val filteredFeedback = remember(feedbackList, statusFilter) {
    if (statusFilter == "ALL") feedbackList
    else feedbackList.filter { it.status == statusFilter }
  }

  if (fbToDelete != null) {
    val target = fbToDelete!!
    ConfirmDeleteDialog(
      itemTitle = "${target.subject} (${target.studentName})",
      itemSubtitle = "Permanently delete this student feedback entry?",
      onConfirmDelete = { onDeleteFeedback(target) },
      onDismiss = { fbToDelete = null },
    )
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      YesDropdownField(
        label = "Filter Feedback Status",
        options = listOf("ALL", "SUBMITTED", "IN_REVIEW", "RESOLVED"),
        selectedOption = statusFilter,
        onOptionSelected = { statusFilter = it },
        optionLabel = { if (it == "ALL") "All Submissions (${feedbackList.size})" else it.replace("_", " ") },
        testTag = "admin_feedback_status_dropdown",
      )
    }

    if (filteredFeedback.isEmpty()) {
      item {
        EmptyStateCard(
          title = "No Student Feedback Submitted",
          subtitle = "Student suggestions, event reviews, and inquiries appear here.",
          icon = Icons.Filled.Feedback,
        )
      }
    } else {
      items(filteredFeedback, key = { it.id }) { fb ->
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
              Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StatusPill(label = fb.category)
                StatusPill(label = fb.status)
              }
              Text(fb.studentName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            Text(fb.subject, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(fb.message, style = MaterialTheme.typography.bodyMedium)
            if (fb.relatedEventTitle.isNotBlank()) {
              Text("Event: ${fb.relatedEventTitle}", style = MaterialTheme.typography.labelSmall)
            }
            if (fb.adminResponse.isNotBlank()) {
              Text(
                "Executive Reply: ${fb.adminResponse}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
              )
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              OutlinedButton(onClick = { replyingItem = fb }) {
                Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (fb.adminResponse.isBlank()) "Respond" else "Update Response")
              }
              IconButton(
                onClick = { fbToDelete = fb },
                modifier = Modifier.testTag("delete_feedback_${fb.id}"),
              ) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete Feedback", tint = MaterialTheme.colorScheme.error)
              }
            }
          }
        }
      }
    }
  }

  if (replyingItem != null) {
    val fb = replyingItem!!
    var status by remember(fb) { mutableStateOf(if (fb.status == "SUBMITTED") "RESOLVED" else fb.status) }
    var replyText by remember(fb) { mutableStateOf(fb.adminResponse) }

    AlertDialog(
      onDismissRequest = { replyingItem = null },
      title = { Text("Respond to ${fb.studentName}") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          YesDropdownField(
            label = "Resolution Status",
            options = listOf("IN_REVIEW", "RESOLVED"),
            selectedOption = status,
            onOptionSelected = { status = it },
            optionLabel = { it.replace("_", " ") },
          )
          OutlinedTextField(
            value = replyText,
            onValueChange = { replyText = it },
            label = { Text("Official Executive Response *") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onRespond(fb.id, status, replyText)
            replyingItem = null
          },
          enabled = replyText.trim().length >= 3,
        ) {
          Text("Send Reply")
        }
      },
      dismissButton = {
        TextButton(onClick = { replyingItem = null }) { Text("Cancel") }
      },
    )
  }
}

private data class ExportPayload(
  val title: String,
  val subtitle: String,
  val headers: List<String>,
  val rows: List<List<String>>,
)

@Composable
fun AdminReportsModuleScreen(
  admin: AdminAccount,
  students: List<StudentProfile>,
  applications: List<MembershipApplication>,
  events: List<SocietyEvent>,
  registrations: List<EventRegistration>,
  attendance: List<AttendanceRecord>,
  financeTransactions: List<FinanceTransaction>,
  onMessage: (String) -> Unit,
) {
  val context = LocalContext.current
  val canSeeFinance = admin.hasPermission("MANAGE_FINANCE")
  var selectedEventFilter by remember { mutableStateOf("ALL") }
  var pendingExport by remember { mutableStateOf<ExportPayload?>(null) }

  val pdfLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.CreateDocument("application/pdf")
  ) { uri: Uri? ->
    val p = pendingExport
    if (uri != null && p != null) {
      PdfExportManager.writeSummaryReportPdfToUri(
        context = context,
        uri = uri,
        reportTitle = p.title,
        subtitle = p.subtitle,
        headers = p.headers,
        rows = p.rows,
      ).onSuccess { onMessage("${p.title} PDF saved!") }
        .onFailure { onMessage("PDF export failed: ${it.localizedMessage}") }
    }
  }

  val csvLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.CreateDocument("text/csv")
  ) { uri: Uri? ->
    val p = pendingExport
    if (uri != null && p != null) {
      PdfExportManager.writeCsvReportToUri(
        context = context,
        uri = uri,
        headers = p.headers,
        rows = p.rows,
      ).onSuccess { onMessage("${p.title} Excel/CSV saved!") }
        .onFailure { onMessage("CSV export failed: ${it.localizedMessage}") }
    }
  }

  fun buildPayload(key: String): ExportPayload = when (key) {
    "MEMBERS" -> ExportPayload(
      title = "Society Members Directory",
      subtitle = "Department of Entomology • Total Students: ${students.size} | Verified Members: ${students.count { it.isVerifiedMember() }}",
      headers = listOf("Full Name", "Reg No", "Position", "Department", "Semester", "Verification", "ID Card", "Member ID"),
      rows = students.map {
        listOf(
          it.fullName,
          it.registrationNumber,
          it.position.ifBlank { "General Member" },
          it.department,
          it.semester,
          it.membershipStatus,
          if (it.isIdCardActive()) "ISSUED" else it.idCardStatus.ifBlank { "NOT_ISSUED" },
          it.membershipId.ifBlank { "-" },
        )
      },
    )
    "APPLICATIONS" -> ExportPayload(
      title = "Membership Applications Report",
      subtitle = "Department of Entomology • Total Applications: ${applications.size}",
      headers = listOf("Student", "Reg No", "Department", "Status", "Assigned ID"),
      rows = applications.map {
        listOf(it.studentName, it.registrationNumber, it.department, it.status, it.assignedMembershipId.ifBlank { "-" })
      },
    )
    "REGISTRATIONS" -> {
      val list = if (selectedEventFilter == "ALL") registrations else registrations.filter { it.eventId == selectedEventFilter }
      ExportPayload(
        title = "Event Registrations & Fee Status",
        subtitle = "Department of Entomology • Filtered Records: ${list.size}",
        headers = listOf("Event", "Student", "Reg No", "Fee_PKR", "Payment", "Status"),
        rows = list.map {
          listOf(it.eventTitle, it.studentName, it.registrationNumber, it.feeAmount.toInt().toString(), it.paymentStatus, it.registrationStatus)
        },
      )
    }
    "ATTENDANCE" -> {
      val list = if (selectedEventFilter == "ALL") attendance else attendance.filter { it.eventId == selectedEventFilter }
      ExportPayload(
        title = "Student Attendance Register",
        subtitle = "Department of Entomology • Present: ${list.count { it.status == "PRESENT" }} / Total: ${list.size}",
        headers = listOf("Event", "Date", "Student", "Reg No", "Status", "Method"),
        rows = list.map {
          listOf(it.eventTitle, it.eventDate, it.studentName, it.registrationNumber, it.status, it.method)
        },
      )
    }
    else -> ExportPayload(
      title = "Society Financial Statement",
      subtitle = "Department of Entomology • Transactions: ${financeTransactions.size}",
      headers = listOf("Date", "Type", "Category", "Description", "Amount_PKR", "Recorded_By"),
      rows = financeTransactions.map {
        listOf(it.transactionDate, it.type, it.category, it.title, it.amount.toInt().toString(), it.recordedByName)
      },
    )
  }

  val reportCards = buildList {
    add(Triple("MEMBERS", "Society Members Directory", "${students.size} students • ${students.count { it.isVerifiedMember() }} verified members"))
    add(Triple("APPLICATIONS", "Membership Applications", "${applications.size} applications • ${applications.count { it.status == "PENDING" }} pending"))
    add(Triple("REGISTRATIONS", "Event Participant & Fee Lists", "${registrations.size} registrations across ${events.size} events"))
    add(Triple("ATTENDANCE", "Attendance Analytics Report", "${attendance.size} check-ins • ${attendance.count { it.status == "PRESENT" }} present"))
    if (canSeeFinance) {
      add(Triple("FINANCE", "Financial Ledger Statement", "${financeTransactions.size} verified income & expense entries"))
    }
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        StatMetricCard(
          title = "Verified Membership Rate",
          value = if (students.isEmpty()) "0%" else "${(students.count { it.isVerifiedMember() } * 100) / students.size}%",
          icon = Icons.Filled.Assessment,
          modifier = Modifier.weight(1f),
        )
        StatMetricCard(
          title = "Attendance Rate",
          value = if (attendance.isEmpty()) "0%" else "${(attendance.count { it.status == "PRESENT" } * 100) / attendance.size}%",
          icon = Icons.Filled.Download,
          modifier = Modifier.weight(1f),
        )
      }
    }

    if (events.isNotEmpty()) {
      item {
        YesDropdownField(
          label = "Event Filter (for Registrations & Attendance Exports)",
          options = listOf("ALL") + events.map { it.id },
          selectedOption = selectedEventFilter,
          onOptionSelected = { selectedEventFilter = it },
          optionLabel = { id ->
            if (id == "ALL") "All Society Events (${events.size})"
            else events.firstOrNull { it.id == id }?.title ?: id
          },
          testTag = "admin_reports_event_dropdown",
        )
      }
    }

    items(reportCards, key = { it.first }) { (key, title, desc) ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
              onClick = {
                val payload = buildPayload(key)
                pendingExport = payload
                pdfLauncher.launch("YES_${key}_Report.pdf")
              },
              modifier = Modifier.weight(1f).testTag("export_pdf_${key.lowercase()}"),
            ) {
              Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(Modifier.width(6.dp))
              Text("PDF")
            }
            OutlinedButton(
              onClick = {
                val payload = buildPayload(key)
                pendingExport = payload
                csvLauncher.launch("YES_${key}_Report.csv")
              },
              modifier = Modifier.weight(1f).testTag("export_csv_${key.lowercase()}"),
            ) {
              Icon(Icons.Filled.TableView, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(Modifier.width(6.dp))
              Text("Excel / CSV")
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminRolesModuleScreen(
  currentAdmin: AdminAccount,
  admins: List<AdminAccount>,
  students: List<StudentProfile>,
  users: List<UserAccount>,
  executiveBody: List<ExecutiveMember> = emptyList(),
  onAssignAdminRole: (String, String, String, AdminRoleType, String, List<String>, Boolean) -> Unit,
  onRevokeAdminRole: (AdminAccount) -> Unit,
  onMessage: (String) -> Unit = {},
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val isSuperAdmin = currentAdmin.hasSuperAdminAccess()
  var roleFilter by remember { mutableStateOf("ALL") }
  var editingAdmin by remember { mutableStateOf<AdminAccount?>(null) }
  var showAssignDialog by remember { mutableStateOf(false) }
  var adminToRevoke by remember { mutableStateOf<AdminAccount?>(null) }
  var previewOfficerIdCard by remember { mutableStateOf<StudentProfile?>(null) }
  var officerPdfTarget by remember { mutableStateOf<Pair<StudentProfile, IdCardDesignTheme>?>(null) }

  val officerPdfLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.CreateDocument("application/pdf")
  ) { uri: Uri? ->
    val target = officerPdfTarget
    if (uri != null && target != null) {
      val (profile, theme) = target
      scope.launch {
        PdfExportManager.exportStudentIdCardPdf(context, uri, profile, theme)
          .onSuccess { onMessage("Official Cabinet ID Card PDF saved for ${profile.fullName}!") }
          .onFailure { onMessage("Failed to save ID Card PDF: ${it.localizedMessage}") }
      }
    }
    officerPdfTarget = null
  }

  if (previewOfficerIdCard != null) {
    val profile = previewOfficerIdCard!!
    OfficialIdCardPreviewDialog(
      student = profile,
      onDownloadPdf = { theme ->
        officerPdfTarget = profile to theme
        val safeName = profile.fullName.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        officerPdfLauncher.launch("YES_Cabinet_ID_${safeName}.pdf")
      },
      onDismiss = { previewOfficerIdCard = null },
    )
  }

  val filteredAdmins = remember(admins, roleFilter) {
    if (roleFilter == "ALL") admins
    else admins.filter { it.adminRole == roleFilter }
  }

  if (adminToRevoke != null) {
    val target = adminToRevoke!!
    ConfirmDeleteDialog(
      itemTitle = "${target.fullName} — ${target.resolvedDisplayDesignation()}",
      itemSubtitle = "Revoke and delete administrative privileges for ${target.email}?",
      onConfirmDelete = { onRevokeAdminRole(target) },
      onDismiss = { adminToRevoke = null },
    )
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text("Administrative & Executive Roles (${AdminRoleType.entries.size} Roles)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          }
          Text(
            "Manage role-based portal access across Administrator, President, Convener, Advisory Board, Senior Vice President, Vice President, Councillor, General Secretary, Joint Secretary, Finance Secretary, IT and Media Cell, Press Secretary, Information Secretary, Event Secretary, Sports Secretary, Attendance Manager, and Associate offices.",
            style = MaterialTheme.typography.bodySmall,
          )
          if (isSuperAdmin) {
            Spacer(Modifier.height(4.dp))
            Button(
              onClick = {
                editingAdmin = null
                showAssignDialog = true
              },
              modifier = Modifier.testTag("assign_admin_role_btn"),
            ) {
              Icon(Icons.Filled.AddModerator, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(Modifier.width(8.dp))
              Text("Appoint Administrator (Search by Reg No)")
            }
          }
        }
      }
    }

    item {
      YesDropdownField(
        label = "Filter Administrators by Role",
        options = listOf("ALL") + AdminRoleType.entries.map { it.key },
        selectedOption = roleFilter,
        onOptionSelected = { roleFilter = it },
        optionLabel = { key ->
          if (key == "ALL") "All Authorized Administrators (${admins.size})"
          else AdminRoleType.fromKey(key).displayTitle
        },
        testTag = "admin_roles_filter_dropdown",
      )
    }

    item {
      SectionTitleRow(title = "Authorized Administrators (${filteredAdmins.size})")
    }

    items(filteredAdmins, key = { it.id.ifBlank { it.uid } }) { adm ->
      val isOwnerRoot = adm.email.equals(BOOTSTRAPPED_SUPER_ADMIN_EMAIL, ignoreCase = true)
      val isCabinetOfficer = adm.isCabinetOfficerRole()
      val officerCardProfile = remember(adm, students, users, executiveBody) {
        if (isCabinetOfficer) adm.toCabinetIdCardProfile(students, users, executiveBody) else null
      }
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
              modifier = Modifier.size(48.dp),
              shape = CircleShape,
              color = MaterialTheme.colorScheme.primaryContainer,
            ) {
              if (officerCardProfile != null && officerCardProfile.photoUri.isNotBlank()) {
                SocietyImage(
                  imageSource = officerCardProfile.photoUri,
                  contentDescription = adm.fullName,
                  modifier = Modifier.fillMaxSize().clip(CircleShape),
                  contentScale = ContentScale.Crop,
                )
              } else {
                Box(contentAlignment = Alignment.Center) {
                  Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
              }
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(adm.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
              if (officerCardProfile != null) {
                Text(
                  "${officerCardProfile.registrationNumber} • ${officerCardProfile.department}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.primary,
                )
              } else {
                Text(adm.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
            StatusPill(label = adm.resolvedDisplayDesignation())
          }
          Text(
            text = "Permissions: ${adm.permissions.joinToString(", ").ifBlank { AdminRoleType.fromKey(adm.adminRole).defaultPermissions.joinToString(", ") }}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
          )
          Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
          ) {
            if (isCabinetOfficer && officerCardProfile != null) {
              OutlinedButton(
                onClick = { previewOfficerIdCard = officerCardProfile },
                modifier = Modifier.testTag("view_admin_officer_id_${adm.uid.ifBlank { adm.id }}"),
              ) {
                Icon(Icons.Filled.Badge, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("View ID Card")
              }
            } else {
              Spacer(Modifier.width(1.dp))
            }

            if (isSuperAdmin && !isOwnerRoot && adm.uid != currentAdmin.uid) {
              Row {
                IconButton(
                  onClick = {
                    editingAdmin = adm
                    showAssignDialog = true
                  },
                ) {
                  Icon(Icons.Filled.Edit, contentDescription = "Update Admin Role")
                }
                IconButton(
                  onClick = { adminToRevoke = adm },
                  modifier = Modifier.testTag("delete_admin_${adm.uid}"),
                ) {
                  Icon(Icons.Filled.Delete, contentDescription = "Delete Admin Role", tint = MaterialTheme.colorScheme.error)
                }
              }
            }
          }
        }
      }
    }
  }

  if (showAssignDialog) {
    val existing = editingAdmin
    var regSearchQuery by remember(existing) { mutableStateOf("") }
    var matchedStudent by remember(existing, students) {
      mutableStateOf(
        if (existing != null) {
          val eUid = existing.uid.ifBlank { existing.id }
          students.firstOrNull { it.studentUid == eUid || it.id == eUid }
        } else null
      )
    }

    var targetUid by remember(existing) { mutableStateOf(existing?.uid?.ifBlank { existing.id } ?: "") }
    var email by remember(existing) { mutableStateOf(existing?.email ?: "") }
    var fullName by remember(existing) { mutableStateOf(existing?.fullName ?: "") }
    var roleType by remember(existing) {
      mutableStateOf(
        if (existing != null) AdminRoleType.fromKey(existing.adminRole)
        else AdminRoleType.PRESIDENT
      )
    }
    var designation by remember(existing) {
      mutableStateOf(existing?.designation?.ifBlank { roleType.defaultDesignation } ?: roleType.defaultDesignation)
    }
    var selectedPerms by remember(existing) {
      mutableStateOf(
        (existing?.permissions ?: emptyList()).toSet()
      )
    }

    // Strictly search by Registration Number ONLY when appointing an administrator
    val regNoMatches = remember(students, regSearchQuery) {
      val q = regSearchQuery.trim()
      if (q.isEmpty()) emptyList()
      else students.filter { st ->
        !st.email.equals(BOOTSTRAPPED_SUPER_ADMIN_EMAIL, ignoreCase = true) &&
          st.registrationNumber.contains(q, ignoreCase = true)
      }.take(8)
    }

    val allPerms = com.example.data.model.SocietyConstants.ALL_PERMISSIONS

    AlertDialog(
      onDismissRequest = { showAssignDialog = false },
      title = { Text(if (existing == null) "Appoint Administrator by Registration No" else "Update Administrator Role") },
      text = {
        Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          if (existing == null) {
            Text(
              "Search strictly by Registration Number (e.g. 2023-ag-4512) to find and select the registered student for appointment:",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
              value = regSearchQuery,
              onValueChange = { regSearchQuery = it },
              label = { Text("Search Registration Number Only *") },
              placeholder = { Text("e.g. 2023-ag-4512") },
              leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("appoint_admin_regno_search"),
            )

            if (regSearchQuery.isNotBlank()) {
              if (regNoMatches.isEmpty()) {
                Text(
                  "No registered student found matching Registration No \"${regSearchQuery.trim()}\".",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.error,
                )
              } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                  regNoMatches.forEach { st ->
                    val uidVal = st.studentUid.ifBlank { st.id }
                    val isSelected = targetUid == uidVal
                    Card(
                      modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                          matchedStudent = st
                          targetUid = uidVal
                          fullName = st.fullName
                          email = st.email
                        }
                        .testTag("regno_match_${st.registrationNumber}"),
                      colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                      ),
                      border = BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                      ),
                    ) {
                      Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                      ) {
                        Column(modifier = Modifier.weight(1f)) {
                          Text("Reg No: ${st.registrationNumber}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                          Text("${st.fullName} • ${st.department}", style = MaterialTheme.typography.bodySmall)
                          Text(st.email, style = MaterialTheme.typography.labelSmall)
                        }
                        if (isSelected) {
                          Icon(Icons.Filled.CheckCircle, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                        }
                      }
                    }
                  }
                }
              }
            }
          }

          if (targetUid.isNotBlank()) {
            Card(
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Selected User for Appointment", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text("Name: $fullName", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                if (matchedStudent != null) {
                  Text("Registration No: ${matchedStudent!!.registrationNumber}", style = MaterialTheme.typography.bodySmall)
                }
                Text("Email: $email", style = MaterialTheme.typography.bodySmall)
              }
            }
          }

          YesDropdownField(
            label = "Select Executive, Convener, Advisory or Associate Role",
            options = AdminRoleType.entries.map { it.key },
            selectedOption = roleType.key,
            onOptionSelected = { key ->
              val rt = AdminRoleType.fromKey(key)
              roleType = rt
              designation = rt.defaultDesignation
              selectedPerms = rt.defaultPermissions.toSet()
            },
            optionLabel = { key -> AdminRoleType.fromKey(key).displayTitle },
            testTag = "appoint_admin_role_dropdown",
          )

          OutlinedTextField(
            value = designation,
            onValueChange = { designation = it },
            label = { Text("Official Designation") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )

          Text("Granular Permissions", style = MaterialTheme.typography.labelMedium)
          FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            allPerms.forEach { perm ->
              val checked = selectedPerms.contains(perm)
              FilterChip(
                selected = checked,
                onClick = {
                  selectedPerms = if (checked) selectedPerms - perm else selectedPerms + perm
                },
                label = { Text(perm.replace("MANAGE_", "").replace("_", " ")) },
              )
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onAssignAdminRole(
              targetUid.trim(),
              email.trim(),
              fullName.trim(),
              roleType,
              designation.ifBlank { roleType.defaultDesignation },
              selectedPerms.toList(),
              true,
            )
            showAssignDialog = false
          },
          enabled = targetUid.isNotBlank() && email.contains("@") && fullName.trim().length >= 2,
          modifier = Modifier.testTag("confirm_appoint_admin_btn"),
        ) {
          Text("Save Appointment")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAssignDialog = false }) { Text("Cancel") }
      },
    )
  }
}

@Composable
fun AdminActivityLogsModuleScreen(
  currentAdmin: AdminAccount,
  auditLogs: List<AdminAuditLog>,
  onDeleteLog: (AdminAuditLog) -> Unit,
) {
  val canDeleteLogs = currentAdmin.hasSuperAdminAccess()
  var searchQuery by remember { mutableStateOf("") }
  var actionFilter by remember { mutableStateOf("ALL") }
  var logToDelete by remember { mutableStateOf<AdminAuditLog?>(null) }

  val actionTypes = remember(auditLogs) {
    listOf("ALL") + auditLogs.map { it.action }.distinct()
  }

  if (logToDelete != null) {
    val target = logToDelete!!
    ConfirmDeleteDialog(
      itemTitle = target.action.replace("_", " "),
      itemSubtitle = "Remove this activity entry (${target.targetSummary})?",
      onConfirmDelete = { onDeleteLog(target) },
      onDismiss = { logToDelete = null },
    )
  }

  val filteredLogs = remember(auditLogs, searchQuery, actionFilter) {
    val q = searchQuery.trim().lowercase()
    auditLogs.filter { log ->
      val actionOk = actionFilter == "ALL" || log.action == actionFilter
      val queryOk = q.isEmpty() ||
        log.action.lowercase().contains(q) ||
        log.actorName.lowercase().contains(q) ||
        log.actorRole.lowercase().contains(q) ||
        log.targetSummary.lowercase().contains(q)
      actionOk && queryOk
    }
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        label = { Text("Search activity by action, office bearer, or target") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("activity_logs_search_input"),
      )
    }

    item {
      YesDropdownField(
        label = "Filter Activity Type",
        options = actionTypes,
        selectedOption = actionFilter,
        onOptionSelected = { actionFilter = it },
        optionLabel = { if (it == "ALL") "All Actions (${auditLogs.size})" else it.replace("_", " ") },
        testTag = "activity_logs_type_dropdown",
      )
    }

    item {
      SectionTitleRow(title = "Recent Administrative Activity (${filteredLogs.size})")
    }

    if (filteredLogs.isEmpty()) {
      item {
        EmptyStateCard(
          title = "No Administrative Activity Found",
          subtitle = "All executive actions across students, events, memberships, certificates, and finances are logged here.",
          icon = Icons.Filled.History,
        )
      }
    } else {
      items(filteredLogs, key = { it.id }) { log ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
              Text(
                log.action.replace("_", " "),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
              )
              Text(log.targetSummary, style = MaterialTheme.typography.bodyMedium)
              Text(
                "By ${log.actorName} (${log.actorRole.replace("_", " ")})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            if (canDeleteLogs) {
              IconButton(
                onClick = { logToDelete = log },
                modifier = Modifier.testTag("delete_audit_log_${log.id}"),
              ) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete Activity Log", tint = MaterialTheme.colorScheme.error)
              }
            }
          }
        }
      }
    }
  }
}
