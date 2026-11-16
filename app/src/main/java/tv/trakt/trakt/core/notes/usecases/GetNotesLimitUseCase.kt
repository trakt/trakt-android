package tv.trakt.trakt.core.notes.usecases

import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.networking.api.v3.V3Api

internal class GetNotesLimitUseCase(
    private val v3Api: V3Api,
    private val sessionManager: SessionManager,
) {
    suspend fun getLimit(): NotesLimit {
        val isVip = sessionManager.getProfile()?.isVip == true
        val usage = v3Api.getUsage().totalNotes
        val max = (if (isVip) usage.vip else usage.free) ?: return NotesLimit(reached = false, isVip = isVip)

        return NotesLimit(
            reached = (usage.current ?: 0) >= max,
            isVip = isVip,
        )
    }

    data class NotesLimit(
        val reached: Boolean,
        val isVip: Boolean,
    )
}
