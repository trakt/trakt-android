package tv.trakt.trakt.core.main.ui.menubar

import InputField
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.core.home.navigation.HomeDestination
import tv.trakt.trakt.core.lists.navigation.ListsDestination
import tv.trakt.trakt.core.main.MainSearchState
import tv.trakt.trakt.core.main.MainSearchStateHolder
import tv.trakt.trakt.core.main.model.NavigationItem
import tv.trakt.trakt.core.movies.navigation.MoviesDestination
import tv.trakt.trakt.core.search.model.SearchInput
import tv.trakt.trakt.core.search.navigation.SearchDestination
import tv.trakt.trakt.core.search.views.SearchFiltersList
import tv.trakt.trakt.core.shows.navigation.ShowsDestination
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private val navigationItems = listOf(
    NavigationItem(
        destination = HomeDestination,
        label = R.string.page_title_home,
        iconOn = R.drawable.ic_home_on,
        iconOff = R.drawable.ic_home_off,
    ),
    NavigationItem(
        destination = ShowsDestination,
        label = R.string.page_title_shows,
        iconOn = R.drawable.ic_shows_on,
        iconOff = R.drawable.ic_shows_off,
    ),
    NavigationItem(
        destination = MoviesDestination,
        label = R.string.page_title_movies,
        iconOn = R.drawable.ic_movies_on,
        iconOff = R.drawable.ic_movies_off,
    ),
    NavigationItem(
        destination = ListsDestination,
        label = R.string.page_title_lists,
        iconOn = R.drawable.ic_lists_on,
        iconOff = R.drawable.ic_lists_off,
    ),
    NavigationItem(
        destination = SearchDestination,
        label = R.string.page_title_search,
        iconOn = R.drawable.ic_search_on,
        iconOff = R.drawable.ic_search_off,
    ),
)

@Composable
internal fun TraktMenuBar(
    currentDestination: NavDestination?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    searchState: MainSearchStateHolder,
    onSelected: (NavigationItem) -> Unit = {},
    onReselected: () -> Unit = {},
    onSearchInput: (SearchInput) -> Unit = {},
) {
    TraktMenuBarContent(
        destination = currentDestination,
        modifier = modifier,
        enabled = enabled,
        stateHolder = searchState,
        onSelected = onSelected,
        onReselected = onReselected,
        onSearchInput = onSearchInput,
    )
}

@Composable
private fun TraktMenuBarContent(
    destination: NavDestination?,
    modifier: Modifier = Modifier,
    stateHolder: MainSearchStateHolder,
    enabled: Boolean = true,
    onSelected: (NavigationItem) -> Unit = {},
    onReselected: () -> Unit = {},
    onSearchInput: (SearchInput) -> Unit = {},
) {
    val searchFocusRequester = remember { FocusRequester() }

    LaunchedEffect(stateHolder.searchState.requestFocus) {
        if (stateHolder.searchState.requestFocus) {
            runCatching {
                searchFocusRequester.requestFocus()
            }
        }
    }

    Column(
        modifier = modifier.imePadding(),
        verticalArrangement = spacedBy(0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SearchContent(
            enabled = enabled,
            searchInput = stateHolder.searchInput,
            visible = stateHolder.searchVisible,
            loading = stateHolder.searchLoading,
            searchFocusRequester = searchFocusRequester,
            onSearchInput = onSearchInput,
        )

        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp),
        ) {
            navigationItems.forEachIndexed { index, item ->
                val isSelected = destination
                    ?.hierarchy
                    ?.any { it.hasRoute(item.destination::class) } == true

                NavigationBarItem(
                    alwaysShowLabel = false,
                    selected = isSelected,
                    onClick = {
                        if (!enabled) return@NavigationBarItem

                        if (destination?.hasRoute(item.destination::class) == true) {
                            onReselected()
                            if (stateHolder.searchVisible) {
                                runCatching {
                                    searchFocusRequester.requestFocus()
                                }
                            }
                            return@NavigationBarItem
                        }

                        onSelected(item)
                    },
                    icon = {
                        Icon(
                            painter = painterResource(
                                if (isSelected) item.iconOn else item.iconOff,
                            ),
                            contentDescription = stringResource(id = item.label),
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TraktTheme.colors.accent,
                        unselectedIconColor = TraktTheme.colors.navigationContent,
                        indicatorColor = Color.Transparent,
                    ),
                )
            }
        }
    }
}

@Composable
private fun SearchContent(
    visible: Boolean,
    enabled: Boolean,
    loading: Boolean,
    searchFocusRequester: FocusRequester,
    searchInput: SearchInput,
    onSearchInput: (SearchInput) -> Unit,
) {
    val searchQuery = rememberTextFieldState(searchInput.query)

    LaunchedEffect(searchQuery.text) {
        onSearchInput(
            searchInput.copy(query = searchQuery.text.toString()),
        )
    }

    LaunchedEffect(searchInput.query) {
        if (searchInput.query.isEmpty()) {
            searchQuery.clearText()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(durationMillis = 200, delayMillis = 200)) + expandVertically(),
        exit = fadeOut(tween(durationMillis = 200)) + shrinkVertically(),
    ) {
        Column(
            verticalArrangement = spacedBy(16.dp, Alignment.CenterVertically),
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp),
        ) {
            SearchFiltersList(
                selectedFilter = searchInput.filter,
                onFilterClick = {
                    if (!visible) {
                        return@SearchFiltersList
                    }
                    onSearchInput(searchInput.copy(filter = it))
                },
                modifier = Modifier
                    .fillMaxWidth(),
            )
            InputField(
                state = searchQuery,
                placeholder = stringResource(searchInput.filter.placeholderRes),
                icon = painterResource(R.drawable.ic_search_off),
                enabled = enabled && visible,
                loading = loading,
                endSlot = {
                    if (searchQuery.text.isNotBlank()) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = null,
                            tint = TraktTheme.colors.textSecondary,
                            modifier = Modifier.onClick {
                                searchQuery.clearText()
                            },
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(searchFocusRequester),
            )
        }
    }
}

@Preview(
    device = "id:pixel_9",
    showBackground = true,
    backgroundColor = 0xFFFFFF,
)
@Composable
private fun Preview1() {
    TraktTheme {
        TraktMenuBarContent(
            destination = null,
            stateHolder = MainSearchStateHolder(),
            modifier = Modifier
                .background(TraktTheme.colors.navigationContainer),
        )
    }
}

@Preview(
    device = "id:pixel_9",
    showBackground = true,
    backgroundColor = 0xFFFFFF,
)
@Composable
private fun Preview2() {
    TraktTheme {
        TraktMenuBarContent(
            destination = null,
            stateHolder = MainSearchStateHolder(
                searchState = MainSearchState(
                    searchVisible = true,
                ),
            ),
            modifier = Modifier
                .background(TraktTheme.colors.navigationContainer),
        )
    }
}
