package tv.trakt.trakt.common.core.movies.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import tv.trakt.trakt.common.core.movies.data.remote.MoviesRemoteSource

val moviesRemoteModule = module {
    singleOf(::MoviesRemoteSource)
}
