package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

const val SOCIETY_ID = "YES_UAF"
const val BOOTSTRAPPED_SUPER_ADMIN_EMAIL = "abubakar.offical227@gmail.com"

sealed interface UiState<out T> {
  data object Loading : UiState<Nothing>
  data class Success<T>(val data: T) : UiState<T>
  data class Error(val message: String) : UiState<Nothing>
}

enum class AdminRoleType(
  val key: String,
  val displayTitle: String,
  val defaultDesignation: String,
  val defaultPermissions: List<String>,
) {
  SUPER_ADMIN(
    key = "SUPER_ADMIN",
    displayTitle = "Super Administrator",
    defaultDesignation = "Super Administrator",
    defaultPermissions = SocietyConstants.ALL_PERMISSIONS,
  ),
  PRESIDENT(
    key = "PRESIDENT",
    displayTitle = "President",
    defaultDesignation = "President",
    defaultPermissions = SocietyConstants.ALL_PERMISSIONS,
  ),
  SENIOR_VICE_PRESIDENT(
    key = "SENIOR_VICE_PRESIDENT",
    displayTitle = "Senior Vice President",
    defaultDesignation = "Senior Vice President",
    defaultPermissions = SocietyConstants.ALL_PERMISSIONS,
  ),
  VICE_PRESIDENT(
    key = "VICE_PRESIDENT",
    displayTitle = "Vice President",
    defaultDesignation = "Vice President",
    defaultPermissions = SocietyConstants.ALL_PERMISSIONS,
  ),
  GENERAL_SECRETARY(
    key = "GENERAL_SECRETARY",
    displayTitle = "General Secretary",
    defaultDesignation = "General Secretary",
    defaultPermissions = SocietyConstants.ALL_PERMISSIONS,
  ),
  JOINT_SECRETARY(
    key = "JOINT_SECRETARY",
    displayTitle = "Joint Secretary",
    defaultDesignation = "Joint Secretary",
    defaultPermissions = listOf(
      "STUDENT_REGISTRATION_APPROVAL", "MEMBERSHIP_FORM_APPROVAL", "CABINET_MANAGEMENT", 
      "EVENT_MANAGEMENT", "REGISTRATION_MANAGEMENT", "ATTENDANCE_MANAGEMENT", 
      "NOTIFICATION_MANAGEMENT", "CERTIFICATE_GENERATION", "CERTIFICATE_APPROVAL"
    ),
  ),
  FINANCE_SECRETARY(
    key = "FINANCE_SECRETARY",
    displayTitle = "Finance Secretary",
    defaultDesignation = "Finance Secretary",
    defaultPermissions = listOf("FINANCE_MANAGEMENT", "REPORTS_AND_ANALYTICS", "REGISTRATION_MANAGEMENT"),
  ),
  SPORTS_SECRETARY(
    key = "SPORTS_SECRETARY",
    displayTitle = "Sports Secretary",
    defaultDesignation = "Sports Secretary",
    defaultPermissions = listOf("EVENT_MANAGEMENT", "REGISTRATION_MANAGEMENT", "ATTENDANCE_MANAGEMENT", "NOTIFICATION_MANAGEMENT"),
  ),
  ASSOCIATE_SPORTS_SECRETARY(
    key = "ASSOCIATE_SPORTS_SECRETARY",
    displayTitle = "Associate Sports Secretary",
    defaultDesignation = "Associate Sports Secretary",
    defaultPermissions = listOf("EVENT_MANAGEMENT", "REGISTRATION_MANAGEMENT", "ATTENDANCE_MANAGEMENT"),
  ),
  PRESS_SECRETARY(
    key = "PRESS_SECRETARY",
    displayTitle = "Press Secretary",
    defaultDesignation = "Press Secretary",
    defaultPermissions = listOf("NOTIFICATION_MANAGEMENT", "DATA_MANAGEMENT", "EVENT_MANAGEMENT", "CERTIFICATE_APPROVAL"),
  ),
  ASSOCIATE_PRESS_SECRETARY(
    key = "ASSOCIATE_PRESS_SECRETARY",
    displayTitle = "Associate Press Secretary",
    defaultDesignation = "Associate Press Secretary",
    defaultPermissions = listOf("NOTIFICATION_MANAGEMENT", "DATA_MANAGEMENT", "EVENT_MANAGEMENT"),
  ),
  EVENT_SECRETARY(
    key = "EVENT_SECRETARY",
    displayTitle = "Event Secretary",
    defaultDesignation = "Event Secretary",
    defaultPermissions = listOf("EVENT_MANAGEMENT", "REGISTRATION_MANAGEMENT", "ATTENDANCE_MANAGEMENT", "NOTIFICATION_MANAGEMENT", "DATA_MANAGEMENT"),
  ),
  IT_MEDIA_CELL(
    key = "IT_MEDIA_CELL",
    displayTitle = "IT and Media Cell",
    defaultDesignation = "IT and Media Cell",
    defaultPermissions = listOf("DATA_MANAGEMENT", "NOTIFICATION_MANAGEMENT", "CERTIFICATE_GENERATION", "IT_MANAGEMENT"),
  ),
  COUNCILLOR(
    key = "COUNCILLOR",
    displayTitle = "Councillor",
    defaultDesignation = "Councillor",
    defaultPermissions = listOf("STUDENT_REGISTRATION_APPROVAL", "MEMBERSHIP_FORM_APPROVAL", "CABINET_MANAGEMENT", "EVENT_MANAGEMENT", "NOTIFICATION_MANAGEMENT"),
  );

  companion object {
    fun fromKey(key: String): AdminRoleType =
      entries.firstOrNull { it.key == key } ?: COUNCILLOR
  }
}

