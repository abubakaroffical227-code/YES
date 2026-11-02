package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.data.auth.attemptSilentGoogleSignIn
import com.example.data.auth.signInWithGoogleInteractive
import com.example.data.model.SocietyConstants
import com.example.ui.components.FullScreenImagePreviewDialog
import com.example.ui.components.SocietyImage
import com.example.ui.components.SplitSocietyUniversityHeader
import com.example.ui.components.UafLogoEmblem
import com.example.ui.components.YesDropdownField
import com.example.ui.components.YesLogoEmblem
import com.example.ui.theme.YesHeroEnd
import com.example.ui.theme.YesHeroStart
import com.example.util.ImageEncodingUtils
import kotlinx.coroutines.delay

@Composable
fun YesSplashScreen(
  onSplashFinished: () -> Unit,
) {
  var visible by remember { mutableStateOf(false) }
  val scale by animateFloatAsState(
    targetValue = if (visible) 1f else 0.88f,
    animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
    label = "splash_scale",
  )

  LaunchedEffect(Unit) {
    visible = true
    delay(1300)
    onSplashFinished()
  }

  val splashBrush = Brush.verticalGradient(
    colors = listOf(
      Color(0xFFF3FBF5),
      Color(0xFFE2F3E8),
      Color(0xFFD5EDE0),
    )
  )

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(splashBrush)
      .windowInsetsPadding(WindowInsets.safeDrawing),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(20.dp),
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 460.dp)
        .padding(28.dp)
        .scale(scale),
    ) {
      // Society Logo on LEFT side, University Logo on RIGHT side
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        YesLogoEmblem(size = 86.dp)
        UafLogoEmblem(size = 86.dp)
      }

      AnimatedVisibility(visible = visible, enter = fadeIn(tween(500))) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = "Young Entomologist Society",
            style = MaterialTheme.typography.headlineMedium,
            color = YesHeroStart,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
          )
          Surface(
            color = YesHeroEnd.copy(alpha = 0.14f),
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, Color(0xFFB8860B).copy(alpha = 0.45f)),
          ) {
            Text(
              text = "Department of Entomology",
              style = MaterialTheme.typography.labelLarge,
              color = YesHeroStart,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp),
            )
          }
          Text(
            text = "University of Agriculture, Faisalabad",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF355240),
          )
        }
      }
    }
  }
}

