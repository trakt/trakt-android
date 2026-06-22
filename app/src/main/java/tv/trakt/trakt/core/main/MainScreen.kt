@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.main

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle.Event.ON_RESUME
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.ktx.AppUpdateResult.Available
import com.google.android.play.core.ktx.AppUpdateResult.Downloaded
import com.google.android.play.core.ktx.clientVersionStalenessDays
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.testing.FakeReviewManager
import com.jakewharton.processphoenix.ProcessPhoenix
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import timber.log.Timber
import tv.trakt.trakt.LocalBottomBarVisibility
import tv.trakt.trakt.LocalCheckInVisibility
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.MainActivity
import tv.trakt.trakt.app.BuildConfig
import tv.trakt.trakt.common.helpers.LaunchedUpdateEffect
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.extensions.HTTP_ERROR_TRAKT_VIP_LIMIT
import tv.trakt.trakt.common.helpers.extensions.getHttpCode
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.MediaType.EPISODE
import tv.trakt.trakt.common.model.MediaType.MOVIE
import tv.trakt.trakt.common.model.WhatsNew
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.auth.ConfigAuth
import tv.trakt.trakt.core.billing.navigation.navigateToBilling
import tv.trakt.trakt.core.checkin.model.CheckInState.ActiveEpisode
import tv.trakt.trakt.core.checkin.model.CheckInState.ActiveMovie
import tv.trakt.trakt.core.discover.navigation.navigateToDiscover
import tv.trakt.trakt.core.home.navigation.HomeDestination
import tv.trakt.trakt.core.lists.navigation.ListsDestination
import tv.trakt.trakt.core.lists.navigation.navigateToLists
import tv.trakt.trakt.core.lists.sections.watchlist.features.all.navigation.navigateToWatchlist
import tv.trakt.trakt.core.main.navigation.MainNavHost
import tv.trakt.trakt.core.main.navigation.isMainDestination
import tv.trakt.trakt.core.main.navigation.isStartDestination
import tv.trakt.trakt.core.main.navigation.navigateToMainDestination
import tv.trakt.trakt.core.main.ui.checkin.MainCheckInView
import tv.trakt.trakt.core.main.ui.menubar.TraktMenuBar
import tv.trakt.trakt.core.main.ui.rateprompt.MainRatePromptView
import tv.trakt.trakt.core.notifications.data.work.INTENT_NOTIFICATION_EXTRAS
import tv.trakt.trakt.core.notifications.data.work.INTENT_NOTIFICATION_TRIVIA_EXTRAS
import tv.trakt.trakt.core.notifications.model.NotificationIntentExtras
import tv.trakt.trakt.core.profile.navigation.ProfileDestination
import tv.trakt.trakt.core.profile.navigation.navigateToProfile
import tv.trakt.trakt.core.ratings.rateprompt.model.RatePromptState.AskSuppress
import tv.trakt.trakt.core.search.navigation.navigateToSearch
import tv.trakt.trakt.core.summary.episodes.navigation.navigateToEpisode
import tv.trakt.trakt.core.summary.movies.navigation.navigateToMovie
import tv.trakt.trakt.core.trivia.navigation.navigateToTrivia
import tv.trakt.trakt.core.welcome.WelcomeScreen
import tv.trakt.trakt.core.welcome.onboarding.OnboardingScreen
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.confirmation.RemoveConfirmationSheet
import tv.trakt.trakt.ui.components.whatsnew.WhatsNewSheet
import tv.trakt.trakt.ui.snackbar.MainSnackbarHost
import tv.trakt.trakt.ui.theme.TraktTheme

private const val IN_APP_UPDATE_REQUEST_CODE = 4001
private const val IN_APP_UPDATE_STALENESS_DAYS = 7