enum class IdCardDesignTheme(
  val key: String,
  val displayName: String,
  val badgeLabel: String,
) {
  ROYAL_EMERALD_GOLD(
    key = "ROYAL_EMERALD_GOLD",
    displayName = "Royal Emerald",
    badgeLabel = "Gold Crest",
  ),
  IMPERIAL_SAPPHIRE_SILVER(
    key = "IMPERIAL_SAPPHIRE_SILVER",
    displayName = "Imperial Sapphire",
    badgeLabel = "Silver Modern",
  ),
  CRIMSON_VELVET_CHAMPAGNE(
    key = "CRIMSON_VELVET_CHAMPAGNE",
    displayName = "Crimson Velvet",
    badgeLabel = "Champagne",
  ),
  OBSIDIAN_EXECUTIVE_AMBER(
    key = "OBSIDIAN_EXECUTIVE_AMBER",
    displayName = "Obsidian Noir",
    badgeLabel = "Amber Luxe",
  ),
  BOTANICAL_IVORY_FOREST(
    key = "BOTANICAL_IVORY_FOREST",
    displayName = "Botanical Ivory",
    badgeLabel = "Light Mint",
  );

  companion object {
    fun defaultFor(student: StudentProfile): IdCardDesignTheme =
      if (student.isCabinetMember()) ROYAL_EMERALD_GOLD else BOTANICAL_IVORY_FOREST
  }
}

object SocietyConstants {
  val EVENT_CATEGORIES = listOf(
    "Seminars",
    "Workshops",
    "Annual Dinner",
    "Sports Gala",
    "Study Tours",
    "Industrial Visits",
    "Educational Trips",
    "Competitions",
    "Welcome and Farewell Events",
    "Society Meetings",
  )

  val EXECUTIVE_DESIGNATIONS = listOf(
    "Patron-in-Chief",
    "Convener",
    "Advisory Board",
    "President",
    "Senior Vice President",
    "Associate Senior Vice President",
    "Vice President",
    "Associate Vice President",
    "General Secretary",
    "Associate General Secretary",
    "Joint Secretary",
    "Associate Joint Secretary",
    "Councillor",
    "Associate Councillor",
    "Finance Secretary",
    "Associate Finance Secretary",
    "IT and Media Cell",
    "Associate IT and Media Cell",
    "Press Secretary",
    "Associate Press Secretary",
    "Information Secretary",
    "Associate Information Secretary",
    "Event Secretary",
    "Associate Event Secretary",
    "Sports Secretary",
    "Associate Sports Secretary",
    "Associate Secretary",
    "Executive Member",
  )

  val GALLERY_ALBUMS = listOf(
    "Annual Dinner",
    "Sports Gala",
    "Study Tours",
    "Workshops & Seminars",
    "Society Meetings",
    "Society Activities",
  )

