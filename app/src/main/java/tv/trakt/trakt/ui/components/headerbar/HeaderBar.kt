package tv.trakt.trakt.ui.components.headerbar

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.Config
import tv.trakt.trakt.common.R
import tv.trakt.trakt.common.helpers.extensions.nowLocal
import tv.trakt.trakt.ui.components.VipChip
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private val todayDateFormat = DateTimeFormatter
    .ofLocalizedDate(FormatStyle.FULL)
    .withLocale(Locale.US)

@Composable
internal fun HeaderBar(
    modifier: Modifier = Modifier,
    height: Dp = TraktTheme.size.navigationHeaderHeight,
    containerColor: Color = TraktTheme.colors.navigationHeaderContainer,
    containerAlpha: Float = 0.98F,
    showVip: Boolean = false,
) {
    val headerBarHeight = WindowInsets.statusBars.asPaddingValues()
        .calculateTopPadding()
        .plus(height)

    val todayLabel = remember {
        nowLocal().format(todayDateFormat)
    }

    val animatedContainerAlpha by animateFloatAsState(
        targetValue = containerAlpha,
        animationSpec = tween(),
    )

    Box(
        modifier = modifier
            .height(headerBarHeight)
            .clip(
                RoundedCornerShape(
                    bottomStart = 24.dp,
                    bottomEnd = 24.dp,
                ),
            )
            .background(containerColor.copy(alpha = animatedContainerAlpha)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(
                    start = TraktTheme.spacing.mainPageHorizontalSpace,
                    end = TraktTheme.spacing.mainPageHorizontalSpace,
                    bottom = TraktTheme.spacing.mainPageHorizontalSpace,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = spacedBy(20.dp),
        ) {
            val uriHandler = LocalUriHandler.current
            Crossfade(
                targetState = showVip,
                label = "HeaderBarVipCrossfade",
            ) { state ->
                if (state) {
                    VipChip(
                        onClick = {
                            uriHandler.openUri(Config.WEB_VIP_URL)
                        },
                    )
                } else {
                    Column(
                        verticalArrangement = spacedBy(1.dp, Alignment.CenterVertically),
                    ) {
                        Text(
                            text = "Hello there!",
                            color = TraktTheme.colors.textPrimary,
                            style = TraktTheme.typography.paragraphSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = todayLabel,
                            color = TraktTheme.colors.textSecondary,
                            style = TraktTheme.typography.meta,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1F))

            Icon(
                painter = painterResource(R.drawable.ic_filter_off),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier.size(24.dp),
            )

            Image(
                painter = painterResource(R.drawable.ic_person_placeholder),
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .border(2.dp, Color.White, CircleShape)
                    .clip(CircleShape),
            )
        }
    }
}

@Preview(widthDp = 320)
@Composable
private fun Preview() {
    TraktTheme {
        HeaderBar()
    }
}

@Preview(widthDp = 320)
@Composable
private fun Preview2() {
    TraktTheme {
        HeaderBar(
            showVip = true,
        )
    }
}
