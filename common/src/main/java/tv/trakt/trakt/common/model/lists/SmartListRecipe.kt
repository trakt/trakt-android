package tv.trakt.trakt.common.model.lists

import android.content.res.Resources
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.globalfilter.EXCLUSION_PREFIX
import tv.trakt.trakt.resources.R
import java.util.Locale

private const val PREVIEW_LABEL_COUNT = 3

@Immutable
data class SmartListRecipePreview(
    val summary: String,
    val remaining: Int,
) {
    companion object {
        fun fromLabels(labels: List<String>): SmartListRecipePreview {
            return SmartListRecipePreview(
                summary = labels.take(PREVIEW_LABEL_COUNT).joinToString(", "),
                remaining = (labels.size - PREVIEW_LABEL_COUNT).coerceAtLeast(0),
            )
        }
    }
}

@Immutable
data class SmartListRecipe(
    val source: String,
    val mediaType: String,
    val includes: ImmutableList<Include>,
    val limits: ImmutableList<String>,
    val hidden: ImmutableList<String>,
) {
    val isEmpty: Boolean
        get() = includes.isEmpty() && limits.isEmpty() && hidden.isEmpty()

    @Immutable
    data class Include(
        val key: String,
        val label: String,
        val values: ImmutableList<Value>,
        val details: ImmutableList<String>,
    )

    @Immutable
    data class Value(
        val label: String,
        val isExcluded: Boolean,
    )

    companion object {
        fun from(
            filters: SmartListFilters,
            awardCeremonies: List<AwardCeremony>,
            resources: Resources,
            locale: Locale,
        ): SmartListRecipe {
            val builder = RecipeBuilder(filters, resources, locale)

            return SmartListRecipe(
                source = resources.getString(filters.source.displayRes),
                mediaType = resources.getString(filters.media.recipeLabelRes),
                includes = builder.includes(awardCeremonies).toImmutableList(),
                limits = builder.limits().toImmutableList(),
                hidden = builder.hidden().toImmutableList(),
            )
        }
    }
}

internal val MediaMode.recipeLabelRes: Int
    get() = when (this) {
        MediaMode.Movies -> R.string.button_text_movies
        MediaMode.Shows -> R.string.button_text_shows
        MediaMode.Media -> R.string.button_text_media
    }

