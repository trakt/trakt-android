package tv.trakt.trakt.core.home.sections.watchlist.features.all.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import tv.trakt.trakt.common.core.lists.model.WatchlistItem
import tv.trakt.trakt.common.model.Images.Size.THUMB
import tv.trakt.trakt.core.shows.ui.ShowMetaFooter
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard

@Composable
internal fun AllHomeWatchlistEpisodeView(
    item: WatchlistItem.ShowItem,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    showCheck: Boolean = true,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onCheckClick: () -> Unit,
    onCheckLongClick: () -> Unit,
) {
    val genresText = item.show.genres.take(2)
        .map { stringResource(it.displayStringRes) }
        .joinToString(", ")

    PanelMediaCard(
        modifier = modifier,
        enabled = enabled,
        title = item.show.title,
        titleOriginal = item.show.titleOriginal,
        subtitle = genresText,
        contentImageUrl = item.show.images?.getPosterUrl(),
        containerImageUrl = item.images?.getFanartUrl(THUMB),
        onClick = onClick,
        onLongClick = onLongClick,
        onImageClick = onClick,
        footerContent = {
            ShowMetaFooter(
                show = item.show,
                mediaIcon = true,
                loading = item.loading,
                check = showCheck,
                onCheckClick = onCheckClick,
                onCheckLongClick = onCheckLongClick,
            )
        },
    )
}
