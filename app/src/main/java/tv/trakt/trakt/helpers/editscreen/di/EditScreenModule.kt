package tv.trakt.trakt.helpers.editscreen.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.helpers.editscreen.EditScreenViewModel
import tv.trakt.trakt.helpers.editscreen.data.model.EditScreenKey

internal val editScreenModule = module {
    viewModel { (values: Set<EditScreenKey>) ->
        EditScreenViewModel(
            enabledValues = values,
            editScreenManager = get(),
        )
    }
}
