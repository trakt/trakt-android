package tv.trakt.trakt.core.people.di

import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.common.Config.API_HD_BASE_URL
import tv.trakt.trakt.core.people.data.remote.PeopleApiClient
import tv.trakt.trakt.core.people.data.remote.PeopleRemoteDataSource
import tv.trakt.trakt.core.people.data.remote.api.PeopleExtrasApi

internal val peopleDataModule = module {
    single<PeopleRemoteDataSource> {
        PeopleApiClient(
            api = PeopleExtrasApi(
                baseUrl = API_HD_BASE_URL,
                httpClientEngine = get(),
                httpClientConfig = get(named("clientConfig")),
            ),
        )
    }
}
