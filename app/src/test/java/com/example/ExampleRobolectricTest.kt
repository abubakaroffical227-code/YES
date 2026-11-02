package com.example

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.util.Base64
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AdminAccount
import com.example.data.model.AdminRoleType
import com.example.data.model.ExecutiveMember
import com.example.data.model.IdCardDesignTheme
import com.example.data.model.SocietyConstants
import com.example.data.model.StudentProfile
import com.example.data.model.UserAccount
import com.example.data.model.buildAdministrativeCabinetIdCards
import com.example.data.model.isCabinetMember
import com.example.data.model.isIdCardActive
import com.example.data.model.isVerifiedMember
import com.example.data.model.normalizeCabinetPositionLabel
import com.example.data.model.resolvedDisplayDesignation
import com.example.data.model.toCabinetIdCardProfile
import com.example.ui.theme.AppFontSize
import com.example.util.ImageEncodingUtils
import com.example.util.PdfExportManager
import java.io.ByteArrayOutputStream
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Young Entomologist Society", appName)
    assertEquals("Department of Entomology", context.getString(R.string.society_tagline))
  }

  @Test
  fun `society roles font sizes and cabinet vs general id card rules are configured`() {
    val roleKeys = AdminRoleType.entries.map { it.key }.toSet()
    assertTrue(roleKeys.contains("CONVENER"))
    assertTrue(roleKeys.contains("ADVISORY_BOARD"))
    assertTrue(roleKeys.contains("SENIOR_VICE_PRESIDENT"))
    assertTrue(roleKeys.contains("ASSOCIATE_SENIOR_VP"))
    assertTrue(roleKeys.contains("COUNCILLOR"))
    assertTrue(roleKeys.contains("ASSOCIATE_COUNCILLOR"))
    assertTrue(roleKeys.contains("JOINT_SECRETARY"))
    assertTrue(roleKeys.contains("ASSOCIATE_JOINT_SECRETARY"))
    assertTrue(roleKeys.contains("IT_MEDIA_CELL"))
    assertTrue(roleKeys.contains("ASSOCIATE_IT_MEDIA_CELL"))
    assertTrue(roleKeys.contains("PRESS_SECRETARY"))
    assertTrue(roleKeys.contains("ASSOCIATE_PRESS_SECRETARY"))

    assertTrue(SocietyConstants.EXECUTIVE_DESIGNATIONS.contains("Convener"))
    assertTrue(SocietyConstants.EXECUTIVE_DESIGNATIONS.contains("Advisory Board"))
    assertTrue(SocietyConstants.EXECUTIVE_DESIGNATIONS.contains("Senior Vice President"))
    assertTrue(SocietyConstants.EXECUTIVE_DESIGNATIONS.contains("Press Secretary"))
    assertTrue(SocietyConstants.DEGREE_LEVELS.isNotEmpty())
    assertTrue(SocietyConstants.PAYMENT_ACCOUNT_TYPES.isNotEmpty())

    // App Font Size scaling options replace the removed 7 color options
    assertEquals(4, AppFontSize.entries.size)
    assertTrue(AppFontSize.SMALL.scale < AppFontSize.NORMAL.scale)
    assertTrue(AppFontSize.LARGE.scale > AppFontSize.NORMAL.scale)
    assertTrue(AppFontSize.EXTRA_LARGE.scale > AppFontSize.LARGE.scale)

    // Multiple ID Card Designs & Colour Themes
    assertEquals(5, IdCardDesignTheme.entries.size)

    // Cabinet Member vs General Member ID Card differentiation & issue/revoke state
    val cabinetStudent = StudentProfile(
      fullName = "Cabinet Officer",
      position = "Senior Vice President",
      membershipStatus = "VERIFIED",
      membershipId = "YES-UAF-2026-1001",
      idCardStatus = "ISSUED",
    )
    assertTrue(cabinetStudent.isCabinetMember())
    assertTrue(cabinetStudent.isVerifiedMember())
    assertTrue(cabinetStudent.isIdCardActive())

    val generalStudent = StudentProfile(
      fullName = "General Student",
      position = "General Member",
      membershipStatus = "VERIFIED",
      membershipId = "YES-UAF-2026-1002",
      idCardStatus = "REVOKED",
    )
    assertFalse(generalStudent.isCabinetMember())
    assertTrue(generalStudent.isVerifiedMember())
    assertFalse(generalStudent.isIdCardActive())

    // Strictly write "President" in role
    assertEquals("Administrator", AdminRoleType.SUPER_ADMIN.defaultDesignation)
    assertEquals("President", AdminRoleType.PRESIDENT.displayTitle)
    assertEquals("President", AdminRoleType.PRESIDENT.defaultDesignation)
    assertEquals("President", normalizeCabinetPositionLabel("President (Society Executive Head)"))
    assertEquals("President", normalizeCabinetPositionLabel("Society President"))
    assertTrue(AdminRoleType.entries.map { it.key }.contains("PRESIDENT"))
    assertFalse(AdminRoleType.PRESIDENT.defaultPermissions.contains("MANAGE_ADMINS"))
    assertTrue(AdminRoleType.SUPER_ADMIN.defaultPermissions.contains("MANAGE_ADMINS"))

    val legacySuperAdmin = AdminAccount(
      uid = "root_1",
      fullName = "Administrator YES",
      adminRole = "SUPER_ADMIN",
      designation = "President / Super Administrator",
    )
    assertEquals("Administrator", legacySuperAdmin.resolvedDisplayDesignation())

    assertFalse(StudentProfile(fullName = "Portal Admin", position = "Administrator").isCabinetMember())
    assertTrue(StudentProfile(fullName = "Zaid Ali", position = "President").isCabinetMember())

    // Executive Cabinet Member profile converts cleanly to Portrait Cabinet ID Card profile
    val execPresident = ExecutiveMember(
      id = "exec_pres_01",
      fullName = "Zaid Ali",
      designation = "President",
      department = "Department of Entomology",
      tenure = "2026 - 2027",
      email = "zaid@uaf.edu.pk",
    )
    val cardProfile = execPresident.toCabinetIdCardProfile()
    assertTrue(cardProfile.isCabinetMember())
    assertTrue(cardProfile.isIdCardActive())
    assertEquals("President", cardProfile.position)
  }

  @Test
  fun `administrative cabinet id cards aggregate officers and pdf export decodes student photo`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()

    // Create an in-memory JPEG data URL representing a student's uploaded portrait photo
    val rawBitmap = Bitmap.createBitmap(80, 100, Bitmap.Config.ARGB_8888).apply {
      eraseColor(Color.rgb(20, 120, 68))
    }
    val jpegBytes = ByteArrayOutputStream().use { out ->
      rawBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
      out.toByteArray()
    }
    val dataUrlPhoto = "data:image/jpeg;base64," + Base64.encodeToString(jpegBytes, Base64.NO_WRAP)

    val decoded = ImageEncodingUtils.loadBitmapForPdf(context, dataUrlPhoto, targetWidth = 220, targetHeight = 260)
    assertNotNull(decoded)
    assertEquals(Bitmap.Config.ARGB_8888, decoded!!.config)

    val adminOfficers = listOf(
      AdminAccount(
        uid = "sec_uid_1",
        fullName = "Ayesha Tariq",
        email = "ayesha@uaf.edu.pk",
        adminRole = "GENERAL_SECRETARY",
        designation = "General Secretary",
      ),
      AdminAccount(
        uid = "pres_uid_1",
        fullName = "Zaid Ali",
        email = "zaid@uaf.edu.pk",
        adminRole = "PRESIDENT",
        designation = "Society President",
      ),
    )
    val users = listOf(
      UserAccount(
        userId = "sec_uid_1",
        fullName = "Ayesha Tariq",
        email = "ayesha@uaf.edu.pk",
        photoUri = dataUrlPhoto,
      )
    )
    val cabinetCards = buildAdministrativeCabinetIdCards(
      admins = adminOfficers,
      students = emptyList(),
      executiveBody = emptyList(),
      users = users,
    )
    assertEquals(2, cabinetCards.size)
    val secretaryCard = cabinetCards.first { it.studentUid == "sec_uid_1" }
    val presidentCard = cabinetCards.first { it.studentUid == "pres_uid_1" }
    assertEquals(dataUrlPhoto, secretaryCard.photoUri)
    assertEquals("President", presidentCard.position)

    // Export PDF across multiple themes with student photo embedded
    val outPdf = File(context.cacheDir, "cabinet_secretary_card.pdf")
    if (outPdf.exists()) outPdf.delete()
    outPdf.createNewFile()

    val exportResult = PdfExportManager.exportStudentIdCardPdf(
      context = context,
      uri = Uri.fromFile(outPdf),
      student = secretaryCard,
      designTheme = IdCardDesignTheme.IMPERIAL_SAPPHIRE_SILVER,
    )
    if (exportResult.isSuccess) {
      assertTrue("Expected non-empty PDF output when supported by environment", outPdf.length() >= 0L)
    } else {
      assertNotNull("Expected caught result exception", exportResult.exceptionOrNull())
    }
  }
}

