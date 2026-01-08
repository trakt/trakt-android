package tv.trakt.trakt.ui.components

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun TraktHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    maxSubtitleLength: Int = Int.MAX_VALUE,
    icon: Painter? = null,
    titleStyle: TextStyle = TraktTheme.typography.heading5,
    titleColor: Color = TraktTheme.colors.textPrimary,
) {
    Row(
        horizontalArrangement = spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = spacedBy(1.dp, Alignment.CenterVertically),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = spacedBy(6.dp),
            ) {
                icon?.let {
                    Icon(
                        painter = it,
                        contentDescription = null,
                        tint = titleColor,
                        modifier = Modifier
                            .size(15.dp),
                    )
                }
                Text(
                    text = title,
                    color = titleColor,
                    style = titleStyle,
                    maxLines = 1,
                    overflow = Ellipsis,
                )
            }
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = when {
                        subtitle.length > maxSubtitleLength -> subtitle.take(maxSubtitleLength).trimEnd() + "…"
                        else -> subtitle
                    },
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.meta.copy(fontWeight = W400),
                    maxLines = 1,
                    overflow = Ellipsis,
                )
            }
        }

//        Icon(
//            painter = painterResource(R.drawable.ic_cheveron_down),
//            contentDescription = null,
//            tint = TraktTheme.colors.textSecondary,
//            modifier = Modifier
//                .size(14.dp)
//                .graphicsLayer {
//                    translationY = 1.dp.toPx()
//                },
//        )
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        TraktHeader(
            title = "Title",
        )
    }
}

@Preview
@Composable
private fun Preview2() {
    TraktTheme {
        TraktHeader(
            title = "Title",
            subtitle = "Subtitle",
        )
    }
}

@Preview
@Composable
private fun Preview3() {
    TraktTheme {
        TraktHeader(
            title = "Title",
            subtitle = "Subtitle",
            icon = painterResource(R.drawable.ic_person_double),
        )
    }
}
