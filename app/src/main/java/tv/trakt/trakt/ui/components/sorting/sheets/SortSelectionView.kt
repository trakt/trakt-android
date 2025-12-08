package tv.trakt.trakt.ui.components.sorting.sheets

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.sorting.SortTypeList
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SortSelectionView(
    modifier: Modifier = Modifier,
    options: ImmutableList<SortTypeList> = SortTypeList.entries.toImmutableList(),
    selected: SortTypeList? = null,
    onSortClick: (SortTypeList) -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier,
    ) {
        ActionButtons(
            selected = selected,
            options = options,
            onSortClick = onSortClick,
        )
    }
}

@Composable
private fun ActionButtons(
    modifier: Modifier = Modifier,
    selected: SortTypeList?,
    options: ImmutableList<SortTypeList>,
    onSortClick: (SortTypeList) -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.contextItemsSpace),
        modifier = modifier
            .graphicsLayer {
                translationX = -8.dp.toPx()
            },
    ) {
        for (sort in options) {
            GhostButton(
                text = stringResource(sort.displayStringRes),
                contentColor = when {
                    sort == selected -> TraktTheme.colors.primaryButtonContent
                    else -> TraktTheme.colors.textSecondary
                },
                icon = when {
                    sort == selected -> painterResource(R.drawable.ic_check)
                    else -> null
                },
                iconSize = 18.dp,
                iconSpace = 12.dp,
                onClick = {
                    onSortClick(sort)
                },
            )
        }
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        SortSelectionView(
            selected = SortTypeList.RUNTIME,
        )
    }
}
