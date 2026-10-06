package tv.trakt.trakt.common.model.globalfilter

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.helpers.serializers.ImmutableListSerializer
import tv.trakt.trakt.common.model.MediaGenre
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.MediaStatus
import tv.trakt.trakt.resources.R

@Immutable
@Serializable
data class GlobalFilter(
    val mode: MediaMode,
    @Serializable(with = ImmutableListSerializer::class)
    val genre: ImmutableList<MediaGenre>? = null,
    @Serializable(with = ImmutableListSerializer::class)
    val subgenre: ImmutableList<String>? = null,
    val years: Pair<Int, Int>? = null,
    val runtime: Pair<Int, Int>? = null,
    @Serializable(with = ImmutableListSerializer::class)
    val availability: ImmutableList<Availability>? = null,
    @Serializable(with = ImmutableListSerializer::class)
    val certification: ImmutableList<Certification>? = null,
    val region: Region? = null,
    @Serializable(with = ImmutableListSerializer::class)
    val countries: ImmutableList<String>? = null,
    @Serializable(with = ImmutableListSerializer::class)
    val statuses: ImmutableList<MediaStatus>? = null,
    val rating: Pair<Int, Int>? = null,
    val hideWatched: Boolean = false,
    val hideWatchlist: Boolean = false,
) {
    companion object {
        val Default = GlobalFilter(
            mode = MediaMode.Media,
        )

        val FilterableStatuses = persistentListOf(
            MediaStatus.ReturningSeries,
            MediaStatus.Released,
            MediaStatus.Continuing,
            MediaStatus.Upcoming,
            MediaStatus.InProduction,
            MediaStatus.PostProduction,
            MediaStatus.Planned,
            MediaStatus.Ended,
            MediaStatus.Canceled,
            MediaStatus.Rumored,
        )
    }

    init {
        require(years == null || years.first == 0 || (years.first >= 1930 && years.second <= 2040)) {
            "Years must be between 1930 and 2040"
        }

        require(runtime == null || (runtime.first >= 0 && runtime.second <= 500)) {
            "Runtime must be between 0 and 500 minutes"
        }

        require(rating == null || (rating.first >= 0 && rating.second <= 100)) {
            "Rating must be between 0 and 100"
        }
    }

    enum class Availability(
        val slug: String,
        @param:StringRes val displayStringRes: Int,
    ) {
        MyFavorites("favorites", R.string.option_text_my_favorites),
        StreamingNow("subscriptions", R.string.option_text_streaming_now),
        AllDigitalReleases("any", R.string.option_text_all_digital_releases),
        ;

        companion object {
            fun fromSlug(slug: String): Availability? {
                return entries.firstOrNull { it.slug == slug }
            }
        }
    }

    enum class Certification(
        val slug: String,
        @param:StringRes val displayStringRes: Int,
    ) {
        ParentalGuidance("pg,tv-pg", R.string.option_text_certification_parental_guidance),
        Teens("pg-13,tv-14", R.string.option_text_certification_teens),
        Mature("r,tv-ma", R.string.option_text_certification_mature),
        Unrated("nr", R.string.option_text_certification_unrated),
    }

    enum class Region(
        val slug: String,
        @param:StringRes val displayStringRes: Int,
    ) {
        NorthAmerica("us,ca", R.string.option_text_north_america),
        Europe(
            "gb,fr,de,it,es,dk,ie,se,be,ru,no,pl,su,nl,at,fi,ro,gr,rs,hu,is,yu,ch,bg,cz,ee,ua",
            R.string.option_text_europe,
        ),
        Asia("in,jp,kr,hk,cn,th,tw,bd,id", R.string.option_text_asia),
        MiddleEast("tr,ir,il,eg", R.string.option_text_middle_east),
        Oceania("au,nz", R.string.option_text_oceania),
        LatinAmerica("mx,br,ar,cl,co", R.string.option_text_latin_america),
        Africa("za", R.string.option_text_africa),
        ;

        companion object {
            // Defunct codes kept in slugs for the API; ICU resolves them to
            // successor states (SU -> Russia, YU -> Serbia) and duplicates entries.
            private val defunctCountryCodes = setOf("su", "yu")

            // Country-only locales: passing the code as language triggers legacy
            // ISO 639 remapping (e.g. "in" -> "id").
            val AllLocales = entries
                .flatMap { it.slug.split(',') }
                .filterNot { it in defunctCountryCodes }
                .map { code -> java.util.Locale("", code) }
                .filter { it.country.isNotEmpty() }
                .distinctBy { it.country }
                .sortedBy { it.displayCountry }
        }
    }

    enum class StatusGroup(
        val statuses: ImmutableList<MediaStatus>,
        @param:StringRes val displayStringRes: Int,
    ) {
        Released(
            persistentListOf(MediaStatus.ReturningSeries, MediaStatus.Continuing, MediaStatus.Released),
            R.string.translated_value_status_released,
        ),
        Upcoming(
            persistentListOf(
                MediaStatus.Upcoming,
                MediaStatus.InProduction,
                MediaStatus.Planned,
                MediaStatus.PostProduction,
            ),
            R.string.translated_value_status_upcoming,
        ),
        Ended(persistentListOf(MediaStatus.Ended), R.string.translated_value_status_ended),
        Canceled(persistentListOf(MediaStatus.Canceled), R.string.translated_value_status_canceled),
        Rumored(persistentListOf(MediaStatus.Rumored), R.string.translated_value_status_rumored),
        ;

        companion object {
            fun fromStatuses(statuses: List<MediaStatus>?): StatusGroup? {
                if (statuses.isNullOrEmpty()) return null
                return entries.firstOrNull { it.statuses.toSet() == statuses.toSet() }
            }
        }
    }

    val isActive: Boolean
        get() = genre != null ||
            subgenre != null ||
            years != null ||
            runtime != null ||
            availability != null ||
            certification != null ||
            region != null ||
            countries != null ||
            statuses != null ||
            (rating != null && rating != 0 to 100) ||
            hideWatched ||
            hideWatchlist

    val isAdvancedActive: Boolean
        get() = genre != null ||
            subgenre != null ||
            years != null ||
            runtime != null ||
            availability != null ||
            certification != null ||
            region != null ||
            countries != null ||
            statuses != null ||
            (rating != null && rating != 0 to 100)
}
