@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.summary.movies.features.context.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.annotation.ExperimentalCoilApi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.CustomListMinimal
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.ui.theme.colors.Shade910
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.lists.ListButton
import tv.trakt.trakt.ui.components.buttons.lists.WatchlistButton
import tv.trakt.trakt.ui.components.confirmation.RemoveConfirmationSheet
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun MovieDetailsListsView(
    movie: Movie,
    viewModel: MovieDetailsListsViewModel,
    inWatchlist: Boolean,
    modifier: Modifier = Modifier,
    onWatchlistClick: (() -> Unit)? = null,
    onAddListClick: ((TraktId) -> Unit)? = null,
    onRemoveListClick: ((TraktId) -> Unit)? = null,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var confirmRemoveWatchlistSheet by remember { mutableStateOf(false) }
    var confirmRemoveListSheet by remember { mutableStateOf<CustomListMinimal?>(null) }

    MovieDetailsListsContent(
        movie = movie,
        loading = state.loading,
        lists = state.lists,
        movieLists = state.movieLists,
        inWatchlist = inWatchlist,
        onWatchlistClick = {
            if (inWatchlist) {
                confirmRemoveWatchlistSheet = true
            } else {
                onWatchlistClick?.invoke()
            }
        },
        onListClick = {
            if (viewModel.isListed(it.id)) {
                confirmRemoveListSheet = it
            } else {
                onAddListClick?.invoke(it.id)
            }
        },
        modifier = modifier,
    )

    RemoveConfirmationSheet(
        active = confirmRemoveWatchlistSheet,
        onYes = {
            confirmRemoveWatchlistSheet = false
            onWatchlistClick?.invoke()
        },
        onNo = { confirmRemoveWatchlistSheet = false },
        title = stringResource(R.string.button_text_watchlist),
        message = stringResource(
            R.string.warning_prompt_remove_from_watchlist,
            movie.title,
        ),
    )

    RemoveConfirmationSheet(
        active = confirmRemoveListSheet != null,
        onYes = {
            confirmRemoveListSheet?.let {
                onRemoveListClick?.invoke(it.id)
                confirmRemoveListSheet = null
            }
        },
        onNo = { confirmRemoveListSheet = null },
        title = stringResource(R.string.button_text_remove_from_list),
        message = stringResource(
            R.string.warning_prompt_remove_from_personal_list,
            movie.title,
            confirmRemoveListSheet?.name ?: "",
        ),
    )
}

@Composable
private fun MovieDetailsListsContent(
    movie: Movie,
    loading: LoadingState,
    inWatchlist: Boolean,
    lists: ImmutableList<CustomListMinimal>,
    movieLists: ImmutableSet<TraktId>,
    modifier: Modifier = Modifier,
    onWatchlistClick: (() -> Unit)? = null,
    onListClick: ((CustomListMinimal) -> Unit)? = null,
) {
    val genresText = remember(movie.genres) {
        movie.genres.take(2).joinToString(" / ") { genre ->
            genre.replaceFirstChar {
                it.uppercaseChar()
            }
        }
    }

    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier,
    ) {
        Text(
            text = movie.title,
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading2,
            maxLines = 1,
            overflow = Ellipsis,
            autoSize = TextAutoSize.StepBased(
                maxFontSize = TraktTheme.typography.heading2.fontSize,
                minFontSize = 16.sp,
                stepSize = 2.sp,
            ),
        )

        Text(
            text = "${movie.released?.year ?: movie.year}  •  $genresText",
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.paragraphSmaller,
            maxLines = 1,
            overflow = Ellipsis,
            modifier = Modifier
                .padding(top = 2.dp),
        )

        Spacer(
            modifier = Modifier
                .padding(top = 22.dp)
                .background(Shade910)
                .fillMaxWidth()
                .height(1.dp),
        )

        ActionButtons(
            loading = loading.isLoading,
            inWatchlist = inWatchlist,
            lists = lists,
            movieLists = movieLists,
            onWatchlistClick = onWatchlistClick,
            onListClick = onListClick,
            modifier = Modifier
                .padding(top = 12.dp),
        )
    }
}

@Composable
private fun ActionButtons(
    modifier: Modifier = Modifier,
    loading: Boolean,
    inWatchlist: Boolean,
    lists: ImmutableList<CustomListMinimal>,
    movieLists: ImmutableSet<TraktId>,
    onWatchlistClick: (() -> Unit)? = null,
    onListClick: ((CustomListMinimal) -> Unit)? = null,
) {
    val scrollState = rememberScrollState()
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.contextItemsSpace),
        modifier = modifier
            .verticalScroll(
                state = scrollState,
                overscrollEffect = null,
            ),
    ) {
        WatchlistButton(
            checked = inWatchlist,
            modifier = Modifier
                .fillMaxWidth()
                .onClick {
                    onWatchlistClick?.invoke()
                },
        )

        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(6.dp),
            modifier = Modifier
                .padding(top = 8.dp, bottom = 8.dp),
        ) {
            Text(
                text = stringResource(R.string.list_title_personal_lists),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.heading5.copy(
                    fontSize = 16.sp,
                ),
                maxLines = 1,
                overflow = Ellipsis,
                textAlign = TextAlign.Start,
                modifier = Modifier,
            )
        }

        for (list in lists) {
            ListButton(
                text = list.name,
                enabled = !loading,
                checked = movieLists.contains(list.id),
                modifier = Modifier
                    .fillMaxWidth()
                    .onClick(enabled = !loading) {
                        onListClick?.invoke(list)
                    },
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun Preview() {
    TraktTheme {
        MovieDetailsListsContent(
            movie = PreviewData.movie1,
            loading = LoadingState.DONE,
            inWatchlist = true,
            lists = listOf(PreviewData.customListMinimal1).toImmutableList(),
            movieLists = setOf(PreviewData.movie1.ids.trakt).toImmutableSet(),
        )
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun Preview2() {
    TraktTheme {
        MovieDetailsListsContent(
            movie = PreviewData.movie1,
            loading = LoadingState.DONE,
            inWatchlist = false,
            lists = listOf(PreviewData.customListMinimal1).toImmutableList(),
            movieLists = emptySet<TraktId>().toImmutableSet(),
        )
    }
}
