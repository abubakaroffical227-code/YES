package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AdminAccount
import com.example.data.model.SocietyEvent
import com.example.data.model.hasPermission
import com.example.data.model.isCabinetOfficerRole
import com.example.data.model.resolvedDisplayDesignation
import com.example.data.model.toCabinetIdCardProfile
import com.example.ui.components.CertificateVerificationDialog
import com.example.ui.components.PermissionGuard
import com.example.ui.components.SocietyImage
import com.example.ui.components.UafLogoEmblem
import com.example.ui.components.YesLogoEmblem
import com.example.ui.theme.AppFontSize
import com.example.ui.viewmodel.SocietyViewModel
import com.example.util.ImageEncodingUtils

private enum class AdminPrimaryTab(
  val label: String,
  val filledIcon: ImageVector,
  val outlinedIcon: ImageVector,
  val tag: String,
) {
  OVERVIEW("Overview", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "admin_nav_overview"),
  STUDENTS("Students", Icons.Filled.Groups, Icons.Outlined.Groups, "admin_nav_students"),
  EVENTS("Events", Icons.Filled.Event, Icons.Outlined.Event, "admin_nav_events"),
  ID_CARDS("ID Card", Icons.Filled.Badge, Icons.Outlined.Badge, "admin_nav_id_cards"),
  MORE("Modules", Icons.Filled.Menu, Icons.Outlined.Menu, "admin_nav_more"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
  admin: AdminAccount,
  viewModel: SocietyViewModel,
  appFontSize: AppFontSize,
  onAppFontSizeChange: (AppFontSize) -> Unit,
  snackbarHostState: SnackbarHostState,
  onSignOut: () -> Unit,
) {
  val context = LocalContext.current
  var primaryTab by rememberSaveable { mutableStateOf(AdminPrimaryTab.OVERVIEW) }
  var activeSubModule by rememberSaveable { mutableStateOf<AdminSubModule?>(null) }
  var selectedRegistrationEventId by rememberSaveable { mutableStateOf<String?>(null) }
  var showVerifyDialog by remember { mutableStateOf(false) }
  var eventToRegister by remember { mutableStateOf<SocietyEvent?>(null) }

  val students by viewModel.adminAllStudents.collectAsStateWithLifecycle()
  val blockedAccounts by viewModel.adminBlockedAccounts.collectAsStateWithLifecycle()
  val users by viewModel.adminAllUsers.collectAsStateWithLifecycle()
  val admins by viewModel.adminAllAdmins.collectAsStateWithLifecycle()
  val events by viewModel.adminAllEvents.collectAsStateWithLifecycle()
  val publishedEvents by viewModel.publishedEvents.collectAsStateWithLifecycle()
  val membershipApps by viewModel.adminAllMembershipApps.collectAsStateWithLifecycle()
  val executiveBody by viewModel.adminAllExecutive.collectAsStateWithLifecycle()
  val registrations by viewModel.adminAllRegistrations.collectAsStateWithLifecycle()
  val myRegistrations by viewModel.myRegistrations.collectAsStateWithLifecycle()
  val attendance by viewModel.adminAllAttendance.collectAsStateWithLifecycle()
  val myAttendance by viewModel.myAttendance.collectAsStateWithLifecycle()
  val announcements by viewModel.adminAllAnnouncements.collectAsStateWithLifecycle()
  val payments by viewModel.adminAllPayments.collectAsStateWithLifecycle()
  val financeTransactions by viewModel.adminFinanceTransactions.collectAsStateWithLifecycle()
  val paymentAccounts by viewModel.adminPaymentAccounts.collectAsStateWithLifecycle()
  val activePaymentAccounts by viewModel.activePaymentAccounts.collectAsStateWithLifecycle()
  val galleryItems by viewModel.adminAllGallery.collectAsStateWithLifecycle()
  val certificates by viewModel.adminAllCertificates.collectAsStateWithLifecycle()
  val feedbackList by viewModel.adminAllFeedback.collectAsStateWithLifecycle()
  val auditLogs by viewModel.adminAuditLogs.collectAsStateWithLifecycle()
  val verifiedCertificate by viewModel.verifiedCertificateResult.collectAsStateWithLifecycle()
  val verificationChecked by viewModel.verificationChecked.collectAsStateWithLifecycle()

  val calendarEvents = remember(events, publishedEvents) {
    (events + publishedEvents).distinctBy { it.id }
  }

  val currentOfficerProfile = remember(admin, students, users, executiveBody, viewModel.currentPhotoUri) {
    if (admin.isCabinetOfficerRole()) {
      admin.toCabinetIdCardProfile(
        students = students,
        users = users,
        executiveBody = executiveBody,
        fallbackPhotoUri = viewModel.currentPhotoUri,
      )
    } else {
      null
    }
  }

  if (showVerifyDialog) {
    CertificateVerificationDialog(
      verifiedCertificate = verifiedCertificate,
      checked = verificationChecked,
      onVerify = { viewModel.verifyCertificate(it) },
      onDismiss = {
        showVerifyDialog = false
        viewModel.clearCertificateVerification()
      },
    )
  }

  if (eventToRegister != null) {
    val ev = eventToRegister!!
    var transactionRef by remember(ev) { mutableStateOf("") }
    var receiptUri by remember(ev) { mutableStateOf("") }
    val receiptPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
      if (uri != null) {
        receiptUri = ImageEncodingUtils.encodeUriToDataUrl(context, uri, maxDimension = 720, quality = 70) ?: uri.toString()
      }
    }
    val visibleAccounts = if (activePaymentAccounts.isNotEmpty()) activePaymentAccounts else paymentAccounts.filter { it.active }

    AlertDialog(
      onDismissRequest = { eventToRegister = null },
      title = { Text("Register for ${ev.title}", fontWeight = FontWeight.Bold) },
      text = {
        Column(
          modifier = Modifier.verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Text("${ev.eventDate} • ${ev.eventTime}", style = MaterialTheme.typography.bodySmall)
          Text("Venue: ${ev.venue}", style = MaterialTheme.typography.bodySmall)
          if (ev.registrationFee <= 0.0) {
            Text(
              "Free Society Event — tap Confirm Registration below.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.SemiBold,
            )
          } else {
            Text(
              "Registration Fee: PKR ${ev.registrationFee.toInt()}",
              style = MaterialTheme.typography.titleSmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold,
            )
            visibleAccounts.forEach { acc ->
              Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth(),
              ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                  Text("${acc.accountType}: ${acc.bankOrWalletName}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                  Text("Title: ${acc.accountTitle} • No: ${acc.accountNumber}", style = MaterialTheme.typography.bodySmall)
                }
              }
            }
            OutlinedTextField(
              value = transactionRef,
              onValueChange = { transactionRef = it },
              label = { Text("Transaction Reference / TID") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
            )
            OutlinedButton(
              onClick = { receiptPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
              modifier = Modifier.fillMaxWidth(),
            ) {
              Icon(Icons.Outlined.AddAPhoto, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(Modifier.width(6.dp))
              Text(if (receiptUri.isNotBlank()) "Payment Receipt Attached ✓" else "Upload Payment Receipt")
            }
            if (receiptUri.isNotBlank()) {
              Surface(
                modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(12.dp)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
              ) {
                SocietyImage(
                  imageSource = receiptUri,
                  contentDescription = "Receipt Preview",
                  modifier = Modifier.fillMaxSize(),
                  contentScale = ContentScale.Crop,
                )
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.registerAdminOrCabinetForEvent(ev, admin, transactionRef, receiptUri)
            eventToRegister = null
          },
          modifier = Modifier.testTag("admin_calendar_confirm_register_btn"),
        ) {
          Text("Confirm Registration")
        }
      },
      dismissButton = {
        TextButton(onClick = { eventToRegister = null }) { Text("Cancel") }
      },
    )
  }

  // Secondary management sub-module host with mandatory BackHandler
  if (activeSubModule != null) {
    val sub = activeSubModule!!
    BackHandler {
      activeSubModule = null
      selectedRegistrationEventId = null
    }

    Scaffold(
      snackbarHost = { SnackbarHost(snackbarHostState) },
      topBar = {
        TopAppBar(
          title = {
            Row(
              modifier = Modifier.fillMaxWidth().padding(end = 12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              YesLogoEmblem(size = 34.dp)
              Text(
                text = sub.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
              )
              UafLogoEmblem(size = 34.dp)
            }
          },
          navigationIcon = {
            IconButton(
              onClick = {
                activeSubModule = null
                selectedRegistrationEventId = null
              },
              modifier = Modifier.testTag("admin_submodule_back_btn"),
            ) {
              Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Admin Dashboard")
            }
          },
        )
      },
    ) { innerPadding ->
      Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        when (sub) {
          AdminSubModule.EVENT_CALENDAR -> PermissionGuard(
            admin = admin, requiredPermission = "EVENT_MANAGEMENT",
            title = "Event Calendar Restricted",
            subtitle = "Access to society event schedules is restricted to Event Secretaries, President, and Joint Secretary."
          ) {
            StudentEventCalendarContent(
              events = calendarEvents,
              registrations = myRegistrations,
              onRegisterClick = { ev -> eventToRegister = ev },
              onCancelRegistration = { reg, ev ->
                viewModel.deleteRegistration(reg, ev, isAdmin = false)
              },
            )
          }
          AdminSubModule.ANNOUNCEMENTS -> PermissionGuard(
            admin = admin, requiredPermission = "NOTIFICATION_MANAGEMENT",
            title = "Announcements Restricted",
            subtitle = "Official notification broadcasting is restricted to Press, IT, and Event Secretaries."
          ) {
            AdminAnnouncementsTab(
              canManageAnnouncements = admin.hasPermission("MANAGE_ANNOUNCEMENTS"),
              announcements = announcements,
              onSaveAnnouncement = { ann, title, msg, cat, target, prio, sched, pub ->
                viewModel.saveAnnouncementAdmin(ann, title, msg, cat, target, prio, sched, pub)
              },
              onDeleteAnnouncement = { viewModel.deleteAnnouncementAdmin(it) },
            )
          }
          AdminSubModule.MEMBERSHIP -> PermissionGuard(
            admin = admin, requiredPermission = "MEMBERSHIP_FORM_APPROVAL",
            title = "Membership Management Restricted",
            subtitle = "Verification of students and ID card issuance is strictly gated to the Executive Board."
          ) {
            AdminMembershipModuleScreen(
              admin = admin,
              applications = membershipApps,
              students = students,
              users = users,
              onReviewApplication = { app, approve, validUntil, notes, assignedPos ->
                viewModel.reviewMembershipApp(app, approve, validUntil, notes, assignedPos)
              },
              onVerifyOrRejectStudent = { st, verified, pos, validUntil, notes ->
                viewModel.verifyOrRejectStudentAdmin(st, verified, pos, validUntil, notes)
              },
              onSetIdCardIssued = { st, issue, pos, validUntil ->
                viewModel.setStudentIdCardIssuedAdmin(st, issue, pos, validUntil)
              },
              onToggleStudentLock = { st, locked ->
                viewModel.toggleStudentProfileLockAdmin(st, locked)
              },
              onDeleteApplication = { viewModel.deleteMembershipApp(it, isAdmin = true) },
              onDeleteStudent = { viewModel.deleteStudentAdmin(it) },
              onPurgeAndBlockStudent = { st, reason ->
                viewModel.deleteWholeStudentDataAndBlockAdmin(st, reason)
              },
              onMessage = { viewModel.postMessage(it) },
            )
          }
          AdminSubModule.EXECUTIVE_BODY -> PermissionGuard(
            admin = admin, requiredPermission = "CABINET_MANAGEMENT",
            title = "Cabinet Management Restricted",
            subtitle = "Modifying the Executive Body roster requires Administrator or Presidential access."
          ) {
            AdminExecutiveBodyModuleScreen(
              canManageExecutive = admin.hasPermission("MANAGE_EXECUTIVE"),
              members = executiveBody,
              students = students,
              users = users,
              onSaveMember = { existing, name, desig, dept, tenure, email, photo, order, pub ->
                viewModel.saveExecutiveMember(existing, name, desig, dept, tenure, email, photo, order, pub)
              },
              onDeleteMember = { viewModel.deleteExecutiveMember(it) },
              onMessage = { viewModel.postMessage(it) },
            )
          }
          AdminSubModule.EVENT_REGISTRATIONS -> PermissionGuard(
            admin = admin, requiredPermission = "REGISTRATION_MANAGEMENT",
            title = "Registrations Restricted",
            subtitle = "Viewing and managing participant sheets is restricted to authorized Event & Finance officers."
          ) {
            AdminEventRegistrationsScreen(
              canManageRegistrations = admin.hasPermission("MANAGE_REGISTRATIONS") || admin.hasPermission("MANAGE_EVENTS"),
              events = events,
              registrations = registrations,
              students = students,
              initialEventId = selectedRegistrationEventId,
              onRegisterStudentAdmin = { ev, st, payStatus ->
                viewModel.registerStudentForEventAdmin(ev, st, payStatus)
              },
              onUpdateRegistration = { reg, rStatus, pStatus ->
                viewModel.updateRegistrationAdmin(reg, rStatus, pStatus)
              },
              onDeleteRegistration = { reg, ev ->
                viewModel.deleteRegistration(reg, ev, isAdmin = true)
              },
              onMessage = { viewModel.postMessage(it) },
            )
          }
          AdminSubModule.ATTENDANCE -> PermissionGuard(
            admin = admin, requiredPermission = "ATTENDANCE_MANAGEMENT",
            title = "Attendance Scanner Restricted",
            subtitle = "QR scanning and manual attendance marking is restricted to Attendance Managers and Event Secretaries."
          ) {
            AdminAttendanceModuleScreen(
              canManageAttendance = admin.hasPermission("MANAGE_ATTENDANCE") || admin.hasPermission("MANAGE_EVENTS"),
              events = events,
              students = students,
              attendance = attendance,
              onMarkAttendance = { ev, uid, sName, regNo, memId, st, method ->
                viewModel.markAttendanceAdmin(ev, uid, sName, regNo, memId, st, method)
              },
              onDeleteAttendance = { viewModel.deleteAttendanceAdmin(it) },
              onMessage = { viewModel.postMessage(it) },
            )
          }
          AdminSubModule.FINANCE -> PermissionGuard(
            admin = admin, requiredPermission = "FINANCE_MANAGEMENT",
            title = "Finance Access Restricted",
            subtitle = "Financial ledgers, balances, and payment verifications are restricted to Finance Secretaries and Senior Cabinet."
          ) {
            AdminFinanceModuleScreen(
              admin = admin,
              events = events,
              transactions = financeTransactions,
              payments = payments,
              registrations = registrations,
              paymentAccounts = paymentAccounts,
              onRecordTransaction = { title, type, cat, amt, date, ref, notes ->
                viewModel.recordFinanceEntry(title, type, cat, amt, date, ref, notes)
              },
              onDeleteTransaction = { viewModel.deleteFinanceEntry(it) },
              onSavePaymentAccount = { existing, accType, bankName, accTitle, accNum, iban, inst, active ->
                viewModel.savePaymentAccountAdmin(existing, accType, bankName, accTitle, accNum, iban, inst, active)
              },
              onDeletePaymentAccount = { viewModel.deletePaymentAccountAdmin(it) },
              onVerifyPayment = { pay, approve -> viewModel.verifyPaymentAdmin(pay, approve) },
              onDeletePayment = { viewModel.deletePaymentRecord(it, isAdmin = true) },
              onDeleteRegistration = { reg, ev -> viewModel.deleteRegistration(reg, ev, isAdmin = true) },
              onMessage = { viewModel.postMessage(it) },
            )
          }
          AdminSubModule.GALLERY -> PermissionGuard(
            admin = admin, requiredPermission = "DATA_MANAGEMENT",
            title = "Gallery Management Restricted",
            subtitle = "Uploading and managing society event photos is restricted to Press and IT Cell."
          ) {
            AdminGalleryModuleScreen(
              canManageGallery = admin.hasPermission("MANAGE_GALLERY"),
              itemsList = galleryItems,
              onSaveItem = { existing, title, album, cap, date, img, theme, app ->
                viewModel.saveGalleryPhotoAdmin(existing, title, album, cap, date, img, theme, app)
              },
              onDeleteItem = { viewModel.deleteGalleryPhotoAdmin(it) },
            )
          }
          AdminSubModule.CERTIFICATES -> PermissionGuard(
            admin = admin, 
            requiredPermission = if (admin.hasPermission("CERTIFICATE_APPROVAL")) "CERTIFICATE_APPROVAL" else "CERTIFICATE_GENERATION",
            title = "Certificates Restricted",
            subtitle = "Issuing and approving official certificates is restricted to IT Cell, Secretaries, and Senior Cabinet."
          ) {
            AdminCertificatesModuleScreen(
              admin = admin,
              certificates = certificates,
              students = students,
              events = events,
              onIssueCertificate = { uid, sName, regNo, evId, evTitle, evDate, cType, sig ->
                viewModel.issueCertificateAdmin(uid, sName, regNo, evId, evTitle, evDate, cType, sig)
              },
              onApproveCertificate = { viewModel.approveCertificate(it) },
              onRejectCertificate = { cert, reason -> viewModel.rejectCertificate(cert, reason) },
              onDeleteCertificate = { viewModel.deleteCertificateAdmin(it) },
              onOpenVerifyDialog = { showVerifyDialog = true },
              onMessage = { viewModel.postMessage(it) },
            )
          }
          AdminSubModule.FEEDBACK -> {
            AdminFeedbackModuleScreen(
              feedbackList = feedbackList,
              onRespond = { id, status, reply -> viewModel.respondToFeedback(id, status, reply) },
              onDeleteFeedback = { viewModel.deleteFeedbackItem(it, isAdmin = true) },
            )
          }
          AdminSubModule.REPORTS -> PermissionGuard(
            admin = admin, requiredPermission = "REPORTS_AND_ANALYTICS",
            title = "Analytics Restricted",
            subtitle = "Exporting society datasets and reports is restricted to Senior Cabinet and Finance officers."
          ) {
            AdminReportsModuleScreen(
              admin = admin,
              students = students,
              applications = membershipApps,
              events = events,
              registrations = registrations,
              attendance = attendance,
              financeTransactions = financeTransactions,
              onMessage = { viewModel.postMessage(it) },
            )
          }
          AdminSubModule.ROLES_SETTINGS -> PermissionGuard(
            admin = admin, requiredPermission = "ADMINISTRATOR_ACCESS",
            title = "System Access Restricted",
            subtitle = "Managing administrative roles and permissions is strictly gated to Super Administrators."
          ) {
            AdminRolesModuleScreen(
              currentAdmin = admin,
              admins = admins,
              students = students,
              users = users,
              executiveBody = executiveBody,
              onAssignAdminRole = { uid, email, name, rType, desig, perms, active ->
                viewModel.assignAdminRole(uid, email, name, rType, desig, perms, active)
              },
              onRevokeAdminRole = { viewModel.revokeAdminRole(it) },
              onMessage = { viewModel.postMessage(it) },
            )
          }
          AdminSubModule.ACTIVITY_LOGS -> PermissionGuard(
            admin = admin, requiredPermission = "ADMINISTRATOR_ACCESS",
            title = "Audit Trail Restricted",
            subtitle = "Viewing the recent admin activity log requires Administrator access."
          ) {
            AdminActivityLogsModuleScreen(
              currentAdmin = admin,
              auditLogs = auditLogs,
              onDeleteLog = { viewModel.deleteAuditLogAdmin(it) },
            )
          }
          AdminSubModule.IT_CELL -> PermissionGuard(
            admin = admin, requiredPermission = "IT_MANAGEMENT",
            title = "IT Cell Restricted",
            subtitle = "Modifying portal configuration and social links requires IT and Media Cell authorization."
          ) {
            AdminItCellModuleScreen(
              viewModel = viewModel,
              onMessage = { viewModel.postMessage(it) }
            )
          }
        }
      }
    }
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
              modifier = Modifier.fillMaxWidth().padding(end = 12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              // Society Logo on LEFT
              YesLogoEmblem(size = 38.dp)
              Column(
                modifier = Modifier.weight(1f).padding(horizontal = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
              ) {
                Text(
                  text = "YES Administrator Portal",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Text(
                  text = "${admin.resolvedDisplayDesignation()} • Department of Entomology",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.primary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }
              // University Logo on RIGHT
              UafLogoEmblem(size = 38.dp)
            }
          },
        )
      },
      bottomBar = {
        if (!isExpanded) {
          NavigationBar {
            AdminPrimaryTab.entries.filter { tab ->
              when (tab) {
                AdminPrimaryTab.OVERVIEW, AdminPrimaryTab.MORE -> true
                AdminPrimaryTab.STUDENTS -> admin.hasPermission("STUDENT_REGISTRATION_APPROVAL") || admin.hasPermission("MEMBERSHIP_FORM_APPROVAL") || admin.hasPermission("DATA_MANAGEMENT")
                AdminPrimaryTab.EVENTS -> admin.hasPermission("EVENT_MANAGEMENT") || admin.hasPermission("REGISTRATION_MANAGEMENT") || admin.hasPermission("ATTENDANCE_MANAGEMENT")
                AdminPrimaryTab.ID_CARDS -> admin.hasPermission("ID_CARD_MANAGEMENT")
              }
            }.forEach { tab ->
              val selected = primaryTab == tab
              NavigationBarItem(
                selected = selected,
                onClick = { primaryTab = tab },
                icon = {
                  Icon(
                    imageVector = if (selected) tab.filledIcon else tab.outlinedIcon,
                    contentDescription = tab.label,
                  )
                },
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
            AdminPrimaryTab.entries.filter { tab ->
              when (tab) {
                AdminPrimaryTab.OVERVIEW, AdminPrimaryTab.MORE -> true
                AdminPrimaryTab.STUDENTS -> admin.hasPermission("STUDENT_REGISTRATION_APPROVAL") || admin.hasPermission("MEMBERSHIP_FORM_APPROVAL") || admin.hasPermission("DATA_MANAGEMENT")
                AdminPrimaryTab.EVENTS -> admin.hasPermission("EVENT_MANAGEMENT") || admin.hasPermission("REGISTRATION_MANAGEMENT") || admin.hasPermission("ATTENDANCE_MANAGEMENT")
                AdminPrimaryTab.ID_CARDS -> admin.hasPermission("ID_CARD_MANAGEMENT")
              }
            }.forEach { tab ->
              val selected = primaryTab == tab
              NavigationRailItem(
                selected = selected,
                onClick = { primaryTab = tab },
                icon = {
                  Icon(
                    imageVector = if (selected) tab.filledIcon else tab.outlinedIcon,
                    contentDescription = tab.label,
                  )
                },
                label = { Text(tab.label) },
                modifier = Modifier.testTag(tab.tag),
              )
            }
          }
        }

        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
          when (primaryTab) {
            AdminPrimaryTab.OVERVIEW -> AdminOverviewTab(
              admin = admin,
              students = students,
              executiveBody = executiveBody,
              admins = admins,
              membershipApps = membershipApps,
              events = calendarEvents,
              registrations = registrations,
              announcements = announcements,
              attendance = attendance,
              myAttendance = myAttendance,
              financeTransactions = financeTransactions,
              onQrCheckIn = { code -> viewModel.checkInAdminOrCabinetByQr(code, admin) },
              onOpenCabinetIdCardsTab = { primaryTab = AdminPrimaryTab.ID_CARDS },
              onOpenModule = { mod ->
                selectedRegistrationEventId = null
                activeSubModule = mod
              },
              onMessage = { viewModel.postMessage(it) },
            )
            AdminPrimaryTab.STUDENTS -> PermissionGuard(
              admin = admin,
              requiredPermission = "STUDENT_REGISTRATION_APPROVAL", // fallback check
              title = "Student Directory Restricted",
              subtitle = "Viewing and managing the student directory is restricted to authorized Executive officers."
            ) {
              AdminStudentsTab(
                admin = admin,
                students = students,
                users = users,
                blockedAccounts = blockedAccounts,
                onSaveStudent = { st, name, email, regNo, deg, dept, sem, phone, pos, mStatus, mId, valid, photo, card, locked, active, idCardSt ->
                  viewModel.saveStudentAdmin(
                    st, name, email, regNo, deg, dept, sem, phone, pos, mStatus, mId, valid, photo, card, locked, active, idCardSt,
                  )
                },
                onVerifyOrRejectStudent = { st, verified, pos, validUntil, notes ->
                  viewModel.verifyOrRejectStudentAdmin(st, verified, pos, validUntil, notes)
                },
                onSetIdCardIssued = { st, issue, pos, validUntil ->
                  viewModel.setStudentIdCardIssuedAdmin(st, issue, pos, validUntil)
                },
                onToggleStudentLock = { st, locked ->
                  viewModel.toggleStudentProfileLockAdmin(st, locked)
                },
                onDeleteStudent = { viewModel.deleteStudentAdmin(it) },
                onPurgeAndBlockStudent = { st, reason ->
                  viewModel.deleteWholeStudentDataAndBlockAdmin(st, reason)
                },
                onUnblockAccount = { viewModel.unblockStudentAccountAdmin(it) },
                onMessage = { viewModel.postMessage(it) },
              )
            }
            AdminPrimaryTab.EVENTS -> PermissionGuard(
              admin = admin,
              requiredPermission = "EVENT_MANAGEMENT",
              title = "Events Management Restricted",
              subtitle = "Scheduling and managing society events is restricted to Event Secretaries and Joint Secretary."
            ) {
              AdminEventsTab(
                admin = admin,
                events = events,
                registrations = registrations,
                onSaveEvent = { ev, title, desc, cat, date, time, venue, poster, dl, fee, maxP, upc, pub ->
                  viewModel.saveEventAdmin(ev, title, desc, cat, date, time, venue, poster, dl, fee, maxP, upc, pub)
                },
                onDeleteEvent = { viewModel.deleteEventAdmin(it) },
                onOpenEventRegistrations = { evId ->
                  selectedRegistrationEventId = evId
                  activeSubModule = AdminSubModule.EVENT_REGISTRATIONS
                },
                onOpenEventCalendar = {
                  selectedRegistrationEventId = null
                  activeSubModule = AdminSubModule.EVENT_CALENDAR
                },
              )
            }
            AdminPrimaryTab.ID_CARDS -> PermissionGuard(
              admin = admin,
              requiredPermission = "ID_CARD_MANAGEMENT",
              title = "ID Cards Restricted",
              subtitle = "Viewing and issuing official identity cards is restricted to the Executive Board."
            ) {
              AdminCabinetIdCardsTab(
                admin = admin,
                admins = admins,
                students = students,
                executiveBody = executiveBody,
                users = users,
                currentPhotoUri = viewModel.currentPhotoUri,
                onMessage = { viewModel.postMessage(it) },
              )
            }
            AdminPrimaryTab.MORE -> AdminMoreMenuTab(
              admin = admin,
              officerIdCardProfile = currentOfficerProfile,
              appFontSize = appFontSize,
              onAppFontSizeChange = onAppFontSizeChange,
              onOpenModule = { mod ->
                selectedRegistrationEventId = null
                activeSubModule = mod
              },
              onOpenVerifyCertificate = { showVerifyDialog = true },
              onSignOut = onSignOut,
              onMessage = { viewModel.postMessage(it) },
            )
          }
        }
      }
    }
  }
}
