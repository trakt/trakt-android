package tv.trakt.trakt.common.core.lists.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import tv.trakt.trakt.common.core.lists.manage.ManageListsViewModel
import tv.trakt.trakt.common.core.lists.usecases.AddPersonalListItemUseCase
import tv.trakt.trakt.common.core.lists.usecases.GetCollaborationsListsUseCase
import tv.trakt.trakt.common.core.lists.usecases.RemovePersonalListItemUseCase
import tv.trakt.trakt.common.core.user.usecases.lists.LoadUserListsUseCase

val manageListsModule = module {
    factoryOf(::LoadUserListsUseCase)
    factoryOf(::GetCollaborationsListsUseCase)
    factoryOf(::AddPersonalListItemUseCase)
    factoryOf(::RemovePersonalListItemUseCase)

    viewModelOf(::ManageListsViewModel)
}
