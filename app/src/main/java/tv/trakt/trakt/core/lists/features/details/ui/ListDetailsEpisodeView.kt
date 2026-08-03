package tv.trakt.trakt.core.lists.features.details.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.model.Images.Size.THUMB
import tv.trakt.trakt.common.model.sorting.SortType.Runtime
import tv.trakt.trakt.common.model.sorting.SortType.UserRating
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.episodes.ui.EpisodeMetaFooter
import tv.trakt.trakt.core.lists.model.CustomListItem
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard

@Composable
internal fun ListDetailsEpisodeView(
    item: CustomListItem.EpisodeItem,
    sorting: Sorting,
    modifier: Modifier = Modifier,
    shadow: Boolean = false,
    enabled: Boolean = true,
    onClick: (CustomListItem.EpisodeItem) -> Unit = { },
) {
    PanelMediaCard(
        modifier = modifier,
        title = item.show.title,
        titleOriginal = null,
        subtitle = item.episode.seasonEpisodeString(),
        shadow = if (shadow) 4.dp else 0.dp,
        enabled = enabled,
        watched = false,
        watchlist = false,
        more = false,
        contentImageUrl = item.images?.getPosterUrl(),
        containerImageUrl = item.episode.images?.getScreenshotUrl(THUMB)
            ?: item.show.images?.getFanartUrl(THUMB),
        onClick = { onClick(item) },
        onImageClick = { onClick(item) },
        onLongClick = null,
        footerContent = {
            EpisodeMetaFooter(
                episode = item.episode,
                mediaIcon = true,
                rating = enabled && sorting.type != UserRating && sorting.type != Runtime,
                userRating = when {
                    sorting.type == UserRating && enabled -> item.userRating
                    else -> null
                },
            )
        },
    )
}
