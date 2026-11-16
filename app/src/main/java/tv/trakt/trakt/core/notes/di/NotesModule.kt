package tv.trakt.trakt.core.notes.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.notes.NotesViewModel
import tv.trakt.trakt.core.notes.usecases.DeleteNoteUseCase
import tv.trakt.trakt.core.notes.usecases.EditNoteUseCase
import tv.trakt.trakt.core.notes.usecases.GetMediaNotesUseCase
import tv.trakt.trakt.core.notes.usecases.GetNotesLimitUseCase
import tv.trakt.trakt.core.notes.usecases.PostNoteUseCase

internal val notesModule = module {
    factoryOf(::GetMediaNotesUseCase)
    factoryOf(::GetNotesLimitUseCase)
    factoryOf(::PostNoteUseCase)
    factoryOf(::EditNoteUseCase)
    factoryOf(::DeleteNoteUseCase)

    viewModel { (mediaType: MediaType, mediaId: TraktId) ->
        NotesViewModel(
            mediaType = mediaType,
            mediaId = mediaId,
            getNotesUseCase = get(),
            getNotesLimitUseCase = get(),
            postNoteUseCase = get(),
            editNoteUseCase = get(),
            deleteNoteUseCase = get(),
            sessionManager = get(),
            analytics = get(),
        )
    }
}
