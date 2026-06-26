package tv.trakt.trakt.core.lists.features.details.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.sorting.SortType.Runtime
import tv.trakt.trakt.common.model.sorting.SortType.UserRating
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.lists.model.CustomListItem
import tv.trakt.trakt.core.shows.ui.ShowMetaFooter
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard

@Composable
internal fun ListDetailsShowView(
    item: CustomListItem.ShowItem,
    sorting: Sorting,
    modifier: Modifier = Modifier,
    showIcon: Boolean = false,
    shadow: Boolean = false,
    enabled: Boolean = true,
    watched: Boolean = false,
    watchlist: Boolean = false,
    onClick: (TraktId) -> Unit = { },
    onLongClick: () -> Unit,
) {
    val genresText = item.show.genres.take(2)
        .map { stringResource(it.displayStringRes) }
        .joinToString(", ")

    PanelMediaCard(
        modifier = modifier,
        title = item.show.title,
        titleOriginal = item.show.titleOriginal,
        subtitle = genresText,
        shadow = if (shadow) 4.dp else 0.dp,
        enabled = enabled,
        watched = watched,
        watchlist = watchlist,
        contentImageUrl = item.images?.getPosterUrl(),
        containerImageUrl = item.images?.getFanartUrl(Images.Size.THUMB),
        onClick = { onClick(item.show.ids.trakt) },
        onLongClick = onLongClick,
        footerContent = {
            ShowMetaFooter(
                show = item.show,
                mediaIcon = showIcon,
                rating = enabled && sorting.type != UserRating && sorting.type != Runtime,
                userRating = when {
                    sorting.type == UserRating && enabled -> item.userRating
                    else -> null
                },
                duration = when {
                    sorting.type == Runtime && enabled -> item.show.totalRuntime
                    else -> null
                },
            )
        },
    )
}
