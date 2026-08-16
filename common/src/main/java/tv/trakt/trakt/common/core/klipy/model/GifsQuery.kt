package tv.trakt.trakt.common.core.klipy.model

import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import tv.trakt.trakt.common.model.pagination.Pagination
import java.util.Locale

/** KLIPY caps a page at 50 items and defaults to 24; search additionally wants at least 8. */
const val GIFS_MIN_PER_PAGE = 8
const val GIFS_MAX_PER_PAGE = 50
const val GIFS_DEFAULT_PER_PAGE = 24

/**
 * Every knob the KLIPY GIF endpoints accept, in domain terms.
 *
 * @param customerId stable per-user identifier - KLIPY uses it for recents, dedupe and ads.
 *   Null keeps the request anonymous.
 */
data class GifsQuery(
    val term: String? = null,
    val pagination: Pagination = Pagination(
        page = 1,
        limit = GIFS_DEFAULT_PER_PAGE,
    ),
    val customerId: String? = null,
    val locale: Locale = Locale.US,
    val contentFilter: GifContentFilter = GifContentFilter.Off,
    val formats: ImmutableSet<GifFormat> = persistentSetOf(GifFormat.Webp, GifFormat.Gif),
) {
    init {
        require(pagination.limit <= GIFS_MAX_PER_PAGE) {
            "KLIPY allows at most $GIFS_MAX_PER_PAGE items per page."
        }
    }
}

enum class GifContentFilter(
    val wireValue: String,
) {
    Off("off"),
    Low("low"),
    Medium("medium"),
    High("high"),
}

enum class GifFormat(
    val wireValue: String,
) {
    Gif("gif"),
    Webp("webp"),
    Jpg("jpg"),
    Mp4("mp4"),
    Webm("webm"),
}
