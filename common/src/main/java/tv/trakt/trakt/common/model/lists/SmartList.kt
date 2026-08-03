package tv.trakt.trakt.common.model.lists

import android.content.res.Resources
import android.icu.text.NumberFormat
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalResources
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.helpers.extensions.capitalize
import tv.trakt.trakt.common.helpers.extensions.toZonedDateTime
import tv.trakt.trakt.common.helpers.serializers.ImmutableListSerializer
import tv.trakt.trakt.common.helpers.serializers.ZonedDateTimeSerializer
import tv.trakt.trakt.common.model.Ids
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.MediaGenre
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.SmartListDto
import tv.trakt.trakt.common.networking.SmartListFiltersDto
import tv.trakt.trakt.resources.R
import java.time.ZonedDateTime
import java.util.Locale

@Immutable
@Serializable
data class SmartList(
    val ids: Ids,
    val name: String,
    val privacy: CustomList.Privacy?,
    @Serializable(ZonedDateTimeSerializer::class)
    val createdAt: ZonedDateTime,
    @Serializable(ZonedDateTimeSerializer::class)
    val updatedAt: ZonedDateTime,
    val images: Images?,
    val source: String,
    val mediaType: MediaMode,
    val filters: SmartListFilters,
) {
    @Composable
    fun rememberDescription(): String {
        val resources = LocalResources.current
        val configuration = LocalConfiguration.current

        return remember(this, configuration) {
            val locale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()

            val sourcePart = sourceLabel(resources)
            val filtersPart = filters.toSummaryParts(resources, locale)

            (listOf(sourcePart) + filtersPart).joinToString(", ")
        }
    }

    private fun sourceLabel(resources: Resources): String {
        return when (source) {
            "trending" -> resources.getString(R.string.list_title_trending)
            "popular" -> resources.getString(R.string.list_title_most_popular)
            "anticipated" -> resources.getString(R.string.list_title_most_anticipated)
            "recommendations" -> resources.getString(R.string.list_title_recommended)
            "discover" -> resources.getString(R.string.page_title_discover)
            else -> source.toSummaryTitleCase()
        }
    }

    companion object {
        fun fromDto(dto: SmartListDto): SmartList {
            return SmartList(
                ids = Ids(
                    trakt = TraktId(dto.ids.trakt),
                    slug = SlugId(dto.ids.slug),
                ),
                name = dto.name,
                privacy = CustomList.Privacy.fromString(dto.privacy),
                createdAt = dto.createdAt.toZonedDateTime(),
                updatedAt = dto.updatedAt.toZonedDateTime(),
                images = Images(
                    posters = dto.images.posters
                        .distinct()
                        .toImmutableList(),
                ),
                source = dto.source,
                mediaType = when (dto.mediaType) {
                    "shows" -> MediaMode.Shows
                    "movies" -> MediaMode.Movies
                    else -> MediaMode.Media
                },
                filters = SmartListFilters.fromDto(dto.filters),
            )
        }
    }
}

