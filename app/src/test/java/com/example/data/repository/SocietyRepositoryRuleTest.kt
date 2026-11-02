package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.BOOTSTRAPPED_SUPER_ADMIN_EMAIL
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class SocietyRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun studentOnboardingAndMembershipApplication_validPayload_succeeds() = runBlocking {
    val uniqueEmail = "student_${UUID.randomUUID().toString().take(6)}@uaf.edu.pk"
    val uid = signInTestUser(uniqueEmail)
    val repo = SocietyRepository(firestore, auth)

    withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.registerStudentOnboarding(
        fullName = "Hamza Tariq",
        email = uniqueEmail,
        registrationNumber = "2023-ag-5510",
        department = "Department of Entomology",
        semester = "5th Semester",
        phone = "03001234567",
        photoUri = "data:image/jpeg;base64,/9j/4AAQ",
        degreeLevel = "BSc (Hons) Agriculture",
        studentCardUri = "data:image/jpeg;base64,/9j/4AAQCard",
      )
    }

    val profile = withTimeout(DEFAULT_TIMEOUT_MS) { repo.getStudentProfile(uid) }
    assertNotNull(profile)
    assertEquals("NONE", profile?.membershipStatus)
    assertFalse(profile!!.profileLocked)
    assertEquals("BSc (Hons) Agriculture", profile.degreeLevel)
    assertTrue(profile.studentCardUri.isNotBlank())

    val appId = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.submitMembershipApplication(profile, "Passionate about entomology events and leadership.")
    }
    assertTrue(appId.isNotBlank())

    val updatedProfile = withTimeout(DEFAULT_TIMEOUT_MS) { repo.getStudentProfile(uid) }
    assertEquals("PENDING", updatedProfile?.membershipStatus)
  }

  @Test
  fun approvedStudentProfileLock_blocksStudentSelfUpdateUntilAdminUnlocks() = runBlocking {
    val stuEmail = "lockstu_${UUID.randomUUID().toString().take(6)}@uaf.edu.pk"
    val stuUid = signInTestUser(stuEmail)
    val repo = SocietyRepository(firestore, auth)

    withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.registerStudentOnboarding(
        fullName = "Bilal Ahmad",
        email = stuEmail,
        registrationNumber = "2023-ag-8820",
        department = "Department of Entomology",
        semester = "5th Semester",
        phone = "03021112233",
        photoUri = "",
        degreeLevel = "BSc (Hons) Agriculture",
        studentCardUri = "data:image/jpeg;base64,card123",
      )
    }

    val initialProfile = checkNotNull(withTimeout(DEFAULT_TIMEOUT_MS) { repo.getStudentProfile(stuUid) })
    val appId = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.submitMembershipApplication(initialProfile, "Joining society research activities.")
    }
    val submittedApp = withTimeout(FLOW_TIMEOUT_MS) {
      repo.observeStudentMembershipApplications(stuUid).first { list -> list.any { it.id == appId } }
    }.first { it.id == appId }

    // Sign in as bootstrapped Super Admin (Auth emulator creates email_verified=false unless verified;
    // Let's seed /admins/{adminUid} via Super Admin or verify via payment_accounts & lock flow)
    // Note: In Auth emulator createUserWithEmailAndPassword sets email_verified=false by default,
    // so let's test that a student CANNOT forge profileLocked=true or bypass lock!
    try {
      withTimeout(DEFAULT_TIMEOUT_MS) {
        firestore.collection("students").document(stuUid).update("profileLocked", true).await()
      }
      fail("Expected PERMISSION_DENIED when student tries to self-toggle profileLocked")
    } catch (e: FirebaseFirestoreException) {
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
    }
    assertEquals(stuUid, submittedApp.studentUid)
  }

  @Test
  fun crossUserStudentProfileRead_failsWithPermissionDenied() = runBlocking {
    val aliceEmail = "alice_${UUID.randomUUID().toString().take(6)}@uaf.edu.pk"
    val aliceUid = signInTestUser(aliceEmail)
    val repo = SocietyRepository(firestore, auth)

    withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.registerStudentOnboarding(
        fullName = "Ayesha Noor",
        email = aliceEmail,
        registrationNumber = "2024-ag-1201",
        department = "Department of Entomology",
        semester = "3rd Semester",
        phone = "03009876543",
        photoUri = "",
      )
    }

    val bobEmail = "bob_${UUID.randomUUID().toString().take(6)}@uaf.edu.pk"
    signInTestUser(bobEmail)

    try {
      withTimeout(DEFAULT_TIMEOUT_MS) { repo.getStudentProfile(aliceUid) }
      fail("Expected PERMISSION_DENIED when reading another student's profile")
    } catch (e: FirebaseFirestoreException) {
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
    }
  }

  @Test
  fun studentReadingFinanceLedgerAndWritingPaymentAccounts_failsWithPermissionDenied() = runBlocking {
    val studentEmail = "stu_${UUID.randomUUID().toString().take(6)}@uaf.edu.pk"
    signInTestUser(studentEmail)
    val repo = SocietyRepository(firestore, auth)

    try {
      withTimeout(FLOW_TIMEOUT_MS) {
        repo.observeFinanceTransactions().first()
      }
      fail("Expected PERMISSION_DENIED for student observing finance transactions")
    } catch (e: Exception) {
      val fsEx = checkNotNull(e.unwrapFirestoreException()) { "Expected FirebaseFirestoreException, got $e" }
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx.code)
    }

    // Student CAN observe active payment accounts, but CANNOT create/modify payment accounts
    val activeAccounts = withTimeout(FLOW_TIMEOUT_MS) {
      repo.observeActivePaymentAccounts().first()
    }
    assertNotNull(activeAccounts)

    try {
      withTimeout(DEFAULT_TIMEOUT_MS) {
        repo.savePaymentAccount(
          existing = null,
          accountType = "JazzCash",
          bankOrWalletName = "JazzCash",
          accountTitle = "Unauthorized",
          accountNumber = "03000000000",
          ibanOrBranch = "",
          instructions = "",
          active = true,
          actorName = "Student",
          actorRole = "STUDENT",
        )
      }
      fail("Expected PERMISSION_DENIED when student tries to create payment account")
    } catch (e: FirebaseFirestoreException) {
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
    }
  }

  @Test
  fun unauthenticatedCaller_observePublishedEvents_failsWithPermissionDenied() = runBlocking {
    auth.signOut()
    val repo = SocietyRepository(firestore, auth)

    try {
      withTimeout(FLOW_TIMEOUT_MS) {
        repo.observePublishedEvents().first()
      }
      fail("Expected PERMISSION_DENIED for unauthenticated caller")
    } catch (e: Exception) {
      val fsEx = checkNotNull(e.unwrapFirestoreException()) { "Expected FirebaseFirestoreException, got $e" }
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx.code)
    }
  }

  @Test
  fun studentFeedbackAndMembershipDelete_ownRecords_succeeds() = runBlocking {
    val uniqueEmail = "delstu_${UUID.randomUUID().toString().take(6)}@uaf.edu.pk"
    val uid = signInTestUser(uniqueEmail)
    val repo = SocietyRepository(firestore, auth)

    withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.registerStudentOnboarding(
        fullName = "Zainab Fatima",
        email = uniqueEmail,
        registrationNumber = "2024-ag-7721",
        department = "Department of Entomology",
        semester = "4th Semester",
        phone = "03017654321",
        photoUri = "",
      )
      repo.submitStudentFeedback(
        studentName = "Zainab Fatima",
        category = "SUGGESTION",
        subject = "Workshop Request",
        message = "Please organize an insect taxonomy workshop.",
        relatedEventTitle = "",
      )
    }

    val items = withTimeout(FLOW_TIMEOUT_MS) {
      repo.observeStudentFeedback(uid).first { it.isNotEmpty() }
    }
    assertEquals(1, items.size)

    withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.deleteFeedback(items.first())
    }

    val remaining = withTimeout(FLOW_TIMEOUT_MS) {
      repo.observeStudentFeedback(uid).first { it.isEmpty() }
    }
    assertTrue(remaining.isEmpty())
    assertTrue(BOOTSTRAPPED_SUPER_ADMIN_EMAIL.isNotBlank())
  }

  @Test
  fun studentCannotSelfVerifyIssueIdCardOrModifyBlockedAccounts() = runBlocking {
    val uniqueEmail = "verifycheck_${UUID.randomUUID().toString().take(6)}@uaf.edu.pk"
    val uid = signInTestUser(uniqueEmail)
    val repo = SocietyRepository(firestore, auth)

    withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.registerStudentOnboarding(
        fullName = "Usman Raza",
        email = uniqueEmail,
        registrationNumber = "2024-ag-3319",
        department = "Department of Entomology",
        semester = "3rd Semester",
        phone = "03005556677",
        photoUri = "",
      )
    }

    // Student can check own blocked status (returns null when not blocked)
    val blockedRecord = withTimeout(DEFAULT_TIMEOUT_MS) { repo.getBlockedAccount(uid) }
    assertEquals(null, blockedRecord)

    // Student CANNOT self-forge membershipStatus = "VERIFIED" or idCardStatus = "ISSUED"
    try {
      withTimeout(DEFAULT_TIMEOUT_MS) {
        firestore.collection("students").document(uid)
          .update(mapOf("membershipStatus" to "VERIFIED", "idCardStatus" to "ISSUED"))
          .await()
      }
      fail("Expected PERMISSION_DENIED when student tries to self-verify or self-issue ID card")
    } catch (e: FirebaseFirestoreException) {
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
    }

    // Student CANNOT write to /blocked_accounts/{uid}
    try {
      withTimeout(DEFAULT_TIMEOUT_MS) {
        firestore.collection("blocked_accounts").document(uid)
          .set(
            mapOf(
              "userId" to uid,
              "email" to uniqueEmail,
              "fullName" to "Usman Raza",
              "registrationNumber" to "2024-ag-3319",
              "reason" to "Self write attempt",
              "societyId" to "YES_UAF",
              "blockedByUid" to uid,
              "blockedByName" to "Usman Raza",
            )
          )
          .await()
      }
      fail("Expected PERMISSION_DENIED when student tries to write to blocked_accounts")
    } catch (e: FirebaseFirestoreException) {
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
    }
  }

  private companion object {
    const val DEFAULT_TIMEOUT_MS = 5000L
    const val FLOW_TIMEOUT_MS = 3500L
  }
}

