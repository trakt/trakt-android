package tv.trakt.trakt.app.ui.theme

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
        fontSize = 58.sp,
        letterSpacing = (-0.02).em,
    ),
    heading2 = TextStyle(
        fontFamily = SplineFamily,
        fontWeight = FontWeight.W600,
        fontSize = 46.sp,
        letterSpacing = (-0.02).em,
    ),
    heading3 = TextStyle(
        fontFamily = SplineFamily,
        fontWeight = FontWeight.W600,
        fontSize = 34.sp,
        letterSpacing = (-0.02).em,
    ),
    heading4 = TextStyle(
        fontFamily = SplineFamily,
        fontWeight = FontWeight.W600,
        fontSize = 26.sp,
        letterSpacing = (-0.02).em,
    ),
    heading5 = TextStyle(
        fontFamily = SplineFamily,
        fontWeight = FontWeight.W600,
        fontSize = 22.sp,
        letterSpacing = 0.em,
    ),
    heading6 = TextStyle(
        fontFamily = SplineFamily,
        fontWeight = FontWeight.W600,
        fontSize = 12.sp,
        letterSpacing = 0.04.em,
    ),
    paragraphLarge = TextStyle(
        fontFamily = SplineFamily,
        fontWeight = FontWeight.W400,
        fontSize = 16.sp,
        lineHeight = 1.2.em,
    ),
    paragraph = TextStyle(
        fontFamily = SplineFamily,
        fontWeight = FontWeight.W400,
        fontSize = 14.sp,
        lineHeight = 1.2.em,
    ),
    paragraphSmall = TextStyle(
        fontFamily = SplineFamily,
        fontWeight = FontWeight.W400,
        fontSize = 12.sp,
        lineHeight = 1.2.em,
    ),
    buttonPrimary = TextStyle(
        fontFamily = SplineFamily,
        fontWeight = FontWeight.W700,
        fontSize = 14.sp,
    ),
    buttonTertiary = TextStyle(
        fontFamily = SplineFamily,
        fontWeight = FontWeight.W600,
        fontSize = 12.sp,
        letterSpacing = 0.04.em,
    ),
    buttonSmall = TextStyle(
        fontFamily = SplineFamily,
        fontWeight = FontWeight.W700,
        fontSize = 10.sp,
    ),
    meta = TextStyle(
        fontFamily = SplineFamily,
        fontWeight = FontWeight.W600,
        fontSize = 10.sp,
        lineHeight = 0.9.em,
        letterSpacing = 0.04.em,
    ),
    // Misc
    navigationLabel = TextStyle(
        fontFamily = SplineFamily,
        fontWeight = FontWeight.W600,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        letterSpacing = 0.04.em,
    ),
    ratingLabel = TextStyle(
        fontFamily = SplineFamily,
        fontWeight = FontWeight.W700,
        fontSize = 16.sp,
        textAlign = TextAlign.Start,
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
    val paragraphLarge: TextStyle = TextStyle.Default,
    val paragraphSmall: TextStyle = TextStyle.Default,
    val paragraph: TextStyle = TextStyle.Default,
    val buttonPrimary: TextStyle = TextStyle.Default,
    val buttonTertiary: TextStyle = TextStyle.Default,
    val buttonSmall: TextStyle = TextStyle.Default,
    val meta: TextStyle = TextStyle.Default,
    // Misc
    val navigationLabel: TextStyle = TextStyle.Default,
    val ratingLabel: TextStyle = TextStyle.Default,
    val cardTitle: TextStyle = TextStyle.Default,
    val cardSubtitle: TextStyle = TextStyle.Default,
)
