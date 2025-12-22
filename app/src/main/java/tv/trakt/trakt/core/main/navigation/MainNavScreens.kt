package tv.trakt.trakt.core.main.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import tv.trakt.trakt.common.model.Images.Size
import tv.trakt.trakt.common.model.MediaType.MOVIE
import tv.trakt.trakt.common.model.MediaType.SHOW
import tv.trakt.trakt.core.billing.navigation.billingScreen
import tv.trakt.trakt.core.billing.navigation.navigateToBilling
import tv.trakt.trakt.core.comments.navigation.commentsScreen
import tv.trakt.trakt.core.comments.navigation.navigateToComments
import tv.trakt.trakt.core.discover.model.DiscoverSection
import tv.trakt.trakt.core.discover.navigation.discoverScreen
import tv.trakt.trakt.core.discover.navigation.navigateToDiscover
import tv.trakt.trakt.core.discover.sections.all.navigation.discoverAllScreen
import tv.trakt.trakt.core.discover.sections.all.navigation.navigateToDiscoverAll
import tv.trakt.trakt.core.home.navigation.homeScreen
import tv.trakt.trakt.core.home.navigation.navigateToHome
import tv.trakt.trakt.core.home.sections.activity.features.all.navigation.homeActivityPersonalScreen
import tv.trakt.trakt.core.home.sections.activity.features.all.navigation.homeActivitySocialScreen
import tv.trakt.trakt.core.home.sections.activity.features.all.navigation.navigateToAllActivityPersonal
import tv.trakt.trakt.core.home.sections.activity.features.all.navigation.navigateToAllActivitySocial
import tv.trakt.trakt.core.home.sections.upnext.features.all.navigation.homeUpNextScreen
import tv.trakt.trakt.core.home.sections.upnext.features.all.navigation.navigateToAllUpNext
import tv.trakt.trakt.core.home.sections.watchlist.features.all.navigation.homeWatchlistScreen
import tv.trakt.trakt.core.home.sections.watchlist.features.all.navigation.navigateToAllHomeWatchlist
import tv.trakt.trakt.core.lists.features.details.navigation.listDetailsScreen
import tv.trakt.trakt.core.lists.features.details.navigation.navigateToListDetails
import tv.trakt.trakt.core.lists.navigation.listsScreen
import tv.trakt.trakt.core.lists.sections.personal.features.all.navigation.allPersonalListScreen
import tv.trakt.trakt.core.lists.sections.personal.features.all.navigation.navigateToPersonalList
import tv.trakt.trakt.core.lists.sections.watchlist.features.all.navigation.allWatchlistScreen
import tv.trakt.trakt.core.lists.sections.watchlist.features.all.navigation.navigateToWatchlist
import tv.trakt.trakt.core.profile.navigation.navigateToProfile
import tv.trakt.trakt.core.profile.navigation.profileScreen
import tv.trakt.trakt.core.profile.sections.favorites.all.navigation.allFavoritesScreen
import tv.trakt.trakt.core.profile.sections.favorites.all.navigation.navigateToFavorites
import tv.trakt.trakt.core.profile.sections.library.all.navigation.allLibraryScreen
import tv.trakt.trakt.core.profile.sections.library.all.navigation.navigateToLibrary
import tv.trakt.trakt.core.search.model.SearchInput
import tv.trakt.trakt.core.search.navigation.searchScreen
import tv.trakt.trakt.core.settings.navigation.navigateToSettings
import tv.trakt.trakt.core.settings.navigation.settingsScreen
import tv.trakt.trakt.core.summary.episodes.navigation.episodeDetailsScreen
import tv.trakt.trakt.core.summary.episodes.navigation.navigateToEpisode
import tv.trakt.trakt.core.summary.movies.navigation.movieDetailsScreen
import tv.trakt.trakt.core.summary.movies.navigation.navigateToMovie
import tv.trakt.trakt.core.summary.people.navigation.navigateToPerson
import tv.trakt.trakt.core.summary.people.navigation.personDetailsScreen
import tv.trakt.trakt.core.summary.shows.navigation.navigateToShow
import tv.trakt.trakt.core.summary.shows.navigation.showDetailsScreen
import tv.trakt.trakt.core.younify.features.younify.navigation.navigateToYounify
import tv.trakt.trakt.core.younify.features.younify.navigation.younifyScreen

