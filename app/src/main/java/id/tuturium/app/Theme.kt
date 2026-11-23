package id.tuturium.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

internal val DisplayFont = FontFamily(Font(R.font.cormorant_garamond))
internal val ReaderFont = FontFamily(Font(R.font.crimson_pro))
internal val HeroInk = Color(0xFFF9F6FF)
internal val HeroStart = Color(0xFF27235E)
internal val HeroEnd = Color(0xFF453299)

private val Light = lightColorScheme(
    primary = Color(0xFF503CBC), onPrimary = Color.White,
    primaryContainer = Color(0xFFE8E1FF), onPrimaryContainer = Color(0xFF281565),
    secondary = Color(0xFF5B5276), secondaryContainer = Color(0xFFEAE3FF),
    tertiary = Color(0xFF684487), background = Color(0xFFF8F7FC), onBackground = Color(0xFF1F1A33),
    surface = Color(0xFFFFFBFF), onSurface = Color(0xFF1F1A33),
    surfaceVariant = Color(0xFFECE7F4), onSurfaceVariant = Color(0xFF564E69),
    surfaceContainer = Color(0xFFF0ECF7), outline = Color(0xFF776F87), outlineVariant = Color(0xFFDDD6E9)
)
private val Dark = darkColorScheme(
    primary = Color(0xFFCCC0FF), onPrimary = Color(0xFF2D1A71),
    primaryContainer = Color(0xFF3F2B8C), onPrimaryContainer = Color(0xFFF1E9FF),
    secondary = Color(0xFFCCC1DF), secondaryContainer = Color(0xFF423852),
    tertiary = Color(0xFFE1B9FF), background = Color(0xFF151222), onBackground = Color(0xFFF2EBFC),
    surface = Color(0xFF1C182B), onSurface = Color(0xFFF2EBFC),
    surfaceVariant = Color(0xFF393149), onSurfaceVariant = Color(0xFFCEC5DD),
    surfaceContainer = Color(0xFF252034), outline = Color(0xFF9D93AD), outlineVariant = Color(0xFF4A415C)
)
private val base = Typography()
private val typography = base.copy(
    displayMedium = base.displayMedium.copy(fontFamily = DisplayFont, fontWeight = FontWeight.SemiBold),
    displaySmall = base.displaySmall.copy(fontFamily = DisplayFont, fontWeight = FontWeight.SemiBold),
    headlineLarge = base.headlineLarge.copy(fontFamily = DisplayFont, fontWeight = FontWeight.SemiBold),
    headlineMedium = base.headlineMedium.copy(fontFamily = DisplayFont, fontWeight = FontWeight.SemiBold),
    titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold)
)

@Composable
fun TuturiumTheme(mode: String, content: @Composable () -> Unit) {
    val dark = mode == "dark" || (mode == "system" && isSystemInDarkTheme())
    MaterialTheme(colorScheme = if (dark) Dark else Light, typography = typography, content = content)
}
