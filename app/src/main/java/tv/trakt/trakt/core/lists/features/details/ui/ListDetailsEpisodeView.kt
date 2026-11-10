package tv.trakt.trakt.core.lists.features.details.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import tv.trakt.trakt.common.model.Images.Size.THUMB
import tv.trakt.trakt.common.model.sorting.SortType.Runtime
import tv.trakt.trakt.common.model.sorting.SortType.UserRating
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.episodes.ui.EpisodeMetaFooter
import tv.trakt.trakt.core.lists.model.CustomListItem
import tv.trakt.trakt.helpers.extensions.isSpoilerHidden
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard

@Composable
internal fun ListDetailsEpisodeView(
    item: CustomListItem.EpisodeItem,
    sorting: Sorting,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: (CustomListItem.EpisodeItem) -> Unit = { },
    onLongClick: (() -> Unit)? = null,
) {
    PanelMediaCard(
        modifier = modifier,
        title = item.show.title,
        titleOriginal = null,
        subtitle = item.episode.seasonEpisodeString(),
        enabled = enabled,
        watched = false,
        watchlist = false,
        more = onLongClick != null,
        contentImageUrl = item.images?.getPosterUrl(),
        containerImageUrl = when {
            item.episode.isSpoilerHidden() -> item.show.images?.getFanartUrl(THUMB)
            else -> item.episode.images?.getScreenshotUrl(THUMB)
                ?: item.show.images?.getFanartUrl(THUMB)
        },
        onClick = { onClick(item) },
        onImageClick = { onClick(item) },
        onLongClick = onLongClick,
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
