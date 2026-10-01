package tv.trakt.trakt.core.comments.features.translation.data

internal interface CommentTranslator {
    suspend fun isAvailable(): Boolean

    /**
     * Translates [text] into the app language.
     */
    suspend fun translate(text: String): Result<String>
}
