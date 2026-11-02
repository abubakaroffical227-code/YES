package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.IdCardDesignTheme
import com.example.data.model.SocietyCertificate
import com.example.data.model.StudentProfile
import com.example.data.model.allocatedRoleOrNull
import com.example.data.model.hasAllocatedRole
import com.example.data.model.isCabinetMember
import com.example.data.model.isIdCardActive
import com.example.data.model.isVerifiedMember
import com.example.data.model.normalizeCabinetPositionLabel
import com.example.ui.theme.AppFontSize
import com.example.util.ImageEncodingUtils
import com.example.util.YesQrCodeBox

@Composable
fun YesLogoEmblem(
  size: Dp = 64.dp,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.size(size),
    shape = CircleShape,
    color = Color.White,
    shadowElevation = 4.dp,
    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)),
  ) {
    Image(
      painter = painterResource(id = R.drawable.img_yes_logo),
      contentDescription = "Young Entomologist Society Logo",
      modifier = Modifier.padding(4.dp).clip(CircleShape),
      contentScale = ContentScale.Fit,
    )
  }
}

@Composable
fun UafLogoEmblem(
  size: Dp = 64.dp,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.size(size),
    shape = CircleShape,
    color = Color.White,
    shadowElevation = 4.dp,
    border = BorderStroke(1.5.dp, Color(0xFFB8860B).copy(alpha = 0.45f)),
  ) {
    Image(
      painter = painterResource(id = R.drawable.img_uaf_logo),
      contentDescription = "University of Agriculture Faisalabad Logo",
      modifier = Modifier.padding(4.dp).clip(CircleShape),
      contentScale = ContentScale.Fit,
    )
  }
}

/**
 * Displays Society Logo on the LEFT and University Logo on the RIGHT.
 */
@Composable
fun DualUniversitySocietyLogos(
  size: Dp = 44.dp,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    YesLogoEmblem(size = size)
    UafLogoEmblem(size = size)
  }
}

/**
 * Full-width header row placing the Society Logo on the LEFT side, centered title/subtitle,
 * and the University Logo on the RIGHT side.
 */
@Composable
fun SplitSocietyUniversityHeader(
  title: String,
  subtitle: String,
  logoSize: Dp = 52.dp,
  titleColor: Color = MaterialTheme.colorScheme.onSurface,
  subtitleColor: Color = MaterialTheme.colorScheme.primary,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    YesLogoEmblem(size = logoSize)
    Column(
      modifier = Modifier.weight(1f).padding(horizontal = 10.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold,
        color = titleColor,
        textAlign = TextAlign.Center,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )
      if (subtitle.isNotBlank()) {
        Text(
          text = subtitle,
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.SemiBold,
          color = subtitleColor,
          textAlign = TextAlign.Center,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
    UafLogoEmblem(size = logoSize)
  }
}

/**
 * Renders either a Firestore-synced Base64 data URL (`data:image/jpeg;base64,...`) or a standard
 * URI via Coil [AsyncImage].
 */
@Composable
fun SocietyImage(
  imageSource: String,
  contentDescription: String?,
  modifier: Modifier = Modifier,
  contentScale: ContentScale = ContentScale.Crop,
) {
  val decodedBitmap = remember(imageSource) {
    ImageEncodingUtils.decodeDataUrlOrNull(imageSource)
  }
  if (decodedBitmap != null) {
    Image(
      bitmap = decodedBitmap.asImageBitmap(),
      contentDescription = contentDescription,
      modifier = modifier,
      contentScale = contentScale,
    )
  } else if (imageSource.isNotBlank()) {
    AsyncImage(
      model = imageSource,
      contentDescription = contentDescription,
      modifier = modifier,
      contentScale = contentScale,
    )
  }
}

@Composable
fun FullScreenImagePreviewDialog(
  title: String,
  imageSource: String,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title, fontWeight = FontWeight.Bold) },
    text = {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(360.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
      ) {
        SocietyImage(
          imageSource = imageSource,
          contentDescription = title,
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Fit,
        )
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) { Text("Close") }
    },
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YesDropdownField(
  label: String,
  options: List<String>,
  selectedOption: String,
  onOptionSelected: (String) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  optionLabel: (String) -> String = { it },
  testTag: String = "",
) {
  var expanded by remember { mutableStateOf(false) }

  ExposedDropdownMenuBox(
    expanded = expanded && enabled,
    onExpandedChange = { if (enabled) expanded = !expanded },
    modifier = modifier.fillMaxWidth(),
  ) {
    OutlinedTextField(
      value = optionLabel(selectedOption),
      onValueChange = {},
      readOnly = true,
      enabled = enabled,
      singleLine = true,
      label = { Text(label) },
      trailingIcon = {
        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && enabled)
      },
      colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
      modifier = Modifier
        .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = enabled)
        .fillMaxWidth()
        .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier),
    )

    ExposedDropdownMenu(
      expanded = expanded && enabled,
      onDismissRequest = { expanded = false },
    ) {
      options.forEach { option ->
        DropdownMenuItem(
          text = {
            Text(
              text = optionLabel(option),
              fontWeight = if (option == selectedOption) FontWeight.Bold else FontWeight.Normal,
            )
          },
          onClick = {
            onOptionSelected(option)
            expanded = false
          },
          contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
        )
      }
    }
  }
}

