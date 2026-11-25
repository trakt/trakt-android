package tv.trakt.trakt.core.summary.sentiment.views

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W600
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.model.MediaMatch
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.recommended.RecommendedSource
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.core.home.sections.recommended.whythis.WhyThisGridCell
import tv.trakt.trakt.core.home.sections.recommended.whythis.WhyThisPosterGrid
import tv.trakt.trakt.core.summary.ui.views.insights.MatchRing
import tv.trakt.trakt.core.summary.ui.views.insights.MatchRingSize
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.chips.InfoChip
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun MatchContent(
    score: Int,
    taste: ImmutableList<MediaMatch.Taste>,
    reasons: ImmutableList<RecommendedSource>,
    reasonsLoading: LoadingState,
    vip: Boolean,
    modifier: Modifier = Modifier,
    collection: UserCollectionState = UserCollectionState.Default,
    onMovieClick: (TraktId) -> Unit = {},
    onShowClick: (TraktId) -> Unit = {},
    upsell: @Composable () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(24.dp),
        modifier = modifier
            .fillMaxWidth(),
    ) {
        MatchScore(
            score = score,
            vip = vip,
        )

        if (taste.isNotEmpty()) {
            MatchTaste(taste = taste)
        }

        if (vip && score >= MediaMatch.LOW_THRESHOLD) {
            MatchReasons(
                reasons = reasons,
                loading = reasonsLoading,
                collection = collection,
                onMovieClick = onMovieClick,
                onShowClick = onShowClick,
            )
        }

        if (!vip) {
            upsell()
        }
    }
}

@Composable
private fun MatchScore(
    score: Int,
    vip: Boolean,
) {
    Row(
        horizontalArrangement = spacedBy(12.dp),
        verticalAlignment = CenterVertically,
    ) {
        MatchRing(
            score = score,
            size = MatchRingSize.Drawer,
        )
        Column(
            verticalArrangement = spacedBy(2.dp),
        ) {
            Text(
                text = stringResource(R.string.text_match_better_than, score),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.paragraphSmall.copy(fontWeight = W600),
            )
            Text(
                text = stringResource(
                    when {
                        vip -> R.string.text_match_basis_history
                        else -> R.string.text_match_basis_free
                    },
                ),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.paragraphSmaller,
            )
        }
    }
}

@Composable
private fun MatchTaste(taste: ImmutableList<MediaMatch.Taste>) {
    Column(
        verticalArrangement = spacedBy(8.dp),
    ) {
        SectionTitle(text = stringResource(R.string.header_match_taste))
        FlowRow(
            horizontalArrangement = spacedBy(TraktTheme.spacing.chipsSpace),
            verticalArrangement = spacedBy(TraktTheme.spacing.chipsSpace),
        ) {
            taste.forEach { item ->
                TasteChip(text = item.label)
            }
        }
    }
}

@Composable
private fun TasteChip(text: String) {
    InfoChip(
        text = text,
        contentColor = TraktTheme.colors.chipContent,
        extraPadding = PaddingValues(
            horizontal = 2.dp,
            vertical = 1.dp,
        ),
    )
}

@Composable
private fun MatchReasons(
    reasons: ImmutableList<RecommendedSource>,
    loading: LoadingState,
    collection: UserCollectionState,
    onMovieClick: (TraktId) -> Unit,
    onShowClick: (TraktId) -> Unit,
) {
    if (loading != LoadingState.Done) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
        ) {
            FilmProgressIndicator(size = 32.dp)
        }
        return
    }

    if (reasons.isEmpty()) return

    Column(
        verticalArrangement = spacedBy(8.dp),
    ) {
        SectionTitle(text = stringResource(R.string.header_match_reasons))
        WhyThisPosterGrid(
            cells = reasons.map { reason ->
                val movie = reason.movie
                val show = reason.show
                when {
                    movie != null -> WhyThisGridCell.Poster(
                        title = movie.title,
                        imageUrl = movie.images?.getPosterUrl(),
                        watched = collection.isWatched(movie.ids.trakt, MediaType.Movie, null),
                        watchlist = collection.isWatchlist(movie.ids.trakt, MediaType.Movie),
                        onClick = { onMovieClick(movie.ids.trakt) },
                    )

                    show != null -> WhyThisGridCell.Poster(
                        title = show.title,
                        imageUrl = show.images?.getPosterUrl(),
                        watched = collection.isWatched(show.ids.trakt, MediaType.Show, show.airedEpisodes),
                        watching = collection.isWatching(show.ids.trakt, MediaType.Show, show.airedEpisodes),
                        watchlist = collection.isWatchlist(show.ids.trakt, MediaType.Show),
                        onClick = { onShowClick(show.ids.trakt) },
                    )

                    else -> WhyThisGridCell.Poster(title = "", imageUrl = null)
                }
            },
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = TraktTheme.colors.textPrimary,
        style = TraktTheme.typography.heading5,
    )
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        MatchContent(
            score = 86,
            taste = persistentListOf(
                MediaMatch.Taste(slug = "world-war-ii", label = "World War II"),
                MediaMatch.Taste(slug = "heist", label = "Heist"),
            ),
            reasons = persistentListOf(),
            reasonsLoading = LoadingState.Done,
            vip = true,
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun PreviewFree() {
    TraktThemePreview {
        MatchContent(
            score = 32,
            taste = persistentListOf(),
            reasons = persistentListOf(),
            reasonsLoading = LoadingState.Idle,
            vip = false,
        )
    }
}
