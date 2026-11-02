package com.example.data.repository

import android.content.Context
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
import com.example.data.model.SOCIETY_ID
import com.example.data.model.SocietyCertificate
import com.example.data.model.SocietyEvent
import com.example.data.model.StudentFeedback
import com.example.data.model.StudentPayment
import com.example.data.model.StudentProfile
import com.example.data.model.UserAccount
import com.example.data.model.SocietyConfig
import com.example.data.model.UserNotification
import com.example.data.model.isCabinetMember
import com.example.data.model.isVerifiedMember
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class SocietyRepository(
  private val db: FirebaseFirestore,
  private val auth: FirebaseAuth = Firebase.auth,
) {
  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    )
  )

  private fun requireUserId(): String =
    auth.currentUser?.uid
      ?: throw IllegalStateException("Authenticated Google session required before Firestore write.")

  private inline fun <reified T : Any> Query.observeSortedList(
    pathLabel: String,
    crossinline timestampSelector: (T) -> Timestamp?,
  ): Flow<List<T>> =
    this.snapshots()
      .map { snapshot ->
        snapshot.toObjects(T::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          .sortedByDescending { timestampSelector(it) ?: Timestamp(0, 0) }
      }
      .catch { error ->
        if (error is Exception) handleFirestoreError(error, OperationType.LIST, pathLabel)
        throw error
      }

  // --- Role & Identity Resolution ---

  suspend fun getUserAccount(uid: String): UserAccount? {
    val docRef = db.collection("users").document(uid)
    return try {
      docRef.get().await().toObject(UserAccount::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.GET, docRef.path)
      throw e
    }
  }

  suspend fun getAdminAccount(uid: String): AdminAccount? {
    val docRef = db.collection("admins").document(uid)
    return try {
      docRef.get().await().toObject(AdminAccount::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.GET, docRef.path)
      throw e
    }
  }

  suspend fun getStudentProfile(uid: String): StudentProfile? {
    val docRef = db.collection("students").document(uid)
    return try {
      docRef.get().await().toObject(StudentProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.GET, docRef.path)
      throw e
    }
  }

  fun observeStudentProfile(uid: String): Flow<StudentProfile?> {
    val docRef = db.collection("students").document(uid)
    return docRef.snapshots()
      .map { it.toObject(StudentProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
      .catch { error ->
        if (error is Exception) handleFirestoreError(error, OperationType.GET, docRef.path)
        throw error
      }
  }

  fun observeAdminAccount(uid: String): Flow<AdminAccount?> {
    val docRef = db.collection("admins").document(uid)
    return docRef.snapshots()
      .map { it.toObject(AdminAccount::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
      .catch { error ->
        if (error is Exception) handleFirestoreError(error, OperationType.GET, docRef.path)
        throw error
      }
  }

  suspend fun getBlockedAccount(uid: String): BlockedAccount? {
    val docRef = db.collection("blocked_accounts").document(uid)
    return try {
      val snap = docRef.get().await()
      if (snap.exists()) {
        snap.toObject(BlockedAccount::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
      } else {
        null
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.GET, docRef.path)
      null
    }
  }

  fun observeBlockedAccount(uid: String): Flow<BlockedAccount?> {
    val docRef = db.collection("blocked_accounts").document(uid)
    return docRef.snapshots()
      .map { snap ->
        if (snap.exists()) {
          snap.toObject(BlockedAccount::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } else {
          null
        }
      }
      .catch { error ->
        if (error is Exception) handleFirestoreError(error, OperationType.GET, docRef.path)
        throw error
      }
  }

  fun observeAllBlockedAccountsAdmin(): Flow<List<BlockedAccount>> =
    db.collection("blocked_accounts")
      .whereEqualTo("societyId", SOCIETY_ID)
      .observeSortedList("blocked_accounts") { it.createdAt }

  suspend fun bootstrapSuperAdminIfOwner(
    uid: String,
    email: String,
    fullName: String,
    photoUri: String,
  ): AdminAccount? {
    if (!email.equals(BOOTSTRAPPED_SUPER_ADMIN_EMAIL, ignoreCase = true)) return null
    val adminRef = db.collection("admins").document(uid)
    val userRef = db.collection("users").document(uid)
    val resolvedName = fullName.takeIf { it.isNotBlank() && !it.equals("President YES", ignoreCase = true) }
      ?: "Administrator YES"

    val existingAdmin = getAdminAccount(uid)
    if (existingAdmin != null) {
      val hasLegacyPresidentTitle = existingAdmin.adminRole == AdminRoleType.SUPER_ADMIN.key &&
        (existingAdmin.designation.contains("President", ignoreCase = true) ||
          existingAdmin.fullName.equals("President YES", ignoreCase = true))
      if (hasLegacyPresidentTitle) {
        val cleanedName = if (existingAdmin.fullName.equals("President YES", ignoreCase = true)) {
          resolvedName
        } else {
          existingAdmin.fullName
        }
        try {
          adminRef.update(
            mapOf(
              "designation" to "Administrator",
              "fullName" to cleanedName,
              "updatedAt" to FieldValue.serverTimestamp(),
            )
          ).await()
        } catch (e: CancellationException) {
          throw e
        } catch (e: Exception) {
          handleFirestoreError(e, OperationType.UPDATE, adminRef.path)
        }
        return existingAdmin.copy(
          designation = "Administrator",
          fullName = cleanedName,
        )
      }
      return existingAdmin
    }

    val adminAccount = AdminAccount(
      uid = uid,
      email = email,
      fullName = resolvedName,
      adminRole = AdminRoleType.SUPER_ADMIN.key,
      designation = "Administrator",
      permissions = AdminRoleType.SUPER_ADMIN.defaultPermissions,
      active = true,
      assignedByUid = uid,
      societyId = SOCIETY_ID,
    )
    val userAccount = UserAccount(
      userId = uid,
      email = email,
      fullName = resolvedName,
      photoUri = photoUri,
      accountType = "ADMIN",
      active = true,
      societyId = SOCIETY_ID,
    )
    return try {
      adminRef.set(adminAccount).await()
      userRef.set(userAccount).await()
      logAdminActivity(
        actorName = adminAccount.fullName,
        actorRole = adminAccount.adminRole,
        action = "BOOTSTRAPPED_SUPER_ADMIN",
        targetSummary = "Initialized System Administrator account for $email",
      )
      getAdminAccount(uid) ?: adminAccount
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, adminRef.path)
      throw e
    }
  }

  suspend fun registerStudentOnboarding(
    fullName: String,
    email: String,
    registrationNumber: String,
    department: String,
    semester: String,
    phone: String,
    photoUri: String,
    degreeLevel: String = "BSc (Hons) Agriculture",
    studentCardUri: String = "",
  ) {
    val uid = requireUserId()
    val userRef = db.collection("users").document(uid)
    val studentRef = db.collection("students").document(uid)
    val notifRef = db.collection("notifications").document("welcome_$uid")

    val userAccount = UserAccount(
      userId = uid,
      email = email,
      fullName = fullName.trim(),
      photoUri = photoUri,
      accountType = "STUDENT",
      active = true,
      societyId = SOCIETY_ID,
    )
    val profile = StudentProfile(
      studentUid = uid,
      fullName = fullName.trim(),
      email = email,
      registrationNumber = registrationNumber.trim(),
      degreeLevel = degreeLevel.trim().ifBlank { "BSc (Hons) Agriculture" },
      department = department.trim(),
      semester = semester.trim(),
      phone = phone.trim(),
      photoUri = photoUri,
      studentCardUri = studentCardUri,
      position = "Member",
      profileLocked = false,
      membershipStatus = "NONE",
      idCardStatus = "NOT_ISSUED",
      membershipId = "",
      membershipValidUntil = "",
      active = true,
      societyId = SOCIETY_ID,
    )
    val welcomeNotif = UserNotification(
      recipientUid = uid,
      title = "Welcome to Young Entomologist Society!",
      body = "Department of Entomology, UAF. Apply for official YES membership to receive your verified digital ID card.",
      type = "ANNOUNCEMENT",
      read = false,
      societyId = SOCIETY_ID,
    )

    try {
      userRef.set(userAccount).await()
      studentRef.set(profile).await()
      notifRef.set(welcomeNotif).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, studentRef.path)
      throw e
    }
  }

  suspend fun updateOwnStudentProfile(
    fullName: String,
    registrationNumber: String,
    department: String,
    semester: String,
    phone: String,
    photoUri: String,
    degreeLevel: String = "BSc (Hons) Agriculture",
    studentCardUri: String = "",
  ) {
    val uid = requireUserId()
    val studentRef = db.collection("students").document(uid)
    val userRef = db.collection("users").document(uid)
    try {
      studentRef.update(
        mapOf(
          "fullName" to fullName.trim(),
          "registrationNumber" to registrationNumber.trim(),
          "degreeLevel" to degreeLevel.trim().ifBlank { "BSc (Hons) Agriculture" },
          "department" to department.trim(),
          "semester" to semester.trim(),
          "phone" to phone.trim(),
          "photoUri" to photoUri,
          "studentCardUri" to studentCardUri,
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()
      userRef.update(
        mapOf(
          "fullName" to fullName.trim(),
          "photoUri" to photoUri,
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, studentRef.path)
      throw e
    }
  }

  // --- Admin Student Management ---

  fun observeAllStudentsAdmin(): Flow<List<StudentProfile>> =
    db.collection("students")
      .whereEqualTo("societyId", SOCIETY_ID)
      .observeSortedList("students") { it.createdAt }

  fun observeAllUsersAdmin(): Flow<List<UserAccount>> =
    db.collection("users")
      .whereEqualTo("societyId", SOCIETY_ID)
      .observeSortedList("users") { it.createdAt }

  suspend fun upsertStudentByAdmin(
    existing: StudentProfile?,
    fullName: String,
    email: String,
    registrationNumber: String,
    department: String,
    semester: String,
    phone: String,
    membershipStatus: String,
    membershipId: String,
    membershipValidUntil: String,
    active: Boolean,
    actorName: String,
    actorRole: String,
    degreeLevel: String = existing?.degreeLevel ?: "BSc (Hons) Agriculture",
    position: String = existing?.position ?: "Member",
    photoUri: String = existing?.photoUri ?: "",
    studentCardUri: String = existing?.studentCardUri ?: "",
    profileLocked: Boolean = existing?.profileLocked ?: false,
    idCardStatus: String = existing?.idCardStatus ?: "NOT_ISSUED",
  ) {
    requireUserId()
    val docId = existing?.id?.takeIf { it.isNotBlank() } ?: "stu_${UUID.randomUUID().toString().take(8)}"
    val docRef = db.collection("students").document(docId)
    val isVerifiedTarget = membershipStatus == "VERIFIED" || membershipStatus == "ACTIVE"
    val wasVerifiedBefore = existing?.isVerifiedMember() == true
    val newlyVerified = isVerifiedTarget && !wasVerifiedBefore
    val finalMembershipId = if (isVerifiedTarget) {
      membershipId.trim().ifBlank { "YES-UAF-2026-${(1000..9999).random()}" }
    } else if (membershipStatus == "REJECTED") {
      ""
    } else {
      membershipId.trim()
    }
    val finalValidUntil = if (isVerifiedTarget) {
      membershipValidUntil.trim().ifBlank { "2027-06-30" }
    } else if (membershipStatus == "REJECTED") {
      ""
    } else {
      membershipValidUntil.trim()
    }
    val finalPosition = if (isVerifiedTarget) {
      position.trim().takeIf { it.isNotBlank() && it != "Member" && it != "Active Member" } ?: "General Member"
    } else {
      position.trim().ifBlank { "Member" }
    }
    val finalIdCardStatus = when {
      membershipStatus == "REJECTED" -> if (existing?.membershipId?.isNotBlank() == true) "REVOKED" else "NOT_ISSUED"
      newlyVerified && idCardStatus != "REVOKED" -> "ISSUED"
      isVerifiedTarget && idCardStatus.isBlank() -> "ISSUED"
      idCardStatus.isBlank() -> "NOT_ISSUED"
      else -> idCardStatus
    }
    val finalLocked = when {
      newlyVerified -> true
      membershipStatus == "REJECTED" -> false
      else -> profileLocked
    }

    try {
      if (existing != null && existing.createdAt != null) {
        val updated = existing.copy(
          fullName = fullName.trim(),
          email = email.trim(),
          registrationNumber = registrationNumber.trim(),
          degreeLevel = degreeLevel.trim().ifBlank { "BSc (Hons) Agriculture" },
          department = department.trim(),
          semester = semester.trim(),
          phone = phone.trim(),
          photoUri = photoUri,
          studentCardUri = studentCardUri,
          position = finalPosition,
          profileLocked = finalLocked,
          membershipStatus = membershipStatus,
          idCardStatus = finalIdCardStatus,
          membershipId = finalMembershipId,
          membershipValidUntil = finalValidUntil,
          active = active,
          updatedAt = null,
        )
        docRef.set(updated).await()
      } else {
        val created = StudentProfile(
          studentUid = docId,
          fullName = fullName.trim(),
          email = email.trim(),
          registrationNumber = registrationNumber.trim(),
          degreeLevel = degreeLevel.trim().ifBlank { "BSc (Hons) Agriculture" },
          department = department.trim(),
          semester = semester.trim(),
          phone = phone.trim(),
          photoUri = photoUri,
          studentCardUri = studentCardUri,
          position = finalPosition,
          profileLocked = finalLocked,
          membershipStatus = membershipStatus,
          idCardStatus = finalIdCardStatus,
          membershipId = finalMembershipId,
          membershipValidUntil = finalValidUntil,
          active = active,
          societyId = SOCIETY_ID,
        )
        docRef.set(created).await()
      }
      if (newlyVerified && existing != null) {
        sendPersonalNotification(
          recipientUid = existing.studentUid.ifBlank { docId },
          title = "YES Application Verified & ID Card Issued",
          body = "Your student profile has been marked VERIFIED and your official YES ID Card ($finalMembershipId) is now issued.",
          type = "MEMBERSHIP",
        )
      }
      logAdminActivity(actorName, actorRole, "SAVED_STUDENT", "${fullName.trim()} ($registrationNumber) [$membershipStatus]")
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      throw e
    }
  }

  suspend fun setStudentProfileLockByAdmin(
    student: StudentProfile,
    locked: Boolean,
    actorName: String,
    actorRole: String,
  ) {
    requireUserId()
    val docId = student.id.ifBlank { student.studentUid }
    val docRef = db.collection("students").document(docId)
    try {
      docRef.update(
        mapOf(
          "profileLocked" to locked,
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()
      sendPersonalNotification(
        recipientUid = student.studentUid.ifBlank { docId },
        title = if (locked) "Profile Locked by Administrator" else "Profile Unlocked for Editing",
        body = if (locked) {
          "Your approved YES profile has been locked by Administration."
        } else {
          "An Administrator has unlocked your profile so you can update your academic details."
        },
        type = "MEMBERSHIP",
      )
      logAdminActivity(
        actorName = actorName,
        actorRole = actorRole,
        action = if (locked) "LOCKED_STUDENT_PROFILE" else "UNLOCKED_STUDENT_PROFILE",
        targetSummary = "${student.fullName} (${student.registrationNumber})",
      )
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  suspend fun verifyOrRejectStudentByAdmin(
    student: StudentProfile,
    verified: Boolean,
    positionOverride: String? = null,
    validUntil: String = "2027-06-30",
    notes: String = "",
    actorName: String,
    actorRole: String,
  ) {
    val reviewerUid = requireUserId()
    val docId = student.id.ifBlank { student.studentUid }
    val targetUid = student.studentUid.ifBlank { docId }
    val studentRef = db.collection("students").document(docId)
    val generatedId = if (verified) {
      student.membershipId.ifBlank { "YES-UAF-2026-${(1000..9999).random()}" }
    } else {
      ""
    }
    val nextPosition = if (verified) {
      positionOverride?.trim()?.takeIf { it.isNotBlank() }
        ?: student.position.takeIf { student.isCabinetMember() }
        ?: "General Member"
    } else {
      student.position.ifBlank { "Member" }
    }
    val nextIdCardStatus = if (verified) {
      "ISSUED"
    } else {
      if (student.membershipId.isNotBlank() || student.idCardStatus == "ISSUED") "REVOKED" else "NOT_ISSUED"
    }

    try {
      if (student.createdAt != null) {
        studentRef.set(
          student.copy(
            membershipStatus = if (verified) "VERIFIED" else "REJECTED",
            idCardStatus = nextIdCardStatus,
            membershipId = generatedId,
            membershipValidUntil = if (verified) validUntil.ifBlank { student.membershipValidUntil.ifBlank { "2027-06-30" } } else "",
            position = nextPosition,
            profileLocked = verified,
            updatedAt = null,
          )
        ).await()
      } else {
        studentRef.update(
          mapOf(
            "membershipStatus" to if (verified) "VERIFIED" else "REJECTED",
            "idCardStatus" to nextIdCardStatus,
            "membershipId" to generatedId,
            "membershipValidUntil" to if (verified) validUntil.ifBlank { "2027-06-30" } else "",
            "position" to nextPosition,
            "profileLocked" to verified,
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        ).await()
      }

      // Sync any pending membership applications for this student
      val pendingApps = db.collection("membership_applications")
        .whereEqualTo("societyId", SOCIETY_ID)
        .whereEqualTo("studentUid", targetUid)
        .get()
        .await()
      for (appDoc in pendingApps.documents) {
        val currentStatus = appDoc.getString("status") ?: ""
        if (currentStatus == "PENDING" || !verified) {
          appDoc.reference.update(
            mapOf(
              "status" to if (verified) "VERIFIED" else "REJECTED",
              "assignedMembershipId" to generatedId,
              "reviewedByUid" to reviewerUid,
              "reviewNotes" to notes.trim().ifBlank {
                if (verified) "Verified by Administrator" else "Application rejected by Administrator"
              },
              "updatedAt" to FieldValue.serverTimestamp(),
            )
          ).await()
        }
      }

      sendPersonalNotification(
        recipientUid = targetUid,
        title = if (verified) "Application Verified & ID Card Issued" else "Student Application Rejected",
        body = if (verified) {
          "Your student application is VERIFIED and your official YES ID Card ($generatedId) has been issued."
        } else {
          "Your student application was marked REJECTED. ${notes.trim()}".trim()
        },
        type = "MEMBERSHIP",
      )
      logAdminActivity(
        actorName = actorName,
        actorRole = actorRole,
        action = if (verified) "VERIFIED_STUDENT_APPLICATION" else "REJECTED_STUDENT_APPLICATION",
        targetSummary = "${student.fullName} (${student.registrationNumber})",
      )
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, studentRef.path)
      throw e
    }
  }

  suspend fun setStudentIdCardStatusByAdmin(
    student: StudentProfile,
    issue: Boolean,
    positionOverride: String? = null,
    validUntil: String = "2027-06-30",
    actorName: String,
    actorRole: String,
  ) {
    requireUserId()
    val docId = student.id.ifBlank { student.studentUid }
    val targetUid = student.studentUid.ifBlank { docId }
    val studentRef = db.collection("students").document(docId)
    val cardId = if (issue) {
      val prefix = if ((positionOverride ?: student.position).let { pos ->
          val norm = pos.trim().lowercase()
          norm.isNotEmpty() && norm !in setOf("member", "active member", "general member", "verified member", "student", "none")
        }
      ) {
        "YES-CAB-2026"
      } else {
        "YES-UAF-2026"
      }
      student.membershipId.ifBlank { "$prefix-${(1000..9999).random()}" }
    } else {
      student.membershipId
    }
    val nextPosition = if (issue) {
      positionOverride?.trim()?.takeIf { it.isNotBlank() }
        ?: student.position.takeIf { student.isCabinetMember() }
        ?: "General Member"
    } else {
      student.position
    }
    val nextMembershipStatus = if (issue && !student.isVerifiedMember()) "VERIFIED" else student.membershipStatus
    val nextIdCardStatus = if (issue) "ISSUED" else "REVOKED"

    try {
      studentRef.update(
        mapOf(
          "idCardStatus" to nextIdCardStatus,
          "membershipId" to cardId,
          "membershipValidUntil" to if (issue) validUntil.ifBlank { student.membershipValidUntil.ifBlank { "2027-06-30" } } else student.membershipValidUntil,
          "membershipStatus" to nextMembershipStatus,
          "position" to nextPosition,
          "profileLocked" to if (issue) true else student.profileLocked,
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()

      sendPersonalNotification(
        recipientUid = targetUid,
        title = if (issue) "Official YES ID Card Issued" else "Official YES ID Card Revoked",
        body = if (issue) {
          "Your official Young Entomologist Society ID Card ($cardId • $nextPosition) is now active in the ID Card module."
        } else {
          "Your Young Entomologist Society ID Card has been revoked by Administration."
        },
        type = "MEMBERSHIP",
      )
      logAdminActivity(
        actorName = actorName,
        actorRole = actorRole,
        action = if (issue) "ISSUED_ID_CARD" else "REVOKED_ID_CARD",
        targetSummary = "${student.fullName} (${student.registrationNumber}) -> $nextPosition",
      )
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, studentRef.path)
      throw e
    }
  }

  suspend fun deleteStudentByAdmin(studentId: String, studentName: String, actorName: String, actorRole: String) {
    requireUserId()
    val docRef = db.collection("students").document(studentId)
    try {
      docRef.delete().await()
      logAdminActivity(actorName, actorRole, "DELETED_STUDENT", studentName)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  suspend fun deleteWholeStudentDataAndBlockAccount(
    student: StudentProfile,
    reason: String,
    actorName: String,
    actorRole: String,
  ) {
    val callerUid = requireUserId()
    if (student.email.equals(BOOTSTRAPPED_SUPER_ADMIN_EMAIL, ignoreCase = true)) {
      throw IllegalArgumentException("Super Administrator owner account cannot be blocked or purged.")
    }
    val docId = student.id.ifBlank { student.studentUid }
    val targetUid = student.studentUid.ifBlank { docId }
    val blockRef = db.collection("blocked_accounts").document(targetUid)

    try {
      // 1. Delete membership applications
      val appsSnap = db.collection("membership_applications")
        .whereEqualTo("societyId", SOCIETY_ID)
        .whereEqualTo("studentUid", targetUid)
        .get()
        .await()
      for (doc in appsSnap.documents) {
        doc.reference.delete().await()
      }

      // 2. Delete event registrations & decrement event registeredCount
      val regsSnap = db.collection("event_registrations")
        .whereEqualTo("societyId", SOCIETY_ID)
        .whereEqualTo("studentUid", targetUid)
        .get()
        .await()
      for (doc in regsSnap.documents) {
        val eventId = doc.getString("eventId") ?: ""
        doc.reference.delete().await()
        if (eventId.isNotBlank()) {
          runCatching {
            val evRef = db.collection("events").document(eventId)
            val evSnap = evRef.get().await()
            val currentCount = evSnap.getLong("registeredCount") ?: 0L
            if (evSnap.exists() && currentCount > 0L) {
              evRef.update(
                mapOf(
                  "registeredCount" to (currentCount - 1L),
                  "updatedAt" to FieldValue.serverTimestamp(),
                )
              ).await()
            }
          }
        }
      }

      // 3. Delete attendance records
      val attSnap = db.collection("attendance_records")
        .whereEqualTo("societyId", SOCIETY_ID)
        .whereEqualTo("studentUid", targetUid)
        .get()
        .await()
      for (doc in attSnap.documents) {
        doc.reference.delete().await()
      }

      // 4. Delete student payment records
      val paySnap = db.collection("payments")
        .whereEqualTo("societyId", SOCIETY_ID)
        .whereEqualTo("studentUid", targetUid)
        .get()
        .await()
      for (doc in paySnap.documents) {
        doc.reference.delete().await()
      }

      // 5. Delete issued certificates
      val certSnap = db.collection("certificates")
        .whereEqualTo("societyId", SOCIETY_ID)
        .whereEqualTo("studentUid", targetUid)
        .get()
        .await()
      for (doc in certSnap.documents) {
        doc.reference.delete().await()
      }

      // 6. Delete student feedback
      val fbSnap = db.collection("feedback")
        .whereEqualTo("societyId", SOCIETY_ID)
        .whereEqualTo("studentUid", targetUid)
        .get()
        .await()
      for (doc in fbSnap.documents) {
        doc.reference.delete().await()
      }

      // 7. Delete personal notifications
      val notifSnap = db.collection("notifications")
        .whereEqualTo("societyId", SOCIETY_ID)
        .whereEqualTo("recipientUid", targetUid)
        .get()
        .await()
      for (doc in notifSnap.documents) {
        doc.reference.delete().await()
      }

      // 8. Delete student profile(s)
      db.collection("students").document(docId).delete().await()
      if (targetUid != docId) {
        runCatching { db.collection("students").document(targetUid).delete().await() }
      }

      // 9. Delete user account record if STUDENT
      runCatching {
        val userRef = db.collection("users").document(targetUid)
        val userSnap = userRef.get().await()
        if (userSnap.exists() && userSnap.getString("accountType") == "STUDENT") {
          userRef.delete().await()
        }
      }

      // 10. Block future login & registration access
      val blockPayload = BlockedAccount(
        userId = targetUid,
        email = student.email.trim().ifBlank { "blocked@uaf.edu.pk" },
        fullName = student.fullName.trim().ifBlank { "Blocked Student" },
        registrationNumber = student.registrationNumber.trim(),
        reason = reason.trim().ifBlank {
          "Account access permanently blocked and student data purged by Administrator."
        },
        blockedByUid = callerUid,
        blockedByName = actorName.trim().ifBlank { "Administrator" },
        societyId = SOCIETY_ID,
      )
      blockRef.set(blockPayload).await()

      logAdminActivity(
        actorName = actorName,
        actorRole = actorRole,
        action = "PURGED_AND_BLOCKED_STUDENT",
        targetSummary = "${student.fullName} (${student.registrationNumber} • ${student.email})",
      )
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, blockRef.path)
      throw e
    }
  }

  suspend fun unblockStudentAccountByAdmin(
    blocked: BlockedAccount,
    actorName: String,
    actorRole: String,
  ) {
    requireUserId()
    val docId = blocked.id.ifBlank { blocked.userId }
    val docRef = db.collection("blocked_accounts").document(docId)
    try {
      docRef.delete().await()
      logAdminActivity(
        actorName = actorName,
        actorRole = actorRole,
        action = "UNBLOCKED_STUDENT_ACCOUNT",
        targetSummary = "${blocked.fullName} (${blocked.email})",
      )
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  // --- Multiple Administrator Management (Super Admin) ---

  fun observeAllAdmins(): Flow<List<AdminAccount>> =
    db.collection("admins")
      .whereEqualTo("societyId", SOCIETY_ID)
      .observeSortedList("admins") { it.createdAt }

  suspend fun assignOrUpdateAdmin(
    targetUid: String,
    email: String,
    fullName: String,
    roleType: AdminRoleType,
    designation: String,
    customPermissions: List<String>,
    active: Boolean,
    existingCreatedAt: Timestamp?,
    actorName: String,
  ) {
    val callerUid = requireUserId()
    val adminRef = db.collection("admins").document(targetUid)
    val userRef = db.collection("users").document(targetUid)
    val finalDesignation = designation.trim().ifBlank { roleType.defaultDesignation }
    try {
      val payload = AdminAccount(
        uid = targetUid,
        email = email.trim(),
        fullName = fullName.trim(),
        adminRole = roleType.key,
        designation = finalDesignation,
        permissions = customPermissions.take(15),
        active = active,
        assignedByUid = callerUid,
        societyId = SOCIETY_ID,
        createdAt = existingCreatedAt,
        updatedAt = null,
      )
      adminRef.set(payload).await()
      // Update user accountType if user doc exists
      val existingUser = getUserAccount(targetUid)
      if (existingUser != null && existingUser.createdAt != null) {
        userRef.set(
          existingUser.copy(
            accountType = if (active) "ADMIN" else "STUDENT",
            updatedAt = null,
          )
        ).await()
      }
      // Sync Cabinet Position & Cabinet ID Card onto student profile if registered
      runCatching {
        val existingStudent = getStudentProfile(targetUid)
        if (existingStudent != null && existingStudent.createdAt != null) {
          val studentDocId = existingStudent.id.ifBlank { targetUid }
          val nextPos = if (active) finalDesignation else "General Member"
          val nextCardId = if (active) {
            existingStudent.membershipId.ifBlank { "YES-CAB-2026-${(1000..9999).random()}" }
          } else {
            existingStudent.membershipId
          }
          db.collection("students").document(studentDocId).update(
            mapOf(
              "position" to nextPos,
              "membershipStatus" to if (active && !existingStudent.isVerifiedMember()) "VERIFIED" else existingStudent.membershipStatus,
              "idCardStatus" to if (active) "ISSUED" else existingStudent.idCardStatus,
              "membershipId" to nextCardId,
              "membershipValidUntil" to existingStudent.membershipValidUntil.ifBlank { "2027-06-30" },
              "updatedAt" to FieldValue.serverTimestamp(),
            )
          ).await()
        }
      }
      logAdminActivity(actorName, "SUPER_ADMIN", "ASSIGNED_ADMIN_ROLE", "${fullName.trim()} -> ${roleType.displayTitle}")
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, adminRef.path)
      throw e
    }
  }

  suspend fun removeAdminRole(targetUid: String, fullName: String, actorName: String) {
    requireUserId()
    val adminRef = db.collection("admins").document(targetUid)
    val userRef = db.collection("users").document(targetUid)
    try {
      adminRef.delete().await()
      val existingUser = getUserAccount(targetUid)
      if (existingUser != null && existingUser.createdAt != null) {
        userRef.set(existingUser.copy(accountType = "STUDENT", updatedAt = null)).await()
      }
      runCatching {
        val existingStudent = getStudentProfile(targetUid)
        if (existingStudent != null && existingStudent.createdAt != null) {
          val studentDocId = existingStudent.id.ifBlank { targetUid }
          db.collection("students").document(studentDocId).update(
            mapOf(
              "position" to "General Member",
              "updatedAt" to FieldValue.serverTimestamp(),
            )
          ).await()
        }
      }
      logAdminActivity(actorName, "SUPER_ADMIN", "REMOVED_ADMIN_ROLE", fullName)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, adminRef.path)
      throw e
    }
  }

  // --- Membership Applications ---

  fun observeStudentMembershipApplications(studentUid: String): Flow<List<MembershipApplication>> =
    db.collection("membership_applications")
      .whereEqualTo("studentUid", studentUid)
      .observeSortedList("membership_applications") { it.createdAt }

  fun observeAllMembershipApplicationsAdmin(): Flow<List<MembershipApplication>> =
    db.collection("membership_applications")
      .whereEqualTo("societyId", SOCIETY_ID)
      .observeSortedList("membership_applications") { it.createdAt }

  suspend fun submitMembershipApplication(
    profile: StudentProfile,
    motivation: String,
  ): String {
    val uid = requireUserId()
    val appId = "app_${UUID.randomUUID().toString().take(10)}"
    val appRef = db.collection("membership_applications").document(appId)
    val studentRef = db.collection("students").document(uid)
    val application = MembershipApplication(
      studentUid = uid,
      studentName = profile.fullName,
      studentEmail = profile.email,
      registrationNumber = profile.registrationNumber,
      degreeLevel = profile.degreeLevel,
      department = profile.department,
      semester = profile.semester,
      phone = profile.phone,
      photoUri = profile.photoUri,
      studentCardUri = profile.studentCardUri,
      motivation = motivation.trim(),
      status = "PENDING",
      assignedMembershipId = "",
      reviewedByUid = "",
      reviewNotes = "",
      societyId = SOCIETY_ID,
    )
    return try {
      appRef.set(application).await()
      if (profile.membershipStatus == "NONE" || profile.membershipStatus == "REJECTED") {
        studentRef.update(
          mapOf(
            "membershipStatus" to "PENDING",
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        ).await()
      }
      appId
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, appRef.path)
      throw e
    }
  }

  suspend fun reviewMembershipApplication(
    application: MembershipApplication,
    approve: Boolean,
    validUntil: String,
    reviewNotes: String,
    actorName: String,
    actorRole: String,
    assignedPosition: String? = null,
  ) {
    val reviewerUid = requireUserId()
    val appRef = db.collection("membership_applications").document(application.id)
    val studentRef = db.collection("students").document(application.studentUid)
    val studentSnap = getStudentProfile(application.studentUid)
    val generatedMemberId = if (approve) {
      studentSnap?.membershipId?.takeIf { it.isNotBlank() }
        ?: "YES-UAF-2026-${(1000..9999).random()}"
    } else {
      ""
    }
    try {
      appRef.update(
        mapOf(
          "status" to if (approve) "VERIFIED" else "REJECTED",
          "assignedMembershipId" to generatedMemberId,
          "reviewedByUid" to reviewerUid,
          "reviewNotes" to reviewNotes.trim(),
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()

      if (studentSnap != null && studentSnap.createdAt != null) {
        val verifiedPosition = if (approve) {
          assignedPosition?.trim()?.takeIf { it.isNotBlank() }
            ?: studentSnap.position.takeIf { studentSnap.isCabinetMember() }
            ?: "General Member"
        } else {
          studentSnap.position
        }
        val nextIdCardStatus = if (approve) {
          "ISSUED"
        } else {
          if (studentSnap.membershipId.isNotBlank() || studentSnap.idCardStatus == "ISSUED") "REVOKED" else "NOT_ISSUED"
        }
        studentRef.set(
          studentSnap.copy(
            membershipStatus = if (approve) "VERIFIED" else "REJECTED",
            idCardStatus = nextIdCardStatus,
            membershipId = if (approve) generatedMemberId else "",
            membershipValidUntil = if (approve) validUntil.ifBlank { "2027-06-30" } else "",
            position = verifiedPosition,
            profileLocked = approve,
            updatedAt = null,
          )
        ).await()
      }

      sendPersonalNotification(
        recipientUid = application.studentUid,
        title = if (approve) "YES Application Verified & ID Card Issued!" else "Membership Application Rejected",
        body = if (approve) {
          "Congratulations! Your application is VERIFIED, your official YES ID Card ($generatedMemberId) has been issued, and your profile is locked."
        } else {
          "Your membership application was marked REJECTED: ${reviewNotes.ifBlank { "Contact General Secretary for details." }}"
        },
        type = "MEMBERSHIP",
      )
      logAdminActivity(
        actorName,
        actorRole,
        if (approve) "VERIFIED_MEMBERSHIP_APPLICATION" else "REJECTED_MEMBERSHIP_APPLICATION",
        "${application.studentName} ($generatedMemberId)",
      )
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, appRef.path)
      throw e
    }
  }

  suspend fun deleteMembershipApplication(
    application: MembershipApplication,
    actorName: String? = null,
    actorRole: String? = null,
  ) {
    requireUserId()
    val docRef = db.collection("membership_applications").document(application.id)
    try {
      docRef.delete().await()
      if (!actorName.isNullOrBlank()) {
        logAdminActivity(actorName, actorRole ?: "ADMIN", "DELETED_MEMBERSHIP_APPLICATION", "${application.studentName} (${application.registrationNumber})")
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  // --- Executive Body ---

  fun observePublishedExecutiveBody(): Flow<List<ExecutiveMember>> =
    db.collection("executive_body")
      .whereEqualTo("published", true)
      .snapshots()
      .map { snap ->
        snap.toObjects(ExecutiveMember::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          .sortedBy { it.displayOrder }
      }
      .catch { error ->
        if (error is Exception) handleFirestoreError(error, OperationType.LIST, "executive_body")
        throw error
      }

  fun observeAllExecutiveBodyAdmin(): Flow<List<ExecutiveMember>> =
    db.collection("executive_body")
      .whereEqualTo("societyId", SOCIETY_ID)
      .snapshots()
      .map { snap ->
        snap.toObjects(ExecutiveMember::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          .sortedBy { it.displayOrder }
      }
      .catch { error ->
        if (error is Exception) handleFirestoreError(error, OperationType.LIST, "executive_body")
        throw error
      }

  suspend fun saveExecutiveMember(
    existing: ExecutiveMember?,
    fullName: String,
    designation: String,
    department: String,
    tenure: String,
    email: String,
    photoUri: String,
    displayOrder: Long,
    published: Boolean,
    actorName: String,
    actorRole: String,
  ) {
    requireUserId()
    val docId = existing?.id?.takeIf { it.isNotBlank() } ?: "exec_${UUID.randomUUID().toString().take(8)}"
    val docRef = db.collection("executive_body").document(docId)
    try {
      val payload = ExecutiveMember(
        fullName = fullName.trim(),
        designation = designation.trim(),
        department = department.trim(),
        tenure = tenure.trim(),
        email = email.trim(),
        photoUri = photoUri,
        displayOrder = displayOrder,
        published = published,
        societyId = SOCIETY_ID,
        createdAt = existing?.createdAt,
        updatedAt = null,
      )
      docRef.set(payload).await()
      if (email.trim().isNotEmpty() && published) {
        runCatching {
          val matchingStudents = db.collection("students")
            .whereEqualTo("societyId", SOCIETY_ID)
            .whereEqualTo("email", email.trim())
            .get()
            .await()
          for (stuDoc in matchingStudents.documents) {
            val existingCardId = stuDoc.getString("membershipId").orEmpty()
            stuDoc.reference.update(
              mapOf(
                "position" to designation.trim(),
                "membershipStatus" to "VERIFIED",
                "idCardStatus" to "ISSUED",
                "membershipId" to existingCardId.ifBlank { "YES-CAB-2026-${(1000..9999).random()}" },
                "membershipValidUntil" to (stuDoc.getString("membershipValidUntil")?.takeIf { it.isNotBlank() } ?: "2027-06-30"),
                "updatedAt" to FieldValue.serverTimestamp(),
              )
            ).await()
          }
        }
      }
      logAdminActivity(actorName, actorRole, "SAVED_EXECUTIVE_MEMBER", "$designation - ${fullName.trim()}")
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      throw e
    }
  }

  suspend fun deleteExecutiveMember(id: String, summary: String, actorName: String, actorRole: String) {
    requireUserId()
    val docRef = db.collection("executive_body").document(id)
    try {
      docRef.delete().await()
      logAdminActivity(actorName, actorRole, "REMOVED_EXECUTIVE_MEMBER", summary)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  // --- Events & Registrations ---

  fun observePublishedEvents(): Flow<List<SocietyEvent>> =
    db.collection("events")
      .whereEqualTo("published", true)
      .observeSortedList("events") { it.createdAt }

  fun observeAllEventsAdmin(): Flow<List<SocietyEvent>> =
    db.collection("events")
      .whereEqualTo("societyId", SOCIETY_ID)
      .observeSortedList("events") { it.createdAt }

  suspend fun saveEvent(
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
    actorName: String,
    actorRole: String,
  ): String {
    val uid = requireUserId()
    val docId = existing?.id?.takeIf { it.isNotBlank() } ?: "evt_${UUID.randomUUID().toString().take(8)}"
    val docRef = db.collection("events").document(docId)
    val qrToken = existing?.qrCodeToken?.takeIf { it.isNotBlank() }
      ?: "YES-EVT-${UUID.randomUUID().toString().take(6).uppercase()}"
    return try {
      val payload = SocietyEvent(
        title = title.trim(),
        description = description.trim(),
        category = category,
        eventDate = eventDate.trim(),
        eventTime = eventTime.trim(),
        venue = venue.trim(),
        posterUri = posterUri,
        registrationDeadline = registrationDeadline.trim(),
        registrationFee = registrationFee,
        maxParticipants = maxParticipants.coerceAtLeast(1L),
        registeredCount = existing?.registeredCount ?: 0L,
        qrCodeToken = qrToken,
        upcoming = upcoming,
        published = published,
        createdByUid = existing?.createdByUid?.takeIf { it.isNotBlank() } ?: uid,
        societyId = SOCIETY_ID,
        createdAt = existing?.createdAt,
        updatedAt = null,
      )
      docRef.set(payload).await()
      logAdminActivity(actorName, actorRole, "SAVED_EVENT", "${title.trim()} ($eventDate)")
      docId
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      throw e
    }
  }

  suspend fun deleteEvent(eventId: String, title: String, actorName: String, actorRole: String) {
    requireUserId()
    val docRef = db.collection("events").document(eventId)
    try {
      docRef.delete().await()
      logAdminActivity(actorName, actorRole, "DELETED_EVENT", title)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  fun observeStudentRegistrations(studentUid: String): Flow<List<EventRegistration>> =
    db.collection("event_registrations")
      .whereEqualTo("studentUid", studentUid)
      .observeSortedList("event_registrations") { it.createdAt }

  fun observeAllRegistrationsAdmin(): Flow<List<EventRegistration>> =
    db.collection("event_registrations")
      .whereEqualTo("societyId", SOCIETY_ID)
      .observeSortedList("event_registrations") { it.createdAt }

  suspend fun registerStudentForEvent(
    event: SocietyEvent,
    student: StudentProfile,
    transactionRef: String = "",
    receiptUri: String = "",
  ) {
    val uid = requireUserId()
    val regId = "reg_${event.id}_$uid".replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
    val regRef = db.collection("event_registrations").document(regId)
    val eventRef = db.collection("events").document(event.id)

    val paymentStatus = when {
      event.registrationFee <= 0.0 -> "FREE"
      transactionRef.isNotBlank() || receiptUri.isNotBlank() -> "RECEIPT_SUBMITTED"
      else -> "UNPAID"
    }
    val regStatus = if (event.registrationFee <= 0.0) "APPROVED" else "PENDING"

    val registration = EventRegistration(
      eventId = event.id,
      eventTitle = event.title,
      eventDate = event.eventDate,
      studentUid = uid,
      studentName = student.fullName,
      registrationNumber = student.registrationNumber,
      department = student.department,
      feeAmount = event.registrationFee,
      paymentStatus = paymentStatus,
      registrationStatus = regStatus,
      receiptUri = receiptUri,
      transactionRef = transactionRef.trim(),
      societyId = SOCIETY_ID,
    )

    try {
      regRef.set(registration).await()
      if (event.registeredCount < event.maxParticipants) {
        eventRef.update(
          mapOf(
            "registeredCount" to (event.registeredCount + 1L),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        ).await()
      }
      if (event.registrationFee > 0.0) {
        val payId = "pay_${event.id}_$uid".replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        val payStatus = if (paymentStatus == "RECEIPT_SUBMITTED") "PENDING_VERIFICATION" else "UNPAID"
        db.collection("payments").document(payId).set(
          StudentPayment(
            studentUid = uid,
            studentName = student.fullName,
            registrationNumber = student.registrationNumber,
            eventId = event.id,
            purposeTitle = "${event.title} Registration Fee",
            amount = event.registrationFee,
            paymentDate = if (payStatus == "PENDING_VERIFICATION") event.eventDate else "",
            transactionRef = transactionRef.trim(),
            receiptUri = receiptUri,
            status = payStatus,
            verifiedByUid = "",
            societyId = SOCIETY_ID,
          )
        ).await()
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, regRef.path)
      throw e
    }
  }

  suspend fun submitPaymentReceiptForRegistration(
    registrationId: String,
    paymentId: String?,
    paymentDate: String,
    transactionRef: String,
    receiptUri: String,
  ) {
    requireUserId()
    val regRef = db.collection("event_registrations").document(registrationId)
    try {
      regRef.update(
        mapOf(
          "receiptUri" to receiptUri,
          "transactionRef" to transactionRef.trim(),
          "paymentStatus" to "RECEIPT_SUBMITTED",
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()
      if (!paymentId.isNullOrBlank()) {
        db.collection("payments").document(paymentId).update(
          mapOf(
            "paymentDate" to paymentDate.trim(),
            "transactionRef" to transactionRef.trim(),
            "receiptUri" to receiptUri,
            "status" to "PENDING_VERIFICATION",
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        ).await()
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, regRef.path)
      throw e
    }
  }

  suspend fun updateRegistrationAndPaymentStatusAdmin(
    registration: EventRegistration,
    registrationStatus: String,
    paymentStatus: String,
    actorName: String,
    actorRole: String,
  ) {
    val adminUid = requireUserId()
    val regRef = db.collection("event_registrations").document(registration.id)
    try {
      regRef.set(
        registration.copy(
          registrationStatus = registrationStatus,
          paymentStatus = paymentStatus,
          updatedAt = null,
        )
      ).await()
      sendPersonalNotification(
        recipientUid = registration.studentUid,
        title = "Event Registration Update: ${registration.eventTitle}",
        body = "Registration status: $registrationStatus • Payment: $paymentStatus",
        type = "PAYMENT",
      )
      logAdminActivity(actorName, actorRole, "UPDATED_REGISTRATION", "${registration.studentName} - ${registration.eventTitle} ($registrationStatus/$paymentStatus) by $adminUid")
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, regRef.path)
      throw e
    }
  }

  suspend fun registerStudentForEventByAdmin(
    event: SocietyEvent,
    student: StudentProfile,
    paymentStatus: String,
    actorName: String,
    actorRole: String,
  ) {
    requireUserId()
    val targetUid = student.studentUid.ifBlank { student.id }
    val regId = "reg_${event.id}_$targetUid".replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
    val regRef = db.collection("event_registrations").document(regId)
    val eventRef = db.collection("events").document(event.id)
    val finalPayStatus = if (event.registrationFee <= 0.0) "FREE" else paymentStatus
    try {
      val existing = regRef.get().await().exists()
      regRef.set(
        EventRegistration(
          eventId = event.id,
          eventTitle = event.title,
          eventDate = event.eventDate,
          studentUid = targetUid,
          studentName = student.fullName,
          registrationNumber = student.registrationNumber,
          department = student.department,
          feeAmount = event.registrationFee,
          paymentStatus = finalPayStatus,
          registrationStatus = "APPROVED",
          societyId = SOCIETY_ID,
        )
      ).await()
      if (!existing && event.registeredCount < event.maxParticipants) {
        eventRef.update(
          mapOf(
            "registeredCount" to (event.registeredCount + 1L),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        ).await()
      }
      logAdminActivity(actorName, actorRole, "REGISTERED_STUDENT_FOR_EVENT", "${student.fullName} -> ${event.title}")
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, regRef.path)
      throw e
    }
  }

  suspend fun deleteEventRegistration(
    registration: EventRegistration,
    event: SocietyEvent?,
    actorName: String? = null,
    actorRole: String? = null,
  ) {
    requireUserId()
    val docRef = db.collection("event_registrations").document(registration.id)
    try {
      docRef.delete().await()
      if (event != null && event.registeredCount > 0L) {
        db.collection("events").document(event.id).update(
          mapOf(
            "registeredCount" to (event.registeredCount - 1L).coerceAtLeast(0L),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        ).await()
      }
      if (!actorName.isNullOrBlank()) {
        logAdminActivity(actorName, actorRole ?: "ADMIN", "DELETED_REGISTRATION", "${registration.studentName} - ${registration.eventTitle}")
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  // --- Attendance ---

  fun observeStudentAttendance(studentUid: String): Flow<List<AttendanceRecord>> =
    db.collection("attendance_records")
      .whereEqualTo("studentUid", studentUid)
      .observeSortedList("attendance_records") { it.createdAt }

  fun observeAllAttendanceAdmin(): Flow<List<AttendanceRecord>> =
    db.collection("attendance_records")
      .whereEqualTo("societyId", SOCIETY_ID)
      .observeSortedList("attendance_records") { it.createdAt }

  suspend fun markAttendanceByAdmin(
    event: SocietyEvent,
    studentUid: String,
    studentName: String,
    registrationNumber: String,
    membershipId: String,
    status: String,
    method: String,
    actorName: String,
    actorRole: String,
  ) {
    val callerUid = requireUserId()
    val attId = "att_${event.id}_$studentUid".replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
    val attRef = db.collection("attendance_records").document(attId)
    try {
      val existingSnap = attRef.get().await().toObject(
        AttendanceRecord::class.java,
        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
      )
      if (existingSnap != null && existingSnap.createdAt != null) {
        attRef.update(
          mapOf(
            "status" to status,
            "method" to method,
            "markedByUid" to callerUid,
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        ).await()
      } else {
        attRef.set(
          AttendanceRecord(
            eventId = event.id,
            eventTitle = event.title,
            eventDate = event.eventDate,
            studentUid = studentUid,
            studentName = studentName,
            registrationNumber = registrationNumber,
            membershipId = membershipId,
            status = status,
            method = method,
            markedByUid = callerUid,
            societyId = SOCIETY_ID,
          )
        ).await()
      }
      logAdminActivity(actorName, actorRole, "MARKED_ATTENDANCE", "$studentName -> $status (${event.title})")
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, attRef.path)
      throw e
    }
  }

  suspend fun studentCheckInWithEventQrToken(
    qrTokenInput: String,
    publishedEvents: List<SocietyEvent>,
    student: StudentProfile,
  ): SocietyEvent {
    val uid = requireUserId()
    val cleanToken = qrTokenInput.trim()
      .removePrefix("YES-EVENT:")
      .removePrefix("yes-event:")
      .trim()
    val matchedEvent = publishedEvents.firstOrNull {
      it.qrCodeToken.equals(cleanToken, ignoreCase = true)
    } ?: throw IllegalArgumentException("Invalid or expired Event QR Code token.")

    val attId = "att_${matchedEvent.id}_$uid".replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
    val attRef = db.collection("attendance_records").document(attId)
    try {
      attRef.set(
        AttendanceRecord(
          eventId = matchedEvent.id,
          eventTitle = matchedEvent.title,
          eventDate = matchedEvent.eventDate,
          studentUid = uid,
          studentName = student.fullName,
          registrationNumber = student.registrationNumber,
          membershipId = student.membershipId,
          status = "PRESENT",
          method = "EVENT_QR_CHECKIN",
          markedByUid = uid,
          societyId = SOCIETY_ID,
        )
      ).await()
      return matchedEvent
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, attRef.path)
      throw e
    }
  }

  suspend fun deleteAttendanceRecord(
    record: AttendanceRecord,
    actorName: String,
    actorRole: String,
  ) {
    requireUserId()
    val docRef = db.collection("attendance_records").document(record.id)
    try {
      docRef.delete().await()
      logAdminActivity(actorName, actorRole, "DELETED_ATTENDANCE", "${record.studentName} (${record.eventTitle})")
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  // --- Announcements & Notifications ---

  fun observePublishedAnnouncements(): Flow<List<Announcement>> =
    db.collection("announcements")
      .whereEqualTo("published", true)
      .observeSortedList("announcements") { it.createdAt }

  fun observeAllAnnouncementsAdmin(): Flow<List<Announcement>> =
    db.collection("announcements")
      .whereEqualTo("societyId", SOCIETY_ID)
      .observeSortedList("announcements") { it.createdAt }

  suspend fun saveAnnouncement(
    existing: Announcement?,
    title: String,
    message: String,
    category: String,
    targetGroup: String,
    priority: String,
    scheduledDate: String,
    published: Boolean,
    authorName: String,
    actorRole: String,
  ) {
    val uid = requireUserId()
    val docId = existing?.id?.takeIf { it.isNotBlank() } ?: "ann_${UUID.randomUUID().toString().take(8)}"
    val docRef = db.collection("announcements").document(docId)
    try {
      if (existing != null && existing.createdAt != null) {
        docRef.update(
          mapOf(
            "title" to title.trim(),
            "message" to message.trim(),
            "category" to category,
            "targetGroup" to targetGroup,
            "priority" to priority,
            "scheduledDate" to scheduledDate.trim(),
            "published" to published,
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        ).await()
      } else {
        docRef.set(
          Announcement(
            title = title.trim(),
            message = message.trim(),
            category = category,
            targetGroup = targetGroup,
            priority = priority,
            scheduledDate = scheduledDate.trim(),
            authorName = authorName,
            authorUid = uid,
            published = published,
            societyId = SOCIETY_ID,
          )
        ).await()
      }
      logAdminActivity(authorName, actorRole, "PUBLISHED_ANNOUNCEMENT", "${title.trim()} [$targetGroup]")
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      throw e
    }
  }

  suspend fun deleteAnnouncement(id: String, title: String, actorName: String, actorRole: String) {
    requireUserId()
    val docRef = db.collection("announcements").document(id)
    try {
      docRef.delete().await()
      logAdminActivity(actorName, actorRole, "DELETED_ANNOUNCEMENT", title)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  fun observeUserNotifications(uid: String): Flow<List<UserNotification>> =
    db.collection("notifications")
      .whereEqualTo("recipientUid", uid)
      .observeSortedList("notifications") { it.createdAt }

  suspend fun sendPersonalNotification(
    recipientUid: String,
    title: String,
    body: String,
    type: String,
  ) {
    requireUserId()
    val notifId = "ntf_${UUID.randomUUID().toString().take(10)}"
    val notifRef = db.collection("notifications").document(notifId)
    try {
      notifRef.set(
        UserNotification(
          recipientUid = recipientUid,
          title = title.take(150),
          body = body.take(600),
          type = type,
          read = false,
          societyId = SOCIETY_ID,
        )
      ).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, notifRef.path)
    }
  }

  suspend fun markNotificationRead(notificationId: String) {
    requireUserId()
    val docRef = db.collection("notifications").document(notificationId)
    try {
      docRef.update(
        mapOf(
          "read" to true,
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  suspend fun deleteNotification(notificationId: String) {
    requireUserId()
    val docRef = db.collection("notifications").document(notificationId)
    try {
      docRef.delete().await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  // --- Finance & Payments ---

  fun observeStudentPayments(studentUid: String): Flow<List<StudentPayment>> =
    db.collection("payments")
      .whereEqualTo("studentUid", studentUid)
      .observeSortedList("payments") { it.createdAt }

  fun observeAllPaymentsFinance(): Flow<List<StudentPayment>> =
    db.collection("payments")
      .whereEqualTo("societyId", SOCIETY_ID)
      .observeSortedList("payments") { it.createdAt }

  fun observeFinanceTransactions(): Flow<List<FinanceTransaction>> =
    db.collection("finance_transactions")
      .whereEqualTo("societyId", SOCIETY_ID)
      .observeSortedList("finance_transactions") { it.createdAt }

  suspend fun submitStudentPaymentReceipt(
    payment: StudentPayment,
    paymentDate: String,
    transactionRef: String,
    receiptUri: String,
  ) {
    requireUserId()
    val payRef = db.collection("payments").document(payment.id)
    try {
      payRef.update(
        mapOf(
          "paymentDate" to paymentDate.trim(),
          "transactionRef" to transactionRef.trim(),
          "receiptUri" to receiptUri,
          "status" to "PENDING_VERIFICATION",
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, payRef.path)
      throw e
    }
  }

  suspend fun verifyStudentPaymentByFinanceAdmin(
    payment: StudentPayment,
    approve: Boolean,
    actorName: String,
    actorRole: String,
  ) {
    val adminUid = requireUserId()
    val payRef = db.collection("payments").document(payment.id)
    try {
      val newStatus = if (approve) "PAID" else "REJECTED"
      payRef.update(
        mapOf(
          "status" to newStatus,
          "verifiedByUid" to adminUid,
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()

      if (approve && payment.amount > 0.0) {
        val txId = "tx_pay_${payment.id}".replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        db.collection("finance_transactions").document(txId).set(
          FinanceTransaction(
            title = "${payment.purposeTitle} - ${payment.studentName}",
            type = "INCOME",
            category = "REGISTRATION_FEE",
            amount = payment.amount,
            transactionDate = payment.paymentDate.ifBlank { "2026-10-31" },
            referenceNumber = payment.transactionRef,
            relatedEventId = payment.eventId,
            notes = "Verified student fee (${payment.registrationNumber})",
            recordedByUid = adminUid,
            recordedByName = actorName,
            societyId = SOCIETY_ID,
          )
        ).await()

        // Sync with event_registrations
        if (payment.eventId.isNotBlank()) {
          val regsSnap = db.collection("event_registrations")
            .whereEqualTo("eventId", payment.eventId)
            .whereEqualTo("studentUid", payment.studentUid)
            .limit(1)
            .get()
            .await()
          
          if (!regsSnap.isEmpty) {
            val regDoc = regsSnap.documents.first()
            regDoc.reference.update(
              mapOf(
                "paymentStatus" to "PAID",
                "registrationStatus" to "APPROVED",
                "updatedAt" to FieldValue.serverTimestamp()
              )
            ).await()
          }
        }
      }

      sendPersonalNotification(
        recipientUid = payment.studentUid,
        title = if (approve) "Payment Verified" else "Payment Verification Issue",
        body = "${payment.purposeTitle} (PKR ${payment.amount.toInt()}) has been marked $newStatus.",
        type = "PAYMENT",
      )
      logAdminActivity(actorName, actorRole, "VERIFIED_PAYMENT", "${payment.studentName} - ${payment.purposeTitle} ($newStatus)")
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, payRef.path)
      throw e
    }
  }

  suspend fun deleteStudentPayment(
    payment: StudentPayment,
    actorName: String? = null,
    actorRole: String? = null,
  ) {
    requireUserId()
    val docRef = db.collection("payments").document(payment.id)
    try {
      docRef.delete().await()
      if (!actorName.isNullOrBlank()) {
        logAdminActivity(actorName, actorRole ?: "FINANCE_ADMIN", "DELETED_PAYMENT_RECORD", "${payment.studentName} - ${payment.purposeTitle}")
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  suspend fun recordFinanceTransaction(
    title: String,
    type: String,
    category: String,
    amount: Double,
    transactionDate: String,
    referenceNumber: String,
    notes: String,
    actorName: String,
    actorRole: String,
  ): String {
    val uid = requireUserId()
    val txId = "tx_${UUID.randomUUID().toString().take(8)}"
    val txRef = db.collection("finance_transactions").document(txId)
    return try {
      txRef.set(
        FinanceTransaction(
          title = title.trim(),
          type = type,
          category = category,
          amount = amount,
          transactionDate = transactionDate.trim(),
          referenceNumber = referenceNumber.trim(),
          relatedEventId = "",
          notes = notes.trim(),
          recordedByUid = uid,
          recordedByName = actorName,
          societyId = SOCIETY_ID,
        )
      ).await()
      logAdminActivity(actorName, actorRole, "RECORDED_${type}", "${title.trim()} (PKR ${amount.toInt()})")
      txId
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, txRef.path)
      throw e
    }
  }

  suspend fun deleteFinanceTransaction(txId: String, summary: String, actorName: String, actorRole: String) {
    requireUserId()
    val docRef = db.collection("finance_transactions").document(txId)
    try {
      docRef.delete().await()
      logAdminActivity(actorName, actorRole, "DELETED_TRANSACTION", summary)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  // --- Official Society Payment Accounts (Finance Admin) ---

  fun observeActivePaymentAccounts(): Flow<List<PaymentAccount>> =
    db.collection("payment_accounts")
      .whereEqualTo("active", true)
      .observeSortedList("payment_accounts") { it.createdAt }

  fun observeAllPaymentAccountsAdmin(): Flow<List<PaymentAccount>> =
    db.collection("payment_accounts")
      .whereEqualTo("societyId", SOCIETY_ID)
      .observeSortedList("payment_accounts") { it.createdAt }

  suspend fun savePaymentAccount(
    existing: PaymentAccount?,
    accountType: String,
    bankOrWalletName: String,
    accountTitle: String,
    accountNumber: String,
    ibanOrBranch: String,
    instructions: String,
    active: Boolean,
    actorName: String,
    actorRole: String,
  ): String {
    val uid = requireUserId()
    val docId = existing?.id?.takeIf { it.isNotBlank() } ?: "acc_${UUID.randomUUID().toString().take(8)}"
    val docRef = db.collection("payment_accounts").document(docId)
    return try {
      if (existing != null && existing.createdAt != null) {
        docRef.update(
          mapOf(
            "accountType" to accountType,
            "bankOrWalletName" to bankOrWalletName.trim(),
            "accountTitle" to accountTitle.trim(),
            "accountNumber" to accountNumber.trim(),
            "ibanOrBranch" to ibanOrBranch.trim(),
            "instructions" to instructions.trim(),
            "active" to active,
            "updatedByUid" to uid,
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        ).await()
      } else {
        docRef.set(
          PaymentAccount(
            accountType = accountType,
            bankOrWalletName = bankOrWalletName.trim(),
            accountTitle = accountTitle.trim(),
            accountNumber = accountNumber.trim(),
            ibanOrBranch = ibanOrBranch.trim(),
            instructions = instructions.trim(),
            active = active,
            updatedByUid = uid,
            societyId = SOCIETY_ID,
          )
        ).await()
      }
      logAdminActivity(actorName, actorRole, "SAVED_PAYMENT_ACCOUNT", "${bankOrWalletName.trim()} (${accountTitle.trim()})")
      docId
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      throw e
    }
  }

  suspend fun deletePaymentAccount(
    accountId: String,
    summary: String,
    actorName: String,
    actorRole: String,
  ) {
    requireUserId()
    val docRef = db.collection("payment_accounts").document(accountId)
    try {
      docRef.delete().await()
      logAdminActivity(actorName, actorRole, "DELETED_PAYMENT_ACCOUNT", summary)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  // --- Gallery ---

  fun observeApprovedGallery(): Flow<List<GalleryItem>> =
    db.collection("gallery_items")
      .whereEqualTo("approved", true)
      .observeSortedList("gallery_items") { it.createdAt }

  fun observeAllGalleryAdmin(): Flow<List<GalleryItem>> =
    db.collection("gallery_items")
      .whereEqualTo("societyId", SOCIETY_ID)
      .observeSortedList("gallery_items") { it.createdAt }

  suspend fun saveGalleryItem(
    existing: GalleryItem?,
    title: String,
    albumCategory: String,
    caption: String,
    eventDate: String,
    imageUri: String,
    accentTheme: String,
    approved: Boolean,
    actorName: String,
    actorRole: String,
  ) {
    val uid = requireUserId()
    val docId = existing?.id?.takeIf { it.isNotBlank() } ?: "gal_${UUID.randomUUID().toString().take(8)}"
    val docRef = db.collection("gallery_items").document(docId)
    try {
      if (existing != null && existing.createdAt != null) {
        docRef.update(
          mapOf(
            "title" to title.trim(),
            "albumCategory" to albumCategory,
            "caption" to caption.trim(),
            "eventDate" to eventDate.trim(),
            "imageUri" to imageUri,
            "accentTheme" to accentTheme,
            "approved" to approved,
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        ).await()
      } else {
        docRef.set(
          GalleryItem(
            title = title.trim(),
            albumCategory = albumCategory,
            caption = caption.trim(),
            eventDate = eventDate.trim(),
            imageUri = imageUri,
            accentTheme = accentTheme,
            approved = approved,
            uploadedByUid = uid,
            societyId = SOCIETY_ID,
          )
        ).await()
      }
      logAdminActivity(actorName, actorRole, "SAVED_GALLERY_PHOTO", "${title.trim()} ($albumCategory)")
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      throw e
    }
  }

  suspend fun deleteGalleryItem(id: String, title: String, actorName: String, actorRole: String) {
    requireUserId()
    val docRef = db.collection("gallery_items").document(id)
    try {
      docRef.delete().await()
      logAdminActivity(actorName, actorRole, "DELETED_GALLERY_PHOTO", title)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  // --- Certificates ---

  fun observeStudentCertificates(studentUid: String): Flow<List<SocietyCertificate>> =
    db.collection("certificates")
      .whereEqualTo("studentUid", studentUid)
      .observeSortedList("certificates") { it.createdAt }

  fun observeAllCertificatesAdmin(): Flow<List<SocietyCertificate>> =
    db.collection("certificates")
      .whereEqualTo("societyId", SOCIETY_ID)
      .observeSortedList("certificates") { it.createdAt }

  suspend fun issueCertificate(
    studentUid: String,
    studentName: String,
    registrationNumber: String,
    eventId: String,
    eventTitle: String,
    eventDate: String,
    certificateType: String,
    authorizedSignatory: String,
    actorName: String,
    actorRole: String,
  ): SocietyCertificate {
    val uid = requireUserId()
    val code = "YES-CERT-2026-${UUID.randomUUID().toString().take(6).uppercase()}"
    val certRef = db.collection("certificates").document(code)
    val cert = SocietyCertificate(
      certificateCode = code,
      studentUid = studentUid,
      studentName = studentName.trim(),
      registrationNumber = registrationNumber.trim(),
      eventId = eventId,
      eventTitle = eventTitle.trim(),
      eventDate = eventDate.trim(),
      certificateType = certificateType,
      authorizedSignatory = authorizedSignatory.trim(),
      issuedByUid = uid,
      status = "PENDING",
      societyId = SOCIETY_ID,
    )
    return try {
      certRef.set(cert).await()
      sendPersonalNotification(
        recipientUid = studentUid,
        title = "Certificate Issued: ${eventTitle.trim()}",
        body = "Your official $certificateType certificate ($code) is ready to download as PDF.",
        type = "CERTIFICATE",
      )
      logAdminActivity(actorName, actorRole, "ISSUED_CERTIFICATE", "$studentName - $code")
      cert.copy(id = code)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, certRef.path)
      throw e
    }
  }

  suspend fun approveCertificateByCabinet(
    certificate: SocietyCertificate,
    actorName: String,
    actorRole: String,
  ) {
    val uid = requireUserId()
    val docId = certificate.id.ifBlank { certificate.certificateCode }
    val docRef = db.collection("certificates").document(docId)
    try {
      docRef.update(
        mapOf(
          "status" to "APPROVED",
          "approverUids" to FieldValue.arrayUnion(uid),
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()
      sendPersonalNotification(
        recipientUid = certificate.studentUid,
        title = "Certificate Approved!",
        body = "Your certificate for ${certificate.eventTitle} has been approved by $actorName ($actorRole).",
        type = "CERTIFICATE",
      )
      logAdminActivity(actorName, actorRole, "APPROVED_CERTIFICATE", "${certificate.certificateCode} (${certificate.studentName})")
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  suspend fun rejectCertificateByCabinet(
    certificate: SocietyCertificate,
    reason: String,
    actorName: String,
    actorRole: String,
  ) {
    val docId = certificate.id.ifBlank { certificate.certificateCode }
    val docRef = db.collection("certificates").document(docId)
    try {
      docRef.update(
        mapOf(
          "status" to "REJECTED",
          "rejectionReason" to reason,
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()
      sendPersonalNotification(
        recipientUid = certificate.studentUid,
        title = "Certificate Rejected",
        body = "Your certificate for ${certificate.eventTitle} was rejected. Reason: $reason",
        type = "CERTIFICATE",
      )
      logAdminActivity(actorName, actorRole, "REJECTED_CERTIFICATE", "${certificate.certificateCode} (${certificate.studentName})")
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  suspend fun verifyCertificateByCode(codeInput: String): SocietyCertificate? {
    val sanitized = codeInput.trim().uppercase()
    if (!sanitized.matches(Regex("^[A-Z0-9_\\-]+$"))) return null
    val docRef = db.collection("certificates").document(sanitized)
    return try {
      val snap = docRef.get().await()
      if (snap.exists()) {
        snap.toObject(SocietyCertificate::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
      } else {
        null
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.GET, docRef.path)
      null
    }
  }

  suspend fun deleteCertificate(
    certificate: SocietyCertificate,
    actorName: String,
    actorRole: String,
  ) {
    requireUserId()
    val docId = certificate.id.ifBlank { certificate.certificateCode }
    val docRef = db.collection("certificates").document(docId)
    try {
      docRef.delete().await()
      logAdminActivity(actorName, actorRole, "DELETED_CERTIFICATE", "${certificate.certificateCode} (${certificate.studentName})")
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  // --- Feedback & Suggestions ---

  fun observeStudentFeedback(studentUid: String): Flow<List<StudentFeedback>> =
    db.collection("feedback")
      .whereEqualTo("studentUid", studentUid)
      .observeSortedList("feedback") { it.createdAt }

  fun observeAllFeedbackAdmin(): Flow<List<StudentFeedback>> =
    db.collection("feedback")
      .whereEqualTo("societyId", SOCIETY_ID)
      .observeSortedList("feedback") { it.createdAt }

  suspend fun submitStudentFeedback(
    studentName: String,
    category: String,
    subject: String,
    message: String,
    relatedEventTitle: String,
  ) {
    val uid = requireUserId()
    val fbId = "fb_${UUID.randomUUID().toString().take(8)}"
    val fbRef = db.collection("feedback").document(fbId)
    try {
      fbRef.set(
        StudentFeedback(
          studentUid = uid,
          studentName = studentName,
          category = category,
          subject = subject.trim(),
          message = message.trim(),
          relatedEventTitle = relatedEventTitle.trim(),
          status = "SUBMITTED",
          adminResponse = "",
          societyId = SOCIETY_ID,
        )
      ).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, fbRef.path)
      throw e
    }
  }

  suspend fun respondToFeedbackAdmin(
    feedbackId: String,
    status: String,
    adminResponse: String,
    actorName: String,
    actorRole: String,
  ) {
    requireUserId()
    val docRef = db.collection("feedback").document(feedbackId)
    try {
      docRef.update(
        mapOf(
          "status" to status,
          "adminResponse" to adminResponse.trim(),
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()
      logAdminActivity(actorName, actorRole, "RESPONDED_FEEDBACK", "Feedback $feedbackId -> $status")
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  suspend fun deleteFeedback(
    feedback: StudentFeedback,
    actorName: String? = null,
    actorRole: String? = null,
  ) {
    requireUserId()
    val docRef = db.collection("feedback").document(feedback.id)
    try {
      docRef.delete().await()
      if (!actorName.isNullOrBlank()) {
        logAdminActivity(actorName, actorRole ?: "ADMIN", "DELETED_FEEDBACK", "${feedback.studentName}: ${feedback.subject}")
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  // --- Audit Logs ---

  fun observeAuditLogsAdmin(): Flow<List<AdminAuditLog>> =
    db.collection("audit_logs")
      .whereEqualTo("societyId", SOCIETY_ID)
      .observeSortedList("audit_logs") { it.createdAt }

  suspend fun logAdminActivity(
    actorName: String,
    actorRole: String,
    action: String,
    targetSummary: String,
  ) {
    val uid = auth.currentUser?.uid ?: return
    val logId = "log_${UUID.randomUUID().toString().take(10)}"
    val logRef = db.collection("audit_logs").document(logId)
    try {
      logRef.set(
        AdminAuditLog(
          actorUid = uid,
          actorName = actorName.take(100).ifBlank { "Administrator" },
          actorRole = actorRole.take(50).ifBlank { "ADMIN" },
          action = action.take(120),
          targetSummary = targetSummary.take(300),
          societyId = SOCIETY_ID,
        )
      ).await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, logRef.path)
    }
  }

  suspend fun deleteAuditLog(logId: String) {
    requireUserId()
    val docRef = db.collection("audit_logs").document(logId)
    try {
      docRef.delete().await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  fun observeSocietyConfig(): Flow<SocietyConfig> =
    db.collection("society_config").document("GLOBAL_CONFIG")
      .snapshots()
      .map { it.toObject(SocietyConfig::class.java) ?: SocietyConfig() }

  suspend fun updateSocietyConfig(
    config: SocietyConfig,
    actorName: String,
    actorRole: String,
  ) {
    val uid = requireUserId()
    val docRef = db.collection("society_config").document("GLOBAL_CONFIG")
    try {
      docRef.set(
        config.copy(
          updatedAtByUid = uid,
          updatedByActorName = actorName,
          updatedAt = null
        )
      ).await()
      logAdminActivity(actorName, actorRole, "UPDATED_SOCIETY_CONFIG", "Modified portal links/news")
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      throw e
    }
  }
}