@Composable
fun StatMetricCard(
  title: String,
  value: String,
  icon: ImageVector,
  accentColor: Color = MaterialTheme.colorScheme.primary,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
) {
  Card(
    modifier = modifier
      .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = accentColor.copy(alpha = 0.14f),
          modifier = Modifier.size(38.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = icon,
              contentDescription = title,
              tint = accentColor,
              modifier = Modifier.size(20.dp),
            )
          }
        }
      }
      Text(
        text = value,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
fun StatusPill(
  label: String,
  modifier: Modifier = Modifier,
) {
  val upper = label.uppercase()
  val (bg, fg) = when (upper) {
    "ACTIVE", "APPROVED", "PAID", "VERIFIED", "ISSUED", "ID ISSUED", "PRESENT", "RESOLVED", "FREE", "LOCKED", "CABINET" ->
      Color(0xFFD2F5E0) to Color(0xFF0A4728)
    "PENDING", "PENDING_VERIFICATION", "RECEIPT_SUBMITTED", "IN_REVIEW", "SUBMITTED", "IMPORTANT", "UNLOCKED", "NOT_ISSUED", "ID NOT ISSUED", "NONE" ->
      Color(0xFFFFE7C2) to Color(0xFF6B3A00)
    "URGENT", "REJECTED", "REVOKED", "ID REVOKED", "BLOCKED", "INACTIVE", "ABSENT", "UNPAID", "EXPENSE" ->
      Color(0xFFFFDAD6) to Color(0xFF7D1D17)
    else ->
      MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
  }

  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(50),
    color = bg,
  ) {
    Text(
      text = label.replace("_", " "),
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.SemiBold,
      color = fg,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
    )
  }
}

@Composable
fun SectionTitleRow(
  title: String,
  actionLabel: String? = null,
  onAction: (() -> Unit)? = null,
) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleLarge,
      color = MaterialTheme.colorScheme.onBackground,
    )
    if (actionLabel != null && onAction != null) {
      TextButton(onClick = onAction) {
        Text(text = actionLabel, style = MaterialTheme.typography.labelLarge)
      }
    }
  }
}

@Composable
fun EmptyStateCard(
  title: String,
  subtitle: String,
  icon: ImageVector = Icons.Outlined.Inbox,
  actionText: String? = null,
  onAction: (() -> Unit)? = null,
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.size(54.dp),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp),
          )
        }
      }
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
      )
      if (actionText != null && onAction != null) {
        Spacer(Modifier.height(4.dp))
        OutlinedButton(onClick = onAction) {
          Text(actionText)
        }
      }
    }
  }
}

private data class IdCardVisualPalette(
  val swatchPrimary: Color,
  val swatchAccent: Color,
  val cardContainer: Color,
  val bodyGradient: Brush,
  val headerGradient: Brush,
  val ribbonGradient: Brush,
  val ribbonContent: Color,
  val borderAccent: Color,
  val titleText: Color,
  val subtitleText: Color,
  val mutedText: Color,
  val specContainer: Color,
  val specBorder: Color,
  val specLabel: Color,
  val specValue: Color,
  val slotFill: Color,
  val photoPlaceholderBg: Color,
  val downloadButtonBg: Color,
  val downloadButtonFg: Color,
  val headerBottomRadius: Dp,
)

