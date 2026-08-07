@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.ratings.allratings

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.ui.components.TraktBottomSheet
import kotlin.random.Random.Default.nextInt

@Composable
internal fun AllRatingsSheet(
    state: SheetState = rememberBottomSheetState(
        initialValue = Hidden,
        enabledValues = setOf(Hidden, Expanded),
    ),
    visible: Boolean,
    ratings: ExternalRating?,
    showId: TraktId? = null,
    malEnabled: Boolean = false,
    onImdbClick: () -> Unit = {},
    onRottenClick: (link: String) -> Unit = {},
    onMalClick: (link: String) -> Unit = {},
    onDismiss: () -> Unit,
) {
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
                onImdbClick = onImdbClick,
                onRottenClick = onRottenClick,
                onMalClick = onMalClick,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp, bottom = 16.dp),
            )
        }
    }
}
