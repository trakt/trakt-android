package tv.trakt.trakt.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.resources.R

private val SplineFamily = FontFamily(
    Font(R.font.spline_sans_light, FontWeight.W300),
    Font(R.font.spline_sans_regular, FontWeight.W400),
    Font(R.font.spline_sans_medium, FontWeight.W500),
    Font(R.font.spline_sans_semibold, FontWeight.W600),
    Font(R.font.spline_sans_bold, FontWeight.W700),
)

internal val Typography: TraktTypography = TraktTypography(
    heading1 = TextStyle(
        fontFamily = SplineFamily,
        fontWeight = FontWeight.W600,
        fontSize = 44.sp,
        letterSpacing = (-0.04).em,
    ),
    heading2 = TextStyle(
        fontFamily = SplineFamily,
        fontWeight = FontWeight.W600,
        fontSize = 32.sp,
        letterSpacing = (-0.01).em,
    ),
    heading3 = TextStyle(
        fontFamily = SplineFamily,
        fontWeight = FontWeight.W600,
        fontSize = 24.sp,
        letterSpacing = (-0.02).em,
    ),
    heading4 = TextStyle(
        fontFamily = SplineFamily,
        fontWeight = FontWeight.W600,
        fontSize = 20.sp,
        letterSpacing = (-0.04).em,
    ),
    heading5 = TextStyle(
        fontFamily = SplineFamily,
        fontWeight = FontWeight.W600,
        fontSize = 18.sp,
        letterSpacing = 0.em,
    ),
    heading6 = TextStyle(
        fontFamily = SplineFamily,
        fontWeight = FontWeight.W600,
        fontSize = 14.sp,
        letterSpacing = 0.04.em,
    ),
    meta = TextStyle(
        fontFamily = SplineFamily,
        fontWeight = FontWeight.W600,
        fontSize = 11.sp,
        lineHeight = 0.9.em,
        letterSpacing = 0.04.em,
    ),
    buttonPrimary = TextStyle(
        fontFamily = SplineFamily,
        fontWeight = FontWeight.W700,
        fontSize = 15.sp,
    ),
    buttonSecondary = TextStyle(
        fontFamily = SplineFamily,
        fontWeight = FontWeight.W600,
        fontSize = 14.sp,
        letterSpacing = 0.04.em,
    ),
    buttonTertiary = TextStyle(
        fontFamily = SplineFamily,
        fontWeight = FontWeight.W600,
        fontSize = 13.sp,
        letterSpacing = 0.04.em,
    ),
    paragraph = TextStyle(
        fontFamily = SplineFamily,
        fontWeight = FontWeight.W400,
        fontSize = 16.sp,
        lineHeight = 1.3.em,
    ),
    paragraphSmall = TextStyle(
        fontFamily = SplineFamily,
        fontWeight = FontWeight.W400,
        fontSize = 14.sp,
        lineHeight = 1.2.em,
    ),
    paragraphSmaller = TextStyle(
        fontFamily = SplineFamily,
        fontWeight = FontWeight.W400,
        fontSize = 13.sp,
        lineHeight = 1.2.em,
    ),
    cardTitle = TextStyle(
        fontFamily = SplineFamily,
        fontWeight = FontWeight.W500,
        fontSize = 12.sp,
        textAlign = TextAlign.Start,
        letterSpacing = (0.48).sp,
    ),
    cardSubtitle = TextStyle(
        fontFamily = SplineFamily,
        fontWeight = FontWeight.W400,
        fontSize = 12.sp,
        textAlign = TextAlign.Start,
        letterSpacing = (0.48).sp,
    ),
)

@Immutable
internal data class TraktTypography(
    val heading1: TextStyle = TextStyle.Default,
    val heading2: TextStyle = TextStyle.Default,
    val heading3: TextStyle = TextStyle.Default,
    val heading4: TextStyle = TextStyle.Default,
    val heading5: TextStyle = TextStyle.Default,
    val heading6: TextStyle = TextStyle.Default,
    val meta: TextStyle = TextStyle.Default,
    val buttonPrimary: TextStyle = TextStyle.Default,
    val buttonSecondary: TextStyle = TextStyle.Default,
    val buttonTertiary: TextStyle = TextStyle.Default,
    val paragraph: TextStyle = TextStyle.Default,
    val paragraphSmall: TextStyle = TextStyle.Default,
    val paragraphSmaller: TextStyle = TextStyle.Default,
    // Misc
    val cardTitle: TextStyle = TextStyle.Default,
    val cardSubtitle: TextStyle = TextStyle.Default,
)
