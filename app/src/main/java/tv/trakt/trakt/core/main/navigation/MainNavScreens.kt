package tv.trakt.trakt.core.main.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import tv.trakt.trakt.common.model.Images.Size
import tv.trakt.trakt.common.model.MediaType.MOVIE
import tv.trakt.trakt.common.model.MediaType.SHOW
import tv.trakt.trakt.core.comments.navigation.commentsScreen
import tv.trakt.trakt.core.comments.navigation.navigateToComments
import tv.trakt.trakt.core.home.navigation.homeScreen
import tv.trakt.trakt.core.home.sections.activity.all.navigation.homeActivityPersonalScreen
import tv.trakt.trakt.core.home.sections.activity.all.navigation.homeActivitySocialScreen
import tv.trakt.trakt.core.home.sections.activity.all.navigation.navigateToAllActivityPersonal
import tv.trakt.trakt.core.home.sections.activity.all.navigation.navigateToAllActivitySocial
import tv.trakt.trakt.core.home.sections.upnext.features.all.navigation.homeUpNextScreen
import tv.trakt.trakt.core.home.sections.upnext.features.all.navigation.navigateToAllUpNext
import tv.trakt.trakt.core.lists.features.details.navigation.listDetailsScreen
import tv.trakt.trakt.core.lists.features.details.navigation.navigateToListDetails
import tv.trakt.trakt.core.lists.navigation.listsScreen
import tv.trakt.trakt.core.lists.sections.personal.features.all.navigation.allPersonalListScreen
import tv.trakt.trakt.core.lists.sections.personal.features.all.navigation.navigateToPersonalList
import tv.trakt.trakt.core.lists.sections.watchlist.features.all.navigation.allWatchlistScreen
import tv.trakt.trakt.core.lists.sections.watchlist.features.all.navigation.navigateToHomeWatchlist
import tv.trakt.trakt.core.lists.sections.watchlist.features.all.navigation.navigateToWatchlist
import tv.trakt.trakt.core.movies.navigation.moviesScreen
import tv.trakt.trakt.core.movies.navigation.navigateToMovies
import tv.trakt.trakt.core.movies.sections.anticipated.all.navigation.moviesAnticipatedScreen
import tv.trakt.trakt.core.movies.sections.anticipated.all.navigation.navigateToAnticipatedMovies
import tv.trakt.trakt.core.movies.sections.popular.all.navigation.moviesPopularScreen
import tv.trakt.trakt.core.movies.sections.popular.all.navigation.navigateToPopularMovies
import tv.trakt.trakt.core.movies.sections.recommended.all.navigation.moviesRecommendedScreen
import tv.trakt.trakt.core.movies.sections.recommended.all.navigation.navigateToRecommendedMovies
import tv.trakt.trakt.core.movies.sections.trending.all.navigation.moviesTrendingScreen
import tv.trakt.trakt.core.movies.sections.trending.all.navigation.navigateToTrendingMovies
import tv.trakt.trakt.core.search.model.SearchInput
import tv.trakt.trakt.core.search.navigation.searchScreen
import tv.trakt.trakt.core.shows.navigation.navigateToShows
import tv.trakt.trakt.core.shows.navigation.showsScreen
import tv.trakt.trakt.core.shows.sections.anticipated.all.navigation.navigateToAnticipatedShows
import tv.trakt.trakt.core.shows.sections.anticipated.all.navigation.showsAnticipatedScreen
import tv.trakt.trakt.core.shows.sections.popular.all.navigation.navigateToPopularShows
import tv.trakt.trakt.core.shows.sections.popular.all.navigation.showsPopularScreen
import tv.trakt.trakt.core.shows.sections.recommended.all.navigation.navigateToRecommendedShows
import tv.trakt.trakt.core.shows.sections.recommended.all.navigation.showsRecommendedScreen
import tv.trakt.trakt.core.shows.sections.trending.all.navigation.navigateToTrendingShows
import tv.trakt.trakt.core.shows.sections.trending.all.navigation.showsTrendingScreen
import tv.trakt.trakt.core.summary.episodes.navigation.episodeDetailsScreen
import tv.trakt.trakt.core.summary.episodes.navigation.navigateToEpisode
import tv.trakt.trakt.core.summary.movies.navigation.movieDetailsScreen
import tv.trakt.trakt.core.summary.movies.navigation.navigateToMovie
import tv.trakt.trakt.core.summary.people.navigation.navigateToPerson
import tv.trakt.trakt.core.summary.people.navigation.personDetailsScreen
import tv.trakt.trakt.core.summary.shows.navigation.navigateToShow
import tv.trakt.trakt.core.summary.shows.navigation.showDetailsScreen
import tv.trakt.trakt.core.user.navigation.navigateToProfile
import tv.trakt.trakt.core.user.navigation.profileScreen

