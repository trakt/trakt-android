package tv.trakt.trakt.core.summary.ui.header

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import tv.trakt.trakt.common.core.translations.model.MediaTranslation
import tv.trakt.trakt.common.helpers.extensions.capitalize
import tv.trakt.trakt.common.helpers.extensions.mediumDateFormat
import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.MediaGenre.Anime
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.core.summary.ui.header.components.DetailsHeaderCreators
import tv.trakt.trakt.core.summary.ui.header.components.DetailsHeaderDate
import tv.trakt.trakt.core.summary.ui.header.components.DetailsHeaderMeta
import tv.trakt.trakt.core.summary.ui.header.components.DetailsHeaderRatings
import tv.trakt.trakt.core.summary.ui.header.components.DetailsHeaderStatus
import tv.trakt.trakt.core.summary.ui.header.components.DetailsHeaderTitle
import tv.trakt.trakt.core.summary.ui.header.components.toCreators
import tv.trakt.trakt.core.summary.ui.header.poster.DetailsHeaderPoster
import tv.trakt.trakt.resources.R

@Composable
internal fun MovieDetailsHeader(
    movie: Movie,
    movieTranslation: MediaTranslation?,
    ratings: ExternalRating?,
    creator: Person?,
    playsCount: Int?,
    creditsCount: Int?,
    loading: Boolean,
    notes: Boolean,
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
    val isReleased = remember { movie.isReleased }
    val isAnime = remember { movie.genres.contains(Anime) }

    DetailsHeader(
        poster = {
            DetailsHeaderPoster(
                imageUrl = movie.images?.getPosterUrl(Images.Size.MEDIUM),
                imagePlaceholderUrl = movie.images?.getPosterUrl(Images.Size.THUMB),
                accentColor = movie.colors?.colors?.first,
                loading = loading,
                creditsCount = creditsCount,
                playsCount = playsCount,
                watched = (playsCount ?: 0) > 0,
                watching = false,
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
                title = movie.title,
                translation = movieTranslation?.title,
            )
            DetailsHeaderCreators(
                label = stringResource(R.string.text_directed_by_short),
                creators = creator.toCreators(),
                onCreatorClick = onCreatorClick,
            )
        },
        details = {
            DetailsHeaderMeta(
                genres = movie.genres,
                date = {
                    DetailsHeaderDate(
                        text = when {
                            isReleased -> movie.yearString
                            else -> movie.released?.format(mediumDateFormat())?.capitalize() ?: movie.yearString
                        },
                        isReleased = isReleased,
                    )
                },
                runtime = movie.runtime,
                certification = movie.certification,
                onInfoClick = onInfoClick,
            )
            movie.status?.let { DetailsHeaderStatus(status = it) }
        },
        ratings = {
            DetailsHeaderRatings(
                traktRatings = movie.rating.ratingPercent.takeIf { isReleased },
                externalRatings = ratings,
                imdbId = movie.ids.imdb,
                malVisible = isAnime,
            )
        },
        modifier = modifier,
    )
}
