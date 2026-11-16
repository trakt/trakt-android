package tv.trakt.trakt.common.core.search.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.common.core.search.SearchViewModel
import tv.trakt.trakt.common.core.search.data.local.people.SearchPeopleLocalSource
import tv.trakt.trakt.common.core.search.data.local.popular.PopularSearchLocalSource
import tv.trakt.trakt.common.core.search.usecase.GetBirthdayPeopleUseCase
import tv.trakt.trakt.common.core.search.usecase.GetSearchResultsUseCase
import tv.trakt.trakt.common.core.search.usecase.popular.GetPopularSearchUseCase
import tv.trakt.trakt.common.core.search.usecase.popular.PostUserSearchUseCase

const val SEARCH_PREFERENCES = "search_preferences"

val commonSearchModule = module {
    single {
        SearchPeopleLocalSource(
            dataStore = get(named(SEARCH_PREFERENCES)),
        )
    }

    single {
        PopularSearchLocalSource(
            dataStore = get(named(SEARCH_PREFERENCES)),
        )
    }

    factoryOf(::GetSearchResultsUseCase)
    factoryOf(::GetPopularSearchUseCase)
    factoryOf(::PostUserSearchUseCase)
    factoryOf(::GetBirthdayPeopleUseCase)

    viewModelOf(::SearchViewModel)
}
