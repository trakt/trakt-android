package tv.trakt.trakt.tv.core.details.movie

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush.Companion.horizontalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle.Event.ON_RESUME
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.tv.LocalDrawerVisibility
import tv.trakt.trakt.tv.LocalSnackbarState
import tv.trakt.trakt.tv.R
import tv.trakt.trakt.tv.common.model.CastPerson
import tv.trakt.trakt.tv.common.model.Comment
import tv.trakt.trakt.tv.common.model.CustomList
import tv.trakt.trakt.tv.common.model.ExternalRating
import tv.trakt.trakt.tv.common.model.ExtraVideo
import tv.trakt.trakt.tv.common.model.Images
import tv.trakt.trakt.tv.common.model.Person
import tv.trakt.trakt.tv.common.model.TraktId
import tv.trakt.trakt.tv.core.details.comments.CommentDetailsDialog
import tv.trakt.trakt.tv.core.details.movie.views.content.MovieCastCrewList
import tv.trakt.trakt.tv.core.details.movie.views.content.MovieCommentsList
import tv.trakt.trakt.tv.core.details.movie.views.content.MovieCustomListsList
import tv.trakt.trakt.tv.core.details.movie.views.content.MovieExtrasList
import tv.trakt.trakt.tv.core.details.movie.views.content.MovieRelatedList
import tv.trakt.trakt.tv.core.details.movie.views.header.MovieActionButtons
import tv.trakt.trakt.tv.core.details.movie.views.header.MovieHeader
import tv.trakt.trakt.tv.core.details.ui.BackdropImage
import tv.trakt.trakt.tv.core.movies.model.Movie
import tv.trakt.trakt.tv.core.people.navigation.PersonDestination
import tv.trakt.trakt.tv.helpers.preview.PreviewData
import tv.trakt.trakt.tv.ui.theme.TraktTheme
import java.time.ZonedDateTime
import kotlin.math.roundToInt

private val sections = listOf(
    "poster",
    "buttons",
    "extras",
    "people",
    "comments",
    "related",
    "lists",
)

@Composable
internal fun MovieDetailsScreen(
    viewModel: MovieDetailsViewModel,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToPerson: (PersonDestination) -> Unit,
    onNavigateToList: (CustomList) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val localContext = LocalContext.current
    val localSnack = LocalSnackbarState.current

    MovieDetailsScreenContent(
        state = state,
        onNavigateToMovie = onNavigateToMovie,
        onNavigateToPerson = onNavigateToPerson,
        onNavigateToList = onNavigateToList,
        onWatchlistClick = viewModel::toggleWatchlist,
        onHistoryClick = viewModel::toggleHistory,
    )

    LaunchedEffect(state.snackMessage) {
        state.snackMessage?.let {
            localSnack.showSnackbar(it.get(localContext))
            viewModel.clearInfoMessage()
        }
    }
}

