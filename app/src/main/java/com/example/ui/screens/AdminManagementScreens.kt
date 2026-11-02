package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assessment
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
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AdminAccount
import com.example.data.model.AdminRoleType
import com.example.data.model.AttendanceRecord
import com.example.data.model.ExecutiveMember
import com.example.data.model.IdCardDesignTheme
import com.example.data.model.MembershipApplication
import com.example.data.model.SocietyConstants
import com.example.data.model.SocietyEvent
import com.example.data.model.StudentProfile
import com.example.data.model.UserAccount
import com.example.data.model.hasPermission
import com.example.data.model.hasSuperAdminAccess
import com.example.data.model.isCabinetMember
import com.example.data.model.isCabinetOfficerRole
import com.example.data.model.isIdCardActive
import com.example.data.model.isVerifiedMember
import com.example.data.model.isVerifiedOrApproved
import com.example.data.model.resolvedDisplayDesignation
import com.example.data.model.toCabinetIdCardProfile
import com.example.ui.components.AppFontSizeSelector
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.FullScreenImagePreviewDialog
import com.example.ui.components.OfficialIdCardPreviewDialog
import com.example.ui.components.QrCameraScannerDialog
import com.example.ui.components.SectionTitleRow
import com.example.ui.components.SocietyImage
import com.example.ui.components.StatMetricCard
import com.example.ui.components.StatusPill
import com.example.ui.components.YesDropdownField
import com.example.ui.theme.AppFontSize
import com.example.util.ImageEncodingUtils
import com.example.util.PdfExportManager
import com.example.util.YesQrCodeBox
import kotlinx.coroutines.launch

enum class AdminSubModule(
  val title: String,
  val subtitle: String,
  val icon: ImageVector,
) {
  EVENT_CALENDAR("Event Calendar", "Monthly Firestore schedule of society meetings, seminars & field trips", Icons.Filled.CalendarMonth),
  ANNOUNCEMENTS("Notices & Announcements", "Publish & manage official society notices, alerts & schedules", Icons.Filled.Campaign),
  MEMBERSHIP("Membership & Official ID Cards", "Verify/reject applications & issue/revoke Portrait Cabinet or General IDs", Icons.Filled.Badge),
  EXECUTIVE_BODY("Executive Body", "President, Convener, Advisory Board, Senior VP, Secretaries & Associates", Icons.Filled.Groups),
  EVENT_REGISTRATIONS("Event Registrations", "Separate participant sheets & exports per event", Icons.Filled.HowToReg),
  ATTENDANCE("Attendance & Camera QR Scan", "Live Camera QR scanner & event attendance register", Icons.Filled.QrCodeScanner),
  FINANCE("Finance & Account Details", "Ledger, official bank/wallet accounts & fee receipts", Icons.Filled.AccountBalanceWallet),
  GALLERY("Society Gallery", "Albums, event photos & approvals", Icons.Filled.PhotoLibrary),
  CERTIFICATES("Certificates & PDF", "Issue dual-logo UAF & YES certificates & verify QR", Icons.Filled.WorkspacePremium),
  FEEDBACK("Feedback & Suggestions", "Student inquiries & executive replies", Icons.Filled.Feedback),
  REPORTS("Reports & Analytics", "Download PDF & Excel/CSV datasets", Icons.Filled.Assessment),
  ROLES_SETTINGS("Admin Roles & Permissions", "Administrator, President, Convener & ${AdminRoleType.entries.size} Roles", Icons.Filled.AdminPanelSettings),
  ACTIVITY_LOGS("Recent Admin Activity", "Separate audit trail of administrative actions", Icons.Filled.History),
  IT_CELL("IT & Media Cell", "Manage website content, media gallery & digital announcements", Icons.Filled.Edit),
}

