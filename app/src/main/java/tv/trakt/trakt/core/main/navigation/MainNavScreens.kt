package tv.trakt.trakt.core.main.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import tv.trakt.trakt.common.model.Images.Size
import tv.trakt.trakt.common.model.MediaType.Movie
import tv.trakt.trakt.common.model.MediaType.Show
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.billing.navigation.billingScreen
import tv.trakt.trakt.core.billing.navigation.navigateToBilling
import tv.trakt.trakt.core.calendar.navigation.calendarScreen
import tv.trakt.trakt.core.calendar.navigation.navigateToCalendar
import tv.trakt.trakt.core.comments.navigation.commentsScreen
import tv.trakt.trakt.core.comments.navigation.navigateToComments
import tv.trakt.trakt.core.discover.model.DiscoverSection
import tv.trakt.trakt.core.discover.navigation.discoverScreen
import tv.trakt.trakt.core.discover.navigation.navigateToDiscover
import tv.trakt.trakt.core.discover.sections.all.navigation.discoverAllScreen
import tv.trakt.trakt.core.discover.sections.all.navigation.navigateToDiscoverAll
import tv.trakt.trakt.core.discover.sections.releases.all.navigation.allReleasesScreen
import tv.trakt.trakt.core.discover.sections.releases.all.navigation.navigateToAllReleases
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
import tv.trakt.trakt.core.lists.features.all.navigation.allListsScreen
import tv.trakt.trakt.core.lists.features.all.navigation.navigateToAllLists
import tv.trakt.trakt.core.lists.features.details.navigation.listDetailsScreen
import tv.trakt.trakt.core.lists.features.details.navigation.navigateToListDetails
import tv.trakt.trakt.core.lists.features.reorder.navigation.listReorderScreen
import tv.trakt.trakt.core.lists.features.reorder.navigation.navigateToListReorder
import tv.trakt.trakt.core.lists.navigation.listsScreen
import tv.trakt.trakt.core.lists.sections.personal.features.all.navigation.allPersonalListScreen
import tv.trakt.trakt.core.lists.sections.personal.features.all.navigation.navigateToPersonalList
import tv.trakt.trakt.core.lists.sections.watchlist.features.all.navigation.allWatchlistScreen
import tv.trakt.trakt.core.lists.sections.watchlist.features.all.navigation.navigateToWatchlist
import tv.trakt.trakt.core.profile.navigation.navigateToProfile
import tv.trakt.trakt.core.profile.navigation.profileScreen
import tv.trakt.trakt.core.profile.sections.activity.all.navigation.navigateToProfileActivity
import tv.trakt.trakt.core.profile.sections.activity.all.navigation.profileAllActivityScreen
import tv.trakt.trakt.core.profile.sections.favorites.all.navigation.allFavoritesScreen
import tv.trakt.trakt.core.profile.sections.favorites.all.navigation.navigateToFavorites
import tv.trakt.trakt.core.profile.sections.library.all.navigation.allLibraryScreen
import tv.trakt.trakt.core.profile.sections.library.all.navigation.navigateToLibrary
import tv.trakt.trakt.core.profile.sections.progress.all.navigation.allProgressScreen
import tv.trakt.trakt.core.profile.sections.progress.all.navigation.navigateToProgress
import tv.trakt.trakt.core.profile.sections.social.all.navigation.allProfileSocialScreen
import tv.trakt.trakt.core.profile.sections.social.all.navigation.navigateToProfileSocial
import tv.trakt.trakt.core.search.model.SearchInput
import tv.trakt.trakt.core.search.navigation.navigateToSearch
import tv.trakt.trakt.core.search.navigation.searchScreen
import tv.trakt.trakt.core.settings.features.younify.navigation.navigateToYounify
import tv.trakt.trakt.core.settings.features.younify.navigation.younifyScreen
import tv.trakt.trakt.core.settings.navigation.navigateToBlockedUsers
import tv.trakt.trakt.core.settings.navigation.navigateToSettings
import tv.trakt.trakt.core.settings.navigation.settingsScreen
import tv.trakt.trakt.core.summary.episodes.features.history.navigation.episodeHistoryScreen
import tv.trakt.trakt.core.summary.episodes.features.history.navigation.navigateToEpisodeHistory
import tv.trakt.trakt.core.summary.episodes.navigation.episodeDetailsScreen
import tv.trakt.trakt.core.summary.episodes.navigation.navigateToEpisode
import tv.trakt.trakt.core.summary.movies.features.history.navigation.movieHistoryScreen
import tv.trakt.trakt.core.summary.movies.features.history.navigation.navigateToMovieHistory
import tv.trakt.trakt.core.summary.movies.navigation.movieDetailsScreen
import tv.trakt.trakt.core.summary.movies.navigation.navigateToMovie
import tv.trakt.trakt.core.summary.people.navigation.navigateToPerson
import tv.trakt.trakt.core.summary.people.navigation.personDetailsScreen
import tv.trakt.trakt.core.summary.sentiment.navigation.navigateToSentiment
import tv.trakt.trakt.core.summary.sentiment.navigation.sentimentScreen
import tv.trakt.trakt.core.summary.shows.features.history.navigation.navigateToShowHistory
import tv.trakt.trakt.core.summary.shows.features.history.navigation.showHistoryScreen
import tv.trakt.trakt.core.summary.shows.features.seasons.all.navigation.allShowSeasonsScreen
import tv.trakt.trakt.core.summary.shows.features.seasons.all.navigation.navigateToAllShowSeasons
import tv.trakt.trakt.core.summary.shows.navigation.navigateToShow
import tv.trakt.trakt.core.summary.shows.navigation.showDetailsScreen
import tv.trakt.trakt.core.trivia.navigation.navigateToTrivia
import tv.trakt.trakt.core.trivia.navigation.triviaScreen
import tv.trakt.trakt.core.userprofile.navigation.navigateToUserProfile
import tv.trakt.trakt.core.userprofile.navigation.userProfileScreen
import tv.trakt.trakt.core.userprofile.sections.favorites.all.allUserProfileFavoritesScreen
import tv.trakt.trakt.core.userprofile.sections.favorites.all.navigateToAllUserProfileFavorites
import tv.trakt.trakt.core.userprofile.sections.history.all.allUserProfileHistoryScreen
import tv.trakt.trakt.core.userprofile.sections.history.all.navigateToAllUserProfileHistory
import tv.trakt.trakt.core.userprofile.sections.lists.all.allUserProfileListsScreen
import tv.trakt.trakt.core.userprofile.sections.lists.all.navigateToAllUserProfileLists
import tv.trakt.trakt.helpers.player.navigation.navigateToYouTubePlayer
import tv.trakt.trakt.helpers.player.navigation.youTubePlayerScreen

