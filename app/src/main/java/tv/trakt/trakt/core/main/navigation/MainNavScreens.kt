package tv.trakt.trakt.core.main.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import tv.trakt.trakt.core.home.navigation.homeScreen
import tv.trakt.trakt.core.lists.navigation.listsScreen
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
import tv.trakt.trakt.core.profile.navigation.navigateToProfile
import tv.trakt.trakt.core.profile.navigation.profileScreen
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

internal fun NavGraphBuilder.homeScreens(controller: NavHostController) {
    with(controller) {
        homeScreen(
            onNavigateToProfile = { navigateToProfile() },
            onNavigateToShows = { navigateToShows() },
            onNavigateToMovies = { navigateToMovies() },
        )
    }
}

internal fun NavGraphBuilder.showsScreens(controller: NavHostController) {
    with(controller) {
        showsScreen(
            onNavigateToProfile = { navigateToProfile() },
            onNavigateToShow = {},
            onNavigateToAllTrending = { navigateToTrendingShows() },
            onNavigateToAllPopular = { navigateToPopularShows() },
            onNavigateToAllAnticipated = { navigateToAnticipatedShows() },
            onNavigateToAllRecommended = { navigateToRecommendedShows() },
        )
        showsTrendingScreen(
            onNavigateBack = { popBackStack() },
        )
        showsPopularScreen(
            onNavigateBack = { popBackStack() },
        )
        showsAnticipatedScreen(
            onNavigateBack = { popBackStack() },
        )
        showsRecommendedScreen(
            onNavigateBack = { popBackStack() },
        )
    }
}

internal fun NavGraphBuilder.moviesScreens(controller: NavHostController) {
    with(controller) {
        moviesScreen(
            onNavigateToProfile = { navigateToProfile() },
            onNavigateToMovie = {},
            onNavigateToAllTrending = { navigateToTrendingMovies() },
            onNavigateToAllPopular = { navigateToPopularMovies() },
            onNavigateToAllAnticipated = { navigateToAnticipatedMovies() },
            onNavigateToAllRecommended = { navigateToRecommendedMovies() },
        )
        moviesTrendingScreen(
            onNavigateBack = { popBackStack() },
        )
        moviesPopularScreen(
            onNavigateBack = { popBackStack() },
        )
        moviesAnticipatedScreen(
            onNavigateBack = { popBackStack() },
        )
        moviesRecommendedScreen(
            onNavigateBack = { popBackStack() },
        )
    }
}

internal fun NavGraphBuilder.listsScreens(controller: NavHostController) {
    with(controller) {
        listsScreen(
            onNavigateToProfile = { navigateToProfile() },
            onNavigateToShows = { navigateToShows() },
            onNavigateToMovies = { navigateToMovies() },
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
            onNavigateToShow = { },
            onNavigateToMovie = { },
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
