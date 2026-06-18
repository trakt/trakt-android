package tv.trakt.trakt.core.userprofile.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import tv.trakt.trakt.common.core.user.usecases.blocking.BlockUserUseCase
import tv.trakt.trakt.common.core.user.usecases.blocking.GetBlockedUsersUseCase
import tv.trakt.trakt.common.core.user.usecases.following.FollowUserUseCase
import tv.trakt.trakt.core.userprofile.UserProfileViewModel
import tv.trakt.trakt.core.userprofile.sections.favorites.UserProfileFavoritesViewModel
import tv.trakt.trakt.core.userprofile.sections.favorites.all.AllUserProfileFavoritesViewModel
import tv.trakt.trakt.core.userprofile.sections.favorites.usecases.GetUserProfileFavoritesUseCase
import tv.trakt.trakt.core.userprofile.sections.history.UserProfileHistoryViewModel
import tv.trakt.trakt.core.userprofile.sections.history.all.AllUserProfileHistoryViewModel
import tv.trakt.trakt.core.userprofile.sections.history.usecases.GetUserProfileHistoryUseCase
import tv.trakt.trakt.core.userprofile.sections.lists.UserProfileListsViewModel
import tv.trakt.trakt.core.userprofile.sections.lists.all.AllUserProfileListsViewModel
import tv.trakt.trakt.core.userprofile.sections.lists.usecases.GetUserProfileListsUseCase
import tv.trakt.trakt.core.userprofile.sections.social.UserProfileSocialViewModel
import tv.trakt.trakt.core.userprofile.sections.social.usecases.GetUserProfileSocialUseCase
import tv.trakt.trakt.core.userprofile.sections.thismonth.GetUserProfileMonthUseCase
import tv.trakt.trakt.core.userprofile.usecases.GetUserProfileDetailsUseCase

internal val userProfileModule = module {
    factoryOf(::GetUserProfileHistoryUseCase)
    factoryOf(::GetUserProfileFavoritesUseCase)
    factoryOf(::GetUserProfileSocialUseCase)
    factoryOf(::GetUserProfileListsUseCase)
    factoryOf(::GetUserProfileDetailsUseCase)
    factoryOf(::GetUserProfileMonthUseCase)

    factoryOf(::GetBlockedUsersUseCase)
    factoryOf(::BlockUserUseCase)
    factoryOf(::FollowUserUseCase)

    // ViewModels

    viewModelOf(::AllUserProfileHistoryViewModel)
    viewModelOf(::AllUserProfileFavoritesViewModel)
    viewModelOf(::AllUserProfileListsViewModel)

    viewModelOf(::UserProfileViewModel)
    viewModelOf(::UserProfileHistoryViewModel)
    viewModelOf(::UserProfileFavoritesViewModel)
    viewModelOf(::UserProfileSocialViewModel)
    viewModelOf(::UserProfileListsViewModel)
}
