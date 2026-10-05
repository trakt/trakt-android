package tv.trakt.trakt.core.comments.features.translation.data

import android.content.Context
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import tv.trakt.trakt.common.helpers.extensions.isOnMeteredNetwork
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.comments.features.translation.model.CommentTranslationDownload
import java.util.Locale

internal class MlKitCommentTranslator(
    private val context: Context,
) : CommentTranslator {
    override val downloadType = CommentTranslationDownload.Language

    private val modelManager by lazy { RemoteModelManager.getInstance() }

    private val downloadConditions = DownloadConditions.Builder().build()

    // Only the last used language pair is kept open, so loaded models do not pile up in memory.
    private val translatorLock = Mutex()
    private var translator: Pair<LanguagePair, Translator>? = null

    override suspend fun isAvailable(): Boolean {
        return true
    }

    override suspend fun isDownloaded(source: Locale): Boolean {
        val languages = languagePair(source) ?: return false

        return try {
            listOf(languages.source, languages.target).all { language ->
                val model = TranslateRemoteModel.Builder(language).build()
                modelManager.isModelDownloaded(model).await()
            }
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.d(error, "ML Kit model status check failed")
            }
            false
        }
    }

    override fun isOnMeteredNetwork(): Boolean {
        return context.isOnMeteredNetwork()
    }

    // ML Kit Translation does not report download progress.
    override suspend fun download(
        source: Locale,
        onProgress: (Int) -> Unit,
    ): Result<Unit> {
        val languages = languagePair(source)
            ?: return Result.failure(IllegalArgumentException("Unsupported language ${source.language}"))

        return translatorLock.withLock {
            try {
                translatorFor(languages)
                    .downloadModelIfNeeded(downloadConditions)
                    .await()
                Result.success(Unit)
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.d(error, "ML Kit model download failed")
                }
                Result.failure(error)
            }
        }
    }

    override suspend fun translate(
        text: String,
        source: Locale,
    ): Result<String> {
        val languages = languagePair(source)
            ?: return Result.failure(IllegalArgumentException("Unsupported language ${source.language}"))

        return translatorLock.withLock {
            try {
                val client = translatorFor(languages)
                client.downloadModelIfNeeded(downloadConditions).await()

                val translation = client.translate(text).await()?.trim()
                when {
                    translation.isNullOrBlank() -> Result.failure(IllegalStateException("Empty translation"))
                    else -> Result.success(translation)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.d(error, "ML Kit translation failed")
                }
                Result.failure(error)
            }
        }
    }

    private fun translatorFor(languages: LanguagePair): Translator {
        val current = translator
        if (current != null && current.first == languages) {
            return current.second
        }

        current?.second?.close()

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(languages.source)
            .setTargetLanguage(languages.target)
            .build()

        return Translation.getClient(options).also {
            translator = languages to it
        }
    }
}

private data class LanguagePair(
    val source: String,
    val target: String,
)

private fun languagePair(source: Locale): LanguagePair? {
    val sourceLanguage = TranslateLanguage.fromLanguageTag(source.language) ?: return null
    val targetLanguage = TranslateLanguage.fromLanguageTag(appLocale().language) ?: return null
    if (sourceLanguage == targetLanguage) return null

    return LanguagePair(
        source = sourceLanguage,
        target = targetLanguage,
    )
}
