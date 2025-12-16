package tv.trakt.trakt.core.summary.ui.header

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.isNowOrBefore
import tv.trakt.trakt.common.helpers.extensions.mediumDateFormat
import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun DetailsHeader(
    show: Show,
    ratings: ExternalRating?,
    airedCount: Int,
    playsCount: Int,
    loading: Boolean,
    onShareClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isReleased = remember {
        show.released?.isNowOrBefore() ?: false
    }

    val playsCount = remember(airedCount, playsCount) {
        if (airedCount == 0) {
            return@remember 0
        }
        playsCount / airedCount
    }

    DetailsHeader(
        title = show.title,
        status = show.status,
        date = {
            Text(
                text = when {
                    isReleased -> (show.released?.year ?: show.year).toString()
                    else -> show.released?.format(mediumDateFormat) ?: show.year.toString()
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
        genres = show.genres,
        runtime = null,
        imageUrl = show.images?.getPosterUrl(Images.Size.MEDIUM),
        imageHorizontal = false,
        accentColor = show.colors?.colors?.first,
        traktRatings = when {
            isReleased -> show.rating.ratingPercent
            else -> null
        },
        externalRatingsVisible = true,
        externalRottenVisible = true,
        externalRatings = ratings,
        playsCount = playsCount,
        episodesCount = show.airedEpisodes,
        creditsCount = null,
        certification = show.certification,
        loading = loading,
        onBackClick = onBackClick,
        onShareClick = onShareClick,
        modifier = modifier,
    )
}
