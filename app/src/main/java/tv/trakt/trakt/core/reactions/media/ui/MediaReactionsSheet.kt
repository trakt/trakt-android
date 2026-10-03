@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.reactions.media.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.reactions.MediaReaction
import tv.trakt.trakt.ui.components.TraktBottomSheet

@Composable
internal fun MediaReactionsSheet(
    visible: Boolean,
    userReactions: ImmutableList<MediaReaction>,
    isLimitReached: Boolean,
    onReactionClick: (MediaReaction) -> Unit,
    onDismiss: () -> Unit,
    state: SheetState = rememberBottomSheetState(
        initialValue = Hidden,
        enabledValues = setOf(Hidden, Expanded),
    ),
) {
    if (visible) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            MediaReactionsPickerView(
                userReactions = userReactions,
                isLimitReached = isLimitReached,
                onReactionClick = onReactionClick,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
            )
        }
    }
}
