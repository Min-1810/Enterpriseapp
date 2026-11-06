package com.fpt.enterpriseapp.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Gradient colors (mirror với colors.xml)
val GradientStart = Color(0xFF667EEA)
val GradientEnd   = Color(0xFF764BA2)

val GradientOrangeStart = Color(0xFFF37021)
val GradientOrangeEnd   = Color(0xFFFF9A56)

val GradientBlueStart = Color(0xFF4FACFE)
val GradientBlueEnd   = Color(0xFF00F2FE)

val GradientGreenStart = Color(0xFF43E97B)
val GradientGreenEnd   = Color(0xFF38F9D7)

val GradientPinkStart = Color(0xFFFF6B9D)
val GradientPinkEnd   = Color(0xFFFFA07A)

val GradientPurpleStart = Color(0xFFA770EF)
val GradientPurpleEnd   = Color(0xFFCF8BF3)

// Surfaces
val SurfaceLight   = Color(0xFFF8F9FF)
val SurfaceCard    = Color(0xFFFFFFFF)
val TextPrimary    = Color(0xFF1A1A2E)
val TextSecondary  = Color(0xFF6B7280)
val DividerColor   = Color(0xFFE5E7EB)

// Status
val StatusPending   = Color(0xFFF59E0B)
val StatusProgress  = Color(0xFF3B82F6)
val StatusDone      = Color(0xFF10B981)
val StatusCancelled = Color(0xFFEF4444)

// Brush tiện dụng
val PrimaryGradientBrush = Brush.linearGradient(listOf(GradientStart, GradientEnd))
val OrangeGradientBrush  = Brush.linearGradient(listOf(GradientOrangeStart, GradientOrangeEnd))
val BlueGradientBrush    = Brush.linearGradient(listOf(GradientBlueStart, GradientBlueEnd))
val GreenGradientBrush   = Brush.linearGradient(listOf(GradientGreenStart, GradientGreenEnd))
val PinkGradientBrush    = Brush.linearGradient(listOf(GradientPinkStart, GradientPinkEnd))
val PurpleGradientBrush  = Brush.linearGradient(listOf(GradientPurpleStart, GradientPurpleEnd))