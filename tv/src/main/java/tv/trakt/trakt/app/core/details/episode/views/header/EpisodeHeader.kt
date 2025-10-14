package tv.trakt.trakt.app.core.details.episode.views.header

import ExternalRatingsStrip
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tv.trakt.trakt.app.common.ui.InfoChip
import tv.trakt.trakt.app.core.details.ui.PosterImage
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.extensions.longDateTimeFormat
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.thousandsFormat
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.Images.Size.MEDIUM
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.resources.R

@Composable
internal fun EpisodeHeader(
    show: Show,
    episode: Episode,
    episodePlays: Int,
    externalRating: ExternalRating?,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester,
    onFocused: (String) -> Unit,
    onPosterUnfocused: () -> Unit,
    onBackdropFocused: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var isPosterFocused by remember { mutableStateOf(false) }

    Row(
        horizontalArrangement = spacedBy(24.dp, Alignment.Start),
        verticalAlignment = Alignment.Bottom,
        modifier = modifier
            .padding(
                start = TraktTheme.spacing.mainContentStartSpace,
                top = TraktTheme.spacing.mainContentVerticalSpace,
            )
            .fillMaxWidth()
            .height(TraktTheme.size.detailsPosterSize),
    ) {
        PosterImage(
            posterUrl = show.images?.getPosterUrl(size = MEDIUM),
            modifier = Modifier
                .onKeyEvent {
                    if (!isPosterFocused) {
                        return@onKeyEvent false
                    }
                    if (it.type == KeyEventType.KeyUp && it.key == Key.DirectionUp) {
                        onBackdropFocused()
                        return@onKeyEvent true
                    }
                    return@onKeyEvent false
                }
                .onFocusChanged {
                    if (it.isFocused) {
                        onFocused("poster")
                    } else {
                        onPosterUnfocused()
                    }
                    scope.launch {
                        delay(100)
                        isPosterFocused = it.isFocused
                    }
                }
                .focusRequester(focusRequester)
                .onClick {
                    onPosterUnfocused()
                },
        )

        Column {
            Text(
                text = episode.title,
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading3,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = TraktTheme.spacing.mainContentEndSpace),
            )

            Text(
                text = show.title,
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading6,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(
                        end = TraktTheme.spacing.mainContentEndSpace,
                    ),
            )

            Row(
                horizontalArrangement = spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 16.dp),
            ) {
                // Season, Episode
                Row(
                    horizontalArrangement = spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_shows_off),
                        contentDescription = "Genres",
                        tint = TraktTheme.colors.textSecondary,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = stringResource(
                            R.string.text_season_episode_number,
                            episode.season,
                            episode.number,
                        ),
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.heading6,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Release date
                episode.firstAired?.let {
                    Row(
                        horizontalArrangement = spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_calendar),
                            contentDescription = "Genres",
                            tint = TraktTheme.colors.textSecondary,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = it.toLocal().format(longDateTimeFormat),
                            color = TraktTheme.colors.textSecondary,
                            style = TraktTheme.typography.heading6,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Column(
                verticalArrangement = spacedBy(8.dp),
                modifier = Modifier.padding(top = 16.dp),
            ) {
                // Ratings
                Row(
                    horizontalArrangement = spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                ) {
                    val hidden = remember(episode.firstAired) {
                        val firstAired = episode.firstAired
                        firstAired == null || firstAired.isAfter(nowUtc())
                    }

                    Row(
                        horizontalArrangement = spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val grayFilter = remember {
                            ColorFilter.colorMatrix(
                                ColorMatrix().apply {
                                    setToSaturation(0F)
                                },
                            )
                        }
                        val redFilter = remember {
                            ColorFilter.tint(Red500)
                        }

                        Image(
                            painter = painterResource(R.drawable.ic_heart),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            colorFilter = if (episode.rating.rating > 0 && !hidden) redFilter else grayFilter,
                        )
                        Text(
                            text = if (episode.rating.rating > 0 && !hidden) {
                                "${episode.rating.ratingPercent}%"
                            } else {
                                "-"
                            },
                            color = TraktTheme.colors.textPrimary,
                            style = TraktTheme.typography.ratingLabel,
                        )

                        if (episode.rating.rating > 0 && !hidden) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.align(Alignment.Top),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = null,
                                    tint = TraktTheme.colors.textSecondary,
                                    modifier = Modifier.size(12.5.dp),
                                )
                                Text(
                                    text = episode.rating.votes.thousandsFormat(),
                                    color = TraktTheme.colors.textSecondary,
                                    style = TraktTheme.typography.ratingLabel.copy(fontSize = 12.sp),
                                )
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = externalRating != null,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        ExternalRatingsStrip(
                            externalRating = externalRating,
                            hidden = hidden,
                        )
                    }
                }

                // Info chips
                if (show.certification != null || episode.firstAired != null) {
                    Row(
                        horizontalArrangement = spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (episodePlays > 1) {
                            InfoChip(
                                text = stringResource(R.string.tag_text_watch_count, episodePlays).uppercase(),
                                containerColor = TraktTheme.colors.accent,
                            )
                        }
                        show.certification?.let {
                            InfoChip(text = it)
                        }
                        episode.firstAired?.let {
                            InfoChip(text = it.year.toString())
                        }
                    }
                }
            }
        }
    }
}
