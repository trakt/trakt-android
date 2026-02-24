package tv.trakt.trakt.core.lists.sections.personal.features.all.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import tv.trakt.trakt.common.model.Images.Size.THUMB
import tv.trakt.trakt.core.episodes.ui.EpisodeMetaFooter
import tv.trakt.trakt.core.lists.model.CustomListItem
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard

@Composable
internal fun AllPersonalListEpisodeView(
    item: CustomListItem.EpisodeItem,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: (CustomListItem.EpisodeItem) -> Unit = { },
) {
    PanelMediaCard(
        modifier = modifier,
        title = item.show.title,
        titleOriginal = item.show.titleOriginal,
        subtitle = item.episode.seasonEpisodeString(),
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
            )
        },
    )
}