@Composable
private fun MovieDetailsScreenContent(
    state: MovieDetailsState,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToPerson: (PersonDestination) -> Unit,
    onNavigateToList: (CustomList) -> Unit,
    onHistoryClick: () -> Unit,
    onWatchlistClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val drawerVisibility = LocalDrawerVisibility.current

    var focusedSection by rememberSaveable { mutableStateOf<String?>(null) }
    val focusRequesters = remember {
        sections.associateBy(
            keySelector = { it },
            valueTransform = { FocusRequester() },
        )
    }

    var selectedComment by remember { mutableStateOf<Comment?>(null) }
    var contentActive by remember { mutableStateOf(true) }

    LaunchedEffect(contentActive) {
        drawerVisibility.value = contentActive
    }

    LifecycleEventEffect(ON_RESUME) {
        // Restore focus to the last focused row
        if (state.movieDetails != null) {
            focusRequesters[focusedSection]?.requestFocus()
        }
    }

    BackHandler(enabled = selectedComment != null || !contentActive) {
        if (!contentActive) {
            contentActive = true
            return@BackHandler
        }
        selectedComment = null
        focusRequesters["comments"]?.requestFocus()
    }

    Box(
        contentAlignment = Alignment.TopStart,
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
            imageUrl = state.movieDetails?.images?.getFanartUrl(Images.Size.FULL),
            blur = 4.dp,
            imageAlpha = 0.85F,
            saturation = 0.95F,
            active = contentActive,
            modifier = Modifier
                .graphicsLayer {
                    // Parallax effect
                    translationY = (scrollState.value * -0.2F).roundToInt().toFloat()
                },
        )

        if (state.movieDetails != null) {
            Column(
                verticalArrangement = spacedBy(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(bottom = TraktTheme.spacing.mainContentVerticalSpace)
                    .alpha(if (contentActive) 1F else 0F),
            ) {
                MovieHeader(
                    movie = state.movieDetails,
                    movieCollection = state.movieCollection,
                    externalRating = state.movieRatings,
                    focusRequester = focusRequesters.getValue("poster"),
                    onFocused = { focusedSection = it },
                    onPosterClick = { contentActive = !contentActive },
                    onPosterUnfocused = { contentActive = true },
                )

                MainContent(
                    state = state,
                    focusRequesters = focusRequesters,
                    onFocused = { focusedSection = it },
                    onMovieClick = { onNavigateToMovie(it.ids.trakt) },
                    onPersonClick = {
                        onNavigateToPerson(
                            PersonDestination(
                                personId = it.ids.trakt.value,
                                sourceId = state.movieDetails.ids.trakt.value,
                                backdropUrl = state.movieDetails.images?.getFanartUrl(Images.Size.FULL),
                            ),
                        )
                    },
                    onCommentClick = { selectedComment = it },
                    onListClick = onNavigateToList,
                    onHistoryClick = onHistoryClick,
                    onWatchlistClick = onWatchlistClick,
                )
            }
        }

        CommentDetailsOverlay(
            selectedComment = selectedComment,
            onExited = {
                selectedComment = null
                focusRequesters["comments"]?.requestFocus()
            },
        )
    }
}

@Composable
private fun MainContent(
    state: MovieDetailsState,
    onFocused: (String) -> Unit,
    onMovieClick: (Movie) -> Unit,
    onPersonClick: (Person) -> Unit,
    onCommentClick: (Comment) -> Unit,
    onListClick: (CustomList) -> Unit,
    onHistoryClick: () -> Unit,
    onWatchlistClick: () -> Unit,
    focusRequesters: Map<String, FocusRequester>,
) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            horizontalArrangement = spacedBy(24.dp, Alignment.Start),
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .padding(
                    start = TraktTheme.spacing.mainContentStartSpace,
                    end = TraktTheme.spacing.mainContentEndSpace,
                ),
        ) {
            val posterWidth = TraktTheme.size.detailsPosterSize * 0.666F
            if (state.user != null) {
                MovieActionButtons(
                    streamingState = state.movieStreamings,
                    collectionState = state.movieCollection,
                    onHistoryClick = onHistoryClick,
                    onWatchlistClick = onWatchlistClick,
                    modifier = Modifier
                        .focusGroup()
                        .focusRequester(focusRequesters.getValue("buttons"))
                        .onFocusChanged { if (it.isFocused) onFocused("buttons") },
                )
            } else {
                Spacer(Modifier.width(posterWidth))
            }

            Text(
                text = state.movieDetails?.overview ?: stringResource(R.string.error_no_overview),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.paragraphLarge,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
            )
        }

        AnimatedVisibility(
            visible = state.movieVideos?.isNotEmpty() == true,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            MovieExtrasList(
                header = stringResource(R.string.header_extras),
                videos = { state.movieVideos ?: emptyList<ExtraVideo>().toImmutableList() },
                onFocused = { onFocused("extras") },
                modifier = Modifier
                    .padding(top = 32.dp)
                    .focusRequester(focusRequesters.getValue("extras")),
            )
        }

        AnimatedVisibility(
            visible = state.movieCast?.isNotEmpty() == true,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            MovieCastCrewList(
                header = stringResource(R.string.header_cast_crew),
                cast = { state.movieCast ?: emptyList<CastPerson>().toImmutableList() },
                onFocused = { onFocused("people") },
                onClick = onPersonClick,
                modifier = Modifier
                    .padding(top = 36.dp)
                    .focusRestorer()
                    .focusRequester(focusRequesters.getValue("people")),
            )
        }

        AnimatedVisibility(
            visible = state.movieComments?.isNotEmpty() == true,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            MovieCommentsList(
                header = stringResource(R.string.header_comments),
                comments = { state.movieComments ?: emptyList<Comment>().toImmutableList() },
                onFocused = { onFocused("comments") },
                onClick = onCommentClick,
                modifier = Modifier
                    .padding(top = 36.dp)
                    .focusRequester(focusRequesters.getValue("comments")),
            )
        }

        AnimatedVisibility(
            visible = state.movieRelated?.isNotEmpty() == true,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            MovieRelatedList(
                header = stringResource(R.string.header_related_movies),
                movies = { state.movieRelated ?: emptyList<Movie>().toImmutableList() },
                onFocused = { onFocused("related") },
                onClick = onMovieClick,
                modifier = Modifier
                    .padding(top = 36.dp)
                    .focusGroup()
                    .focusRequester(focusRequesters.getValue("related")),
            )
        }

        AnimatedVisibility(
            visible = state.movieLists?.isNotEmpty() == true,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            MovieCustomListsList(
                header = stringResource(R.string.header_lists),
                lists = { state.movieLists ?: emptyList<CustomList>().toImmutableList() },
                onFocused = { onFocused("lists") },
                onClick = onListClick,
                modifier = Modifier
                    .padding(top = 36.dp)
                    .focusRequester(focusRequesters.getValue("lists")),
            )
        }
    }
}