private fun visualPaletteFor(theme: IdCardDesignTheme): IdCardVisualPalette = when (theme) {
  IdCardDesignTheme.ROYAL_EMERALD_GOLD -> IdCardVisualPalette(
    swatchPrimary = Color(0xFF0A3D22),
    swatchAccent = Color(0xFFF6D365),
    cardContainer = Color(0xFF062916),
    bodyGradient = Brush.verticalGradient(
      listOf(Color(0xFF07311B), Color(0xFF0A3D22), Color(0xFF052414), Color(0xFF03170C))
    ),
    headerGradient = Brush.horizontalGradient(
      listOf(Color(0xFF093A20), Color(0xFF115431), Color(0xFF093A20))
    ),
    ribbonGradient = Brush.horizontalGradient(
      listOf(Color(0xFFB8860B), Color(0xFFF6D365), Color(0xFFD4AF37), Color(0xFFB8860B))
    ),
    ribbonContent = Color(0xFF1F1200),
    borderAccent = Color(0xFFD4AF37),
    titleText = Color.White,
    subtitleText = Color(0xFFF6D365),
    mutedText = Color(0xFFD2F5E0),
    specContainer = Color(0xFF0A3820).copy(alpha = 0.88f),
    specBorder = Color(0xFFD4AF37).copy(alpha = 0.45f),
    specLabel = Color(0xFFF6D365),
    specValue = Color.White,
    slotFill = Color(0xFF02130A),
    photoPlaceholderBg = Color(0xFF10492B),
    downloadButtonBg = Color(0xFFF6D365),
    downloadButtonFg = Color(0xFF1F1200),
    headerBottomRadius = 0.dp,
  )

  IdCardDesignTheme.IMPERIAL_SAPPHIRE_SILVER -> IdCardVisualPalette(
    swatchPrimary = Color(0xFF0E2B5C),
    swatchAccent = Color(0xFF8CCBFF),
    cardContainer = Color(0xFF091D3E),
    bodyGradient = Brush.verticalGradient(
      listOf(Color(0xFF0C2752), Color(0xFF11356E), Color(0xFF091E40), Color(0xFF061329))
    ),
    headerGradient = Brush.linearGradient(
      listOf(Color(0xFF164287), Color(0xFF0D2B59))
    ),
    ribbonGradient = Brush.horizontalGradient(
      listOf(Color(0xFF63B3FF), Color(0xFFDCEEFF), Color(0xFF8CCBFF))
    ),
    ribbonContent = Color(0xFF061936),
    borderAccent = Color(0xFF8CCBFF),
    titleText = Color.White,
    subtitleText = Color(0xFF9ED2FF),
    mutedText = Color(0xFFDCEEFF),
    specContainer = Color(0xFF102E60).copy(alpha = 0.88f),
    specBorder = Color(0xFF8CCBFF).copy(alpha = 0.45f),
    specLabel = Color(0xFF9ED2FF),
    specValue = Color.White,
    slotFill = Color(0xFF051024),
    photoPlaceholderBg = Color(0xFF173F7A),
    downloadButtonBg = Color(0xFF8CCBFF),
    downloadButtonFg = Color(0xFF061936),
    headerBottomRadius = 22.dp,
  )

  IdCardDesignTheme.CRIMSON_VELVET_CHAMPAGNE -> IdCardVisualPalette(
    swatchPrimary = Color(0xFF5E1124),
    swatchAccent = Color(0xFFF5D491),
    cardContainer = Color(0xFF3D0A17),
    bodyGradient = Brush.verticalGradient(
      listOf(Color(0xFF561021), Color(0xFF6B152A), Color(0xFF420B19), Color(0xFF29060E))
    ),
    headerGradient = Brush.horizontalGradient(
      listOf(Color(0xFF70162C), Color(0xFF8C1E39), Color(0xFF70162C))
    ),
    ribbonGradient = Brush.horizontalGradient(
      listOf(Color(0xFFC89B4B), Color(0xFFFCE2AE), Color(0xFFE5BA6B))
    ),
    ribbonContent = Color(0xFF2B0710),
    borderAccent = Color(0xFFEBC376),
    titleText = Color.White,
    subtitleText = Color(0xFFFCE2AE),
    mutedText = Color(0xFFF9DCE2),
    specContainer = Color(0xFF5C1223).copy(alpha = 0.90f),
    specBorder = Color(0xFFEBC376).copy(alpha = 0.48f),
    specLabel = Color(0xFFFCE2AE),
    specValue = Color.White,
    slotFill = Color(0xFF1F030A),
    photoPlaceholderBg = Color(0xFF75192F),
    downloadButtonBg = Color(0xFFF5D491),
    downloadButtonFg = Color(0xFF2B0710),
    headerBottomRadius = 28.dp,
  )

  IdCardDesignTheme.OBSIDIAN_EXECUTIVE_AMBER -> IdCardVisualPalette(
    swatchPrimary = Color(0xFF1A1E24),
    swatchAccent = Color(0xFFF5A623),
    cardContainer = Color(0xFF121519),
    bodyGradient = Brush.verticalGradient(
      listOf(Color(0xFF1E232B), Color(0xFF262C36), Color(0xFF15191F), Color(0xFF0D1014))
    ),
    headerGradient = Brush.horizontalGradient(
      listOf(Color(0xFF282F3A), Color(0xFF363F4D), Color(0xFF282F3A))
    ),
    ribbonGradient = Brush.horizontalGradient(
      listOf(Color(0xFFD98809), Color(0xFFFFCE73), Color(0xFFF5A623))
    ),
    ribbonContent = Color(0xFF191000),
    borderAccent = Color(0xFFF5A623),
    titleText = Color.White,
    subtitleText = Color(0xFFFFCE73),
    mutedText = Color(0xFFD5DCE4),
    specContainer = Color(0xFF232933),
    specBorder = Color(0xFFF5A623).copy(alpha = 0.45f),
    specLabel = Color(0xFFFFCE73),
    specValue = Color.White,
    slotFill = Color(0xFF090B0E),
    photoPlaceholderBg = Color(0xFF2E3642),
    downloadButtonBg = Color(0xFFF5A623),
    downloadButtonFg = Color(0xFF191000),
    headerBottomRadius = 12.dp,
  )

  IdCardDesignTheme.BOTANICAL_IVORY_FOREST -> IdCardVisualPalette(
    swatchPrimary = Color(0xFFE6F5EC),
    swatchAccent = Color(0xFF0E5A34),
    cardContainer = Color.White,
    bodyGradient = Brush.verticalGradient(
      listOf(Color(0xFFFFFFFF), Color(0xFFF7FCF9), Color(0xFFF1F9F4))
    ),
    headerGradient = Brush.verticalGradient(
      listOf(Color(0xFFDDF2E5), Color(0xFFEAF7EF))
    ),
    ribbonGradient = Brush.horizontalGradient(
      listOf(Color(0xFF0A4728), Color(0xFF1B7A48), Color(0xFF0E5A34))
    ),
    ribbonContent = Color.White,
    borderAccent = Color(0xFF1F7A4C),
    titleText = Color(0xFF0E2217),
    subtitleText = Color(0xFF1B6B40),
    mutedText = Color(0xFF2A593F),
    specContainer = Color(0xFFEDF7F1),
    specBorder = Color(0xFFC6E2D1),
    specLabel = Color(0xFF16663C),
    specValue = Color(0xFF0E2217),
    slotFill = Color(0xFFC6E2D1),
    photoPlaceholderBg = Color(0xFFDDF3E6),
    downloadButtonBg = Color(0xFF0E5A34),
    downloadButtonFg = Color.White,
    headerBottomRadius = 20.dp,
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IdCardDesignThemeSelector(
  selectedTheme: IdCardDesignTheme,
  onSelectTheme: (IdCardDesignTheme) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(6.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Icon(
        imageVector = Icons.Filled.Palette,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(16.dp),
      )
      Text(
        text = "ID Card Design & Colour Theme",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
      )
    }

    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
      verticalArrangement = Arrangement.spacedBy(6.dp),
      modifier = Modifier.fillMaxWidth(),
    ) {
      IdCardDesignTheme.entries.forEach { theme ->
        val selected = theme == selectedTheme
        val palette = visualPaletteFor(theme)
        Surface(
          shape = RoundedCornerShape(50),
          color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
          border = BorderStroke(
            width = if (selected) 1.8.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
          ),
          modifier = Modifier
            .clickable { onSelectTheme(theme) }
            .testTag("id_card_theme_${theme.key.lowercase()}"),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Box(
              modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(palette.swatchPrimary),
              contentAlignment = Alignment.Center,
            ) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(palette.swatchAccent),
              )
            }
            Text(
              text = theme.displayName,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
            )
          }
        }
      }
    }
  }
}

