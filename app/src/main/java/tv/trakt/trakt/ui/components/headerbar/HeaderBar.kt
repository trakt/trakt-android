package tv.trakt.trakt.ui.components.headerbar

import android.annotation.SuppressLint
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import tv.trakt.trakt.MainActivity
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_HEADER_NEWS_1
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_HEADER_NEWS_2
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_HEADER_NEWS_ENABLED
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_HEADER_NEWS_URL
import tv.trakt.trakt.common.helpers.GreetingQuotes
import tv.trakt.trakt.common.helpers.extensions.nowLocal
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.core.auth.ConfigAuth
import tv.trakt.trakt.core.main.helpers.MediaModeProvider
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.MediaModeButtons
import tv.trakt.trakt.ui.components.buttons.TertiaryButton
import tv.trakt.trakt.ui.components.headerbar.model.HeaderNews
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.format.DateTimeFormatter
import java.util.Locale

private val todayDateFormat = DateTimeFormatter
    .ofPattern("EEEE, MMMM d, yyyy")
    .withLocale(Locale.US)

@Composable
internal fun HeaderBar(
    modifier: Modifier = Modifier,
    containerColor: Color = TraktTheme.colors.navigationHeaderContainer,
    containerAlpha: Float = 0.98F,
    title: String? = null,
    showVip: Boolean = false,
    showLogin: Boolean = false,
    showMediaButtons: Boolean = true,
    userVip: Boolean = false,
    userLoading: Boolean = false,
) {
    val mediaMode: MediaModeProvider = koinInject()
    val currentMediaMode = remember { mediaMode.getMode() }

    val scope = rememberCoroutineScope()
    val localActivity = LocalActivity.current
    val localMode = LocalInspectionMode.current
    val uriHandler = LocalUriHandler.current

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
            if (showMediaButtons) {
                MediaModeButtons(
                    mode = currentMediaMode,
                    onModeSelect = {
                        scope.launch {
                            mediaMode.setMode(it)
                        }
                    },
                )
            } else {
                @SuppressLint("UnusedCrossfadeTargetStateParameter")
                Crossfade(
                    targetState = showVip && !userVip,
                    label = "header_crossfade",
                    modifier = Modifier.weight(1F, fill = false),
                ) { vip ->
                    Box(
                        contentAlignment = Alignment.CenterEnd,
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

                        Column(
                            verticalArrangement = spacedBy(2.dp, Alignment.CenterVertically),
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
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
                                textAlign = TextAlign.Center,
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
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }

            if (showLogin) {
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

@Preview(widthDp = 400)
@Composable
private fun Preview() {
    TraktTheme {
        HeaderBar(
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
            userVip = false,
            showMediaButtons = false,
        )
    }
}

@Preview(widthDp = 400)
@Composable
private fun Preview3() {
    TraktTheme {
        HeaderBar(
            showLogin = true,
        )
    }
}

@Preview(widthDp = 400)
@Composable
private fun Preview4() {
    TraktTheme {
        HeaderBar(
            showVip = true,
            showLogin = true,
        )
    }
}
