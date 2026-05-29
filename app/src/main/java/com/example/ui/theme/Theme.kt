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

// 1. Monochrome (Crisp Minimalist Space)
private val MonochromeLight = lightColorScheme(
    primary = Color(0xFF000000), 
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF0F0F0),
    onPrimaryContainer = Color(0xFF000000),
    secondary = Color(0xFF555555),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFEBEBEB),
    onSecondaryContainer = Color(0xFF111111),
    background = Color(0xFFFFFFFF), 
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF3F3F5),
    onSurfaceVariant = Color(0xFF4A4A4D),
    outline = Color(0xFFD6D6D9),
    outlineVariant = Color(0xFFE5E5E8)
)

private val MonochromeDark = darkColorScheme(
    primary = Color(0xFFFFFFFF), 
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF333333),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFFAAAAAA),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF2C2C2C),
    onSecondaryContainer = Color(0xFFEBEBEB),
    background = Color(0xFF09090B), 
    onBackground = Color(0xFFF4F4F5),
    surface = Color(0xFF18181A),
    onSurface = Color(0xFFF4F4F5),
    surfaceVariant = Color(0xFF27272A), 
    onSurfaceVariant = Color(0xFFA1A1AA),
    outline = Color(0xFF52525B),
    outlineVariant = Color(0xFF3F3F46)
)

// 2. Ocean
private val OceanLight = lightColorScheme(
    primary = Color(0xFF005696), 
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE0F0FF),
    onPrimaryContainer = Color(0xFF001931),
    secondary = Color(0xFF006782),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD1F4FF),
    onSecondaryContainer = Color(0xFF001F29),
    background = Color(0xFFF9FBFC), 
    surface = Color(0xFFF9FBFC),
    surfaceVariant = Color(0xFFE6EAEF),
    onSurfaceVariant = Color(0xFF454b51),
    outline = Color(0xFFC4CAD2),
    outlineVariant = Color(0xFFD3DCE5)
)
private val OceanDark = darkColorScheme(
    primary = Color(0xFF91CCFF), 
    onPrimary = Color(0xFF00335C),
    primaryContainer = Color(0xFF004981),
    onPrimaryContainer = Color(0xFFD0E8FF),
    secondary = Color(0xFF86D2F0),
    onSecondary = Color(0xFF003546),
    secondaryContainer = Color(0xFF004D65),
    onSecondaryContainer = Color(0xFFC5E9F8),
    background = Color(0xFF0D141A), 
    onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF0D141A),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF1C252C), 
    onSurfaceVariant = Color(0xFF909fa8),
    outline = Color(0xFF3B4856),
    outlineVariant = Color(0xFF263341)
)

// 3. Forest
private val ForestLight = lightColorScheme(
    primary = Color(0xFF166B38),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD9F4DD),
    onPrimaryContainer = Color(0xFF00210C),
    secondary = Color(0xFF4D6352),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCFE9D3),
    onSecondaryContainer = Color(0xFF092011),
    background = Color(0xFFFAFDF9),
    surface = Color(0xFFFAFDF9),
    surfaceVariant = Color(0xFFDEE5DD),
    onSurfaceVariant = Color(0xFF424942),
    outline = Color(0xFFC2C9C1),
    outlineVariant = Color(0xFFD4DFD3)
)
private val ForestDark = darkColorScheme(
    primary = Color(0xFF8FD8A2),
    onPrimary = Color(0xFF00391B),
    primaryContainer = Color(0xFF005229),
    onPrimaryContainer = Color(0xFFABF5BD),
    secondary = Color(0xFFB3CCB7),
    onSecondary = Color(0xFF1F3526),
    secondaryContainer = Color(0xFF354B3B),
    onSecondaryContainer = Color(0xFFCFE9D3),
    background = Color(0xFF111411),
    onBackground = Color(0xFFE1E5E1),
    surface = Color(0xFF111411),
    onSurface = Color(0xFFE1E5E1),
    surfaceVariant = Color(0xFF212722),
    onSurfaceVariant = Color(0xFFC1C9BF),
    outline = Color(0xFF465046),
    outlineVariant = Color(0xFF2B332B)
)