/**
 * Official Young Entomologist Society (YES) & University of Agriculture, Faisalabad (UAF)
 * **Portrait Identity Card**.
 * - Excludes Credential ID, Degree Name, and Email Address for a clean, executive portrait badge.
 * - Places the **Society Logo on the LEFT** and the **University Logo on the RIGHT**.
 */
@Composable
fun DigitalMembershipCard(
  student: StudentProfile,
  modifier: Modifier = Modifier,
  selectedTheme: IdCardDesignTheme? = null,
  @Suppress("UNUSED_PARAMETER") onThemeChange: ((IdCardDesignTheme) -> Unit)? = null,
  showThemeSelector: Boolean = false,
  onDownloadPdf: ((IdCardDesignTheme) -> Unit)? = null,
) {
  val activeTheme = selectedTheme ?: IdCardDesignTheme.ROYAL_EMERALD_GOLD

  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    if (student.isCabinetMember()) {
      CabinetMemberIdCard(
        student = student,
        designTheme = activeTheme,
        modifier = Modifier.widthIn(max = 344.dp).fillMaxWidth(),
        onDownloadPdf = onDownloadPdf?.let { cb -> { cb(activeTheme) } },
      )
    } else {
      GeneralMemberIdCard(
        student = student,
        designTheme = activeTheme,
        modifier = Modifier.widthIn(max = 344.dp).fillMaxWidth(),
        onDownloadPdf = onDownloadPdf?.let { cb -> { cb(activeTheme) } },
      )
    }
  }
}

@Composable
fun OfficialIdCardPreviewDialog(
  student: StudentProfile,
  initialTheme: IdCardDesignTheme? = null,
  onDownloadPdf: ((IdCardDesignTheme) -> Unit)? = null,
  onDismiss: () -> Unit,
) {
  val isCabinet = student.isCabinetMember()
  val currentTheme = initialTheme ?: IdCardDesignTheme.ROYAL_EMERALD_GOLD

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (isCabinet) "Cabinet Officer Identity Card" else "Member Identity Card",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        DigitalMembershipCard(
          student = student,
          selectedTheme = currentTheme,
          showThemeSelector = false,
          onDownloadPdf = onDownloadPdf,
        )
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        modifier = Modifier.testTag("close_id_card_preview_btn"),
      ) {
        Text("Close")
      }
    },
  )
}

@Composable
private fun PortraitCardDetailRow(
  label: String,
  value: String,
  labelColor: Color,
  valueColor: Color,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.Top,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = labelColor,
      fontWeight = FontWeight.SemiBold,
      modifier = Modifier.weight(0.40f),
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall,
      color = valueColor,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.End,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.weight(0.60f),
    )
  }
}

