@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.summary.ui.header.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.ImdbId
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.ratings.allratings.AllRatingsSheet
import tv.trakt.trakt.core.summary.ui.DetailsRatings
import tv.trakt.trakt.helpers.extensions.TraktThemePreview

/** Rating badges; tapping them opens every rating in [AllRatingsSheet]. */
@Composable
internal fun DetailsHeaderRatings(
    traktRatings: Int?,
    externalRatings: ExternalRating?,
    imdbId: ImdbId?,
    modifier: Modifier = Modifier,
    showId: TraktId? = null,
    rottenVisible: Boolean = true,
    malVisible: Boolean = false,
) {
    var allRatingsVisible by remember { mutableStateOf(false) }

    DetailsRatings(
        traktRatings = traktRatings,
        externalRatings = externalRatings,
        rottenEnabled = rottenVisible,
        malEnabled = malVisible,
        modifier = modifier
            .padding(top = 16.dp)
            .onClick(enabled = externalRatings != null) {
                allRatingsVisible = true
            },
    )

    AllRatingsSheet(
        visible = allRatingsVisible,
        ratings = externalRatings,
        imdbId = imdbId,
        showId = showId,
        malEnabled = malVisible,
        onDismiss = { allRatingsVisible = false },
    )
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        DetailsHeaderRatings(
            traktRatings = 72,
            externalRatings = previewExternalRatings,
            imdbId = null,
        )
    }
}
