package tv.trakt.trakt.core.profile.sections.favorites.all.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.core.discover.ui.ShowMetaFooter
import tv.trakt.trakt.core.profile.model.FavoriteItem
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard

@Composable
internal fun AllFavoritesShowView(
    item: FavoriteItem.ShowItem,
    modifier: Modifier = Modifier,
    mediaIcon: Boolean = true,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
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
        subtitle = genresText,
        contentImageUrl = item.images?.getPosterUrl(),
        containerImageUrl = item.images?.getFanartUrl(Images.Size.THUMB),
        onClick = onClick,
        onLongClick = onLongClick,
        footerContent = {
            ShowMetaFooter(
                show = item.show,
                mediaIcon = mediaIcon,
            )
        },
    )
}
