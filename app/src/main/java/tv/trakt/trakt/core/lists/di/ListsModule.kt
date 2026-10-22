package tv.trakt.trakt.core.lists.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.core.lists.ListsViewModel
import tv.trakt.trakt.core.lists.features.all.AllListsViewModel
import tv.trakt.trakt.core.lists.features.details.ListDetailsViewModel
import tv.trakt.trakt.core.lists.features.details.usecases.GetListItemsUseCase
import tv.trakt.trakt.core.lists.features.reorder.ListReorderViewModel
import tv.trakt.trakt.core.lists.features.reorder.lists.ListsReorderViewModel
import tv.trakt.trakt.core.lists.features.reorder.lists.usecase.ReorderPersonalListsUseCase
import tv.trakt.trakt.core.lists.features.reorder.usecase.ReorderListUseCase
import tv.trakt.trakt.core.lists.features.reorder.watchlist.WatchlistReorderViewModel
import tv.trakt.trakt.core.lists.features.reorder.watchlist.usecase.ReorderWatchlistUseCase
import tv.trakt.trakt.core.lists.features.smart.CreateSmartListViewModel
import tv.trakt.trakt.core.lists.features.smart.usecase.CreateSmartListUseCase
import tv.trakt.trakt.core.lists.features.smart.usecase.GetSmartListPreviewUseCase
import tv.trakt.trakt.core.lists.sections.collaborations.usecases.GetCollaborationsListsUseCase
import tv.trakt.trakt.core.lists.sections.liked.usecases.GetLikedListsUseCase
import tv.trakt.trakt.core.lists.sections.liked.usecases.manage.AddLikedListUseCase
import tv.trakt.trakt.core.lists.sections.liked.usecases.manage.RemoveLikedListUseCase
import tv.trakt.trakt.core.lists.sections.personal.features.all.AllPersonalListViewModel
import tv.trakt.trakt.core.lists.sections.personal.features.context.movie.ListMovieContextViewModel
import tv.trakt.trakt.core.lists.sections.personal.features.context.show.ListShowContextViewModel
import tv.trakt.trakt.core.lists.sections.personal.usecases.GetPersonalListItemsUseCase
import tv.trakt.trakt.core.lists.sections.personal.usecases.GetPersonalListsUseCase
import tv.trakt.trakt.core.lists.sections.personal.usecases.manage.AddPersonalListItemUseCase
import tv.trakt.trakt.core.lists.sections.personal.usecases.manage.RemovePersonalListItemUseCase
import tv.trakt.trakt.core.lists.sections.smart.details.SmartListDetailsViewModel
import tv.trakt.trakt.core.lists.sections.smart.usecases.DeleteSmartListUseCase
import tv.trakt.trakt.core.lists.sections.smart.usecases.GetSmartListItemsUseCase
import tv.trakt.trakt.core.lists.sections.smart.usecases.GetSmartListsUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.ListsWatchlistViewModel
import tv.trakt.trakt.core.lists.sections.watchlist.features.all.AllWatchlistViewModel
import tv.trakt.trakt.core.lists.sections.watchlist.features.context.movies.WatchlistMovieContextViewModel
import tv.trakt.trakt.core.lists.sections.watchlist.features.context.shows.WatchlistShowContextViewModel
import tv.trakt.trakt.core.lists.sections.watchlist.usecases.GetMoviesWatchlistUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.usecases.GetShowsWatchlistUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.usecases.GetWatchlistUseCase
import tv.trakt.trakt.core.lists.sheets.create.CreateListViewModel
import tv.trakt.trakt.core.lists.sheets.create.usecases.CreateListUseCase
import tv.trakt.trakt.core.lists.sheets.edit.EditListViewModel
import tv.trakt.trakt.core.lists.sheets.edit.usecases.EditListUseCase
import tv.trakt.trakt.core.lists.usecases.GetListsFilterUseCase

