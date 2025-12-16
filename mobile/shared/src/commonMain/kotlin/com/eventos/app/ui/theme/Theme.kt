package com.eventos.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = OnPrimary,
    
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryLight,
    onSecondaryContainer = OnSecondary,
    
    tertiary = Info,
    onTertiary = OnPrimary,
    
    error = Error,
    onError = OnPrimary,
    
    background = Background,
    onBackground = OnBackground,
    
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDarkTheme,
    onPrimary = OnSurface,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = OnPrimary,
    
    secondary = SecondaryLight,
    onSecondary = OnSurface,
    secondaryContainer = SecondaryDark,
    onSecondaryContainer = OnPrimary,
    
    tertiary = Info,
    onTertiary = OnPrimary,
    
    error = Error,
    onError = OnPrimary,
    
    background = BackgroundDarkTheme,
    onBackground = OnSurfaceDarkTheme,
    
    surface = SurfaceDarkTheme,
    onSurface = OnSurfaceDarkTheme,
    surfaceVariant = Color(0xFF1E1E1E),
    onSurfaceVariant = Color(0xFFBDBDBD),
)

@Composable
fun EventosAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

