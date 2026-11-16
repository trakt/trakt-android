package tv.trakt.trakt.core.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.NoteType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.UserNote
import tv.trakt.trakt.core.notes.NotesState.NoteEditor
import tv.trakt.trakt.core.notes.usecases.DeleteNoteUseCase
import tv.trakt.trakt.core.notes.usecases.EditNoteUseCase
import tv.trakt.trakt.core.notes.usecases.GetMediaNotesUseCase
import tv.trakt.trakt.core.notes.usecases.GetNotesLimitUseCase
import tv.trakt.trakt.core.notes.usecases.PostNoteUseCase

@Suppress("UNCHECKED_CAST")
internal class NotesViewModel(
    private val mediaType: MediaType,
    private val mediaId: TraktId,
    private val getNotesUseCase: GetMediaNotesUseCase,
    private val getNotesLimitUseCase: GetNotesLimitUseCase,
    private val postNoteUseCase: PostNoteUseCase,
    private val editNoteUseCase: EditNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val sessionManager: SessionManager,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = NotesState()

    private val notesState = MutableStateFlow(initialState.notes)
    private val editorState = MutableStateFlow(initialState.editor)
    private val loadingEditorState = MutableStateFlow(initialState.loadingEditor)
    private val loadingDeleteState = MutableStateFlow(initialState.loadingDelete)
    private val errorState = MutableStateFlow(initialState.error)

    private var job: Job? = null

    init {
        loadNotes()
    }

    private fun loadNotes() {
        viewModelScope.launch {
            refreshNotes()
        }
    }

    private suspend fun refreshNotes() {
        try {
            notesState.update {
                getNotesUseCase.getNotes(mediaType, mediaId)
            }
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
        }
    }

    fun addNote(type: NoteType) {
        if (editorState.value != null || job?.isActive == true) {
            return
        }
        if (notesState.value.orEmpty().any { it.type == type }) {
            return
        }

        job = viewModelScope.launch {
            try {
                if (!sessionManager.isAuthenticated()) {
                    return@launch
                }

                val limit = getNotesLimitUseCase.getLimit()
                if (limit.reached) {
                    if (!limit.isVip) {
                        analytics.logVipGate(feature = "notes")
                    }
                    return@launch
                }

                editorState.update { NoteEditor(type) }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            } finally {
                job = null
            }
        }
    }

    fun editNote(note: UserNote) {
        if (editorState.value != null) {
            return
        }
        editorState.update { NoteEditor(type = note.type, note = note) }
    }

    fun closeEditor() {
        if (loadingEditorState.value.isLoading) {
            return
        }
        editorState.update { null }
        errorState.update { null }
    }

    fun submitNote(text: String) {
        val editor = editorState.value ?: return
        val trimmed = text.trim()
        if (trimmed.isEmpty() || trimmed.length > NOTE_CHARACTER_LIMIT || job?.isActive == true) {
            return
        }

        job = viewModelScope.launch {
            try {
                loadingEditorState.update { Loading }
                errorState.update { null }

                when (val note = editor.note) {
                    null -> {
                        postNoteUseCase.postNote(mediaType, mediaId, editor.type, trimmed)
                        analytics.logNoteAdd(type = editor.type.value)
                    }
                    else -> {
                        editNoteUseCase.editNote(note, trimmed)
                        analytics.logNoteEdit(type = editor.type.value)
                    }
                }

                editorState.update { null }
                refreshNotes()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingEditorState.update { Done }
                job = null
            }
        }
    }

    fun deleteNote(note: UserNote) {
        if (job?.isActive == true) {
            return
        }

        job = viewModelScope.launch {
            try {
                loadingDeleteState.update { Loading }
                deleteNoteUseCase.deleteNote(note)
                analytics.logNoteDelete(type = note.type.value)
                notesState.update { notes ->
                    notes?.filterNot { it.id == note.id }?.toImmutableList()
                }
                refreshNotes()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingDeleteState.update { Done }
                job = null
            }
        }
    }

    fun clearError() {
        errorState.update { null }
    }

    val state = combine(
        notesState,
        editorState,
        loadingEditorState,
        loadingDeleteState,
        errorState,
    ) { state ->
        NotesState(
            notes = state[0] as ImmutableList<UserNote>?,
            editor = state[1] as NoteEditor?,
            loadingEditor = state[2] as LoadingState,
            loadingDelete = state[3] as LoadingState,
            error = state[4] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
