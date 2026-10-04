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
    onPrimary = Color(0xFF00372F),
    primaryContainer = Color(0xFF005045),
    onPrimaryContainer = Color(0xFF70F7DF),
    secondary = DarkSecondary,
    onSecondary = Color(0xFF422C00),
    secondaryContainer = Color(0xFF5E4000),
    onSecondaryContainer = Color(0xFFFFDEA3),
    tertiary = Color(0xFF90D3CA),
    background = DarkBg,
    onBackground = Color(0xFFE0E5E2),
    surface = DarkSurface,
    onSurface = Color(0xFFE0E5E2),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFC0CAC6),
    outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = QalamEmerald,
    onPrimary = Color.White,
    primaryContainer = EmeraldContainerLight,
    onPrimaryContainer = OnEmeraldContainerLight,
    secondary = QalamGold,
    onSecondary = Color.White,
    secondaryContainer = GoldContainerLight,
    onSecondaryContainer = OnGoldContainerLight,
    tertiary = QalamEmeraldLight,
    background = ParchmentLight,
    onBackground = InkBlack,
    surface = Color.White,
    onSurface = InkBlack,
    surfaceVariant = ParchmentCard,
    onSurfaceVariant = Color(0xFF454B48),
    outline = ParchmentBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep distinct brand identity
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
