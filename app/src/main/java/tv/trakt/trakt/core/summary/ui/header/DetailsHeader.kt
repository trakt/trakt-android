package tv.trakt.trakt.core.summary.ui.header

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.model.MediaGenre
import tv.trakt.trakt.common.model.MediaStatus
import tv.trakt.trakt.core.summary.ui.header.components.DetailsHeaderDate
import tv.trakt.trakt.core.summary.ui.header.components.DetailsHeaderMeta
import tv.trakt.trakt.core.summary.ui.header.components.DetailsHeaderRatings
import tv.trakt.trakt.core.summary.ui.header.components.DetailsHeaderStatus
import tv.trakt.trakt.core.summary.ui.header.components.DetailsHeaderTitle
import tv.trakt.trakt.core.summary.ui.header.components.previewExternalRatings
import tv.trakt.trakt.core.summary.ui.header.poster.DetailsHeaderPoster
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.ui.theme.TraktTheme

/**
 * Layout shared by the details headers: [poster] on top, then [title], [details] and [ratings]
 * stacked and centred below it.
 */
@Composable
internal fun DetailsHeader(
    poster: @Composable () -> Unit,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    details: @Composable () -> Unit = {},
    ratings: @Composable (() -> Unit)? = null,
) {
    Column(
        horizontalAlignment = CenterHorizontally,
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        poster()

        Column(
            horizontalAlignment = CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 15.dp,
                    start = TraktTheme.spacing.mainPageHorizontalSpace,
                    end = TraktTheme.spacing.mainPageHorizontalSpace,
                ),
        ) {
            title()
            details()

            if (ratings != null) {
                ratings()
            } else {
                Spacer(Modifier.padding(top = 4.dp))
            }
        }
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        DetailsHeader(
            poster = {
                DetailsHeaderPoster(
                    imageUrl = null,
                    imagePlaceholderUrl = null,
                    accentColor = null,
                    loading = false,
                    creditsCount = 0,
                    playsCount = 2,
                    watched = true,
                    watching = false,
                    personImdb = null,
                    onBackClick = {},
                    onShareClick = {},
                    onShareImageClick = {},
                    onWatchedClick = {},
                )
            },
            title = {
                DetailsHeaderTitle(
                    title = "Movie Title",
                    translation = null,
                )
            },
            details = {
                DetailsHeaderMeta(
                    genres = persistentListOf(MediaGenre.Action, MediaGenre.Comedy),
                    date = { DetailsHeaderDate(text = "2024", isReleased = true) },
                    episodesCount = 23,
                    certification = "PG-13",
                    onInfoClick = {},
                )
                DetailsHeaderStatus(status = MediaStatus.Released)
            },
            ratings = {
                DetailsHeaderRatings(
                    traktRatings = 72,
                    externalRatings = previewExternalRatings,
                    imdbId = null,
                    malVisible = true,
                )
            },
        )
    }
}
