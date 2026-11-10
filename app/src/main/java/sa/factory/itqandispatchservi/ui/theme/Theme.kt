package sa.factory.itqandispatchservi.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BrandPrimary,
    secondary = BrandSecondary,
    background = BrandBackground,
    surface = BrandSurface,
    surfaceVariant = BrandCard,
    onPrimary = Color.Black,
    onBackground = BrandTextPrimary,
    onSurface = BrandTextPrimary
)

@Composable
fun ItqanDispatchTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
