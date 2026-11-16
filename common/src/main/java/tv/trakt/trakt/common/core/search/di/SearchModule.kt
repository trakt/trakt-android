package tv.trakt.trakt.common.core.search.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import tv.trakt.trakt.common.core.search.SearchViewModel
import tv.trakt.trakt.common.core.search.usecase.GetBirthdayPeopleUseCase
import tv.trakt.trakt.common.core.search.usecase.GetSearchResultsUseCase
import tv.trakt.trakt.common.core.search.usecase.popular.GetPopularSearchUseCase
import tv.trakt.trakt.common.core.search.usecase.popular.PostUserSearchUseCase

val commonSearchModule = module {
    factoryOf(::GetSearchResultsUseCase)
    factoryOf(::GetPopularSearchUseCase)
    factoryOf(::PostUserSearchUseCase)
    factoryOf(::GetBirthdayPeopleUseCase)

    viewModelOf(::SearchViewModel)
}
