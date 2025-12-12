package tv.trakt.trakt.ui.theme.model

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import kotlinx.serialization.Serializable
import tv.trakt.trakt.ui.theme.colors.DarkColors
import tv.trakt.trakt.ui.theme.colors.TraktColors

@Serializable
data class CustomTheme(
    val id: String,
    val type: String,
    val backgroundImageUrl: String?,
    val colors: Colors?,
    val filters: Filters?,
) {
    @Serializable
    data class Colors(
        val accent: String?,
        val primaryButtonContainer: String?,
        val sentimentsContainer: String?,
        val sentimentsAccent: String?,
        val detailsStatus1: String?,
        val detailsStatus2: String?,
        val vipAccent: String?,
    )

    @Serializable
    data class Filters(
        val shows: DiscoverFilters?,
        val movies: DiscoverFilters?,
    )

    @Serializable
    data class DiscoverFilters(
        val trending: FilterOptions?,
        val recommended: FilterOptions?,
        val anticipated: FilterOptions?,
        val popular: FilterOptions?,
    )

    @Serializable
    data class FilterOptions(
        val genres: List<String>?,
        val subgenres: List<String>?,
        val years: FilterYears?,
    )

    @Serializable
    data class FilterYears(
        val from: Int,
        val to: Int,
    ) {
        override fun toString(): String {
            return "$from-$to"
        }
    }
}

internal fun CustomTheme.Colors.toTraktDarkColors(): TraktColors {
    return DarkColors.copy(
        accent = accent?.let { Color(it.toColorInt()) }
            ?: DarkColors.accent,
        primaryButtonContainer = primaryButtonContainer?.let { Color(it.toColorInt()) }
            ?: DarkColors.primaryButtonContainer,
        sentimentsContainer = sentimentsContainer?.let { Color(it.toColorInt()) }
            ?: DarkColors.sentimentsContainer,
        sentimentsAccent = sentimentsAccent?.let { Color(it.toColorInt()) }
            ?: DarkColors.sentimentsAccent,
        detailsStatus1 = detailsStatus1?.let { Color(it.toColorInt()) }
            ?: DarkColors.detailsStatus1,
        detailsStatus2 = detailsStatus2?.let { Color(it.toColorInt()) }
            ?: DarkColors.detailsStatus2,
        vipAccent = vipAccent?.let { Color(it.toColorInt()) }
            ?: DarkColors.vipAccent,
    )
}
