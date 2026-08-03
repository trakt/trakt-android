package tv.trakt.trakt.app.core.lists.filters

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.globalfilter.GlobalFilterDecade
import tv.trakt.trakt.common.model.globalfilter.GlobalFilterRuntime
import java.time.Year

@Immutable
internal data class TvListFilterConfiguration(
    val allowedMediaModes: ImmutableList<MediaMode>,
    val showHideWatched: Boolean,
    val showHideWatchlisted: Boolean,
) {
    init {
        require(allowedMediaModes.isNotEmpty()) {
            "At least one media mode must be allowed"
        }
    }

    val defaultFilter: GlobalFilter
        get() = GlobalFilter.Default.copy(mode = allowedMediaModes.first())

    fun normalize(filter: GlobalFilter): GlobalFilter {
        val mediaMode = filter.mode.takeIf(allowedMediaModes::contains)
            ?: allowedMediaModes.first()

        return filter.copy(
            mode = mediaMode,
            subgenre = null,
            hideWatched = filter.hideWatched && showHideWatched,
            hideWatchlist = filter.hideWatchlist && showHideWatchlisted,
        )
    }

    fun reset(filter: GlobalFilter): GlobalFilter {
        return defaultFilter.copy(
            mode = normalize(filter).mode,
        )
    }

    fun hasSimpleIncompatibleValues(filter: GlobalFilter): Boolean {
        val normalized = normalize(filter)
        val currentYear = Year.now().value

        return normalized.genre.hasMultipleSelections() ||
            normalized.years?.let { years ->
                years != (currentYear to currentYear) &&
                    GlobalFilterDecade.entries.none { it.years == years }
            } == true ||
            normalized.runtime?.let { runtime ->
                GlobalFilterRuntime.entries.none { it.runtime == runtime }
            } == true ||
            normalized.availability.hasMultipleSelections() ||
            normalized.certification.hasMultipleSelections() ||
            !normalized.countries.isNullOrEmpty() ||
            normalized.rating?.let { rating ->
                rating.first % SIMPLE_RATING_STEP != 0 ||
                    rating.second % SIMPLE_RATING_STEP != 0
            } == true
    }

    fun toSimple(filter: GlobalFilter): GlobalFilter {
        val normalized = normalize(filter)
        val currentYear = Year.now().value

        return normalized.copy(
            genre = normalized.genre.takeSingleSelection(),
            years = when (normalized.years) {
                currentYear to currentYear -> GlobalFilterDecade.CurrentYear.years
                else -> normalized.years?.takeIf { years ->
                    GlobalFilterDecade.entries.any { it.years == years }
                }
            },
            runtime = normalized.runtime?.takeIf { runtime ->
                GlobalFilterRuntime.entries.any { it.runtime == runtime }
            },
            availability = normalized.availability.takeSingleSelection(),
            certification = normalized.certification.takeSingleSelection(),
            countries = null,
            rating = normalized.rating?.takeIf { rating ->
                rating.first % SIMPLE_RATING_STEP == 0 &&
                    rating.second % SIMPLE_RATING_STEP == 0
            },
        )
    }

    companion object {
        val MoviesWatchlist = TvListFilterConfiguration(
            allowedMediaModes = persistentListOf(MediaMode.Movies),
            showHideWatched = true,
            showHideWatchlisted = false,
        )

        val ShowsWatchlist = TvListFilterConfiguration(
            allowedMediaModes = persistentListOf(MediaMode.Shows),
            showHideWatched = true,
            showHideWatchlisted = false,
        )

        val MixedList = TvListFilterConfiguration(
            allowedMediaModes = persistentListOf(
                MediaMode.Media,
                MediaMode.Shows,
                MediaMode.Movies,
            ),
            showHideWatched = true,
            showHideWatchlisted = true,
        )

        val MoviesList = TvListFilterConfiguration(
            allowedMediaModes = persistentListOf(MediaMode.Movies),
            showHideWatched = true,
            showHideWatchlisted = true,
        )

        val ShowsList = TvListFilterConfiguration(
            allowedMediaModes = persistentListOf(MediaMode.Shows),
            showHideWatched = true,
            showHideWatchlisted = true,
        )

        private const val SIMPLE_RATING_STEP = 5
    }
}

private fun <T> ImmutableList<T>?.takeSingleSelection(): ImmutableList<T>? {
    return this?.takeIf { it.size == 1 }
}

private fun ImmutableList<*>?.hasMultipleSelections(): Boolean {
    return this != null && size > 1
}
