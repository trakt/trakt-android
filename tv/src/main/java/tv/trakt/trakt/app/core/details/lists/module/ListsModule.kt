package tv.trakt.trakt.app.core.details.lists.module

import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.openapitools.client.apis.ListsApi
import tv.trakt.trakt.app.core.details.lists.data.remote.ListsApiClient
import tv.trakt.trakt.app.core.details.lists.data.remote.ListsRemoteDataSource
import tv.trakt.trakt.common.Config.API_BASE_URL

internal val customListsDataModule = module {
    single<ListsRemoteDataSource> {
        ListsApiClient(
            api = ListsApi(
                baseUrl = API_BASE_URL,
                httpClientEngine = get(),
                httpClientConfig = get(named("clientConfig")),
            ),
        )
    }
}
