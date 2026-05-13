package cn.edu.app.douyu.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DoyuDarkPrimary,
    secondary = DoyuMint,
    tertiary = DoyuSky,
    background = DoyuDarkBackground,
    surface = DoyuDarkSurface,
    onPrimary = Color(0xFF3A1018),
    onSecondary = Color(0xFF102A20),
    onTertiary = Color(0xFF11263A),
    onBackground = Color(0xFFFFF3F5),
    onSurface = Color(0xFFFFF3F5),
    outline = Color(0xFF72545E),
    error = DoyuError
)

private val LightColorScheme = lightColorScheme(
    primary = DoyuPetal,
    secondary = DoyuMint,
    tertiary = DoyuSky,
    background = DoyuCream,
    surface = DoyuSurface,
    surfaceVariant = DoyuSurfaceSoft,
    onPrimary = Color.White,
    onSecondary = DoyuText,
    onTertiary = DoyuText,
    onBackground = DoyuText,
    onSurface = DoyuText,
    onSurfaceVariant = DoyuTextMuted,
    outline = DoyuOutline,
    error = DoyuError
)

@Composable
fun DouYuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
