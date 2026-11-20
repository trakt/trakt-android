package tv.trakt.trakt.common.model.lists

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.openapitools.client.models.GetAwardsCatalog200ResponseInner.MediaTypes
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.networking.AwardCeremonyDto

private const val AWARD_PREFIX_END = " for "

@Immutable
data class AwardCeremony(
    val slug: String,
    val name: String,
    val forMovies: Boolean,
    val forShows: Boolean,
    val awards: ImmutableList<Award>,
) {
    @Immutable
    data class Award(
        val slug: String,
        val name: String,
    )

    fun supports(media: MediaMode): Boolean {
        return when (media) {
            MediaMode.Media -> true
            MediaMode.Movies -> forMovies
            MediaMode.Shows -> forShows
        }
    }

    companion object {
        fun fromDto(dto: AwardCeremonyDto): AwardCeremony {
            return AwardCeremony(
                slug = dto.slug,
                name = dto.name,
                forMovies = MediaTypes.MOVIE in dto.mediaTypes,
                forShows = MediaTypes.SHOW in dto.mediaTypes,
                awards = dto.awards
                    .map { Award(slug = it.slug, name = it.name) }
                    .withShortNames()
                    .toImmutableList(),
            )
        }
    }
}

/** Drops the name prefix shared by every award of a ceremony, up to " for ", e.g. "Academy Award for ". */
private fun List<AwardCeremony.Award>.withShortNames(): List<AwardCeremony.Award> {
    if (size < 2) return this

    val shared = map { it.name }.reduce { prefix, name -> prefix.commonPrefixWith(name) }
    val end = shared.lastIndexOf(AWARD_PREFIX_END)
    if (end == -1) return this

    val cut = end + AWARD_PREFIX_END.length
    return map { it.copy(name = it.name.substring(cut)) }
}
