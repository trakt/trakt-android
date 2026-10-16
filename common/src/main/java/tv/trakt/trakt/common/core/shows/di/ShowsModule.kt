package tv.trakt.trakt.common.core.shows.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import tv.trakt.trakt.common.core.shows.data.remote.ShowsRemoteSource

val showsRemoteModule = module {
    singleOf(::ShowsRemoteSource)
}