internal fun NavGraphBuilder.homeScreens(controller: NavHostController) {
    with(controller) {
        homeScreen(
            onNavigateToProfile = { navigateToProfile() },
            onNavigateToShow = { navigateToShow(it) },
            onNavigateToShows = { navigateToShows() },
            onNavigateToMovie = { navigateToMovie(it) },
            onNavigateToMovies = { navigateToMovies() },
            onNavigateToEpisode = { showId, episode ->
                navigateToEpisode(showId, episode)
            },
            onNavigateToAllUpNext = { navigateToAllUpNext() },
            onNavigateToAllPersonal = { navigateToAllActivityPersonal() },
            onNavigateToAllSocial = { navigateToAllActivitySocial() },
            onNavigateToAllWatchlist = { navigateToHomeWatchlist() },
        )

        homeUpNextScreen(
            onNavigateToShow = { navigateToShow(it) },
            onNavigateToEpisode = { showId, episode ->
                navigateToEpisode(showId, episode)
            },
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

internal fun NavGraphBuilder.showsScreens(controller: NavHostController) {
    with(controller) {
        showsScreen(
            onNavigateToProfile = { navigateToProfile() },
            onNavigateToShow = { navigateToShow(it) },
            onNavigateToAllTrending = { navigateToTrendingShows() },
            onNavigateToAllPopular = { navigateToPopularShows() },
            onNavigateToAllAnticipated = { navigateToAnticipatedShows() },
            onNavigateToAllRecommended = { navigateToRecommendedShows() },
        )
        showsTrendingScreen(
            onNavigateBack = { popBackStack() },
            onNavigateToShow = { navigateToShow(it) },
        )
        showsPopularScreen(
            onNavigateBack = { popBackStack() },
            onNavigateToShow = { navigateToShow(it) },
        )
        showsAnticipatedScreen(
            onNavigateBack = { popBackStack() },
            onNavigateToShow = { navigateToShow(it) },
        )
        showsRecommendedScreen(
            onNavigateBack = { popBackStack() },
            onNavigateToShow = { navigateToShow(it) },
        )
        showDetailsScreen(
            onNavigateToShow = { navigateToShow(it) },
            onNavigateToComments = {
                navigateToComments(
                    mediaId = it.ids.trakt,
                    mediaType = SHOW,
                    mediaImage = it.images?.getFanartUrl(),
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
            onCommentsClick = { show, episode ->
                navigateToComments(
                    showId = show.ids.trakt,
                    showImage = show.images?.getFanartUrl(),
                    seasonEpisode = episode.seasonEpisode,
                )
            },
            onNavigateBack = { popBackStack() },
        )
    }
}

internal fun NavGraphBuilder.moviesScreens(controller: NavHostController) {
    with(controller) {
        moviesScreen(
            onNavigateToProfile = { navigateToProfile() },
            onNavigateToMovie = { navigateToMovie(it) },
            onNavigateToAllTrending = { navigateToTrendingMovies() },
            onNavigateToAllPopular = { navigateToPopularMovies() },
            onNavigateToAllAnticipated = { navigateToAnticipatedMovies() },
            onNavigateToAllRecommended = { navigateToRecommendedMovies() },
        )
        moviesTrendingScreen(
            onNavigateBack = { popBackStack() },
            onNavigateToMovie = { navigateToMovie(it) },
        )
        moviesPopularScreen(
            onNavigateBack = { popBackStack() },
            onNavigateToMovie = { navigateToMovie(it) },
        )
        moviesAnticipatedScreen(
            onNavigateBack = { popBackStack() },
            onNavigateToMovie = { navigateToMovie(it) },
        )
        moviesRecommendedScreen(
            onNavigateBack = { popBackStack() },
            onNavigateToMovie = { navigateToMovie(it) },
        )
        movieDetailsScreen(
            onNavigateToMovie = { navigateToMovie(it) },
            onNavigateToComments = {
                navigateToComments(
                    mediaId = it.ids.trakt,
                    mediaType = MOVIE,
                    mediaImage = it.images?.getFanartUrl(),
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
            onNavigateBack = { popBackStack() },
        )
    }
}

internal fun NavGraphBuilder.listsScreens(controller: NavHostController) {
    with(controller) {
        listsScreen(
            onNavigateToProfile = { navigateToProfile() },
            onNavigateToShow = { navigateToShow(it) },
            onNavigateToShows = { navigateToShows() },
            onNavigateToMovie = { navigateToMovie(it) },
            onNavigateToMovies = { navigateToMovies() },
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
            onNavigateToProfile = { navigateToProfile() },
        )
    }
}

internal fun NavGraphBuilder.profileScreens(controller: NavHostController) {
    with(controller) {
        profileScreen(
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
