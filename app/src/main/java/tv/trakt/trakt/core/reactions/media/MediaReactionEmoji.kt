package tv.trakt.trakt.core.reactions.media

internal enum class MediaReactionEmoji(
    val slug: String,
    val emoji: String,
) {
    // Love & celebration
    HeartEyes("heart_eyes", "😍"),
    Heart("heart", "❤️"),
    FaceHoldingBackTears("face_holding_back_tears", "🥹"),
    PartyingFace("partying_face", "🥳"),

    // Laughter
    RollingOnTheFloorLaughing("rolling_on_the_floor_laughing", "🤣"),
    SmilingFaceWithTear("smiling_face_with_tear", "🥲"),

    // Sadness
    Sob("sob", "😭"),
    BrokenHeart("broken_heart", "💔"),

    // Shock & fear
    ExplodingHead("exploding_head", "🤯"),
    Scream("scream", "😱"),
    Flushed("flushed", "😳"),
    ColdSweat("cold_sweat", "😰"),
    FaceWithPeekingEye("face_with_peeking_eye", "🫣"),
    Grimacing("grimacing", "😬"),

    // Anger & disgust
    FaceWithSymbolsOnMouth("face_with_symbols_on_mouth", "🤬"),
    FaceVomiting("face_vomiting", "🤮"),
    FaceWithRollingEyes("face_with_rolling_eyes", "🙄"),

    // Bored & drained
    NeutralFace("neutral_face", "😐"),
    Weary("weary", "😩"),
    YawningFace("yawning_face", "🥱"),
    WoozyFace("woozy_face", "🥴"),
    ColdFace("cold_face", "🥶"),
    MeltingFace("melting_face", "🫠"),

    // Thinking & curiosity
    ThinkingFace("thinking_face", "🤔"),
    FaceWithMonocle("face_with_monocle", "🧐"),
    NerdFace("nerd_face", "🤓"),
    DisguisedFace("disguised_face", "🥸"),
    ShushingFace("shushing_face", "🤫"),

    // Gestures
    ThumbsUp("+1", "👍"),
    ThumbsDown("-1", "👎"),
    Clap("clap", "👏"),
    TheHorns("the_horns", "🤘"),
    PinchedFingers("pinched_fingers", "🤌"),

    // Objects
    Fire("fire", "🔥"),
    Skull("skull", "💀"),
    Popcorn("popcorn", "🍿"),
    ;

    companion object {
        fun fromSlug(slug: String): MediaReactionEmoji? = entries.firstOrNull { it.slug == slug }
    }
}
