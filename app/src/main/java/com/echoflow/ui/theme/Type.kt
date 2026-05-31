package com.echoflow.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Expressive type scale.
 *
 * Material 3 Expressive uses bolder, larger display/headline styles with tightened tracking to
 * create stronger visual rhythm and let users locate key elements faster. We stay on the platform
 * sans-serif (Roboto Flex on modern Android) but push weight and size for the expressive feel.
 */
private val Sans = FontFamily.SansSerif

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Black,
        fontSize = 44.sp, lineHeight = 52.sp, letterSpacing = (-1).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.ExtraBold,
        fontSize = 34.sp, lineHeight = 42.sp, letterSpacing = (-0.5).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Bold,
        fontSize = 28.sp, lineHeight = 36.sp, letterSpacing = (-0.25).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.ExtraBold,
        fontSize = 30.sp, lineHeight = 38.sp, letterSpacing = (-0.5).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Bold,
        fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = (-0.25).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Bold,
        fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = 0.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Bold,
        fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.1.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.2.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.3.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Bold,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp,
    ),
)
