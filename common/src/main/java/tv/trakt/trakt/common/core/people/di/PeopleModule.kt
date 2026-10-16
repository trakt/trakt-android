package tv.trakt.trakt.common.core.people.di

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import tv.trakt.trakt.common.core.people.data.remote.PeopleApiClient
import tv.trakt.trakt.common.core.people.data.remote.PeopleRemoteDataSource

val peopleRemoteModule = module {
    singleOf(::PeopleApiClient) { bind<PeopleRemoteDataSource>() }
}
