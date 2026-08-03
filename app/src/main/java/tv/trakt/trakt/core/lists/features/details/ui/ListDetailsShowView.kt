package tv.trakt.trakt.core.lists.features.details.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.common.model.sorting.SortType.Runtime
import tv.trakt.trakt.common.model.sorting.SortType.UserRating
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.shows.ui.ShowMetaFooter
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard

@Composable
internal fun ListDetailsShowView(
    show: Show,
    sorting: Sorting,
    showUserRating: UserRating?,
    modifier: Modifier = Modifier,
    showIcon: Boolean = false,
    shadow: Boolean = false,
    enabled: Boolean = true,
    more: Boolean = true,
    watched: Boolean = false,
    watching: Boolean = false,
    watchlist: Boolean = false,
    onClick: (TraktId) -> Unit = { },
    onLongClick: () -> Unit,
) {
    val genresText = show.genres.take(2)
        .map { stringResource(it.displayStringRes) }
        .joinToString(", ")

    PanelMediaCard(
        modifier = modifier,
        title = show.title,
        titleOriginal = show.titleOriginal,
        subtitle = genresText,
        shadow = if (shadow) 4.dp else 0.dp,
        more = more,
        enabled = enabled,
        watched = watched,
        watching = watching,
        watchlist = watchlist,
        contentImageUrl = show.images?.getPosterUrl(),
        containerImageUrl = show.images?.getFanartUrl(Images.Size.THUMB),
        onClick = { onClick(show.ids.trakt) },
        onImageClick = { onClick(show.ids.trakt) },
        onLongClick = onLongClick,
        footerContent = {
            ShowMetaFooter(
                show = show,
                mediaIcon = showIcon,
                rating = enabled && sorting.type != UserRating && sorting.type != Runtime,
                userRating = when {
                    sorting.type == UserRating && enabled -> showUserRating
                    else -> null
                },
                duration = when {
                    sorting.type == Runtime && enabled -> show.totalRuntime
                    else -> null
                },
            )
        },
    )
}
