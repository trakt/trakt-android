package tv.trakt.trakt.core.summary.sentiment.model

import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey

internal enum class RealityCheckSection(
    val collapsingKey: CollapsingKey,
) {
    RealStory(CollapsingKey.REALITY_CHECK_REAL_STORY),
    Claims(CollapsingKey.REALITY_CHECK_CLAIMS),
    People(CollapsingKey.REALITY_CHECK_PEOPLE),
    Timeline(CollapsingKey.REALITY_CHECK_TIMELINE),
    LeftOut(CollapsingKey.REALITY_CHECK_LEFT_OUT),
}
