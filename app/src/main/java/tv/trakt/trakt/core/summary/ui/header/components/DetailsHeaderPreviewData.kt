package tv.trakt.trakt.core.summary.ui.header.components

import kotlinx.collections.immutable.persistentMapOf
import tv.trakt.trakt.common.model.ExternalRating

internal val previewExternalRatings = ExternalRating(
    trakt = ExternalRating.TraktRating(
        rating = 7.2F,
        votes = 12345,
        distribution = persistentMapOf(),
    ),
    imdb = ExternalRating.ImdbRating(
        rating = 7.5F,
        votes = 12345,
        link = "https://www.imdb.com/title/tt1234567/",
    ),
    meta = ExternalRating.MetaRating(
        rating = 75,
        link = "https://www.metacritic.com/movie/sample-movie",
    ),
    rotten = ExternalRating.RottenRating(
        rating = 85F,
        state = "fresh",
        userRating = 90,
        userState = "upright",
        link = "https://www.rottentomatoes.com/m/sample_movie",
    ),
    tmdb = ExternalRating.TmdbRating(
        rating = 8.2F,
        votes = 45678,
        link = "https://www.themoviedb.org/movie/12345",
    ),
    mal = ExternalRating.MalRating(
        rating = 8.5F,
        votes = 7890,
        link = "https://myanimelist.net/anime/12345",
    ),
    letterboxd = ExternalRating.LetterboxdRating(
        rating = 4.5F,
        votes = 1234,
        link = "https://letterboxd.com/film/example/",
    ),
)
