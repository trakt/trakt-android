package tv.trakt.trakt.core.comments.features.translation.data

import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

internal interface CommentTranslator {
    suspend fun isAvailable(): Boolean

    /**
     * Returns true when everything needed to translate from [source] is already on the device.
     */
    suspend fun isDownloaded(source: Locale): Boolean

    /**
     * Returns true when a download would go over a metered network, such as mobile data.
     */
    fun isOnMeteredNetwork(): Boolean

    /**
     * Downloads what is needed to translate from [source] into the app language.
     */
    suspend fun download(source: Locale): Result<Unit>

    /**
     * Translates [text] written in [source] into the app language.
     */
    suspend fun translate(
        text: String,
        source: Locale,
    ): Result<String>
}

internal fun appLocale(): Locale {
    return AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
}
