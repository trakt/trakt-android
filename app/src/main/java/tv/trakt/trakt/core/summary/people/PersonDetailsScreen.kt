@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.summary.people

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.Config.WEB_V3_BASE_URL
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.summary.ui.DetailsBackground
import tv.trakt.trakt.core.summary.ui.DetailsHeader
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.InfoChip
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.components.mediacards.skeletons.VerticalMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun PersonDetailsScreen(
    modifier: Modifier = Modifier,
    viewModel: PersonDetailsViewModel,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    PersonDetailsContent(
        state = state,
        modifier = modifier,
        onShowClick = {
            when {
                viewModel.isCurrentMediaId(it.ids.trakt) -> onNavigateBack()
                else -> onNavigateToShow(it.ids.trakt)
            }
        },
        onMovieClick = {
            when {
                viewModel.isCurrentMediaId(it.ids.trakt) -> onNavigateBack()
                else -> onNavigateToMovie(it.ids.trakt)
            }
        },
        onBackClick = onNavigateBack,
    )
}

@Composable
internal fun PersonDetailsContent(
    state: PersonDetailsState,
    modifier: Modifier = Modifier,
    onShowClick: ((Show) -> Unit)? = null,
    onMovieClick: ((Movie) -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
) {
    val context = LocalContext.current

    val listState = rememberLazyListState(
        cacheWindow = LazyLayoutCacheWindow(
            aheadFraction = 1F,
            behindFraction = 1F,
        ),
    )

    val listScrollConnection = rememberSaveable(
        saver = SimpleScrollConnection.Saver,
    ) {
        SimpleScrollConnection()
    }

    val sectionPadding = PaddingValues(
        horizontal = TraktTheme.spacing.mainPageHorizontalSpace,
    )

    val contentPadding = PaddingValues(
        top = WindowInsets.statusBars.asPaddingValues()
            .calculateTopPadding()
            .plus(16.dp),
        bottom = WindowInsets.navigationBars.asPaddingValues()
            .calculateBottomPadding()
            .plus(TraktTheme.size.navigationBarHeight * 2),
    )

    Box(
        contentAlignment = TopCenter,
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .nestedScroll(listScrollConnection),
    ) {
        state.personDetails?.let { person ->
            DetailsBackground(
                imageUrl = state.personBackdropUrl,
                translation = listScrollConnection.resultOffset,
                color = TraktTheme.colors.backgroundPrimary,
            )

            LazyColumn(
                state = listState,
                verticalArrangement = spacedBy(0.dp),
                contentPadding = contentPadding,
                overscrollEffect = null,
            ) {
                item {
                    DetailsHeader(
                        person = person,
                        loading = state.loadingDetails.isLoading,
                        onShareClick = { sharePerson(person, context) },
                        onBackClick = onBackClick ?: {},
                    )
                }

                item {
                    AnimatedVisibility(
                        visible = state.loadingDetails == DONE,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        DetailsOverview(
                            loading = state.loadingDetails,
                            overview = person.biography,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = TraktTheme.spacing.mainPageHorizontalSpace),
                        )
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = state.loadingCredits != IDLE,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        ShowsCreditsList(
                            loading = state.loadingCredits,
                            listItems = state.personShowCredits,
                            sectionPadding = sectionPadding,
                            contentPadding = sectionPadding,
                            onClick = onShowClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp),
                        )
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = state.loadingCredits != IDLE,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        MoviesCreditsList(
                            loading = state.loadingCredits,
                            listItems = state.personMovieCredits,
                            sectionPadding = sectionPadding,
                            contentPadding = sectionPadding,
                            onClick = onMovieClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailsOverview(
    loading: LoadingState,
    modifier: Modifier = Modifier,
    overview: String? = null,
) {
    var isCollapsed by remember { mutableStateOf(true) }

    Text(
        text = when {
            loading.isLoading -> ""
            overview.isNullOrBlank() -> stringResource(R.string.text_overview_placeholder)
            else -> overview
        },
        style = TraktTheme.typography.paragraphSmall,
        color = TraktTheme.colors.textSecondary,
        maxLines = if (isCollapsed) 6 else Int.MAX_VALUE,
        textAlign = TextAlign.Start,
        overflow = Ellipsis,
        modifier = modifier
            .onClick {
                isCollapsed = !isCollapsed
            },
    )
}

@Composable
private fun ShowsCreditsList(
    loading: LoadingState,
    listItems: ImmutableList<Show>?,
    modifier: Modifier = Modifier,
    sectionPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onClick: ((Show) -> Unit)? = null,
    onLongClick: ((Show) -> Unit)? = null,
) {
    val listState = rememberLazyListState(
        cacheWindow = LazyLayoutCacheWindow(
            aheadFraction = 1F,
            behindFraction = 1F,
        ),
    )

    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(sectionPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = CenterVertically,
        ) {
            TraktHeader(
                title = stringResource(R.string.page_title_shows),
            )
        }

        Crossfade(
            targetState = loading,
            animationSpec = tween(200),
        ) { loading ->
            when (loading) {
                IDLE, LOADING -> {
                    ListLoadingView(
                        visible = loading.isLoading,
                        contentPadding = contentPadding,
                    )
                }
                DONE -> {
                    if (listItems?.isEmpty() == true) {
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
                                items = listItems ?: EmptyImmutableList,
                                key = { it.ids.trakt.value },
                            ) { item ->
                                VerticalMediaCard(
                                    title = item.title,
                                    imageUrl = item.images?.getPosterUrl(),
                                    onClick = { onClick?.invoke(item) },
                                    onLongClick = { onLongClick?.invoke(item) },
                                    chipContent = {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(TraktTheme.spacing.chipsSpace),
                                        ) {
                                            item.year?.let {
                                                InfoChip(text = it.toString())
                                            }
                                            if (item.airedEpisodes > 0) {
                                                InfoChip(
                                                    text = stringResource(
                                                        R.string.tag_text_number_of_episodes,
                                                        item.airedEpisodes,
                                                    ),
                                                )
                                            }
                                        }
                                    },
                                    modifier = Modifier.animateItem(
                                        fadeInSpec = null,
                                        fadeOutSpec = null,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoviesCreditsList(
    loading: LoadingState,
    listItems: ImmutableList<Movie>?,
    modifier: Modifier = Modifier,
    sectionPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onClick: ((Movie) -> Unit)? = null,
    onLongClick: ((Movie) -> Unit)? = null,
) {
    val listState = rememberLazyListState(
        cacheWindow = LazyLayoutCacheWindow(
            aheadFraction = 1F,
            behindFraction = 1F,
        ),
    )

    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(sectionPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = CenterVertically,
        ) {
            TraktHeader(
                title = stringResource(R.string.page_title_movies),
            )
        }

        Crossfade(
            targetState = loading,
            animationSpec = tween(200),
        ) { loading ->
            when (loading) {
                IDLE, LOADING -> {
                    ListLoadingView(
                        visible = loading.isLoading,
                        contentPadding = contentPadding,
                    )
                }
                DONE -> {
                    if (listItems?.isEmpty() == true) {
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
                                items = listItems ?: EmptyImmutableList,
                                key = { it.ids.trakt.value },
                            ) { item ->
                                VerticalMediaCard(
                                    title = item.title,
                                    imageUrl = item.images?.getPosterUrl(),
                                    onClick = { onClick?.invoke(item) },
                                    onLongClick = { onLongClick?.invoke(item) },
                                    chipContent = {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(TraktTheme.spacing.chipsSpace),
                                        ) {
                                            item.released?.let {
                                                InfoChip(text = it.year.toString())
                                            }
                                            item.runtime?.inWholeMinutes?.let {
                                                val runtimeString = remember(item.runtime) {
                                                    it.durationFormat()
                                                }
                                                InfoChip(text = runtimeString)
                                            }
                                        }
                                    },
                                    modifier = Modifier.animateItem(
                                        fadeInSpec = null,
                                        fadeOutSpec = null,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ListLoadingView(
    visible: Boolean = true,
    contentPadding: PaddingValues,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(space = TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
        userScrollEnabled = false,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (visible) 1F else 0F),
    ) {
        items(count = 6) {
            VerticalMediaSkeletonCard()
        }
    }
}

@Composable
private fun ListEmptyView(contentPadding: PaddingValues) {
    Text(
        text = stringResource(R.string.list_placeholder_empty),
        color = TraktTheme.colors.textSecondary,
        style = TraktTheme.typography.heading6,
        modifier = Modifier.padding(contentPadding),
    )
}

private fun sharePerson(
    person: Person,
    context: Context,
) {
    val shareText = "${WEB_V3_BASE_URL}people/${person.ids.slug.value}"
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
    }

    context.startActivity(Intent.createChooser(intent, person.name))
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        PersonDetailsContent(
            state = PersonDetailsState(
                personDetails = PreviewData.person1,
            ),
        )
    }
}
