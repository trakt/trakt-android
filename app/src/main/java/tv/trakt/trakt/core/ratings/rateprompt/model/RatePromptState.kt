package tv.trakt.trakt.core.ratings.rateprompt.model

import kotlinx.collections.immutable.ImmutableList

sealed interface RatePromptState {
    object Idle : RatePromptState

    object Loading : RatePromptState

    object AskSuppress : RatePromptState

    data class UnratedMedia(
        val media: ImmutableList<RatePromptMedia>,
    ) : RatePromptState

    fun isActive(): Boolean {
        return when {
            this is UnratedMedia -> true
            else -> false
        }
    }
}
