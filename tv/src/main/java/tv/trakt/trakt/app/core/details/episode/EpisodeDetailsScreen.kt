package tv.trakt.trakt.app.core.details.episode

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
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.testing.FakeReviewManager
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import timber.log.Timber
import tv.trakt.trakt.app.BuildConfig
import tv.trakt.trakt.app.LocalDrawerVisibility
import tv.trakt.trakt.app.LocalSnackbarState
import tv.trakt.trakt.app.common.model.CastPerson
import tv.trakt.trakt.app.common.model.Comment
import tv.trakt.trakt.app.common.model.SyncHistoryEpisodeItem
import tv.trakt.trakt.app.common.ui.ConfirmationDialog
import tv.trakt.trakt.app.core.details.comments.CommentDetailsDialog
import tv.trakt.trakt.app.core.details.episode.views.content.EpisodeCastCrewList
import tv.trakt.trakt.app.core.details.episode.views.content.EpisodeCommentsList
import tv.trakt.trakt.app.core.details.episode.views.content.EpisodeRecentlyWatchedList
import tv.trakt.trakt.app.core.details.episode.views.content.EpisodeRelatedList
import tv.trakt.trakt.app.core.details.episode.views.content.EpisodeSeasonEpisodesList
import tv.trakt.trakt.app.core.details.episode.views.header.EpisodeActionButtons
import tv.trakt.trakt.app.core.details.episode.views.header.EpisodeHeader
import tv.trakt.trakt.app.core.details.ui.BackdropImage
import tv.trakt.trakt.app.core.episodes.model.Episode
import tv.trakt.trakt.app.core.people.navigation.PersonDestination
import tv.trakt.trakt.app.helpers.preview.PreviewData
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.resources.R
import kotlin.math.roundToInt

private val sections = listOf(
    "poster",
    "buttons",
    "people",
    "season",
    "comments",
    "related",
    "history",
)

@Composable
internal fun EpisodeDetailsScreen(
    viewModel: EpisodeDetailsViewModel,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateToPerson: (PersonDestination) -> Unit,
    onNavigateToStreamings: (showId: TraktId, episode: Episode) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var historyConfirmationDialog: Long? by remember { mutableStateOf(null) }

    val localContext = LocalContext.current
    val localSnack = LocalSnackbarState.current

    EpisodeDetailsScreenContent(
        state = state,
        onNavigateToShow = onNavigateToShow,
        onNavigateToEpisode = onNavigateToEpisode,
        onNavigateToPerson = onNavigateToPerson,
        onNavigateToStreamings = { showId, episode ->
            viewModel.clearWatchNowTip()
            onNavigateToStreamings(showId, episode)
        },
        onAddHistoryClick = viewModel::addToHistory,
        onRemoveHistoryClick = { historyConfirmationDialog = it },
    )

    if (historyConfirmationDialog != null) {
        HistoryConfirmationOverlay(
            title = state.episodeDetails?.seasonEpisodeString()
                ?: stringResource(R.string.list_title_recently_watched),
            episodePlayId = historyConfirmationDialog ?: -1L,
            onConfirm = viewModel::removeFromHistory,
            onDismiss = { historyConfirmationDialog = null },
        )
    }

    LaunchedEffect(state.snackMessage) {
        state.snackMessage?.let {
            localSnack.showSnackbar(it.get(localContext))
            viewModel.clearInfoMessage()
        }
    }

    LaunchedEffect(state.isReviewRequest) {
        if (state.isReviewRequest) {
            viewModel.clearReviewRequest()

            val activity = localContext as? androidx.activity.ComponentActivity
            activity?.let { activity ->
                val manager = when {
                    BuildConfig.DEBUG -> FakeReviewManager(activity)
                    else -> ReviewManagerFactory.create(activity)
                }

                manager.requestReviewFlow().addOnCompleteListener { request ->
                    if (request.isSuccessful) {
                        val reviewInfo = request.result
                        manager.launchReviewFlow(activity, reviewInfo)
                        Timber.d("Review flow launched")
                    } else {
                        Timber.w("Review flow error: ${request.exception}")
                    }
                }
            }
        }
    }
}

