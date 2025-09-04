package tv.trakt.trakt.app.core.profile.di

import androidx.lifecycle.SavedStateHandle
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.openapitools.client.apis.CalendarsApi
import org.openapitools.client.apis.HistoryApi
import org.openapitools.client.apis.UsersApi
import tv.trakt.trakt.app.core.profile.ProfileViewModel
import tv.trakt.trakt.app.core.profile.data.remote.ProfileApiClient
import tv.trakt.trakt.app.core.profile.data.remote.ProfileRemoteDataSource
import tv.trakt.trakt.app.core.profile.sections.favorites.movies.ProfileFavoriteMoviesViewModel
import tv.trakt.trakt.app.core.profile.sections.favorites.movies.usecases.GetFavoriteMoviesUseCase
import tv.trakt.trakt.app.core.profile.sections.favorites.movies.viewall.ProfileFavoriteMoviesViewAllViewModel
import tv.trakt.trakt.app.core.profile.sections.favorites.shows.ProfileFavoriteShowsViewModel
import tv.trakt.trakt.app.core.profile.sections.favorites.shows.usecases.GetFavoriteShowsUseCase
import tv.trakt.trakt.app.core.profile.sections.favorites.shows.viewall.ProfileFavoriteShowsViewAllViewModel
import tv.trakt.trakt.app.core.profile.sections.history.ProfileHistoryViewModel
import tv.trakt.trakt.app.core.profile.sections.history.usecases.GetProfileHistoryUseCase
import tv.trakt.trakt.app.core.profile.sections.history.usecases.SyncProfileHistoryUseCase
import tv.trakt.trakt.app.core.profile.sections.history.viewall.ProfileHistoryViewAllViewModel
import tv.trakt.trakt.app.core.profile.usecases.LogoutProfileUseCase
import tv.trakt.trakt.common.Config.API_BASE_URL

internal val profileDataModule = module {
    single<ProfileRemoteDataSource> {
        val httpClientEngine = get<HttpClientEngine>()
        val httpClientConfig = get<(HttpClientConfig<*>) -> Unit>(named("authorizedClientConfig"))

        ProfileApiClient(
            api = UsersApi(
                baseUrl = API_BASE_URL,
                httpClientEngine = httpClientEngine,
                httpClientConfig = httpClientConfig,
            ),
            calendarsApi = CalendarsApi(
                baseUrl = API_BASE_URL,
                httpClientEngine = httpClientEngine,
                httpClientConfig = httpClientConfig,
            ),
            historyApi = HistoryApi(
                baseUrl = API_BASE_URL,
                httpClientEngine = httpClientEngine,
                httpClientConfig = httpClientConfig,
            ),
        )
    }
}

internal val profileModule = module {

    factory {
        LogoutProfileUseCase(
            sessionManager = get(),
            showsSyncLocalDataSource = get(),
            moviesSyncLocalDataSource = get(),
            episodesSyncLocalDataSource = get(),
            recentSearchLocalDataSource = get(),
        )
    }

    factory {
        GetProfileHistoryUseCase(
            remoteUserSource = get(),
            localMoviesSource = get(),
            localEpisodesSource = get(),
        )
    }

    factory {
        SyncProfileHistoryUseCase(
            localShowsSyncSource = get(),
            localMoviesSyncSource = get(),
            localEpisodesSyncSource = get(),
        )
    }

    factory {
        GetFavoriteShowsUseCase(
            remoteUserSource = get(),
            localShowsSource = get(),
        )
    }

    factory {
        GetFavoriteMoviesUseCase(
            remoteUserSource = get(),
            localMoviesSource = get(),
        )
    }

    viewModel { (_: SavedStateHandle) ->
        ProfileViewModel(
            sessionManager = get(),
            logoutUseCase = get(),
        )
    }

    viewModel {
        ProfileHistoryViewModel(
            getHistoryCase = get(),
            syncHistoryCase = get(),
        )
    }

    viewModel {
        ProfileHistoryViewAllViewModel(
            getHistoryCase = get(),
            syncHistoryCase = get(),
        )
    }

    viewModel {
        ProfileFavoriteShowsViewModel(
            getFavoriteShowsCase = get(),
        )
    }

    viewModel {
        ProfileFavoriteShowsViewAllViewModel(
            getFavoriteShowsCase = get(),
        )
    }

    viewModel {
        ProfileFavoriteMoviesViewModel(
            getFavoriteMoviesCase = get(),
        )
    }

    viewModel {
        ProfileFavoriteMoviesViewAllViewModel(
            getFavoriteMoviesCase = get(),
        )
    }
}
