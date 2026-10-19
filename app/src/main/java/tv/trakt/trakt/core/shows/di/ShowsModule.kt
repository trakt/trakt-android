package tv.trakt.trakt.core.shows.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.discover.sections.anticipated.usecases.GetAnticipatedShowsUseCase
import tv.trakt.trakt.core.discover.sections.anticipated.usecases.shows.DefaultGetAnticipatedShowsUseCase
import tv.trakt.trakt.core.discover.sections.popular.usecases.GetPopularShowsUseCase
import tv.trakt.trakt.core.discover.sections.popular.usecases.shows.DefaultGetPopularShowsUseCase
import tv.trakt.trakt.core.discover.sections.releases.usecases.shows.DefaultGetReleasesShowsUseCase
import tv.trakt.trakt.core.discover.sections.releases.usecases.shows.GetReleasesShowsUseCase
import tv.trakt.trakt.core.discover.sections.trending.usecases.GetTrendingShowsUseCase
import tv.trakt.trakt.core.discover.sections.trending.usecases.shows.DefaultGetTrendingShowsUseCase
import tv.trakt.trakt.core.home.sections.recommended.usecase.GetRecommendedShowsUseCase
import tv.trakt.trakt.core.home.sections.recommended.usecase.HideRecommendedShowUseCase
import tv.trakt.trakt.core.home.sections.recommended.usecase.shows.DefaultGetRecommendedShowsUseCase
import tv.trakt.trakt.core.shows.ui.context.ShowContextViewModel

internal val showsModule = module {
    factory<GetTrendingShowsUseCase>(
        qualifier = named("defaultTrendingShowsUseCase"),
    ) {
        DefaultGetTrendingShowsUseCase(
            remoteSource = get(),
            localTrendingSource = get(),
            localShowSource = get(),
            seasonalThemeUseCase = get(),
        )
    }

    factory<GetPopularShowsUseCase>(
        qualifier = named("defaultPopularShowsUseCase"),
    ) {
        DefaultGetPopularShowsUseCase(
            remoteSource = get(),
            localPopularSource = get(),
            localShowSource = get(),
            seasonalThemeUseCase = get(),
        )
    }

    factory<GetAnticipatedShowsUseCase>(
        qualifier = named("defaultAnticipatedShowsUseCase"),
    ) {
        DefaultGetAnticipatedShowsUseCase(
            remoteSource = get(),
            localAnticipatedSource = get(),
            localShowSource = get(),
            seasonalThemeUseCase = get(),
        )
    }

    factory<GetReleasesShowsUseCase>(
        qualifier = named("defaultReleasesShowsUseCase"),
    ) {
        DefaultGetReleasesShowsUseCase(
            remoteSource = get(),
            localSource = get(),
            localShowSource = get(),
            localEpisodeSource = get(),
        )
    }

    factory<GetRecommendedShowsUseCase>(
        qualifier = named("defaultRecommendedShowsUseCase"),
    ) {
        DefaultGetRecommendedShowsUseCase(
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
            userWatchlistLocalSource = get(),
            userWatchlistMinLocalSource = get(),
            loadProgressUseCase = get(),
            loadWatchlistUseCase = get(),
            sessionManager = get(),
            analytics = get(),
            errorsManager = get(),
            dataChanges = get(),
        )
    }
}
