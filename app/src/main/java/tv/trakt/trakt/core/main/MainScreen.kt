package tv.trakt.trakt.core.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import tv.trakt.trakt.core.main.model.NavigationItem
import tv.trakt.trakt.core.shows.navigation.ShowsDestination
import tv.trakt.trakt.core.shows.navigation.showsScreen
import tv.trakt.trakt.ui.theme.TraktTheme
import tv.trakt.trakt.common.R as RCommon

@Composable
internal fun MainScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    val currentDestination = navController
        .currentBackStackEntryFlow
        .collectAsStateWithLifecycle(initialValue = null)

    var selectedDestination by remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary),
    ) {
        MainNavHost(
            navController = navController,
        )

        NavigationBar(
            containerColor = TraktTheme.colors.navigationContainer,
            contentColor = Color.Transparent,
            modifier = Modifier
                .align(BottomCenter)
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        topStart = 24.dp,
                        topEnd = 24.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp,
                    ),
                ),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
            ) {
                navigationItems.forEachIndexed { index, item ->
                    val isSelected = (selectedDestination == index)
                    NavigationBarItem(
                        alwaysShowLabel = false,
                        selected = isSelected,
                        onClick = { selectedDestination = index },
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
}

@Composable
private fun MainNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = ShowsDestination,
        modifier = modifier,
    ) {
        with(navController) {
            showsScreen(
                onNavigateToShow = {},
            )
        }
    }
}

@Preview
@Composable
private fun MainScreenPreview() {
    TraktTheme {
        MainScreen()
    }
}

private val navigationItems = listOf(
    NavigationItem(
        label = RCommon.string.home,
        iconOn = RCommon.drawable.ic_home_on,
        iconOff = RCommon.drawable.ic_home_off,
    ),
    NavigationItem(
        label = RCommon.string.shows,
        iconOn = RCommon.drawable.ic_shows_on,
        iconOff = RCommon.drawable.ic_shows_off,
    ),
    NavigationItem(
        label = RCommon.string.movies,
        iconOn = RCommon.drawable.ic_movies_on,
        iconOff = RCommon.drawable.ic_movies_off,
    ),
    NavigationItem(
        label = RCommon.string.lists,
        iconOn = RCommon.drawable.ic_lists_on,
        iconOff = RCommon.drawable.ic_lists_off,
    ),
    NavigationItem(
        label = RCommon.string.search,
        iconOn = RCommon.drawable.ic_search,
        iconOff = RCommon.drawable.ic_search,
    ),
)