  val DEPARTMENTS = listOf(
    "Department of Entomology",
    "Plant Pathology",
    "Agronomy",
    "Plant Breeding & Genetics",
    "Soil & Environmental Sciences",
    "Horticulture",
    "Agricultural Engineering",
    "Computer Science",
  )

  val DEGREE_LEVELS = listOf(
    "BSc (Hons) Agriculture",
    "BS",
    "MSc (Hons)",
    "MS / MPhil",
    "PhD",
    "Diploma / Short Course",
  )

  val SEMESTERS = listOf(
    "1st Semester",
    "2nd Semester",
    "3rd Semester",
    "4th Semester",
    "5th Semester",
    "6th Semester",
    "7th Semester",
    "8th Semester",
    "Research / Thesis",
  )

  val PAYMENT_ACCOUNT_TYPES = listOf(
    "Bank Account",
    "JazzCash",
    "EasyPaisa",
    "SadaPay",
    "Nayapay",
    "Other",
  )

  val FINANCE_INCOME_CATEGORIES = listOf(
    "REGISTRATION_FEE",
    "MEMBERSHIP_FEE",
    "SPONSORSHIP",
    "UNIVERSITY_GRANT",
    "DONATION",
    "OTHER_INCOME",
  )

  val FINANCE_EXPENSE_CATEGORIES = listOf(
    "EVENT_LOGISTICS",
    "REFRESHMENTS",
    "PRINTING_BANNERS",
    "SHIELDS_CERTIFICATES",
    "TRANSPORT_TOUR",
    "EQUIPMENT",
    "OTHER_EXPENSE",
  )

  val ALL_PERMISSIONS = listOf(
    "ADMINISTRATOR_ACCESS",
    "STUDENT_REGISTRATION_APPROVAL",
    "MEMBERSHIP_FORM_APPROVAL",
    "REGISTRATION_MANAGEMENT",
    "ATTENDANCE_MANAGEMENT",
    "FINANCE_MANAGEMENT",
    "CERTIFICATE_GENERATION",
    "CERTIFICATE_APPROVAL",
    "ID_CARD_MANAGEMENT",
    "CABINET_MANAGEMENT",
    "REPORTS_AND_ANALYTICS",
    "DATA_MANAGEMENT",
    "NOTIFICATION_MANAGEMENT",
    "EVENT_MANAGEMENT",
    "IT_MANAGEMENT",
    "OTHER_PERMISSIONS",
  )
}