@Composable
internal fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    intent: Intent? = null,
    newIntent: MutableState<Intent?>? = null,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val localContext = LocalContext.current
    val localRes = LocalResources.current
    val localActivity = LocalActivity.current
    val localSnackbar = LocalSnackbarState.current

    val navController = rememberNavController()
    val currentDestination = navController
        .currentBackStackEntryFlow
        .collectAsStateWithLifecycle(initialValue = null)

    val searchState = rememberSearchState(currentDestination.value?.destination)
    var whatsNewState by remember { mutableStateOf<WhatsNew?>(null) }
    var stopRatePromptSheet by remember { mutableStateOf(false) }

    LifecycleEventEffect(ON_RESUME) {
        viewModel.loadData()
        viewModel.loadCheckIn()
        viewModel.loadNotifications(localContext)
    }

    LaunchedUpdateEffect(state.user) {
        when {
            state.user != null && state.loadingUser == Done -> {
                viewModel.clearLoadingUser()
                localSnackbar.showSnackbar(
                    message = localRes.getString(R.string.text_info_signed_in),
                    duration = SnackbarDuration.Short,
                )
            }
            state.user == null -> {
                navController.navigateToMainDestination(HomeDestination)
                localSnackbar.showSnackbar(
                    message = localRes.getString(R.string.text_info_signed_out),
                    duration = SnackbarDuration.Short,
                )
            }
        }
    }

    LaunchedEffect(state.userVipStatus) {
        with(state.userVipStatus) {
            if (this == null || this.first == null || this.second == null) {
                return@LaunchedEffect
            }

            if (this.first == true && this.second == false) {
                localActivity?.let {
                    ProcessPhoenix.triggerRebirth(it)
                }
            }
        }
    }

    LaunchedEffect(state.whatsNew) {
        whatsNewState = state.whatsNew
    }

    LaunchedEffect(intent, newIntent?.value) {
        handleShortcutIntent(
            intent = intent,
            navController = navController,
            onRequestFocus = searchState.onRequestFocus,
        )

        handleNotificationIntent(
            intent = newIntent?.value ?: intent,
            navController = navController,
        )
    }

    LaunchedEffect(state.ratePrompt) {
        if (state.ratePrompt is AskSuppress) {
            stopRatePromptSheet = true
            viewModel.clearRatePrompt()
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            if (it.getHttpCode() == HTTP_ERROR_TRAKT_VIP_LIMIT) {
                navController.navigateToBilling()
            }
            viewModel.clearError()
        }
    }

    LaunchedAppReview(
        state = state,
        onDismiss = viewModel::dismissInAppReview,
    )

    LaunchedAppUpdate(
        state = state,
        onCompleteUpdate = {
            scope.launch {
                viewModel.completeInAppUpdate()
            }
        },
        onClearUpdate = viewModel::clearInAppUpdate,
    )

    MainScreenContent(
        modifier = modifier,
        state = state,
        searchState = searchState,
        navController = navController,
        currentDestination = currentDestination,
        onDismissWelcome = viewModel::dismissWelcome,
        onDismissCheckIn = viewModel::dismissCheckIn,
    )

    WhatsNewSheet(
        data = whatsNewState,
        onDismiss = {
            whatsNewState?.id?.let { id ->
                viewModel.dismissWhatsNew(id)
                whatsNewState = null
            }
        },
    )

    RemoveConfirmationSheet(
        active = stopRatePromptSheet,
        title = stringResource(R.string.button_text_stop_asking),
        message = stringResource(R.string.warning_prompt_suppress_ratings_toast),
        onYes = {
            stopRatePromptSheet = false
            viewModel.suppressRatePrompt()
        },
        onNo = {
            stopRatePromptSheet = false
        },
    )

    BackHandler(
        enabled = !state.welcome.isActive,
    ) {
        with(currentDestination.value?.destination) {
            if (isStartDestination(this)) {
                (localContext as? Activity)?.finish()
                return@BackHandler
            }
            if (isMainDestination(this)) {
                navController.navigateToMainDestination(HomeDestination)
                return@BackHandler
            }
        }

        navController.popBackStack()
    }
}

