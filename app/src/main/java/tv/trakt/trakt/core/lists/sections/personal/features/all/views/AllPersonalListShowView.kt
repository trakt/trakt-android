package tv.trakt.trakt.core.lists.sections.personal.features.all.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import tv.trakt.trakt.common.model.Images.Size
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.model.CustomListItem
import tv.trakt.trakt.core.shows.ui.ShowMetaFooter
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard

@Composable
internal fun AllPersonalListShowView(
    item: CustomListItem.ShowItem,
    modifier: Modifier = Modifier,
    showIcon: Boolean = false,
    watched: Boolean = false,
    watchlist: Boolean = false,
    enabled: Boolean = true,
    onClick: (TraktId) -> Unit,
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
        contentImageUrl = item.images?.getPosterUrl(),
        containerImageUrl = item.images?.getFanartUrl(Size.THUMB),
        watched = watched,
        watchlist = watchlist,
        enabled = enabled,
        onClick = { onClick(item.show.ids.trakt) },
        onImageClick = { onClick(item.show.ids.trakt) },
        onLongClick = onLongClick,
        footerContent = {
            ShowMetaFooter(
                show = item.show,
                mediaIcon = showIcon,
            )
        },
    )
}
