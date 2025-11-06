package tv.trakt.trakt.ui.components.headerbar

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.jakewharton.processphoenix.ProcessPhoenix
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tv.trakt.trakt.MainActivity
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_HEADER_NEWS_1
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_HEADER_NEWS_2
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_HEADER_NEWS_ENABLED
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_HEADER_NEWS_URL
import tv.trakt.trakt.common.helpers.GreetingQuotes
import tv.trakt.trakt.common.helpers.extensions.nowLocal
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.ui.theme.colors.Orange100
import tv.trakt.trakt.common.ui.theme.colors.Orange500
import tv.trakt.trakt.core.auth.ConfigAuth
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.TertiaryButton
import tv.trakt.trakt.ui.components.headerbar.model.HeaderNews
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
    userLoading: Boolean = false,
    onProfileClick: () -> Unit = {},
) {
    val localActivity = LocalActivity.current
    val localMode = LocalInspectionMode.current
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()

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

    val news = remember {
        if (localMode) {
            HeaderNews()
        } else {
            val remoteConfig = Firebase.remoteConfig
            HeaderNews(
                enabled = remoteConfig.getBoolean(MOBILE_HEADER_NEWS_ENABLED),
                news1 = remoteConfig.getString(MOBILE_HEADER_NEWS_1),
                news2 = remoteConfig.getString(MOBILE_HEADER_NEWS_2),
                newsUrl = remoteConfig.getString(MOBILE_HEADER_NEWS_URL),
            )
        }
    }

    val halloween = remember {
        (localActivity as? MainActivity)?.halloweenConfig
    }

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
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Crossfade(
                targetState = showVip && !userVip,
                label = "HeaderBarVipCrossfade",
                modifier = Modifier
                    .weight(1F, fill = false),
            ) { vip ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.heightIn(min = contentHeight),
                ) {
                    val isNewsHeader = remember(news) {
                        news.enabled
                    }
                    val isNewsUrl = remember(news) {
                        news.newsUrl.isNotBlank()
                    }

                    val isHalloweenHeader = remember(halloween) {
                        halloween?.enabled == true && halloween.visible
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = spacedBy(2.dp),
                        modifier = Modifier.align(Alignment.Center),
                    ) {
                        Column(
                            verticalArrangement = spacedBy(2.dp, Alignment.CenterVertically),
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .onClick {
                                    if (isNewsHeader && isNewsUrl) {
                                        uriHandler.openUri(news.newsUrl)
                                    }
                                },
                        ) {
                            val todayQuote = remember(nowUtc().dayOfYear) {
                                GreetingQuotes.getTodayQuote()
                            }
                            Text(
                                text = when {
                                    isHalloweenHeader -> halloween?.header ?: todayQuote
                                    isNewsHeader -> news.news1
                                    !title.isNullOrBlank() -> title
                                    else -> todayQuote
                                },
                                color = TraktTheme.colors.textPrimary,
                                style = TraktTheme.typography.paragraphSmall.copy(fontWeight = W700),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = when {
                                    isHalloweenHeader -> halloween?.subheader ?: todayLabel
                                    isNewsHeader -> news.news2
                                    else -> todayLabel
                                },
                                color = TraktTheme.colors.textSecondary,
                                style = TraktTheme.typography.meta,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            when {
                showProfile -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = spacedBy(10.dp),
                        modifier = Modifier.height(contentHeight),
                    ) {
                        if (halloween?.visible == true) {
                            var isChecked by remember {
                                mutableStateOf(halloween.enabled)
                            }

                            Switch(
                                checked = isChecked,
                                onCheckedChange = {
                                    isChecked = it
                                    (localActivity as? MainActivity)?.toggleHalloween(it)
                                    scope.launch {
                                        delay(100)
                                        ProcessPhoenix.triggerRebirth(localActivity)
                                    }
                                },
                                thumbContent = {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_pumpkin),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                },
                                colors = SwitchDefaults.colors(
                                    checkedBorderColor = Orange100,
                                    checkedTrackColor = Orange100,
                                    checkedThumbColor = Orange500,
                                    checkedIconColor = White,
                                    uncheckedBorderColor = White,
                                    uncheckedTrackColor = White,
                                    uncheckedThumbColor = White,
                                    uncheckedIconColor = Orange500,
                                ),
                            )
                        }

//                        Box(
//                            modifier = Modifier
//                                .size(contentHeight)
//                                .onClick(onClick = onProfileClick),
//                        ) {
//                            val vipAccent = TraktTheme.colors.vipAccent
//                            val borderColor = remember(userVip) {
//                                if (userVip) vipAccent else White
//                            }
//                            if (userAvatar != null) {
//                                AsyncImage(
//                                    model = userAvatar,
//                                    contentDescription = "User avatar",
//                                    contentScale = ContentScale.Crop,
//                                    error = painterResource(R.drawable.ic_person_placeholder),
//                                    modifier = Modifier
//                                        .border(2.dp, borderColor, CircleShape)
//                                        .clip(CircleShape),
//                                )
//                            } else {
//                                Image(
//                                    painter = painterResource(R.drawable.ic_person_placeholder),
//                                    contentDescription = null,
//                                    modifier = Modifier
//                                        .border(2.dp, borderColor, CircleShape)
//                                        .clip(CircleShape),
//                                )
//                            }
//                        }
                    }
                }
                showJoinTrakt -> {
                    TertiaryButton(
                        text = stringResource(R.string.button_text_login),
                        icon = painterResource(R.drawable.ic_trakt_icon),
                        height = contentHeight,
                        loading = userLoading,
                        enabled = !userLoading,
                        onClick = {
                            uriHandler.openUri(ConfigAuth.authCodeUrl)
                        },
                    )
                }
            }
        }
    }
}

@Preview(widthDp = 400)
@Composable
private fun Preview() {
    TraktTheme {
        HeaderBar(
            showProfile = true,
            userVip = true,
        )
    }
}

@Preview(widthDp = 400)
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

@Preview(widthDp = 400)
@Composable
private fun Preview3() {
    TraktTheme {
        HeaderBar(
            showJoinTrakt = true,
        )
    }
}

@Preview(widthDp = 400)
@Composable
private fun Preview4() {
    TraktTheme {
        HeaderBar(
            showVip = true,
            showJoinTrakt = true,
        )
    }
}
