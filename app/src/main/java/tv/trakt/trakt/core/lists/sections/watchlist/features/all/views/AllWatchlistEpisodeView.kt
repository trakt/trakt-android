package tv.trakt.trakt.core.lists.sections.watchlist.features.all.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import tv.trakt.trakt.common.model.Images.Size.THUMB
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.shows.ui.ShowMetaFooter
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard

@Composable
internal fun AllWatchlistEpisodeView(
    item: WatchlistItem.ShowItem,
    modifier: Modifier = Modifier,
    showCheck: Boolean = true,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onCheckClick: () -> Unit,
    onCheckLongClick: () -> Unit,
) {
    PanelMediaCard(
        modifier = modifier,
        title = item.show.title,
        titleOriginal = item.show.titleOriginal,
        subtitle = item.progress?.nextEpisode?.seasonEpisodeString() ?: "",
        contentImageUrl = item.show.images?.getPosterUrl(),
        containerImageUrl = item.images?.getFanartUrl(THUMB),
        onClick = onClick,
        onLongClick = onLongClick,
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
