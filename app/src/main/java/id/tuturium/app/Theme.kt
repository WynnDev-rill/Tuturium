package id.tuturium.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

// A system-supplied casual handwriting family gives the interface the friendly
// handwritten feel of the supplied reference without bundling a proprietary font.
internal val DisplayFont = FontFamily.Cursive
internal val ReaderFont = FontFamily.Cursive
internal val HeroInk = Color.White
internal val HeroStart = Color(0xFF202020)
internal val HeroEnd = Color(0xFF292929)

private val Light = lightColorScheme(
    primary = Color(0xFF4756D9), onPrimary = Color.White,
    primaryContainer = Color(0xFFDFE2FF), onPrimaryContainer = Color(0xFF1B2479),
    secondary = Color(0xFF52566A), secondaryContainer = Color(0xFFE7E8F2),
    tertiary = Color(0xFF5261B8), background = Color(0xFFFAFAFA), onBackground = Color(0xFF111111),
    surface = Color.White, onSurface = Color(0xFF111111),
    surfaceVariant = Color(0xFFE9E9ED), onSurfaceVariant = Color(0xFF53535A),
    surfaceContainer = Color(0xFFF0F0F2), outline = Color(0xFF73737D), outlineVariant = Color(0xFFDDDDDF)
)
private val Dark = darkColorScheme(
    primary = Color(0xFF6875FF), onPrimary = Color.White,
    primaryContainer = Color(0xFF2B2E51), onPrimaryContainer = Color.White,
    secondary = Color(0xFFCACAE1), secondaryContainer = Color(0xFF303039),
    tertiary = Color(0xFF9BA5FF), background = Color.Black, onBackground = Color.White,
    surface = Color(0xFF202020), onSurface = Color.White,
    surfaceVariant = Color(0xFF303030), onSurfaceVariant = Color(0xFFB2B2B2),
    surfaceContainer = Color(0xFF242424), outline = Color(0xFF8C8C8C),
    outlineVariant = Color(0xFF3E3E3E)
)
private val base = Typography()
private val typography = base.copy(
    displayLarge = base.displayLarge.copy(fontFamily = DisplayFont),
    displayMedium = base.displayMedium.copy(fontFamily = DisplayFont),
    displaySmall = base.displaySmall.copy(fontFamily = DisplayFont),
    headlineLarge = base.headlineLarge.copy(fontFamily = DisplayFont, fontWeight = FontWeight.Bold),
    headlineMedium = base.headlineMedium.copy(fontFamily = DisplayFont, fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.copy(fontFamily = DisplayFont, fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.copy(fontFamily = DisplayFont, fontWeight = FontWeight.SemiBold),
    titleMedium = base.titleMedium.copy(fontFamily = DisplayFont, fontWeight = FontWeight.SemiBold),
    titleSmall = base.titleSmall.copy(fontFamily = DisplayFont),
    bodyLarge = base.bodyLarge.copy(fontFamily = DisplayFont),
    bodyMedium = base.bodyMedium.copy(fontFamily = DisplayFont),
    bodySmall = base.bodySmall.copy(fontFamily = DisplayFont),
    labelLarge = base.labelLarge.copy(fontFamily = DisplayFont),
    labelMedium = base.labelMedium.copy(fontFamily = DisplayFont),
    labelSmall = base.labelSmall.copy(fontFamily = DisplayFont)
)

@Composable
fun TuturiumTheme(mode: String, content: @Composable () -> Unit) {
    val dark = mode == "dark" || (mode == "system" && isSystemInDarkTheme())
    MaterialTheme(colorScheme = if (dark) Dark else Light, typography = typography, content = content)
}
