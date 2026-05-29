package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Blue Palettes
private val BlueLight = lightColorScheme(
    primary = Color(0xFF0F5CC0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E4FF),
    onPrimaryContainer = Color(0xFF001A43),
    secondary = Color(0xFF535F70),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7E3F8),
    onSecondaryContainer = Color(0xFF101C2B),
    background = Color(0xFFF9F9FF),
    surface = Color(0xFFFAF9FD),
    surfaceVariant = Color(0xFFE1E2EC),
    onSurfaceVariant = Color(0xFF44464F)
)
private val BlueDark = darkColorScheme(
    primary = Color(0xFFA9C7FF),
    onPrimary = Color(0xFF002F66),
    primaryContainer = Color(0xFF00458F),
    onPrimaryContainer = Color(0xFFD6E4FF),
    secondary = Color(0xFFBBC7DB),
    onSecondary = Color(0xFF253141),
    secondaryContainer = Color(0xFF3B4858),
    onSecondaryContainer = Color(0xFFD7E3F8),
    background = Color(0xFF111318),
    surface = Color(0xFF111318),
    surfaceVariant = Color(0xFF44464F),
    onSurfaceVariant = Color(0xFFC5C6D0)
)

// Purple Palettes
private val PurpleLight = lightColorScheme(
    primary = Color(0xFF8634B5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF7D8FF),
    onPrimaryContainer = Color(0xFF300049),
    secondary = Color(0xFFEA1E63),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFD9E2),
    onSecondaryContainer = Color(0xFF3F0013),
    background = Color(0xFFFCF8FC),
    surface = Color(0xFFFCF8FC),
    surfaceVariant = Color(0xFFEADBEC),
    onSurfaceVariant = Color(0xFF4C444D)
)
private val PurpleDark = darkColorScheme(
    primary = Color(0xFFEAB2FF),
    onPrimary = Color(0xFF51007A),
    primaryContainer = Color(0xFF6B159B),
    onPrimaryContainer = Color(0xFFF7D8FF),
    secondary = Color(0xFFFFB2C0),
    onSecondary = Color(0xFF650025),
    secondaryContainer = Color(0xFF8E0038),
    onSecondaryContainer = Color(0xFFFFD9E2),
    background = Color(0xFF161217),
    surface = Color(0xFF161217),
    surfaceVariant = Color(0xFF4C444D),
    onSurfaceVariant = Color(0xFFCECCD2)
)

// Green Palettes
private val GreenLight = lightColorScheme(
    primary = Color(0xFF046D38),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF9DF7B2),
    onPrimaryContainer = Color(0xFF00210C),
    secondary = Color(0xFF506352),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD2E8D3),
    onSecondaryContainer = Color(0xFF0D1F12),
    background = Color(0xFFF6FBF5),
    surface = Color(0xFFF6FBF5),
    surfaceVariant = Color(0xFFDDE5DB),
    onSurfaceVariant = Color(0xFF414942)
)
private val GreenDark = darkColorScheme(
    primary = Color(0xFF82DA98),
    onPrimary = Color(0xFF00391B),
    primaryContainer = Color(0xFF005229),
    onPrimaryContainer = Color(0xFF9DF7B2),
    secondary = Color(0xFFB7CCB7),
    onSecondary = Color(0xFF233426),
    secondaryContainer = Color(0xFF394B3C),
    onSecondaryContainer = Color(0xFFD2E8D3),
    background = Color(0xFF0F1511),
    surface = Color(0xFF0F1511),
    surfaceVariant = Color(0xFF414942),
    onSurfaceVariant = Color(0xFFC1C9BF)
)

