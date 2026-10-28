@file:OptIn(ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.summary.people.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.summary.people.ListEmptyView
import tv.trakt.trakt.core.summary.people.ListLoadingView
import tv.trakt.trakt.core.summary.people.helpers.historyPersonCredits
import tv.trakt.trakt.core.summary.people.model.PersonCreditItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktSectionHeader
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun HistoryCreditsList(
    loading: LoadingState,
    personShowCredits: ImmutableMap<String, ImmutableList<PersonCreditItem.ShowItem>>?,
    personMovieCredits: ImmutableMap<String, ImmutableList<PersonCreditItem.MovieItem>>?,
    userCollection: UserCollectionState,
    modifier: Modifier = Modifier,
    sectionPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onShowClick: ((Show) -> Unit)? = null,
    onMovieClick: ((Movie) -> Unit)? = null,
    onShowLongClick: ((Show) -> Unit)? = null,
    onMovieLongClick: ((Movie) -> Unit)? = null,
    onMoreClick: (() -> Unit)? = null,
) {
    val listState = rememberLazyListState(
        cacheWindow = LazyLayoutCacheWindow(
            aheadFraction = 1F,
            behindFraction = 1F,
        ),
    )

    val listItems = remember(personShowCredits, personMovieCredits, userCollection) {
        historyPersonCredits(
            showCredits = personShowCredits,
            movieCredits = personMovieCredits,
            collection = userCollection,
        )
    }

    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        TraktSectionHeader(
            title = stringResource(R.string.list_title_from_my_history),
            chevron = loading != Done || listItems.isNotEmpty(),
            collapsable = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(sectionPadding)
                .onClick(enabled = loading == Done && listItems.isNotEmpty()) {
                    onMoreClick?.invoke()
                },
        )

        Crossfade(
            targetState = loading,
            animationSpec = tween(200),
        ) { loading ->
            when (loading) {
                Idle, Loading -> {
                    ListLoadingView(
                        visible = loading.isLoading,
                        contentPadding = contentPadding,
                    )
                }

                Done -> {
                    if (listItems.isEmpty()) {
                        ListEmptyView(
                            contentPadding = sectionPadding,
                        )
                    } else {
                        LazyRow(
                            state = listState,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(TraktTheme.spacing.mainRowSpace),
                            contentPadding = contentPadding,
                        ) {
                            items(
                                items = listItems,
                                key = { it.key },
                            ) { item ->
                                PersonCreditCard(
                                    item = item,
                                    userCollection = userCollection,
                                    onShowClick = { onShowClick?.invoke(it) },
                                    onShowLongClick = { onShowLongClick?.invoke(it) },
                                    onMovieClick = { onMovieClick?.invoke(it) },
                                    onMovieLongClick = { onMovieLongClick?.invoke(it) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
