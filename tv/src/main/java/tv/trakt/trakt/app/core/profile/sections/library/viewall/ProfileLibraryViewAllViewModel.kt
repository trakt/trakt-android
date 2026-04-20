package tv.trakt.trakt.app.core.profile.sections.library.viewall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.plus
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.app.core.profile.ProfileConfig.LIBRARY_ALL_PAGE_LIMIT
import tv.trakt.trakt.app.core.profile.sections.library.usecases.GetProfileLibraryUseCase
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation

internal class ProfileLibraryViewAllViewModel(
    private val getLibraryUseCase: GetProfileLibraryUseCase,
) : ViewModel() {
    private val initialState = ProfileLibraryViewAllState()

    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val loadingPageState = MutableStateFlow(initialState.isLoadingPage)
    private val itemsState = MutableStateFlow(initialState.items)
    private val errorState = MutableStateFlow(initialState.error)

    private var nextDataPage: Int = 1
    private var hasMoreData: Boolean = true

    init {
        loadData()
    }

    private fun loadData() {
        if (loadingState.value || loadingPageState.value) {
            return
        }
        viewModelScope.launch {
            try {
                nextDataPage = 1
                itemsState.update { null }
                loadingState.update { true }

                itemsState.update {
                    getLibraryUseCase.getLibrary(
                        limit = LIBRARY_ALL_PAGE_LIMIT,
                        availableOn = "plex",
                    )
                }
                nextDataPage += 1
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                }
            } finally {
                loadingState.update { false }
            }
        }
    }

    fun loadNextDataPage() {
        if (loadingPageState.value || !hasMoreData) {
            return
        }
        viewModelScope.launch {
            try {
                loadingPageState.update { true }

                val items = getLibraryUseCase.getLibrary(
                    page = nextDataPage,
                    limit = LIBRARY_ALL_PAGE_LIMIT,
                    availableOn = "plex",
                )

                itemsState.update {
                    it?.toPersistentList()?.plus(items)
                }

                hasMoreData = (items.size >= LIBRARY_ALL_PAGE_LIMIT)
                nextDataPage += 1
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                }
            } finally {
                loadingPageState.update { false }
            }
        }
    }

    val state = combine(
        loadingState,
        loadingPageState,
        itemsState,
        errorState,
    ) { s1, s2, s3, s4 ->
        ProfileLibraryViewAllState(
            isLoading = s1,
            isLoadingPage = s2,
            items = s3,
            error = s4,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
