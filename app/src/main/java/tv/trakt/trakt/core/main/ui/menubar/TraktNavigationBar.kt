package tv.trakt.trakt.core.main.ui.menubar

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import tv.trakt.trakt.ui.theme.TraktTheme
import tv.trakt.trakt.common.R as RCommon

@Composable
internal fun TraktNavigationBar(
    currentDestination: NavDestination?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onSelected: (NavigationItem) -> Unit = {},
    onReselected: () -> Unit = {},
) {
    Row(
        modifier = modifier
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

private val navigationItems = listOf(
    NavigationItem(
        destination = HomeDestination,
        label = RCommon.string.home,
        iconOn = RCommon.drawable.ic_home_on,
        iconOff = RCommon.drawable.ic_home_off,
    ),
    NavigationItem(
        destination = ShowsDestination,
        label = RCommon.string.shows,
        iconOn = RCommon.drawable.ic_shows_on,
        iconOff = RCommon.drawable.ic_shows_off,
    ),
    NavigationItem(
        destination = MoviesDestination,
        label = RCommon.string.movies,
        iconOn = RCommon.drawable.ic_movies_on,
        iconOff = RCommon.drawable.ic_movies_off,
    ),
    NavigationItem(
        destination = ListsDestination,
        label = RCommon.string.lists,
        iconOn = RCommon.drawable.ic_lists_on,
        iconOff = RCommon.drawable.ic_lists_off,
    ),
    NavigationItem(
        destination = SearchDestination,
        label = RCommon.string.search,
        iconOn = RCommon.drawable.ic_search,
        iconOff = RCommon.drawable.ic_search,
    ),
)
