package tv.trakt.trakt.core.users.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import tv.trakt.trakt.common.core.user.usecases.blocking.BlockUserUseCase
import tv.trakt.trakt.common.core.user.usecases.blocking.GetBlockedUsersUseCase
import tv.trakt.trakt.common.core.user.usecases.following.FollowUserUseCase
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.users.UserProfileViewModel
import tv.trakt.trakt.core.users.sections.favorites.UserProfileFavoritesViewModel
import tv.trakt.trakt.core.users.sections.favorites.all.AllUserProfileFavoritesViewModel
import tv.trakt.trakt.core.users.sections.favorites.usecases.GetUserProfileFavoritesUseCase
import tv.trakt.trakt.core.users.sections.history.UserProfileHistoryViewModel
import tv.trakt.trakt.core.users.sections.history.all.AllUserProfileHistoryViewModel
import tv.trakt.trakt.core.users.sections.history.usecases.GetUserProfileHistoryUseCase
import tv.trakt.trakt.core.users.sections.lists.UserProfileListsViewModel
import tv.trakt.trakt.core.users.sections.lists.all.AllUserProfileListsViewModel
import tv.trakt.trakt.core.users.sections.lists.usecases.GetUserProfileListsUseCase
import tv.trakt.trakt.core.users.sections.social.UserProfileSocialViewModel
import tv.trakt.trakt.core.users.sections.social.usecases.GetUserProfileSocialUseCase
import tv.trakt.trakt.core.users.sections.thismonth.GetUserProfileMonthUseCase
import tv.trakt.trakt.core.users.usecases.GetUserProfileDetailsUseCase

internal val userProfileModule = module {
    viewModelOf(::UserProfileViewModel)

    viewModelOf(::AllUserProfileHistoryViewModel)

    viewModelOf(::AllUserProfileFavoritesViewModel)

    viewModelOf(::AllUserProfileListsViewModel)

    viewModel { (userId: TraktId) ->
        UserProfileHistoryViewModel(
            userId = userId,
            getHistoryUseCase = get(),
            collapsingManager = get(),
        )
    }

    viewModel { (userId: TraktId) ->
        UserProfileFavoritesViewModel(
            userId = userId,
            getFavoritesUseCase = get(),
            collectionStateProvider = get(),
            collapsingManager = get(),
        )
    }

    viewModel { (userId: TraktId) ->
        UserProfileSocialViewModel(
            userId = userId,
            getSocialUseCase = get(),
            collapsingManager = get(),
        )
    }

    viewModel { (userId: TraktId) ->
        UserProfileListsViewModel(
            userId = userId,
            getListsUseCase = get(),
            collapsingManager = get(),
        )
    }

    factoryOf(::GetUserProfileHistoryUseCase)
    factoryOf(::GetUserProfileFavoritesUseCase)
    factoryOf(::GetUserProfileSocialUseCase)
    factoryOf(::GetUserProfileListsUseCase)
    factoryOf(::GetUserProfileDetailsUseCase)
    factoryOf(::GetUserProfileMonthUseCase)

    factoryOf(::GetBlockedUsersUseCase)
    factoryOf(::BlockUserUseCase)
    factoryOf(::FollowUserUseCase)
}
