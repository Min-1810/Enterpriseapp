package com.fpt.enterpriseapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = GradientStart,
    onPrimary = Color.White,
    primaryContainer = GradientEnd,
    onPrimaryContainer = Color.White,
    secondary = GradientOrangeStart,
    onSecondary = Color.White,
    background = SurfaceLight,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceLight,
    onSurfaceVariant = TextSecondary,
    error = StatusCancelled,
    onError = Color.White,
    outline = DividerColor
)

// DARK MODE
private val DarkColors = darkColorScheme(
    primary = GradientStart,
    onPrimary = Color.White,
    primaryContainer = GradientEnd,
    onPrimaryContainer = Color.White,
    secondary = GradientOrangeStart,
    onSecondary = Color.White,
    background = Color(0xFF121212), // Nền đen xám
    onBackground = Color(0xFFE0E0E0), // Chữ trắng xám
    surface = Color(0xFF1E1E1E), // Nền thẻ xám đậm
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFB0B0B0),
    error = StatusCancelled,
    onError = Color.White,
    outline = Color(0xFF444444)
)

@Composable
fun FptEnterpriseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Tự động lấy theo cài đặt điện thoại
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current

    // Đổi màu thanh trạng thái (Status Bar) cho phù hợp
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}