// 4. Sunset
private val SunsetLight = lightColorScheme(
    primary = Color(0xFF8E4E00),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFE0CA),
    onPrimaryContainer = Color(0xFF2D1600),
    secondary = Color(0xFF755845),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDCC5),
    onSecondaryContainer = Color(0xFF2A1708),
    background = Color(0xFFFCFAF8),
    surface = Color(0xFFFCFAF8),
    surfaceVariant = Color(0xFFEBE1D9),
    onSurfaceVariant = Color(0xFF4C4640),
    outline = Color(0xFFCEC4BE),
    outlineVariant = Color(0xFFDFD1C7)
)
private val SunsetDark = darkColorScheme(
    primary = Color(0xFFFFB781),
    onPrimary = Color(0xFF4C2700),
    primaryContainer = Color(0xFF6C3A00),
    onPrimaryContainer = Color(0xFFFFDCC4),
    secondary = Color(0xFFE5C0A9),
    onSecondary = Color(0xFF422B1A),
    secondaryContainer = Color(0xFF5B4130),
    onSecondaryContainer = Color(0xFFFFDCC5),
    background = Color(0xFF161311),
    onBackground = Color(0xFFE6E0DD),
    surface = Color(0xFF161311),
    onSurface = Color(0xFFE6E0DD),
    surfaceVariant = Color(0xFF2A231F),
    onSurfaceVariant = Color(0xFFD6C2B7),
    outline = Color(0xFF544A43),
    outlineVariant = Color(0xFF3D322B)
)

// 5. Lavender
private val LavenderLight = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625B71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    background = Color(0xFFFDFBFD),
    surface = Color(0xFFFDFBFD),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFFCAC4D0),
    outlineVariant = Color(0xFFD9D3E0)
)
private val LavenderDark = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    background = Color(0xFF141216),
    onBackground = Color(0xFFE5E1E6),
    surface = Color(0xFF141216),
    onSurface = Color(0xFFE5E1E6),
    surfaceVariant = Color(0xFF242229),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF49454F),
    outlineVariant = Color(0xFF322E3A)
)

// 6. Rose
private val RoseLight = lightColorScheme(
    primary = Color(0xFF904256),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFD9DF),
    onPrimaryContainer = Color(0xFF3A0015),
    secondary = Color(0xFF75565D),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFD9E0),
    onSecondaryContainer = Color(0xFF2C151A),
    background = Color(0xFFFCFAFA),
    surface = Color(0xFFFCFAFA),
    surfaceVariant = Color(0xFFEBE0E2),
    onSurfaceVariant = Color(0xFF4C4446),
    outline = Color(0xFFCFC4C5),
    outlineVariant = Color(0xFFE0D2D5)
)
private val RoseDark = darkColorScheme(
    primary = Color(0xFFFFB1C2),
    onPrimary = Color(0xFF561429),
    primaryContainer = Color(0xFF722B3F),
    onPrimaryContainer = Color(0xFFFFD9DF),
    secondary = Color(0xFFE4BDC4),
    onSecondary = Color(0xFF432930),
    secondaryContainer = Color(0xFF5C3F46),
    onSecondaryContainer = Color(0xFFFFD9E0),
    background = Color(0xFF171213),
    onBackground = Color(0xFFE6E1E2),
    surface = Color(0xFF171213),
    onSurface = Color(0xFFE6E1E2),
    surfaceVariant = Color(0xFF2B2224),
    onSurfaceVariant = Color(0xFFD6C2C5),
    outline = Color(0xFF544A4D),
    outlineVariant = Color(0xFF3B2E32)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeName: String = "monochrome",
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val supportsDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val colorScheme = when {
        themeName == "dynamic" && supportsDynamic -> if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        themeName == "ocean" -> if (darkTheme) OceanDark else OceanLight
        themeName == "forest" -> if (darkTheme) ForestDark else ForestLight
        themeName == "sunset" -> if (darkTheme) SunsetDark else SunsetLight
        themeName == "lavender" -> if (darkTheme) LavenderDark else LavenderLight
        themeName == "rose" -> if (darkTheme) RoseDark else RoseLight
        else -> if (darkTheme) MonochromeDark else MonochromeLight // monochrome is default
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
