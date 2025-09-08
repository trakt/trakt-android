package tv.trakt.trakt.app.core.people

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.common.ui.GenericErrorView
import tv.trakt.trakt.app.common.ui.InfoChip
import tv.trakt.trakt.app.common.ui.PositionFocusLazyRow
import tv.trakt.trakt.app.common.ui.mediacards.HorizontalMediaCard
import tv.trakt.trakt.app.core.details.ui.BackdropImage
import tv.trakt.trakt.app.core.details.ui.PosterImage
import tv.trakt.trakt.app.helpers.extensions.emptyFocusListItems
import tv.trakt.trakt.app.helpers.preview.PreviewData
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.resources.R
import kotlin.math.roundToInt

private val sections = listOf(
    "poster",
    "overview",
    "shows",
    "movies",
)

@Composable
internal fun PersonDetailsScreen(
    viewModel: PersonDetailsViewModel,
    onShowClick: (TraktId) -> Unit,
    onMovieClick: (TraktId) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    PersonDetailsScreenContent(
        state = state,
        onShowClick = {
            if (viewModel.validateSourceId(it.ids.trakt)) {
                onShowClick(it.ids.trakt)
            }
        },
        onMovieClick = {
            if (viewModel.validateSourceId(it.ids.trakt)) {
                onMovieClick(it.ids.trakt)
            }
        },
    )
}

