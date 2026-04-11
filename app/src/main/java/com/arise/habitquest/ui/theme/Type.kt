package com.arise.habitquest.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// To add custom fonts: place Rajdhani & Inter .ttf files in res/font/
// then replace FontFamily.SansSerif with Font(...) references.
val RajdhaniFamily: FontFamily = FontFamily.SansSerif   // Replace with Rajdhani TTFs
val InterFamily: FontFamily = FontFamily.SansSerif       // Replace with Inter TTFs

val AriseTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = RajdhaniFamily, fontWeight = FontWeight.Bold,
        fontSize = 57.sp, lineHeight = 64.sp, letterSpacing = (-0.25).sp, color = TextPrimary
    ),
    displayMedium = TextStyle(
        fontFamily = RajdhaniFamily, fontWeight = FontWeight.Bold,
        fontSize = 45.sp, lineHeight = 52.sp, letterSpacing = 0.sp, color = TextPrimary
    ),
    displaySmall = TextStyle(
        fontFamily = RajdhaniFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp, lineHeight = 44.sp, letterSpacing = 0.sp, color = TextPrimary
    ),
    headlineLarge = TextStyle(
        fontFamily = RajdhaniFamily, fontWeight = FontWeight.Bold,
        fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = 1.sp, color = TextPrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = RajdhaniFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp, lineHeight = 36.sp, letterSpacing = 0.5.sp, color = TextPrimary
    ),
    headlineSmall = TextStyle(
        fontFamily = RajdhaniFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = 0.5.sp, color = TextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = InterFamily, fontWeight = FontWeight.Bold,
        fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = 0.sp, color = TextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = InterFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp, color = TextPrimary
    ),
    titleSmall = TextStyle(
        fontFamily = InterFamily, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp, color = TextSecondary
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFamily, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp, color = TextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFamily, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp, color = TextSecondary
    ),
    bodySmall = TextStyle(
        fontFamily = InterFamily, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp, color = TextDim
    ),
    labelLarge = TextStyle(
        fontFamily = RajdhaniFamily, fontWeight = FontWeight.Bold,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 1.5.sp, color = TextPrimary
    ),
    labelMedium = TextStyle(
        fontFamily = RajdhaniFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 1.25.sp, color = TextSecondary
    ),
    labelSmall = TextStyle(
        fontFamily = RajdhaniFamily, fontWeight = FontWeight.Medium,
        fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 1.sp, color = TextDim
    )
)

// System voice text style — monospace italic, lavender tint
val SystemTextStyle = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Normal,
    fontStyle = FontStyle.Italic,
    fontSize = 13.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.5.sp,
    color = TextSystem
)

// Rank letter in badge
val RankLetterStyle = TextStyle(
    fontFamily = RajdhaniFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
    letterSpacing = 2.sp,
    color = TextPrimary
)
