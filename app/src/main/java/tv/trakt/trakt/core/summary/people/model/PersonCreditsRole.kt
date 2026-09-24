package tv.trakt.trakt.core.summary.people.model

internal enum class PersonCreditsRole(
    val filterKey: String,
) {
    Acting("acting"),
    CreatedBy("created by"),
    Directing("directing"),
    Writing("writing"),
}