internal fun NavGraphBuilder.homeScreens(
    controller: NavHostController,
    userLoading: Boolean,
) {
    with(controller) {
        homeScreen(
            userLoading = userLoading,
            onNavigateToShow = { navigateToShow(it) },
            onNavigateToDiscover = { navigateToDiscover() },
            onNavigateToMovie = { navigateToMovie(it) },
            onNavigateToEpisode = { showId, episode ->
                navigateToEpisode(showId, episode)
            },
            onNavigateToAllUpNext = { navigateToAllUpNext() },
            onNavigateToAllPersonal = { navigateToAllActivityPersonal(filtersEnabled = true) },
            onNavigateToAllSocial = { navigateToAllActivitySocial() },
            onNavigateToAllWatchlist = { navigateToAllHomeWatchlist() },
            onNavigateToVip = { navigateToBilling() },
        )

        homeUpNextScreen(
            onNavigateToShow = { navigateToShow(it) },
            onNavigateToEpisode = { showId, episode ->
                navigateToEpisode(showId, episode)
            },
            onNavigateBack = { popBackStack() },
        )

        homeWatchlistScreen(
            onNavigateToShow = { navigateToShow(it) },
            onNavigateToMovie = { navigateToMovie(it) },
            onNavigateBack = { popBackStack() },
        )

        homeActivityPersonalScreen(
            onNavigateToShow = { navigateToShow(it) },
            onNavigateToEpisode = { showId, episode ->
                navigateToEpisode(showId, episode)
            },
            onNavigateToMovie = { navigateToMovie(it) },
            onNavigateBack = { popBackStack() },
        )

        homeActivitySocialScreen(
            onNavigateToShow = { navigateToShow(it) },
            onNavigateToEpisode = { showId, episode ->
                navigateToEpisode(showId, episode)
            },
            onNavigateToMovie = { navigateToMovie(it) },
            onNavigateBack = { popBackStack() },
        )
    }
}

internal fun NavGraphBuilder.discoverScreens(
    controller: NavHostController,
    customThemeEnabled: Boolean,
) {
    with(controller) {
        discoverScreen(
            onNavigateToShow = { navigateToShow(it) },
            onNavigateToMovie = { navigateToMovie(it) },
            onNavigateToAllTrending = {
                navigateToDiscoverAll(DiscoverSection.TRENDING)
            },
            onNavigateToAllPopular = {
                navigateToDiscoverAll(DiscoverSection.POPULAR)
            },
            onNavigateToAllAnticipated = {
                navigateToDiscoverAll(DiscoverSection.ANTICIPATED)
            },
            onNavigateToAllRecommended = {
                navigateToDiscoverAll(DiscoverSection.RECOMMENDED)
            },
            onNavigateToVip = {
                navigateToBilling()
            },
        )
        discoverAllScreen(
            customThemeEnabled = customThemeEnabled,
            onNavigateBack = { popBackStack() },
            onNavigateToShow = { navigateToShow(it) },
            onNavigateToMovie = { navigateToMovie(it) },
        )
    }
}

internal fun NavGraphBuilder.showsScreens(controller: NavHostController) {
    with(controller) {
        showDetailsScreen(
            onNavigateToShow = { navigateToShow(it) },
            onNavigateToComments = { show, filter ->
                navigateToComments(
                    mediaId = show.ids.trakt,
                    mediaType = SHOW,
                    mediaImage = show.images?.getFanartUrl(),
                    filter = filter,
                )
            },
            onNavigateToList = { show, list ->
                navigateToListDetails(
                    listId = list.ids.trakt.value,
                    listTitle = list.name,
                    listDescription = list.description,
                    mediaId = show.ids.trakt,
                    mediaType = SHOW,
                    mediaImage = show.images?.getFanartUrl(),
                )
            },
            onNavigateToEpisode = { showId, episode ->
                navigateToEpisode(showId, episode)
            },
            onNavigateToPerson = { show, person ->
                navigateToPerson(
                    personId = person.ids.trakt,
                    sourceMediaId = show.ids.trakt,
                    backdropUrl = show.images?.getFanartUrl(Size.THUMB),
                )
            },
            onNavigateBack = { popBackStack() },
        )
    }
}

internal fun NavGraphBuilder.episodesScreens(controller: NavHostController) {
    with(controller) {
        episodeDetailsScreen(
            onShowClick = {
                navigateToShow(it.ids.trakt)
            },
            onEpisodeCLick = { showId, episode ->
                navigateToEpisode(showId, episode)
            },
            onCommentsClick = { show, episode, filter ->
                navigateToComments(
                    episodeId = episode.ids.trakt,
                    showId = show.ids.trakt,
                    showImage = show.images?.getFanartUrl(),
                    seasonEpisode = episode.seasonEpisode,
                    filter = filter,
                )
            },
            onPersonClick = { show, _, person ->
                navigateToPerson(
                    personId = person.ids.trakt,
                    sourceMediaId = show.ids.trakt,
                    backdropUrl = show.images?.getFanartUrl(Size.THUMB),
                )
            },
            onNavigateBack = { popBackStack() },
        )
    }
}

