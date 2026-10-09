package tv.trakt.trakt.core.applinks.model

internal enum class ShareArrivalResult(
    val value: String,
) {
    Recorded("recorded"),
    Duplicate("duplicate"),
    Rejected("rejected"),
    Anonymous("anonymous"),
    Uncredited("uncredited"),
    Failed("failed"),
    ;

    companion object {
        fun fromApi(value: String): ShareArrivalResult? {
            return listOf(Recorded, Duplicate, Rejected).firstOrNull { it.value == value }
        }
    }
}
