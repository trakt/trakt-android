package tv.trakt.trakt.core.profile.sections.library.views

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.helpers.extensions.mediumDateFormat
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.core.library.model.LibraryItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.InfoChip
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.temporal.ChronoUnit.DAYS

@Composable
internal fun LibraryItemView(
    item: LibraryItem,
    modifier: Modifier = Modifier,
    onEpisodeClick: () -> Unit = {},
    onMovieClick: () -> Unit = {},
) {
    when (item) {
        is LibraryItem.EpisodeItem -> {
            VerticalMediaCard(
                title = "",
                imageUrl = item.show.images?.getPosterUrl(),
                more = false,
                onClick = onEpisodeClick,
                cardContent = {
                    InfoChip(
                        text = item.collectedAt.toLocal().format(mediumDateFormat),
                        iconPainter = painterResource(R.drawable.ic_library_check),
                        containerColor = TraktTheme.colors.chipContainerOnContent,
                    )
                },
                chipContent = { modifier ->
                    Column(
                        verticalArrangement = spacedBy(1.dp),
                        modifier = modifier
                            .onClick(onClick = onEpisodeClick),
                    ) {
                        Text(
                            text = item.show.title,
                            style = TraktTheme.typography.cardTitle,
                            color = TraktTheme.colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        Text(
                            text = item.episode.seasonEpisodeString(),
                            style = TraktTheme.typography.cardSubtitle,
                            color = TraktTheme.colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                modifier = modifier,
            )
        }

        is LibraryItem.MovieItem -> {
            VerticalMediaCard(
                title = "",
                imageUrl = item.movie.images?.getPosterUrl(),
                onClick = onMovieClick,
                more = false,
                cardContent = {
                    InfoChip(
                        text = item.collectedAt.toLocal().format(mediumDateFormat),
                        iconPainter = painterResource(R.drawable.ic_library_check),
                        containerColor = TraktTheme.colors.chipContainerOnContent,
                    )
                },
                chipContent = { modifier ->
                    Column(
                        verticalArrangement = spacedBy(1.dp),
                        modifier = modifier
                            .onClick {
                                onMovieClick()
                            },
                    ) {
                        Text(
                            text = item.movie.title,
                            style = TraktTheme.typography.cardTitle,
                            color = TraktTheme.colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        Text(
                            text = item.movie.runtime?.inWholeMinutes?.durationFormat() ?: "TBA",
                            style = TraktTheme.typography.cardSubtitle,
                            color = TraktTheme.colors.textSecondary,
                        )
                    }
                },
                modifier = modifier,
            )
        }
    }
}

@Preview
@Composable
private fun Preview1() {
    TraktTheme {
        LibraryItemView(
            item = LibraryItem.EpisodeItem(
                show = PreviewData.show1,
                episode = PreviewData.episode1,
                collectedAt = nowUtcInstant().minus(3, DAYS),
                updatedAt = nowUtcInstant().minus(1, DAYS),
                availableOn = EmptyImmutableList,
            ),
        )
    }
}

@Preview
@Composable
private fun Preview2() {
    TraktTheme {
        Row(
            horizontalArrangement = spacedBy(8.dp),
        ) {
            LibraryItemView(
                item = LibraryItem.MovieItem(
                    movie = PreviewData.movie1,
                    collectedAt = nowUtcInstant().minus(3, DAYS),
                    updatedAt = nowUtcInstant().minus(1, DAYS),
                    availableOn = EmptyImmutableList,
                ),
            )

            LibraryItemView(
                item = LibraryItem.EpisodeItem(
                    show = PreviewData.show1.copy(
                        released = nowUtc().minusDays(5),
                    ),
                    episode = PreviewData.episode1,
                    collectedAt = nowUtcInstant().minus(3, DAYS),
                    updatedAt = nowUtcInstant().minus(1, DAYS),
                    availableOn = EmptyImmutableList,
                ),
            )
        }
    }
}
