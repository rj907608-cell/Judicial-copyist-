package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color(0xFF06182C),
    primaryContainer = Color(0xFF134074),
    onPrimaryContainer = Color(0xFFD0E1FD),
    secondary = DarkSecondary,
    onSecondary = Color(0xFF422C00),
    secondaryContainer = Color(0xFF5E4000),
    onSecondaryContainer = Color(0xFFFFDEA3),
    tertiary = WordDocBlueLight,
    background = DarkBg,
    onBackground = Color(0xFFF1F5F9),
    surface = DarkSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = DarkBorder
)

// Premium Daylight Judicial Theme
private val LightColorScheme = lightColorScheme(
    primary = JudicialNavy,
    onPrimary = Color.White,
    primaryContainer = NavyContainerLight,
    onPrimaryContainer = OnNavyContainerLight,
    secondary = JudicialGold,
    onSecondary = Color.White,
    secondaryContainer = GoldContainerLight,
    onSecondaryContainer = OnGoldContainerLight,
    tertiary = WordDocBlue,
    background = SlateLightBg,
    onBackground = SlateText,
    surface = SlateCardBg,
    onSurface = SlateText,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = SlateMuted,
    outline = SlateBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Pure crisp daylight theme by default as requested!
    dynamicColor: Boolean = false, // Keep distinct royal judicial brand identity
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
