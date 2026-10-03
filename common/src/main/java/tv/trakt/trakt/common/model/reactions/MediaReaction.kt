package tv.trakt.trakt.common.model.reactions

enum class MediaReaction(
    val value: String,
    val emoji: String,
) {
    HeartEyes("heart_eyes", "😍"),
    Love("love", "❤️"),
    Rofl("rofl", "🤣"),
    HoldingBackTears("holding_back_tears", "🥹"),
    Partying("partying", "🥳"),
    MindBlown("mind_blown", "🤯"),
    Cursing("cursing", "🤬"),
    Weary("weary", "😩"),
    Woozy("woozy", "🥴"),
    Yawning("yawning", "🥱"),
    Spoiler("spoiler", "🫣"),
    Shushing("shushing", "🤫"),
    SmilingTear("smiling_tear", "🥲"),
    Neutral("neutral", "😐"),
    Shocked("shocked", "😱"),
    Anxious("anxious", "😰"),
    Thinking("thinking", "🤔"),
    Flushed("flushed", "😳"),
    Melting("melting", "🫠"),
    Grimacing("grimacing", "😬"),
    Vomiting("vomiting", "🤮"),
    EyeRoll("eye_roll", "🙄"),
    Like("like", "👍"),
    RockOn("rock_on", "🤘"),
    Dislike("dislike", "👎"),
    Bravo("bravo", "👏"),
    Skull("skull", "💀"),
    Popcorn("popcorn", "🍿"),
    Fire("fire", "🔥"),
    Crying("crying", "😭"),
    PinchedFingers("pinched_fingers", "🤌"),
    BrokenHeart("broken_heart", "💔"),
    Freezing("freezing", "🥶"),
    Disguised("disguised", "🥸"),
    Nerd("nerd", "🤓"),
    Monocle("monocle", "🧐"),
    ;

    companion object {
        /** Server-side cap of reactions one user can hold on one item. */
        const val MAX_PER_MEDIA = 3

        fun fromValue(value: String): MediaReaction? {
            return entries.firstOrNull { it.value == value }
        }
    }
}
