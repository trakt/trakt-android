package tv.trakt.trakt.core.summary.credits.model

import tv.trakt.trakt.resources.R

internal enum class CreditsMode(
    val displayRes: Int,
    val iconRes: Int,
) {
    Main(R.string.header_main_cast, R.drawable.ic_cast),
    Supporting(R.string.header_supporting_cast, R.drawable.ic_cast),
    Crew(R.string.drawer_meta_info_crew, R.drawable.ic_crew),
}
