package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.example.R
import com.example.data.model.IdCardDesignTheme
import com.example.data.model.SocietyCertificate
import com.example.data.model.StudentProfile
import com.example.data.model.hasAllocatedRole
import com.example.data.model.isCabinetMember
import com.example.data.model.normalizeCabinetPositionLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PdfExportManager {

  private data class PdfCardPalette(
    val pageBackdrop: Int,
    val cardTop: Int,
    val cardBottom: Int,
    val headerBand: Int,
    val accentPrimary: Int,
    val accentSecondary: Int,
    val ribbonText: Int,
    val primaryText: Int,
    val secondaryText: Int,
    val specBoxFill: Int,
    val specLabel: Int,
    val specValue: Int,
    val slotFill: Int,
    val isLightCard: Boolean,
  )

  private fun paletteFor(theme: IdCardDesignTheme): PdfCardPalette = when (theme) {
    IdCardDesignTheme.ROYAL_EMERALD_GOLD -> PdfCardPalette(
      pageBackdrop = Color.rgb(239, 246, 241),
      cardTop = Color.rgb(6, 46, 25),
      cardBottom = Color.rgb(3, 25, 13),
      headerBand = Color.rgb(12, 68, 38),
      accentPrimary = Color.rgb(218, 175, 55),
      accentSecondary = Color.rgb(246, 211, 101),
      ribbonText = Color.rgb(28, 16, 0),
      primaryText = Color.WHITE,
      secondaryText = Color.rgb(209, 242, 222),
      specBoxFill = Color.rgb(10, 58, 33),
      specLabel = Color.rgb(246, 211, 101),
      specValue = Color.WHITE,
      slotFill = Color.rgb(2, 18, 9),
      isLightCard = false,
    )
    IdCardDesignTheme.IMPERIAL_SAPPHIRE_SILVER -> PdfCardPalette(
      pageBackdrop = Color.rgb(238, 243, 250),
      cardTop = Color.rgb(11, 34, 68),
      cardBottom = Color.rgb(6, 19, 41),
      headerBand = Color.rgb(18, 54, 104),
      accentPrimary = Color.rgb(125, 196, 255),
      accentSecondary = Color.rgb(220, 238, 255),
      ribbonText = Color.rgb(6, 24, 48),
      primaryText = Color.WHITE,
      secondaryText = Color.rgb(198, 224, 252),
      specBoxFill = Color.rgb(15, 44, 86),
      specLabel = Color.rgb(156, 210, 255),
      specValue = Color.WHITE,
      slotFill = Color.rgb(5, 15, 32),
      isLightCard = false,
    )
    IdCardDesignTheme.CRIMSON_VELVET_CHAMPAGNE -> PdfCardPalette(
      pageBackdrop = Color.rgb(249, 241, 242),
      cardTop = Color.rgb(78, 14, 28),
      cardBottom = Color.rgb(42, 7, 15),
      headerBand = Color.rgb(106, 20, 39),
      accentPrimary = Color.rgb(235, 195, 118),
      accentSecondary = Color.rgb(252, 226, 174),
      ribbonText = Color.rgb(44, 10, 16),
      primaryText = Color.WHITE,
      secondaryText = Color.rgb(248, 218, 224),
      specBoxFill = Color.rgb(92, 18, 34),
      specLabel = Color.rgb(252, 226, 174),
      specValue = Color.WHITE,
      slotFill = Color.rgb(28, 4, 10),
      isLightCard = false,
    )
    IdCardDesignTheme.OBSIDIAN_EXECUTIVE_AMBER -> PdfCardPalette(
      pageBackdrop = Color.rgb(242, 243, 245),
      cardTop = Color.rgb(26, 30, 36),
      cardBottom = Color.rgb(13, 16, 20),
      headerBand = Color.rgb(38, 44, 52),
      accentPrimary = Color.rgb(245, 166, 35),
      accentSecondary = Color.rgb(255, 206, 115),
      ribbonText = Color.rgb(24, 15, 0),
      primaryText = Color.WHITE,
      secondaryText = Color.rgb(210, 218, 226),
      specBoxFill = Color.rgb(34, 40, 48),
      specLabel = Color.rgb(255, 206, 115),
      specValue = Color.WHITE,
      slotFill = Color.rgb(8, 10, 12),
      isLightCard = false,
    )
    IdCardDesignTheme.BOTANICAL_IVORY_FOREST -> PdfCardPalette(
      pageBackdrop = Color.rgb(242, 248, 244),
      cardTop = Color.rgb(255, 255, 255),
      cardBottom = Color.rgb(247, 252, 249),
      headerBand = Color.rgb(225, 244, 233),
      accentPrimary = Color.rgb(14, 90, 52),
      accentSecondary = Color.rgb(31, 122, 76),
      ribbonText = Color.WHITE,
      primaryText = Color.rgb(14, 34, 23),
      secondaryText = Color.rgb(31, 105, 65),
      specBoxFill = Color.rgb(237, 247, 241),
      specLabel = Color.rgb(22, 102, 60),
      specValue = Color.rgb(14, 34, 23),
      slotFill = Color.rgb(198, 226, 210),
      isLightCard = true,
    )
  }

  fun writeCertificatePdfToUri(
    context: Context,
    uri: Uri,
    certificate: SocietyCertificate,
  ): Result<Unit> = runCatching {
    val pdf = PdfDocument()
    // Landscape A4-proportioned page (842 x 595 pt)
    val pageInfo = PdfDocument.PageInfo.Builder(842, 595, 1).create()
    val page = pdf.startPage(pageInfo)
    val canvas: Canvas = page.canvas

    // Warm ivory parchment background
    canvas.drawColor(Color.rgb(252, 251, 246))

    // Outer royal emerald frame
    val outerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.STROKE
      strokeWidth = 8f
      color = Color.rgb(10, 66, 38)
    }
    canvas.drawRoundRect(RectF(20f, 20f, 822f, 575f), 14f, 14f, outerBorderPaint)

    // Middle antique gold ornamental frame
    val goldBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.STROKE
      strokeWidth = 2.5f
      color = Color.rgb(184, 134, 11)
    }
    canvas.drawRoundRect(RectF(32f, 32f, 810f, 563f), 10f, 10f, goldBorderPaint)

    // Subtle inner emerald hairline
    val innerHairline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.STROKE
      strokeWidth = 1f
      color = Color.rgb(24, 110, 66)
    }
    canvas.drawRoundRect(RectF(40f, 40f, 802f, 555f), 8f, 8f, innerHairline)

    // Top decorative emerald & gold header band
    val bandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.rgb(236, 247, 240)
    }
    canvas.drawRoundRect(RectF(48f, 48f, 794f, 156f), 10f, 10f, bandPaint)

    // Top-LEFT Official Society Logo (YES)
    val yesBmp = BitmapFactory.decodeResource(context.resources, R.drawable.img_yes_logo)
    if (yesBmp != null) {
      val scaledYes = Bitmap.createScaledBitmap(yesBmp, 84, 84, true)
      canvas.drawBitmap(scaledYes, 66f, 60f, null)
    }

    // Top-RIGHT Official University Logo (UAF)
    val uafBmp = BitmapFactory.decodeResource(context.resources, R.drawable.img_uaf_logo)
    if (uafBmp != null) {
      val scaledUaf = Bitmap.createScaledBitmap(uafBmp, 84, 84, true)
      canvas.drawBitmap(scaledUaf, 692f, 60f, null)
    }

    // Society, Department & University Header
    val socPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      textAlign = Paint.Align.CENTER
      color = Color.rgb(14, 90, 52)
      typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
      textSize = 19f
    }
    canvas.drawText("YOUNG ENTOMOLOGIST SOCIETY (YES)", 421f, 82f, socPaint)

    val deptPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      textAlign = Paint.Align.CENTER
      color = Color.rgb(160, 108, 8)
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
      textSize = 13.5f
    }
    canvas.drawText("DEPARTMENT OF ENTOMOLOGY", 421f, 105f, deptPaint)

    val uniPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      textAlign = Paint.Align.CENTER
      color = Color.rgb(10, 66, 38)
      typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
      textSize = 17f
    }
    canvas.drawText("UNIVERSITY OF AGRICULTURE, FAISALABAD", 421f, 132f, uniPaint)

    // Gold Ribbon Pill for Certificate Type
    val ribbonBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.rgb(10, 66, 38)
    }
    canvas.drawRoundRect(RectF(220f, 174f, 622f, 216f), 21f, 21f, ribbonBg)
    canvas.drawRoundRect(RectF(220f, 174f, 622f, 216f), 21f, 21f, goldBorderPaint)

    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      textAlign = Paint.Align.CENTER
      color = Color.rgb(255, 226, 154)
      typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
      textSize = 21f
    }
    canvas.drawText("CERTIFICATE OF ${certificate.certificateType}", 421f, 202f, titlePaint)

    val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      textAlign = Paint.Align.CENTER
      color = Color.rgb(42, 58, 48)
      typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
      textSize = 15f
    }
    canvas.drawText("This official academic society credential is proudly conferred upon", 421f, 250f, bodyPaint)

    val recipientPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      textAlign = Paint.Align.CENTER
      color = Color.rgb(10, 61, 34)
      typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC)
      textSize = 33f
    }
    canvas.drawText(certificate.studentName, 421f, 294f, recipientPaint)
    canvas.drawLine(205f, 306f, 637f, 306f, goldBorderPaint)

    val regPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      textAlign = Paint.Align.CENTER
      color = Color.rgb(55, 75, 63)
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
      textSize = 13f
    }
    canvas.drawText(
      "University Registration No: ${certificate.registrationNumber}",
      421f,
      330f,
      regPaint,
    )

    canvas.drawText(
      "in recognition of commendable contribution and verified participation in",
      421f,
      362f,
      bodyPaint,
    )

    val eventPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      textAlign = Paint.Align.CENTER
      color = Color.rgb(14, 90, 52)
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
      textSize = 20f
    }
    canvas.drawText(certificate.eventTitle, 421f, 394f, eventPaint)

    val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      textAlign = Paint.Align.CENTER
      color = Color.rgb(68, 86, 75)
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
      textSize = 12.5f
    }
    canvas.drawText(
      "Held on ${certificate.eventDate} • Department of Entomology, University of Agriculture, Faisalabad",
      421f,
      418f,
      metaPaint,
    )

    // Scannable QR Code in bottom-left
    val qrBmp = QrCodeGenerator.createBitmap("YES-VERIFY:${certificate.certificateCode}", 88)
    canvas.drawBitmap(qrBmp, 62f, 444f, null)

    val leftSmallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      textAlign = Paint.Align.LEFT
      color = Color.rgb(55, 75, 63)
      textSize = 10.5f
    }
    canvas.drawText("Verification Code: ${certificate.certificateCode}", 160f, 492f, leftSmallPaint)
    canvas.drawText("Department of Entomology • YES UAF", 160f, 508f, leftSmallPaint)
    canvas.drawText("Scan QR to verify official issuance", 160f, 524f, leftSmallPaint)

    // Dual Signature Blocks (Center-Right & Far-Right)
    val sigLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.rgb(14, 90, 52)
      strokeWidth = 1.4f
    }
    canvas.drawLine(390f, 502f, 560f, 502f, sigLinePaint)
    canvas.drawLine(595f, 502f, 775f, 502f, sigLinePaint)

    val sigTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      textAlign = Paint.Align.CENTER
      color = Color.rgb(14, 90, 52)
      typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
      textSize = 12f
    }
    val sigSubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      textAlign = Paint.Align.CENTER
      color = Color.rgb(75, 92, 82)
      textSize = 10.5f
    }
    canvas.drawText("Convener / Patron", 475f, 518f, sigTitlePaint)
    canvas.drawText("Department of Entomology, UAF", 475f, 533f, sigSubPaint)

    canvas.drawText("Authorized Signatory", 685f, 518f, sigTitlePaint)
    canvas.drawText(certificate.authorizedSignatory.take(36), 685f, 533f, sigSubPaint)

    pdf.finishPage(page)
    context.contentResolver.openOutputStream(uri)?.use { out ->
      pdf.writeTo(out)
    } ?: error("Unable to open output stream for Certificate PDF")
    pdf.close()
  }

  /**
   * Suspending PDF exporter that resolves any student photograph (`data:image`, `https://` Google
   * avatar, `content://`, or `file://`) into a software `ARGB_8888` bitmap BEFORE drawing onto the
   * [PdfDocument] page, guaranteeing the student's photograph is always rendered in the downloaded PDF.
   */
  suspend fun exportStudentIdCardPdf(
    context: Context,
    uri: Uri,
    student: StudentProfile,
    designTheme: IdCardDesignTheme = IdCardDesignTheme.defaultFor(student),
  ): Result<Unit> = withContext(Dispatchers.IO) {
    val loadedPhoto = ImageEncodingUtils.loadBitmapForPdf(
      context = context,
      imageSource = student.photoUri,
      targetWidth = 224,
      targetHeight = 260,
    )
    writeStudentIdCardPdfToUri(
      context = context,
      uri = uri,
      student = student,
      designTheme = designTheme,
      preloadedPhoto = loadedPhoto,
    )
  }

  fun writeStudentIdCardPdfToUri(
    context: Context,
    uri: Uri,
    student: StudentProfile,
    designTheme: IdCardDesignTheme = IdCardDesignTheme.defaultFor(student),
    preloadedPhoto: Bitmap? = null,
  ): Result<Unit> = runCatching {
    val pdf = PdfDocument()
    try {
      val pageInfo = PdfDocument.PageInfo.Builder(380, 600, 1).create()
      val page = pdf.startPage(pageInfo)
      val canvas: Canvas = page.canvas

      val resolvedPhoto = preloadedPhoto ?: ImageEncodingUtils.decodeDataUrlOrNull(student.photoUri)?.let {
        ImageEncodingUtils.prepareCenterCroppedSoftwareBitmap(it, 224, 260)
      }

      drawThemedPortraitIdCardPdf(
        context = context,
        canvas = canvas,
        student = student,
        designTheme = designTheme,
        studentPhoto = resolvedPhoto,
      )

      pdf.finishPage(page)
      context.contentResolver.openOutputStream(uri)?.use { out ->
        pdf.writeTo(out)
      } ?: error("Unable to open output stream for ID Card PDF")
    } finally {
      runCatching { pdf.close() }
    }
  }

  private fun drawThemedPortraitIdCardPdf(
    context: Context,
    canvas: Canvas,
    student: StudentProfile,
    designTheme: IdCardDesignTheme,
    studentPhoto: Bitmap?,
  ) {
    val isCabinet = student.isCabinetMember()
    val palette = paletteFor(designTheme)
    val hasAllocatedRole = student.hasAllocatedRole()
    val cleanRole = if (hasAllocatedRole) {
      normalizeCabinetPositionLabel(student.position)
    } else {
      ""
    }

    canvas.drawColor(palette.pageBackdrop)

    val cardRect = RectF(16f, 16f, 364f, 584f)
    val cardClipPath = Path().apply {
      addRoundRect(cardRect, 22f, 22f, Path.Direction.CW)
    }

    // Card Vertical Gradient Fill
    val cardFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      shader = LinearGradient(
        190f,
        16f,
        190f,
        584f,
        palette.cardTop,
        palette.cardBottom,
        Shader.TileMode.CLAMP,
      )
    }
    canvas.save()
    canvas.clipPath(cardClipPath)
    canvas.drawRect(cardRect, cardFillPaint)

    // Theme-specific Decorative Header Geometry
    val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.headerBand }
    when (designTheme) {
      IdCardDesignTheme.IMPERIAL_SAPPHIRE_SILVER -> {
        val slantPath = Path().apply {
          moveTo(16f, 16f)
          lineTo(364f, 16f)
          lineTo(364f, 118f)
          lineTo(16f, 102f)
          close()
        }
        canvas.drawPath(slantPath, headerPaint)
      }
      IdCardDesignTheme.CRIMSON_VELVET_CHAMPAGNE -> {
        canvas.drawOval(RectF(-30f, -52f, 410f, 126f), headerPaint)
      }
      IdCardDesignTheme.OBSIDIAN_EXECUTIVE_AMBER -> {
        canvas.drawRect(16f, 16f, 364f, 106f, headerPaint)
        val sideStripe = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.accentPrimary }
        canvas.drawRect(16f, 16f, 22f, 584f, sideStripe)
      }
      else -> {
        canvas.drawRect(16f, 16f, 364f, 106f, headerPaint)
      }
    }
    canvas.restore()

    val borderStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.STROKE
      strokeWidth = if (isCabinet) 2.5f else 2f
      color = palette.accentPrimary
    }

    // Top Lanyard Slot
    val slotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.slotFill }
    val slotRect = RectF(162f, 25f, 218f, 32f)
    canvas.drawRoundRect(slotRect, 4f, 4f, slotPaint)
    canvas.drawRoundRect(slotRect, 4f, 4f, Paint(borderStroke).apply { strokeWidth = 1f })

    // Society Logo on LEFT (x = 28f) & University Logo on RIGHT (x = 302f) in crisp circular medallions
    val logoCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    canvas.drawCircle(53f, 68f, 25f, logoCirclePaint)
    canvas.drawCircle(327f, 68f, 25f, logoCirclePaint)

    val logoOptions = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
    BitmapFactory.decodeResource(context.resources, R.drawable.img_yes_logo, logoOptions)?.let { bmp ->
      val softBmp = ImageEncodingUtils.ensureSoftwareArgbBitmap(bmp)
      canvas.drawBitmap(Bitmap.createScaledBitmap(softBmp, 44, 44, true), 31f, 46f, Paint(Paint.FILTER_BITMAP_FLAG))
    }
    BitmapFactory.decodeResource(context.resources, R.drawable.img_uaf_logo, logoOptions)?.let { bmp ->
      val softBmp = ImageEncodingUtils.ensureSoftwareArgbBitmap(bmp)
      canvas.drawBitmap(Bitmap.createScaledBitmap(softBmp, 44, 44, true), 305f, 46f, Paint(Paint.FILTER_BITMAP_FLAG))
    }

    val hTitle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      textAlign = Paint.Align.CENTER
      color = palette.primaryText
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
      textSize = 11.2f
    }
    canvas.drawText("YOUNG ENTOMOLOGIST SOCIETY", 190f, 61f, hTitle)

    val hSub = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      textAlign = Paint.Align.CENTER
      color = if (palette.isLightCard) palette.secondaryText else palette.accentSecondary
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
      textSize = 9.2f
    }
    canvas.drawText("DEPARTMENT OF ENTOMOLOGY • UAF", 190f, 77f, hSub)

    val hTier = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      textAlign = Paint.Align.CENTER
      color = palette.secondaryText
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
      textSize = 8.2f
    }
    canvas.drawText(
      if (isCabinet) "OFFICIAL CABINET IDENTITY CARD" else "OFFICIAL MEMBER IDENTITY CARD",
      190f,
      92f,
      hTier,
    )

    // Role / Position Banner (Only drawn when an allocated role exists)
    if (hasAllocatedRole) {
      val plaquePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(
          16f,
          104f,
          364f,
          140f,
          palette.accentPrimary,
          palette.accentSecondary,
          Shader.TileMode.CLAMP,
        )
      }
      canvas.drawRect(16f, 104f, 364f, 140f, plaquePaint)
      val roleTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = palette.ribbonText
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textSize = 12.5f
      }
      canvas.drawText("★  ${cleanRole.uppercase().take(30)}  ★", 190f, 127f, roleTextPaint)
    }

    // Centered Portrait Student Photograph Frame (Clipped Rounded Rect + ARGB_8888 Bitmap)
    val photoRect = RectF(134f, 152f, 246f, 282f)
    val photoBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.specBoxFill }
    canvas.drawRoundRect(photoRect, 16f, 16f, photoBg)

    if (studentPhoto != null && !studentPhoto.isRecycled) {
      val photoClip = Path().apply {
        addRoundRect(photoRect, 16f, 16f, Path.Direction.CW)
      }
      val bmpPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
      canvas.save()
      canvas.clipPath(photoClip)
      canvas.drawBitmap(studentPhoto, null, photoRect, bmpPaint)
      canvas.restore()
    } else {
      val initialsPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = if (palette.isLightCard) palette.accentPrimary else palette.accentSecondary
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textSize = 30f
      }
      val initials = student.fullName
        .split(" ")
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .take(2)
        .joinToString("")
      canvas.drawText(initials.ifBlank { "YES" }, 190f, 228f, initialsPaint)
    }
    canvas.drawRoundRect(photoRect, 16f, 16f, borderStroke)

    // Centered Student Full Name (No Email, No Credential ID, No Degree Name)
    val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      textAlign = Paint.Align.CENTER
      color = palette.primaryText
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
      textSize = 17.5f
    }
    canvas.drawText(student.fullName.take(28), 190f, 310f, namePaint)

    // Decorative Accent Divider under Name
    val accentLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = palette.accentPrimary
      strokeWidth = 2f
    }
    canvas.drawLine(125f, 320f, 255f, 320f, accentLine)

    // Clean Specification Box (Role, Registration Number, Department, Session/Semester ONLY)
    val specBox = RectF(30f, 332f, 350f, 438f)
    val specBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.specBoxFill }
    canvas.drawRoundRect(specBox, 14f, 14f, specBg)
    canvas.drawRoundRect(specBox, 14f, 14f, Paint(borderStroke).apply { strokeWidth = 1.1f })

    val rowLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = palette.specLabel
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
      textSize = 9.5f
    }
    val rowValPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      textAlign = Paint.Align.RIGHT
      color = palette.specValue
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
      textSize = 9.8f
    }

    val sessionOrSemesterValue = if (isCabinet) {
      student.membershipValidUntil.ifBlank { student.semester.ifBlank { "2026 - 2027" } }
    } else {
      student.semester.ifBlank { student.membershipValidUntil.ifBlank { "Active Session" } }
    }

    val specRows = mutableListOf<Pair<String, String>>().apply {
      if (hasAllocatedRole) {
        add("ROLE" to cleanRole.take(28))
      }
      add("REG. NUMBER" to student.registrationNumber.ifBlank { "UAF-YES" }.take(26))
      add("DEPARTMENT" to student.department.ifBlank { "Department of Entomology" }.take(28))
      add((if (hasAllocatedRole) "TENURE / VALID" else "SEMESTER") to sessionOrSemesterValue.take(26))
    }

    var rowY = 354f
    specRows.forEach { (lbl, v) ->
      canvas.drawText(lbl, 44f, rowY, rowLabelPaint)
      canvas.drawText(v, 336f, rowY, rowValPaint)
      rowY += 23f
    }

    // Centered Scannable QR Code at Bottom
    val qrPayload = if (isCabinet) {
      "YES-CABINET:$cleanRole:${student.registrationNumber}:${student.studentUid}"
    } else {
      "YES-MEMBER:${student.registrationNumber}:${student.studentUid}"
    }
    val qrBmp = ImageEncodingUtils.ensureSoftwareArgbBitmap(QrCodeGenerator.createBitmap(qrPayload, 94))
    val qrBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    val qrRect = RectF(139f, 448f, 241f, 550f)
    canvas.drawRoundRect(qrRect, 12f, 12f, qrBg)
    canvas.drawBitmap(qrBmp, 143f, 452f, Paint(Paint.FILTER_BITMAP_FLAG))
    canvas.drawRoundRect(qrRect, 12f, 12f, Paint(borderStroke).apply { strokeWidth = 1.5f })

    val footerText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      textAlign = Paint.Align.CENTER
      color = if (palette.isLightCard) palette.accentPrimary else palette.accentSecondary
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
      textSize = 8.4f
    }
    canvas.drawText(
      "YOUNG ENTOMOLOGIST SOCIETY • UNIVERSITY OF AGRICULTURE, FAISALABAD",
      190f,
      570f,
      footerText,
    )

    canvas.drawRoundRect(cardRect, 22f, 22f, borderStroke)
  }

  fun writeSummaryReportPdfToUri(
    context: Context,
    uri: Uri,
    reportTitle: String,
    subtitle: String,
    headers: List<String>,
    rows: List<List<String>>,
  ): Result<Unit> = runCatching {
    val pdf = PdfDocument()
    val pageWidth = 595
    val pageHeight = 842
    val rowsPerPage = 24
    val chunks = if (rows.isEmpty()) listOf(emptyList()) else rows.chunked(rowsPerPage)

    chunks.forEachIndexed { pageIdx, pageRows ->
      val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIdx + 1).create()
      val page = pdf.startPage(pageInfo)
      val canvas = page.canvas
      canvas.drawColor(Color.WHITE)

      val bannerPaint = Paint().apply { color = Color.rgb(14, 90, 52) }
      canvas.drawRect(0f, 0f, pageWidth.toFloat(), 86f, bannerPaint)

      val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textSize = 18f
      }
      canvas.drawText("Young Entomologist Society (YES) — Department of Entomology", 28f, 38f, headerTextPaint)

      val subBannerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(210, 245, 224)
        textSize = 12f
      }
      canvas.drawText("$reportTitle • $subtitle (Page ${pageIdx + 1}/${chunks.size})", 28f, 62f, subBannerPaint)

      val colCount = headers.size.coerceAtLeast(1)
      val colWidth = (pageWidth - 56f) / colCount
      var y = 118f

      val tableHeaderBg = Paint().apply { color = Color.rgb(231, 240, 234) }
      canvas.drawRect(28f, y - 18f, pageWidth - 28f, y + 8f, tableHeaderBg)

      val colHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(14, 90, 52)
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textSize = 10.5f
      }
      headers.forEachIndexed { i, h ->
        canvas.drawText(h.take(22), 34f + i * colWidth, y, colHeaderPaint)
      }

      y += 26f
      val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(30, 45, 36)
        textSize = 10f
      }
      val dividerPaint = Paint().apply {
        color = Color.rgb(220, 232, 224)
        strokeWidth = 1f
      }

      if (pageRows.isEmpty()) {
        canvas.drawText("No records available for the selected report criteria.", 34f, y + 12f, cellPaint)
      } else {
        pageRows.forEach { row ->
          row.forEachIndexed { i, cell ->
            if (i < colCount) {
              canvas.drawText(cell.take(24), 34f + i * colWidth, y, cellPaint)
            }
          }
          canvas.drawLine(28f, y + 7f, pageWidth - 28f, y + 7f, dividerPaint)
          y += 26f
        }
      }

      pdf.finishPage(page)
    }

    context.contentResolver.openOutputStream(uri)?.use { out ->
      pdf.writeTo(out)
    } ?: error("Unable to open PDF output stream")
    pdf.close()
  }

  fun writeCsvReportToUri(
    context: Context,
    uri: Uri,
    headers: List<String>,
    rows: List<List<String>>,
  ): Result<Unit> = runCatching {
    fun escapeCsv(value: String): String {
      val clean = value.replace("\"", "\"\"")
      return "\"$clean\""
    }
    val sb = StringBuilder()
    sb.appendLine(headers.joinToString(",") { escapeCsv(it) })
    rows.forEach { row ->
      sb.appendLine(row.joinToString(",") { escapeCsv(it) })
    }
    context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { writer ->
      writer.write(sb.toString())
    } ?: error("Unable to open CSV/Excel output stream")
  }
}
