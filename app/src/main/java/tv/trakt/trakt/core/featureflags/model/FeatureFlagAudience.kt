package tv.trakt.trakt.core.featureflags.model

internal enum class FeatureFlagAudience(
    val remoteValue: String,
) {
    Off("off"),
    Director("director"),
    Vip("vip"),
    Public("public"),
    ;

    companion object {
        fun fromRemoteValue(value: String): FeatureFlagAudience {
            val normalized = value.trim().lowercase()
            return entries.firstOrNull { it.remoteValue == normalized } ?: Off
        }
    }
}
