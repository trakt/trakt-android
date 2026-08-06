package tv.trakt.trakt.core.summary.ui.header

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import tv.trakt.trakt.common.Config.webImdbMediaUrl
import tv.trakt.trakt.common.core.translations.model.MediaTranslation
import tv.trakt.trakt.common.helpers.extensions.capitalize
import tv.trakt.trakt.common.helpers.extensions.mediumDateFormat
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.openExternalAppLink
import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.MediaGenre.Anime
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun DetailsHeader(
    movie: Movie,
    movieTranslation: MediaTranslation?,
    ratings: ExternalRating?,
    creator: Person?,
    playsCount: Int?,
    creditsCount: Int?,
    loading: Boolean,
    onCreatorClick: (Person) -> Unit,
    onShareClick: () -> Unit,
    onShareImageClick: () -> Unit,
    onInfoClick: () -> Unit,
    onWatchedClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val isReleased = remember { movie.isReleased }
    val isAnime = remember { movie.genres.contains(Anime) }

    DetailsHeader(
        title = movie.title,
        titleTranslation = movieTranslation?.title,
        status = movie.status,
        watched = (playsCount ?: 0) > 0,
        watching = false,
        date = {
            Text(
                text = when {
                    isReleased -> movie.yearString
                    else -> movie.released?.format(mediumDateFormat())?.capitalize() ?: movie.yearString
                },
                color = when {
                    isReleased -> TraktTheme.colors.textSecondary
                    else -> TraktTheme.colors.textPrimary
                },
                style = when {
                    isReleased -> TraktTheme.typography.paragraphSmaller
                    else -> TraktTheme.typography.paragraphSmaller.copy(fontWeight = FontWeight.W700)
                },
                maxLines = 1,
                modifier = Modifier.padding(
                    end = if (!isReleased) 1.dp else 0.dp,
                ),
            )
        },
        titleFooter = {
            val animatedAlpha by animateFloatAsState(
                targetValue = if (creator == null) 0f else 1f,
                animationSpec = tween(delayMillis = 50),
                label = "alpha",
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = spacedBy(3.dp),
                modifier = Modifier
                    .alpha(animatedAlpha),
            ) {
                Text(
                    text = stringResource(R.string.text_directed_by_short),
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.paragraphSmaller,
                )

                Text(
                    text = (creator?.name ?: "").ifBlank { "-" },
                    color = when {
                        creator?.name?.isNotBlank() == true -> TraktTheme.colors.textPrimary
                        else -> TraktTheme.colors.textSecondary
                    },
                    style = TraktTheme.typography.paragraphSmaller,
                    modifier = Modifier
                        .onClick {
                            creator?.let {
                                onCreatorClick(it)
                            }
                        },
                )
            }
        },
        genres = movie.genres,
        runtime = movie.runtime,
        imageUrl = movie.images?.getPosterUrl(Images.Size.MEDIUM),
        imagePlaceholderUrl = movie.images?.getPosterUrl(Images.Size.THUMB),
        imageHorizontal = false,
        accentColor = movie.colors?.colors?.first,
        traktRatings = when {
            isReleased -> movie.rating.ratingPercent
            else -> null
        },
        externalRatingsVisible = true,
        externalRottenVisible = true,
        externalMalVisible = isAnime,
        externalRatings = ratings,
        creditsCount = creditsCount,
        playsCount = playsCount,
        episodesCount = null,
        certification = movie.certification,
        loading = loading,
        onBackClick = onBackClick,
        onInfoClick = onInfoClick,
        onWatchedClick = onWatchedClick,
        onShareClick = onShareClick,
        onShareImageClick = onShareImageClick,
        onImdbClick = {
            movie.ids.imdb?.let {
                openExternalAppLink(
                    context = context,
                    packageId = "com.imdb.mobile",
                    packageName = "imdb",
                    uri = webImdbMediaUrl(it.value).toUri(),
                )
            }
        },
        onMalClick = { link ->
            if (link.isNotBlank()) {
                openExternalAppLink(
                    context = context,
                    packageId = "net.myanimelist.app",
                    packageName = "mal",
                    uri = link.toUri(),
                )
            }
        },
        onRottenClick = { link ->
            if (link.isNotBlank()) {
                openExternalAppLink(
                    context = context,
                    packageId = "com.rottentomatoes.android",
                    packageName = "rottentomatoes",
                    uri = link.toUri(),
                )
            }
        },
        modifier = modifier,
    )
}
