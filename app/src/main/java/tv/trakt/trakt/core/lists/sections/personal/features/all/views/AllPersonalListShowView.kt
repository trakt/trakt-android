package tv.trakt.trakt.core.lists.sections.personal.features.all.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import tv.trakt.trakt.common.model.Images.Size
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.model.PersonalListItem
import tv.trakt.trakt.core.shows.ui.ShowMetaFooter
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard

@Composable
internal fun AllPersonalListShowView(
    item: PersonalListItem.ShowItem,
    modifier: Modifier = Modifier,
    showIcon: Boolean = false,
    watched: Boolean = false,
    watchlist: Boolean = false,
    onClick: (TraktId) -> Unit,
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
        containerImageUrl = item.images?.getFanartUrl(Size.THUMB),
        watched = watched,
        watchlist = watchlist,
        onClick = { onClick(item.show.ids.trakt) },
        onLongClick = onLongClick,
        footerContent = {
            ShowMetaFooter(
                show = item.show,
                mediaIcon = showIcon,
            )
        },
    )
}
