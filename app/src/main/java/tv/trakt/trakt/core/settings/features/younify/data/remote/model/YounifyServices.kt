package tv.trakt.trakt.core.settings.features.younify.data.remote.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable

@Serializable
internal data class YounifyServices(
    val available: ImmutableList<YounifyService>,
    val linked: ImmutableList<YounifyService>,
)

@Serializable
internal data class YounifyService(
    val id: String,
    val name: String,
    val color: String,
    val images: Images,
) {
    @Serializable
    internal data class Images(
        val logo: String,
    )
}
