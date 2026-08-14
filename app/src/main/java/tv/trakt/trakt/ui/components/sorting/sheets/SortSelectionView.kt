package tv.trakt.trakt.ui.components.sorting.sheets

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.DeviceSheetPreview
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.sorting.SortOrder
import tv.trakt.trakt.common.model.sorting.SortType
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SortSelectionView(
    modifier: Modifier = Modifier,
    typeOptions: ImmutableList<SortType> = SortType.entries.toImmutableList(),
    selectedType: SortType? = null,
    selectedOrder: SortOrder? = null,
    onSortClick: (SortType) -> Unit = {},
    onOrderClick: (SortOrder) -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier,
    ) {
        ActionButtons(
            selectedType = selectedType,
            selectedOrder = selectedOrder,
            options = typeOptions,
            onSortClick = onSortClick,
            onOrderClick = onOrderClick,
        )
    }
}

@Composable
private fun ActionButtons(
    modifier: Modifier = Modifier,
    selectedType: SortType?,
    selectedOrder: SortOrder?,
    options: ImmutableList<SortType>,
    onSortClick: (SortType) -> Unit = {},
    onOrderClick: (SortOrder) -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.contextItemsSpace / 1.5F),
        modifier = modifier
            .graphicsLayer {
                translationX = -8.dp.toPx()
            },
    ) {
        for (sort in options) {
            Box {
                GhostButton(
                    text = stringResource(sort.displayStringRes),
                    contentColor = when {
                        sort == selectedType -> TraktTheme.colors.textPrimary
                        else -> TraktTheme.colors.textSecondary.copy(alpha = 0.7F)
                    },
                    icon = when {
                        sort == selectedType -> painterResource(R.drawable.ic_check_google)
                        sort.displayIconRes != null -> painterResource(sort.displayIconRes!!)
                        else -> null
                    },
                    iconSize = 22.dp,
                    iconSpace = 10.dp,
                    onClick = {
                        if (sort == selectedType) {
                            selectedOrder?.let { onOrderClick(it.toggle()) }
                        } else {
                            onSortClick(sort)
                        }
                    },
                )

                if (sort == selectedType) {
                    selectedOrder?.let {
                        Row(
                            horizontalArrangement = spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 4.dp)
                                .height(28.dp)
                                .border(
                                    width = 1.dp,
                                    color = TraktTheme.colors.chipContainer,
                                    shape = RoundedCornerShape(100),
                                )
                                .padding(horizontal = 10.dp)
                                .onClick(throttle = false) {
                                    onOrderClick(it.toggle())
                                },
                        ) {
                            Icon(
                                painter = painterResource(it.displayIconRes),
                                contentDescription = null,
                                tint = TraktTheme.colors.textPrimary,
                                modifier = Modifier
                                    .size(12.dp),
                            )

                            Text(
                                text = stringResource(it.displayStringRes).uppercase(),
                                color = TraktTheme.colors.textPrimary,
                                style = TraktTheme.typography.buttonTertiary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@DeviceSheetPreview
@Composable
private fun Preview() {
    TraktThemeLightDark {
        SortSelectionView(
            selectedType = SortType.Runtime,
            selectedOrder = SortOrder.Desc,
        )
    }
}
