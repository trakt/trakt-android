package tv.trakt.trakt.common.core.people.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import tv.trakt.trakt.common.core.people.data.remote.PeopleRemoteSource

val peopleRemoteModule = module {
    singleOf(::PeopleRemoteSource)
}
