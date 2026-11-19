package tv.trakt.trakt.core.summary.ui.header

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.core.translations.model.MediaTranslation
import tv.trakt.trakt.common.helpers.extensions.capitalize
import tv.trakt.trakt.common.helpers.extensions.mediumDateFormat
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.Images.Size
import tv.trakt.trakt.common.model.MediaGenre.Anime
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.summary.ui.header.components.DetailsHeaderCreators
import tv.trakt.trakt.core.summary.ui.header.components.DetailsHeaderDate
import tv.trakt.trakt.core.summary.ui.header.components.DetailsHeaderMeta
import tv.trakt.trakt.core.summary.ui.header.components.DetailsHeaderRatings
import tv.trakt.trakt.core.summary.ui.header.components.DetailsHeaderStatus
import tv.trakt.trakt.core.summary.ui.header.components.DetailsHeaderTitle
import tv.trakt.trakt.core.summary.ui.header.poster.DetailsHeaderPoster
import tv.trakt.trakt.resources.R

@Composable
internal fun ShowDetailsHeader(
    show: Show,
    showTranslation: MediaTranslation?,
    ratings: ExternalRating?,
    creators: ImmutableList<Person>?,
    loading: Boolean,
    notes: Boolean,
    watched: Boolean,
    watching: Boolean,
    onCreatorClick: (Person) -> Unit,
    onShareClick: () -> Unit,
    onShareImageClick: () -> Unit,
    onNotesClick: () -> Unit,
    onInfoClick: () -> Unit,
    onWatchedClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    posterOverlay: (@Composable (onReady: (Boolean) -> Unit) -> Unit)? = null,
) {
    val isReleased = show.rememberReleased()
    val isAnime = remember { show.genres.contains(Anime) }

    DetailsHeader(
        poster = {
            DetailsHeaderPoster(
                imageUrl = show.images?.getPosterUrl(Size.MEDIUM),
                imagePlaceholderUrl = show.images?.getPosterUrl(Size.THUMB),
                accentColor = show.colors?.colors?.first,
                loading = loading,
                creditsCount = null,
                playsCount = null,
                watched = watched,
                watching = watching,
                personImdb = null,
                onBackClick = onBackClick,
                onShareClick = onShareClick,
                onShareImageClick = onShareImageClick,
                onWatchedClick = onWatchedClick,
                extraRightColumn = {
                    DetailsHeaderNotesButton(
                        visible = notes,
                        onClick = onNotesClick,
                    )
                },
                posterOverlay = posterOverlay,
            )
        },
        title = {
            DetailsHeaderTitle(
                title = show.title,
                translation = showTranslation?.title,
            )
            DetailsHeaderCreators(
                label = stringResource(R.string.text_created_by_short),
                creators = creators,
                onCreatorClick = onCreatorClick,
            )
        },
        details = {
            DetailsHeaderMeta(
                genres = show.genres,
                date = {
                    DetailsHeaderDate(
                        text = when {
                            isReleased -> (show.releasedAt?.toLocal()?.year ?: show.year).toString()
                            else -> show.releasedAt?.toLocal()?.format(mediumDateFormat())?.capitalize()
                                ?: show.year.toString()
                        },
                        isReleased = isReleased,
                    )
                },
                episodesCount = show.airedEpisodes,
                certification = show.certification,
                onInfoClick = onInfoClick,
            )
            show.status?.let { DetailsHeaderStatus(status = it) }
        },
        ratings = {
            DetailsHeaderRatings(
                traktRatings = show.rating.ratingPercent.takeIf { isReleased },
                externalRatings = ratings,
                imdbId = show.ids.imdb,
                showId = show.ids.trakt,
                malVisible = isAnime,
            )
        },
        modifier = modifier,
    )
}
