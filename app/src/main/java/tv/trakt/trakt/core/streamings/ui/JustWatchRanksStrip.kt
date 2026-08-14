package tv.trakt.trakt.core.streamings.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import tv.trakt.trakt.common.core.streamings.model.StreamingsResult
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.openExternalAppLink
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import kotlin.math.absoluteValue

@Composable
internal fun JustWatchRanksStrip(
    ranks: StreamingsResult.Ranks?,
    justWatchLink: String?,
) {
    val context = LocalContext.current

    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(0.dp),
        modifier = Modifier.onClick {
            if (justWatchLink == null) {
                return@onClick
            }
            openExternalAppLink(
                context = context,
                packageId = "com.justwatch.justwatch",
                packageName = "justwatch",
                uri = "https://${justWatchLink.removePrefix("https://")}".toUri(),
            )
        },
    ) {
        val jwColor = when (TraktTheme.colors.isLight) {
            true -> 0xFFEEB705
            false -> 0xFFfcc405
        }
        Icon(
            painter = painterResource(R.drawable.ic_justwatch),
            contentDescription = null,
            tint = Color(jwColor),
            modifier = Modifier
                .size(14.dp),
        )
        Text(
            text = "JustWatch",
            color = Color(jwColor),
            style = TraktTheme.typography.meta,
            modifier = Modifier.padding(start = 2.dp),
        )

        AnimatedVisibility(
            visible = ranks?.rank != null,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.padding(start = 8.dp),
        ) {
            ranks?.let {
                Row(
                    verticalAlignment = CenterVertically,
                ) {
                    Text(
                        text = "${it.rank?.toString() ?: "-"}.",
                        color = Color(jwColor),
                        style = TraktTheme.typography.meta,
                    )

                    Row(
                        verticalAlignment = CenterVertically,
                        horizontalArrangement = spacedBy(0.dp),
                        modifier = Modifier.padding(start = 8.dp),
                    ) {
                        val delta = it.delta
                        val deltaText = when {
                            delta == null || delta == 0 -> "0"
                            delta > 0 -> "+$delta"
                            else -> "-${delta.absoluteValue}"
                        }
                        Text(
                            text = "($deltaText)",
                            color = when {
                                delta == null || delta == 0 -> TraktTheme.colors.textSecondary
                                delta > 0 -> Color(0xff6ce2ad)
                                else -> Color(0xffff5454)
                            },
                            style = TraktTheme.typography.meta,
                        )
                    }
                }
            }
        }
    }
}

@Preview(widthDp = 360)
@Composable
private fun Preview() {
    TraktThemeLightDark {
        Column(
            verticalArrangement = spacedBy(12.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            JustWatchRanksStrip(
                ranks = StreamingsResult.Ranks(
                    rank = 12,
                    delta = 5,
                    link = null,
                ),
                justWatchLink = "justwatch.com/us/movie/dune",
            )

            JustWatchRanksStrip(
                ranks = StreamingsResult.Ranks(
                    rank = 34,
                    delta = -7,
                    link = null,
                ),
                justWatchLink = "justwatch.com/us/movie/dune",
            )

            JustWatchRanksStrip(
                ranks = StreamingsResult.Ranks(
                    rank = 8,
                    delta = 0,
                    link = null,
                ),
                justWatchLink = "justwatch.com/us/movie/dune",
            )

            JustWatchRanksStrip(
                ranks = null,
                justWatchLink = null,
            )
        }
    }
}
