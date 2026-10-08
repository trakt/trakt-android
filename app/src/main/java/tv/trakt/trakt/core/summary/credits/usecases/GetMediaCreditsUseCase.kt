package tv.trakt.trakt.core.summary.credits.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.model.CastPerson
import tv.trakt.trakt.common.model.CrewPerson
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.networking.CastMemberDto
import tv.trakt.trakt.common.networking.CrewMemberDto
import tv.trakt.trakt.core.episodes.data.remote.EpisodesRemoteDataSource
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.core.people.data.local.PeopleLocalDataSource
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.core.summary.credits.model.CreditsSource
import tv.trakt.trakt.core.summary.credits.model.MediaCredits

private val CREW_DEPARTMENTS = listOf("created by", "directing", "writing")

internal class GetMediaCreditsUseCase(
    private val moviesSource: MoviesRemoteDataSource,
    private val showsSource: ShowsRemoteDataSource,
    private val episodesSource: EpisodesRemoteDataSource,
    private val peopleLocalSource: PeopleLocalDataSource,
) {
    suspend fun getCredits(source: CreditsSource): MediaCredits {
        val castCrew = when (source) {
            is CreditsSource.Movie -> moviesSource.getCastCrew(source.movieId)
            is CreditsSource.Show -> showsSource.getCastCrew(
                showId = source.showId,
                guestStars = true,
            )
            is CreditsSource.Episode -> episodesSource.getCastCrew(
                showId = source.showId,
                season = source.season,
                episode = source.episode,
                guestStars = true,
            )
        }

        // Episode counts are only meaningful across a whole show.
        val withEpisodes = source is CreditsSource.Show

        val main = (castCrew.cast ?: emptyList()).toCastPeople(withEpisodes)
        val supporting = (castCrew.guestStars ?: emptyList()).toCastPeople(withEpisodes)
        val crew = CREW_DEPARTMENTS
            .flatMap { castCrew.crew?.get(it) ?: emptyList() }
            .toCrewPeople(withEpisodes)

        peopleLocalSource.upsertPeople(
            (main + supporting).map { it.person } + crew.map { it.person },
        )

        val isSplit = main.isNotEmpty() && supporting.isNotEmpty()
        return MediaCredits(
            main = if (isSplit) main else (main + supporting).toImmutableList(),
            supporting = if (isSplit) supporting else EmptyImmutableList,
            crew = crew,
        )
    }

    private fun List<CastMemberDto>.toCastPeople(withEpisodes: Boolean): ImmutableList<CastPerson> {
        return distinctBy { it.person.ids.trakt }
            .map { member ->
                CastPerson(
                    person = Person.fromDto(member.person),
                    characters = member.characters,
                    episodesCount = if (withEpisodes) member.episodeCount ?: 0 else 0,
                )
            }
            .toImmutableList()
    }

    private fun List<CrewMemberDto>.toCrewPeople(withEpisodes: Boolean): ImmutableList<CrewPerson> {
        return groupBy { it.person.ids.trakt }
            .map { (_, roles) ->
                CrewPerson(
                    person = Person.fromDto(roles.first().person),
                    jobs = roles
                        .flatMap { it.jobs }
                        .distinct()
                        .toImmutableList(),
                    episodesCount = if (withEpisodes) roles.maxOf { it.episodeCount ?: 0 } else 0,
                )
            }
            .toImmutableList()
    }
}