private class RecipeBuilder(
    private val filters: SmartListFilters,
    private val resources: Resources,
    private val locale: Locale,
) {
    fun includes(awardCeremonies: List<AwardCeremony>): List<SmartListRecipe.Include> {
        return listOfNotNull(
            genres(),
            stringRow("subgenres", R.string.header_smart_list_subgenres, filters.subgenres) {
                it.toGenreLabel(resources)
            },
            stringRow(
                key = "keywords",
                labelRes = R.string.header_smart_list_keywords,
                values = filters.keywords,
                details = matchDetails(filters.keywords, filters.keywordsMatchAll),
                format = String::toSummaryTitleCase,
            ),
            people(),
            stringRow(
                "studios",
                R.string.header_smart_list_studios,
                filters.studios,
                format = String::toSummaryTitleCase,
            ),
        ) + awards(awardCeremonies) + listOfNotNull(
            stringRow(
                "networks",
                R.string.header_smart_list_networks,
                filters.networks,
                format = String::toSummaryTitleCase,
            ),
            stringRow("languages", R.string.header_smart_list_languages, filters.languages) {
                Locale.forLanguageTag(it).getDisplayLanguage(locale)
            },
            includeRow(
                key = "countries",
                labelRes = R.string.header_country,
                values = filters.countries.toValues(excluded = false, format = ::countryName) +
                    filters.excludedCountries.toValues(excluded = true, format = ::countryName),
            ),
            includeRow(
                key = "certifications",
                labelRes = R.string.header_certification,
                values = filters.certifications.toValues(excluded = false) { it.uppercase(locale) } +
                    filters.excludedCertifications.toValues(excluded = true) { it.uppercase(locale) },
            ),
            stringRow("statuses", R.string.header_status, filters.statuses, format = String::toSummaryTitleCase),
            watchNow(),
        )
    }

    fun limits(): List<String> {
        val ranges = listOfNotNull(
            formatNumberRange(filters.years.orEmpty(), R.string.advanced_filter_label_release_year, resources),
            filters.releasedWithinDays?.let {
                resources.getString(R.string.list_summary_released_within_days, it.toString())
            },
            formatNumberRange(filters.runtimes.orEmpty(), R.string.advanced_filter_label_runtime, resources),
            formatPercentRange(filters.ratings.orEmpty(), "Trakt", resources, locale),
            formatVotesRange(filters.votes.orEmpty(), "Trakt", resources, locale),
            formatFloatRange(filters.imdbRatings.orEmpty(), resources)?.let { "IMDb $it" },
            formatVotesRange(filters.imdbVotes.orEmpty(), "IMDb", resources, locale),
            formatFloatRange(filters.tmdbRatings.orEmpty(), resources)?.let { "TMDB $it" },
            formatPercentRange(filters.rtMeters.orEmpty(), "RT", resources, locale),
            formatPercentRange(filters.rtUserMeters.orEmpty(), "RT Audience", resources, locale),
            formatFloatRange(filters.letterboxdRatings.orEmpty(), resources)?.let { "Letterboxd $it" },
            formatFloatRange(filters.malRatings.orEmpty(), resources)?.let { "MAL $it" },
        ) + ParentalGuideCategory.entries.mapNotNull { category ->
            formatParentalRange(category, filters.parentalRange(category).orEmpty(), resources)
        }

        return ranges + listOfNotNull(
            resources.getString(R.string.toggle_text_include_unrated_parental)
                .takeIf { filters.parentalIncludeUnrated },
        )
    }

    fun hidden(): List<String> {
        return listOf(
            filters.ignoreWatched to R.string.tag_text_watched,
            filters.ignoreWatchlisted to R.string.tag_text_watchlisted,
            filters.ignoreWatching to R.string.toggle_text_hide_watching,
            filters.ignoreUnreleased to R.string.toggle_text_hide_unreleased,
            filters.ignoreReleased to R.string.toggle_text_hide_released,
            filters.ignoreEnded to R.string.toggle_text_hide_ended,
            filters.ignoreAiring to R.string.toggle_text_hide_airing,
            filters.ignoreNoReleaseDate to R.string.toggle_text_hide_no_release_date,
        ).filter { it.first }
            .map { resources.getString(it.second) }
    }

    private fun genres(): SmartListRecipe.Include? {
        val included = filters.genres.orEmpty()
        val values = included.map { resources.getString(it.displayStringRes) }.toValues(excluded = false) +
            filters.excludedGenres.orEmpty().map { resources.getString(it.displayStringRes) }.toValues(excluded = true)

        return includeRow(
            key = "genres",
            labelRes = R.string.header_genre,
            values = values,
            details = matchDetails(included, filters.genresMatchAll),
        )
    }

    private fun people(): SmartListRecipe.Include? {
        val role = filters.peopleRole?.let {
            detail(R.string.label_smart_list_people_role, resources.getString(it.displayRes))
        }

        return stringRow(
            key = "people",
            labelRes = R.string.header_smart_list_people,
            values = filters.people,
            details = matchDetails(filters.people, filters.peopleMatchAll) + listOfNotNull(role),
            format = String::toSummaryTitleCase,
        )
    }

    private fun watchNow(): SmartListRecipe.Include? {
        val country = filters.watchnowCountry?.let {
            detail(R.string.header_country, countryName(it))
        }

        return includeRow(
            key = "watchnow",
            labelRes = R.string.header_streaming,
            values = filters.availability.orEmpty()
                .map { resources.getString(it.displayStringRes) }
                .toValues(excluded = false),
            details = listOfNotNull(country),
        )
    }

    private fun awards(awardCeremonies: List<AwardCeremony>): List<SmartListRecipe.Include> {
        val awards = filters.awards.orEmpty().distinct()
        if (awards.isEmpty()) return emptyList()

        val defaultLabel = resources.getString(R.string.header_smart_list_awards)
        val groups = awards.groupBy { raw ->
            awardCeremonies.firstOrNull { ceremony ->
                ceremony.awards.any { it.slug == raw.removePrefix(EXCLUSION_PREFIX) }
            }
        }

        return groups.entries.mapIndexed { index, (ceremony, values) ->
            SmartListRecipe.Include(
                key = ceremony?.let { "awards:${it.slug}" } ?: "awards",
                label = ceremony?.name ?: defaultLabel,
                values = values.map { raw ->
                    raw.toValue { slug ->
                        ceremony?.awards?.firstOrNull { it.slug == slug }?.name ?: slug.toSummaryTitleCase()
                    }
                }.toImmutableList(),
                details = when (index) {
                    groups.size - 1 -> awardDetails()
                    else -> emptyList()
                }.toImmutableList(),
            )
        }
    }

    private fun awardDetails(): List<String> {
        val min = filters.awardYears?.getOrNull(0)
        val max = filters.awardYears?.getOrNull(1)
        val years = if (min != null && max != null) {
            detail(
                R.string.label_smart_list_ceremony_year,
                resources.getString(R.string.list_summary_range_between, min.toString(), max.toString()),
            )
        } else {
            null
        }

        return listOfNotNull(
            resources.getString(R.string.toggle_text_award_winners_only).takeIf { filters.awardWon },
            years,
        )
    }

    private fun matchDetails(
        included: List<Any>?,
        matchAll: Boolean,
    ): List<String> {
        val count = included.orEmpty().count { it !is String || !it.startsWith(EXCLUSION_PREFIX) }
        if (count < 2) return emptyList()

        val value = when {
            matchAll -> R.string.option_text_match_all
            else -> R.string.option_text_match_any
        }
        return listOf(detail(R.string.label_smart_list_match, resources.getString(value)))
    }

    private fun stringRow(
        key: String,
        labelRes: Int,
        values: List<String>?,
        details: List<String> = emptyList(),
        format: (String) -> String,
    ): SmartListRecipe.Include? {
        return includeRow(
            key = key,
            labelRes = labelRes,
            values = values.orEmpty().distinct().map { it.toValue(format) },
            details = details,
        )
    }

    private fun includeRow(
        key: String,
        labelRes: Int,
        values: List<SmartListRecipe.Value>,
        details: List<String> = emptyList(),
    ): SmartListRecipe.Include? {
        if (values.isEmpty()) return null

        return SmartListRecipe.Include(
            key = key,
            label = resources.getString(labelRes),
            values = values.toImmutableList(),
            details = details.toImmutableList(),
        )
    }

    private fun detail(
        labelRes: Int,
        value: String,
    ): String {
        return resources.getString(R.string.text_smart_list_recipe_detail, resources.getString(labelRes), value)
    }

    private fun countryName(code: String): String {
        val name = Locale("", code.uppercase(Locale.ROOT)).getDisplayCountry(locale)
        return name.ifBlank { code.uppercase(locale) }
    }
}

private fun String.toValue(format: (String) -> String): SmartListRecipe.Value {
    val isExcluded = startsWith(EXCLUSION_PREFIX)
    return SmartListRecipe.Value(
        label = format(removePrefix(EXCLUSION_PREFIX)),
        isExcluded = isExcluded,
    )
}

private fun List<String>?.toValues(
    excluded: Boolean,
    format: (String) -> String = { it },
): List<SmartListRecipe.Value> {
    return orEmpty().map { SmartListRecipe.Value(label = format(it), isExcluded = excluded) }
}
