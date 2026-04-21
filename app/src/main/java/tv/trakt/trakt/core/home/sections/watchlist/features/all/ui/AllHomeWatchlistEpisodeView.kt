package tv.trakt.trakt.core.home.sections.watchlist.features.all.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import tv.trakt.trakt.common.model.Images.Size.THUMB
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.shows.ui.ShowMetaFooter
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard

@Composable
internal fun AllHomeWatchlistEpisodeView(
    item: WatchlistItem.ShowItem,
    modifier: Modifier = Modifier,
    showCheck: Boolean = true,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onCheckClick: () -> Unit,
    onCheckLongClick: () -> Unit,
) {
    val genresText = remember(item.show.genres) {
        item.show.genres.take(2).joinToString(", ") { genre ->
            genre.replaceFirstChar {
                it.uppercaseChar()
            }
        }
    }

    PanelMediaCard(
        modifier = modifier,
        title = item.show.title,
        titleOriginal = item.show.titleOriginal,
//        subtitle = stringResource(R.string.episode_footer_season_episode, 1, 1),
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
