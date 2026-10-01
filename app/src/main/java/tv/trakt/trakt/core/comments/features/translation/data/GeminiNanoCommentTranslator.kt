package tv.trakt.trakt.core.comments.features.translation.data

import androidx.appcompat.app.AppCompatDelegate
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Candidate
import com.google.mlkit.genai.prompt.GenerateContentRequest
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.SystemInstruction
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import java.util.Locale

private const val MAX_OUTPUT_TOKENS = 2048

/**
 * Translates comments with Gemini Nano through the ML Kit GenAI Prompt API.
 * Only reports availability when the model is already on the device; it never triggers a model download.
 */
internal class GeminiNanoCommentTranslator : CommentTranslator {
    private val model by lazy { Generation.getClient() }

    // Gemini Nano runs one inference at a time; parallel requests fail with BUSY.
    private val inferenceLock = Mutex()

    private var available = false

    override suspend fun isAvailable(): Boolean {
        if (available) return true

        return try {
            available = model.checkStatus() == FeatureStatus.AVAILABLE
            available
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.d(error, "Gemini Nano status check failed")
            }
            false
        }
    }

    override suspend fun translate(text: String): Result<String> {
        return inferenceLock.withLock {
            try {
                val candidate = model.generateContent(buildRequest(text))
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

    private suspend fun buildRequest(text: String): GenerateContentRequest {
        val instruction = translationInstruction(appLocale())
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

private fun appLocale(): Locale {
    return AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
}

private fun translationInstruction(target: Locale): String {
    val language = target.getDisplayName(Locale.ENGLISH)
    return "Translate the user's text into $language. " +
        "Keep the original meaning, tone, Markdown formatting, links, @mentions, emoji and line breaks. " +
        "Keep titles of movies and shows, and names of people, unchanged. " +
        "Reply with the translated text only, without notes, explanations or quotes."
}
