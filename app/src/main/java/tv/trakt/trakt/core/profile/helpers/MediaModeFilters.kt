package tv.trakt.trakt.core.profile.helpers

import tv.trakt.trakt.common.core.favorites.FavoriteItem
import tv.trakt.trakt.common.core.library.LibraryItem
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.profile.ProfileCommentItem
import tv.trakt.trakt.common.model.profile.ProfileRatingItem

internal fun FavoriteItem.matches(mode: MediaMode): Boolean {
    return when (this) {
        is FavoriteItem.ShowItem -> mode.isMediaOrShows
        is FavoriteItem.MovieItem -> mode.isMediaOrMovies
    }
}

internal fun LibraryItem.matches(mode: MediaMode): Boolean {
    return when (this) {
        is LibraryItem.EpisodeItem -> mode.isMediaOrShows
        is LibraryItem.MovieItem -> mode.isMediaOrMovies
    }
}

internal fun ProfileRatingItem.matches(mode: MediaMode): Boolean {
    return when (this) {
        is ProfileRatingItem.ShowItem,
        is ProfileRatingItem.SeasonItem,
        is ProfileRatingItem.EpisodeItem,
        -> mode.isMediaOrShows
        is ProfileRatingItem.MovieItem -> mode.isMediaOrMovies
    }
}

internal fun ProfileCommentItem.matches(mode: MediaMode): Boolean {
    return when (this) {
        is ProfileCommentItem.ShowItem,
        is ProfileCommentItem.EpisodeItem,
        -> mode.isMediaOrShows
        is ProfileCommentItem.MovieItem -> mode.isMediaOrMovies
    }
}
