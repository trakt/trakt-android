package tv.trakt.trakt.core.lists.features.details.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.model.Images.Size.THUMB
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.sorting.SortType.Runtime
import tv.trakt.trakt.common.model.sorting.SortType.UserRating
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.episodes.ui.SeasonMetaFooter
import tv.trakt.trakt.core.lists.model.CustomListItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard

@Composable
internal fun ListDetailsSeasonView(
    item: CustomListItem.SeasonItem,
    sorting: Sorting,
    modifier: Modifier = Modifier,
    shadow: Boolean = false,
    enabled: Boolean = true,
    onClick: (TraktId) -> Unit = { },
) {
    PanelMediaCard(
        modifier = modifier,
        title = item.show.title,
        titleOriginal = null,
        subtitle = stringResource(R.string.text_season_number, item.season.number),
        shadow = if (shadow) 4.dp else 0.dp,
        enabled = enabled,
        watched = false,
        watchlist = false,
        more = false,
        contentImageUrl = item.images?.getPosterUrl(),
        containerImageUrl = item.show.images?.getFanartUrl(THUMB),
        onClick = { onClick(item.show.ids.trakt) },
        onLongClick = null,
        footerContent = {
            SeasonMetaFooter(
                season = item.season,
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
