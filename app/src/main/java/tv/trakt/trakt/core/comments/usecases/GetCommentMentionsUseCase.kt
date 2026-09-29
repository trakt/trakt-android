package tv.trakt.trakt.core.comments.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.Config.WEB_V3_BASE_URL
import tv.trakt.trakt.core.comments.model.CommentMention
import tv.trakt.trakt.core.comments.model.MentionSource
import tv.trakt.trakt.core.episodes.data.remote.EpisodesRemoteDataSource
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource

internal class GetCommentMentionsUseCase(
    private val moviesSource: MoviesRemoteDataSource,
    private val showsSource: ShowsRemoteDataSource,
    private val episodesSource: EpisodesRemoteDataSource,
) {
    suspend fun getMentions(source: MentionSource): ImmutableList<CommentMention> {
        val castCrew = when (source) {
            is MentionSource.Movie -> moviesSource.getCastCrew(source.movieId)
            is MentionSource.Show -> showsSource.getCastCrew(source.showId)
            is MentionSource.Episode -> episodesSource.getCastCrew(
                showId = source.showId,
                season = source.season,
                episode = source.episode,
            )
        }

        return (castCrew.cast ?: emptyList())
            .distinctBy { it.person.ids.trakt }
            .map { member ->
                CommentMention(
                    name = member.person.name,
                    href = "${WEB_V3_BASE_URL}people/${member.person.ids.slug}",
                    detail = member.characters.firstOrNull { it.isNotBlank() },
                )
            }
            .toImmutableList()
    }
}
