package tv.trakt.trakt.app.core.home.sections.social.viewall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.app.core.home.HomeConfig.HOME_SOCIAL_PAGE_LIMIT
import tv.trakt.trakt.app.core.home.sections.social.usecases.GetSocialActivityUseCase
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation

internal class SocialViewAllViewModel(
    private val getSocialActivityUseCase: GetSocialActivityUseCase,
) : ViewModel() {
    private val initialState = SocialViewAllState()

    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val itemsState = MutableStateFlow(initialState.items)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
    }

    private fun loadData() {
        if (loadingState.value) {
            return
        }
        viewModelScope.launch {
            try {
                itemsState.update { null }
                loadingState.update { true }

                itemsState.update {
                    getSocialActivityUseCase.getSocialActivity(
                        limit = HOME_SOCIAL_PAGE_LIMIT,
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.w(error, "Failed to load data")
                }
            } finally {
                loadingState.update { false }
            }
        }
    }

    val state = combine(
        loadingState,
        itemsState,
        errorState,
    ) { s1, s2, s3 ->
        SocialViewAllState(
            isLoading = s1,
            items = s2,
            error = s3,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
