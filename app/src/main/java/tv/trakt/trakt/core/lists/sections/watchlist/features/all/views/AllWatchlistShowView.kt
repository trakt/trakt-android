package tv.trakt.trakt.core.lists.sections.watchlist.features.all.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.shows.ui.ShowMetaFooter
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard

@Composable
internal fun AllWatchlistShowView(
    item: WatchlistItem.ShowItem,
    modifier: Modifier = Modifier,
    watched: Boolean = false,
    enabled: Boolean = true,
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
        enabled = enabled,
        contentImageUrl = item.images?.getPosterUrl(),
        containerImageUrl = item.images?.getFanartUrl(Images.Size.THUMB),
        onClick = onClick,
        onLongClick = onLongClick,
        onImageClick = onClick,
        footerContent = {
            ShowMetaFooter(
                show = item.show,
                mediaIcon = true,
            )
        },
    )
}
