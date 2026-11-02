package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.auth.authStateFlow
import com.example.data.auth.signOutFromSociety
import com.example.data.model.BlockedAccount
import com.example.ui.components.SplitSocietyUniversityHeader
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.StudentDashboardScreen
import com.example.ui.screens.StudentOnboardingScreen
import com.example.ui.screens.UnifiedAuthScreen
import com.example.ui.screens.YesSplashScreen
import com.example.ui.theme.AppFontSize
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ThemePreference
import com.example.ui.viewmodel.RoleSessionState
import com.example.ui.viewmodel.SocietyViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

private const val PREFS_NAME = "yes_ui_prefs"
private const val KEY_APP_FONT_SIZE = "app_font_size_scale"

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val savedFontSize = runCatching {
      AppFontSize.valueOf(
        prefs.getString(KEY_APP_FONT_SIZE, AppFontSize.NORMAL.name)
          ?: AppFontSize.NORMAL.name
      )
    }.getOrDefault(AppFontSize.NORMAL)

    setContent {
      var appFontSize by rememberSaveable { mutableStateOf(savedFontSize) }

      MyApplicationTheme(
        themePreference = ThemePreference.LIGHT,
        appFontSize = appFontSize,
      ) {
        YesRootApp(
          appFontSize = appFontSize,
          onAppFontSizeChange = { nextSize ->
            appFontSize = nextSize
            prefs.edit().putString(KEY_APP_FONT_SIZE, nextSize.name).apply()
          },
        )
      }
    }
  }
}

@Composable
fun YesRootApp(
  appFontSize: AppFontSize = AppFontSize.NORMAL,
  onAppFontSizeChange: (AppFontSize) -> Unit = {},
) {
  var showSplash by rememberSaveable { mutableStateOf(true) }

  if (showSplash) {
    YesSplashScreen(
      onSplashFinished = { showSplash = false },
    )
    return
  }

  val currentUser by remember { Firebase.auth.authStateFlow() }
    .collectAsStateWithLifecycle(initialValue = Firebase.auth.currentUser)

  val user = currentUser
  if (user == null) {
    UnifiedAuthScreen(onAuthenticated = {})
  } else {
    AuthenticatedRoleRouter(
      user = user,
      appFontSize = appFontSize,
      onAppFontSizeChange = onAppFontSizeChange,
    )
  }
}

