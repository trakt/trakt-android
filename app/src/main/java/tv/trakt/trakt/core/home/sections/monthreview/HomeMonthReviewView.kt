package tv.trakt.trakt.core.home.sections.monthreview

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.ui.theme.colors.Purple600
import tv.trakt.trakt.common.ui.theme.colors.Shade10
import tv.trakt.trakt.common.ui.theme.colors.Shade900
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private val BannerBase = Shade900
private val BannerPill = Purple600
private val BannerForeground = Shade10
private val BannerShape = RoundedCornerShape(16.dp)
private val BannerHeight = 52.dp
private val PillMinWidth = 80.dp
private val TouchTargetSize = 48.dp

@Composable
internal fun HomeMonthReviewView(
    month: YearMonth,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onDismissClick: () -> Unit = {},
) {
    val configuration = LocalConfiguration.current
    val monthName = remember(configuration, month) {
        val locale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
        month.month.getDisplayName(TextStyle.FULL_STANDALONE, locale)
            .replaceFirstChar { it.titlecase(locale) }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .heightIn(min = BannerHeight)
            .shadow(4.dp, BannerShape)
            .background(BannerBase, BannerShape)
            .border(1.dp, BannerForeground.copy(alpha = 0.08F), BannerShape)
            .clip(BannerShape),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1F)
                .fillMaxHeight()
                .onClick(onClick = onClick),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = spacedBy(1.dp, Alignment.CenterVertically),
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(min = PillMinWidth)
                    .background(BannerPill)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = monthName,
                    color = BannerForeground,
                    style = TraktTheme.typography.paragraphSmall,
                    maxLines = 1,
                )
                Text(
                    text = stringResource(R.string.mir_banner_in_review),
                    color = BannerForeground.copy(alpha = 0.75F),
                    style = TraktTheme.typography.meta,
                    maxLines = 1,
                )
            }

            Text(
                text = stringResource(R.string.mir_banner_body_short, monthName),
                color = BannerForeground.copy(alpha = 0.9F),
                style = TraktTheme.typography.paragraphSmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1F)
                    .padding(horizontal = 12.dp),
            )
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(end = 2.dp)
                .size(TouchTargetSize)
                .onClick(onClick = onDismissClick),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = null,
                tint = BannerForeground.copy(alpha = 0.5F),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Preview(widthDp = 380)
@Composable
private fun Preview() {
    TraktTheme {
        HomeMonthReviewView(
            month = YearMonth.of(2026, 9),
            modifier = Modifier.padding(16.dp),
        )
    }
}