@Composable
fun UnifiedAuthScreen(
  onAuthenticated: () -> Unit,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }

  var isSigningIn by remember { mutableStateOf(false) }
  var errorText by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(Unit) {
    attemptSilentGoogleSignIn(
      context = context,
      credentialManager = credentialManager,
      scope = scope,
      onAuthSuccess = onAuthenticated,
      onUnauthenticated = {},
    )
  }

  val lightPortalBrush = Brush.linearGradient(
    colors = listOf(
      Color(0xFFF9FCF9),
      Color(0xFFEEF7F1),
      Color(0xFFE4F1E8),
    )
  )

  val accentEmerald = Color(0xFF146C38)
  val accentGold = Color(0xFFB8860B)

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(lightPortalBrush),
    contentAlignment = Alignment.Center,
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val topRight = Offset(size.width * 0.86f, size.height * 0.14f)
      val bottomLeft = Offset(size.width * 0.12f, size.height * 0.88f)
      drawCircle(
        color = accentGold.copy(alpha = 0.10f),
        radius = size.minDimension * 0.40f,
        center = topRight,
      )
      drawCircle(
        color = accentEmerald.copy(alpha = 0.08f),
        radius = size.minDimension * 0.46f,
        center = bottomLeft,
      )
      drawCircle(
        color = accentEmerald.copy(alpha = 0.12f),
        radius = size.minDimension * 0.34f,
        center = Offset(size.width * 0.5f, size.height * 0.5f),
        style = Stroke(width = 1.5f),
      )
    }

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 460.dp)
        .windowInsetsPadding(WindowInsets.safeDrawing)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp, vertical = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.2.dp, accentGold.copy(alpha = 0.38f)),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
          // Welcome Banner: Society Logo on LEFT side, University Logo on RIGHT side
          Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFFF3FAF5),
            border = BorderStroke(1.dp, accentGold.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              YesLogoEmblem(size = 62.dp)
              Column(
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp),
              ) {
                Text(
                  text = "YES • UAF",
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.ExtraBold,
                  color = accentEmerald,
                  textAlign = TextAlign.Center,
                )
                Text(
                  text = "Official Portal",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color(0xFF42594B),
                  textAlign = TextAlign.Center,
                )
              }
              UafLogoEmblem(size = 62.dp)
            }
          }

          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Text(
              text = "Young Entomologist Society",
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.ExtraBold,
              color = Color(0xFF0D2618),
              textAlign = TextAlign.Center,
            )

            Surface(
              shape = RoundedCornerShape(50),
              color = Color(0xFFE3F4E9),
              border = BorderStroke(1.dp, accentEmerald.copy(alpha = 0.25f)),
            ) {
              Text(
                text = "DEPARTMENT OF ENTOMOLOGY",
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Bold,
                color = accentEmerald,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
              )
            }

            Text(
              text = "University of Agriculture, Faisalabad",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFF42594B),
              textAlign = TextAlign.Center,
            )
          }

          HorizontalDivider(
            modifier = Modifier.width(76.dp),
            color = accentGold.copy(alpha = 0.42f),
          )

          Text(
            text = "UNIFIED SOCIETY PORTAL",
            style = MaterialTheme.typography.labelLarge,
            letterSpacing = 1.6.sp,
            fontWeight = FontWeight.SemiBold,
            color = accentEmerald,
          )

          if (errorText != null) {
            Surface(
              color = Color(0xFFFFEDEA),
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text(
                text = errorText!!,
                color = Color(0xFF93000A),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(12.dp),
              )
            }
          }

          Button(
            onClick = {
              errorText = null
              isSigningIn = true
              signInWithGoogleInteractive(
                context = context,
                credentialManager = credentialManager,
                scope = scope,
                onAuthSuccess = {
                  isSigningIn = false
                  onAuthenticated()
                },
                onAuthCancelled = {
                  isSigningIn = false
                },
                onAuthError = { msg ->
                  isSigningIn = false
                  errorText = msg
                },
              )
            },
            enabled = !isSigningIn,
            colors = ButtonDefaults.buttonColors(
              containerColor = accentEmerald,
              contentColor = Color.White,
            ),
            modifier = Modifier
              .fillMaxWidth()
              .height(56.dp)
              .testTag("google_sign_in_button"),
            shape = RoundedCornerShape(50),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
          ) {
            if (isSigningIn) {
              CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.White,
                strokeWidth = 2.dp,
              )
              Spacer(Modifier.width(10.dp))
              Text("Signing In...", style = MaterialTheme.typography.titleMedium)
            } else {
              Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null)
              Spacer(Modifier.width(10.dp))
              Text("Sign in with Google", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

@Composable
fun StudentOnboardingScreen(
  email: String,
  initialPhotoUri: String,
  isBusy: Boolean,
  onSubmitRegistration: (
    fullName: String,
    regNo: String,
    degreeLevel: String,
    department: String,
    semester: String,
    phone: String,
    photoUri: String,
    studentCardUri: String,
  ) -> Unit,
  onSignOut: () -> Unit,
) {
  val context = LocalContext.current

  var fullName by remember { mutableStateOf("") }
  var regNo by remember { mutableStateOf("") }
  var degreeLevel by remember { mutableStateOf(SocietyConstants.DEGREE_LEVELS.first()) }
  var department by remember { mutableStateOf(SocietyConstants.DEPARTMENTS.first()) }
  var semester by remember { mutableStateOf(SocietyConstants.SEMESTERS.first()) }
  var phone by remember { mutableStateOf("") }
  var photoUri by remember(initialPhotoUri) { mutableStateOf(initialPhotoUri) }
  var studentCardUri by remember { mutableStateOf("") }
  var previewImageTitleAndSource by remember { mutableStateOf<Pair<String, String>?>(null) }

  val photoPicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      photoUri = ImageEncodingUtils.encodeUriToDataUrl(context, uri, maxDimension = 420, quality = 70)
        ?: uri.toString()
    }
  }

  val studentCardPicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      studentCardUri = ImageEncodingUtils.encodeUriToDataUrl(context, uri, maxDimension = 640, quality = 70)
        ?: uri.toString()
    }
  }

  if (previewImageTitleAndSource != null) {
    val (title, src) = previewImageTitleAndSource!!
    FullScreenImagePreviewDialog(
      title = title,
      imageSource = src,
      onDismiss = { previewImageTitleAndSource = null },
    )
  }

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = Color(0xFFF6FBF7),
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .windowInsetsPadding(WindowInsets.safeDrawing)
        .verticalScroll(rememberScrollState())
        .padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Card(
        modifier = Modifier.fillMaxWidth().widthIn(max = 560.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
      ) {
        Column(
          modifier = Modifier.padding(22.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          SplitSocietyUniversityHeader(
            title = "Student Registration",
            subtitle = "Department of Entomology • $email",
            logoSize = 46.dp,
          )

          HorizontalDivider()

          // Student Photograph Picker
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            if (photoUri.isNotBlank()) {
              SocietyImage(
                imageSource = photoUri,
                contentDescription = "Selected student photograph",
                modifier = Modifier
                  .size(64.dp)
                  .clip(CircleShape)
                  .clickable { previewImageTitleAndSource = "Student Photograph" to photoUri },
                contentScale = ContentScale.Crop,
              )
            } else {
              Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(64.dp),
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(Icons.Outlined.AddAPhoto, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
              }
            }
            OutlinedButton(
              onClick = {
                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
              },
              modifier = Modifier.weight(1f).testTag("reg_pick_photo_btn"),
            ) {
              Icon(Icons.Outlined.AddAPhoto, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(Modifier.width(8.dp))
              Text(if (photoUri.isNotBlank()) "Change Profile Photo ✓" else "Upload Student Photo *")
            }
          }

          // University Student Card Upload
          Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            shape = RoundedCornerShape(14.dp),
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Icon(Icons.Filled.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                  Column {
                    Text("University Student Card", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text("Visible to Administrator for verification", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  }
                }
              }
              if (studentCardUri.isNotBlank()) {
                SocietyImage(
                  imageSource = studentCardUri,
                  contentDescription = "Uploaded University Student Card",
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { previewImageTitleAndSource = "University Student Card" to studentCardUri },
                  contentScale = ContentScale.Crop,
                )
              }
              OutlinedButton(
                onClick = {
                  studentCardPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                modifier = Modifier.fillMaxWidth().testTag("reg_upload_student_card_btn"),
              ) {
                Icon(Icons.Filled.Badge, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (studentCardUri.isNotBlank()) "Replace Uploaded Student Card ✓" else "Upload University Student Card")
              }
            }
          }

          OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Enter Student Full Name *") },
            placeholder = { Text("Enter your full name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("reg_full_name_input"),
          )

          OutlinedTextField(
            value = regNo,
            onValueChange = { regNo = it },
            label = { Text("University Registration Number * (e.g. 2023-ag-4512)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("reg_number_input"),
          )

          YesDropdownField(
            label = "Degree Level",
            options = SocietyConstants.DEGREE_LEVELS,
            selectedOption = degreeLevel,
            onOptionSelected = { degreeLevel = it },
            testTag = "reg_degree_dropdown",
          )

          YesDropdownField(
            label = "Academic Department",
            options = SocietyConstants.DEPARTMENTS,
            selectedOption = department,
            onOptionSelected = { department = it },
            testTag = "reg_department_dropdown",
          )

          YesDropdownField(
            label = "Current Semester",
            options = SocietyConstants.SEMESTERS,
            selectedOption = semester,
            onOptionSelected = { semester = it },
            testTag = "reg_semester_dropdown",
          )

          OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Contact Phone / WhatsApp Number") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("reg_phone_input"),
          )

          Button(
            onClick = {
              onSubmitRegistration(
                fullName,
                regNo,
                degreeLevel,
                department,
                semester,
                phone,
                photoUri,
                studentCardUri,
              )
            },
            enabled = !isBusy && fullName.trim().length >= 2 && regNo.trim().length >= 2,
            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("complete_registration_button"),
          ) {
            Text(if (isBusy) "Creating Student Account..." else "Complete Student Registration")
          }

          TextButton(
            onClick = onSignOut,
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.align(Alignment.CenterHorizontally),
          ) {
            Text("Use a different Google account")
          }
        }
      }
    }
  }
}