@Composable
fun AdminItCellModuleScreen(
  viewModel: com.example.ui.viewmodel.SocietyViewModel,
  onMessage: (String) -> Unit,
) {
  val config by viewModel.societyConfig.collectAsStateWithLifecycle()
  var flashNews by remember(config) { mutableStateOf(config.portalFlashNews) }
  var facebook by remember(config) { mutableStateOf(config.facebookLink) }
  var instagram by remember(config) { mutableStateOf(config.instagramLink) }
  var twitter by remember(config) { mutableStateOf(config.twitterLink) }
  var youtube by remember(config) { mutableStateOf(config.youtubeLink) }
  var linkedin by remember(config) { mutableStateOf(config.linkedinLink) }
  var tiktok by remember(config) { mutableStateOf(config.tiktokLink) }
  var website by remember(config) { mutableStateOf(config.websiteLink) }
  var societyAbout by remember(config) { mutableStateOf(config.societyAbout) }
  var societyEmail by remember(config) { mutableStateOf(config.societyEmail) }
  var societyPhone by remember(config) { mutableStateOf(config.societyPhone) }

  val publishedEvents by viewModel.publishedEvents.collectAsStateWithLifecycle()
  val approvedGallery by viewModel.approvedGallery.collectAsStateWithLifecycle()
  val adminAllGallery by viewModel.adminAllGallery.collectAsStateWithLifecycle()
  val adminAllAnnouncements by viewModel.adminAllAnnouncements.collectAsStateWithLifecycle()
  val adminAllCertificates by viewModel.adminAllCertificates.collectAsStateWithLifecycle()

  val pendingGalleryCount = adminAllGallery.count { !it.approved }
  val pendingCertCount = adminAllCertificates.count { it.status == "PENDING" }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Portal & Social Media Configuration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Text(
            "Authorized IT & Media Cell officers can update global portal news, society contact links, and oversee digital media publication.",
            style = MaterialTheme.typography.bodySmall,
          )
        }
      }
    }

    item {
      SectionTitleRow(title = "Media Cell Summaries")
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatMetricCard(
          title = "Pending Photos",
          value = "$pendingGalleryCount",
          icon = Icons.Filled.PhotoLibrary,
          accentColor = if (pendingGalleryCount > 0) Color(0xFFB46914) else MaterialTheme.colorScheme.primary,
          modifier = Modifier.weight(1f),
        )
        StatMetricCard(
          title = "Pending Certs",
          value = "$pendingCertCount",
          icon = Icons.Filled.WorkspacePremium,
          accentColor = if (pendingCertCount > 0) Color(0xFFB46914) else MaterialTheme.colorScheme.primary,
          modifier = Modifier.weight(1f),
        )
      }
    }

    item {
      OutlinedTextField(
        value = flashNews,
        onValueChange = { flashNews = it },
        label = { Text("Portal Flash News (Scrolling Announcement)") },
        minLines = 2,
        modifier = Modifier.fillMaxWidth().testTag("it_cell_flash_news_input"),
      )
    }

    item {
      SectionTitleRow(title = "Social Media & Website Links")
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = website,
          onValueChange = { website = it },
          label = { Text("Society Official Website") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("it_cell_website_input"),
        )
        OutlinedTextField(
          value = facebook,
          onValueChange = { facebook = it },
          label = { Text("Facebook Profile Link") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("it_cell_facebook_input"),
        )
        OutlinedTextField(
          value = instagram,
          onValueChange = { instagram = it },
          label = { Text("Instagram Profile Link") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("it_cell_instagram_input"),
        )
        OutlinedTextField(
          value = twitter,
          onValueChange = { twitter = it },
          label = { Text("Twitter / X Link") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("it_cell_twitter_input"),
        )
        OutlinedTextField(
          value = youtube,
          onValueChange = { youtube = it },
          label = { Text("YouTube Channel Link") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("it_cell_youtube_input"),
        )
        OutlinedTextField(
          value = linkedin,
          onValueChange = { linkedin = it },
          label = { Text("LinkedIn Company Page") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("it_cell_linkedin_input"),
        )
        OutlinedTextField(
          value = tiktok,
          onValueChange = { tiktok = it },
          label = { Text("TikTok Profile Link") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("it_cell_tiktok_input"),
        )
      }
    }

    item {
      SectionTitleRow(title = "About Society & Contact")
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = societyAbout,
          onValueChange = { societyAbout = it },
          label = { Text("About Society (Detailed Description)") },
          minLines = 3,
          modifier = Modifier.fillMaxWidth().testTag("it_cell_about_input"),
        )
        OutlinedTextField(
          value = societyEmail,
          onValueChange = { societyEmail = it },
          label = { Text("Official Society Email") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("it_cell_email_input"),
        )
        OutlinedTextField(
          value = societyPhone,
          onValueChange = { societyPhone = it },
          label = { Text("Official Society Phone") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("it_cell_phone_input"),
        )
      }
    }

    item {
      Button(
        onClick = {
          viewModel.saveSocietyConfig(
            config.copy(
              portalFlashNews = flashNews,
              facebookLink = facebook,
              instagramLink = instagram,
              twitterLink = twitter,
              youtubeLink = youtube,
              linkedinLink = linkedin,
              tiktokLink = tiktok,
              websiteLink = website,
              societyAbout = societyAbout,
              societyEmail = societyEmail,
              societyPhone = societyPhone,
            )
          )
        },
        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("it_cell_save_config_btn"),
      ) {
        Icon(Icons.Filled.CheckCircle, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Save & Apply Global Settings")
      }
    }
  }
}


@Composable
fun AdminMoreMenuTab(
  admin: AdminAccount,
  officerIdCardProfile: StudentProfile? = null,
  appFontSize: AppFontSize,
  onAppFontSizeChange: (AppFontSize) -> Unit,
  onOpenModule: (AdminSubModule) -> Unit,
  onOpenVerifyCertificate: () -> Unit,
  onSignOut: () -> Unit,
  onMessage: (String) -> Unit = {},
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  var showOfficerIdCard by remember { mutableStateOf(false) }
  val canSeeFinance = admin.hasPermission("MANAGE_FINANCE")
  val officerPdfLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.CreateDocument("application/pdf")
  ) { uri: Uri? ->
    val profile = officerIdCardProfile
    if (uri != null && profile != null) {
      scope.launch {
        PdfExportManager.exportStudentIdCardPdf(context, uri, profile)
          .onSuccess { onMessage("Official Cabinet ID Card PDF saved for ${profile.fullName}!") }
          .onFailure { onMessage("Failed to export ID Card PDF: ${it.localizedMessage}") }
      }
    }
  }

  if (showOfficerIdCard && officerIdCardProfile != null) {
    OfficialIdCardPreviewDialog(
      student = officerIdCardProfile,
      onDownloadPdf = {
        val safeName = officerIdCardProfile.fullName.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        officerPdfLauncher.launch("YES_Cabinet_ID_${safeName}.pdf")
      },
      onDismiss = { showOfficerIdCard = false },
    )
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    if (admin.isCabinetOfficerRole() && officerIdCardProfile != null) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth().testTag("admin_officer_profile_card"),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Surface(
              modifier = Modifier.size(54.dp),
              shape = CircleShape,
              color = MaterialTheme.colorScheme.surface,
              border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
            ) {
              if (officerIdCardProfile.photoUri.isNotBlank()) {
                SocietyImage(
                  imageSource = officerIdCardProfile.photoUri,
                  contentDescription = officerIdCardProfile.fullName,
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
              StatusPill(label = admin.resolvedDisplayDesignation())
              Text(officerIdCardProfile.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
              Text(
                "${officerIdCardProfile.registrationNumber} • ${officerIdCardProfile.department}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            Button(
              onClick = { showOfficerIdCard = true },
              modifier = Modifier.testTag("admin_profile_view_id_card_btn"),
            ) {
              Icon(Icons.Filled.Badge, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(Modifier.width(6.dp))
              Text("ID Card")
            }
          }
        }
      }
    }

    item {
      SectionTitleRow(title = "Executive Management Modules")
    }

    items(AdminSubModule.entries, key = { it.name }) { module ->
      val hasPermission = when (module) {
        AdminSubModule.EVENT_CALENDAR -> admin.hasPermission("EVENT_MANAGEMENT")
        AdminSubModule.ANNOUNCEMENTS -> admin.hasPermission("NOTIFICATION_MANAGEMENT")
        AdminSubModule.MEMBERSHIP -> admin.hasPermission("MEMBERSHIP_FORM_APPROVAL") || admin.hasPermission("ID_CARD_MANAGEMENT") || admin.hasPermission("STUDENT_REGISTRATION_APPROVAL")
        AdminSubModule.EXECUTIVE_BODY -> admin.hasPermission("CABINET_MANAGEMENT")
        AdminSubModule.EVENT_REGISTRATIONS -> admin.hasPermission("REGISTRATION_MANAGEMENT")
        AdminSubModule.ATTENDANCE -> admin.hasPermission("ATTENDANCE_MANAGEMENT")
        AdminSubModule.FINANCE -> admin.hasPermission("FINANCE_MANAGEMENT")
        AdminSubModule.GALLERY -> admin.hasPermission("DATA_MANAGEMENT")
        AdminSubModule.CERTIFICATES -> admin.hasPermission("CERTIFICATE_GENERATION") || admin.hasPermission("CERTIFICATE_APPROVAL")
        AdminSubModule.FEEDBACK -> true
        AdminSubModule.REPORTS -> admin.hasPermission("REPORTS_AND_ANALYTICS")
        AdminSubModule.ROLES_SETTINGS -> admin.hasPermission("ADMINISTRATOR_ACCESS")
        AdminSubModule.ACTIVITY_LOGS -> admin.hasPermission("ADMINISTRATOR_ACCESS")
        AdminSubModule.IT_CELL -> admin.hasPermission("IT_MANAGEMENT")
      }
      val isLocked = !hasPermission && !admin.hasSuperAdminAccess()

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable(enabled = !isLocked) { if (!isLocked) onOpenModule(module) }
          .testTag("admin_module_${module.name.lowercase()}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (isLocked) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isLocked) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(48.dp),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = if (isLocked) Icons.Filled.Lock else module.icon,
                contentDescription = module.title,
                tint = if (isLocked) MaterialTheme.colorScheme.onErrorContainer
                else MaterialTheme.colorScheme.primary,
              )
            }
          }
          Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(module.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
              text = if (isLocked) "Restricted. Access required." else module.subtitle,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          if (isLocked) {
            StatusPill(label = "LOCKED")
          }
        }
      }
    }

    item {
      Spacer(Modifier.height(4.dp))
      SectionTitleRow(title = "App Font Size, Verification & Session")
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
          OutlinedButton(
            onClick = onOpenVerifyCertificate,
            modifier = Modifier.fillMaxWidth().testTag("admin_verify_certificate_btn"),
          ) {
            Icon(Icons.Outlined.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Verify Certificate Authenticity")
          }

          AppFontSizeSelector(
            appFontSize = appFontSize,
            onAppFontSizeChange = onAppFontSizeChange,
          )

          Button(
            onClick = onSignOut,
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.errorContainer,
              contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ),
            modifier = Modifier.fillMaxWidth().testTag("admin_logout_button"),
          ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Logout (${admin.email})")
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminMembershipModuleScreen(
  admin: AdminAccount,
  applications: List<MembershipApplication>,
  students: List<StudentProfile>,
  users: List<UserAccount> = emptyList(),
  onReviewApplication: (MembershipApplication, Boolean, String, String, String?) -> Unit,
  onVerifyOrRejectStudent: (StudentProfile, Boolean, String?, String, String) -> Unit,
  onSetIdCardIssued: (StudentProfile, Boolean, String?, String) -> Unit,
  onToggleStudentLock: (StudentProfile, Boolean) -> Unit,
  onDeleteApplication: (MembershipApplication) -> Unit,
  onDeleteStudent: (StudentProfile) -> Unit,
  onPurgeAndBlockStudent: (StudentProfile, String) -> Unit,
  onMessage: (String) -> Unit,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  val canApprove = admin.hasPermission("MEMBERSHIP_FORM_APPROVAL") || admin.hasPermission("STUDENT_REGISTRATION_APPROVAL")
  val canManageCards = admin.hasPermission("ID_CARD_MANAGEMENT")
  val canAdmin = admin.hasPermission("ADMINISTRATOR_ACCESS")

  var viewFilter by remember { mutableStateOf("APPLICATIONS") }
  var appStatusFilter by remember { mutableStateOf("ALL") }
  var previewCardStudent by remember { mutableStateOf<StudentProfile?>(null) }
  var idCardExportTarget by remember { mutableStateOf<StudentProfile?>(null) }
  var reviewingApp by remember { mutableStateOf<MembershipApplication?>(null) }
  var rejectingApp by remember { mutableStateOf<MembershipApplication?>(null) }
  var issuingCardStudent by remember { mutableStateOf<StudentProfile?>(null) }
  var purgingStudent by remember { mutableStateOf<StudentProfile?>(null) }
  var appToDelete by remember { mutableStateOf<MembershipApplication?>(null) }
  var memberToDelete by remember { mutableStateOf<StudentProfile?>(null) }
  var zoomImage by remember { mutableStateOf<Pair<String, String>?>(null) }

  fun enrichStudentPhoto(st: StudentProfile): StudentProfile {
    if (st.photoUri.isNotBlank()) return st
    val userPhoto = users.firstOrNull { u ->
      (st.studentUid.isNotBlank() && (u.userId == st.studentUid || u.id == st.studentUid)) ||
        (st.email.isNotBlank() && u.email.equals(st.email.trim(), ignoreCase = true))
    }?.photoUri.orEmpty()
    val appPhoto = applications.firstOrNull { a ->
      a.studentUid == st.studentUid || (st.email.isNotBlank() && a.studentEmail.equals(st.email.trim(), ignoreCase = true))
    }?.photoUri.orEmpty()
    val resolved = appPhoto.ifBlank { userPhoto }
    return if (resolved.isNotBlank()) st.copy(photoUri = resolved) else st
  }

  val pdfLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.CreateDocument("application/pdf")
  ) { uri: Uri? ->
    val st = idCardExportTarget
    if (uri != null && st != null) {
      scope.launch {
        PdfExportManager.exportStudentIdCardPdf(context, uri, enrichStudentPhoto(st))
          .onSuccess { onMessage("Official Student ID Card PDF saved!") }
          .onFailure { onMessage("PDF export failed: ${it.localizedMessage}") }
      }
    }
    idCardExportTarget = null
  }

  if (zoomImage != null) {
    val (title, src) = zoomImage!!
    FullScreenImagePreviewDialog(
      title = title,
      imageSource = src,
      onDismiss = { zoomImage = null },
    )
  }

  if (appToDelete != null) {
    val target = appToDelete!!
    ConfirmDeleteDialog(
      itemTitle = "${target.studentName} (${target.registrationNumber})",
      itemSubtitle = "Permanently delete this membership application?",
      onConfirmDelete = { onDeleteApplication(target) },
      onDismiss = { appToDelete = null },
    )
  }

  if (memberToDelete != null) {
    val target = memberToDelete!!
    ConfirmDeleteDialog(
      itemTitle = "${target.fullName} (${target.membershipId.ifBlank { target.registrationNumber }})",
      itemSubtitle = "Delete this student membership record?",
      onConfirmDelete = { onDeleteStudent(target) },
      onDismiss = { memberToDelete = null },
    )
  }

  val filteredApplications = remember(applications, appStatusFilter) {
    when (appStatusFilter) {
      "PENDING" -> applications.filter { it.status == "PENDING" }
      "VERIFIED" -> applications.filter { it.isVerifiedOrApproved() }
      "REJECTED" -> applications.filter { it.status == "REJECTED" }
      else -> applications
    }
  }

  val filteredMembers = remember(students, viewFilter) {
    when (viewFilter) {
      "VERIFIED_CARDS" -> students.filter { it.isVerifiedMember() && it.isIdCardActive() }
      "REVOKED_OR_UNISSUED" -> students.filter { !it.isIdCardActive() }
      "REJECTED" -> students.filter { it.membershipStatus == "REJECTED" }
      else -> students
    }
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      YesDropdownField(
        label = "Membership & ID Card View",
        options = listOf("APPLICATIONS", "VERIFIED_CARDS", "REVOKED_OR_UNISSUED", "REJECTED"),
        selectedOption = viewFilter,
        onOptionSelected = { viewFilter = it },
        optionLabel = { opt ->
          when (opt) {
            "APPLICATIONS" -> "Membership Applications (${applications.size})"
            "VERIFIED_CARDS" -> "Issued ID Cards — Cabinet & General (${students.count { it.isVerifiedMember() && it.isIdCardActive() }})"
            "REVOKED_OR_UNISSUED" -> "Revoked / Unissued ID Cards (${students.count { !it.isIdCardActive() }})"
            "REJECTED" -> "Rejected Student Applications (${students.count { it.membershipStatus == "REJECTED" }})"
            else -> opt
          }
        },
        testTag = "admin_membership_view_dropdown",
      )
    }

    if (viewFilter == "APPLICATIONS") {
      item {
        YesDropdownField(
          label = "Filter Application Verification Status",
          options = listOf("ALL", "PENDING", "VERIFIED", "REJECTED"),
          selectedOption = appStatusFilter,
          onOptionSelected = { appStatusFilter = it },
          optionLabel = { st ->
            when (st) {
              "ALL" -> "All Applications (${applications.size})"
              "PENDING" -> "Pending Verification (${applications.count { it.status == "PENDING" }})"
              "VERIFIED" -> "Verified Applications (${applications.count { it.isVerifiedOrApproved() }})"
              "REJECTED" -> "Rejected Applications (${applications.count { it.status == "REJECTED" }})"
              else -> st
            }
          },
          testTag = "admin_app_status_filter_dropdown",
        )
      }

      if (filteredApplications.isEmpty()) {
        item {
          EmptyStateCard(
            title = "No Applications in Filter",
            subtitle = "Submitted student membership requests with uploaded university cards appear here for Verified / Rejected decisions.",
            icon = Icons.Filled.Badge,
          )
        }
      } else {
        items(filteredApplications, key = { it.id }) { app ->
          val matchedStudent = students.firstOrNull { it.studentUid == app.studentUid || it.id == app.studentUid }
          val photoSrc = app.photoUri.ifBlank { matchedStudent?.photoUri.orEmpty() }
          val cardSrc = app.studentCardUri.ifBlank { matchedStudent?.studentCardUri.orEmpty() }
          val deg = app.degreeLevel.ifBlank { matchedStudent?.degreeLevel.orEmpty() }
          val phone = app.phone.ifBlank { matchedStudent?.phone.orEmpty() }

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
                    .size(56.dp)
                    .clickable(enabled = photoSrc.isNotBlank()) {
                      if (photoSrc.isNotBlank()) zoomImage = "${app.studentName} — Photo" to photoSrc
                    },
                  shape = CircleShape,
                  color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                  if (photoSrc.isNotBlank()) {
                    SocietyImage(
                      imageSource = photoSrc,
                      contentDescription = app.studentName,
                      modifier = Modifier.fillMaxSize(),
                      contentScale = ContentScale.Crop,
                    )
                  } else {
                    Box(contentAlignment = Alignment.Center) {
                      Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                  }
                }

                Column(modifier = Modifier.weight(1f)) {
                  Text(app.studentName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                  Text("${app.registrationNumber} • ${deg.ifBlank { "Student" }}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                  Text("${app.department} (${app.semester}) • ${phone.ifBlank { app.studentEmail }}", style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  StatusPill(label = if (app.status == "APPROVED") "VERIFIED" else app.status)
                  if (canAdmin) {
                    IconButton(
                      onClick = { appToDelete = app },
                      modifier = Modifier.testTag("delete_membership_app_${app.id}"),
                    ) {
                      Icon(Icons.Filled.Delete, contentDescription = "Delete Application", tint = MaterialTheme.colorScheme.error)
                    }
                  }
                }
              }

              if (cardSrc.isNotBlank()) {
                Surface(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(145.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { zoomImage = "${app.studentName} — University Student Card" to cardSrc },
                  border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                ) {
                  SocietyImage(
                    imageSource = cardSrc,
                    contentDescription = "University Student Card",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                  )
                }
                Text("Tap student card image above to inspect full size", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
              }

              Text(app.motivation, style = MaterialTheme.typography.bodyMedium)
              if (app.assignedMembershipId.isNotBlank()) {
                Text("Issued Membership ID: ${app.assignedMembershipId}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
              }
              if (canApprove) {
                FlowRow(
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                  Button(
                    onClick = { reviewingApp = app },
                    modifier = Modifier.testTag("review_app_${app.id}"),
                  ) {
                    Icon(Icons.Filled.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Verified")
                  }
                  OutlinedButton(
                    onClick = { rejectingApp = app },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("reject_app_${app.id}"),
                  ) {
                    Icon(Icons.Filled.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Rejected")
                  }
                  if (matchedStudent != null && canManageCards) {
                    if (matchedStudent.isIdCardActive()) {
                      OutlinedButton(
                        onClick = { onSetIdCardIssued(matchedStudent, false, null, "") },
                        modifier = Modifier.testTag("app_revoke_id_${app.id}"),
                      ) {
                        Icon(Icons.Filled.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Revoke ID Card")
                      }
                    } else {
                      OutlinedButton(
                        onClick = { issuingCardStudent = matchedStudent },
                        modifier = Modifier.testTag("app_issue_id_${app.id}"),
                      ) {
                        Icon(Icons.Filled.Badge, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Issue ID Card")
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    } else {
      if (filteredMembers.isEmpty()) {
        item {
          EmptyStateCard(title = "No Records in This View", subtitle = "Mark applications Verified or Issue ID Cards to manage General & Cabinet Member credentials.")
        }
      } else {
        items(filteredMembers, key = { it.id }) { st ->
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
                  modifier = Modifier.size(50.dp),
                  shape = CircleShape,
                  color = MaterialTheme.colorScheme.primaryContainer,
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
                Column(modifier = Modifier.weight(1f)) {
                  Text(st.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                  Text(
                    "${st.registrationNumber} • ${st.position.ifBlank { "General Member" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                  )
                  Text("ID: ${st.membershipId.ifBlank { "Unassigned" }}", style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                  StatusPill(label = st.membershipStatus)
                  StatusPill(label = if (st.isIdCardActive()) "ISSUED" else st.idCardStatus.ifBlank { "NOT_ISSUED" })
                  if (st.isCabinetMember()) StatusPill(label = "CABINET")
                }
              }

              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Button(onClick = { previewCardStudent = enrichStudentPhoto(st) }) {
                  Icon(Icons.Filled.QrCode2, contentDescription = null, modifier = Modifier.size(18.dp))
                  Spacer(Modifier.width(6.dp))
                  Text(if (st.isCabinetMember()) "Cabinet ID Card" else "General ID Card")
                }
                if (canApprove) {
                  if (!st.isVerifiedMember()) {
                    OutlinedButton(onClick = { onVerifyOrRejectStudent(st, true, null, "2027-06-30", "Verified by Admin") }) {
                      Text("Verified")
                    }
                  }
                  if (st.membershipStatus != "REJECTED") {
                    OutlinedButton(
                      onClick = { onVerifyOrRejectStudent(st, false, null, "", "Rejected by Admin") },
                      colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    ) {
                      Text("Rejected")
                    }
                  }
                }
                if (canManageCards) {
                  if (st.isIdCardActive()) {
                    OutlinedButton(onClick = { onSetIdCardIssued(st, false, null, "") }) {
                      Icon(Icons.Filled.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                      Spacer(Modifier.width(4.dp))
                      Text("Revoke ID Card")
                    }
                  } else {
                    OutlinedButton(onClick = { issuingCardStudent = st }) {
                      Icon(Icons.Filled.Badge, contentDescription = null, modifier = Modifier.size(16.dp))
                      Spacer(Modifier.width(4.dp))
                      Text("Issue ID Card")
                    }
                  }
                }
                if (canApprove || canManageCards) {
                  OutlinedButton(onClick = { onToggleStudentLock(st, !st.profileLocked) }) {
                    Icon(
                      imageVector = if (st.profileLocked) Icons.Filled.LockOpen else Icons.Filled.Lock,
                      contentDescription = null,
                      modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(if (st.profileLocked) "Unlock" else "Lock")
                  }
                }
                if (canAdmin) {
                  IconButton(onClick = { purgingStudent = st }) {
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

  // Verify Application Dialog with Cabinet / General Member Position Selector
  if (reviewingApp != null) {
    val app = reviewingApp!!
    val matchedStudent = students.firstOrNull { it.studentUid == app.studentUid || it.id == app.studentUid }
    val positionChoices = remember { listOf("General Member") + SocietyConstants.EXECUTIVE_DESIGNATIONS }
    var selectedPosition by remember(app) {
      mutableStateOf(
        if (matchedStudent != null && matchedStudent.isCabinetMember()) matchedStudent.position else "General Member"
      )
    }
    var validUntil by remember(app) { mutableStateOf("2027-06-30") }
    var notes by remember(app) { mutableStateOf("Application VERIFIED by YES Executive Board. Official ID Card issued and profile locked.") }

    AlertDialog(
      onDismissRequest = { reviewingApp = null },
      title = { Text("Mark Application VERIFIED & Issue ID Card") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("${app.studentName} (${app.registrationNumber})", fontWeight = FontWeight.Bold)
          Text(
            "Select General Member for the Standard Member ID Card, or an Executive Cabinet Position to issue the Royal Gold Cabinet Credential with Cabinet Position plaque.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          YesDropdownField(
            label = "Assigned Society Role / Card Type",
            options = positionChoices,
            selectedOption = selectedPosition,
            onOptionSelected = { selectedPosition = it },
            optionLabel = { pos ->
              if (pos == "General Member") "General Member (Standard ID Card)"
              else "$pos (Cabinet Member ID Card)"
            },
          )
          OutlinedTextField(
            value = validUntil,
            onValueChange = { validUntil = it },
            label = { Text("ID Card Validity Date (YYYY-MM-DD)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
          OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Verification Remarks") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onReviewApplication(app, true, validUntil, notes, selectedPosition)
            reviewingApp = null
          },
          modifier = Modifier.testTag("confirm_approve_membership_btn"),
        ) {
          Text("Mark Verified & Issue ID")
        }
      },
      dismissButton = {
        TextButton(onClick = { reviewingApp = null }) { Text("Cancel") }
      },
    )
  }

  // Reject Application Dialog
  if (rejectingApp != null) {
    val app = rejectingApp!!
    var rejectNotes by remember(app) {
      mutableStateOf("Application REJECTED by YES Administration. Please verify your academic registration details and student card.")
    }
    AlertDialog(
      onDismissRequest = { rejectingApp = null },
      title = { Text("Mark Application REJECTED") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("${app.studentName} (${app.registrationNumber})", fontWeight = FontWeight.Bold)
          OutlinedTextField(
            value = rejectNotes,
            onValueChange = { rejectNotes = it },
            label = { Text("Rejection Remarks") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onReviewApplication(app, false, "", rejectNotes, null)
            rejectingApp = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
          modifier = Modifier.testTag("confirm_reject_membership_btn"),
        ) {
          Text("Mark Rejected")
        }
      },
      dismissButton = {
        TextButton(onClick = { rejectingApp = null }) { Text("Cancel") }
      },
    )
  }

  // Issue ID Card Dialog (for student rows)
  if (issuingCardStudent != null) {
    val st = issuingCardStudent!!
    val positionChoices = remember { listOf("General Member") + SocietyConstants.EXECUTIVE_DESIGNATIONS }
    var selectedPosition by remember(st) {
      mutableStateOf(if (st.isCabinetMember()) st.position else "General Member")
    }
    var validUntil by remember(st) { mutableStateOf(st.membershipValidUntil.ifBlank { "2027-06-30" }) }

    AlertDialog(
      onDismissRequest = { issuingCardStudent = null },
      title = { Text("Issue Official YES ID Card") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("${st.fullName} (${st.registrationNumber})", fontWeight = FontWeight.Bold)
          YesDropdownField(
            label = "Card Type / Cabinet Role",
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
            onSetIdCardIssued(st, true, selectedPosition, validUntil)
            issuingCardStudent = null
          }
        ) {
          Text("Issue ID Card")
        }
      },
      dismissButton = {
        TextButton(onClick = { issuingCardStudent = null }) { Text("Cancel") }
      },
    )
  }

  // Purge All Data & Block Account Dialog
  if (purgingStudent != null) {
    val st = purgingStudent!!
    var blockReason by remember(st) {
      mutableStateOf("Account permanently blocked and all society records deleted by YES Administration.")
    }
    AlertDialog(
      onDismissRequest = { purgingStudent = null },
      title = { Text("Delete Whole Data & Block Login", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("${st.fullName} (${st.registrationNumber})", fontWeight = FontWeight.Bold)
          Text(
            "All student records across every module will be permanently deleted AND this student will be blocked from accessing their account when logging in.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
          )
          OutlinedTextField(
            value = blockReason,
            onValueChange = { blockReason = it },
            label = { Text("Block Reason") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onPurgeAndBlockStudent(st, blockReason)
            purgingStudent = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        ) {
          Text("Delete All & Block")
        }
      },
      dismissButton = {
        TextButton(onClick = { purgingStudent = null }) { Text("Cancel") }
      },
    )
  }

  if (previewCardStudent != null) {
    val st = enrichStudentPhoto(previewCardStudent!!)
    OfficialIdCardPreviewDialog(
      student = st,
      onDownloadPdf = {
        idCardExportTarget = st
        val safeReg = st.registrationNumber.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        pdfLauncher.launch("YES_ID_${safeReg.ifBlank { "Member" }}.pdf")
      },
      onDismiss = { previewCardStudent = null },
    )
  }
}


@Composable
fun AdminExecutiveBodyModuleScreen(
  canManageExecutive: Boolean,
  members: List<ExecutiveMember>,
  students: List<StudentProfile>,
  users: List<UserAccount> = emptyList(),
  onSaveMember: (ExecutiveMember?, String, String, String, String, String, String, Long, Boolean) -> Unit,
  onDeleteMember: (ExecutiveMember) -> Unit,
  onMessage: (String) -> Unit,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  var filterDesignation by remember { mutableStateOf("ALL") }
  var editingMember by remember { mutableStateOf<ExecutiveMember?>(null) }
  var showDialog by remember { mutableStateOf(false) }
  var memberToDelete by remember { mutableStateOf<ExecutiveMember?>(null) }
  var previewCabinetProfile by remember { mutableStateOf<StudentProfile?>(null) }
  var cabinetPdfTarget by remember { mutableStateOf<Pair<StudentProfile, IdCardDesignTheme>?>(null) }

  val cabinetPdfLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.CreateDocument("application/pdf")
  ) { uri: Uri? ->
    val target = cabinetPdfTarget
    if (uri != null && target != null) {
      val (profile, theme) = target
      scope.launch {
        PdfExportManager.exportStudentIdCardPdf(context, uri, profile, theme)
          .onSuccess { onMessage("Portrait Cabinet ID Card PDF saved for ${profile.fullName}!") }
          .onFailure { onMessage("Failed to save ID Card PDF: ${it.localizedMessage}") }
      }
    }
    cabinetPdfTarget = null
  }

  if (previewCabinetProfile != null) {
    val cardProfile = previewCabinetProfile!!
    OfficialIdCardPreviewDialog(
      student = cardProfile,
      onDownloadPdf = { theme ->
        cabinetPdfTarget = cardProfile to theme
        val safeName = cardProfile.fullName.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        cabinetPdfLauncher.launch("YES_Cabinet_ID_${safeName}.pdf")
      },
      onDismiss = { previewCabinetProfile = null },
    )
  }

  val filteredMembers = remember(members, filterDesignation) {
    if (filterDesignation == "ALL") members
    else members.filter { it.designation == filterDesignation }
  }

  if (memberToDelete != null) {
    val target = memberToDelete!!
    ConfirmDeleteDialog(
      itemTitle = "${target.fullName} — ${target.designation}",
      itemSubtitle = "Remove this office bearer from the Executive Body?",
      onConfirmDelete = { onDeleteMember(target) },
      onDismiss = { memberToDelete = null },
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
          label = "Filter by Designation",
          options = listOf("ALL") + SocietyConstants.EXECUTIVE_DESIGNATIONS,
          selectedOption = filterDesignation,
          onOptionSelected = { filterDesignation = it },
          optionLabel = { if (it == "ALL") "All Designations (${members.size})" else it },
          testTag = "admin_exec_designation_dropdown",
        )
      }

      if (filteredMembers.isEmpty()) {
        item {
          EmptyStateCard(
            title = "No Executive Office Bearers Listed",
            subtitle = "Add the President, Convener, Advisory Board, Senior Vice President, Vice President, Councillor, General Secretary, Joint Secretary, IT & Media Cell, Press Secretary, and Associates.",
            icon = Icons.Filled.Groups,
          )
        }
      } else {
        items(filteredMembers, key = { it.id }) { m ->
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                if (m.photoUri.isNotBlank()) {
                  SocietyImage(
                    imageSource = m.photoUri,
                    contentDescription = m.fullName,
                    modifier = Modifier.size(56.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop,
                  )
                } else {
                  Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(56.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                      Text(m.fullName.take(2).uppercase(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                  }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                  StatusPill(label = m.designation)
                  Text(m.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                  Text("${m.department} • Tenure ${m.tenure}", style = MaterialTheme.typography.bodySmall)
                }
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                OutlinedButton(
                  onClick = {
                    previewCabinetProfile = m.toCabinetIdCardProfile(students, users)
                  },
                  modifier = Modifier.testTag("view_cabinet_id_card_admin_${m.id}"),
                ) {
                  Icon(Icons.Filled.Badge, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(Modifier.width(6.dp))
                  Text("View ID Card")
                }

                if (canManageExecutive) {
                  Row {
                    IconButton(onClick = {
                      editingMember = m
                      showDialog = true
                    }) {
                      Icon(Icons.Filled.Edit, contentDescription = "Edit Office Bearer")
                    }
                    IconButton(
                      onClick = { memberToDelete = m },
                      modifier = Modifier.testTag("delete_exec_${m.id}"),
                    ) {
                      Icon(Icons.Filled.Delete, contentDescription = "Delete Office Bearer", tint = MaterialTheme.colorScheme.error)
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    if (canManageExecutive) {
      FloatingActionButton(
        onClick = {
          editingMember = null
          showDialog = true
        },
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(18.dp)
          .testTag("add_executive_member_fab"),
      ) {
        Icon(Icons.Filled.Add, contentDescription = "Add Office Bearer")
      }
    }
  }

  if (showDialog) {
    val m = editingMember
    var fullName by remember(m) { mutableStateOf(m?.fullName ?: "") }
    var designation by remember(m) { mutableStateOf(m?.designation ?: SocietyConstants.EXECUTIVE_DESIGNATIONS.first()) }
    var department by remember(m) { mutableStateOf(m?.department?.ifBlank { "Department of Entomology" } ?: "Department of Entomology") }
    var tenure by remember(m) { mutableStateOf(m?.tenure ?: "2026 - 2027") }
    var email by remember(m) { mutableStateOf(m?.email ?: "") }
    var photoUri by remember(m) { mutableStateOf(m?.photoUri ?: "") }
    var orderStr by remember(m) { mutableStateOf((m?.displayOrder ?: 10L).toString()) }
    var published by remember(m) { mutableStateOf(m?.published ?: true) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
      if (uri != null) {
        photoUri = ImageEncodingUtils.encodeUriToDataUrl(context, uri, maxDimension = 420, quality = 68) ?: uri.toString()
      }
    }

    AlertDialog(
      onDismissRequest = { showDialog = false },
      title = { Text(if (m == null) "Add Office Bearer" else "Edit Office Bearer") },
      text = {
        Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
          YesDropdownField(
            label = "Designation (Convener, Advisory, Executive & Associates)",
            options = SocietyConstants.EXECUTIVE_DESIGNATIONS,
            selectedOption = designation,
            onOptionSelected = { designation = it },
          )
          YesDropdownField(
            label = "Department",
            options = SocietyConstants.DEPARTMENTS,
            selectedOption = department,
            onOptionSelected = { department = it },
          )
          OutlinedTextField(value = tenure, onValueChange = { tenure = it }, label = { Text("Tenure * (e.g. 2026 - 2027)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Official Email") }, singleLine = true, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = orderStr, onValueChange = { orderStr = it }, label = { Text("Display Rank (1 = Top)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
          OutlinedButton(
            onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            modifier = Modifier.fillMaxWidth(),
          ) {
            Icon(Icons.Outlined.AddAPhoto, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(if (photoUri.isNotBlank()) "Photograph Selected ✓" else "Upload Office Bearer Photo")
          }
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Visible on Student Portal")
            Switch(checked = published, onCheckedChange = { published = it })
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onSaveMember(m, fullName, designation, department, tenure, email, photoUri, orderStr.toLongOrNull() ?: 10L, published)
            showDialog = false
          },
          enabled = fullName.trim().length >= 2 && tenure.isNotBlank(),
        ) {
          Text("Save")
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
fun AdminAttendanceModuleScreen(
  canManageAttendance: Boolean,
  events: List<SocietyEvent>,
  students: List<StudentProfile>,
  attendance: List<AttendanceRecord>,
  onMarkAttendance: (SocietyEvent, String, String, String, String, String, String) -> Unit,
  onDeleteAttendance: (AttendanceRecord) -> Unit,
  onMessage: (String) -> Unit,
) {
  val context = LocalContext.current
  var selectedEventId by remember(events) { mutableStateOf(events.firstOrNull()?.id ?: "") }
  val selectedEvent = events.firstOrNull { it.id == selectedEventId } ?: events.firstOrNull()
  var attendanceSheetFilter by remember { mutableStateOf("ALL") }
  var qrMemberScanInput by remember { mutableStateOf("") }
  var showCameraScanner by remember { mutableStateOf(false) }
  var showEventQrModal by remember { mutableStateOf(false) }
  var recordToDelete by remember { mutableStateOf<AttendanceRecord?>(null) }

  val eventAttendance = remember(attendance, selectedEvent) {
    if (selectedEvent == null) emptyList()
    else attendance.filter { it.eventId == selectedEvent.id }
  }
  val recordByStudent = remember(eventAttendance) {
    eventAttendance.associateBy { it.studentUid }
  }

  val filteredStudents = remember(students, recordByStudent, attendanceSheetFilter) {
    students.filter { st ->
      val uid = st.studentUid.ifBlank { st.id }
      val recStatus = recordByStudent[uid]?.status
      when (attendanceSheetFilter) {
        "PRESENT" -> recStatus == "PRESENT"
        "ABSENT" -> recStatus == "ABSENT"
        "EXCUSED" -> recStatus == "EXCUSED"
        "UNMARKED" -> recStatus == null
        else -> true
      }
    }
  }

  fun matchAndMarkStudentQr(rawPayload: String) {
    val ev = selectedEvent ?: return
    val raw = rawPayload.trim()
    val (idOrReg, uidHint) = when {
      raw.startsWith("YES-CABINET:") -> {
        val parts = raw.removePrefix("YES-CABINET:").split(":")
        parts.getOrNull(1)?.trim().orEmpty() to parts.getOrNull(2)?.trim().orEmpty()
      }
      raw.startsWith("YES-MEMBER:") -> {
        val parts = raw.removePrefix("YES-MEMBER:").split(":")
        parts.firstOrNull()?.trim().orEmpty() to parts.getOrNull(1)?.trim().orEmpty()
      }
      else -> {
        val parts = raw.split(":")
        parts.firstOrNull()?.trim().orEmpty() to parts.getOrNull(1)?.trim().orEmpty()
      }
    }
    val matched = students.firstOrNull { st ->
      (uidHint.isNotBlank() && (st.studentUid == uidHint || st.id == uidHint)) ||
        st.membershipId.equals(idOrReg, ignoreCase = true) ||
        st.registrationNumber.equals(idOrReg, ignoreCase = true)
    }
    if (matched != null) {
      onMarkAttendance(
        ev,
        matched.studentUid.ifBlank { matched.id },
        matched.fullName,
        matched.registrationNumber,
        matched.membershipId,
        "PRESENT",
        "QR_SCAN",
      )
      qrMemberScanInput = ""
    } else {
      onMessage("No student matched \"$raw\". Verify student ID card QR or Registration No.")
    }
  }

  if (showCameraScanner && selectedEvent != null) {
    QrCameraScannerDialog(
      title = "Scan Student ID Card QR",
      subtitle = "Point camera at a student's official YES ID Card QR code to mark PRESENT for ${selectedEvent.title}.",
      onQrCodeScanned = { code ->
        showCameraScanner = false
        matchAndMarkStudentQr(code)
      },
      onDismiss = { showCameraScanner = false },
    )
  }

  if (recordToDelete != null) {
    val target = recordToDelete!!
    ConfirmDeleteDialog(
      itemTitle = "${target.studentName} (${target.status})",
      itemSubtitle = "Delete attendance record for \"${target.eventTitle}\"?",
      onConfirmDelete = { onDeleteAttendance(target) },
      onDismiss = { recordToDelete = null },
    )
  }

  val exportPdfLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.CreateDocument("application/pdf")
  ) { uri: Uri? ->
    if (uri != null && selectedEvent != null) {
      val rows = eventAttendance.map {
        listOf(it.studentName, it.registrationNumber, it.membershipId.ifBlank { "-" }, it.status, it.method)
      }
      PdfExportManager.writeSummaryReportPdfToUri(
        context = context,
        uri = uri,
        reportTitle = "Attendance Report — ${selectedEvent.title}",
        subtitle = "Date: ${selectedEvent.eventDate} • Total Records: ${rows.size}",
        headers = listOf("Student Name", "Reg No", "Member ID", "Status", "Method"),
        rows = rows,
      ).onSuccess { onMessage("Attendance PDF exported successfully!") }
        .onFailure { onMessage("PDF export failed: ${it.localizedMessage}") }
    }
  }

  val exportCsvLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.CreateDocument("text/csv")
  ) { uri: Uri? ->
    if (uri != null && selectedEvent != null) {
      val rows = eventAttendance.map {
        listOf(it.eventTitle, it.eventDate, it.studentName, it.registrationNumber, it.membershipId, it.status, it.method)
      }
      PdfExportManager.writeCsvReportToUri(
        context = context,
        uri = uri,
        headers = listOf("Event", "Date", "Student Name", "Reg No", "Membership ID", "Status", "Method"),
        rows = rows,
      ).onSuccess { onMessage("Attendance CSV exported successfully!") }
        .onFailure { onMessage("CSV export failed: ${it.localizedMessage}") }
    }
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    if (events.isEmpty()) {
      item {
        EmptyStateCard(
          title = "No Events Available",
          subtitle = "Create a society event first to scan QR codes and mark student attendance.",
        )
      }
      return@LazyColumn
    }

    item {
      YesDropdownField(
        label = "Select Society Event",
        options = events.map { it.id },
        selectedOption = selectedEvent?.id ?: events.first().id,
        onOptionSelected = { selectedEventId = it },
        optionLabel = { id ->
          val ev = events.firstOrNull { it.id == id }
          if (ev != null) "${ev.title} (${ev.eventDate})" else id
        },
        testTag = "admin_attendance_event_dropdown",
      )
    }

    if (selectedEvent != null) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(selectedEvent.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Present: ${eventAttendance.count { it.status == "PRESENT" }} • Total Marked: ${eventAttendance.size}", style = MaterialTheme.typography.bodySmall)
              }
              OutlinedButton(onClick = { showEventQrModal = true }) {
                Icon(Icons.Filled.QrCode2, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Event QR")
              }
            }

            if (canManageAttendance) {
              Button(
                onClick = { showCameraScanner = true },
                modifier = Modifier.fillMaxWidth().testTag("open_admin_qr_camera_btn"),
              ) {
                Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Open Camera QR Scanner (Scan Student ID Card)")
              }

              OutlinedTextField(
                value = qrMemberScanInput,
                onValueChange = { qrMemberScanInput = it },
                label = { Text("Or Enter Student Membership ID / Reg No") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("scan_student_qr_input"),
              )
              OutlinedButton(
                onClick = { matchAndMarkStudentQr(qrMemberScanInput) },
                enabled = qrMemberScanInput.isNotBlank(),
                modifier = Modifier.fillMaxWidth().testTag("submit_qr_attendance_btn"),
              ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Mark Student PRESENT by ID / Reg No")
              }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
              OutlinedButton(
                onClick = { exportPdfLauncher.launch("YES_Attendance_${selectedEvent.eventDate}.pdf") },
                modifier = Modifier.weight(1f),
              ) {
                Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Export PDF")
              }
              OutlinedButton(
                onClick = { exportCsvLauncher.launch("YES_Attendance_${selectedEvent.eventDate}.csv") },
                modifier = Modifier.weight(1f),
              ) {
                Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Export CSV")
              }
            }
          }
        }
      }

      if (eventAttendance.isNotEmpty()) {
        item {
          SectionTitleRow(title = "Marked Attendance Records (${eventAttendance.size})")
        }
        items(eventAttendance, key = { "marked_${it.id}" }) { rec ->
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
              Column(modifier = Modifier.weight(1f)) {
                Text(rec.studentName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("${rec.registrationNumber} • Method: ${rec.method}", style = MaterialTheme.typography.bodySmall)
              }
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StatusPill(label = rec.status)
                if (canManageAttendance) {
                  IconButton(
                    onClick = { recordToDelete = rec },
                    modifier = Modifier.testTag("delete_attendance_${rec.id}"),
                  ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete Attendance Record", tint = MaterialTheme.colorScheme.error)
                  }
                }
              }
            }
          }
        }
      }

      item {
        SectionTitleRow(title = "Manual Student Attendance Sheet (${filteredStudents.size})")
        YesDropdownField(
          label = "Filter Attendance Sheet",
          options = listOf("ALL", "PRESENT", "ABSENT", "EXCUSED", "UNMARKED"),
          selectedOption = attendanceSheetFilter,
          onOptionSelected = { attendanceSheetFilter = it },
          optionLabel = {
            when (it) {
              "ALL" -> "All Students (${students.size})"
              "UNMARKED" -> "Not Yet Marked"
              else -> it
            }
          },
          testTag = "admin_attendance_filter_dropdown",
        )
      }

      items(filteredStudents, key = { it.id }) { st ->
        val uid = st.studentUid.ifBlank { st.id }
        val existingRecord = recordByStudent[uid]
        val currentStatus = existingRecord?.status
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(st.fullName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("${st.registrationNumber} • ${st.membershipId.ifBlank { "No ID" }}", style = MaterialTheme.typography.bodySmall)
              }
              Row(verticalAlignment = Alignment.CenterVertically) {
                if (currentStatus != null) {
                  StatusPill(label = currentStatus)
                }
                if (canManageAttendance && existingRecord != null) {
                  IconButton(onClick = { recordToDelete = existingRecord }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Clear Attendance", tint = MaterialTheme.colorScheme.error)
                  }
                }
              }
            }
            if (canManageAttendance) {
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("PRESENT", "ABSENT", "EXCUSED").forEach { statusOpt ->
                  FilterChip(
                    selected = currentStatus == statusOpt,
                    onClick = {
                      onMarkAttendance(
                        selectedEvent,
                        uid,
                        st.fullName,
                        st.registrationNumber,
                        st.membershipId,
                        statusOpt,
                        "MANUAL",
                      )
                    },
                    label = { Text(statusOpt) },
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  if (showEventQrModal && selectedEvent != null) {
    AlertDialog(
      onDismissRequest = { showEventQrModal = false },
      title = { Text("Event Check-In QR") },
      text = {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
          Text(selectedEvent.title, fontWeight = FontWeight.Bold)
          YesQrCodeBox(payload = selectedEvent.qrCodeToken, boxSize = 180.dp)
          Text("Token: ${selectedEvent.qrCodeToken}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
      },
      confirmButton = {
        Button(onClick = { showEventQrModal = false }) { Text("Close") }
      },
    )
  }
}
