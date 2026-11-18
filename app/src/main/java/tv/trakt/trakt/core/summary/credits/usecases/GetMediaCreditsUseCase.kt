package tv.trakt.trakt.core.summary.credits.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.local.PeopleLocalSource
import tv.trakt.trakt.common.data.remote.EpisodesRemoteSource
import tv.trakt.trakt.common.data.remote.MoviesRemoteSource
import tv.trakt.trakt.common.data.remote.ShowsRemoteSource
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.model.CastPerson
import tv.trakt.trakt.common.model.CrewPerson
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.common.networking.CastMemberDto
import tv.trakt.trakt.common.networking.CrewMemberDto
import tv.trakt.trakt.core.featureflags.model.FeatureFlag
import tv.trakt.trakt.core.featureflags.usecases.FeatureFlagsUseCase
import tv.trakt.trakt.core.summary.credits.model.CreditsSource
import tv.trakt.trakt.core.summary.credits.model.MediaCredits

private val CREW_DEPARTMENTS = listOf("created by", "directing", "writing")

internal class GetMediaCreditsUseCase(
    private val moviesSource: MoviesRemoteSource,
    private val showsSource: ShowsRemoteSource,
    private val episodesSource: EpisodesRemoteSource,
    private val peopleLocalSource: PeopleLocalSource,
    private val featureFlagsUseCase: FeatureFlagsUseCase,
) {
    suspend fun getCredits(source: CreditsSource): MediaCredits {
        val portraits = featureFlagsUseCase.isEnabled(FeatureFlag.CharacterPortraits)

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

        val portraitOf = source.portraitOf().takeIf { portraits }

        val main = (castCrew.cast ?: emptyList()).toCastPeople(withEpisodes, portraitOf)
        val supporting = (castCrew.guestStars ?: emptyList()).toCastPeople(withEpisodes, portraitOf)
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

    private fun CreditsSource.portraitOf(): Pair<MediaType, TraktId> {
        return when (this) {
            is CreditsSource.Movie -> MediaType.Movie to movieId
            is CreditsSource.Show -> MediaType.Show to showId
            is CreditsSource.Episode -> MediaType.Show to showId
        }
    }

    private fun List<CastMemberDto>.toCastPeople(
        withEpisodes: Boolean,
        portraitOf: Pair<MediaType, TraktId>?,
    ): ImmutableList<CastPerson> {
        return distinctBy { it.person.ids.trakt }
            .map { member ->
                CastPerson(
                    person = Person.fromDto(member.person),
                    characters = member.characters,
                    episodesCount = if (withEpisodes) member.episodeCount ?: 0 else 0,
                    portraitUrl = portraitOf?.let { (mediaType, mediaId) ->
                        Images.portraitUrl(
                            mediaType = mediaType,
                            mediaId = mediaId,
                            personId = member.person.ids.trakt.toTraktId(),
                        )
                    },
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