@Composable
private fun CommentDetailsOverlay(
    selectedComment: Comment?,
    onExited: () -> Unit,
) {
    AnimatedVisibility(
        visible = selectedComment != null,
        enter = EnterTransition.None,
        exit = ExitTransition.None,
    ) {
        if (selectedComment != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        horizontalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.9F))),
                    ),
            ) {
                CommentDetailsDialog(
                    comment = selectedComment,
                    modifier = Modifier
                        .padding(32.dp)
                        .width(400.dp)
                        .align(Alignment.TopEnd)
                        .animateEnterExit(
                            enter = fadeIn() + slideInHorizontally { it / 10 },
                            exit = ExitTransition.None,
                        )
                        .focusProperties {
                            onExit = { onExited() }
                        },
                )
            }
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
private fun Preview() {
    TraktTheme {
        MovieDetailsScreenContent(
            state = MovieDetailsState(
                movieDetails = PreviewData.movie1,
                movieRatings = ExternalRating(
                    tmdb = ExternalRating.TmdbRating(
                        rating = 8.5F,
                        votes = 123456,
                        link = "https://www.themoviedb.org/movie/123456",
                    ),
                    imdb = ExternalRating.ImdbRating(
                        rating = 7.9F,
                        votes = 1_267_356,
                        link = "https://www.imdb.com/title/tt1234567/",
                    ),
                    meta = ExternalRating.MetaRating(
                        rating = 85,
                        link = "https://www.metacritic.com/movie/some-movie",
                    ),
                    rotten = ExternalRating.RottenRating(
                        rating = 90F,
                        state = "Fresh",
                        userRating = 80,
                        userState = "spilled",
                        link = "https://www.rottentomatoes.com/m/some_movie",
                    ),
                ),
                movieVideos = listOf(
                    ExtraVideo(
                        title = "Featurette: Thailand",
                        url = "https://youtube.com/watch?v=4mdAbk4d8KY",
                        site = "youtube",
                        type = "behind the scenes",
                        official = true,
                        publishedAt = ZonedDateTime.now(),
                    ),
                    ExtraVideo(
                        title = "Featurette: Thailand 2",
                        url = "https://youtube.com/watch?v=4mdAbk4dXXY",
                        site = "youtube",
                        type = "trailer",
                        official = true,
                        publishedAt = ZonedDateTime.now(),
                    ),
                ).toImmutableList(),
                movieCast = listOf(
                    CastPerson(
                        person = PreviewData.person1,
                        characters = listOf("Character 1", "Character 2"),
                    ),
                    CastPerson(
                        person = PreviewData.person2,
                        characters = listOf("Character 2"),
                    ),
                ).toImmutableList(),
                movieRelated = listOf(
                    PreviewData.movie1,
                    PreviewData.movie2,
                ).toImmutableList(),
                movieLists = listOf(
                    PreviewData.customList1,
                ).toImmutableList(),
            ),
            onNavigateToMovie = {},
            onNavigateToPerson = {},
            onNavigateToList = {},
            onHistoryClick = {},
            onWatchlistClick = {},
        )
    }
}
