package tv.trakt.trakt.core.profile.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.core.profile.ProfileViewModel

internal val profileModule = module {
    viewModel {
        ProfileViewModel()
    }
}
