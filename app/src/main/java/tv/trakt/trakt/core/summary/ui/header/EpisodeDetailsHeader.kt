package tv.trakt.trakt.core.summary.ui.header

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.core.translations.model.MediaTranslation
import tv.trakt.trakt.common.helpers.coil.withFallback
import tv.trakt.trakt.common.helpers.extensions.capitalize
import tv.trakt.trakt.common.helpers.extensions.mediumDateFormat
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.summary.ui.header.components.DetailsHeaderCreators
import tv.trakt.trakt.core.summary.ui.header.components.DetailsHeaderDate
import tv.trakt.trakt.core.summary.ui.header.components.DetailsHeaderMeta
import tv.trakt.trakt.core.summary.ui.header.components.DetailsHeaderRatings
import tv.trakt.trakt.core.summary.ui.header.components.DetailsHeaderTitle
import tv.trakt.trakt.core.summary.ui.header.components.toCreators
import tv.trakt.trakt.core.summary.ui.header.poster.DetailsHeaderPosterHorizontal
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun EpisodeDetailsHeader(
    episode: Episode,
    show: Show,
    episodeTranslation: MediaTranslation?,
    ratings: ExternalRating?,
    creator: Person?,
    playsCount: Int?,
    spoilersHidden: Boolean,
    loading: Boolean,
    onShowClick: (Show) -> Unit,
    onCreatorClick: (Person) -> Unit,
    onShareClick: () -> Unit,
    onInfoClick: () -> Unit,
    onWatchedClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isReleased = episode.rememberReleased()

    DetailsHeader(
        poster = {
            DetailsHeaderPosterHorizontal(
                imageUrl = when {
                    spoilersHidden -> show.images?.getFanartUrl()
                    else -> episode.images?.getScreenshotUrl()
                        .withFallback(show.images?.getFanartUrl())
                },
                accentColor = null,
                loading = loading,
                creditsCount = 0,
                playsCount = playsCount,
                watched = (playsCount ?: 0) > 0,
                watching = false,
                onBackClick = onBackClick,
                onShareClick = onShareClick,
                onWatchedClick = onWatchedClick,
                modifier = Modifier.padding(
                    horizontal = TraktTheme.spacing.mainPageHorizontalSpace,
                ),
            )
        },
        title = {
            EpisodeShowLine(
                episode = episode,
                show = show,
                onShowClick = onShowClick,
            )
            DetailsHeaderTitle(
                title = when {
                    spoilersHidden -> episode.seasonEpisodeString()
                    else -> episode.title
                },
                translation = episodeTranslation?.title.takeUnless { spoilersHidden },
            )
            DetailsHeaderCreators(
                label = stringResource(R.string.text_directed_by_short),
                creators = creator.toCreators(),
                onCreatorClick = onCreatorClick,
            )
        },
        details = {
            DetailsHeaderMeta(
                genres = show.genres,
                date = {
                    DetailsHeaderDate(
                        text = when {
                            isReleased -> (episode.releasedAt?.toLocal()?.year ?: show.year).toString()
                            else -> episode.releasedAt?.toLocal()
                                ?.format(mediumDateFormat())
                                ?.capitalize()
                                ?: show.year.toString()
                        },
                        isReleased = isReleased,
                    )
                },
                runtime = episode.runtime,
                certification = show.certification,
                onInfoClick = onInfoClick,
            )
        },
        ratings = {
            DetailsHeaderRatings(
                traktRatings = episode.rating.ratingPercent.takeIf { isReleased },
                externalRatings = ratings,
                imdbId = episode.ids.imdb ?: show.ids.imdb,
                rottenVisible = false,
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun EpisodeShowLine(
    episode: Episode,
    show: Show,
    onShowClick: (Show) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(4.dp),
        modifier = Modifier.padding(
            top = 4.dp,
            bottom = 4.dp,
        ),
    ) {
        ShowLineText(
            text = show.title.uppercase(),
            color = TraktTheme.colors.textPrimary,
            modifier = Modifier.onClick(onClick = { onShowClick(show) }),
        )
        ShowLineText(
            text = "/",
            color = TraktTheme.colors.textSecondary,
        )
        ShowLineText(
            text = episode.seasonEpisode.toDisplayString(),
            color = TraktTheme.colors.textPrimary,
        )
    }
}

@Composable
private fun ShowLineText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val style = TraktTheme.typography.heading6.copy(fontSize = 13.sp)

    Text(
        text = text,
        color = color,
        style = style,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        autoSize = TextAutoSize.StepBased(
            maxFontSize = style.fontSize,
            minFontSize = 10.sp,
            stepSize = 1.sp,
        ),
        modifier = modifier,
    )
}
