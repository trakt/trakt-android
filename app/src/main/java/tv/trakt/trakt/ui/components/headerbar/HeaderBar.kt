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
import androidx.compose.foundation.layout.sizeIn
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import tv.trakt.trakt.common.Config
import tv.trakt.trakt.common.helpers.GreetingQuotes
import tv.trakt.trakt.common.helpers.extensions.nowLocal
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.VipChip
import tv.trakt.trakt.ui.components.buttons.TertiaryButton
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
    containerColor: Color = TraktTheme.colors.navigationHeaderContainer,
    containerAlpha: Float = 0.98F,
    title: String? = null,
    showVip: Boolean = false,
    showProfile: Boolean = false,
    showJoinTrakt: Boolean = false,
    userAvatar: String? = null,
    userVip: Boolean = false,
    onJoinClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
) {
    val contentHeight = 34.dp
    val headerBarHeight = WindowInsets.statusBars.asPaddingValues()
        .calculateTopPadding()
        .plus(TraktTheme.size.navigationHeaderHeight)

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
                )
                .sizeIn(minHeight = contentHeight),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = spacedBy(20.dp),
        ) {
            val uriHandler = LocalUriHandler.current
            Crossfade(
                targetState = showVip && !userVip,
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
                        verticalArrangement = spacedBy(2.dp, Alignment.CenterVertically),
                    ) {
                        Text(
                            text = title ?: GreetingQuotes.getTodayQuote(), // TODO
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

//            Icon(
//                painter = painterResource(R.drawable.ic_filter_off),
//                contentDescription = null,
//                tint = TraktTheme.colors.textPrimary,
//                modifier = Modifier.size(24.dp),
//            )

            when {
                showProfile -> {
                    Box(
                        modifier = Modifier
                            .size(contentHeight)
                            .onClick(onProfileClick),
                    ) {
                        val borderColor = remember(userVip) {
                            if (userVip) Color.Red else Color.White
                        }
                        if (userAvatar != null) {
                            AsyncImage(
                                model = userAvatar,
                                contentDescription = "User avatar",
                                contentScale = ContentScale.Crop,
                                error = painterResource(R.drawable.ic_person_placeholder),
                                modifier = Modifier
                                    .border(2.dp, borderColor, CircleShape)
                                    .clip(CircleShape),
                            )
                        } else {
                            Image(
                                painter = painterResource(R.drawable.ic_person_placeholder),
                                contentDescription = null,
                                modifier = Modifier
                                    .border(2.dp, borderColor, CircleShape)
                                    .clip(CircleShape),
                            )
                        }

                        if (userVip) {
                            Icon(
                                painter = painterResource(R.drawable.ic_crown),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .graphicsLayer {
                                        val offset = 4.dp
                                        translationX = offset.toPx()
                                        translationY = -offset.toPx()
                                    }
                                    .shadow(
                                        elevation = 1.dp,
                                        shape = CircleShape,
                                    )
                                    .background(Color.Red, shape = CircleShape)
                                    .size(18.dp)
                                    .padding(bottom = (4).dp, top = 3.dp),
                            )
                        }
                    }
                }
                showJoinTrakt -> {
                    TertiaryButton(
                        text = stringResource(R.string.button_text_join_trakt),
                        icon = painterResource(R.drawable.ic_plus_round),
                        height = contentHeight,
                        onClick = onJoinClick,
                    )
                }
            }
        }
    }
}

@Preview(widthDp = 350)
@Composable
private fun Preview() {
    TraktTheme {
        HeaderBar(
            showProfile = true,
            userVip = true,
        )
    }
}

@Preview(widthDp = 350)
@Composable
private fun Preview2() {
    TraktTheme {
        HeaderBar(
            showVip = true,
            showProfile = true,
            userVip = false,
        )
    }
}

@Preview(widthDp = 350)
@Composable
private fun Preview3() {
    TraktTheme {
        HeaderBar(
            showJoinTrakt = true,
        )
    }
}
