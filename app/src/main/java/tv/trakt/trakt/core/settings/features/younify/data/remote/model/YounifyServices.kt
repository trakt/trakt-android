package tv.trakt.trakt.core.settings.features.younify.data.remote.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable

@Serializable
internal data class YounifyServices(
    val available: Available,
//    val linked: Linked,
) {
    @Serializable
    internal data class Available(
        val watched: ImmutableList<YounifyService>,
//        val ratings: ImmutableList<YounifyService>,
    )

    @Serializable
    internal data class Linked(
        val active: ImmutableList<YounifyService>,
        val inactive: ImmutableList<YounifyService>,
    )
}

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
