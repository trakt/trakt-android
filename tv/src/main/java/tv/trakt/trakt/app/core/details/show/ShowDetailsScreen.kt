package tv.trakt.trakt.app.core.details.show

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.ScrollState
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle.Event.ON_RESUME
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import tv.trakt.trakt.app.LocalDrawerVisibility
import tv.trakt.trakt.app.LocalSnackbarState
import tv.trakt.trakt.app.common.ui.ConfirmationDialog
import tv.trakt.trakt.app.core.details.comments.CommentDetailsDialog
import tv.trakt.trakt.app.core.details.show.views.content.ShowCastCrewList
import tv.trakt.trakt.app.core.details.show.views.content.ShowCommentsList
import tv.trakt.trakt.app.core.details.show.views.content.ShowCustomsList
import tv.trakt.trakt.app.core.details.show.views.content.ShowEpisodesList
import tv.trakt.trakt.app.core.details.show.views.content.ShowExtrasList
import tv.trakt.trakt.app.core.details.show.views.content.ShowRelatedList
import tv.trakt.trakt.app.core.details.show.views.content.ShowSeasonsList
import tv.trakt.trakt.app.core.details.show.views.header.ShowActionButtons
import tv.trakt.trakt.app.core.details.show.views.header.ShowHeader
import tv.trakt.trakt.app.core.details.ui.BackdropImage
import tv.trakt.trakt.app.core.episodes.model.Episode
import tv.trakt.trakt.app.core.episodes.model.Season
import tv.trakt.trakt.app.core.people.navigation.PersonDestination
import tv.trakt.trakt.app.helpers.preview.PreviewData
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.model.CastPerson
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.ExtraVideo
import tv.trakt.trakt.common.model.Images.Size
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.resources.R
import java.time.ZonedDateTime
import kotlin.math.roundToInt

private val sections = listOf(
    "poster",
    "buttons",
    "extras",
    "people",
    "seasons",
    "episodes",
    "comments",
    "related",
    "lists",
)

@Composable
internal fun ShowDetailsScreen(
    viewModel: ShowDetailsViewModel,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateToPerson: (PersonDestination) -> Unit,
    onNavigateToList: (CustomList) -> Unit,
    onNavigateToVideo: (String) -> Unit,
    onNavigateToStreamings: (showId: TraktId) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var historyConfirmationDialog by remember { mutableStateOf(false) }

    val localContext = LocalContext.current
    val localSnack = LocalSnackbarState.current

    ShowDetailsScreenContent(
        state = state,
        onNavigateToShow = onNavigateToShow,
        onNavigateToEpisode = onNavigateToEpisode,
        onNavigateToPerson = onNavigateToPerson,
        onNavigateToList = onNavigateToList,
        onNavigateToVideo = onNavigateToVideo,
        onNavigateToStreamings = {
            viewModel.clearWatchNowTip()
            onNavigateToStreamings(it)
        },
        onWatchlistClick = viewModel::toggleWatchlist,
        onSeasonClick = viewModel::loadSeason,
        onHistoryClick = {
            historyConfirmationDialog = true
        },
    )

    if (historyConfirmationDialog) {
        HistoryConfirmationOverlay(
            showTitle = state.showDetails?.title ?: "",
            onConfirm = { viewModel.toggleHistory() },
            onDismiss = { historyConfirmationDialog = false },
        )
    }

    LaunchedEffect(state.snackMessage) {
        state.snackMessage?.let {
            localSnack.showSnackbar(it.get(localContext))
            viewModel.clearInfoMessage()
        }
    }
}