@Composable
private fun AuthenticatedRoleRouter(
  user: FirebaseUser,
  appFontSize: AppFontSize,
  onAppFontSizeChange: (AppFontSize) -> Unit,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }
  val snackbarHostState = remember { SnackbarHostState() }

  val vm: SocietyViewModel = viewModel(
    key = user.uid,
    factory = SocietyViewModel.factory(
      currentUserId = user.uid,
      currentUserEmail = user.email.orEmpty(),
      currentDisplayName = user.displayName.orEmpty(),
      currentPhotoUri = user.photoUrl?.toString().orEmpty(),
    ),
  )

  val roleState by vm.roleState.collectAsStateWithLifecycle()
  val snackbarMessage by vm.snackbarMessage.collectAsStateWithLifecycle()
  val isBusy by vm.isBusy.collectAsStateWithLifecycle()

  val liveAdmin by vm.liveAdminAccount.collectAsStateWithLifecycle()
  val liveStudent by vm.liveStudentProfile.collectAsStateWithLifecycle()
  val publishedEvents by vm.publishedEvents.collectAsStateWithLifecycle()
  val publishedExecutive by vm.publishedExecutive.collectAsStateWithLifecycle()
  val publishedAnnouncements by vm.publishedAnnouncements.collectAsStateWithLifecycle()
  val approvedGallery by vm.approvedGallery.collectAsStateWithLifecycle()
  val activePaymentAccounts by vm.activePaymentAccounts.collectAsStateWithLifecycle()
  val myMembershipApps by vm.myMembershipApps.collectAsStateWithLifecycle()
  val myRegistrations by vm.myRegistrations.collectAsStateWithLifecycle()
  val myAttendance by vm.myAttendance.collectAsStateWithLifecycle()
  val myPayments by vm.myPayments.collectAsStateWithLifecycle()
  val myCertificates by vm.myCertificates.collectAsStateWithLifecycle()
  val myFeedback by vm.myFeedback.collectAsStateWithLifecycle()
  val myNotifications by vm.myNotifications.collectAsStateWithLifecycle()
  val verifiedCertificate by vm.verifiedCertificateResult.collectAsStateWithLifecycle()
  val verificationChecked by vm.verificationChecked.collectAsStateWithLifecycle()

  LaunchedEffect(snackbarMessage) {
    val msg = snackbarMessage
    if (!msg.isNullOrBlank()) {
      snackbarHostState.showSnackbar(msg)
      vm.clearSnackbar()
    }
  }

  val handleSignOut: () -> Unit = {
    signOutFromSociety(
      credentialManager = credentialManager,
      scope = scope,
    )
  }

  when (val state = roleState) {
    is RoleSessionState.Loading -> {
      Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
      ) {
        Box(
          modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(24.dp),
          contentAlignment = Alignment.Center,
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
          ) {
            SplitSocietyUniversityHeader(
              title = "Young Entomologist Society",
              subtitle = "Department of Entomology • UAF",
              logoSize = 62.dp,
            )
            CircularProgressIndicator()
            Text(
              text = "Verifying Society Role & Permissions...",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onBackground,
            )
          }
        }
      }
    }

    is RoleSessionState.BlockedSession -> {
      BlockedAccountScreen(
        blocked = state.blocked,
        onSignOut = handleSignOut,
      )
    }

    is RoleSessionState.NeedsStudentRegistration -> {
      StudentOnboardingScreen(
        email = state.email,
        initialPhotoUri = state.photoUri,
        isBusy = isBusy,
        onSubmitRegistration = { fullName, regNo, degreeLevel, dept, sem, phone, photo, cardPhoto ->
          vm.completeStudentRegistration(
            fullName = fullName,
            registrationNumber = regNo,
            degreeLevel = degreeLevel,
            department = dept,
            semester = sem,
            phone = phone,
            photoUri = photo,
            studentCardUri = cardPhoto,
          )
        },
        onSignOut = handleSignOut,
      )
    }

    is RoleSessionState.AdminSession -> {
      val activeAdmin = liveAdmin ?: state.admin
      AdminDashboardScreen(
        admin = activeAdmin,
        viewModel = vm,
        appFontSize = appFontSize,
        onAppFontSizeChange = onAppFontSizeChange,
        snackbarHostState = snackbarHostState,
        onSignOut = handleSignOut,
      )
    }

    is RoleSessionState.StudentSession -> {
      val rawStudent = liveStudent ?: state.student
      val activeStudent = if (rawStudent.photoUri.isBlank() && user.photoUrl != null) {
        rawStudent.copy(photoUri = user.photoUrl.toString())
      } else {
        rawStudent
      }
      StudentDashboardScreen(
        student = activeStudent,
        events = publishedEvents,
        registrations = myRegistrations,
        membershipApps = myMembershipApps,
        announcements = publishedAnnouncements,
        notifications = myNotifications,
        executiveBody = publishedExecutive,
        payments = myPayments,
        paymentAccounts = activePaymentAccounts,
        attendance = myAttendance,
        certificates = myCertificates,
        galleryItems = approvedGallery,
        feedbackList = myFeedback,
        verifiedCertificate = verifiedCertificate,
        verificationChecked = verificationChecked,
        appFontSize = appFontSize,
        onAppFontSizeChange = onAppFontSizeChange,
        snackbarHostState = snackbarHostState,
        onMessage = { vm.postMessage(it) },
        onApplyMembership = { vm.applyForMembership(activeStudent, it) },
        onDeleteMembershipApplication = { vm.deleteMembershipApp(it, isAdmin = false) },
        onRegisterEvent = { ev, ref, receipt -> vm.registerForEvent(ev, activeStudent, ref, receipt) },
        onCancelRegistration = { reg, ev -> vm.deleteRegistration(reg, ev, isAdmin = false) },
        onUploadPaymentReceipt = { pay, date, ref, receipt -> vm.uploadPaymentReceipt(pay, date, ref, receipt) },
        onDeletePayment = { vm.deletePaymentRecord(it, isAdmin = false) },
        onQrCheckIn = { vm.checkInEventByQr(it, activeStudent) },
        onVerifyCertificate = { vm.verifyCertificate(it) },
        onClearVerification = { vm.clearCertificateVerification() },
        onSubmitFeedback = { cat, sub, msg, evTitle -> vm.submitFeedback(activeStudent.fullName, cat, sub, msg, evTitle) },
        onDeleteFeedback = { vm.deleteFeedbackItem(it, isAdmin = false) },
        onMarkNotificationRead = { vm.markNotificationRead(it) },
        onDeleteNotification = { vm.deleteNotification(it) },
        onUpdateProfile = { f, r, deg, d, s, p, ph, cardPh ->
          vm.updateOwnProfile(f, r, deg, d, s, p, ph, cardPh)
        },
        onSignOut = handleSignOut,
      )
    }

    is RoleSessionState.Error -> {
      Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
      ) {
        Box(
          modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(24.dp),
          contentAlignment = Alignment.Center,
        ) {
          Card(
            modifier = Modifier.fillMaxWidth().widthIn(max = 460.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          ) {
            Column(
              modifier = Modifier.padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              SplitSocietyUniversityHeader(
                title = "Young Entomologist Society",
                subtitle = "Department of Entomology • UAF",
                logoSize = 52.dp,
              )
              Text("Account Verification Issue", style = MaterialTheme.typography.headlineSmall)
              Text(
                text = state.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
              )
              Spacer(Modifier.height(4.dp))
              Button(
                onClick = { vm.resolveRoleAndSession() },
                modifier = Modifier.fillMaxWidth().testTag("retry_role_resolution_btn"),
              ) {
                Text("Retry Connection")
              }
              OutlinedButton(
                onClick = handleSignOut,
                modifier = Modifier.fillMaxWidth().testTag("error_sign_out_btn"),
              ) {
                Text("Sign Out")
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun BlockedAccountScreen(
  blocked: BlockedAccount,
  onSignOut: () -> Unit,
) {
  Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background,
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .windowInsetsPadding(WindowInsets.safeDrawing)
        .padding(24.dp),
      contentAlignment = Alignment.Center,
    ) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 480.dp)
          .testTag("blocked_account_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error),
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          SplitSocietyUniversityHeader(
            title = "Young Entomologist Society",
            subtitle = "Department of Entomology • UAF",
            logoSize = 48.dp,
          )

          HorizontalDivider()

          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.size(64.dp),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Filled.Block,
                contentDescription = "Blocked Account",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(34.dp),
              )
            }
          }

          Text(
            text = "Account Access Blocked",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
          )

          Text(
            text = "Your student data has been removed and login access to the Young Entomologist Society portal has been blocked by Society Administration.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
          )

          if (blocked.reason.isNotBlank()) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text(
                text = "Administrative Notice: ${blocked.reason}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(12.dp),
              )
            }
          }

          Button(
            onClick = onSignOut,
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.error,
              contentColor = MaterialTheme.colorScheme.onError,
            ),
            modifier = Modifier.fillMaxWidth().testTag("blocked_sign_out_btn"),
          ) {
            Text("Sign Out")
          }
        }
      }
    }
  }
}