@Composable
private fun MainScreenContent(
    modifier: Modifier,
    state: MainState,
    searchState: MainSearchStateHolder,
    navController: NavHostController,
    currentDestination: State<NavBackStackEntry?>,
    onDismissWelcome: () -> Unit = {},
    onDismissCheckIn: () -> Unit = {},
) {
    val localActivity = LocalActivity.current
    val localSnackbar = LocalSnackbarState.current
    val localBottomBarVisibility = LocalBottomBarVisibility.current
    val localCheckInVisibility = LocalCheckInVisibility.current
    val localUriHandler = LocalUriHandler.current

    val customThemeConfig = remember {
        (localActivity as? MainActivity)?.customThemeConfig
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Crossfade(
            targetState = state.welcome,
            animationSpec = tween(500),
        ) { targetState ->
            when {
                targetState.welcome -> {
                    WelcomeScreen(
                        onDismiss = onDismissWelcome,
                    )
                }

                targetState.onboarding -> {
                    OnboardingScreen(
                        onLogin = {
                            localUriHandler.openUri(ConfigAuth.authCodeUrl)
                        },
                    )
                }

                else -> {
                    MainNavHost(
                        navController = navController,
                        customThemeEnabled = customThemeConfig?.enabled == true,
                        userLoading = state.loadingUser.isLoading,
                        searchInput = searchState.searchInput,
                        onSearchLoading = searchState.onSearchLoading,
                    )

                    AnimatedVisibility(
                        visible = localBottomBarVisibility.value,
                        enter = fadeIn(tween(200)) + slideInVertically(initialOffsetY = { it / 2 }),
                        exit = fadeOut(tween(200)) + slideOutVertically(targetOffsetY = { it / 2 }),
                        modifier = Modifier.align(BottomCenter),
                    ) {
                        val bottomColor = TraktTheme.colors.backgroundPrimary
                        val bottomGradient = remember(localCheckInVisibility.value) {
                            when {
                                localCheckInVisibility.value -> verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        bottomColor.copy(alpha = 0.95F),
                                        bottomColor,
                                        bottomColor,
                                    ),
                                )
                                else -> SolidColor(Color.Transparent)
                            }
                        }

                        Column(
                            verticalArrangement = spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth(TraktTheme.size.navigationBarRatio)
                                .background(bottomGradient),
                        ) {
                            MainRatePromptView(
                                state = state,
                                onMediaClick = {
                                    navController.navigateToMovie(
                                        movieId = it.movie.ids.trakt,
                                    )
                                },
                            )

                            MainCheckInView(
                                state = state,
                                onMediaClick = {
                                    when (val it = state.checkIn) {
                                        is ActiveMovie -> navController.navigateToMovie(
                                            movieId = it.movie.ids.trakt,
                                        )
                                        is ActiveEpisode -> navController.navigateToEpisode(
                                            showId = it.show.ids.trakt,
                                            episode = it.episode,
                                        )
                                        else -> Unit
                                    }
                                },
                                onDismiss = onDismissCheckIn,
                            )

                            NavigationBar(
                                containerColor = TraktTheme.colors.navigationContainer,
                                contentColor = TraktTheme.colors.accent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .dropShadow(
                                        shape = RoundedCornerShape(
                                            topStart = 24.dp,
                                            topEnd = 24.dp,
                                        ),
                                        shadow = Shadow(
                                            radius = 6.dp,
                                            color = Color.Black,
                                            spread = 2.dp,
                                            alpha = 0.25F,
                                        ),
                                    )
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 24.dp,
                                            topEnd = 24.dp,
                                        ),
                                    ),
                            ) {
                                TraktMenuBar(
                                    currentDestination = currentDestination.value?.destination,
                                    enabled = localBottomBarVisibility.value,
                                    user = state.user,
                                    searchState = searchState,
                                    onSelected = {
                                        if (state.user != null || it.destination == HomeDestination) {
                                            navController.navigateToMainDestination(it.destination)
                                        } else {
                                            localUriHandler.openUri(ConfigAuth.authCodeUrl)
                                        }
                                    },
                                    onProfileSelected = {
                                        if (state.user != null) {
                                            navController.navigateToMainDestination(ProfileDestination)
                                        } else {
                                            localUriHandler.openUri(ConfigAuth.authCodeUrl)
                                        }
                                    },
                                    onReselected = {
                                        currentDestination.value?.destination?.let {
                                            if (it.hasRoute(ListsDestination::class) && state.user != null) {
                                                navController.navigateToWatchlist()
                                            }
                                        }
                                    },
                                    onSearchInput = searchState.onSearchInput,
                                )
                            }
                        }
                    }

                    MainSnackbarHost(
                        snackbarHostState = localSnackbar,
                        checkInVisible = state.checkIn?.isActive() == true,
                        ratePromptVisible = state.ratePrompt?.isActive() == true,
                    )
                }
            }
        }

        var overlayVisible by remember {
            mutableStateOf(customThemeConfig?.overlayVisible == true)
        }

        if (overlayVisible && customThemeConfig?.theme?.type == "christmas") {
            Box {
                Image(
                    painter = painterResource(R.drawable.img_splash_christmas),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            onClick = {},
                            indication = null,
                            interactionSource = null,
                        ),
                )

                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .align(BottomCenter)
                        .padding(
                            WindowInsets.navigationBars.asPaddingValues()
                                .calculateBottomPadding()
                                .plus(64.dp),
                        )
                        .background(TraktTheme.colors.accent, shape = RoundedCornerShape(100))
                        .padding(8.dp)
                        .size(18.dp)
                        .onClick {
                            (localActivity as? MainActivity)?.toggleCustomThemeOverlay()
                            overlayVisible = false
                        },
                )
            }
        }
    }
}

@Composable
private fun LaunchedAppReview(
    state: MainState,
    onDismiss: () -> Unit,
) {
    val localActivity = LocalActivity.current

    LaunchedEffect(state.review) {
        if (state.review == true) {
            onDismiss()
            localActivity?.let { activity ->
                val manager = when {
                    BuildConfig.DEBUG -> FakeReviewManager(activity)
                    else -> ReviewManagerFactory.create(activity)
                }

                manager
                    .requestReviewFlow()
                    .addOnCompleteListener { request ->
                        if (request.isSuccessful) {
                            val reviewInfo = request.result
                            manager.launchReviewFlow(activity, reviewInfo)
                            Timber.d("Review flow launched")
                        } else {
                            Timber.e("Review flow error: ${request.exception}")
                        }
                    }

                Timber.d("Launching in-app review")
            }
        }
    }
}

