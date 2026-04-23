package tv.trakt.trakt.common.core.tutorials

import tv.trakt.trakt.common.core.tutorials.model.TutorialKey

class StubTutorialsManager : TutorialsManager {
    override suspend fun get(key: TutorialKey): Boolean {
        return false
    }

    override suspend fun acknowledge(key: TutorialKey) = Unit
}
