package tv.trakt.trakt.common.firebase.analytics.implementation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private data class Tracked(
    val name: String,
    val dims: Map<String, String>,
    val metrics: Map<String, Int>,
)

private class RecordingEvents : HalEvents {
    val tracked = mutableListOf<Tracked>()
    var lastUserType: String? = null

    override fun track(
        name: String,
        dims: Map<String, String>,
        metrics: Map<String, Int>,
    ) {
        tracked += Tracked(name, dims, metrics)
    }

    override fun setUserType(userType: String?) {
        lastUserType = userType
    }
}

class HalAnalyticsTest {
    private val events = RecordingEvents()
    private val crashlyticsUserIds = mutableListOf<String>()
    private val analytics = HalAnalytics(
        events = events,
        setCrashlyticsUserId = { crashlyticsUserIds += it },
        reactions = HalAnalyticsReactions(events),
        ratings = HalAnalyticsRatings(events),
        comments = HalAnalyticsComments(events),
        progress = HalAnalyticsProgress(events),
        trivia = HalAnalyticsTrivia(),
        playback = HalAnalyticsPlayback(),
    )

    @Test
    fun `screen view becomes a page_view route template`() {
        analytics.logScreenView("movie_details")

        assertEquals(
            listOf(Tracked("page_view", mapOf("route" to "/movie_details"), emptyMap())),
            events.tracked,
        )
    }

    @Test
    fun `route keeps only characters the collector accepts`() {
        assertEquals("/all_discover_trending", halRoute("all_discover_Trending"))
        assertEquals("/a_b_c", halRoute("a b?c"))
        assertEquals("/home", halRoute("home"))
    }

    @Test
    fun `media mode click maps to discover-mode`() {
        analytics.logMediaModeClick("Movies")
        analytics.logMediaModeClick("Shows")
        analytics.logMediaModeClick("Media")

        assertEquals(
            listOf("movie", "show", "media"),
            events.tracked.map { it.dims["mode"] },
        )
        assertTrue(events.tracked.all { it.name == "discover-mode" })
        assertTrue(events.tracked.all { it.dims["source"] == "media-mode-toggle" })
    }

    @Test
    fun `unknown media mode is not sent`() {
        analytics.logMediaModeClick("Calendar")

        assertTrue(events.tracked.isEmpty())
    }

    @Test
    fun `passive media mode is not sent`() {
        analytics.logMediaMode("Media")

        assertTrue(events.tracked.isEmpty())
    }

    @Test
    fun `share arrival maps type and outcome`() {
        analytics.logShareArrival(type = "Show", result = "Recorded")

        assertEquals(
            listOf(Tracked("share.arrival", mapOf("type" to "show", "outcome" to "recorded"), emptyMap())),
            events.tracked,
        )
    }

    @Test
    fun `comment reactions map to action react`() {
        analytics.reactions.logReactionAdd(reaction = "love", source = "comment")
        analytics.reactions.logReactionRemove(source = "comment")

        assertEquals(
            listOf(
                Tracked("action.react", mapOf("action" to "add", "type" to "comment"), emptyMap()),
                Tracked("action.react", mapOf("action" to "remove", "type" to "comment"), emptyMap()),
            ),
            events.tracked,
        )
    }

    @Test
    fun `media reactions carry the media type`() {
        analytics.reactions.logMediaReactionAdd(reaction = "love", mediaType = "Episode")
        analytics.reactions.logMediaReactionRemove(reaction = "love", mediaType = "season")

        assertEquals(
            listOf(
                Tracked("action.react", mapOf("action" to "add", "type" to "episode"), emptyMap()),
                Tracked("action.react", mapOf("action" to "remove", "type" to "season"), emptyMap()),
            ),
            events.tracked,
        )
    }

    @Test
    fun `rating add sends the value as dim and metric`() {
        analytics.ratings.logRatingAdd(rating = 8, mediaType = "movie")

        assertEquals(
            listOf(
                Tracked(
                    "media-action.rate",
                    mapOf("action" to "added", "rating" to "8"),
                    mapOf("rating" to 8),
                ),
            ),
            events.tracked,
        )
    }

    @Test
    fun `rating remove sends no value`() {
        analytics.ratings.logRatingRemove(mediaType = "movie")

        assertEquals(
            listOf(Tracked("media-action.rate", mapOf("action" to "removed"), emptyMap())),
            events.tracked,
        )
    }

    @Test
    fun `comments and replies map to add-comment and delete-comment`() {
        analytics.comments.logCommentAdd(mediaType = "movie")
        analytics.comments.logReplyAdd()
        analytics.comments.logCommentRemove()
        analytics.comments.logReplyRemove()

        assertEquals(
            listOf(
                Tracked("media-action.add-comment", mapOf("action" to "post", "type" to "text"), emptyMap()),
                Tracked("media-action.add-comment", mapOf("action" to "reply", "type" to "text"), emptyMap()),
                Tracked("media-action.delete-comment", emptyMap(), emptyMap()),
                Tracked("media-action.delete-comment", emptyMap(), emptyMap()),
            ),
            events.tracked,
        )
    }

    @Test
    fun `progress maps to mark-as-watched and watchlist`() {
        analytics.progress.logAddWatchedMedia(mediaType = "movie", source = "home", date = null)
        analytics.progress.logRemoveWatchedMedia(mediaType = "movie", source = "home")
        analytics.progress.logAddWatchlistMedia(mediaType = "show", source = "home")
        analytics.progress.logRemoveWatchlistMedia(mediaType = "show", source = "home")

        assertEquals(
            listOf(
                Tracked("media-action.mark-as-watched", mapOf("action" to "add"), emptyMap()),
                Tracked("media-action.mark-as-watched", mapOf("action" to "remove"), emptyMap()),
                Tracked("media-action.watchlist", mapOf("action" to "add"), emptyMap()),
                Tracked("media-action.watchlist", mapOf("action" to "remove"), emptyMap()),
            ),
            events.tracked,
        )
    }

    @Test
    fun `events without a collector contract are dropped`() {
        analytics.logUserLogin("deeplink")
        analytics.logUserLogout()
        analytics.ratings.logFavoriteAdd("movie")
        analytics.ratings.logFavoriteRemove("movie", "profile")
        analytics.comments.logCommentTranslate(120)
        analytics.trivia.logScreenView("movie")
        analytics.playback.logPlaybackStart("movie")
        analytics.playback.logPlaybackStop("movie")
        analytics.setUserProperty("director", "true")

        assertTrue(events.tracked.isEmpty())
        assertNull(events.lastUserType)
    }

    @Test
    fun `vip property drives user type`() {
        analytics.setUserProperty("vip", "true")
        assertEquals("vip", events.lastUserType)

        analytics.setUserProperty("vip", "false")
        assertEquals("standard", events.lastUserType)

        analytics.setUserProperty("vip", null)
        assertEquals("anonymous", events.lastUserType)
    }

    @Test
    fun `user id only reaches crashlytics`() {
        analytics.setUserId("42")
        analytics.setUserId(null)

        assertEquals(listOf("42"), crashlyticsUserIds)
        assertTrue(events.tracked.isEmpty())
        assertNull(events.lastUserType)
    }
}
