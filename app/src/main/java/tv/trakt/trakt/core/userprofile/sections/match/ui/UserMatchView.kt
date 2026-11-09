package tv.trakt.trakt.core.userprofile.sections.match.ui

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Ids
import tv.trakt.trakt.common.model.Images.Size
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.UserMatch
import tv.trakt.trakt.core.userprofile.sections.match.model.MatchStripEntry
import tv.trakt.trakt.core.userprofile.sections.match.model.sharedCount
import tv.trakt.trakt.core.userprofile.sections.match.model.toMatchStrip
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private const val TOPICS_LIMIT = 6

@Composable
internal fun UserMatchView(
    match: UserMatch,
    viewer: User?,
    user: User,
    sharedMovies: ImmutableList<Movie>?,
    sharedShows: ImmutableList<Show>?,
    modifier: Modifier = Modifier,
    onMovieClick: (Movie) -> Unit = {},
    onShowClick: (Show) -> Unit = {},
) {
    val strip = remember(match) { match.toMatchStrip() }
    val stageItems = strip
        .mapNotNull { entry ->
            when (entry.type) {
                MatchStripEntry.Type.Movie -> stageItem(entry, sharedMovies, Movie::ids) {
                    SharedMedia(it.title, it.images?.getPosterUrl(), { onMovieClick(it) })
                }
                MatchStripEntry.Type.Show -> stageItem(entry, sharedShows, Show::ids) {
                    SharedMedia(it.title, it.images?.getPosterUrl(), { onShowClick(it) })
                }
            }
        }
        .toImmutableList()

    val backdropUrl = strip.firstOrNull()?.let { entry ->
        when (entry.type) {
            MatchStripEntry.Type.Movie -> sharedMovies?.find { it.ids.trakt == entry.id }?.images
            MatchStripEntry.Type.Show -> sharedShows?.find { it.ids.trakt == entry.id }?.images
        }?.getFanartUrl(Size.MEDIUM)
    }

    Column(
        verticalArrangement = spacedBy(20.dp),
        modifier = modifier
            .verticalScroll(
                state = rememberScrollState(),
                overscrollEffect = null,
            )
            .padding(
                top = 3.dp,
                bottom = 32.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
            ),
    ) {
        MatchHero(
            score = match.score,
            viewer = viewer,
            user = user,
            backdropUrl = backdropUrl,
        )

        if (match.sharedTopics.isEmpty()) {
            Text(
                text = stringResource(R.string.match_drawer_no_shared_topics),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.paragraphSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            MatchTopics(topics = match.sharedTopics.take(TOPICS_LIMIT).toImmutableList())
        }

        MatchTitleStage(
            items = stageItems,
            more = (match.sharedCount - stageItems.size).coerceAtLeast(0),
        )
    }
}

private inline fun <T> stageItem(
    entry: MatchStripEntry,
    loaded: List<T>?,
    ids: (T) -> Ids,
    toMedia: (T) -> SharedMedia,
): MatchStageItem? {
    if (loaded == null) return MatchStageItem(entry = entry, media = null)
    val item = loaded.find { ids(it).trakt == entry.id } ?: return null
    return MatchStageItem(entry = entry, media = toMedia(item))
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        UserMatchView(
            match = UserMatchPreviewData.match,
            viewer = PreviewData.user1,
            user = PreviewData.user1,
            sharedMovies = persistentListOf(PreviewData.movie1, PreviewData.movie2),
            sharedShows = persistentListOf(PreviewData.show1, PreviewData.show2),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun LoadingSinglePreview() {
    TraktThemePreview {
        UserMatchView(
            match = UserMatchPreviewData.match.copy(
                sharedMovies = persistentListOf(PreviewData.movie1.ids.trakt),
                sharedShows = persistentListOf(),
            ),
            viewer = PreviewData.user1,
            user = PreviewData.user1,
            sharedMovies = null,
            sharedShows = null,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun NothingSharedPreview() {
    TraktThemePreview {
        UserMatchView(
            match = UserMatchPreviewData.match.copy(
                sharedTopics = persistentListOf(),
                sharedMovies = persistentListOf(),
                sharedShows = persistentListOf(),
            ),
            viewer = PreviewData.user1,
            user = PreviewData.user1,
            sharedMovies = persistentListOf(),
            sharedShows = persistentListOf(),
            modifier = Modifier.padding(16.dp),
        )
    }
}
