package tv.trakt.trakt.core.profile.sections.favorites.all.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.sorting.SortType.Runtime
import tv.trakt.trakt.common.model.sorting.SortType.UserRating
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.favorites.model.FavoriteItem
import tv.trakt.trakt.core.shows.ui.ShowMetaFooter
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard

@Composable
internal fun AllFavoritesShowView(
    item: FavoriteItem.ShowItem,
    sorting: Sorting,
    modifier: Modifier = Modifier,
    watched: Boolean = false,
    watching: Boolean = false,
    watchlist: Boolean = false,
    mediaIcon: Boolean = true,
    loading: Boolean = false,
    onClick: () -> Unit,
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
        watched = watched,
        watching = watching,
        watchlist = watchlist,
        enabled = !loading,
        contentImageUrl = item.images?.getPosterUrl(),
        containerImageUrl = item.images?.getFanartUrl(Images.Size.THUMB),
        onClick = onClick,
        onLongClick = onLongClick,
        onImageClick = onClick,
        footerContent = {
            ShowMetaFooter(
                show = item.show,
                mediaIcon = mediaIcon,
                rating = sorting.type != UserRating && sorting.type != Runtime,
                userRating = when {
                    sorting.type == UserRating -> item.userRating
                    else -> null
                },
                duration = when {
                    sorting.type == Runtime -> item.show.totalRuntime
                    else -> null
                },
            )
        },
    )
}
