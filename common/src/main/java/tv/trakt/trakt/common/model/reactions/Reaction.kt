package tv.trakt.trakt.common.model.reactions

enum class Reaction(
    val emoji: String,
    val value: String,
) {
    LIKE("👍", "like"),
    DISLIKE("👎", "dislike"),
    LOVE("❤️", "love"),
    LAUGH("😂", "laugh"),
    SHOCKED("😮", "shocked"),
    BRAVO("👏", "bravo"),
    SPOILER("🫣", "spoiler"),
}