@IgnoreExtraProperties
data class UserAccount(
  @DocumentId val id: String = "",
  val userId: String = "",
  val email: String = "",
  val fullName: String = "",
  val photoUri: String = "",
  val accountType: String = "STUDENT",
  val active: Boolean = true,
  val societyId: String = SOCIETY_ID,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class StudentProfile(
  @DocumentId val id: String = "",
  val studentUid: String = "",
  val fullName: String = "",
  val email: String = "",
  val registrationNumber: String = "",
  val degreeLevel: String = "BSc (Hons) Agriculture",
  val department: String = "Department of Entomology",
  val semester: String = "1st Semester",
  val phone: String = "",
  val photoUri: String = "",
  val studentCardUri: String = "",
  val position: String = "Member",
  val profileLocked: Boolean = false,
  val membershipStatus: String = "NONE",
  val idCardStatus: String = "",
  val membershipId: String = "",
  val membershipValidUntil: String = "",
  val active: Boolean = true,
  val societyId: String = SOCIETY_ID,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

fun StudentProfile.isVerifiedMember(): Boolean =
  membershipStatus.equals("VERIFIED", ignoreCase = true) ||
    membershipStatus.equals("ACTIVE", ignoreCase = true)

fun StudentProfile.isIdCardActive(): Boolean =
  when (idCardStatus.uppercase()) {
    "ISSUED" -> true
    "REVOKED", "NOT_ISSUED" -> false
    else -> isVerifiedMember() && membershipId.isNotBlank()
  }

fun StudentProfile.hasAllocatedRole(): Boolean {
  val clean = position.trim()
  if (clean.isEmpty()) return false
  return clean.lowercase() !in setOf(
    "member",
    "active member",
    "general member",
    "verified member",
    "student",
    "none",
    "administrator",
    "super administrator",
    "system administrator",
  )
}

fun StudentProfile.allocatedRoleOrNull(): String? {
  return if (hasAllocatedRole()) normalizeCabinetPositionLabel(position) else null
}

fun StudentProfile.isCabinetMember(): Boolean = hasAllocatedRole()

@IgnoreExtraProperties
data class AdminAccount(
  @DocumentId val id: String = "",
  val uid: String = "",
  val email: String = "",
  val fullName: String = "",
  val adminRole: String = "ATTENDANCE_MANAGER",
  val designation: String = "Coordinator",
  val permissions: List<String> = emptyList(),
  val active: Boolean = true,
  val assignedByUid: String = "",
  val societyId: String = SOCIETY_ID,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

fun AdminAccount.hasSuperAdminAccess(): Boolean =
  active && (adminRole == "SUPER_ADMIN" || permissions.contains("MANAGE_ADMINS"))

fun AdminAccount.isCabinetOfficerRole(): Boolean =
  active && !adminRole.equals("SUPER_ADMIN", ignoreCase = true)

fun AdminAccount.resolvedDisplayDesignation(): String {
  if (adminRole == "SUPER_ADMIN") {
    val clean = designation.trim()
    if (clean.isEmpty() || clean.contains("President", ignoreCase = true)) {
      return "Administrator"
    }
    return clean
  }
  if (adminRole == "PRESIDENT") {
    return "President"
  }
  val clean = designation.trim()
  if (clean.equals("President (Society Executive Head)", ignoreCase = true) ||
    clean.equals("Society President", ignoreCase = true)
  ) {
    return "President"
  }
  return clean.ifBlank { AdminRoleType.fromKey(adminRole).defaultDesignation }
}

fun AdminAccount.resolvedRoleLabel(): String =
  if (adminRole == "SUPER_ADMIN") {
    "Administrator"
  } else {
    AdminRoleType.fromKey(adminRole).displayTitle
  }

fun AdminAccount.hasPermission(perm: String): Boolean {
  if (!active) return false
  if (hasSuperAdminAccess()) return true
  if (permissions.contains(perm)) return true
  return AdminRoleType.fromKey(adminRole).defaultPermissions.contains(perm)
}

@IgnoreExtraProperties
data class MembershipApplication(
  @DocumentId val id: String = "",
  val studentUid: String = "",
  val studentName: String = "",
  val studentEmail: String = "",
  val registrationNumber: String = "",
  val degreeLevel: String = "",
  val department: String = "",
  val semester: String = "",
  val phone: String = "",
  val photoUri: String = "",
  val studentCardUri: String = "",
  val motivation: String = "",
  val status: String = "PENDING",
  val assignedMembershipId: String = "",
  val reviewedByUid: String = "",
  val reviewNotes: String = "",
  val societyId: String = SOCIETY_ID,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

fun MembershipApplication.isVerifiedOrApproved(): Boolean =
  status.equals("VERIFIED", ignoreCase = true) ||
    status.equals("APPROVED", ignoreCase = true)

@IgnoreExtraProperties
data class ExecutiveMember(
  @DocumentId val id: String = "",
  val fullName: String = "",
  val designation: String = "",
  val department: String = "Department of Entomology",
  val tenure: String = "2026 - 2027",
  val email: String = "",
  val photoUri: String = "",
  val displayOrder: Long = 10L,
  val published: Boolean = true,
  val societyId: String = SOCIETY_ID,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

fun normalizeCabinetPositionLabel(rawPosition: String): String {
  val trimmed = rawPosition.trim()
  return if (
    trimmed.equals("President (Society Executive Head)", ignoreCase = true) ||
    trimmed.equals("Society President", ignoreCase = true) ||
    trimmed.equals("PRESIDENT", ignoreCase = true)
  ) {
    "President"
  } else {
    trimmed
  }
}

fun ExecutiveMember.toCabinetIdCardProfile(
  students: List<StudentProfile> = emptyList(),
  users: List<UserAccount> = emptyList(),
): StudentProfile {
  val matched = students.firstOrNull { st ->
    (email.isNotBlank() && st.email.equals(email.trim(), ignoreCase = true)) ||
      st.fullName.trim().equals(fullName.trim(), ignoreCase = true)
  }
  val matchedUser = users.firstOrNull { u ->
    (matched != null && (u.userId == matched.studentUid || u.id == matched.id)) ||
      (email.isNotBlank() && u.email.equals(email.trim(), ignoreCase = true)) ||
      u.fullName.trim().equals(fullName.trim(), ignoreCase = true)
  }
  val safeSlug = id.takeLast(4).uppercase().ifBlank {
    displayOrder.coerceAtLeast(1L).toString().padStart(2, '0')
  }
  val tenureYear = tenure.take(4).filter { it.isDigit() }.ifBlank { "2026" }
  val normalizedRole = normalizeCabinetPositionLabel(designation).ifBlank { "Executive Member" }
  return StudentProfile(
    id = matched?.id ?: id,
    studentUid = matched?.studentUid?.ifBlank { matched.id }
      ?: matchedUser?.userId?.ifBlank { matchedUser.id }
      ?: id.ifBlank { "cabinet_${fullName.lowercase().replace(Regex("[^a-z0-9]"), "_")}" },
    fullName = fullName.ifBlank { matched?.fullName ?: matchedUser?.fullName ?: "Executive Officer" },
    email = email.ifBlank { matched?.email ?: matchedUser?.email ?: "executive.yes@uaf.edu.pk" },
    registrationNumber = matched?.registrationNumber?.takeIf { it.isNotBlank() }
      ?: "YES-EXEC-$safeSlug",
    degreeLevel = matched?.degreeLevel?.takeIf { it.isNotBlank() } ?: "BSc (Hons) Agriculture",
    department = department.ifBlank { matched?.department ?: "Department of Entomology" },
    semester = matched?.semester?.takeIf { it.isNotBlank() } ?: "Tenure $tenure",
    phone = matched?.phone.orEmpty(),
    photoUri = photoUri
      .ifBlank { matched?.photoUri.orEmpty() }
      .ifBlank { matchedUser?.photoUri.orEmpty() },
    studentCardUri = matched?.studentCardUri.orEmpty(),
    position = normalizedRole,
    profileLocked = true,
    membershipStatus = "VERIFIED",
    idCardStatus = if (matched?.idCardStatus.equals("REVOKED", ignoreCase = true)) "REVOKED" else "ISSUED",
    membershipId = matched?.membershipId?.takeIf { it.isNotBlank() }
      ?: "YES-CAB-$tenureYear-$safeSlug",
    membershipValidUntil = matched?.membershipValidUntil?.takeIf { it.isNotBlank() } ?: tenure,
    active = true,
    societyId = SOCIETY_ID,
  )
}

fun AdminAccount.toCabinetIdCardProfile(
  students: List<StudentProfile> = emptyList(),
  users: List<UserAccount> = emptyList(),
  executiveBody: List<ExecutiveMember> = emptyList(),
  fallbackPhotoUri: String = "",
): StudentProfile {
  val resolvedUid = uid.ifBlank { id }
  val matchedStudent = students.firstOrNull { st ->
    (resolvedUid.isNotBlank() && (st.studentUid == resolvedUid || st.id == resolvedUid)) ||
      (email.isNotBlank() && st.email.equals(email.trim(), ignoreCase = true))
  }
  val matchedUser = users.firstOrNull { u ->
    (resolvedUid.isNotBlank() && (u.userId == resolvedUid || u.id == resolvedUid)) ||
      (email.isNotBlank() && u.email.equals(email.trim(), ignoreCase = true))
  }
  val matchedExec = executiveBody.firstOrNull { ex ->
    (email.isNotBlank() && ex.email.equals(email.trim(), ignoreCase = true)) ||
      ex.fullName.trim().equals(fullName.trim(), ignoreCase = true)
  }
  val safeSlug = resolvedUid.takeLast(4).uppercase().ifBlank { "01" }
  val cleanRole = normalizeCabinetPositionLabel(resolvedDisplayDesignation())
  return StudentProfile(
    id = matchedStudent?.id?.ifBlank { resolvedUid } ?: resolvedUid,
    studentUid = resolvedUid,
    fullName = fullName
      .ifBlank { matchedStudent?.fullName.orEmpty() }
      .ifBlank { matchedExec?.fullName.orEmpty() }
      .ifBlank { matchedUser?.fullName.orEmpty() }
      .ifBlank { "Cabinet Officer" },
    email = email.ifBlank { matchedStudent?.email ?: matchedUser?.email ?: "cabinet.yes@uaf.edu.pk" },
    registrationNumber = matchedStudent?.registrationNumber?.takeIf { it.isNotBlank() }
      ?: "YES-CAB-$safeSlug",
    degreeLevel = matchedStudent?.degreeLevel?.takeIf { it.isNotBlank() } ?: "BSc (Hons) Agriculture",
    department = matchedStudent?.department?.takeIf { it.isNotBlank() }
      ?: matchedExec?.department?.takeIf { it.isNotBlank() }
      ?: "Department of Entomology",
    semester = matchedStudent?.semester?.takeIf { it.isNotBlank() }
      ?: matchedExec?.tenure?.let { "Tenure $it" }
      ?: "2026 - 2027",
    phone = matchedStudent?.phone.orEmpty(),
    photoUri = matchedStudent?.photoUri.orEmpty()
      .ifBlank { matchedExec?.photoUri.orEmpty() }
      .ifBlank { matchedUser?.photoUri.orEmpty() }
      .ifBlank { fallbackPhotoUri },
    studentCardUri = matchedStudent?.studentCardUri.orEmpty(),
    position = cleanRole,
    profileLocked = true,
    membershipStatus = "VERIFIED",
    idCardStatus = if (matchedStudent?.idCardStatus.equals("REVOKED", ignoreCase = true)) "REVOKED" else "ISSUED",
    membershipId = matchedStudent?.membershipId?.takeIf { it.isNotBlank() } ?: "YES-CAB-2026-$safeSlug",
    membershipValidUntil = matchedStudent?.membershipValidUntil?.takeIf { it.isNotBlank() }
      ?: matchedExec?.tenure?.takeIf { it.isNotBlank() }
      ?: "2026 - 2027",
    active = active,
    societyId = SOCIETY_ID,
  )
}

/**
 * Aggregates and deduplicates Official Cabinet ID Card profiles for all students who hold
 * administrative control or Executive Cabinet roles (President, Secretaries, Associate Secretaries,
 * Senior VP, VP, Convener, Advisory Board, Councillor, IT & Media Cell, Press Secretary, etc.).
 */
fun buildAdministrativeCabinetIdCards(
  admins: List<AdminAccount>,
  students: List<StudentProfile>,
  executiveBody: List<ExecutiveMember>,
  users: List<UserAccount>,
  currentAdminUid: String = "",
  currentPhotoUri: String = "",
): List<StudentProfile> {
  val results = mutableListOf<StudentProfile>()
  val seenUids = mutableSetOf<String>()
  val seenEmails = mutableSetOf<String>()
  val seenNameRoles = mutableSetOf<String>()

  fun registerCard(card: StudentProfile) {
    val uidKey = card.studentUid.trim().lowercase()
    val emailKey = card.email.trim().lowercase()
    val nameRoleKey = "${card.fullName.trim().lowercase()}::${card.position.trim().lowercase()}"
    if (uidKey.isNotBlank() && !uidKey.startsWith("cabinet_") && seenUids.contains(uidKey)) return
    if (emailKey.isNotBlank() && emailKey != "executive.yes@uaf.edu.pk" && emailKey != "cabinet.yes@uaf.edu.pk" && seenEmails.contains(emailKey)) return
    if (seenNameRoles.contains(nameRoleKey)) return

    if (uidKey.isNotBlank()) seenUids.add(uidKey)
    if (emailKey.isNotBlank()) seenEmails.add(emailKey)
    seenNameRoles.add(nameRoleKey)
    results.add(card)
  }

  // 1. All students appointed with administrative control in /admins (Secretaries, President, VPs, Associates, etc.)
  admins
    .filter { it.isCabinetOfficerRole() }
    .forEach { adm ->
      val admUid = adm.uid.ifBlank { adm.id }
      val fallbackPhoto = if (currentAdminUid.isNotBlank() && admUid == currentAdminUid) currentPhotoUri else ""
      registerCard(
        adm.toCabinetIdCardProfile(
          students = students,
          users = users,
          executiveBody = executiveBody,
          fallbackPhotoUri = fallbackPhoto,
        )
      )
    }

  // 2. All verified/registered students assigned a Cabinet role on their student profile
  students
    .filter { it.active && it.isCabinetMember() }
    .forEach { st ->
      val stUid = st.studentUid.ifBlank { st.id }
      val matchedUser = users.firstOrNull { u ->
        (stUid.isNotBlank() && (u.userId == stUid || u.id == stUid)) ||
          (st.email.isNotBlank() && u.email.equals(st.email.trim(), ignoreCase = true))
      }
      val matchedExec = executiveBody.firstOrNull { ex ->
        (st.email.isNotBlank() && ex.email.equals(st.email.trim(), ignoreCase = true)) ||
          ex.fullName.trim().equals(st.fullName.trim(), ignoreCase = true)
      }
      val fallbackPhoto = if (currentAdminUid.isNotBlank() && stUid == currentAdminUid) currentPhotoUri else ""
      registerCard(
        st.copy(
          position = normalizeCabinetPositionLabel(st.position),
          photoUri = st.photoUri
            .ifBlank { matchedExec?.photoUri.orEmpty() }
            .ifBlank { matchedUser?.photoUri.orEmpty() }
            .ifBlank { fallbackPhoto },
          membershipStatus = if (st.isVerifiedMember()) st.membershipStatus else "VERIFIED",
          idCardStatus = if (st.idCardStatus.equals("REVOKED", ignoreCase = true)) "REVOKED" else "ISSUED",
        )
      )
    }

  // 3. Executive Body directory members
  executiveBody
    .sortedBy { it.displayOrder }
    .forEach { ex ->
      registerCard(ex.toCabinetIdCardProfile(students = students, users = users))
    }

  return results
}

@IgnoreExtraProperties
data class SocietyEvent(
  @DocumentId val id: String = "",
  val title: String = "",
  val description: String = "",
  val category: String = "Seminars",
  val eventDate: String = "",
  val eventTime: String = "",
  val venue: String = "",
  val posterUri: String = "",
  val registrationDeadline: String = "",
  val registrationFee: Double = 0.0,
  val maxParticipants: Long = 100L,
  val registeredCount: Long = 0L,
  val qrCodeToken: String = "",
  val upcoming: Boolean = true,
  val published: Boolean = true,
  val createdByUid: String = "",
  val societyId: String = SOCIETY_ID,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class EventRegistration(
  @DocumentId val id: String = "",
  val eventId: String = "",
  val eventTitle: String = "",
  val eventDate: String = "",
  val studentUid: String = "",
  val studentName: String = "",
  val registrationNumber: String = "",
  val department: String = "",
  val feeAmount: Double = 0.0,
  val paymentStatus: String = "FREE",
  val registrationStatus: String = "APPROVED",
  val receiptUri: String = "",
  val transactionRef: String = "",
  val societyId: String = SOCIETY_ID,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class AttendanceRecord(
  @DocumentId val id: String = "",
  val eventId: String = "",
  val eventTitle: String = "",
  val eventDate: String = "",
  val studentUid: String = "",
  val studentName: String = "",
  val registrationNumber: String = "",
  val membershipId: String = "",
  val status: String = "PRESENT",
  val method: String = "MANUAL",
  val markedByUid: String = "",
  val societyId: String = SOCIETY_ID,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class Announcement(
  @DocumentId val id: String = "",
  val title: String = "",
  val message: String = "",
  val category: String = "GENERAL",
  val targetGroup: String = "ALL_STUDENTS",
  val priority: String = "NORMAL",
  val scheduledDate: String = "",
  val authorName: String = "",
  val authorUid: String = "",
  val published: Boolean = true,
  val societyId: String = SOCIETY_ID,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class UserNotification(
  @DocumentId val id: String = "",
  val recipientUid: String = "",
  val title: String = "",
  val body: String = "",
  val type: String = "ANNOUNCEMENT",
  val read: Boolean = false,
  val societyId: String = SOCIETY_ID,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class StudentPayment(
  @DocumentId val id: String = "",
  val studentUid: String = "",
  val studentName: String = "",
  val registrationNumber: String = "",
  val eventId: String = "",
  val purposeTitle: String = "",
  val amount: Double = 0.0,
  val paymentDate: String = "",
  val transactionRef: String = "",
  val receiptUri: String = "",
  val status: String = "UNPAID",
  val verifiedByUid: String = "",
  val societyId: String = SOCIETY_ID,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class FinanceTransaction(
  @DocumentId val id: String = "",
  val title: String = "",
  val type: String = "INCOME",
  val category: String = "REGISTRATION_FEE",
  val amount: Double = 0.0,
  val transactionDate: String = "",
  val referenceNumber: String = "",
  val relatedEventId: String = "",
  val notes: String = "",
  val recordedByUid: String = "",
  val recordedByName: String = "",
  val societyId: String = SOCIETY_ID,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class GalleryItem(
  @DocumentId val id: String = "",
  val title: String = "",
  val albumCategory: String = "Society Activities",
  val caption: String = "",
  val eventDate: String = "",
  val imageUri: String = "",
  val accentTheme: String = "EMERALD",
  val approved: Boolean = true,
  val uploadedByUid: String = "",
  val societyId: String = SOCIETY_ID,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class SocietyCertificate(
  @DocumentId val id: String = "",
  val certificateCode: String = "",
  val studentUid: String = "",
  val studentName: String = "",
  val registrationNumber: String = "",
  val eventId: String = "",
  val eventTitle: String = "",
  val eventDate: String = "",
  val certificateType: String = "PARTICIPATION",
  val status: String = "PENDING",
  val approverUids: List<String> = emptyList(),
  val rejectionReason: String = "",
  val authorizedSignatory: String = "President & Patron-in-Chief, Young Entomologist Society (UAF)",
  val issuedByUid: String = "",
  val societyId: String = SOCIETY_ID,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class StudentFeedback(
  @DocumentId val id: String = "",
  val studentUid: String = "",
  val studentName: String = "",
  val category: String = "SUGGESTION",
  val subject: String = "",
  val message: String = "",
  val relatedEventTitle: String = "",
  val status: String = "SUBMITTED",
  val adminResponse: String = "",
  val societyId: String = SOCIETY_ID,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class AdminAuditLog(
  @DocumentId val id: String = "",
  val actorUid: String = "",
  val actorName: String = "",
  val actorRole: String = "",
  val action: String = "",
  val targetSummary: String = "",
  val societyId: String = SOCIETY_ID,
  @ServerTimestamp val createdAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class PaymentAccount(
  @DocumentId val id: String = "",
  val accountType: String = "Bank Account",
  val bankOrWalletName: String = "",
  val accountTitle: String = "",
  val accountNumber: String = "",
  val ibanOrBranch: String = "",
  val instructions: String = "",
  val active: Boolean = true,
  val updatedByUid: String = "",
  val societyId: String = SOCIETY_ID,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class BlockedAccount(
  @DocumentId val id: String = "",
  val userId: String = "",
  val email: String = "",
  val fullName: String = "",
  val registrationNumber: String = "",
  val reason: String = "",
  val blockedByUid: String = "",
  val blockedByName: String = "",
  val societyId: String = SOCIETY_ID,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class SocietyConfig(
  @DocumentId val id: String = "GLOBAL_CONFIG",
  val portalFlashNews: String = "Welcome to YES Portal! Complete your profile to issue ID cards.",
  val facebookLink: String = "https://facebook.com/yes.uaf",
  val instagramLink: String = "https://instagram.com/yes.uaf",
  val twitterLink: String = "https://twitter.com/yes_uaf",
  val websiteLink: String = "https://uaf.edu.pk",
  val youtubeLink: String = "https://youtube.com/@yes_uaf",
  val linkedinLink: String = "https://linkedin.com/company/yes-uaf",
  val tiktokLink: String = "https://tiktok.com/@yes_uaf",
  val societyAbout: String = "The Young Entomologist Society is the premier student organization of the Department of Entomology, UAF, dedicated to scientific research, professional development, and community engagement in the field of insect science.",
  val societyEmail: String = "yes@uaf.edu.pk",
  val societyPhone: String = "+92 41 111 222 333",
  val updatedAtByUid: String = "",
  val updatedByActorName: String = "",
  @ServerTimestamp val updatedAt: Timestamp? = null,
)
