package tv.trakt.trakt.common.core.movies.di

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import tv.trakt.trakt.common.core.movies.data.remote.MoviesApiClient
import tv.trakt.trakt.common.core.movies.data.remote.MoviesRemoteDataSource

val moviesRemoteModule = module {
    singleOf(::MoviesApiClient) { bind<MoviesRemoteDataSource>() }
}
