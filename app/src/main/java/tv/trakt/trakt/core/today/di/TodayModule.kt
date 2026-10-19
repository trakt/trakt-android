package tv.trakt.trakt.core.today.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import tv.trakt.trakt.common.core.today.model.TodayMedia
import tv.trakt.trakt.core.today.data.local.TodayLocalSource
import tv.trakt.trakt.core.today.model.TodayForYouItem
import tv.trakt.trakt.core.today.pill.TodayPillViewModel
import tv.trakt.trakt.core.today.stories.TodayStoriesViewModel
import tv.trakt.trakt.core.today.stories.details.TodayPosterDetailsViewModel
import tv.trakt.trakt.core.today.stories.watched.TodayWatchedViewModel
import tv.trakt.trakt.core.today.stories.watchlist.TodayWatchlistViewModel
import tv.trakt.trakt.core.today.usecases.GetTodayDetailsUseCase
import tv.trakt.trakt.core.today.usecases.GetTodayForYouUseCase
import tv.trakt.trakt.core.today.usecases.GetTodayStoriesUseCase
import tv.trakt.trakt.core.today.usecases.UpdateTodayHistoryUseCase
import tv.trakt.trakt.core.today.usecases.UpdateTodayWatchlistUseCase

internal val todayDataModule = module {
    singleOf(::TodayLocalSource)
}

internal val todayModule = module {
    factoryOf(::GetTodayStoriesUseCase)
    factoryOf(::GetTodayDetailsUseCase)
    factoryOf(::UpdateTodayWatchlistUseCase)
    factoryOf(::GetTodayForYouUseCase)
    factoryOf(::UpdateTodayHistoryUseCase)

    viewModelOf(::TodayPillViewModel)
    viewModelOf(::TodayStoriesViewModel)
    viewModel { (media: TodayMedia) ->
        TodayPosterDetailsViewModel(
            media = media,
            getTodayDetailsUseCase = get(),
        )
    }
    viewModel { (item: TodayForYouItem) ->
        TodayWatchedViewModel(
            item = item,
            updateHistoryUseCase = get(),
            analytics = get(),
        )
    }
    viewModel { (media: TodayMedia) ->
        TodayWatchlistViewModel(
            media = media,
            updateWatchlistUseCase = get(),
            dataChanges = get(),
            sessionManager = get(),
            errorsManager = get(),
            analytics = get(),
        )
    }
}