@Immutable
@Serializable
data class SmartListFilters(
    @Serializable(with = ImmutableListSerializer::class)
    val genres: ImmutableList<String>,
    @Serializable(with = ImmutableListSerializer::class)
    val subgenres: ImmutableList<String>,
    @Serializable(with = ImmutableListSerializer::class)
    val certifications: ImmutableList<String>,
    @Serializable(with = ImmutableListSerializer::class)
    val languages: ImmutableList<String>,
    @Serializable(with = ImmutableListSerializer::class)
    val countries: ImmutableList<String>,
    @Serializable(with = ImmutableListSerializer::class)
    val statuses: ImmutableList<String>,
    @Serializable(with = ImmutableListSerializer::class)
    val networks: ImmutableList<String>,
    @Serializable(with = ImmutableListSerializer::class)
    val watchNow: ImmutableList<String>,
    @Serializable(with = ImmutableListSerializer::class)
    val years: ImmutableList<Int>,
    @Serializable(with = ImmutableListSerializer::class)
    val ratings: ImmutableList<Int>,
    @Serializable(with = ImmutableListSerializer::class)
    val runtimes: ImmutableList<Int>,
    @Serializable(with = ImmutableListSerializer::class)
    val imdbRatings: ImmutableList<Int>,
    @Serializable(with = ImmutableListSerializer::class)
    val rtMeters: ImmutableList<Int>,
    @Serializable(with = ImmutableListSerializer::class)
    val rtUserMeters: ImmutableList<Int>,
    val ignoreWatched: Boolean,
    val ignoreWatchlisted: Boolean,
) {
    fun toSummaryParts(
        resources: Resources,
        locale: Locale,
    ): List<String> {
        val values = genres.map { it.toGenreLabel(resources) } +
            subgenres.map { it.toGenreLabel(resources) } +
            certifications.map { it.uppercase(locale) } +
            languages.map { it.toSummaryTitleCase() } +
            countries.map { it.uppercase(locale) } +
            statuses.map { it.toSummaryTitleCase() } +
            networks.map { it.toSummaryTitleCase() } +
            watchNow.map { it.toSummaryTitleCase() }

        val ranges = listOfNotNull(
            formatNumberRange(years, R.string.advanced_filter_label_release_year, resources),
            formatPercentRange(ratings, "Trakt", resources, locale),
            formatNumberRange(runtimes, R.string.advanced_filter_label_runtime, resources),
            formatPlainRange(imdbRatings, resources)?.let { "IMDb $it" },
            formatPercentRange(rtMeters, "RT", resources, locale),
            formatPercentRange(rtUserMeters, "RT Audience", resources, locale),
        )

        val flags = listOfNotNull(
            resources.getString(R.string.header_hide_watched).takeIf { ignoreWatched },
            resources.getString(R.string.header_hide_watchlisted).takeIf { ignoreWatchlisted },
        )

        return values + ranges + flags
    }

    companion object {
        fun fromDto(dto: SmartListFiltersDto): SmartListFilters {
            return SmartListFilters(
                genres = dto.genres.orEmpty().toImmutableList(),
                subgenres = dto.subgenres.orEmpty().toImmutableList(),
                certifications = dto.certifications.orEmpty().toImmutableList(),
                languages = dto.languages.orEmpty().toImmutableList(),
                countries = dto.countries.orEmpty().toImmutableList(),
                statuses = dto.statuses.orEmpty().toImmutableList(),
                networks = dto.networks.orEmpty().toImmutableList(),
                watchNow = dto.watchnow.orEmpty().toImmutableList(),
                years = dto.years.orEmpty().toImmutableList(),
                ratings = dto.ratings.orEmpty().toImmutableList(),
                runtimes = dto.runtimes.orEmpty().toImmutableList(),
                imdbRatings = dto.imdbRatings.orEmpty().toImmutableList(),
                rtMeters = dto.rtMeters.orEmpty().toImmutableList(),
                rtUserMeters = dto.rtUserMeters.orEmpty().toImmutableList(),
                ignoreWatched = dto.ignoreWatched ?: false,
                ignoreWatchlisted = dto.ignoreWatchlisted ?: false,
            )
        }
    }
}

private fun formatNumberRange(
    range: List<Int>,
    @StringRes labelRes: Int,
    resources: Resources,
): String? {
    val min = range.getOrNull(0)
    val max = range.getOrNull(1)

    return when {
        min != null && max != null -> resources.getString(labelRes, min, max)
        min != null -> resources.getString(R.string.list_summary_range_from, min.toString())
        else -> null
    }
}

private fun formatPlainRange(
    range: List<Int>,
    resources: Resources,
): String? {
    val min = range.getOrNull(0)
    val max = range.getOrNull(1)

    return when {
        min != null && max != null -> resources.getString(
            R.string.list_summary_range_between,
            min.toString(),
            max.toString(),
        )
        min != null -> resources.getString(R.string.list_summary_range_from, min.toString())
        else -> null
    }
}

private fun formatPercentRange(
    range: List<Int>,
    label: String,
    resources: Resources,
    locale: Locale,
): String? {
    val min = range.getOrNull(0)
    val max = range.getOrNull(1)
    val percentFormat = NumberFormat.getPercentInstance(locale)

    return when {
        min != null && max != null -> resources.getString(
            R.string.list_summary_range_labeled_between,
            label,
            percentFormat.format(min / 100.0),
            percentFormat.format(max / 100.0),
        )
        min != null -> resources.getString(
            R.string.list_summary_range_labeled_from,
            label,
            percentFormat.format(min / 100.0),
        )
        else -> null
    }
}

private fun String.toGenreLabel(resources: Resources): String {
    val genre = MediaGenre.fromSlug(this) ?: return toSummaryTitleCase()
    return resources.getString(genre.displayStringRes)
}

private fun String.toSummaryTitleCase(): String {
    return split('-', '_').joinToString(" ") { it.capitalize() }
}