@Composable
private fun ShowDetailsScreenContent(
    state: ShowDetailsState,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateToPerson: (PersonDestination) -> Unit,
    onNavigateToList: (CustomList) -> Unit,
    onNavigateToVideo: (String) -> Unit,
    onNavigateToStreamings: (showId: TraktId) -> Unit,
    onHistoryClick: () -> Unit,
    onWatchlistClick: () -> Unit,
    onSeasonClick: (Season) -> Unit,
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
        if (state.showDetails != null) {
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
            imageUrl = state.showDetails?.images?.getFanartUrl(Size.FULL),
            blur = 4.dp,
            imageAlpha = 0.85F,
            saturation = 0.95F,
            active = contentActive,
            modifier = Modifier.graphicsLayer {
                // Parallax effect
                translationY = (scrollState.value * -0.2F).roundToInt().toFloat()
            },
        )

        if (state.showDetails != null) {
            Column(
                verticalArrangement = spacedBy(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(bottom = TraktTheme.spacing.mainContentVerticalSpace)
                    .alpha(if (contentActive) 1F else 0F),
            ) {
                ShowHeader(
                    show = state.showDetails,
                    showCollection = state.showCollection,
                    externalRating = state.showRatings,
                    focusRequester = focusRequesters.getValue("poster"),
                    onFocused = { focusedSection = it },
                    onBackdropFocused = { contentActive = !contentActive },
                    onPosterUnfocused = { contentActive = true },
                )
                MainContent(
                    state = state,
                    scrollState = scrollState,
                    focusRequesters = focusRequesters,
                    onFocused = { focusedSection = it },
                    onShowClick = { onNavigateToShow(it.ids.trakt) },
                    onPersonClick = {
                        onNavigateToPerson(
                            PersonDestination(
                                personId = it.ids.trakt.value,
                                sourceId = state.showDetails.ids.trakt.value,
                                backdropUrl = state.showDetails.images?.getFanartUrl(Size.FULL),
                            ),
                        )
                    },
                    onSeasonClick = onSeasonClick,
                    onEpisodeClick = { episode ->
                        onNavigateToEpisode(state.showDetails.ids.trakt, episode)
                    },
                    onCommentClick = { selectedComment = it },
                    onListClick = onNavigateToList,
                    onVideoClick = onNavigateToVideo,
                    onHistoryClick = onHistoryClick,
                    onWatchlistClick = onWatchlistClick,
                    onStreamingsClick = {
                        onNavigateToStreamings(state.showDetails.ids.trakt)
                    },
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
    state: ShowDetailsState,
    onFocused: (String) -> Unit,
    onShowClick: (Show) -> Unit,
    onPersonClick: (Person) -> Unit,
    onSeasonClick: (Season) -> Unit,
    onEpisodeClick: (episode: Episode) -> Unit,
    onCommentClick: (Comment) -> Unit,
    onListClick: (CustomList) -> Unit,
    onVideoClick: (String) -> Unit,
    onHistoryClick: () -> Unit,
    onWatchlistClick: () -> Unit,
    onStreamingsClick: () -> Unit,
    focusRequesters: Map<String, FocusRequester>,
    scrollState: ScrollState,
) {
    var initialFocused by rememberSaveable { mutableStateOf(false) }

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
            if (state.user != null) {
                LaunchedEffect(Unit) {
                    if (initialFocused) return@LaunchedEffect
                    initialFocused = true

                    focusRequesters["buttons"]?.requestFocus()
                    delay(50)
                    scrollState.scrollTo(0)
                }

                ShowActionButtons(
                    streamingState = state.showStreamings,
                    collectionState = state.showCollection,
                    onHistoryClick = onHistoryClick,
                    onWatchlistClick = onWatchlistClick,
                    onStreamingLongClick = onStreamingsClick,
                    modifier = Modifier
                        .focusGroup()
                        .focusRequester(focusRequesters.getValue("buttons"))
                        .onFocusChanged {
                            if (it.isFocused) onFocused("buttons")
                        },
                )
            } else {
                val posterWidth = TraktTheme.size.detailsPosterSize * 0.666F
                Spacer(Modifier.width(posterWidth))
            }

            Text(
                text = state.showDetails?.overview ?: stringResource(R.string.text_overview_placeholder),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.paragraphLarge,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
            )
        }

        AnimatedVisibility(
            visible = state.showVideos?.isNotEmpty() == true,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            ShowExtrasList(
                header = stringResource(R.string.list_title_extras),
                videos = { state.showVideos ?: emptyList<ExtraVideo>().toImmutableList() },
                onFocused = { onFocused("extras") },
                onClicked = onVideoClick,
                modifier = Modifier
                    .padding(top = 24.dp)
                    .focusRequester(focusRequesters.getValue("extras")),
            )
        }

        AnimatedVisibility(
            visible = state.showCast?.isNotEmpty() == true,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            ShowCastCrewList(
                header = stringResource(R.string.list_title_actors),
                cast = { state.showCast ?: emptyList<CastPerson>().toImmutableList() },
                onFocused = { onFocused("people") },
                onClick = onPersonClick,
                modifier = Modifier
                    .padding(top = 36.dp)
                    .focusRestorer()
                    .focusRequester(focusRequesters.getValue("people")),
            )
        }

        AnimatedVisibility(
            visible = state.showSeasons.seasons.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            val headerSeasons =
                stringResource(R.string.list_title_seasons)

            val headerCurrentSeason = state.showSeasons.selectedSeason?.let {
                when {
                    it.isSpecial -> stringResource(R.string.text_season_specials)
                    else -> stringResource(R.string.text_season_number, it.number)
                }
            }

            ShowSeasonsList(
                header1 = "$headerSeasons / ",
                header2 = headerCurrentSeason ?: "",
                show = state.showDetails,
                seasons = { state.showSeasons },
                onFocused = { onFocused("seasons") },
                onSeasonClick = onSeasonClick,
                modifier = Modifier
                    .padding(top = 36.dp)
                    .focusRestorer()
                    .focusRequester(focusRequesters.getValue("seasons")),
            )
        }

        AnimatedVisibility(
            visible = state.showSeasons.selectedSeasonEpisodes.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            ShowEpisodesList(
                isLoading = state.showSeasons.isSeasonLoading,
                show = state.showDetails,
                episodes = { state.showSeasons.selectedSeasonEpisodes },
                onFocused = { onFocused("episodes") },
                onEpisodeClick = onEpisodeClick,
                modifier = Modifier
                    .padding(top = 24.dp)
                    .focusRestorer()
                    .focusRequester(focusRequesters.getValue("episodes")),
            )
        }

        AnimatedVisibility(
            visible = state.showComments?.isNotEmpty() == true,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            ShowCommentsList(
                header = stringResource(R.string.list_title_comments),
                comments = { state.showComments ?: emptyList<Comment>().toImmutableList() },
                onFocused = { onFocused("comments") },
                onClick = onCommentClick,
                modifier = Modifier
                    .padding(top = 36.dp)
                    .focusRequester(focusRequesters.getValue("comments")),
            )
        }

        AnimatedVisibility(
            visible = state.showRelated?.isNotEmpty() == true,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            ShowRelatedList(
                header = stringResource(R.string.list_title_related_shows),
                shows = { state.showRelated ?: emptyList<Show>().toImmutableList() },
                onFocused = { onFocused("related") },
                onClick = onShowClick,
                modifier = Modifier
                    .padding(top = 36.dp)
                    .focusRestorer()
                    .focusRequester(focusRequesters.getValue("related")),
            )
        }

        AnimatedVisibility(
            visible = state.showLists?.isNotEmpty() == true,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            ShowCustomsList(
                header = stringResource(R.string.list_title_popular_lists),
                lists = { state.showLists ?: emptyList<CustomList>().toImmutableList() },
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
                        horizontalGradient(
                            0.0f to Color.Transparent,
                            0.5f to Color.Black.copy(alpha = 0.7F),
                            1.0f to Color.Black.copy(alpha = 0.9F),
                        ),
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

@Composable
private fun HistoryConfirmationOverlay(
    showTitle: String,
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.66F)),
    )
    Dialog(onDismissRequest = onDismiss) {
        ConfirmationDialog(
            title = stringResource(R.string.button_text_mark_as_watched),
            message = stringResource(R.string.warning_prompt_mark_as_watched_show, showTitle),
            onConfirm = {
                onConfirm()
                onDismiss()
            },
            onCancel = onDismiss,
        )
    }
}

@Preview(
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF131517,
    heightDp = 1500,
)
@Composable
private fun MainScreenPreview() {
    TraktTheme {
        ShowDetailsScreenContent(
            state = ShowDetailsState(
                showDetails = PreviewData.show1,
                showRatings = ExternalRating(
                    imdb = ExternalRating.ImdbRating(
                        rating = 7.9F,
                        votes = 1_267_356,
                        link = "https://www.imdb.com/title/tt1234567/",
                    ),
                    meta = ExternalRating.MetaRating(
                        rating = 85,
                        link = "https://www.metacritic.com/show/some-show",
                    ),
                    rotten = ExternalRating.RottenRating(
                        rating = 90F,
                        state = "Fresh",
                        userRating = 80,
                        userState = "spilled",
                        link = "https://www.rottentomatoes.com/m/some_show",
                    ),
                ),
                showVideos = listOf(
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
                showCast = listOf(
                    CastPerson(
                        person = PreviewData.person1,
                        characters = listOf("Character 1", "Character 2"),
                    ),
                    CastPerson(
                        person = PreviewData.person2,
                        characters = listOf("Character 2"),
                    ),
                ).toImmutableList(),
                showRelated = listOf(
                    PreviewData.show1,
                    PreviewData.show2,
                ).toImmutableList(),
                showComments = listOf(
                    PreviewData.comment1,
                ).toImmutableList(),
            ),
            onNavigateToShow = {},
            onNavigateToEpisode = { _, _ -> },
            onNavigateToPerson = {},
            onNavigateToList = {},
            onNavigateToVideo = {},
            onHistoryClick = {},
            onWatchlistClick = {},
            onNavigateToStreamings = {},
            onSeasonClick = {},
        )
    }
}
