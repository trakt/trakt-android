package tv.trakt.trakt.core.shows.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.discover.sections.anticipated.usecases.GetAnticipatedShowsUseCase
import tv.trakt.trakt.core.discover.sections.popular.usecases.GetPopularShowsUseCase
import tv.trakt.trakt.core.discover.sections.releases.usecases.shows.GetReleasesShowsUseCase
import tv.trakt.trakt.core.discover.sections.trending.usecases.GetTrendingShowsUseCase
import tv.trakt.trakt.core.home.sections.recommended.usecase.GetRecommendedShowsUseCase
import tv.trakt.trakt.core.home.sections.recommended.usecase.HideRecommendedShowUseCase
import tv.trakt.trakt.core.shows.ui.context.ShowContextViewModel

internal val showsModule = module {
    factory(
        qualifier = named("defaultTrendingShowsUseCase"),
    ) {
        GetTrendingShowsUseCase(
            remoteSource = get(),
            localTrendingSource = get(),
            localShowSource = get(),
            seasonalThemeUseCase = get(),
        )
    }

    factory(
        qualifier = named("defaultPopularShowsUseCase"),
    ) {
        GetPopularShowsUseCase(
            remoteSource = get(),
            localPopularSource = get(),
            localShowSource = get(),
            seasonalThemeUseCase = get(),
        )
    }

    factory(
        qualifier = named("defaultAnticipatedShowsUseCase"),
    ) {
        GetAnticipatedShowsUseCase(
            remoteSource = get(),
            localAnticipatedSource = get(),
            localShowSource = get(),
            seasonalThemeUseCase = get(),
        )
    }

    factory(
        qualifier = named("defaultReleasesShowsUseCase"),
    ) {
        GetReleasesShowsUseCase(
            remoteSource = get(),
            localSource = get(),
            localShowSource = get(),
            localEpisodeSource = get(),
        )
    }

    factory(
        qualifier = named("defaultRecommendedShowsUseCase"),
    ) {
        GetRecommendedShowsUseCase(
            remoteSource = get(),
            localRecommendedSource = get(),
            localShowSource = get(),
        )
    }

    factoryOf(::HideRecommendedShowUseCase)

    viewModel { (show: Show) ->
        ShowContextViewModel(
            show = show,
            updateWatchlistUseCase = get(),
            updateHistoryUseCase = get(),
            userProgressLocalSource = get(),
            userWatchlistMinLocalSource = get(),
            loadProgressUseCase = get(),
            loadWatchlistUseCase = get(),
            sessionManager = get(),
            analytics = get(),
            errorsManager = get(),
        )
    }
}
