package com.raktaseva.connect.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BloodRed = Color(0xFFD32F2F)
val EmergencyOrange = Color(0xFFFF6F00)
val SuccessGreen = Color(0xFF388E3C)
val WarningYellow = Color(0xFFFBC02D)
val AppBackground = Color(0xFFF5F5F5)
val TextPrimary = Color(0xFF212121)

private val RaktaColorScheme = lightColorScheme(
    primary = BloodRed,
    secondary = EmergencyOrange,
    tertiary = SuccessGreen,
    background = AppBackground,
    surface = Color.White,
    onPrimary = Color.White,
    onSurface = TextPrimary
)

@Composable
fun RaktaSevaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RaktaColorScheme,
        content = content
    )
}
