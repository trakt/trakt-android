package tv.trakt.trakt.common.model.reactions

enum class Reaction(
    val emoji: String,
) {
    LIKE("👍"),
    DISLIKE("👎"),
    LOVE("❤️"),
    LAUGH("😂"),
    SHOCKED("😮"),
    BRAVO("👏"),
    SPOILER("🫣"),
}
