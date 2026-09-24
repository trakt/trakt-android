package tv.trakt.trakt.core.main

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import kotlinx.coroutines.delay
import tv.trakt.trakt.core.main.navigation.isNonSearchDestination
import tv.trakt.trakt.core.search.model.SearchFilter
import tv.trakt.trakt.core.search.model.SearchInput
import tv.trakt.trakt.core.search.navigation.SearchDestination
import kotlin.time.Duration.Companion.milliseconds

@Immutable
internal data class MainSearchState(
    val searchInput: SearchInput = SearchInput(),
    val searchLoading: Boolean = false,
    val searchVisible: Boolean = false,
    val requestFocus: Boolean = false,
)

@Composable
internal fun rememberSearchState(currentDestination: NavDestination?): MainSearchStateHolder {
    val queryState = rememberTextFieldState()
    var searchState by remember {
        mutableStateOf(MainSearchState())
    }

    LaunchedEffect(queryState) {
        snapshotFlow { queryState.text.toString() }
            .collect { query ->
                searchState = searchState.copy(
                    searchInput = searchState.searchInput.copy(query = query),
                )
            }
    }

    val searchVisible = remember(currentDestination) {
        currentDestination?.hasRoute(SearchDestination::class) == true
    }

    LaunchedEffect(currentDestination) {
        if (isNonSearchDestination(currentDestination)) {
            delay(200.milliseconds)
            queryState.clearText()
            searchState = MainSearchState()
        }
    }

    return remember(searchState, searchVisible) {
        MainSearchStateHolder(
            searchState = searchState.copy(searchVisible = searchVisible),
            queryState = queryState,
            onSearchQuery = {
                queryState.setTextAndPlaceCursorAtEnd(it)
            },
            onSearchFilter = {
                searchState = searchState.copy(
                    searchInput = searchState.searchInput.copy(filter = it),
                )
            },
            onSearchLoading = {
                searchState = searchState.copy(searchLoading = it)
            },
            onRequestFocus = {
                searchState = searchState.copy(requestFocus = true)
            },
        )
    }
}

internal data class MainSearchStateHolder(
    val searchState: MainSearchState = MainSearchState(),
    val queryState: TextFieldState = TextFieldState(),
    val onSearchQuery: (String) -> Unit = {},
    val onSearchFilter: (SearchFilter) -> Unit = {},
    val onSearchLoading: (Boolean) -> Unit = {},
    val onRequestFocus: () -> Unit = {},
) {
    val searchInput: SearchInput get() = searchState.searchInput
    val searchLoading: Boolean get() = searchState.searchLoading
    val searchVisible: Boolean get() = searchState.searchVisible
}
