package tv.trakt.trakt.core.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import kotlinx.coroutines.delay
import tv.trakt.trakt.core.main.navigation.isNonSearchDestination
import tv.trakt.trakt.core.search.model.SearchInput
import tv.trakt.trakt.core.search.navigation.SearchDestination

@Immutable
internal data class SearchState(
    val searchInput: SearchInput = SearchInput(),
    val searchLoading: Boolean = false,
)

@Composable
internal fun rememberSearchState(currentDestination: NavDestination?): SearchStateManager {
    var searchState by remember { mutableStateOf(SearchState()) }
    val searchVisible = remember(currentDestination) {
        currentDestination?.hasRoute(SearchDestination::class) == true
    }

    LaunchedEffect(currentDestination) {
        if (isNonSearchDestination(currentDestination)) {
            delay(300)
            searchState = SearchState()
        }
    }

    return remember(searchState, searchVisible) {
        SearchStateManager(
            state = searchState,
            searchVisible = searchVisible,
            updateSearchInput = { newInput ->
                searchState = searchState.copy(searchInput = newInput)
            },
            updateSearchLoading = { loading ->
                searchState = searchState.copy(searchLoading = loading)
            },
            resetSearchState = {
                searchState = SearchState()
            },
        )
    }
}

internal data class SearchStateManager(
    val state: SearchState,
    val searchVisible: Boolean,
    val updateSearchInput: (SearchInput) -> Unit,
    val updateSearchLoading: (Boolean) -> Unit,
    val resetSearchState: () -> Unit,
) {
    val searchInput: SearchInput get() = state.searchInput
    val searchLoading: Boolean get() = state.searchLoading
}
