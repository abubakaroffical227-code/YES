package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.data.model.AdminAccount
import com.example.data.model.AdminAuditLog
import com.example.data.model.AdminRoleType
import com.example.data.model.Announcement
import com.example.data.model.AttendanceRecord
import com.example.data.model.BOOTSTRAPPED_SUPER_ADMIN_EMAIL
import com.example.data.model.BlockedAccount
import com.example.data.model.EventRegistration
import com.example.data.model.ExecutiveMember
import com.example.data.model.FinanceTransaction
import com.example.data.model.GalleryItem
import com.example.data.model.MembershipApplication
import com.example.data.model.PaymentAccount
import com.example.data.model.SocietyCertificate
import com.example.data.model.SocietyEvent
import com.example.data.model.StudentFeedback
import com.example.data.model.SocietyConfig
import com.example.data.model.StudentPayment
import com.example.data.model.StudentProfile
import com.example.data.model.UserAccount
import com.example.data.model.UserNotification
import com.example.data.model.hasPermission
import com.example.data.model.toCabinetIdCardProfile
import com.example.data.repository.SocietyRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface RoleSessionState {
  data object Loading : RoleSessionState
  data class NeedsStudentRegistration(
    val uid: String,
    val email: String,
    val suggestedName: String,
    val photoUri: String,
  ) : RoleSessionState
  data class AdminSession(val admin: AdminAccount) : RoleSessionState
  data class StudentSession(val student: StudentProfile) : RoleSessionState
  data class BlockedSession(val blocked: BlockedAccount) : RoleSessionState
  data class Error(val message: String) : RoleSessionState
}

