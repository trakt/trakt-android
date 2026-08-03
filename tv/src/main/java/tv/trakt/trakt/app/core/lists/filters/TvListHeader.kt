package tv.trakt.trakt.app.core.lists.filters

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.sorting.Sorting

@Composable
internal fun TvListHeader(
    title: String,
    modifier: Modifier = Modifier,
    controlsState: TvListControlsState,
    titleMaxLines: Int = 1,
    downFocusRequester: FocusRequester = FocusRequester.Default,
    onFilterApplied: (GlobalFilter) -> Unit,
    onSortingApplied: (Sorting) -> Unit,
    titleActions: @Composable RowScope.() -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .focusGroup(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(TraktTheme.spacing.mainGridSpace),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(HeaderTitleAreaWidth),
        ) {
            Text(
                text = title,
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading4,
                maxLines = titleMaxLines,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1F)
                    .focusProperties {
                        down = downFocusRequester
                    }
                    .focusable(),
            )

            titleActions()
        }

        TvListControls(
            state = controlsState,
            onFilterApplied = onFilterApplied,
            onSortingApplied = onSortingApplied,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
        )
    }
}

// Prevents long titles from overlapping the centered selector at the smallest TV layout.
private val HeaderTitleAreaWidth = 300.dp
