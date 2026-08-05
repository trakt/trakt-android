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
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.networking.SmartListDto
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
    val filters: SmartListFilters,
) {
    @Composable
    fun rememberDescription(): String {
        val resources = LocalResources.current
        val configuration = LocalConfiguration.current

        return remember(this, configuration) {
            val locale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()

            val sourcePart = resources.getString(filters.source.displayRes)
            val filtersPart = filters.toSummaryParts(resources, locale)

            (listOfNotNull(sourcePart) + filtersPart).joinToString(", ")
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
                filters = SmartListFilters.fromDto(dto),
            )
        }
    }
}

enum class SmartListSource(
    val value: String,
    @StringRes val displayRes: Int,
) {
    Trending("trending", R.string.list_title_trending),
    Popular("popular", R.string.list_title_most_popular),
    Anticipated("anticipated", R.string.list_title_most_anticipated),
    Recommendations("recommendations", R.string.list_title_recommended),
    Discover("discover", R.string.page_title_discover),
    Unknown("", R.string.text_unknown),
    ;

    companion object {
        fun fromApiValue(value: String): SmartListSource {
            return entries
                .firstOrNull { it.value == value }
                ?: Unknown
        }
    }
}

@Immutable
@Serializable
data class SmartListFilters(
    val media: MediaMode,
    val source: SmartListSource,
    @Serializable(with = ImmutableListSerializer::class)
    val genres: ImmutableList<MediaGenre>? = null,
    @Serializable(with = ImmutableListSerializer::class)
    val subgenres: ImmutableList<String>? = null,
    @Serializable(with = ImmutableListSerializer::class)
    val certifications: ImmutableList<String>? = null,
    @Serializable(with = ImmutableListSerializer::class)
    val languages: ImmutableList<String>? = null,
    @Serializable(with = ImmutableListSerializer::class)
    val countries: ImmutableList<String>? = null,
    @Serializable(with = ImmutableListSerializer::class)
    val statuses: ImmutableList<String>? = null,
    @Serializable(with = ImmutableListSerializer::class)
    val networks: ImmutableList<String>? = null,
    @Serializable(with = ImmutableListSerializer::class)
    val availability: ImmutableList<GlobalFilter.Availability>? = null,
    @Serializable(with = ImmutableListSerializer::class)
    val years: ImmutableList<Int>? = null,
    @Serializable(with = ImmutableListSerializer::class)
    val ratings: ImmutableList<Int>? = null,
    @Serializable(with = ImmutableListSerializer::class)
    val runtimes: ImmutableList<Int>? = null,
    @Serializable(with = ImmutableListSerializer::class)
    val imdbRatings: ImmutableList<Int>? = null,
    @Serializable(with = ImmutableListSerializer::class)
    val rtMeters: ImmutableList<Int>? = null,
    @Serializable(with = ImmutableListSerializer::class)
    val rtUserMeters: ImmutableList<Int>? = null,
    val ignoreWatched: Boolean = false,
    val ignoreWatchlisted: Boolean = false,
) {
    init {
        require(media == MediaMode.Shows || media == MediaMode.Movies) {
            "Media must be either Shows or Movies"
        }
    }

    fun toSummaryParts(
        resources: Resources,
        locale: Locale,
    ): List<String> {
        val values = genres.orEmpty().map { resources.getString(it.displayStringRes) } +
            subgenres.orEmpty().map { it.toGenreLabel(resources) } +
            certifications.orEmpty().map { it.uppercase(locale) } +
            languages.orEmpty().map { it.toSummaryTitleCase() } +
            countries.orEmpty().map { it.uppercase(locale) } +
            statuses.orEmpty().map { it.toSummaryTitleCase() } +
            networks.orEmpty().map { it.toSummaryTitleCase() } +
            availability.orEmpty().map { resources.getString(it.displayStringRes) }

        val ranges = listOfNotNull(
            formatNumberRange(years.orEmpty(), R.string.advanced_filter_label_release_year, resources),
            formatPercentRange(ratings.orEmpty(), "Trakt", resources, locale),
            formatNumberRange(runtimes.orEmpty(), R.string.advanced_filter_label_runtime, resources),
            formatPlainRange(imdbRatings.orEmpty(), resources)?.let { "IMDb $it" },
            formatPercentRange(rtMeters.orEmpty(), "RT", resources, locale),
            formatPercentRange(rtUserMeters.orEmpty(), "RT Audience", resources, locale),
        )

        val flags = listOfNotNull(
            resources.getString(R.string.header_hide_watched).takeIf { ignoreWatched },
            resources.getString(R.string.header_hide_watchlisted).takeIf { ignoreWatchlisted },
        )

        return values + ranges + flags
    }

    /**
     * Clears every filter field back to its default, keeping only [media] and [source].
     * Used when switching between simple and advanced filter modes.
     */
    fun cleared(): SmartListFilters =
        SmartListFilters(
            media = media,
            source = source,
        )

    companion object {
        val Default = SmartListFilters(
            media = MediaMode.Shows,
            source = SmartListSource.Trending,
        )

        fun fromDto(dto: SmartListDto): SmartListFilters {
            val filters = dto.filters
            return SmartListFilters(
                source = SmartListSource.fromApiValue(dto.source),
                media = when (dto.mediaType) {
                    "movies" -> MediaMode.Movies
                    else -> MediaMode.Shows
                },
                genres = filters.genres.orEmpty()
                    .mapNotNull { MediaGenre.fromSlug(it) }
                    .toImmutableList(),
                subgenres = filters.subgenres.orEmpty().toImmutableList(),
                certifications = filters.certifications.orEmpty().toImmutableList(),
                languages = filters.languages.orEmpty().toImmutableList(),
                countries = filters.countries.orEmpty().toImmutableList(),
                statuses = filters.statuses.orEmpty().toImmutableList(),
                networks = filters.networks.orEmpty().toImmutableList(),
                availability = filters.watchnow.orEmpty()
                    .mapNotNull { GlobalFilter.Availability.fromSlug(it) }
                    .toImmutableList(),
                years = filters.years.orEmpty().toImmutableList(),
                ratings = filters.ratings.orEmpty().toImmutableList(),
                runtimes = filters.runtimes.orEmpty().toImmutableList(),
                imdbRatings = filters.imdbRatings.orEmpty().toImmutableList(),
                rtMeters = filters.rtMeters.orEmpty().toImmutableList(),
                rtUserMeters = filters.rtUserMeters.orEmpty().toImmutableList(),
                ignoreWatched = filters.ignoreWatched ?: false,
                ignoreWatchlisted = filters.ignoreWatchlisted ?: false,
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
