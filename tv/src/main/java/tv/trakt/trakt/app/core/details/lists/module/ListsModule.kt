package tv.trakt.trakt.app.core.details.lists.module

import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.openapitools.client.apis.ListsApi
import tv.trakt.trakt.common.Config.API_BASE_URL
import tv.trakt.trakt.common.core.lists.data.remote.ListsApiClient
import tv.trakt.trakt.common.core.lists.data.remote.ListsRemoteDataSource

internal val customListsDataModule = module {
    single<ListsRemoteDataSource> {
        ListsApiClient(
            listsApi = ListsApi(
                baseUrl = API_BASE_URL,
                httpClientEngine = get(),
                httpClientConfig = get(named("authorizedClientConfig")),
            ),
            cacheMarker = get(),
        )
    }
}