@OptIn(ExperimentalCoroutinesApi::class)
class SocietyViewModel(
  private val repository: SocietyRepository,
  val currentUserId: String,
  val currentUserEmail: String,
  val currentDisplayName: String,
  val currentPhotoUri: String,
) : ViewModel() {

  private val _roleState = MutableStateFlow<RoleSessionState>(RoleSessionState.Loading)
  val roleState: StateFlow<RoleSessionState> = _roleState.asStateFlow()

  private val _snackbarMessage = MutableStateFlow<String?>(null)
  val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

  private val _isBusy = MutableStateFlow(false)
  val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

  private val _verifiedCertificateResult = MutableStateFlow<SocietyCertificate?>(null)
  val verifiedCertificateResult: StateFlow<SocietyCertificate?> = _verifiedCertificateResult.asStateFlow()

  private val _verificationChecked = MutableStateFlow(false)
  val verificationChecked: StateFlow<Boolean> = _verificationChecked.asStateFlow()

  init {
    resolveRoleAndSession()
    observeLiveBlockStatus()
  }

  private fun observeLiveBlockStatus() {
    viewModelScope.launch {
      repository.observeBlockedAccount(currentUserId)
        .catch { e -> Log.w(TAG, "Blocked account listener warning: ${e.message}") }
        .collectLatest { blocked ->
          val current = _roleState.value
          if (blocked != null && current !is RoleSessionState.AdminSession) {
            _roleState.value = RoleSessionState.BlockedSession(blocked)
          } else if (blocked == null && current is RoleSessionState.BlockedSession) {
            resolveRoleAndSession()
          }
        }
    }
  }

  fun clearSnackbar() {
    _snackbarMessage.value = null
  }

  fun postMessage(msg: String) {
    _snackbarMessage.value = msg
  }

  fun resolveRoleAndSession() {
    viewModelScope.launch {
      _roleState.value = RoleSessionState.Loading
      try {
        // 1. Check if bootstrapped owner email -> auto-provision Super Admin
        if (currentUserEmail.equals(BOOTSTRAPPED_SUPER_ADMIN_EMAIL, ignoreCase = true)) {
          val superAdmin = repository.bootstrapSuperAdminIfOwner(
            uid = currentUserId,
            email = currentUserEmail,
            fullName = currentDisplayName.ifBlank { "Administrator YES" },
            photoUri = currentPhotoUri,
          )
          if (superAdmin != null && superAdmin.active) {
            _roleState.value = RoleSessionState.AdminSession(superAdmin)
            return@launch
          }
        }

        // 2. Check if user has an active Admin record in /admins/{uid}
        val existingAdmin = repository.getAdminAccount(currentUserId)
        if (existingAdmin != null && existingAdmin.active) {
          _roleState.value = RoleSessionState.AdminSession(existingAdmin)
          return@launch
        }

        // 3. Check if student account is blocked by Administration
        val blockedAccount = repository.getBlockedAccount(currentUserId)
        if (blockedAccount != null) {
          _roleState.value = RoleSessionState.BlockedSession(blockedAccount)
          return@launch
        }

        // 4. Otherwise check Student profile in /students/{uid}
        val studentProfile = repository.getStudentProfile(currentUserId)
        if (studentProfile != null) {
          if (!studentProfile.active) {
            _roleState.value = RoleSessionState.Error(
              "Your student account has been deactivated by the society administration. Please contact the General Secretary."
            )
          } else {
            _roleState.value = RoleSessionState.StudentSession(studentProfile)
          }
        } else {
          _roleState.value = RoleSessionState.NeedsStudentRegistration(
            uid = currentUserId,
            email = currentUserEmail,
            suggestedName = currentDisplayName,
            photoUri = currentPhotoUri,
          )
        }
      } catch (e: Exception) {
        Log.e(TAG, "Failed to resolve user role", e)
        _roleState.value = RoleSessionState.Error(
          e.localizedMessage ?: "Unable to load your society account. Check connection and try again."
        )
      }
    }
  }

  private fun <T> Flow<List<T>>.safeListState(tag: String): StateFlow<List<T>> =
    this.catch { e ->
      Log.w(TAG, "Stream error on $tag: ${e.message}")
      emit(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

  val liveStudentProfile: StateFlow<StudentProfile?> = _roleState
    .flatMapLatest { state ->
      when (state) {
        is RoleSessionState.StudentSession -> repository.observeStudentProfile(currentUserId)
        else -> flowOf(null)
      }
    }
    .catch { emit(null) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

  val liveAdminAccount: StateFlow<AdminAccount?> = _roleState
    .flatMapLatest { state ->
      if (state is RoleSessionState.AdminSession) {
        repository.observeAdminAccount(currentUserId)
      } else {
        flowOf(null)
      }
    }
    .catch { emit(null) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

  // Shared Published Streams
  val publishedEvents: StateFlow<List<SocietyEvent>> = _roleState
    .flatMapLatest { state ->
      if (state is RoleSessionState.StudentSession || state is RoleSessionState.AdminSession) {
        repository.observePublishedEvents()
      } else flowOf(emptyList())
    }.safeListState("publishedEvents")

  val publishedExecutive: StateFlow<List<ExecutiveMember>> = _roleState
    .flatMapLatest { state ->
      if (state is RoleSessionState.StudentSession || state is RoleSessionState.AdminSession) {
        repository.observePublishedExecutiveBody()
      } else flowOf(emptyList())
    }.safeListState("publishedExecutive")

  val publishedAnnouncements: StateFlow<List<Announcement>> = _roleState
    .flatMapLatest { state ->
      if (state is RoleSessionState.StudentSession || state is RoleSessionState.AdminSession) {
        repository.observePublishedAnnouncements()
      } else flowOf(emptyList())
    }.safeListState("publishedAnnouncements")

  val approvedGallery: StateFlow<List<GalleryItem>> = _roleState
    .flatMapLatest { state ->
      if (state is RoleSessionState.StudentSession || state is RoleSessionState.AdminSession) {
        repository.observeApprovedGallery()
      } else flowOf(emptyList())
    }.safeListState("approvedGallery")

  val activePaymentAccounts: StateFlow<List<PaymentAccount>> = _roleState
    .flatMapLatest { state ->
      if (state is RoleSessionState.StudentSession || state is RoleSessionState.AdminSession) {
        repository.observeActivePaymentAccounts()
      } else flowOf(emptyList())
    }.safeListState("activePaymentAccounts")

  // Student-Scoped Personal Streams
  val myMembershipApps: StateFlow<List<MembershipApplication>> = _roleState
    .flatMapLatest { state ->
      if (state is RoleSessionState.StudentSession) {
        repository.observeStudentMembershipApplications(currentUserId)
      } else flowOf(emptyList())
    }.safeListState("myMembershipApps")

  val myRegistrations: StateFlow<List<EventRegistration>> = _roleState
    .flatMapLatest { state ->
      if (state is RoleSessionState.StudentSession || state is RoleSessionState.AdminSession) {
        repository.observeStudentRegistrations(currentUserId)
      } else flowOf(emptyList())
    }.safeListState("myRegistrations")

  val myAttendance: StateFlow<List<AttendanceRecord>> = _roleState
    .flatMapLatest { state ->
      if (state is RoleSessionState.StudentSession || state is RoleSessionState.AdminSession) {
        repository.observeStudentAttendance(currentUserId)
      } else flowOf(emptyList())
    }.safeListState("myAttendance")

  val myPayments: StateFlow<List<StudentPayment>> = _roleState
    .flatMapLatest { state ->
      if (state is RoleSessionState.StudentSession) {
        repository.observeStudentPayments(currentUserId)
      } else flowOf(emptyList())
    }.safeListState("myPayments")

  val myCertificates: StateFlow<List<SocietyCertificate>> = _roleState
    .flatMapLatest { state ->
      if (state is RoleSessionState.StudentSession) {
        repository.observeStudentCertificates(currentUserId)
      } else flowOf(emptyList())
    }.safeListState("myCertificates")

  val myFeedback: StateFlow<List<StudentFeedback>> = _roleState
    .flatMapLatest { state ->
      if (state is RoleSessionState.StudentSession) {
        repository.observeStudentFeedback(currentUserId)
      } else flowOf(emptyList())
    }.safeListState("myFeedback")

  val myNotifications: StateFlow<List<UserNotification>> = _roleState
    .flatMapLatest { state ->
      if (state is RoleSessionState.StudentSession) {
        repository.observeUserNotifications(currentUserId)
      } else flowOf(emptyList())
    }.safeListState("myNotifications")

  // Admin-Only Role & Permission-Scoped Streams
  val adminAllStudents: StateFlow<List<StudentProfile>> = _roleState
    .flatMapLatest { state ->
      if (state is RoleSessionState.AdminSession) repository.observeAllStudentsAdmin()
      else flowOf(emptyList())
    }.safeListState("adminAllStudents")

  val adminBlockedAccounts: StateFlow<List<BlockedAccount>> = _roleState
    .flatMapLatest { state ->
      if (state is RoleSessionState.AdminSession &&
        (state.admin.hasPermission("MANAGE_STUDENTS") || state.admin.hasPermission("MANAGE_MEMBERSHIP"))
      ) {
        repository.observeAllBlockedAccountsAdmin()
      } else flowOf(emptyList())
    }.safeListState("adminBlockedAccounts")

  val adminAllUsers: StateFlow<List<UserAccount>> = _roleState
    .flatMapLatest { state ->
      if (state is RoleSessionState.AdminSession) repository.observeAllUsersAdmin()
      else flowOf(emptyList())
    }.safeListState("adminAllUsers")

  val adminAllAdmins: StateFlow<List<AdminAccount>> = _roleState
    .flatMapLatest { state ->
      if (state is RoleSessionState.AdminSession) repository.observeAllAdmins()
      else flowOf(emptyList())
    }.safeListState("adminAllAdmins")

  val adminAllEvents: StateFlow<List<SocietyEvent>> = _roleState
    .flatMapLatest { state ->
      if (state is RoleSessionState.AdminSession) repository.observeAllEventsAdmin()
      else flowOf(emptyList())
    }.safeListState("adminAllEvents")

  val adminAllMembershipApps: StateFlow<List<MembershipApplication>> = _roleState
    .flatMapLatest { state ->
      if (state is RoleSessionState.AdminSession && state.admin.hasPermission("MANAGE_MEMBERSHIP")) {
        repository.observeAllMembershipApplicationsAdmin()
      } else flowOf(emptyList())
    }.safeListState("adminAllMembershipApps")

  val adminAllExecutive: StateFlow<List<ExecutiveMember>> = _roleState
    .flatMapLatest { state ->
      if (state is RoleSessionState.AdminSession) {
        if (state.admin.hasPermission("MANAGE_EXECUTIVE")) {
          repository.observeAllExecutiveBodyAdmin()
        } else {
          repository.observePublishedExecutiveBody()
        }
      } else flowOf(emptyList())
    }.safeListState("adminAllExecutive")

  val adminAllRegistrations: StateFlow<List<EventRegistration>> = _roleState
    .flatMapLatest { state ->
      if (state is RoleSessionState.AdminSession &&
        (state.admin.hasPermission("MANAGE_EVENTS") ||
          state.admin.hasPermission("MANAGE_REGISTRATIONS") ||
          state.admin.hasPermission("MANAGE_FINANCE"))
      ) {
        repository.observeAllRegistrationsAdmin()
      } else flowOf(emptyList())
    }.safeListState("adminAllRegistrations")

  val adminAllAttendance: StateFlow<List<AttendanceRecord>> = _roleState
    .flatMapLatest { state ->
      if (state is RoleSessionState.AdminSession &&
        (state.admin.hasPermission("MANAGE_ATTENDANCE") || state.admin.hasPermission("MANAGE_EVENTS"))
      ) {
        repository.observeAllAttendanceAdmin()
      } else flowOf(emptyList())
    }.safeListState("adminAllAttendance")

  val adminAllAnnouncements: StateFlow<List<Announcement>> = _roleState
    .flatMapLatest { state ->
      if (state is RoleSessionState.AdminSession &&
        (state.admin.hasPermission("MANAGE_ANNOUNCEMENTS") || state.admin.hasPermission("MANAGE_EVENTS"))
      ) {
        repository.observeAllAnnouncementsAdmin()
      } else flowOf(emptyList())
    }.safeListState("adminAllAnnouncements")

  val adminAllPayments: StateFlow<List<StudentPayment>> = _roleState
    .flatMapLatest { state ->
      if (state is RoleSessionState.AdminSession && state.admin.hasPermission("MANAGE_FINANCE")) {
        repository.observeAllPaymentsFinance()
      } else flowOf(emptyList())
    }.safeListState("adminAllPayments")

  val adminFinanceTransactions: StateFlow<List<FinanceTransaction>> = _roleState
    .flatMapLatest { state ->
      if (state is RoleSessionState.AdminSession && state.admin.hasPermission("MANAGE_FINANCE")) {
        repository.observeFinanceTransactions()
      } else flowOf(emptyList())
    }.safeListState("adminFinanceTransactions")

  val adminPaymentAccounts: StateFlow<List<PaymentAccount>> = _roleState
    .flatMapLatest { state ->
      if (state is RoleSessionState.AdminSession && state.admin.hasPermission("MANAGE_FINANCE")) {
        repository.observeAllPaymentAccountsAdmin()
      } else flowOf(emptyList())
    }.safeListState("adminPaymentAccounts")

  val adminAllGallery: StateFlow<List<GalleryItem>> = _roleState
    .flatMapLatest { state ->
      if (state is RoleSessionState.AdminSession && state.admin.hasPermission("MANAGE_GALLERY")) {
        repository.observeAllGalleryAdmin()
      } else flowOf(emptyList())
    }.safeListState("adminAllGallery")

  val adminAllCertificates: StateFlow<List<SocietyCertificate>> = _roleState
    .flatMapLatest { state ->
      if (state is RoleSessionState.AdminSession &&
        (state.admin.hasPermission("MANAGE_CERTIFICATES") || state.admin.hasPermission("MANAGE_EVENTS"))
      ) {
        repository.observeAllCertificatesAdmin()
      } else flowOf(emptyList())
    }.safeListState("adminAllCertificates")

  val adminAllFeedback: StateFlow<List<StudentFeedback>> = _roleState
    .flatMapLatest { state ->
      if (state is RoleSessionState.AdminSession) repository.observeAllFeedbackAdmin()
      else flowOf(emptyList())
    }.safeListState("adminAllFeedback")

  val adminAuditLogs: StateFlow<List<AdminAuditLog>> = _roleState
    .flatMapLatest { state ->
      if (state is RoleSessionState.AdminSession) repository.observeAuditLogsAdmin()
      else flowOf(emptyList())
    }.safeListState("adminAuditLogs")

  // --- Action Execution Wrapper ---

  private fun executeAction(successMessage: String, block: suspend () -> Unit) {
    viewModelScope.launch {
      _isBusy.value = true
      try {
        block()
        if (successMessage.isNotBlank()) {
          _snackbarMessage.value = successMessage
        }
      } catch (e: Exception) {
        Log.e(TAG, "Action failed", e)
        _snackbarMessage.value = e.localizedMessage ?: "Operation failed. Check permissions and fields."
      } finally {
        _isBusy.value = false
      }
    }
  }

  // --- Student & Shared Actions ---

  fun completeStudentRegistration(
    fullName: String,
    registrationNumber: String,
    degreeLevel: String,
    department: String,
    semester: String,
    phone: String,
    photoUri: String,
    studentCardUri: String,
  ) {
    executeAction("Welcome to Young Entomologist Society!") {
      repository.registerStudentOnboarding(
        fullName = fullName,
        email = currentUserEmail.ifBlank { "$currentUserId@uaf.edu.pk" },
        registrationNumber = registrationNumber,
        department = department,
        semester = semester,
        phone = phone,
        photoUri = photoUri,
        degreeLevel = degreeLevel,
        studentCardUri = studentCardUri,
      )
      resolveRoleAndSession()
    }
  }

  fun updateOwnProfile(
    fullName: String,
    registrationNumber: String,
    degreeLevel: String,
    department: String,
    semester: String,
    phone: String,
    photoUri: String,
    studentCardUri: String,
  ) = executeAction("Student profile updated.") {
    repository.updateOwnStudentProfile(
      fullName = fullName,
      registrationNumber = registrationNumber,
      department = department,
      semester = semester,
      phone = phone,
      photoUri = photoUri,
      degreeLevel = degreeLevel,
      studentCardUri = studentCardUri,
    )
  }

  fun applyForMembership(profile: StudentProfile, motivation: String) =
    executeAction("Membership application submitted for review!") {
      repository.submitMembershipApplication(profile, motivation)
    }

  fun deleteMembershipApp(application: MembershipApplication, isAdmin: Boolean) =
    executeAction("Membership application deleted.") {
      if (isAdmin) {
        val (actorName, actorRole) = currentActor()
        repository.deleteMembershipApplication(application, actorName, actorRole)
      } else {
        repository.deleteMembershipApplication(application)
      }
    }

  fun registerForEvent(
    event: SocietyEvent,
    student: StudentProfile,
    transactionRef: String,
    receiptUri: String,
  ) = executeAction("Successfully registered for ${event.title}!") {
    repository.registerStudentForEvent(event, student, transactionRef, receiptUri)
  }

  fun deleteRegistration(registration: EventRegistration, event: SocietyEvent?, isAdmin: Boolean) =
    executeAction("Event registration removed.") {
      if (isAdmin) {
        val (actorName, actorRole) = currentActor()
        repository.deleteEventRegistration(registration, event, actorName, actorRole)
      } else {
        repository.deleteEventRegistration(registration, event)
      }
    }

  fun uploadPaymentReceipt(
    payment: StudentPayment,
    paymentDate: String,
    transactionRef: String,
    receiptUri: String,
  ) = executeAction("Payment receipt submitted for verification.") {
    repository.submitStudentPaymentReceipt(payment, paymentDate, transactionRef, receiptUri)
    val matchingReg = myRegistrations.value.firstOrNull { it.eventId == payment.eventId }
    if (matchingReg != null) {
      repository.submitPaymentReceiptForRegistration(
        registrationId = matchingReg.id,
        paymentId = null,
        paymentDate = paymentDate,
        transactionRef = transactionRef,
        receiptUri = receiptUri,
      )
    }
  }

  fun deletePaymentRecord(payment: StudentPayment, isAdmin: Boolean) =
    executeAction("Payment record deleted.") {
      if (isAdmin) {
        val (actorName, actorRole) = currentActor()
        repository.deleteStudentPayment(payment, actorName, actorRole)
      } else {
        repository.deleteStudentPayment(payment)
      }
    }

  fun checkInEventByQr(qrCodeInput: String, student: StudentProfile) =
    executeAction("") {
      val cleanToken = qrCodeInput.trim()
        .removePrefix("YES-EVENT:")
        .removePrefix("yes-event:")
        .trim()
      val preMatched = publishedEvents.value.firstOrNull {
        it.qrCodeToken.equals(cleanToken, ignoreCase = true)
      }
      if (preMatched != null && myAttendance.value.any { it.eventId == preMatched.id && it.status == "PRESENT" }) {
        _snackbarMessage.value = "Attendance is already marked PRESENT for ${preMatched.title}!"
        return@executeAction
      }
      val matched = repository.studentCheckInWithEventQrToken(qrCodeInput, publishedEvents.value, student)
      _snackbarMessage.value = "Attendance marked PRESENT for ${matched.title}!"
    }

  fun resolveOfficerAsStudentProfile(admin: AdminAccount): StudentProfile =
    admin.toCabinetIdCardProfile(
      students = adminAllStudents.value,
      users = adminAllUsers.value,
      executiveBody = adminAllExecutive.value,
      fallbackPhotoUri = currentPhotoUri,
    )

  fun checkInAdminOrCabinetByQr(qrCodeInput: String, admin: AdminAccount) =
    executeAction("") {
      val allEvents = (adminAllEvents.value + publishedEvents.value).distinctBy { it.id }
      val cleanToken = qrCodeInput.trim()
        .removePrefix("YES-EVENT:")
        .removePrefix("yes-event:")
        .trim()
      val preMatched = allEvents.firstOrNull {
        it.qrCodeToken.equals(cleanToken, ignoreCase = true)
      }
      if (preMatched != null && myAttendance.value.any { it.eventId == preMatched.id && it.status == "PRESENT" }) {
        _snackbarMessage.value = "Attendance is already marked PRESENT for ${preMatched.title}!"
        return@executeAction
      }
      val officerProfile = resolveOfficerAsStudentProfile(admin)
      val matched = repository.studentCheckInWithEventQrToken(qrCodeInput, allEvents, officerProfile)
      _snackbarMessage.value = "Attendance marked PRESENT for ${matched.title}!"
    }

  fun registerAdminOrCabinetForEvent(
    event: SocietyEvent,
    admin: AdminAccount,
    transactionRef: String,
    receiptUri: String,
  ) = executeAction("Successfully registered for ${event.title}!") {
    val officerProfile = resolveOfficerAsStudentProfile(admin)
    repository.registerStudentForEvent(event, officerProfile, transactionRef, receiptUri)
  }

  fun submitFeedback(
    studentName: String,
    category: String,
    subject: String,
    message: String,
    relatedEventTitle: String,
  ) = executeAction("Your submission has been sent to YES Administration.") {
    repository.submitStudentFeedback(studentName, category, subject, message, relatedEventTitle)
  }

  fun deleteFeedbackItem(feedback: StudentFeedback, isAdmin: Boolean) =
    executeAction("Feedback record deleted.") {
      if (isAdmin) {
        val (actorName, actorRole) = currentActor()
        repository.deleteFeedback(feedback, actorName, actorRole)
      } else {
        repository.deleteFeedback(feedback)
      }
    }

  fun markNotificationRead(id: String) = executeAction("") {
    repository.markNotificationRead(id)
  }

  fun deleteNotification(id: String) = executeAction("Notification deleted.") {
    repository.deleteNotification(id)
  }

  fun verifyCertificate(code: String) {
    viewModelScope.launch {
      _isBusy.value = true
      try {
        val cert = repository.verifyCertificateByCode(code)
        _verifiedCertificateResult.value = cert
        _verificationChecked.value = true
      } finally {
        _isBusy.value = false
      }
    }
  }

  fun clearCertificateVerification() {
    _verifiedCertificateResult.value = null
    _verificationChecked.value = false
  }

  // --- Administrator Actions ---

  private fun currentActor(): Pair<String, String> {
    val admin = liveAdminAccount.value ?: (_roleState.value as? RoleSessionState.AdminSession)?.admin
    return (admin?.fullName ?: currentDisplayName.ifBlank { "Administrator" }) to (admin?.adminRole ?: "ADMIN")
  }

  fun saveStudentAdmin(
    existing: StudentProfile?,
    fullName: String,
    email: String,
    registrationNumber: String,
    degreeLevel: String,
    department: String,
    semester: String,
    phone: String,
    position: String,
    membershipStatus: String,
    membershipId: String,
    membershipValidUntil: String,
    photoUri: String,
    studentCardUri: String,
    profileLocked: Boolean,
    active: Boolean,
    idCardStatus: String = existing?.idCardStatus ?: "NOT_ISSUED",
  ) = executeAction("Student record saved.") {
    val (actorName, actorRole) = currentActor()
    repository.upsertStudentByAdmin(
      existing = existing,
      fullName = fullName,
      email = email,
      registrationNumber = registrationNumber,
      department = department,
      semester = semester,
      phone = phone,
      membershipStatus = membershipStatus,
      membershipId = membershipId,
      membershipValidUntil = membershipValidUntil,
      active = active,
      actorName = actorName,
      actorRole = actorRole,
      degreeLevel = degreeLevel,
      position = position,
      photoUri = photoUri,
      studentCardUri = studentCardUri,
      profileLocked = profileLocked,
      idCardStatus = idCardStatus,
    )
  }

  fun verifyOrRejectStudentAdmin(
    student: StudentProfile,
    verified: Boolean,
    positionOverride: String? = null,
    validUntil: String = "2027-06-30",
    notes: String = "",
  ) = executeAction(
    if (verified) "Student application marked VERIFIED & ID Card issued!" else "Student application marked REJECTED."
  ) {
    val (actorName, actorRole) = currentActor()
    repository.verifyOrRejectStudentByAdmin(
      student = student,
      verified = verified,
      positionOverride = positionOverride,
      validUntil = validUntil,
      notes = notes,
      actorName = actorName,
      actorRole = actorRole,
    )
  }

  fun setStudentIdCardIssuedAdmin(
    student: StudentProfile,
    issue: Boolean,
    positionOverride: String? = null,
    validUntil: String = "2027-06-30",
  ) = executeAction(
    if (issue) "Official YES ID Card issued to ${student.fullName}!" else "Official YES ID Card revoked for ${student.fullName}."
  ) {
    val (actorName, actorRole) = currentActor()
    repository.setStudentIdCardStatusByAdmin(
      student = student,
      issue = issue,
      positionOverride = positionOverride,
      validUntil = validUntil,
      actorName = actorName,
      actorRole = actorRole,
    )
  }

  fun toggleStudentProfileLockAdmin(student: StudentProfile, locked: Boolean) =
    executeAction(if (locked) "Student profile locked." else "Student profile unlocked for editing.") {
      val (actorName, actorRole) = currentActor()
      repository.setStudentProfileLockByAdmin(student, locked, actorName, actorRole)
    }

  fun deleteStudentAdmin(student: StudentProfile) = executeAction("Student record deleted.") {
    val (actorName, actorRole) = currentActor()
    repository.deleteStudentByAdmin(student.id.ifBlank { student.studentUid }, student.fullName, actorName, actorRole)
  }

  fun deleteWholeStudentDataAndBlockAdmin(student: StudentProfile, reason: String) =
    executeAction("All student data deleted & login access permanently blocked for ${student.fullName}.") {
      val (actorName, actorRole) = currentActor()
      repository.deleteWholeStudentDataAndBlockAccount(
        student = student,
        reason = reason,
        actorName = actorName,
        actorRole = actorRole,
      )
    }

  fun unblockStudentAccountAdmin(blocked: BlockedAccount) =
    executeAction("Account unblocked for ${blocked.fullName}.") {
      val (actorName, actorRole) = currentActor()
      repository.unblockStudentAccountByAdmin(blocked, actorName, actorRole)
    }

  fun reviewMembershipApp(
    application: MembershipApplication,
    approve: Boolean,
    validUntil: String,
    notes: String,
    assignedPosition: String? = null,
  ) = executeAction(if (approve) "Application VERIFIED & Official ID Card issued!" else "Application REJECTED.") {
    val (actorName, actorRole) = currentActor()
    repository.reviewMembershipApplication(
      application = application,
      approve = approve,
      validUntil = validUntil,
      reviewNotes = notes,
      actorName = actorName,
      actorRole = actorRole,
      assignedPosition = assignedPosition,
    )
  }

  fun saveExecutiveMember(
    existing: ExecutiveMember?,
    fullName: String,
    designation: String,
    department: String,
    tenure: String,
    email: String,
    photoUri: String,
    displayOrder: Long,
    published: Boolean,
  ) = executeAction("Executive office bearer saved.") {
    val (actorName, actorRole) = currentActor()
    repository.saveExecutiveMember(
      existing, fullName, designation, department, tenure, email, photoUri, displayOrder, published, actorName, actorRole,
    )
  }

  fun deleteExecutiveMember(member: ExecutiveMember) = executeAction("Office bearer deleted.") {
    val (actorName, actorRole) = currentActor()
    repository.deleteExecutiveMember(member.id, "${member.designation} - ${member.fullName}", actorName, actorRole)
  }

  fun saveEventAdmin(
    existing: SocietyEvent?,
    title: String,
    description: String,
    category: String,
    eventDate: String,
    eventTime: String,
    venue: String,
    posterUri: String,
    registrationDeadline: String,
    registrationFee: Double,
    maxParticipants: Long,
    upcoming: Boolean,
    published: Boolean,
  ) = executeAction("Event published & saved.") {
    val (actorName, actorRole) = currentActor()
    repository.saveEvent(
      existing, title, description, category, eventDate, eventTime, venue, posterUri,
      registrationDeadline, registrationFee, maxParticipants, upcoming, published, actorName, actorRole,
    )
  }

  fun deleteEventAdmin(event: SocietyEvent) = executeAction("Event deleted.") {
    val (actorName, actorRole) = currentActor()
    repository.deleteEvent(event.id, event.title, actorName, actorRole)
  }

  fun registerStudentForEventAdmin(
    event: SocietyEvent,
    student: StudentProfile,
    paymentStatus: String,
  ) = executeAction("${student.fullName} registered for ${event.title}.") {
    val (actorName, actorRole) = currentActor()
    repository.registerStudentForEventByAdmin(event, student, paymentStatus, actorName, actorRole)
  }

  fun updateRegistrationAdmin(
    registration: EventRegistration,
    registrationStatus: String,
    paymentStatus: String,
  ) = executeAction("Participant registration updated.") {
    val (actorName, actorRole) = currentActor()
    repository.updateRegistrationAndPaymentStatusAdmin(
      registration, registrationStatus, paymentStatus, actorName, actorRole,
    )
  }

  fun markAttendanceAdmin(
    event: SocietyEvent,
    studentUid: String,
    studentName: String,
    registrationNumber: String,
    membershipId: String,
    status: String,
    method: String,
  ) = executeAction("Attendance recorded: $studentName ($status)") {
    val (actorName, actorRole) = currentActor()
    repository.markAttendanceByAdmin(
      event, studentUid, studentName, registrationNumber, membershipId, status, method, actorName, actorRole,
    )
  }

  fun deleteAttendanceAdmin(record: AttendanceRecord) = executeAction("Attendance record deleted.") {
    val (actorName, actorRole) = currentActor()
    repository.deleteAttendanceRecord(record, actorName, actorRole)
  }

  fun saveAnnouncementAdmin(
    existing: Announcement?,
    title: String,
    message: String,
    category: String,
    targetGroup: String,
    priority: String,
    scheduledDate: String,
    published: Boolean,
  ) = executeAction("Announcement published.") {
    val (actorName, actorRole) = currentActor()
    repository.saveAnnouncement(
      existing, title, message, category, targetGroup, priority, scheduledDate, published, actorName, actorRole,
    )
  }

  fun deleteAnnouncementAdmin(ann: Announcement) = executeAction("Announcement deleted.") {
    val (actorName, actorRole) = currentActor()
    repository.deleteAnnouncement(ann.id, ann.title, actorName, actorRole)
  }

  fun verifyPaymentAdmin(payment: StudentPayment, approve: Boolean) =
    executeAction(if (approve) "Payment verified & added to Finance Ledger!" else "Payment rejected.") {
      val (actorName, actorRole) = currentActor()
      repository.verifyStudentPaymentByFinanceAdmin(payment, approve, actorName, actorRole)
    }

  fun recordFinanceEntry(
    title: String,
    type: String,
    category: String,
    amount: Double,
    transactionDate: String,
    referenceNumber: String,
    notes: String,
  ) = executeAction("$type recorded in Society Finance Ledger.") {
    val (actorName, actorRole) = currentActor()
    repository.recordFinanceTransaction(
      title, type, category, amount, transactionDate, referenceNumber, notes, actorName, actorRole,
    )
  }

  fun deleteFinanceEntry(tx: FinanceTransaction) = executeAction("Transaction deleted.") {
    val (actorName, actorRole) = currentActor()
    repository.deleteFinanceTransaction(tx.id, tx.title, actorName, actorRole)
  }

  fun savePaymentAccountAdmin(
    existing: PaymentAccount?,
    accountType: String,
    bankOrWalletName: String,
    accountTitle: String,
    accountNumber: String,
    ibanOrBranch: String,
    instructions: String,
    active: Boolean,
  ) = executeAction("Official payment account saved.") {
    val (actorName, actorRole) = currentActor()
    repository.savePaymentAccount(
      existing = existing,
      accountType = accountType,
      bankOrWalletName = bankOrWalletName,
      accountTitle = accountTitle,
      accountNumber = accountNumber,
      ibanOrBranch = ibanOrBranch,
      instructions = instructions,
      active = active,
      actorName = actorName,
      actorRole = actorRole,
    )
  }

  fun deletePaymentAccountAdmin(account: PaymentAccount) = executeAction("Payment account deleted.") {
    val (actorName, actorRole) = currentActor()
    repository.deletePaymentAccount(
      accountId = account.id,
      summary = "${account.bankOrWalletName} (${account.accountTitle})",
      actorName = actorName,
      actorRole = actorRole,
    )
  }

  fun saveGalleryPhotoAdmin(
    existing: GalleryItem?,
    title: String,
    albumCategory: String,
    caption: String,
    eventDate: String,
    imageUri: String,
    accentTheme: String,
    approved: Boolean,
  ) = executeAction("Gallery item saved.") {
    val (actorName, actorRole) = currentActor()
    repository.saveGalleryItem(
      existing, title, albumCategory, caption, eventDate, imageUri, accentTheme, approved, actorName, actorRole,
    )
  }

  fun deleteGalleryPhotoAdmin(item: GalleryItem) = executeAction("Gallery photograph deleted.") {
    val (actorName, actorRole) = currentActor()
    repository.deleteGalleryItem(item.id, item.title, actorName, actorRole)
  }

  fun issueCertificateAdmin(
    studentUid: String,
    studentName: String,
    registrationNumber: String,
    eventId: String,
    eventTitle: String,
    eventDate: String,
    certificateType: String,
    authorizedSignatory: String,
  ) = executeAction("Official YES Certificate issued!") {
    val (actorName, actorRole) = currentActor()
    repository.issueCertificate(
      studentUid, studentName, registrationNumber, eventId, eventTitle, eventDate,
      certificateType, authorizedSignatory, actorName, actorRole,
    )
  }

  fun deleteCertificateAdmin(cert: SocietyCertificate) = executeAction("Certificate deleted.") {
    val (actorName, actorRole) = currentActor()
    repository.deleteCertificate(cert, actorName, actorRole)
  }

  fun approveCertificate(cert: SocietyCertificate) = executeAction("Certificate APPROVED!") {
    val (actorName, actorRole) = currentActor()
    repository.approveCertificateByCabinet(cert, actorName, actorRole)
  }

  fun rejectCertificate(cert: SocietyCertificate, reason: String) = executeAction("Certificate REJECTED.") {
    val (actorName, actorRole) = currentActor()
    repository.rejectCertificateByCabinet(cert, reason, actorName, actorRole)
  }

  fun respondToFeedback(feedbackId: String, status: String, response: String) =
    executeAction("Feedback response updated.") {
      val (actorName, actorRole) = currentActor()
      repository.respondToFeedbackAdmin(feedbackId, status, response, actorName, actorRole)
    }

  fun assignAdminRole(
    targetUid: String,
    email: String,
    fullName: String,
    roleType: AdminRoleType,
    designation: String,
    permissions: List<String>,
    active: Boolean,
  ) = executeAction("Administrator role updated for $fullName.") {
    val (actorName, _) = currentActor()
    val existingAdmin = adminAllAdmins.value.firstOrNull { it.id == targetUid || it.uid == targetUid }
    repository.assignOrUpdateAdmin(
      targetUid = targetUid,
      email = email,
      fullName = fullName,
      roleType = roleType,
      designation = designation,
      customPermissions = permissions,
      active = active,
      existingCreatedAt = existingAdmin?.createdAt,
      actorName = actorName,
    )
  }

  fun revokeAdminRole(admin: AdminAccount) = executeAction("Administrator role deleted.") {
    val (actorName, _) = currentActor()
    repository.removeAdminRole(admin.uid.ifBlank { admin.id }, admin.fullName, actorName)
  }

  fun deleteAuditLogAdmin(log: AdminAuditLog) = executeAction("Activity log deleted.") {
    repository.deleteAuditLog(log.id)
  }

  val societyConfig: StateFlow<SocietyConfig> = repository.observeSocietyConfig()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), SocietyConfig())

  fun saveSocietyConfig(config: SocietyConfig) = executeAction("Portal settings updated.") {
    val (actorName, actorRole) = currentActor()
    repository.updateSocietyConfig(config, actorName, actorRole)
  }

  companion object {
    private const val TAG = "SocietyVM"

    fun factory(
      currentUserId: String,
      currentUserEmail: String,
      currentDisplayName: String,
      currentPhotoUri: String,
    ): ViewModelProvider.Factory = viewModelFactory {
      initializer {
        val app = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) {
          "APPLICATION_KEY missing from CreationExtras"
        }
        val databaseId = app.getString(R.string.firestore_database_id)
        val db = FirebaseFirestore.getInstance(databaseId)
        SocietyViewModel(
          repository = SocietyRepository(db),
          currentUserId = currentUserId,
          currentUserEmail = currentUserEmail,
          currentDisplayName = currentDisplayName,
          currentPhotoUri = currentPhotoUri,
        )
      }
    }
  }
}
