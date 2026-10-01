package tv.trakt.trakt.core.ratings.data.work

internal class PostRatingException(
    cause: Throwable,
) : Exception("Failed to post rating", cause)
