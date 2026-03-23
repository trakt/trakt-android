package tv.trakt.trakt.app.core.details.lists.module

import org.koin.dsl.module
import tv.trakt.trakt.common.core.lists.data.remote.ListsApiClient
import tv.trakt.trakt.common.core.lists.data.remote.ListsRemoteDataSource

internal val customListsDataModule = module {
    single<ListsRemoteDataSource> {
        ListsApiClient(
            listsApi = get(),
            cacheMarker = get(),
        )
    }
}
