package tv.trakt.trakt.core.summary.shows.features.actors.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.local.PeopleLocalSource
import tv.trakt.trakt.common.data.remote.ShowsRemoteSource
import tv.trakt.trakt.common.model.CastPerson
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.featureflags.model.FeatureFlag
import tv.trakt.trakt.core.featureflags.usecases.FeatureFlagsUseCase

internal class GetShowActorsUseCase(
    private val remoteSource: ShowsRemoteSource,
    private val peopleLocalSource: PeopleLocalSource,
    private val featureFlagsUseCase: FeatureFlagsUseCase,
) {
    suspend fun getCastCrew(showId: TraktId): ImmutableList<CastPerson> {
        val portraits = featureFlagsUseCase.isEnabled(FeatureFlag.CharacterPortraits)

        val castCrew = remoteSource.getCastCrew(showId)

        val cast = (castCrew.cast ?: emptyList())
            .distinctBy { it.person.ids.trakt }
            .take(30)
            .map { person ->
                CastPerson(
                    characters = person.characters,
                    person = Person.fromDto(person.person),
                    episodesCount = person.episodeCount ?: 0,
                    portraitUrl = Images.portraitUrl(
                        mediaType = MediaType.Show,
                        mediaId = showId,
                        personId = person.person.ids.trakt.toTraktId(),
                    ).takeIf { portraits },
                )
            }

        peopleLocalSource.upsertPeople(cast.map { it.person })

        return cast.toImmutableList()
    }
}