@Composable
private fun LaunchedAppUpdate(
    state: MainState,
    onCompleteUpdate: () -> Unit,
    onClearUpdate: () -> Unit,
) {
    val localRes = LocalResources.current
    val localActivity = LocalActivity.current
    val localSnackbar = LocalSnackbarState.current

    var hasPromptedUpdate by rememberSaveable { mutableStateOf(false) }
    var hasPromptedDownload by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.update) {
        when (val update = state.update) {
            is Available -> {
                val updateInfo = update.updateInfo

                val isStale = (updateInfo.clientVersionStalenessDays ?: -1) >= IN_APP_UPDATE_STALENESS_DAYS
                val isAllowed = updateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)

                if (isStale && isAllowed && !hasPromptedUpdate) {
                    localActivity?.let { activity ->
                        hasPromptedUpdate = true
                        update.startFlexibleUpdate(activity, IN_APP_UPDATE_REQUEST_CODE)
                    }
                }
            }
            is Downloaded -> {
                if (!hasPromptedDownload) {
                    val result = localSnackbar.showSnackbar(
                        message = localRes.getString(R.string.text_info_update_downloaded),
                        actionLabel = localRes.getString(R.string.button_text_install),
                        duration = SnackbarDuration.Indefinite,
                    )

                    if (result == SnackbarResult.ActionPerformed) {
                        onCompleteUpdate()
                    } else {
                        onClearUpdate()
                    }

                    hasPromptedDownload = true
                }
            }
            else -> {}
        }
    }
}

private fun handleShortcutIntent(
    intent: Intent?,
    navController: NavController,
    onRequestFocus: () -> Unit = {},
) {
    Timber.d("Handling shortcut intent with extras: ${intent?.extras}")

    if (intent == null) {
        Timber.d("Intent is null, returning...")
        return
    }

    with(intent.extras ?: return) {
        when {
            containsKey("shortcutSearchExtra") -> {
                intent.removeExtra("shortcutSearchExtra")
                navController.navigateToSearch()
                onRequestFocus()
            }

            containsKey("shortcutDiscoverExtra") -> {
                intent.removeExtra("shortcutDiscoverExtra")
                navController.navigateToDiscover()
            }

            containsKey("shortcutListsExtra") -> {
                intent.removeExtra("shortcutListsExtra")
                navController.navigateToLists()
            }

            containsKey("shortcutProfileExtra") -> {
                intent.removeExtra("shortcutProfileExtra")
                navController.navigateToProfile()
            }
        }
    }
}

@Suppress("IntroduceWhenSubject")
private fun handleNotificationIntent(
    intent: Intent?,
    navController: NavController,
) {
    val extras = intent?.extras
    Timber.d("Handling notification intent with extras: ${intent?.extras}")

    if (intent == null) {
        Timber.d("Intent is null, returning...")
        return
    }

    if (extras?.containsKey(INTENT_NOTIFICATION_TRIVIA_EXTRAS) == true) {
        val extrasJson = extras.getString(INTENT_NOTIFICATION_TRIVIA_EXTRAS)
        if (extrasJson.isNullOrBlank()) {
            return
        }

        val triviaExtras = Json.decodeFromString<NotificationIntentExtras>(extrasJson)
        if (triviaExtras.mediaId == -1) {
            return
        }

        navController.navigateToTrivia(
            mediaId = triviaExtras.mediaId.toTraktId(),
            mediaType = triviaExtras.mediaType,
            mediaImage = triviaExtras.mediaImage,
            mediaTitle = triviaExtras.mediaTitle,
            navSource = "check_in_notif",
        )
        return
    }

    if (extras?.containsKey(INTENT_NOTIFICATION_EXTRAS) == true) {
        val extrasJson = extras.getString(INTENT_NOTIFICATION_EXTRAS)
        if (extrasJson.isNullOrBlank()) {
            return
        }

        val extras = Json.decodeFromString<NotificationIntentExtras>(extrasJson)
        if (extras.mediaId == -1) {
            return
        }

        when {
            extras.mediaType == EPISODE -> {
                if (extras.extraId == null || extras.extraValue1 == null || extras.extraValue2 == null) {
                    return
                }
                navController.navigateToEpisode(
                    showId = extras.extraId.toTraktId(),
                    episodeId = extras.mediaId.toTraktId(),
                    episodeSeason = extras.extraValue1,
                    episodeNumber = extras.extraValue2,
                )
            }
            extras.mediaType == MOVIE -> {
                navController.navigateToMovie(
                    movieId = extras.mediaId.toTraktId(),
                )
            }
        }
    }
}