// Orange Palettes
private val OrangeLight = lightColorScheme(
    primary = Color(0xFF904E00),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDCC0),
    onPrimaryContainer = Color(0xFF2E1500),
    secondary = Color(0xFF825515),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDDB1),
    onSecondaryContainer = Color(0xFF2A1700),
    background = Color(0xFFFFF8F5),
    surface = Color(0xFFFFF8F5),
    surfaceVariant = Color(0xFFF4E0D4),
    onSurfaceVariant = Color(0xFF52443C)
)
private val OrangeDark = darkColorScheme(
    primary = Color(0xFFFFB774),
    onPrimary = Color(0xFF4D2700),
    primaryContainer = Color(0xFF6E3A00),
    onPrimaryContainer = Color(0xFFFFDCC0),
    secondary = Color(0xFFFAB970),
    onSecondary = Color(0xFF472A00),
    secondaryContainer = Color(0xFF643F04),
    onSecondaryContainer = Color(0xFFFFDDB1),
    background = Color(0xFF17130F),
    surface = Color(0xFF17130F),
    surfaceVariant = Color(0xFF52443C),
    onSurfaceVariant = Color(0xFFD7C3B7)
)

// Pink Palettes
private val PinkLight = lightColorScheme(
    primary = Color(0xFF9E2A5D),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD9E4),
    onPrimaryContainer = Color(0xFF3E0020),
    secondary = Color(0xFF745661),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFD9E4),
    onSecondaryContainer = Color(0xFF2B151F),
    background = Color(0xFFFFF8F8),
    surface = Color(0xFFFFF8F8),
    surfaceVariant = Color(0xFFF2DDE2),
    onSurfaceVariant = Color(0xFF514347)
)
private val PinkDark = darkColorScheme(
    primary = Color(0xFFFFB0CB),
    onPrimary = Color(0xFF610034),
    primaryContainer = Color(0xFF7F0F46),
    onPrimaryContainer = Color(0xFFFFD9E4),
    secondary = Color(0xFFE2BDCB),
    onSecondary = Color(0xFF422933),
    secondaryContainer = Color(0xFF5B3E4A),
    onSecondaryContainer = Color(0xFFFFD9E4),
    background = Color(0xFF171214),
    surface = Color(0xFF171214),
    surfaceVariant = Color(0xFF514347),
    onSurfaceVariant = Color(0xFFD6C2C6)
)

// Neutral Palettes
private val NeutralLight = lightColorScheme(
    primary = Color(0xFF4F5F6B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3E4F2),
    onPrimaryContainer = Color(0xFF0B1C26),
    secondary = Color(0xFF575F65),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDBE3E9),
    onSecondaryContainer = Color(0xFF141C21),
    background = Color(0xFFF8F9FA),
    surface = Color(0xFFF8F9FA),
    surfaceVariant = Color(0xFFE0E2E5),
    onSurfaceVariant = Color(0xFF43474A)
)
private val NeutralDark = darkColorScheme(
    primary = Color(0xFFB7C8D5),
    onPrimary = Color(0xFF21323E),
    primaryContainer = Color(0xFF384854),
    onPrimaryContainer = Color(0xFFD3E4F2),
    secondary = Color(0xFFBFC7CD),
    onSecondary = Color(0xFF293136),
    secondaryContainer = Color(0xFF3F474C),
    onSecondaryContainer = Color(0xFFDBE3E9),
    background = Color(0xFF1A1C1E),
    surface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFF43474A),
    onSurfaceVariant = Color(0xFFC3C7CA)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeName: String = "blue",
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val supportsDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val colorScheme: ColorScheme = when (themeName.lowercase()) {
        "dynamic" -> {
            if (supportsDynamic) {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (darkTheme) BlueDark else BlueLight
            }
        }
        "purple" -> if (darkTheme) PurpleDark else PurpleLight
        "green" -> if (darkTheme) GreenDark else GreenLight
        "orange" -> if (darkTheme) OrangeDark else OrangeLight
        "pink" -> if (darkTheme) PinkDark else PinkLight
        "neutral" -> if (darkTheme) NeutralDark else NeutralLight
        else -> if (darkTheme) BlueDark else BlueLight // default to Blue
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
