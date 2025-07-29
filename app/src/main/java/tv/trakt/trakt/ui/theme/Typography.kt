package tv.trakt.trakt.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.R

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
        letterSpacing = (-0.04).em,
    ),
    heading3 = TextStyle(
        fontFamily = SplineFamily,
        fontWeight = FontWeight.W600,
        fontSize = 24.sp,
        letterSpacing = (-0.04).em,
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
        fontSize = 16.sp,
        letterSpacing = 0.04.em,
    ),
    meta = TextStyle(
        fontFamily = SplineFamily,
        fontWeight = FontWeight.W600,
        fontSize = 10.sp,
        lineHeight = 0.9.em,
        letterSpacing = 0.04.em,
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
)
