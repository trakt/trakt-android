package tv.trakt.trakt.core.summary.episodes.features.context.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.summary.episodes.usecases.GetEpisodeStreamingUseCase

internal class EpisodeDetailsContextViewModel(
    private val show: Show,
    private val episode: Episode,
    private val sessionManager: SessionManager,
    private val getStreamingsUseCase: GetEpisodeStreamingUseCase,
) : ViewModel() {
    private val initialState = EpisodeDetailsContextState()

    private val streamingsState = MutableStateFlow(initialState.streamings)
    private val userState = MutableStateFlow(initialState.user)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val user = sessionManager.getProfile()
                .also { user ->
                    userState.update { user }
                }

            if (user == null) {
                streamingsState.update {
                    it.copy(
                        service = null,
                        noServices = true,
                    )
                }
                return@launch
            }

            try {
                streamingsState.update { it.copy(loading = true) }

                val streamingService = getStreamingsUseCase.getStreamingService(
                    user = user,
                    show = show,
                    episode = episode,
                )

                streamingsState.update {
                    it.copy(
                        slug = show.ids.slug,
                        service = streamingService.streamingService,
                        noServices = streamingService.noServices,
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                }
            } finally {
                streamingsState.update { it.copy(loading = false) }
            }
        }
    }

    val state: StateFlow<EpisodeDetailsContextState> = combine(
        userState,
        streamingsState,
        errorState,
    ) { s1, s2, s3 ->
        EpisodeDetailsContextState(
            user = s1,
            streamings = s2,
            error = s3,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