@Composable
private fun CabinetMemberIdCard(
  student: StudentProfile,
  designTheme: IdCardDesignTheme,
  modifier: Modifier = Modifier,
  onDownloadPdf: (() -> Unit)? = null,
) {
  val palette = visualPaletteFor(designTheme)
  val cleanRole = remember(student.position) {
    normalizeCabinetPositionLabel(student.position).ifBlank { "Executive Member" }
  }
  val qrPayload = remember(student.registrationNumber, student.studentUid, cleanRole) {
    "YES-CABINET:$cleanRole:${student.registrationNumber}:${student.studentUid}"
  }
  val isCardActive = student.isIdCardActive()
  val isRevoked = student.idCardStatus.equals("REVOKED", ignoreCase = true)

  Card(
    modifier = modifier.testTag("digital_membership_card_cabinet"),
    shape = RoundedCornerShape(26.dp),
    colors = CardDefaults.cardColors(containerColor = palette.cardContainer),
    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
    border = BorderStroke(2.2.dp, palette.borderAccent),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(palette.bodyGradient),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      // Styled Institutional Header Band: Society Logo (LEFT) + University Logo (RIGHT)
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(
            RoundedCornerShape(
              topStart = 26.dp,
              topEnd = 26.dp,
              bottomStart = palette.headerBottomRadius,
              bottomEnd = palette.headerBottomRadius,
            )
          )
          .background(palette.headerGradient)
          .padding(top = 10.dp, bottom = 12.dp, start = 14.dp, end = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Surface(
          modifier = Modifier.size(width = 56.dp, height = 6.dp),
          shape = RoundedCornerShape(50),
          color = palette.slotFill,
          border = BorderStroke(1.dp, palette.borderAccent.copy(alpha = 0.65f)),
        ) {}

        Spacer(Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          YesLogoEmblem(size = 46.dp)
          Column(
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Text(
              text = "YOUNG ENTOMOLOGIST SOCIETY",
              style = MaterialTheme.typography.labelLarge,
              color = palette.titleText,
              fontWeight = FontWeight.ExtraBold,
              textAlign = TextAlign.Center,
            )
            Text(
              text = "DEPARTMENT OF ENTOMOLOGY • UAF",
              style = MaterialTheme.typography.labelSmall,
              color = palette.subtitleText,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center,
            )
            Text(
              text = "EXECUTIVE CABINET CARD",
              style = MaterialTheme.typography.labelSmall,
              letterSpacing = 0.8.sp,
              color = palette.mutedText,
              fontWeight = FontWeight.SemiBold,
              textAlign = TextAlign.Center,
            )
          }
          UafLogoEmblem(size = 46.dp)
        }
      }

      // Full-Width Cabinet Role Ribbon Plaque
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(palette.ribbonGradient)
          .padding(horizontal = 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
        ) {
          Icon(
            imageVector = Icons.Filled.Stars,
            contentDescription = null,
            tint = palette.ribbonContent,
            modifier = Modifier.size(16.dp),
          )
          Spacer(Modifier.width(6.dp))
          Text(
            text = cleanRole.uppercase(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            color = palette.ribbonContent,
            textAlign = TextAlign.Center,
          )
          Spacer(Modifier.width(6.dp))
          Icon(
            imageVector = Icons.Filled.Stars,
            contentDescription = null,
            tint = palette.ribbonContent,
            modifier = Modifier.size(16.dp),
          )
        }
      }

      if (isRevoked) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF93000A))
            .padding(vertical = 6.dp, horizontal = 12.dp),
          contentAlignment = Alignment.Center,
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Filled.Block, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Text(
              text = "ID CARD REVOKED BY ADMINISTRATION",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.ExtraBold,
              color = Color.White,
            )
          }
        }
      }

      // Vertical Portrait Body (No Credential ID, No Degree Name, No Email)
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Framed Portrait Photograph
        Surface(
          modifier = Modifier.size(width = 112.dp, height = 132.dp),
          shape = RoundedCornerShape(18.dp),
          color = palette.photoPlaceholderBg,
          border = BorderStroke(2.5.dp, palette.borderAccent),
          shadowElevation = 6.dp,
        ) {
          if (student.photoUri.isNotBlank()) {
            SocietyImage(
              imageSource = student.photoUri,
              contentDescription = "Cabinet Portrait of ${student.fullName}",
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop,
            )
          } else {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
              Icon(
                Icons.Filled.Person,
                contentDescription = "Executive Portrait",
                tint = palette.subtitleText,
                modifier = Modifier.size(54.dp),
              )
            }
          }
        }

        // Status & Theme Badges
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Surface(
            shape = RoundedCornerShape(50),
            color = palette.borderAccent.copy(alpha = 0.18f),
            border = BorderStroke(1.dp, palette.borderAccent),
          ) {
            Text(
              text = "CABINET OFFICER",
              style = MaterialTheme.typography.labelSmall,
              color = palette.subtitleText,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            )
          }
          StatusPill(
            label = when {
              isRevoked -> "REVOKED"
              isCardActive -> "ISSUED"
              student.isVerifiedMember() -> "VERIFIED"
              else -> student.membershipStatus
            }
          )
        }

        // Full Name Only (Email removed)
        Text(
          text = student.fullName,
          style = MaterialTheme.typography.titleLarge,
          color = palette.titleText,
          fontWeight = FontWeight.ExtraBold,
          textAlign = TextAlign.Center,
        )

        // Clean Specification Box (Role, Reg. Number, Department, Tenure/Valid)
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          color = palette.specContainer,
          border = BorderStroke(1.dp, palette.specBorder),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            PortraitCardDetailRow(
              label = "ROLE",
              value = cleanRole,
              labelColor = palette.specLabel,
              valueColor = palette.specValue,
            )
            HorizontalDivider(color = palette.specBorder)
            PortraitCardDetailRow(
              label = "REG. NUMBER",
              value = student.registrationNumber.ifBlank { "UAF-YES" },
              labelColor = palette.specLabel,
              valueColor = palette.specValue,
            )
            HorizontalDivider(color = palette.specBorder)
            PortraitCardDetailRow(
              label = "DEPARTMENT",
              value = student.department.ifBlank { "Department of Entomology" },
              labelColor = palette.specLabel,
              valueColor = palette.specValue,
            )
            HorizontalDivider(color = palette.specBorder)
            PortraitCardDetailRow(
              label = "TENURE / VALID",
              value = student.membershipValidUntil.ifBlank { student.semester.ifBlank { "2026 - 2027" } },
              labelColor = palette.specLabel,
              valueColor = palette.specValue,
            )
          }
        }

        // Centered QR Verification Code
        Surface(
          shape = RoundedCornerShape(16.dp),
          border = BorderStroke(1.8.dp, palette.borderAccent),
          color = Color.White,
        ) {
          YesQrCodeBox(
            payload = qrPayload,
            boxSize = 98.dp,
            description = "Cabinet ID QR Code for ${student.fullName}",
          )
        }

        Text(
          text = "YOUNG ENTOMOLOGIST SOCIETY • UAF",
          style = MaterialTheme.typography.labelSmall,
          color = palette.subtitleText,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center,
        )

        if (onDownloadPdf != null && isCardActive) {
          Button(
            onClick = onDownloadPdf,
            colors = ButtonDefaults.buttonColors(
              containerColor = palette.downloadButtonBg,
              contentColor = palette.downloadButtonFg,
            ),
            modifier = Modifier.fillMaxWidth().testTag("download_id_card_pdf_btn"),
          ) {
            Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Download Official ID Card PDF", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
private fun GeneralMemberIdCard(
  student: StudentProfile,
  designTheme: IdCardDesignTheme,
  modifier: Modifier = Modifier,
  onDownloadPdf: (() -> Unit)? = null,
) {
  val palette = visualPaletteFor(designTheme)
  val qrPayload = remember(student.registrationNumber, student.studentUid) {
    "YES-MEMBER:${student.registrationNumber}:${student.studentUid}"
  }
  val isCardActive = student.isIdCardActive()
  val isRevoked = student.idCardStatus.equals("REVOKED", ignoreCase = true)

  Card(
    modifier = modifier.testTag("digital_membership_card"),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = palette.cardContainer),
    elevation = CardDefaults.cardElevation(defaultElevation = 7.dp),
    border = BorderStroke(1.8.dp, palette.borderAccent),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(palette.bodyGradient),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      // Portrait Header: Society Logo (LEFT) + University Logo (RIGHT)
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(
            RoundedCornerShape(
              topStart = 24.dp,
              topEnd = 24.dp,
              bottomStart = palette.headerBottomRadius,
              bottomEnd = palette.headerBottomRadius,
            )
          )
          .background(palette.headerGradient)
          .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Surface(
          modifier = Modifier.size(width = 54.dp, height = 6.dp),
          shape = RoundedCornerShape(50),
          color = palette.slotFill,
          border = BorderStroke(1.dp, palette.borderAccent.copy(alpha = 0.50f)),
        ) {}
        Spacer(Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          YesLogoEmblem(size = 44.dp)
          Column(
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Text(
              text = "YOUNG ENTOMOLOGIST SOCIETY",
              style = MaterialTheme.typography.labelLarge,
              color = palette.titleText,
              fontWeight = FontWeight.ExtraBold,
              textAlign = TextAlign.Center,
            )
            Text(
              text = "Department of Entomology • UAF",
              style = MaterialTheme.typography.labelSmall,
              color = palette.subtitleText,
              fontWeight = FontWeight.SemiBold,
              textAlign = TextAlign.Center,
            )
          }
          UafLogoEmblem(size = 44.dp)
        }

        Spacer(Modifier.height(8.dp))
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(palette.ribbonGradient)
            .padding(horizontal = 14.dp, vertical = 4.dp),
        ) {
          Text(
            text = "GENERAL MEMBER IDENTITY CARD",
            style = MaterialTheme.typography.labelSmall,
            color = palette.ribbonContent,
            fontWeight = FontWeight.Bold,
          )
        }
      }

      if (isRevoked) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFDAD6))
            .padding(vertical = 6.dp, horizontal = 12.dp),
          contentAlignment = Alignment.Center,
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Filled.Block, contentDescription = null, tint = Color(0xFF93000A), modifier = Modifier.size(16.dp))
            Text(
              text = "ID CARD REVOKED BY ADMINISTRATOR",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF93000A),
            )
          }
        }
      }

      // Vertical Portrait General Member Body (No Credential ID, No Degree Name, No Email)
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Portrait Student Photograph Frame
        Surface(
          modifier = Modifier.size(width = 106.dp, height = 124.dp),
          shape = RoundedCornerShape(16.dp),
          color = palette.photoPlaceholderBg,
          border = BorderStroke(2.dp, palette.borderAccent),
        ) {
          if (student.photoUri.isNotBlank()) {
            SocietyImage(
              imageSource = student.photoUri,
              contentDescription = "Photo of ${student.fullName}",
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop,
            )
          } else {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
              Icon(
                Icons.Filled.Person,
                contentDescription = "Student Portrait",
                tint = palette.subtitleText,
                modifier = Modifier.size(48.dp),
              )
            }
          }
        }

        // Member Tier & Status Pills
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          if (student.hasAllocatedRole()) {
            Surface(
              shape = RoundedCornerShape(50),
              color = palette.borderAccent.copy(alpha = 0.16f),
              border = BorderStroke(1.dp, palette.borderAccent.copy(alpha = 0.6f)),
            ) {
              Text(
                text = student.allocatedRoleOrNull()!!.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = palette.subtitleText,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
              )
            }
          }
          StatusPill(
            label = when {
              isRevoked -> "REVOKED"
              isCardActive -> "ISSUED"
              student.isVerifiedMember() -> "VERIFIED"
              else -> student.membershipStatus
            }
          )
        }

        // Student Full Name Only (Email removed)
        Text(
          text = student.fullName,
          style = MaterialTheme.typography.titleLarge,
          color = palette.titleText,
          fontWeight = FontWeight.ExtraBold,
          textAlign = TextAlign.Center,
        )

        // Clean Specification Box (Position if allocated, Reg. Number, Department, Semester, Valid Until)
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = palette.specContainer,
          border = BorderStroke(1.dp, palette.specBorder),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            if (student.hasAllocatedRole()) {
              PortraitCardDetailRow(
                label = "ROLE",
                value = student.allocatedRoleOrNull()!!,
                labelColor = palette.specLabel,
                valueColor = palette.specValue,
              )
              HorizontalDivider(color = palette.specBorder)
            }
            PortraitCardDetailRow(
              label = "REG. NUMBER",
              value = student.registrationNumber,
              labelColor = palette.specLabel,
              valueColor = palette.specValue,
            )
            HorizontalDivider(color = palette.specBorder)
            PortraitCardDetailRow(
              label = "DEPARTMENT",
              value = student.department,
              labelColor = palette.specLabel,
              valueColor = palette.specValue,
            )
            HorizontalDivider(color = palette.specBorder)
            PortraitCardDetailRow(
              label = "SEMESTER",
              value = student.semester,
              labelColor = palette.specLabel,
              valueColor = palette.specValue,
            )
            if (student.membershipValidUntil.isNotBlank()) {
              HorizontalDivider(color = palette.specBorder)
              PortraitCardDetailRow(
                label = "VALID UNTIL",
                value = student.membershipValidUntil,
                labelColor = palette.specLabel,
                valueColor = palette.specValue,
              )
            }
          }
        }

        // Centered QR Verification Code
        Surface(
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.4.dp, palette.borderAccent),
          color = Color.White,
        ) {
          YesQrCodeBox(
            payload = qrPayload,
            boxSize = 96.dp,
            description = "General Member ID QR Code for ${student.fullName}",
          )
        }

        if (student.profileLocked) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Icon(
              Icons.Filled.Lock,
              contentDescription = null,
              tint = palette.subtitleText,
              modifier = Modifier.size(14.dp),
            )
            Text(
              text = "Verified Profile • YES UAF",
              style = MaterialTheme.typography.labelSmall,
              color = palette.subtitleText,
              fontWeight = FontWeight.SemiBold,
            )
          }
        }

        if (onDownloadPdf != null && isCardActive) {
          Button(
            onClick = onDownloadPdf,
            colors = ButtonDefaults.buttonColors(
              containerColor = palette.downloadButtonBg,
              contentColor = palette.downloadButtonFg,
            ),
            modifier = Modifier.fillMaxWidth().testTag("download_id_card_pdf_btn"),
          ) {
            Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Download ID Card PDF (${designTheme.displayName})", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

/**
 * Dual-logo ornamental certificate preview card with Society Logo on LEFT and University Logo on RIGHT.
 */
@Composable
fun OfficialCertificateCard(
  certificate: SocietyCertificate,
  onDownloadPdf: () -> Unit,
  onDelete: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFFFCFBF6)),
    border = BorderStroke(2.dp, Color(0xFF0E5C35)),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(6.dp)
        .background(Color(0xFFFCFBF6), RoundedCornerShape(16.dp))
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      // Society Logo on LEFT, University Logo on RIGHT
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFECF7F0),
        border = BorderStroke(1.dp, Color(0xFFB8860B).copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          YesLogoEmblem(size = 44.dp)
          Column(
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Text(
              text = "YOUNG ENTOMOLOGIST SOCIETY (YES)",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.ExtraBold,
              color = Color(0xFF0E5C35),
              textAlign = TextAlign.Center,
            )
            Text(
              text = "DEPARTMENT OF ENTOMOLOGY",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFA06C08),
              textAlign = TextAlign.Center,
            )
            Text(
              text = "UNIVERSITY OF AGRICULTURE, FAISALABAD",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF0A4226),
              textAlign = TextAlign.Center,
            )
          }
          UafLogoEmblem(size = 44.dp)
        }
      }

      // Gold-bordered Emerald Ribbon
      Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Surface(
          shape = RoundedCornerShape(50),
          color = Color(0xFF0A4226),
          border = BorderStroke(1.dp, Color(0xFFB8860B)),
        ) {
          Text(
            text = "CERTIFICATE OF ${certificate.certificateType}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFE29A),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp),
          )
        }
      }

      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Text(
          text = "Proudly presented to",
          style = MaterialTheme.typography.bodySmall,
          fontStyle = FontStyle.Italic,
          color = Color(0xFF3E5246),
        )
        Text(
          text = certificate.studentName,
          style = MaterialTheme.typography.headlineSmall,
          fontFamily = FontFamily.Serif,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0A3D22),
          textAlign = TextAlign.Center,
        )
        Text(
          text = "Registration No: ${certificate.registrationNumber}",
          style = MaterialTheme.typography.labelMedium,
          color = Color(0xFF374B3F),
          fontWeight = FontWeight.SemiBold,
        )
        Text(
          text = certificate.eventTitle,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0E5C35),
          textAlign = TextAlign.Center,
        )
        Text(
          text = "Held on ${certificate.eventDate} • Department of Entomology, UAF",
          style = MaterialTheme.typography.bodySmall,
          color = Color(0xFF44564B),
          textAlign = TextAlign.Center,
        )
      }

      HorizontalDivider(color = Color(0xFFD8E6DD))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.weight(1f),
        ) {
          YesQrCodeBox(
            payload = "YES-VERIFY:${certificate.certificateCode}",
            boxSize = 66.dp,
            description = "Certificate QR ${certificate.certificateCode}",
          )
          Column {
            Text(
              text = "ID: ${certificate.certificateCode}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF0A4226),
            )
            Text(
              text = certificate.authorizedSignatory,
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF4B5E52),
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Button(
            onClick = onDownloadPdf,
            modifier = Modifier.testTag("download_cert_${certificate.id}"),
          ) {
            Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("PDF")
          }
          if (onDelete != null) {
            OutlinedButton(
              onClick = onDelete,
              colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
              modifier = Modifier.testTag("delete_cert_${certificate.id}"),
            ) {
              Icon(
                Icons.Filled.DeleteForever,
                contentDescription = "Delete Certificate",
                modifier = Modifier.size(18.dp),
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun CertificateVerificationDialog(
  verifiedCertificate: SocietyCertificate?,
  checked: Boolean,
  onVerify: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  var codeInput by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Icon(Icons.Outlined.QrCode2, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text("Verify Certificate Authenticity")
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          "Enter the unique Certificate ID or QR verification code (e.g., YES-CERT-2026-8841) to confirm official society issuance.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
          value = codeInput,
          onValueChange = { codeInput = it },
          label = { Text("Certificate ID / QR Code") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("certificate_code_input"),
        )
        if (checked) {
          if (verifiedCertificate != null) {
            Card(
              colors = CardDefaults.cardColors(containerColor = Color(0xFFD2F5E0)),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Icon(Icons.Filled.Verified, contentDescription = "Verified", tint = Color(0xFF0A4728))
                  Text("AUTHENTIC YES CERTIFICATE", fontWeight = FontWeight.Bold, color = Color(0xFF0A4728))
                }
                Text("Recipient: ${verifiedCertificate.studentName} (${verifiedCertificate.registrationNumber})", color = Color(0xFF0A4728))
                Text("Event: ${verifiedCertificate.eventTitle}", color = Color(0xFF0A4728))
                Text("Date: ${verifiedCertificate.eventDate} • Type: ${verifiedCertificate.certificateType}", color = Color(0xFF0A4728), style = MaterialTheme.typography.bodySmall)
              }
            }
          } else {
            Card(
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(Icons.Outlined.ErrorOutline, contentDescription = "Not found", tint = MaterialTheme.colorScheme.onErrorContainer)
                Text(
                  "No matching certificate found for \"${codeInput.trim()}\".",
                  color = MaterialTheme.colorScheme.onErrorContainer,
                  style = MaterialTheme.typography.bodySmall,
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { if (codeInput.isNotBlank()) onVerify(codeInput.replace("YES-VERIFY:", "").trim()) },
        enabled = codeInput.isNotBlank(),
        modifier = Modifier.testTag("verify_certificate_submit"),
      ) {
        Text("Verify")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Close") }
    },
  )
}

@Composable
fun ConfirmDeleteDialog(
  itemTitle: String,
  itemSubtitle: String = "This action permanently removes this record from the society database.",
  onConfirmDelete: () -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Icon(
        imageVector = Icons.Filled.DeleteForever,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.error,
        modifier = Modifier.size(32.dp),
      )
    },
    title = { Text("Delete Record?") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(itemTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(itemSubtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onConfirmDelete()
          onDismiss()
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.error,
          contentColor = MaterialTheme.colorScheme.onError,
        ),
        modifier = Modifier.testTag("confirm_delete_dialog_btn"),
      ) {
        Text("Delete")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    },
  )
}

/**
 * App Font Size Module Control (replaces all color theme options).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppFontSizeSelector(
  appFontSize: AppFontSize,
  onAppFontSizeChange: (AppFontSize) -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Icon(Icons.Filled.FormatSize, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
      Text("App Font Size", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }

    Text(
      "Adjust application text scaling across all screens and modules (Light Mode Only):",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    YesDropdownField(
      label = "Select App Font Size",
      options = AppFontSize.entries.map { it.name },
      selectedOption = appFontSize.name,
      onOptionSelected = { name ->
        AppFontSize.entries.firstOrNull { it.name == name }?.let(onAppFontSizeChange)
      },
      optionLabel = { name ->
        val entry = AppFontSize.entries.firstOrNull { it.name == name }
        if (entry != null) "${entry.displayName} — ${entry.subtitle}" else name
      },
      testTag = "app_font_size_dropdown",
    )

    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      AppFontSize.entries.forEach { sizeOption ->
        val selected = appFontSize == sizeOption
        Surface(
          shape = RoundedCornerShape(50),
          color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
          border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
          ),
          modifier = Modifier
            .clickable { onAppFontSizeChange(sizeOption) }
            .testTag("font_size_chip_${sizeOption.name.lowercase()}"),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            if (selected) {
              Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(15.dp),
              )
            }
            Text(
              text = sizeOption.displayName,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            )
          }
        }
      }
    }
  }
}
