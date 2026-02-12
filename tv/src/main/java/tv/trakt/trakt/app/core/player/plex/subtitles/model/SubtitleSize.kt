package tv.trakt.trakt.app.core.player.plex.subtitles.model

enum class SubtitleSize(
    val scale: Float,
) {
    SMALL(0.5f),
    SMALLER(0.75f),
    DEFAULT(1f),
    BIGGER(1.25f),
    BIG(1.5f),
}
