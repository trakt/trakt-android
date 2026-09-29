package tv.trakt.trakt.core.ratings.ui

internal enum class RatingDelight {
    RottenTomato,
    Popcorn,
    FavoriteGlow,
}

private const val MAX_RATING = 10
private const val LOWEST_RATINGS_CEILING = 2

internal fun ratingDelight(rating: Int): RatingDelight? {
    return when (rating) {
        MAX_RATING -> RatingDelight.Popcorn
        in 1..LOWEST_RATINGS_CEILING -> RatingDelight.RottenTomato
        else -> null
    }
}