internal fun NavGraphBuilder.homeScreens(
    controller: NavHostController,
    userLoading: Boolean,
) {
    with(controller) {
        homeScreen(
            userLoading = userLoading,
            onNavigateToShow = { navigateToShow(it) },
            onNavigateToDiscover = { navigateToDiscover() },
            onNavigateToCalendar = { navigateToCalendar() },
            onNavigateToMovie = { navigateToMovie(it) },
            onNavigateToEpisode = { showId, episode ->
                navigateToEpisode(showId, episode)
            },
            onNavigateToAllUpNext = { navigateToAllUpNext() },
            onNavigateToAllPersonal = { navigateToAllActivityPersonal(filtersEnabled = true) },
            onNavigateToAllSocial = { navigateToAllActivitySocial() },
            onNavigateToAllWatchlist = { navigateToAllHomeWatchlist() },
            onNavigateToAllRecommended = {
                navigateToDiscoverAll(DiscoverSection.RECOMMENDED)
            },
            onNavigateToVip = { navigateToBilling() },
            onNavigateToUser = { navigateToUserProfile(it) },
        )

        homeUpNextScreen(
            onNavigateToShow = { navigateToShow(it) },
            onNavigateToMovie = { navigateToMovie(it) },
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
            onNavigateToUser = { navigateToUserProfile(it) },
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
            onNavigateToEpisode = { showId, episode ->
                navigateToEpisode(showId, episode)
            },
            onNavigateToAllTrending = {
                navigateToDiscoverAll(DiscoverSection.TRENDING)
            },
            onNavigateToAllPopular = {
                navigateToDiscoverAll(DiscoverSection.POPULAR)
            },
            onNavigateToAllAnticipated = {
                navigateToDiscoverAll(DiscoverSection.ANTICIPATED)
            },
            onNavigateToAllReleases = {
                navigateToAllReleases()
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
        allReleasesScreen(
            onNavigateBack = { popBackStack() },
            onEpisodeClick = { showId, episode ->
                navigateToEpisode(showId, episode)
            },
            onShowClick = { navigateToShow(it) },
            onMovieClick = { navigateToMovie(it) },
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
                    mediaType = Show,
                    mediaImage = show.images?.getFanartUrl(),
                    filter = filter,
                )
            },
            onNavigateToList = { show, list ->
                navigateToListDetails(
                    list = list,
                    mediaId = show.ids.trakt,
                    mediaType = listOf(Show),
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
            onNavigateToTrivia = { show ->
                navigateToTrivia(
                    mediaId = show.ids.trakt,
                    mediaType = Show,
                    mediaImage = show.images?.getFanartUrl(),
                    mediaTitle = show.title,
                    navSource = "show_summary",
                )
            },
            onNavigateToSentiment = { show, sentiments ->
                navigateToSentiment(
                    sentiments = sentiments,
                    mediaTitle = show.title,
                    mediaImage = show.images?.getFanartUrl(),
                    navSource = "show_summary",
                )
            },
            onNavigateToTrailer = { navigateToYouTubePlayer(it) },
            onNavigateToExtra = { navigateToYouTubePlayer(it.url) },
            onNavigateToAllSeasons = { show, initialSeason ->
                navigateToAllShowSeasons(
                    showId = show.ids.trakt,
                    initialSeason = initialSeason,
                    backgroundUrl = show.images?.getFanartUrl(),
                )
            },
            onNavigateToHistory = { show, watched ->
                navigateToShowHistory(
                    showId = show.ids.trakt,
                    showTitle = show.title,
                    watched = watched,
                    backgroundUrl = show.images?.getFanartUrl(),
                )
            },
            onNavigateToUser = { navigateToUserProfile(it) },
            onNavigateVip = { navigateToBilling() },
            onNavigateBack = { popBackStack() },
        )

        showHistoryScreen(
            onNavigateToEpisode = { showId, episode ->
                navigateToEpisode(showId, episode)
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
            onAllSeasonsClick = { show, season ->
                navigateToAllShowSeasons(
                    showId = show.ids.trakt,
                    initialSeason = season,
                    backgroundUrl = show.images?.getFanartUrl(),
                )
            },
            onNavigateToHistory = { show, episode, watched ->
                navigateToEpisodeHistory(
                    episodeId = episode.ids.trakt,
                    episodeTitle = episode.title,
                    watched = watched,
                    backgroundUrl = show.images?.getFanartUrl(),
                )
            },
            onNavigateToUser = { navigateToUserProfile(it) },
            onNavigateVip = { navigateToBilling() },
            onNavigateBack = { popBackStack() },
        )

        episodeHistoryScreen(
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
                    mediaType = Movie,
                    mediaImage = movie.images?.getFanartUrl(),
                    filter = filter,
                )
            },
            onNavigateToList = { movie, list ->
                navigateToListDetails(
                    list = list,
                    mediaId = movie.ids.trakt,
                    mediaType = listOf(Movie),
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
            onNavigateToTrivia = { movie ->
                navigateToTrivia(
                    mediaId = movie.ids.trakt,
                    mediaType = Movie,
                    mediaImage = movie.images?.getFanartUrl(),
                    mediaTitle = movie.title,
                    navSource = "movie_summary",
                )
            },
            onNavigateToSentiment = { movie, sentiments ->
                navigateToSentiment(
                    sentiments = sentiments,
                    mediaTitle = movie.title,
                    mediaImage = movie.images?.getFanartUrl(),
                    navSource = "movie_summary",
                )
            },
            onNavigateToTrailer = { navigateToYouTubePlayer(it) },
            onNavigateToExtra = { navigateToYouTubePlayer(it.url) },
            onNavigateToHistory = { movie, watched ->
                navigateToMovieHistory(
                    movieId = movie.ids.trakt,
                    movieTitle = movie.title,
                    watched = watched,
                    backgroundUrl = movie.images?.getFanartUrl(),
                )
            },
            onNavigateToUser = { navigateToUserProfile(it) },
            onNavigateVip = { navigateToBilling() },
            onNavigateBack = { popBackStack() },
        )

        movieHistoryScreen(
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
            onNavigateToSearch = { navigateToSearch() },
            onNavigateToMovie = { navigateToMovie(it) },
            onNavigateToWatchlist = { navigateToWatchlist() },
            onNavigateToPersonalList = {
                navigateToPersonalList(
                    listId = it.ids.trakt.value,
                    listTitle = it.name,
                    listDescription = it.description,
                    listPrivacy = it.privacy,
                )
            },
            onNavigateToCustomList = { list ->
                navigateToListDetails(
                    list = list,
                    mediaId = (-1).toTraktId(),
                    mediaType = listOf(Show, Movie),
                    mediaImage = null,
                )
            },
            onNavigateToAllLists = { navigateToAllLists(it) },
            onNavigateToVip = { navigateToBilling() },
        )
        allListsScreen(
            onNavigateToList = {
                navigateToListDetails(
                    list = it,
                    mediaId = (-1).toTraktId(),
                    mediaType = listOf(Show, Movie),
                    mediaImage = null,
                )
            },
            onNavigateToPersonalList = {
                navigateToPersonalList(
                    listId = it.ids.trakt.value,
                    listTitle = it.name,
                    listDescription = it.description,
                    listPrivacy = it.privacy,
                )
            },
            onNavigateBack = { popBackStack() },
        )
        allWatchlistScreen(
            onNavigateToShow = { navigateToShow(it) },
            onNavigateToMovie = { navigateToMovie(it) },
            onNavigateBack = { popBackStack() },
        )
        allPersonalListScreen(
            onNavigateToShow = { navigateToShow(it) },
            onNavigateToMovie = { navigateToMovie(it) },
            onNavigateToEpisode = { showId, episode ->
                navigateToEpisode(showId, episode)
            },
            onNavigateToReorder = { list ->
                navigateToListReorder(
                    list = list,
                )
            },
            onNavigateBack = { popBackStack() },
        )
        listDetailsScreen(
            onNavigateBack = { popBackStack() },
            onNavigateToShow = { navigateToShow(it) },
            onNavigateToMovie = { navigateToMovie(it) },
            onNavigateToEpisode = { showId, episode ->
                navigateToEpisode(showId, episode)
            },
        )
        listReorderScreen(
            onNavigateBack = { popBackStack() },
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
            onNavigateToList = { list ->
                navigateToListDetails(
                    list = list,
                    mediaId = (-1).toTraktId(),
                    mediaType = listOf(Show, Movie),
                    mediaImage = null,
                )
            },
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
            onNavigateToProgress = { navigateToProgress() },
            onNavigateToActivity = { navigateToProfileActivity() },
            onNavigateToLibrary = { navigateToLibrary() },
            onNavigateToDiscover = { navigateToDiscover() },
            onNavigateToSettings = { navigateToSettings() },
            onNavigateToHome = { navigateToHome() },
            onNavigateToUser = { navigateToUserProfile(it) },
            onNavigateToSocial = { navigateToProfileSocial() },
            onNavigateToVip = { navigateToBilling() },
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

        allProgressScreen(
            onNavigateToShow = { navigateToShow(it) },
            onNavigateBack = { popBackStack() },
        )

        allProfileSocialScreen(
            onNavigateToUser = { navigateToUserProfile(it) },
            onNavigateBack = { popBackStack() },
        )

        profileAllActivityScreen(
            onNavigateToShow = { navigateToShow(it) },
            onNavigateToMovie = { navigateToMovie(it) },
            onNavigateToEpisode = { showId, episode ->
                navigateToEpisode(showId, episode)
            },
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
            onNavigateToUser = { navigateToUserProfile(it) },
            onNavigateBack = { popBackStack() },
        )
    }
}

internal fun NavGraphBuilder.allShowSeasonsScreens(controller: NavHostController) {
    with(controller) {
        allShowSeasonsScreen(
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

internal fun NavGraphBuilder.triviaScreens(controller: NavHostController) {
    with(controller) {
        triviaScreen(
            onNavigateBack = { popBackStack() },
        )
    }
}

internal fun NavGraphBuilder.sentimentScreens(controller: NavHostController) {
    with(controller) {
        sentimentScreen(
            onNavigateVip = { navigateToBilling() },
            onNavigateBack = { popBackStack() },
        )
    }
}

internal fun NavGraphBuilder.settingsScreens(controller: NavHostController) {
    with(controller) {
        settingsScreen(
            onNavigateHome = { navigateToHome() },
            onNavigateYounify = { navigateToYounify() },
            onNavigateBlockedUsers = { navigateToBlockedUsers() },
            onNavigateBack = { popBackStack() },
        )

        younifyScreen(
            onNavigateToVip = { navigateToBilling() },
            onNavigateBack = { popBackStack() },
        )
    }
}

internal fun NavGraphBuilder.calendarScreens(controller: NavHostController) {
    with(controller) {
        calendarScreen(
            onEpisodeClick = { showId, episode ->
                navigateToEpisode(showId, episode)
            },
            onShowClick = { navigateToShow(it) },
            onMovieClick = { navigateToMovie(it) },
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

internal fun NavGraphBuilder.userProfileScreens(controller: NavHostController) {
    with(controller) {
        userProfileScreen(
            onNavigateToShow = ::navigateToShow,
            onNavigateToMovie = ::navigateToMovie,
            onNavigateToEpisode = ::navigateToEpisode,
            onNavigateToUser = ::navigateToUserProfile,
            onNavigateToAllHistory = ::navigateToAllUserProfileHistory,
            onNavigateToAllFavorites = ::navigateToAllUserProfileFavorites,
            onNavigateToAllLists = ::navigateToAllUserProfileLists,
            onNavigateToList = { list ->
                navigateToListDetails(
                    list = list,
                    mediaId = (-1).toTraktId(),
                    mediaType = listOf(Show, Movie),
                    mediaImage = null,
                )
            },
            onNavigateBack = ::popBackStack,
        )

        allUserProfileHistoryScreen(
            onNavigateToShow = { navigateToShow(it) },
            onNavigateToEpisode = { showId, episode -> navigateToEpisode(showId, episode) },
            onNavigateToMovie = { navigateToMovie(it) },
            onNavigateBack = { popBackStack() },
        )

        allUserProfileFavoritesScreen(
            onNavigateToShow = { navigateToShow(it) },
            onNavigateToMovie = { navigateToMovie(it) },
            onNavigateBack = { popBackStack() },
        )

        allUserProfileListsScreen(
            onNavigateToList = { list ->
                navigateToListDetails(
                    list = list,
                    mediaId = (-1).toTraktId(),
                    mediaType = listOf(Show, Movie),
                    mediaImage = null,
                )
            },
            onNavigateBack = { popBackStack() },
        )
    }
}

internal fun NavGraphBuilder.youTubePlayerScreens(controller: NavHostController) {
    with(controller) {
        youTubePlayerScreen(
            onNavigateBack = { popBackStack() },
        )
    }
}
