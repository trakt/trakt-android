package tv.trakt.app.tv.core.details.lists.module

import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.openapitools.client.apis.ListsApi
import tv.trakt.app.tv.Config.API_BASE_URL
import tv.trakt.app.tv.core.details.lists.data.remote.ListsApiClient
import tv.trakt.app.tv.core.details.lists.data.remote.ListsRemoteDataSource

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
