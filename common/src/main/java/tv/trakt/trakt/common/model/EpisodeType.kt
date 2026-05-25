package tv.trakt.trakt.common.model

import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

enum class EpisodeType(
    val value: String,
    @param:StringRes val stringRes: Int,
) {
    SERIES_PREMIERE("series_premiere", R.string.tag_text_series_premiere),
    SERIES_FINALE("series_finale", R.string.tag_text_series_finale),
    SEASON_PREMIERE("season_premiere", R.string.tag_text_season_premiere),
    SEASON_FINALE("season_finale", R.string.tag_text_season_finale),
    MID_SEASON_PREMIERE("mid_season_premiere", R.string.tag_text_mid_season_premiere),
    MID_SEASON_FINALE("mid_season_finale", R.string.tag_text_mid_season_finale),
    ;

    val isPremiere: Boolean
        get() = this == SERIES_PREMIERE || this == SEASON_PREMIERE

    val isFinale: Boolean
        get() = this == SERIES_FINALE || this == SEASON_FINALE

    companion object {
        fun fromValue(value: String): EpisodeType? = entries.find { it.value == value }
    }
}
