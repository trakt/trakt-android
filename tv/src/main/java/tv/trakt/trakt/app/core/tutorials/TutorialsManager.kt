package tv.trakt.trakt.app.core.tutorials

import tv.trakt.trakt.app.core.tutorials.model.TutorialKey

internal interface TutorialsManager {
    /**
     * Checks if the tutorial with the given key has been acknowledged.
     * If it returns `true`, the tutorial has been completed and will not be shown again.
     */
    suspend fun get(key: TutorialKey): Boolean

    /**
     * Acknowledges the tutorial with the given key, marking it as completed.
     * This will prevent the tutorial from being shown again in the future.
     */
    suspend fun acknowledge(key: TutorialKey)
}
