package tv.trakt.trakt.common.model

import androidx.annotation.Keep
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import tv.trakt.trakt.resources.R

@Keep
@Immutable
@Serializable
enum class MediaGenre(
    val slug: String,
    @param:StringRes val displayStringRes: Int,
) {
    Action("action", R.string.translated_value_genre_action),
    Adventure("adventure", R.string.translated_value_genre_adventure),
    Animation("animation", R.string.translated_value_genre_animation),
    Anime("anime", R.string.translated_value_genre_anime),
    Biography("biography", R.string.translated_value_genre_biography),
    Children("children", R.string.translated_value_genre_children),
    Comedy("comedy", R.string.translated_value_genre_comedy),
    Crime("crime", R.string.translated_value_genre_crime),
    Documentary("documentary", R.string.translated_value_genre_documentary),
    Donghua("donghua", R.string.translated_value_genre_donghua),
    Drama("drama", R.string.translated_value_genre_drama),
    Family("family", R.string.translated_value_genre_family),
    Fantasy("fantasy", R.string.translated_value_genre_fantasy),
    GameShow("game-show", R.string.translated_value_genre_game_show),
    History("history", R.string.translated_value_genre_history),
    Holiday("holiday", R.string.translated_value_genre_holiday),
    HomeAndGarden("home-and-garden", R.string.translated_value_genre_home_and_garden),
    Horror("horror", R.string.translated_value_genre_horror),
    MiniSeries("mini-series", R.string.translated_value_genre_mini_series),
    Music("music", R.string.translated_value_genre_music),
    Musical("musical", R.string.translated_value_genre_musical),
    Mystery("mystery", R.string.translated_value_genre_mystery),
    News("news", R.string.translated_value_genre_news),
    None("none", R.string.translated_value_genre_none),
    Reality("reality", R.string.translated_value_genre_reality),
    Romance("romance", R.string.translated_value_genre_romance),
    ScienceFiction("science-fiction", R.string.translated_value_genre_science_fiction),
    Short("short", R.string.translated_value_genre_short),
    Soap("soap", R.string.translated_value_genre_soap),
    SpecialInterest("special-interest", R.string.translated_value_genre_special_interest),
    SportingEvent("sporting-event", R.string.translated_value_genre_sporting_event),
    Superhero("superhero", R.string.translated_value_genre_superhero),
    Suspense("suspense", R.string.translated_value_genre_suspense),
    TalkShow("talk-show", R.string.translated_value_genre_talk_show),
    Thriller("thriller", R.string.translated_value_genre_thriller),
    War("war", R.string.translated_value_genre_war),
    Western("western", R.string.translated_value_genre_western),
    ;

    companion object {
fun fromSlug(value: String?): MediaGenre? = entries.firstOrNull { it.slug.equals(value, ignoreCase = true) }
    }
}
