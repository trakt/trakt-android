package tv.trakt.trakt.common.core.shows.di

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import tv.trakt.trakt.common.core.shows.data.remote.ShowsApiClient
import tv.trakt.trakt.common.core.shows.data.remote.ShowsRemoteDataSource

val showsRemoteModule = module {
    singleOf(::ShowsApiClient) { bind<ShowsRemoteDataSource>() }
}