@Composable
private fun EpisodeDetailsScreenContent(
    state: EpisodeDetailsState,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateToPerson: (PersonDestination) -> Unit,
    onNavigateToStreamings: (showId: TraktId, episode: Episode) -> Unit,
    onAddHistoryClick: () -> Unit,
    onRemoveHistoryClick: (id: Long) -> Unit,
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

    var contentActive by remember { mutableStateOf(true) }
    var selectedComment by remember { mutableStateOf<Comment?>(null) }

    LaunchedEffect(contentActive) {
        drawerVisibility.value = contentActive
    }

    LifecycleEventEffect(ON_RESUME) {
        // Restore focus to the last focused row
        if (state.showDetails != null && state.episodeDetails != null) {
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

        val showFanartUrl = state.showDetails?.images?.getFanartUrl(Images.Size.FULL)
        val episodeFanartUrl = state.episodeDetails?.images?.getScreenshotUrl(Images.Size.FULL)

        BackdropImage(
            imageUrl = episodeFanartUrl ?: showFanartUrl,
            blur = 4.dp,
            imageAlpha = 0.85F,
            saturation = 0.95F,
            active = contentActive,
            modifier = Modifier.graphicsLayer {
                // Parallax effect
                translationY = (scrollState.value * -0.2F).roundToInt().toFloat()
            },
        )

        if (state.showDetails != null && state.episodeDetails != null) {
            Column(
                verticalArrangement = spacedBy(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(bottom = TraktTheme.spacing.mainContentVerticalSpace)
                    .alpha(if (contentActive) 1F else 0F),
            ) {
                EpisodeHeader(
                    show = state.showDetails,
                    episode = state.episodeDetails,
                    episodePlays = state.episodeHistory.episodesPlays,
                    externalRating = state.episodeRatings,
                    focusRequester = focusRequesters.getValue("poster"),
                    onFocused = { focusedSection = it },
                    onBackdropFocused = { contentActive = !contentActive },
                    onPosterUnfocused = { contentActive = true },
                )
                MainContent(
                    state = state,
                    onFocused = { focusedSection = it },
                    onShowClicked = { onNavigateToShow(it.ids.trakt) },
                    onEpisodeClicked = {
                        // Break if user taps the current episode
                        if (state.episodeDetails.ids.trakt != it.ids.trakt) {
                            onNavigateToEpisode(
                                state.showDetails.ids.trakt,
                                it,
                            )
                        } else {
                            focusRequesters["poster"]?.requestFocus()
                        }
                    },
                    onPersonClicked = {
                        onNavigateToPerson(
                            PersonDestination(
                                personId = it.ids.trakt.value,
                                sourceId = state.showDetails.ids.trakt.value,
                                backdropUrl = state.showDetails.images?.getFanartUrl(Images.Size.FULL),
                            ),
                        )
                    },
                    onCommentClicked = { selectedComment = it },
                    onAddHistoryClick = onAddHistoryClick,
                    onRemoveHistoryClick = onRemoveHistoryClick,
                    onStreamingsClick = {
                        onNavigateToStreamings(
                            state.showDetails.ids.trakt,
                            state.episodeDetails,
                        )
                    },
                    focusRequesters = focusRequesters,
                    scrollState = scrollState,
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
    state: EpisodeDetailsState,
    onFocused: (String) -> Unit,
    onShowClicked: (Show) -> Unit,
    onEpisodeClicked: (Episode) -> Unit,
    onPersonClicked: (Person) -> Unit,
    onCommentClicked: (Comment) -> Unit,
    onStreamingsClick: () -> Unit,
    onAddHistoryClick: () -> Unit,
    onRemoveHistoryClick: (id: Long) -> Unit,
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
            val posterWidth = TraktTheme.size.detailsPosterSize * 0.666F
            if (state.user != null) {
                LaunchedEffect(Unit) {
                    if (initialFocused) return@LaunchedEffect
                    initialFocused = true

                    focusRequesters["buttons"]?.requestFocus()
                    delay(50)
                    scrollState.scrollTo(0)
                }

                EpisodeActionButtons(
                    streamingState = state.episodeStreamings,
                    historyState = state.episodeHistory,
                    episode = state.episodeDetails?.seasonEpisode,
                    onHistoryClick = onAddHistoryClick,
                    onStreamingLongClick = onStreamingsClick,
                    modifier = Modifier
                        .focusGroup()
                        .focusRequester(focusRequesters.getValue("buttons"))
                        .onFocusChanged {
                            if (it.isFocused) onFocused("buttons")
                        },
                )
            } else {
                Spacer(Modifier.width(posterWidth))
            }

            Text(
                text = state.episodeDetails?.overview ?: stringResource(R.string.text_overview_placeholder),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.paragraphLarge,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
            )
        }

        AnimatedVisibility(
            visible = state.episodeCast?.isNotEmpty() == true,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            EpisodeCastCrewList(
                header = stringResource(R.string.list_title_actors),
                cast = { state.episodeCast ?: emptyList<CastPerson>().toImmutableList() },
                onFocused = { onFocused("people") },
                onClick = onPersonClicked,
                modifier = Modifier
                    .padding(top = 36.dp)
                    .focusProperties {
                        onEnter = {
                            focusRequesters["people"]?.requestFocus()
                        }
                    }
                    .focusRestorer()
                    .focusRequester(focusRequesters.getValue("people")),
            )
        }

        AnimatedVisibility(
            visible = state.episodeCast?.isNotEmpty() == true,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            EpisodeSeasonEpisodesList(
                header1 = "${stringResource(R.string.list_title_seasons)} / ",
                header2 = stringResource(R.string.text_season_number, state.episodeDetails?.season ?: 0),
                show = state.showDetails,
                episodes = { state.episodeSeason ?: emptyList<Episode>().toImmutableList() },
                onFocused = { onFocused("season") },
                onClicked = onEpisodeClicked,
                modifier = Modifier
                    .padding(top = 36.dp)
                    .focusProperties {
                        onEnter = {
                            focusRequesters["season"]?.requestFocus()
                        }
                    }
                    .focusRestorer()
                    .focusRequester(focusRequesters.getValue("season")),
            )
        }

        AnimatedVisibility(
            visible = state.episodeComments?.isNotEmpty() == true,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            EpisodeCommentsList(
                header = stringResource(R.string.list_title_comments),
                comments = { state.episodeComments ?: emptyList<Comment>().toImmutableList() },
                onFocused = { onFocused("comments") },
                onClicked = onCommentClicked,
                modifier = Modifier
                    .padding(top = 36.dp)
                    .focusRequester(focusRequesters.getValue("comments")),
            )
        }

        AnimatedVisibility(
            visible = state.episodeRelated?.isNotEmpty() == true,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            EpisodeRelatedList(
                header = stringResource(R.string.list_title_related_shows),
                shows = { state.episodeRelated ?: emptyList<Show>().toImmutableList() },
                onFocused = { onFocused("related") },
                onClicked = onShowClicked,
                modifier = Modifier
                    .padding(top = 36.dp)
                    .focusRestorer()
                    .focusRequester(focusRequesters.getValue("related")),
            )
        }

        val episodesPlays = state.episodeHistory.episodesPlays
        AnimatedVisibility(
            visible = episodesPlays > 0,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            LaunchedEffect(episodesPlays) {
                if (episodesPlays <= 0) {
                    focusRequesters["poster"]?.requestFocus()
                }
            }

            EpisodeRecentlyWatchedList(
                header = stringResource(R.string.list_title_recently_watched),
                items = { state.episodeHistory.episodes ?: emptyList<SyncHistoryEpisodeItem>().toImmutableList() },
                onFocused = { onFocused("history") },
                onClicked = {
                    onRemoveHistoryClick(it)
                    focusRequesters["history"]?.requestFocus()
                },
                modifier = Modifier
                    .padding(top = 36.dp)
                    .focusProperties {
                        onEnter = {
                            focusRequesters["history"]?.requestFocus()
                        }
                    }
                    .focusRestorer()
                    .focusRequester(focusRequesters.getValue("history")),
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
    title: String,
    episodePlayId: Long,
    onConfirm: (id: Long) -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.66F)),
    )
    Dialog(onDismissRequest = onDismiss) {
        ConfirmationDialog(
            title = title,
            message = stringResource(R.string.warning_prompt_remove_single_watched),
            onConfirm = {
                onConfirm(episodePlayId)
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
private fun Preview() {
    TraktTheme {
        EpisodeDetailsScreenContent(
            state = EpisodeDetailsState(
                showDetails = PreviewData.show1,
                episodeDetails = PreviewData.episode1,
                isLoading = false,
                snackMessage = null,
            ),
            onNavigateToShow = {},
            onNavigateToEpisode = { _, _ -> },
            onNavigateToPerson = { _ -> },
            onAddHistoryClick = {},
            onNavigateToStreamings = { _, _ -> },
            onRemoveHistoryClick = {},
        )
    }
}
