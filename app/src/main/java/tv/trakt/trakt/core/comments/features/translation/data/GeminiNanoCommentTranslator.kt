package tv.trakt.trakt.core.comments.features.translation.data

import android.content.Context
import com.google.mlkit.genai.common.DownloadStatus.DownloadCompleted
import com.google.mlkit.genai.common.DownloadStatus.DownloadFailed
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Candidate
import com.google.mlkit.genai.prompt.GenerateContentRequest
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.SystemInstruction
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import tv.trakt.trakt.common.helpers.extensions.isOnMeteredNetwork
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import java.util.Locale

private const val MAX_OUTPUT_TOKENS = 2048

/**
 * Translates comments with Gemini Nano through the ML Kit GenAI Prompt API.
 * Available on devices that support Gemini Nano; the model is downloaded on demand through AICore.
 */
internal class GeminiNanoCommentTranslator(
    private val context: Context,
) : CommentTranslator {
    private val model by lazy { Generation.getClient() }

    // Gemini Nano runs one inference at a time; parallel requests fail with BUSY.
    private val inferenceLock = Mutex()

    private var supported = false
    private var downloaded = false

    override suspend fun isAvailable(): Boolean {
        if (supported) return true

        supported = status() != FeatureStatus.UNAVAILABLE
        return supported
    }

    override suspend fun isDownloaded(source: Locale): Boolean {
        if (downloaded) return true

        downloaded = status() == FeatureStatus.AVAILABLE
        return downloaded
    }

    override fun isOnMeteredNetwork(): Boolean {
        return context.isOnMeteredNetwork()
    }

    override suspend fun download(source: Locale): Result<Unit> {
        return try {
            val status = model.download().first { it is DownloadCompleted || it is DownloadFailed }
            if (status is DownloadFailed) {
                Timber.d(status.e, "Gemini Nano download failed")
                return Result.failure(status.e)
            }

            downloaded = true
            Result.success(Unit)
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.d(error, "Gemini Nano download failed")
            }
            Result.failure(error)
        }
    }

    override suspend fun translate(
        text: String,
        source: Locale,
    ): Result<String> {
        return inferenceLock.withLock {
            try {
                val candidate = model.generateContent(buildRequest(text, source))
                    .candidates
                    .firstOrNull()
                val translation = candidate?.text?.trim()

                when {
                    translation.isNullOrBlank() -> {
                        Result.failure(IllegalStateException("Empty translation"))
                    }
                    candidate?.finishReason == Candidate.FinishReason.MAX_TOKENS -> {
                        Result.failure(IllegalStateException("Translation was truncated"))
                    }
                    else -> {
                        Result.success(translation)
                    }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.d(error, "Gemini Nano translation failed")
                }
                Result.failure(error)
            }
        }
    }

    private suspend fun status(): Int {
        return try {
            model.checkStatus()
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.d(error, "Gemini Nano status check failed")
            }
            FeatureStatus.UNAVAILABLE
        }
    }

    private suspend fun buildRequest(
        text: String,
        source: Locale,
    ): GenerateContentRequest {
        val instruction = translationInstruction(source = source, target = appLocale())
        val config: GenerateContentRequest.Builder.() -> Unit = {
            temperature = 0F
            topK = 1
            maxOutputTokens = MAX_OUTPUT_TOKENS
        }

        return when {
            model.isSystemPromptAvailable() -> generateContentRequest(
                SystemInstruction(instruction),
                TextPart(text),
                config,
            )
            else -> generateContentRequest(
                TextPart("$instruction\n\n$text"),
                config,
            )
        }
    }
}

private fun translationInstruction(
    source: Locale,
    target: Locale,
): String {
    val sourceLanguage = source.getDisplayName(Locale.ENGLISH)
    val targetLanguage = target.getDisplayName(Locale.ENGLISH)
    return "Translate the user's text from $sourceLanguage into $targetLanguage. " +
        "Keep the original meaning, tone, Markdown formatting, links, @mentions, emoji and line breaks. " +
        "Keep titles of movies and shows, and names of people, unchanged. " +
        "Reply with the translated text only, without notes, explanations or quotes."
}
