package tv.trakt.trakt.core.summary.movies.features.reactions.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.api.v3.V3Api
import tv.trakt.trakt.core.reactions.media.MediaReactionEmoji
import tv.trakt.trakt.core.reactions.media.data.MediaReaction
import kotlin.time.Duration.Companion.milliseconds

internal class GetMovieReactionsUseCase(
    @Suppress("unused")
    private val remoteSource: V3Api,
) {
    suspend fun getReactions(movieId: TraktId): ImmutableList<MediaReaction> {
        // TODO: map remoteSource.getReactionsSummary(MediaType.Movie, movieId) into MediaReaction items
        //  once the summary payload shape is confirmed. Stubbed with fake latency and data.
        delay(600.milliseconds)

        return persistentListOf(
            MediaReaction(id = 1, count = 18, emoji = MediaReactionEmoji.HeartEyes),
            MediaReaction(id = 2, count = 33, emoji = MediaReactionEmoji.Popcorn),
            MediaReaction(id = 3, count = 55, emoji = MediaReactionEmoji.RollingOnTheFloorLaughing),
            MediaReaction(id = 4, count = 87, emoji = MediaReactionEmoji.Skull),
            MediaReaction(id = 5, count = 12, emoji = MediaReactionEmoji.Sob),
            MediaReaction(id = 6, count = 2048, emoji = MediaReactionEmoji.Fire),
            MediaReaction(id = 7, count = 512, emoji = MediaReactionEmoji.ExplodingHead),
            MediaReaction(id = 8, count = 256, emoji = MediaReactionEmoji.PartyingFace),
            MediaReaction(id = 9, count = 199, emoji = MediaReactionEmoji.ThumbsUp),
            MediaReaction(id = 10, count = 64, emoji = MediaReactionEmoji.Scream),
            MediaReaction(id = 11, count = 33, emoji = MediaReactionEmoji.ThinkingFace),
            MediaReaction(id = 12, count = 21, emoji = MediaReactionEmoji.Clap),
            MediaReaction(id = 13, count = 8, emoji = MediaReactionEmoji.YawningFace),
            MediaReaction(id = 14, count = 3, emoji = MediaReactionEmoji.FaceVomiting),
            MediaReaction(id = 15, count = 1, emoji = MediaReactionEmoji.NerdFace),
        )
    }
}
