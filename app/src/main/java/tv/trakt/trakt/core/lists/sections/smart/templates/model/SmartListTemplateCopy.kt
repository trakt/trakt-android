package tv.trakt.trakt.core.lists.sections.smart.templates.model

import android.content.res.Resources
import androidx.annotation.StringRes
import tv.trakt.trakt.common.model.lists.SmartListFilters
import tv.trakt.trakt.resources.R
import java.util.Locale

private typealias Showcase = (SmartListFilters) -> SmartListFilters

private val votes: Showcase = { it.cleared().copy(votes = it.votes) }
private val statuses: Showcase = { it.cleared().copy(statuses = it.statuses) }
private val releasedWithinDays: Showcase = { it.cleared().copy(releasedWithinDays = it.releasedWithinDays) }
private val runtimes: Showcase = { it.cleared().copy(runtimes = it.runtimes) }
private val keywords: Showcase = { it.cleared().copy(keywords = it.keywords) }
private val studios: Showcase = { it.cleared().copy(studios = it.studios) }
private val people: Showcase = { it.cleared().copy(people = it.people) }
private val networks: Showcase = { it.cleared().copy(networks = it.networks) }
private val countries: Showcase = {
    it.cleared().copy(countries = it.countries, excludedCountries = it.excludedCountries)
}
private val genres: Showcase = { it.cleared().copy(genres = it.genres, excludedGenres = it.excludedGenres) }
private val ignoreWatched: Showcase = { it.cleared().copy(ignoreWatched = it.ignoreWatched) }
private val ignoreWatchlisted: Showcase = { it.cleared().copy(ignoreWatchlisted = it.ignoreWatchlisted) }
private val parentalFrightening: Showcase = { it.cleared().copy(parentalFrightening = it.parentalFrightening) }

internal enum class SmartListTemplateCopy(
    val id: String,
    @param:StringRes val nameRes: Int,
    @param:StringRes val pitchRes: Int,
    private val showcase: Showcase?,
) {
    HiddenGems(
        "hidden-gems",
        R.string.smart_list_template_hidden_gems_name,
        R.string.smart_list_template_hidden_gems_pitch,
        votes,
    ),
    CriticsDarlings(
        "critics-darlings",
        R.string.smart_list_template_critics_darlings_name,
        R.string.smart_list_template_critics_darlings_pitch,
        null,
    ),
    FinishedStories(
        "finished-stories",
        R.string.smart_list_template_finished_stories_name,
        R.string.smart_list_template_finished_stories_pitch,
        statuses,
    ),
    StillRunning(
        "still-running",
        R.string.smart_list_template_still_running_name,
        R.string.smart_list_template_still_running_pitch,
        statuses,
    ),
    BestOfTheYear(
        "best-of-the-year",
        R.string.smart_list_template_best_of_the_year_name,
        R.string.smart_list_template_best_of_the_year_pitch,
        releasedWithinDays,
    ),
    NewShowsWorthIt(
        "new-shows-worth-it",
        R.string.smart_list_template_new_shows_worth_it_name,
        R.string.smart_list_template_new_shows_worth_it_pitch,
        releasedWithinDays,
    ),
    ShortAndSweet(
        "short-and-sweet",
        R.string.smart_list_template_short_and_sweet_name,
        R.string.smart_list_template_short_and_sweet_pitch,
        runtimes,
    ),
    TimeTwisters(
        "time-twisters",
        R.string.smart_list_template_time_twisters_name,
        R.string.smart_list_template_time_twisters_pitch,
        keywords,
    ),
    A24AfterDark(
        "a24-after-dark",
        R.string.smart_list_template_a24_after_dark_name,
        R.string.smart_list_template_a24_after_dark_pitch,
        studios,
    ),
    A24Essentials(
        "a24-essentials",
        R.string.smart_list_template_a24_essentials_name,
        R.string.smart_list_template_a24_essentials_pitch,
        studios,
    ),
    TheAuteurs(
        "the-auteurs",
        R.string.smart_list_template_the_auteurs_name,
        R.string.smart_list_template_the_auteurs_pitch,
        people,
    ),
    HboPrestige(
        "hbo-prestige",
        R.string.smart_list_template_hbo_prestige_name,
        R.string.smart_list_template_hbo_prestige_pitch,
        networks,
    ),
    KdramaFever(
        "kdrama-fever",
        R.string.smart_list_template_kdrama_fever_name,
        R.string.smart_list_template_kdrama_fever_pitch,
        countries,
    ),
    BeyondTheStars(
        "beyond-the-stars",
        R.string.smart_list_template_beyond_the_stars_name,
        R.string.smart_list_template_beyond_the_stars_pitch,
        genres,
    ),
    Whodunits(
        "whodunits",
        R.string.smart_list_template_whodunits_name,
        R.string.smart_list_template_whodunits_pitch,
        genres,
    ),
    JustLandedServices(
        "just-landed-services",
        R.string.smart_list_template_just_landed_services_name,
        R.string.smart_list_template_just_landed_services_pitch,
        releasedWithinDays,
    ),
    HaventSeenIt(
        "havent-seen-it",
        R.string.smart_list_template_havent_seen_it_name,
        R.string.smart_list_template_havent_seen_it_pitch,
        ignoreWatched,
    ),
    LittleOnes(
        "little-ones",
        R.string.smart_list_template_little_ones_name,
        R.string.smart_list_template_little_ones_pitch,
        parentalFrightening,
    ),
    NextObsession(
        "next-obsession",
        R.string.smart_list_template_next_obsession_name,
        R.string.smart_list_template_next_obsession_pitch,
        ignoreWatchlisted,
    ),
    ;

    fun toChipLabels(
        filters: SmartListFilters,
        resources: Resources,
        locale: Locale,
    ): List<String> {
        val labels = filters.toSummaryParts(resources, locale)
        val showcaseLabel = showcase
            ?.invoke(filters)
            ?.toSummaryParts(resources, locale)
            ?.firstOrNull()
            ?: return labels

        val showcaseIndex = labels.indexOf(showcaseLabel)
        if (showcaseIndex < 0) return labels

        return listOf(showcaseLabel) + labels.filterIndexed { index, _ -> index != showcaseIndex }
    }

    companion object {
        fun fromId(id: String): SmartListTemplateCopy? {
            return entries.firstOrNull { it.id == id }
        }
    }
}
