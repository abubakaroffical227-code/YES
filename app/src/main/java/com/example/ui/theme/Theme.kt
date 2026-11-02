package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp

enum class ThemePreference(val label: String) {
  LIGHT("Light"),
}

enum class AppFontSize(
  val displayName: String,
  val subtitle: String,
  val scale: Float,
) {
  SMALL(
    displayName = "Small",
    subtitle = "Compact text density (88%)",
    scale = 0.88f,
  ),
  NORMAL(
    displayName = "Default",
    subtitle = "Standard balanced readability (100%)",
    scale = 1.00f,
  ),
  LARGE(
    displayName = "Large",
    subtitle = "Enhanced reading comfort (114%)",
    scale = 1.14f,
  ),
  EXTRA_LARGE(
    displayName = "Extra Large",
    subtitle = "Maximum visibility & accessibility (126%)",
    scale = 1.26f,
  ),
}

private val YesLightColorScheme = lightColorScheme(
  primary = YesForestPrimary,
  onPrimary = YesOnPrimary,
  primaryContainer = YesPrimaryContainer,
  onPrimaryContainer = YesOnPrimaryContainer,
  secondary = YesEmeraldSecondary,
  onSecondary = YesOnSecondary,
  secondaryContainer = YesSecondaryContainer,
  onSecondaryContainer = YesOnSecondaryContainer,
  tertiary = YesAmberTertiary,
  onTertiary = YesOnTertiary,
  tertiaryContainer = YesTertiaryContainer,
  onTertiaryContainer = YesOnTertiaryContainer,
  background = YesBackgroundLight,
  onBackground = YesOnBackgroundLight,
  surface = YesSurfaceLight,
  onSurface = YesOnSurfaceLight,
  surfaceVariant = YesSurfaceVariantLight,
  onSurfaceVariant = YesOnSurfaceVariantLight,
  outline = YesOutlineLight,
)

val YesShapes = Shapes(
  extraSmall = RoundedCornerShape(8.dp),
  small = RoundedCornerShape(12.dp),
  medium = RoundedCornerShape(16.dp),
  large = RoundedCornerShape(22.dp),
  extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun MyApplicationTheme(
  themePreference: ThemePreference = ThemePreference.LIGHT,
  appFontSize: AppFontSize = AppFontSize.NORMAL,
  content: @Composable () -> Unit,
) {
  val baseDensity = LocalDensity.current
  val scaledDensity = Density(
    density = baseDensity.density,
    fontScale = appFontSize.scale,
  )

  CompositionLocalProvider(LocalDensity provides scaledDensity) {
    MaterialTheme(
      colorScheme = YesLightColorScheme,
      typography = Typography,
      shapes = YesShapes,
      content = content,
    )
  }
}
