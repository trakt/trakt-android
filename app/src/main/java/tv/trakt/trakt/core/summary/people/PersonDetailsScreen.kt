@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.summary.people

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import kotlinx.collections.immutable.toImmutableMap
import tv.trakt.trakt.common.Config.WEB_V3_BASE_URL
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.extensions.longDateFormat
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.ui.theme.colors.Shade900
import tv.trakt.trakt.core.movies.ui.context.sheet.MovieContextSheet
import tv.trakt.trakt.core.shows.ui.context.sheet.ShowContextSheet
import tv.trakt.trakt.core.summary.people.ui.HistoryCreditsList
import tv.trakt.trakt.core.summary.people.ui.MoviesCreditsList
import tv.trakt.trakt.core.summary.people.ui.ShowsCreditsList
import tv.trakt.trakt.core.summary.ui.DetailsBackground
import tv.trakt.trakt.core.summary.ui.header.DetailsHeader
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.mediacards.skeletons.VerticalMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.LocalDate

@Composable
internal fun PersonDetailsScreen(
    modifier: Modifier = Modifier,
    viewModel: PersonDetailsViewModel,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var contextShowSheet by remember { mutableStateOf<Show?>(null) }
    var contextMovieSheet by remember { mutableStateOf<Movie?>(null) }

    LaunchedEffect(
        state.navigateShow,
        state.navigateMovie,
    ) {
        state.navigateShow?.let {
            viewModel.clearNavigation()
            onNavigateToShow(it)
        }
        state.navigateMovie?.let {
            viewModel.clearNavigation()
            onNavigateToMovie(it)
        }
    }

    PersonDetailsContent(
        state = state,
        modifier = modifier,
        onShowClick = {
            when {
                viewModel.isCurrentMediaId(it.ids.trakt) -> onNavigateBack()
                else -> viewModel.navigateToShow(it)
            }
        },
        onMovieClick = {
            when {
                viewModel.isCurrentMediaId(it.ids.trakt) -> onNavigateBack()
                else -> viewModel.navigateToMovie(it)
            }
        },
        onShowLongClick = {
            contextShowSheet = it
        },
        onMovieLongClick = {
            contextMovieSheet = it
        },
        onBackClick = onNavigateBack,
    )

    ShowContextSheet(
        show = contextShowSheet,
        onDismiss = {
            contextShowSheet = null
        },
    )

    MovieContextSheet(
        movie = contextMovieSheet,
        onDismiss = {
            contextMovieSheet = null
        },
    )
}

@Composable
internal fun PersonDetailsContent(
    state: PersonDetailsState,
    modifier: Modifier = Modifier,
    onShowClick: ((Show) -> Unit)? = null,
    onShowLongClick: ((Show) -> Unit)? = null,
    onMovieClick: ((Movie) -> Unit)? = null,
    onMovieLongClick: ((Movie) -> Unit)? = null,
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
        DetailsBackground(
            imageUrl = state.personBackdropUrl,
            translation = listScrollConnection.resultOffset,
            color = TraktTheme.colors.backgroundPrimary,
        )

        state.personDetails?.let { person ->
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
                    DetailsBirthday(
                        birthday = person.birthday,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = TraktTheme.spacing.mainPageHorizontalSpace,
                                end = TraktTheme.spacing.mainPageHorizontalSpace,
                                bottom = 24.dp,
                            ),
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

                if (state.user != null) {
                    item {
                        AnimatedVisibility(
                            visible = state.loadingCredits != IDLE,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            HistoryCreditsList(
                                loading = state.loadingCredits,
                                personShowCredits = state.personShowCredits,
                                personMovieCredits = state.personMovieCredits,
                                userCollection = state.collection,
                                sectionPadding = sectionPadding,
                                contentPadding = sectionPadding,
                                onShowClick = onShowClick,
                                onMovieClick = onMovieClick,
                                onShowLongClick = onShowLongClick,
                                onMovieLongClick = onMovieLongClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 24.dp),
                            )
                        }
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
                            person = state.personDetails,
                            listItems = (state.personShowCredits ?: emptyMap()).toImmutableMap(),
                            userCollection = state.collection,
                            sectionPadding = sectionPadding,
                            contentPadding = sectionPadding,
                            onClick = onShowClick,
                            onLongClick = onShowLongClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp),
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
                            person = state.personDetails,
                            listItems = (state.personMovieCredits ?: emptyMap()).toImmutableMap(),
                            userCollection = state.collection,
                            sectionPadding = sectionPadding,
                            contentPadding = sectionPadding,
                            onClick = onMovieClick,
                            onLongClick = onMovieLongClick,
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
private fun DetailsBirthday(
    modifier: Modifier = Modifier,
    birthday: LocalDate? = null,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = spacedBy(24.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.End,
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = stringResource(R.string.header_birthday).uppercase(),
                style = TraktTheme.typography.meta,
                color = TraktTheme.colors.textSecondary.copy(alpha = 0.7f),
            )
            Text(
                text = birthday?.format(longDateFormat) ?: "-",
                style = TraktTheme.typography.paragraphSmall,
                color = TraktTheme.colors.textPrimary,
                textAlign = TextAlign.Start,
            )
        }

        Spacer(
            modifier = Modifier
                .width(1.dp)
                .height(42.dp)
                .background(Shade900),
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.weight(1f),
        ) {
            val ageText: String = remember(birthday) {
                birthday?.let {
                    val today = nowLocalDay()
                    val age = (today.year - it.year) - when {
                        it.dayOfYear <= today.dayOfYear -> 0
                        else -> 1
                    }
                    age.toString()
                } ?: "-"
            }

            Text(
                text = stringResource(R.string.header_age).uppercase(),
                style = TraktTheme.typography.meta,
                color = TraktTheme.colors.textSecondary.copy(alpha = 0.7f),
            )
            Text(
                text = ageText,
                style = TraktTheme.typography.paragraphSmall,
                color = TraktTheme.colors.textPrimary,
                textAlign = TextAlign.Start,
            )
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
internal fun ListLoadingView(
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
        items(count = 12) {
            VerticalMediaSkeletonCard(
                chip = true,
                secondaryChip = true,
                chipRatio = 0.66F,
                secondaryChipRatio = 0.5F,
                chipSpacing = 8.dp,
            )
        }
    }
}

@Composable
internal fun ListEmptyView(contentPadding: PaddingValues) {
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
                loadingDetails = DONE,
            ),
        )
    }
}
