package tv.trakt.trakt.core.main.ui.menubar

import InputField
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import tv.trakt.trakt.core.home.navigation.HomeDestination
import tv.trakt.trakt.core.lists.navigation.ListsDestination
import tv.trakt.trakt.core.main.model.NavigationItem
import tv.trakt.trakt.core.movies.navigation.MoviesDestination
import tv.trakt.trakt.core.search.navigation.SearchDestination
import tv.trakt.trakt.core.shows.navigation.ShowsDestination
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun TraktNavigationBar(
    currentDestination: NavDestination?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onSelected: (NavigationItem) -> Unit = {},
    onReselected: () -> Unit = {},
) {
    val isSearch = remember(currentDestination) {
        currentDestination?.hasRoute(SearchDestination::class) == true
    }

    val searchFocusRequester = remember { FocusRequester() }

    Column(
        modifier = modifier
            .imePadding(),
        verticalArrangement = spacedBy(0.dp),
    ) {
        AnimatedVisibility(
            visible = isSearch,
            enter = fadeIn(tween(delayMillis = 150)) + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            InputField(
                placeholder = stringResource(R.string.input_placeholder_search2),
                icon = painterResource(R.drawable.ic_search),
                loading = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 42.dp)
                    .padding(top = 24.dp)
                    .focusRequester(searchFocusRequester),
            )
        }
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp),
        ) {
            navigationItems.forEachIndexed { index, item ->
                val isSelected = currentDestination
                    ?.hierarchy
                    ?.any { it.hasRoute(item.destination::class) } == true

                NavigationBarItem(
                    alwaysShowLabel = false,
                    selected = isSelected,
                    onClick = {
                        if (!enabled) return@NavigationBarItem

                        if (currentDestination?.hasRoute(item.destination::class) == true) {
                            onReselected()
                            if (isSearch) {
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

@Preview(
    device = "id:pixel_9",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun TraktNavigationBarPreview() {
    TraktTheme {
        TraktNavigationBar(
            currentDestination = null,
        )
    }
}

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
        iconOn = R.drawable.ic_search,
        iconOff = R.drawable.ic_search,
    ),
)
