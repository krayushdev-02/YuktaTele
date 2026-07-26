package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = YuktaBlue,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = androidx.compose.ui.graphics.Color(0xFF2B2D35),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFFDBE1FF),
    secondary = YuktaPink,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFF23252A),
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFFC4C6D0),
    tertiary = YuktaPurple,
    background = DarkBg,
    surface = DarkSurface,
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF2B2D35),
    onBackground = androidx.compose.ui.graphics.Color.White,
    onSurface = androidx.compose.ui.graphics.Color.White,
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFC4C6D0)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = YuktaBlue,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = YuktaBlueLight,
    onPrimaryContainer = YuktaBlueDark,
    secondary = YuktaPink,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFFE1E2EC),
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFF44474E),
    tertiary = YuktaPurple,
    background = LightBg,
    surface = LightSurface,
    surfaceVariant = LightSurfaceElevated,
    onBackground = androidx.compose.ui.graphics.Color(0xFF1A1B1F),
    onSurface = androidx.compose.ui.graphics.Color(0xFF1A1B1F),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF44474E)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Set to false to force our beautiful customized theme
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
