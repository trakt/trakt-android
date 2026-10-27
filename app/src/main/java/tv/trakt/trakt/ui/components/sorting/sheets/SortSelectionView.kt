package tv.trakt.trakt.ui.components.sorting.sheets

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.DeviceSheetPreview
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.sorting.ListsSortType
import tv.trakt.trakt.common.model.sorting.SortOption
import tv.trakt.trakt.common.model.sorting.SortOrder
import tv.trakt.trakt.common.model.sorting.SortType
import tv.trakt.trakt.core.home.sections.upnext.model.UpNextSortType
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun <T : SortOption> SortSelectionView(
    typeOptions: ImmutableList<T>,
    modifier: Modifier = Modifier,
    selectedType: T? = null,
    selectedOrder: SortOrder? = null,
    descriptionsVisible: Boolean = false,
    onSortClick: (T) -> Unit = {},
    onOrderClick: (SortOrder) -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier,
    ) {
        ActionButtons(
            selectedType = selectedType,
            selectedOrder = selectedOrder,
            descriptionsVisible = descriptionsVisible,
            options = typeOptions,
            onSortClick = onSortClick,
            onOrderClick = onOrderClick,
        )
    }
}

@Composable
private fun <T : SortOption> ActionButtons(
    modifier: Modifier = Modifier,
    selectedType: T?,
    selectedOrder: SortOrder?,
    descriptionsVisible: Boolean,
    options: ImmutableList<T>,
    onSortClick: (T) -> Unit = {},
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
                val contentColor = when {
                    sort == selectedType -> TraktTheme.colors.textPrimary
                    else -> TraktTheme.colors.textSecondary.copy(alpha = 0.7F)
                }
                val icon = when {
                    sort == selectedType -> painterResource(R.drawable.ic_check_google)
                    else -> sort.displayIconRes?.let { painterResource(it) }
                }
                val onClick: () -> Unit = {
                    if (sort == selectedType) {
                        selectedOrder?.let { onOrderClick(it.toggle()) }
                    } else {
                        onSortClick(sort)
                    }
                }
                val descriptionRes = when {
                    descriptionsVisible -> sort.descriptionRes(selectedOrder ?: SortOrder.Desc)
                    else -> null
                }

                if (descriptionRes == null) {
                    GhostButton(
                        text = stringResource(sort.displayStringRes),
                        contentColor = contentColor,
                        icon = icon,
                        iconSize = 22.dp,
                        iconSpace = 10.dp,
                        onClick = onClick,
                    )
                } else {
                    DescribedSortButton(
                        text = stringResource(sort.displayStringRes),
                        description = stringResource(descriptionRes),
                        contentColor = contentColor,
                        icon = icon,
                        onClick = onClick,
                    )
                }

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

@Composable
private fun DescribedSortButton(
    text: String,
    description: String,
    contentColor: Color,
    icon: Painter?,
    onClick: () -> Unit,
) {
    Row(
        horizontalArrangement = spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .onClick(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        icon?.let {
            Image(
                painter = it,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(contentColor),
                modifier = Modifier.requiredSize(22.dp),
            )
        }

        Column {
            Text(
                text = text.uppercase(),
                color = contentColor,
                style = TraktTheme.typography.buttonSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = description,
                color = contentColor,
                style = TraktTheme.typography.meta.copy(
                    fontWeight = W400,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@DeviceSheetPreview
@Composable
private fun Preview() {
    TraktThemeLightDark {
        SortSelectionView(
            typeOptions = SortType.entries.toImmutableList(),
            selectedType = SortType.Runtime,
            selectedOrder = SortOrder.Desc,
        )
    }
}

@DeviceSheetPreview
@Composable
private fun DescribedPreview() {
    TraktThemeLightDark {
        SortSelectionView(
            typeOptions = UpNextSortType.entries.toImmutableList(),
            selectedType = UpNextSortType.Released,
            selectedOrder = SortOrder.Asc,
            descriptionsVisible = true,
        )
    }
}

@DeviceSheetPreview
@Composable
private fun ListsPreview() {
    TraktThemeLightDark {
        SortSelectionView(
            typeOptions = ListsSortType.entries.toImmutableList(),
            selectedType = ListsSortType.Updated,
            selectedOrder = SortOrder.Desc,
        )
    }
}
