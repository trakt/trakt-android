package tv.trakt.trakt.core.settings.features.cover

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.ui.components.TraktBottomSheet
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CoverImageSheet(
    state: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    ),
    mediaId: TraktId?,
    mediaTitle: String,
    mediaType: MediaType,
    mediaImage: String?,
    onImageSet: () -> Unit,
    onDismiss: () -> Unit,
    onVipClick: () -> Unit,
) {
    val sheetScope = rememberCoroutineScope()

    if (mediaId != null) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            CoverImageView(
                viewModel = koinViewModel(
                    key = Random.nextInt().toString(),
                    parameters = {
                        parametersOf(
                            mediaId,
                            mediaTitle,
                            mediaType,
                            mediaImage,
                        )
                    },
                ),
                onImageSet = {
                    sheetScope.launch { state.hide() }
                        .invokeOnCompletion {
                            if (!state.isVisible) {
                                onImageSet()
                            }
                        }
                },
                onDismiss = {
                    sheetScope.launch { state.hide() }
                        .invokeOnCompletion {
                            if (!state.isVisible) {
                                onDismiss()
                            }
                        }
                },
                onVipClick = {
                    sheetScope.launch { state.hide() }
                        .invokeOnCompletion {
                            if (!state.isVisible) {
                                onVipClick()
                            }
                        }
                },
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
            )
        }
    }
}
