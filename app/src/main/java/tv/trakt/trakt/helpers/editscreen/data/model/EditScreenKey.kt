package tv.trakt.trakt.helpers.editscreen.data.model

import kotlinx.collections.immutable.toImmutableSet
import tv.trakt.trakt.resources.R

internal enum class EditScreenKey(
    val preferenceKey: String,
    val displayStringRes: Int,
) {
    HomeUpNext("key_edit_home_up_next", R.string.list_title_up_next),
    HomeWatchlist("key_edit_home_watchlist", R.string.list_title_start_watching),
    HomeStreaks("key_edit_home_streaks", R.string.label_stats_current_streak),
    HomeUpcoming("key_edit_home_upcoming", R.string.page_title_calendar),
    HomeHistory("key_edit_home_history", R.string.list_title_history),
    HomeSocial("key_edit_home_social", R.string.list_title_social_activity),

    ProfileHistory("key_edit_profile_history", R.string.list_title_history),
    ProfileProgress("key_edit_profile_progress", R.string.list_title_progress),
    ProfileFavorites("key_edit_profile_favorites", R.string.list_title_favorites),
    ProfileLibrary("key_edit_profile_library", R.string.list_title_library),
    ProfileSocial("key_edit_profile_social", R.string.list_title_social),

    ListsWatchlist("key_edit_lists_watchlist", R.string.list_title_watchlist),
    ListsMyLists("key_edit_lists_my_lists", R.string.list_title_personal_lists),

    ShowWhereToWatch("key_edit_show_where_to_watch", R.string.page_title_where_to_watch),
    ShowSentiment("key_edit_show_sentiment", R.string.header_community_sentiment),
    ShowReviews("key_edit_show_reviews", R.string.list_title_comments),
    ShowActors("key_edit_show_actors", R.string.list_title_actors),
    ShowSeasons("key_edit_show_seasons", R.string.list_title_seasons),
    ShowExtras("key_edit_show_extras", R.string.list_title_extras),
    ShowRelated("key_edit_show_related", R.string.list_title_related_shows),
    ShowLists("key_edit_show_lists", R.string.list_title_popular_lists),
    ShowHistory("key_edit_show_history", R.string.list_title_history),
    ShowTrivia("key_edit_show_trivia", R.string.list_title_trivia),

    MovieWhereToWatch("key_edit_movie_where_to_watch", R.string.page_title_where_to_watch),
    MovieSentiment("key_edit_movie_sentiment", R.string.header_community_sentiment),
    MovieReviews("key_edit_movie_reviews", R.string.list_title_comments),
    MovieActors("key_edit_movie_actors", R.string.list_title_actors),
    MovieExtras("key_edit_movie_extras", R.string.list_title_extras),
    MovieRelated("key_edit_movie_related", R.string.list_title_related_movies),
    MovieLists("key_edit_movie_lists", R.string.list_title_popular_lists),
    MovieHistory("key_edit_movie_history", R.string.list_title_history),
    MovieTrivia("key_edit_movie_trivia", R.string.list_title_trivia),

    EpisodeWhereToWatch("key_edit_episode_where_to_watch", R.string.page_title_where_to_watch),
    EpisodeReviews("key_edit_episode_reviews", R.string.list_title_comments),
    EpisodeActors("key_edit_episode_actors", R.string.list_title_actors),
    EpisodeSeason("key_edit_episode_season", R.string.list_title_seasons),
    EpisodeRelated("key_edit_episode_related", R.string.list_title_related_shows),
    EpisodeHistory("key_edit_episode_history", R.string.list_title_history),

    UserProfileHistory("key_edit_user_profile_history", R.string.list_title_history),
    UserProfileFavorites("key_edit_user_profile_favorites", R.string.list_title_favorites),
    UserProfileLists("key_edit_user_profile_lists", R.string.list_title_user_lists),
    UserProfileSocial("key_edit_user_profile_social", R.string.list_title_social),

    DiscoverTrending("key_edit_discover_trending", R.string.list_title_trending),
    DiscoverPopular("key_edit_discover_popular", R.string.list_title_most_popular),
    DiscoverAnticipated("key_edit_discover_anticipated", R.string.list_title_most_anticipated),
    DiscoverRecommended("key_edit_discover_recommended", R.string.list_title_recommended),
    ;

    companion object {
        val DiscoverKeys = setOf(
            DiscoverTrending,
            DiscoverAnticipated,
            DiscoverPopular,
            DiscoverRecommended,
        ).toImmutableSet()

        val HomeKeys = setOf(
            HomeUpNext,
            HomeWatchlist,
            HomeStreaks,
            HomeUpcoming,
            HomeHistory,
            HomeSocial,
        ).toImmutableSet()

        val ProfileKeys = setOf(
            ProfileHistory,
            ProfileProgress,
            ProfileFavorites,
            ProfileLibrary,
            ProfileSocial,
        ).toImmutableSet()

        val ListsKeys = setOf(
            ListsWatchlist,
            ListsMyLists,
        ).toImmutableSet()

        val ShowKeys = setOf(
            ShowWhereToWatch,
            ShowSentiment,
            ShowReviews,
            ShowActors,
            ShowSeasons,
            ShowExtras,
            ShowRelated,
            ShowLists,
            ShowHistory,
            ShowTrivia,
        ).toImmutableSet()

        val MovieKeys = setOf(
            MovieWhereToWatch,
            MovieSentiment,
            MovieReviews,
            MovieActors,
            MovieExtras,
            MovieRelated,
            MovieLists,
            MovieHistory,
            MovieTrivia,
        ).toImmutableSet()

        val EpisodeKeys = setOf(
            EpisodeWhereToWatch,
            EpisodeReviews,
            EpisodeActors,
            EpisodeSeason,
            EpisodeRelated,
            EpisodeHistory,
        ).toImmutableSet()

        val UserProfileKeys = setOf(
            UserProfileHistory,
            UserProfileFavorites,
            UserProfileLists,
            UserProfileSocial,
        ).toImmutableSet()
    }
}
