package tv.trakt.trakt.core.main

import android.content.Intent
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalResources
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import timber.log.Timber
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.model.MediaType.Episode
import tv.trakt.trakt.common.model.MediaType.Movie
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.applinks.AppLinkEvent
import tv.trakt.trakt.core.applinks.model.AppLink
import tv.trakt.trakt.core.applinks.model.parseAppLink
import tv.trakt.trakt.core.calendar.navigation.navigateToCalendar
import tv.trakt.trakt.core.discover.navigation.navigateToDiscover
import tv.trakt.trakt.core.home.sections.upnext.features.all.navigation.navigateToAllUpNext
import tv.trakt.trakt.core.lists.navigation.navigateToLists
import tv.trakt.trakt.core.notifications.data.work.INTENT_NOTIFICATION_EXTRAS
import tv.trakt.trakt.core.notifications.data.work.INTENT_NOTIFICATION_TRIVIA_EXTRAS
import tv.trakt.trakt.core.notifications.model.NotificationIntentExtras
import tv.trakt.trakt.core.profile.navigation.navigateToProfile
import tv.trakt.trakt.core.search.navigation.SearchDestination
import tv.trakt.trakt.core.search.navigation.navigateToSearch
import tv.trakt.trakt.core.summary.episodes.navigation.navigateToEpisode
import tv.trakt.trakt.core.summary.movies.navigation.navigateToMovie
import tv.trakt.trakt.core.summary.people.navigation.navigateToPerson
import tv.trakt.trakt.core.summary.shows.navigation.navigateToShow
import tv.trakt.trakt.core.trivia.navigation.navigateToTrivia
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.widgets.INTENT_WIDGET_TARGET_EXTRA
import tv.trakt.trakt.widgets.WidgetIntentTarget

@Composable
internal fun LaunchedIntentHandler(
    intent: Intent?,
    newIntent: Intent?,
    navController: NavController,
    currentDestination: NavBackStackEntry?,
    searchState: MainSearchStateHolder,
    onAppLink: (AppLink) -> Unit,
) {
    var pendingSearchQuery by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(intent, newIntent) {
        val currentIntent = newIntent ?: intent

        val appLink = extractAppLink(currentIntent)
        if (appLink != null) {
            onAppLink(appLink)
            return@LaunchedEffect
        }

        val processTextQuery = extractProcessTextQuery(currentIntent)
        if (processTextQuery != null) {
            // Defer applying the query until the search destination is active,
            // so rememberSearchState's non-search reset can't clobber it.
            pendingSearchQuery = processTextQuery
            navController.navigateToSearch()
            return@LaunchedEffect
        }

        handleShortcutIntent(
            intent = intent,
            navController = navController,
            onRequestFocus = searchState.onRequestFocus,
        )

        handleNotificationIntent(
            intent = currentIntent,
            navController = navController,
        )

        handleWidgetIntent(
            intent = currentIntent,
            navController = navController,
        )
    }

    LaunchedEffect(currentDestination, pendingSearchQuery) {
        val query = pendingSearchQuery ?: return@LaunchedEffect
        val onSearch = currentDestination
            ?.destination
            ?.hasRoute(SearchDestination::class) == true

        if (onSearch) {
            searchState.onSearchQuery(query)
            searchState.onRequestFocus()
            pendingSearchQuery = null
        }
    }
}

@Composable
internal fun LaunchedAppLinkEvents(
    events: Flow<AppLinkEvent>,
    welcomeActive: Boolean,
    navController: NavController,
) {
    val localRes = LocalResources.current
    val localSnackbar = LocalSnackbarState.current
    val isWelcomeActive by rememberUpdatedState(welcomeActive)

    LaunchedEffect(Unit) {
        events.collect { event ->
            // Links can arrive on a cold start while the welcome screen still covers the NavHost.
            snapshotFlow { isWelcomeActive }.first { !it }

            when (event) {
                is AppLinkEvent.OpenShow -> navController.navigateToShow(event.showId)
                is AppLinkEvent.OpenMovie -> navController.navigateToMovie(event.movieId)
                is AppLinkEvent.OpenEpisode -> navController.navigateToEpisode(
                    showId = event.showId,
                    episodeId = event.episodeId,
                    episodeSeason = event.season,
                    episodeNumber = event.number,
                )
                is AppLinkEvent.OpenPerson -> navController.navigateToPerson(
                    personId = event.personId,
                    sourceMediaId = null,
                    backdropUrl = null,
                )
                AppLinkEvent.NotFound -> localSnackbar.showSnackbar(
                    message = localRes.getString(R.string.error_text_imdb_link_not_found),
                    duration = SnackbarDuration.Short,
                )
                AppLinkEvent.Error -> localSnackbar.showSnackbar(
                    message = localRes.getString(R.string.error_text_unexpected_error_short),
                )
            }
        }
    }
}

private fun extractAppLink(intent: Intent?): AppLink? {
    if (intent == null || intent.action != Intent.ACTION_VIEW) {
        return null
    }

    val appLink = intent.data?.let(::parseAppLink) ?: return null
    intent.data = null

    return appLink
}

private fun extractProcessTextQuery(intent: Intent?): String? {
    if (intent == null || intent.action != Intent.ACTION_PROCESS_TEXT) {
        return null
    }

    val text = intent
        .getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)
        ?.toString()
        ?.trim()

    if (text.isNullOrBlank()) {
        return null
    }

    // Clear so recomposition / config change does not re-trigger the search.
    intent.removeExtra(Intent.EXTRA_PROCESS_TEXT)
    intent.action = null

    return text
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

private fun handleWidgetIntent(
    intent: Intent?,
    navController: NavController,
) {
    if (intent == null) {
        return
    }

    val targetJson = intent.getStringExtra(INTENT_WIDGET_TARGET_EXTRA)
    if (targetJson.isNullOrBlank()) {
        return
    }

    intent.removeExtra(INTENT_WIDGET_TARGET_EXTRA)

    val target = runCatching {
        Json.decodeFromString<WidgetIntentTarget>(targetJson)
    }.getOrElse { error ->
        Timber.recordError(error)
        return
    }

    when (target) {
        is WidgetIntentTarget.Show -> navController.navigateToShow(
            showId = target.showId.toTraktId(),
        )

        is WidgetIntentTarget.Episode -> navController.navigateToEpisode(
            showId = target.showId.toTraktId(),
            episodeId = target.episodeId.toTraktId(),
            episodeSeason = target.season,
            episodeNumber = target.number,
        )

        is WidgetIntentTarget.Movie -> navController.navigateToMovie(
            movieId = target.movieId.toTraktId(),
        )

        is WidgetIntentTarget.Calendar -> navController.navigateToCalendar()

        is WidgetIntentTarget.UpNext -> navController.navigateToAllUpNext()
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
            extras.mediaType == Episode -> {
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
            extras.mediaType == Movie -> {
                navController.navigateToMovie(
                    movieId = extras.mediaId.toTraktId(),
                )
            }
        }
    }
}