@Composable
private fun PersonDetailsScreenContent(
    state: PersonDetailsState,
    modifier: Modifier = Modifier,
    onShowClick: (Show) -> Unit,
    onMovieClick: (Movie) -> Unit,
) {
    var focusedSection by rememberSaveable { mutableStateOf<String?>(null) }
    val focusRequesters = remember {
        sections.associateBy(
            keySelector = { it },
            valueTransform = { FocusRequester() },
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .focusProperties {
                onEnter = {
                    focusRequesters[focusedSection]?.requestFocus()
                }
            },
    ) {
        val scrollState = rememberScrollState()

        BackdropImage(
            imageUrl = state.personBackdropUrl,
            modifier = Modifier
                .graphicsLayer {
                    // Parallax effect
                    translationY = (scrollState.value * -0.2F).roundToInt().toFloat()
                },
            saturation = 0.8F,
        )

        if (state.personDetails != null) {
            Column(
                verticalArrangement = spacedBy(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(bottom = TraktTheme.spacing.mainContentVerticalSpace),
            ) {
                HeaderContent(
                    person = state.personDetails,
                    focusRequesters = focusRequesters,
                    onFocused = { focusedSection = it },
                    onExpanded = {
                        focusRequesters["poster"]?.requestFocus()
                        focusRequesters["overview"]?.requestFocus()
                    },
                )
                if (state.error == null) {
                    MainContent(
                        showCredits = state.personShowCredits,
                        movieCredits = state.personMovieCredits,
                        onFocused = { focusedSection = it },
                        onShowClick = onShowClick,
                        onMovieClick = onMovieClick,
                        focusRequesters = focusRequesters,
                    )
                } else {
                    GenericErrorView(
                        error = state.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = TraktTheme.spacing.mainContentStartSpace,
                                end = TraktTheme.spacing.mainContentEndSpace,
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderContent(
    person: Person,
    modifier: Modifier = Modifier,
    focusRequesters: Map<String, FocusRequester>,
    onFocused: (String) -> Unit,
    onExpanded: () -> Unit,
) {
    var isBioExpanded by remember { mutableStateOf(false) }
    var isBioFocused by remember { mutableStateOf(false) }

    Row(
        horizontalArrangement = spacedBy(24.dp, Alignment.Start),
        verticalAlignment = Alignment.Bottom,
        modifier = modifier
            .padding(
                start = TraktTheme.spacing.mainContentStartSpace,
                top = TraktTheme.spacing.mainContentVerticalSpace,
                end = TraktTheme.spacing.mainContentEndSpace,
            )
            .fillMaxWidth()
            .height(TraktTheme.size.detailsPosterSize),
    ) {
        PosterImage(
            posterUrl = person.images?.getHeadshotUrl(),
            modifier = Modifier
                .onFocusChanged {
                    if (it.hasFocus) onFocused("poster")
                }
                .focusRequester(focusRequesters.getValue("poster"))
                .focusable(),
        )

        Column(
            verticalArrangement = spacedBy(8.dp),
        ) {
            Text(
                text = person.name,
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading3,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = TraktTheme.spacing.mainContentEndSpace),
            )

            AnimatedVisibility(
                visible = !person.biography.isNullOrBlank(),
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                val color = TraktTheme.colors.accent
                Text(
                    text = person.biography ?: stringResource(R.string.text_overview_placeholder),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.paragraphLarge,
                    maxLines = if (isBioExpanded) Int.MAX_VALUE else 5,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .focusRequester(focusRequesters.getValue("overview"))
                        .onFocusChanged {
                            isBioFocused = it.isFocused
                        }
                        .onClick {
                            isBioExpanded = !isBioExpanded
                            if (isBioExpanded) {
                                onExpanded()
                            }
                        }
                        .drawWithContent {
                            drawContent()
                            if (isBioFocused) {
                                val offset = 10.dp
                                drawRoundRect(
                                    color = color,
                                    topLeft = Offset(-offset.toPx(), -(offset / 1.5F).toPx()),
                                    size = Size(
                                        width = size.width + (offset * 2).toPx(),
                                        height = size.height + (offset * 1.5F).toPx(),
                                    ),
                                    cornerRadius = CornerRadius(16.dp.toPx()),
                                    style = Stroke(width = 2.5.dp.toPx()),
                                )
                            }
                        },
                )
            }
        }
    }
}

@Composable
private fun MainContent(
    showCredits: ImmutableList<Show>?,
    movieCredits: ImmutableList<Movie>?,
    onFocused: (String) -> Unit,
    onShowClick: (Show) -> Unit,
    onMovieClick: (Movie) -> Unit,
    focusRequesters: Map<String, FocusRequester>,
) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxWidth(),
    ) {
        AnimatedVisibility(
            visible = showCredits?.isNotEmpty() == true,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            ShowCreditsList(
                header = stringResource(R.string.page_title_shows),
                shows = showCredits ?: emptyList<Show>().toImmutableList(),
                onFocused = { onFocused("shows") },
                onClicked = onShowClick,
                modifier = Modifier
                    .focusRequester(focusRequesters.getValue("shows")),
            )
        }

        AnimatedVisibility(
            visible = movieCredits?.isNotEmpty() == true,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            MovieCreditsList(
                header = stringResource(R.string.page_title_movies),
                movies = movieCredits ?: emptyList<Movie>().toImmutableList(),
                onFocused = { onFocused("movies") },
                onClicked = onMovieClick,
                modifier = Modifier
                    .padding(top = 36.dp)
                    .focusRequester(focusRequesters.getValue("movies")),
            )
        }
    }
}

@Composable
private fun ShowCreditsList(
    header: String,
    shows: ImmutableList<Show>,
    onFocused: () -> Unit,
    onClicked: (Show) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier,
    ) {
        Text(
            text = header,
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading4,
            modifier = Modifier.padding(
                start = TraktTheme.spacing.mainContentStartSpace,
            ),
        )

        PositionFocusLazyRow(
            contentPadding = PaddingValues(
                start = TraktTheme.spacing.mainContentStartSpace,
                end = TraktTheme.spacing.mainContentEndSpace,
            ),
        ) {
            items(
                items = shows,
                key = { it.ids.trakt.value },
            ) { show ->
                HorizontalMediaCard(
                    title = show.title,
                    containerImageUrl = show.images?.getFanartUrl(),
                    contentImageUrl = show.images?.getLogoUrl(),
                    paletteColor = show.colors?.colors?.second,
                    onClick = { onClicked(show) },
                    footerContent = {
                        val episodes = show.airedEpisodes
                        if (episodes > 0) {
                            InfoChip(
                                text = stringResource(R.string.tag_text_number_of_episodes, show.airedEpisodes),
                            )
                        }
                    },
                    modifier = Modifier
                        .onFocusChanged {
                            if (it.hasFocus) {
                                onFocused()
                            }
                        },
                )
            }

            emptyFocusListItems()
        }
    }
}

@Composable
private fun MovieCreditsList(
    header: String,
    movies: ImmutableList<Movie>,
    onFocused: () -> Unit,
    onClicked: (Movie) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier,
    ) {
        Text(
            text = header,
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading4,
            modifier = Modifier.padding(
                start = TraktTheme.spacing.mainContentStartSpace,
            ),
        )

        PositionFocusLazyRow(
            contentPadding = PaddingValues(
                start = TraktTheme.spacing.mainContentStartSpace,
                end = TraktTheme.spacing.mainContentEndSpace,
            ),
        ) {
            items(
                items = movies,
                key = { it.ids.trakt.value },
            ) { movie ->
                HorizontalMediaCard(
                    title = movie.title,
                    containerImageUrl = movie.images?.getFanartUrl(),
                    contentImageUrl = movie.images?.getLogoUrl(),
                    paletteColor = movie.colors?.colors?.second,
                    onClick = { onClicked(movie) },
                    footerContent = {
                        val runtime = movie.runtime?.inWholeMinutes
                        if (runtime != null) {
                            InfoChip(
                                text = runtime.durationFormat(),
                            )
                        }
                    },
                    modifier = Modifier
                        .onFocusChanged {
                            if (it.hasFocus) {
                                onFocused()
                            }
                        },
                )
            }

            emptyFocusListItems()
        }
    }
}

@Preview(
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF131517,
    heightDp = 1500,
)
@Composable
private fun ScreenPreview() {
    TraktTheme {
        PersonDetailsScreenContent(
            state = PersonDetailsState(
                isLoading = false,
                personDetails = PreviewData.person1,
                personShowCredits = listOf(PreviewData.show1).toImmutableList(),
                personMovieCredits = listOf(PreviewData.movie1, PreviewData.movie2).toImmutableList(),
            ),
            onShowClick = {},
            onMovieClick = {},
        )
    }
}
