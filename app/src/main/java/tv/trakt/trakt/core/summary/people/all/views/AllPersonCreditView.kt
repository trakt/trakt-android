package tv.trakt.trakt.core.summary.people.all.views

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.uppercaseWords
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.movies.ui.MovieMetaFooter
import tv.trakt.trakt.core.shows.ui.ShowMetaFooter
import tv.trakt.trakt.core.summary.people.model.PersonCreditItem
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AllPersonCreditView(
    item: PersonCreditItem,
    userCollection: UserCollectionState,
    modifier: Modifier = Modifier,
    mediaIcon: Boolean = false,
    onShowClick: (Show) -> Unit = {},
    onShowLongClick: (Show) -> Unit = {},
    onMovieClick: (Movie) -> Unit = {},
    onMovieLongClick: (Movie) -> Unit = {},
) {
    val credit = (item.credit ?: "")
        .ifBlank { stringResource(R.string.text_unknown) }
        .uppercaseWords()

    val onClick = {
        when (item) {
            is PersonCreditItem.ShowItem -> onShowClick(item.show)
            is PersonCreditItem.MovieItem -> onMovieClick(item.movie)
        }
    }

    PanelMediaCard(
        title = item.title,
        titleOriginal = when (item) {
            is PersonCreditItem.ShowItem -> item.show.titleOriginal
            is PersonCreditItem.MovieItem -> item.movie.titleOriginal
        },
        subtitle = credit,
        contentImageUrl = item.images?.getPosterUrl(),
        containerImageUrl = item.images?.getFanartUrl(Images.Size.THUMB),
        watched = userCollection.isWatched(item.id, item.type, item.airedEpisodes),
        watching = userCollection.isWatching(item.id, item.type, item.airedEpisodes),
        watchlist = userCollection.isWatchlist(item.id, item.type),
        onClick = onClick,
        onImageClick = onClick,
        onLongClick = {
            when (item) {
                is PersonCreditItem.ShowItem -> onShowLongClick(item.show)
                is PersonCreditItem.MovieItem -> onMovieLongClick(item.movie)
            }
        },
        footerContent = {
            when (item) {
                is PersonCreditItem.ShowItem -> ShowMetaFooter(
                    show = item.show,
                    mediaIcon = mediaIcon,
                )
                is PersonCreditItem.MovieItem -> MovieMetaFooter(
                    movie = item.movie,
                    mediaIcon = mediaIcon,
                )
            }
        },
        modifier = modifier,
    )
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        Column(
            verticalArrangement = spacedBy(TraktTheme.spacing.mainListVerticalSpace),
        ) {
            AllPersonCreditView(
                item = PersonCreditItem.ShowItem(
                    show = PreviewData.show1,
                    episodesCount = 12,
                    credit = "Self",
                ),
                userCollection = UserCollectionState.Default,
                mediaIcon = true,
            )
            AllPersonCreditView(
                item = PersonCreditItem.MovieItem(
                    movie = PreviewData.movie1,
                    credit = null,
                ),
                userCollection = UserCollectionState.Default,
                mediaIcon = true,
            )
        }
    }
}
