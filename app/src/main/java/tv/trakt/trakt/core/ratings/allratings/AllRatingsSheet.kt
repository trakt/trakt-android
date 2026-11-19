@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.ratings.allratings

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.SheetValue.PartiallyExpanded
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.common.Config.webImdbMediaUrl
import tv.trakt.trakt.common.helpers.extensions.openExternalAppLink
import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.ImdbId
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.ui.components.TraktBottomSheet
import kotlin.random.Random.Default.nextInt

@Composable
internal fun AllRatingsSheet(
    state: SheetState = rememberBottomSheetState(
        initialValue = Hidden,
        enabledValues = setOf(Hidden, PartiallyExpanded, Expanded),
    ),
    visible: Boolean,
    ratings: ExternalRating?,
    imdbId: ImdbId?,
    showId: TraktId? = null,
    malEnabled: Boolean = false,
    seasonsEnabled: Boolean = true,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val viewModelKey = remember(visible) {
        nextInt().toString()
    }

    if (visible && ratings != null) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            AllRatingsView(
                viewModel = koinViewModel(key = viewModelKey) {
                    parametersOf(showId)
                },
                ratings = ratings,
                malEnabled = malEnabled,
                seasonsEnabled = seasonsEnabled,
                onImdbClick = {
                    imdbId?.let {
                        context.openRatingLink(
                            link = webImdbMediaUrl(it.value),
                            packageId = "com.imdb.mobile",
                            packageName = "imdb",
                        )
                    }
                },
                onRottenClick = { link ->
                    context.openRatingLink(
                        link = link,
                        packageId = "com.rottentomatoes.android",
                        packageName = "rottentomatoes",
                    )
                },
                onMalClick = { link ->
                    context.openRatingLink(
                        link = link,
                        packageId = "net.myanimelist.app",
                        packageName = "mal",
                    )
                },
                onLetterboxdClick = { link ->
                    context.openRatingLink(
                        link = link,
                        packageId = "com.letterboxd.letterboxd",
                        packageName = "letterboxd",
                    )
                },
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp, bottom = 12.dp),
            )
        }
    }
}

private fun Context.openRatingLink(
    link: String,
    packageId: String,
    packageName: String,
) {
    if (link.isBlank()) return

    openExternalAppLink(
        context = this,
        packageId = packageId,
        packageName = packageName,
        uri = link.toUri(),
    )
}
