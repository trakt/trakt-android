package tv.trakt.trakt.core.summary.people.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import tv.trakt.trakt.core.summary.people.PersonDetailsViewModel

internal val personDetailsModule = module {
    viewModelOf(::PersonDetailsViewModel)
}
