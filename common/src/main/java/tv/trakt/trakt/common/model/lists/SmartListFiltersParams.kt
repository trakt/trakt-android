package tv.trakt.trakt.common.model.lists

import tv.trakt.trakt.common.model.MediaMode

fun SmartListFilters.toApiParams(): Map<String, String> {
    val mediaType = when (media) {
        MediaMode.Media -> "media"
        MediaMode.Shows -> "shows"
        MediaMode.Movies -> "movies"
    }

    return listOf(
        "source" to source.value,
        "media_type" to mediaType,
        "genres" to genreTokens.joinToList(),
        "genres_operator" to "and".takeIf { genresMatchAll },
        "subgenres" to subgenres?.joinToList(),
        "certifications" to certificationTokens.joinToList(),
        "languages" to languages?.joinToList(),
        "countries" to countryTokens.joinToList(),
        "statuses" to statuses?.joinToList(),
        "networks" to networks?.joinToList(),
        "watchnow" to availability?.joinToList { it.slug },
        "watchnow_country" to watchnowCountry,
        "keywords" to keywords?.joinToList(),
        "keywords_operator" to "and".takeIf { keywordsMatchAll },
        "people" to people?.joinToList(),
        "people_operator" to "and".takeIf { peopleMatchAll },
        "people_role" to peopleRole?.value,
        "studios" to studios?.joinToList(),
        "awards" to awards?.joinToList(),
        "award_years" to awardYears?.joinToRange(),
        "award_won" to awardWon.toFlagParam(),
        "years" to years?.joinToRange(),
        "ratings" to ratings?.joinToRange(),
        "runtimes" to runtimes?.joinToRange(),
        "imdb_ratings" to imdbRatings?.joinToRange(),
        "rt_meters" to rtMeters?.joinToRange(),
        "rt_user_meters" to rtUserMeters?.joinToRange(),
        "tmdb_ratings" to tmdbRatings?.joinToRange(),
        "letterboxd_ratings" to letterboxdRatings?.joinToRange(),
        "mal_ratings" to malRatings?.joinToRange(),
        "votes" to votes?.joinToRange(),
        "imdb_votes" to imdbVotes?.joinToRange(),
        "released_within_days" to releasedWithinDays?.toString(),
        "parental_include_unrated" to parentalIncludeUnrated.toFlagParam(),
        "ignore_watched" to ignoreWatched.toFlagParam(),
        "ignore_watchlisted" to ignoreWatchlisted.toFlagParam(),
        "ignore_watching" to ignoreWatching.toFlagParam(),
        "ignore_unreleased" to ignoreUnreleased.toFlagParam(),
        "ignore_released" to ignoreReleased.toFlagParam(),
        "ignore_ended" to ignoreEnded.toFlagParam(),
        "ignore_airing" to ignoreAiring.toFlagParam(),
        "ignore_no_release_date" to ignoreNoReleaseDate.toFlagParam(),
    ).plus(
        ParentalGuideCategory.entries.map { it.apiKey to parentalRange(it)?.joinToRange() },
    ).mapNotNull { (key, value) -> value?.let { key to it } }
        .toMap()
}

private fun <T> List<T>.joinToList(transform: (T) -> String = { it.toString() }): String? =
    takeIf { it.isNotEmpty() }?.joinToString(",", transform = transform)

private fun Boolean.toFlagParam(): String? = takeIf { it }?.toString()

private fun <T : Number> List<T>.joinToRange(): String? = takeIf { it.isNotEmpty() }?.joinToString("-")
