package tv.trakt.trakt.core.lists.sections.personal.features.all.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import tv.trakt.trakt.common.model.Images.Size.THUMB
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.model.CustomListItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard

@Composable
internal fun AllPersonalListSeasonView(
    item: CustomListItem.SeasonItem,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: (TraktId) -> Unit = { },
) {
    PanelMediaCard(
        modifier = modifier,
        title = item.show.title,
        titleOriginal = null,
        subtitle = stringResource(R.string.text_season_number, item.season.number),
        enabled = enabled,
        watched = false,
        watchlist = false,
        more = false,
        contentImageUrl = item.images?.getPosterUrl(),
        containerImageUrl = item.show.images?.getFanartUrl(THUMB),
        onClick = { onClick(item.show.ids.trakt) },
        onImageClick = { onClick(item.show.ids.trakt) },
        onLongClick = null,
    )
}
