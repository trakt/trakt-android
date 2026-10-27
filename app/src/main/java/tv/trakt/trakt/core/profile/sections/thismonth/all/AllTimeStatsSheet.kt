@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.profile.sections.thismonth.all

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.core.profile.sections.thismonth.model.ProfileStats
import tv.trakt.trakt.ui.components.TraktBottomSheet

@Composable
internal fun AllTimeStatsSheet(
    state: SheetState = rememberBottomSheetState(
        initialValue = Hidden,
        enabledValues = setOf(Hidden, Expanded),
    ),
    stats: ProfileStats?,
    isVip: Boolean,
    visible: Boolean,
    onYearClick: (year: Int) -> Unit,
    onVipClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (visible && stats != null) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            AllTimeStatsView(
                stats = stats,
                isVip = isVip,
                onYearClick = onYearClick,
                onVipClick = onVipClick,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp, bottom = 20.dp),
            )
        }
    }
}
