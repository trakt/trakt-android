package tv.trakt.trakt.core.lists.sections.smart.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.auth.session.helpers.distinctUntilSessionChanged
import tv.trakt.trakt.common.data.local.ListsSmartLocalSource
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.lists.SmartList
import tv.trakt.trakt.common.model.lists.SmartListTemplate
import tv.trakt.trakt.core.filters.data.GlobalFilterManager
import tv.trakt.trakt.core.lists.sections.smart.templates.model.visibleSmartListTemplates
import tv.trakt.trakt.core.lists.sections.smart.templates.usecases.GetSmartListTemplatesUseCase
import tv.trakt.trakt.core.lists.sections.smart.templates.usecases.SmartListTemplatesHiddenUseCase
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@Suppress("UNCHECKED_CAST")
internal class SmartListTemplatesViewModel(
    private val sessionManager: SessionManager,
    private val getTemplatesUseCase: GetSmartListTemplatesUseCase,
    private val hiddenUseCase: SmartListTemplatesHiddenUseCase,
    private val localSmartListsSource: ListsSmartLocalSource,
    private val filterManager: GlobalFilterManager,
) : ViewModel() {
    private val initialState = SmartListTemplatesState()

    private val templatesState = MutableStateFlow(initialState.templates)
    private val listsState = MutableStateFlow<ImmutableList<SmartList>>(EmptyImmutableList)

    private var dataJob: Job? = null
    private var listsJob: Job? = null

    init {
        observeUser()
        observeLists()
    }

    private fun observeUser() {
        sessionManager.observeProfile()
            .distinctUntilSessionChanged()
            .onEach { user ->
                when (user) {
                    null -> clearData()
                    else -> loadData()
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeLists() {
        localSmartListsSource.observeUpdates()
            .debounce(200.milliseconds)
            .onEach {
                if (sessionManager.isAuthenticated()) {
                    loadLists()
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadData() {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            try {
                coroutineScope {
                    val lists = async { getTemplatesUseCase.getUserSmartLists() }
                    val templates = async { getTemplatesUseCase.getTemplates() }

                    listsState.update { lists.await() }
                    templatesState.update { templates.await() }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    templatesState.update { EmptyImmutableList }
                    Timber.recordError(error)
                }
            }
        }
    }

    private fun loadLists() {
        listsJob?.cancel()
        listsJob = viewModelScope.launch {
            try {
                listsState.update { getTemplatesUseCase.getUserSmartLists() }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    private fun clearData() {
        dataJob?.cancel()
        listsJob?.cancel()
        templatesState.update { null }
        listsState.update { EmptyImmutableList }
    }

    fun hide() {
        viewModelScope.launch {
            hiddenUseCase.hide()
        }
    }

    val state = combine(
        templatesState,
        listsState,
        filterManager.observeFilter()
            .map { it.mode }
            .onStart { emit(filterManager.getFilter().mode) }
            .distinctUntilChanged(),
        hiddenUseCase.observeHidden(),
    ) { state ->
        val templates = state[0] as ImmutableList<SmartListTemplate>?
        SmartListTemplatesState(
            templates = templates?.let {
                visibleSmartListTemplates(
                    templates = it,
                    lists = state[1] as ImmutableList<SmartList>,
                    mode = state[2] as MediaMode,
                )
            },
            hidden = state[3] as Boolean,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