internal const val LISTS_PREFERENCES = "lists_preferences_mobile"

internal val listsDataModule = module {
    single<DataStore<Preferences>>(named(LISTS_PREFERENCES)) {
        createStore(
            context = androidApplication(),
        )
    }
}

internal val listsModule = module {
    factoryOf(::GetWatchlistUseCase)
    factoryOf(::GetShowsWatchlistUseCase)
    factoryOf(::GetMoviesWatchlistUseCase)
    factoryOf(::GetPersonalListsUseCase)
    factoryOf(::GetLikedListsUseCase)
    factoryOf(::GetCollaborationsListsUseCase)
    factoryOf(::GetSmartListsUseCase)
    factoryOf(::GetSmartListItemsUseCase)
    factoryOf(::GetSmartListPreviewUseCase)
    factoryOf(::CreateSmartListUseCase)
    factoryOf(::DeleteSmartListUseCase)
    factoryOf(::GetPersonalListItemsUseCase)
    factoryOf(::GetListItemsUseCase)
    factoryOf(::CreateListUseCase)
    factoryOf(::EditListUseCase)
    factoryOf(::RemovePersonalListItemUseCase)
    factoryOf(::AddPersonalListItemUseCase)
    factoryOf(::AddLikedListUseCase)
    factoryOf(::RemoveLikedListUseCase)
    factoryOf(::ReorderListUseCase)
    factoryOf(::ReorderPersonalListsUseCase)
    factoryOf(::ReorderWatchlistUseCase)

    factory {
        GetListsFilterUseCase(
            dataStore = get(named(LISTS_PREFERENCES)),
        )
    }

    viewModelOf(::ListsViewModel)
    viewModelOf(::ListsWatchlistViewModel)
    viewModelOf(::ListDetailsViewModel)
    viewModelOf(::SmartListDetailsViewModel)
    viewModelOf(::ListReorderViewModel)
    viewModelOf(::ListsReorderViewModel)
    viewModelOf(::WatchlistReorderViewModel)
    viewModelOf(::CreateListViewModel)
    viewModelOf(::EditListViewModel)
    viewModelOf(::AllWatchlistViewModel)
    viewModelOf(::AllPersonalListViewModel)
    viewModelOf(::AllListsViewModel)
    viewModelOf(::CreateSmartListViewModel)
    viewModelOf(::WatchlistShowContextViewModel)
    viewModelOf(::ListShowContextViewModel)

    viewModel {
        WatchlistMovieContextViewModel(
            appContext = androidApplication(),
            updateMovieWatchlistUseCase = get(),
            userWatchlistLocalSource = get(),
            userWatchlistMinLocalSource = get(),
            updateMovieHistoryUseCase = get(),
            loadProgressUseCase = get(),
            sessionManager = get(),
            checkInManager = get(),
            ratePromptManager = get(),
            analytics = get(),
            dataChanges = get(),
        )
    }

    viewModel { (movie: Movie, list: CustomList) ->
        ListMovieContextViewModel(
            appContext = androidApplication(),
            movie = movie,
            list = list,
            updateMovieWatchlistUseCase = get(),
            updateMovieHistoryUseCase = get(),
            removeListItemUseCase = get(),
            userProgressLocalSource = get(),
            userWatchlistLocalSource = get(),
            userWatchlistMinLocalSource = get(),
            loadProgressUseCase = get(),
            loadWatchlistMinUseCase = get(),
            dataChanges = get(),
            sessionManager = get(),
            checkInManager = get(),
            ratePromptManager = get(),
            errorsManager = get(),
            analytics = get(),
        )
    }
}

private fun createStore(context: Context): DataStore<Preferences> {
    return PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler(
            produceNewData = { emptyPreferences() },
        ),
        migrations = listOf(SharedPreferencesMigration(context, LISTS_PREFERENCES)),
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        produceFile = { context.preferencesDataStoreFile(LISTS_PREFERENCES) },
    )
}
