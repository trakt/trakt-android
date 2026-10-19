package tv.trakt.trakt.common.core.sync.changes

data class DataChange(
    val kinds: Set<Kind>,
    val origin: ChangeOrigin? = null,
    val targetId: Int? = null,
) {
    enum class Kind {
        History,
        Progress,
        Watchlist,
        Lists,
        ListOrder,
        Ratings,
        Favorites,
        CheckIn,
        Comments,
        Reactions,
        Social,
    }
}