internal fun NavGraphBuilder.moviesScreens(controller: NavHostController) {
    with(controller) {
        movieDetailsScreen(
            onNavigateToMovie = { navigateToMovie(it) },
            onNavigateToComments = { movie, filter ->
                navigateToComments(
                    mediaId = movie.ids.trakt,
                    mediaType = MOVIE,
                    mediaImage = movie.images?.getFanartUrl(),
                    filter = filter,
                )
            },
            onNavigateToList = { movie, list ->
                navigateToListDetails(
                    listId = list.ids.trakt.value,
                    listTitle = list.name,
                    listDescription = list.description,
                    mediaId = movie.ids.trakt,
                    mediaType = MOVIE,
                    mediaImage = movie.images?.getFanartUrl(),
                )
            },
            onNavigateToPerson = { movie, person ->
                navigateToPerson(
                    personId = person.ids.trakt,
                    sourceMediaId = movie.ids.trakt,
                    backdropUrl = movie.images?.getFanartUrl(Size.THUMB),
                )
            },
            onNavigateBack = { popBackStack() },
        )
    }
}

internal fun NavGraphBuilder.listsScreens(controller: NavHostController) {
    with(controller) {
        listsScreen(
            onNavigateToProfile = { navigateToProfile() },
            onNavigateToShow = { navigateToShow(it) },
            onNavigateToDiscover = { navigateToDiscover() },
            onNavigateToMovie = { navigateToMovie(it) },
            onNavigateToWatchlist = { navigateToWatchlist() },
            onNavigateToList = {
                navigateToPersonalList(
                    listId = it.ids.trakt.value,
                    listTitle = it.name,
                    listDescription = it.description,
                )
            },
        )
        allWatchlistScreen(
            onNavigateToShow = { navigateToShow(it) },
            onNavigateToMovie = { navigateToMovie(it) },
            onNavigateBack = { popBackStack() },
        )
        allPersonalListScreen(
            onNavigateToShow = { navigateToShow(it) },
            onNavigateToMovie = { navigateToMovie(it) },
            onNavigateBack = { popBackStack() },
        )
        listDetailsScreen(
            onNavigateBack = { popBackStack() },
            onNavigateToShow = { navigateToShow(it) },
            onNavigateToMovie = { navigateToMovie(it) },
        )
    }
}

internal fun NavGraphBuilder.searchScreens(
    controller: NavHostController,
    searchInput: SearchInput,
    onSearchLoading: (Boolean) -> Unit,
) {
    with(controller) {
        searchScreen(
            searchInput = searchInput,
            onSearchLoading = onSearchLoading,
            onNavigateToShow = { navigateToShow(it) },
            onNavigateToMovie = { navigateToMovie(it) },
            onNavigateToPerson = { navigateToPerson(it, null, null) },
        )
    }
}

internal fun NavGraphBuilder.profileScreens(controller: NavHostController) {
    with(controller) {
        profileScreen(
            onNavigateToShow = { navigateToShow(it) },
            onNavigateToMovie = { navigateToMovie(it) },
            onNavigateToEpisode = { showId, episode ->
                navigateToEpisode(showId, episode)
            },
            onNavigateToHistory = {
                navigateToAllActivityPersonal(filtersEnabled = false)
            },
            onNavigateToFavorites = { navigateToFavorites() },
            onNavigateToLibrary = { navigateToLibrary() },
            onNavigateToDiscover = { navigateToDiscover() },
            onNavigateToSettings = { navigateToSettings() },
            onNavigateToHome = { navigateToHome() },
        )

        allFavoritesScreen(
            onNavigateToShow = { navigateToShow(it) },
            onNavigateToMovie = { navigateToMovie(it) },
            onNavigateBack = { popBackStack() },
        )

        allLibraryScreen(
            onNavigateToShow = { navigateToShow(it) },
            onNavigateToMovie = { navigateToMovie(it) },
            onNavigateBack = { popBackStack() },
        )
    }
}

internal fun NavGraphBuilder.peopleScreens(controller: NavHostController) {
    with(controller) {
        personDetailsScreen(
            onNavigateToShow = { navigateToShow(it) },
            onNavigateToMovie = { navigateToMovie(it) },
            onNavigateBack = { popBackStack() },
        )
    }
}

internal fun NavGraphBuilder.commentsScreens(controller: NavHostController) {
    with(controller) {
        commentsScreen(
            onNavigateBack = { popBackStack() },
        )
    }
}

internal fun NavGraphBuilder.settingsScreens(controller: NavHostController) {
    with(controller) {
        settingsScreen(
            onNavigateHome = { navigateToHome() },
            onNavigateYounify = { navigateToYounify() },
            onNavigateBack = { popBackStack() },
        )

        younifyScreen(
            onNavigateBack = { popBackStack() },
        )
    }
}

internal fun NavGraphBuilder.billingScreens(controller: NavHostController) {
    with(controller) {
        billingScreen(
            onNavigateBack = { popBackStack() },
        )
    }
}
