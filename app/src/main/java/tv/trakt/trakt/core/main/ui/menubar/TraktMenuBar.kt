package tv.trakt.trakt.core.main.ui.menubar

import InputField
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.discover.navigation.DiscoverDestination
import tv.trakt.trakt.core.home.navigation.HomeDestination
import tv.trakt.trakt.core.lists.navigation.ListsDestination
import tv.trakt.trakt.core.main.MainSearchState
import tv.trakt.trakt.core.main.MainSearchStateHolder
import tv.trakt.trakt.core.main.model.NavigationItem
import tv.trakt.trakt.core.profile.navigation.ProfileDestination
import tv.trakt.trakt.core.search.model.SearchInput
import tv.trakt.trakt.core.search.navigation.SearchDestination
import tv.trakt.trakt.core.search.views.SearchFiltersList
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
        destination = DiscoverDestination,
        label = R.string.page_title_shows,
        iconOn = R.drawable.ic_discover_on,
        iconOff = R.drawable.ic_discover_off,
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
    user: User? = null,
    searchState: MainSearchStateHolder,
    onSelected: (NavigationItem) -> Unit = {},
    onProfileSelected: () -> Unit = {},
    onReselected: () -> Unit = {},
    onSearchInput: (SearchInput) -> Unit = {},
) {
    TraktMenuBarContent(
        destination = currentDestination,
        modifier = modifier,
        enabled = enabled,
        user = user,
        stateHolder = searchState,
        onSelected = onSelected,
        onProfileClick = onProfileSelected,
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
    user: User? = null,
    onSelected: (NavigationItem) -> Unit = {},
    onProfileClick: () -> Unit = {},
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
            verticalAlignment = Alignment.CenterVertically,
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

            val profileSelected = destination
                ?.hierarchy
                ?.any { it.hasRoute(ProfileDestination::class) } == true

            ProfileItem(
                selected = profileSelected,
                vip = user?.isAnyVip == true,
                userAvatar = user?.images?.avatar?.full,
                onClick = {
                    if (!enabled || profileSelected) {
                        return@ProfileItem
                    }
                    onProfileClick()
                },
                modifier = Modifier
                    .graphicsLayer {
                        translationY = -1.dp.toPx()
                    }
                    .weight(1f),
            )
        }
    }
}

@Composable
fun ProfileItem(
    modifier: Modifier = Modifier,
    vip: Boolean = false,
    selected: Boolean = false,
    userAvatar: String? = null,
    onClick: () -> Unit = {},
) {
    val size = 26.dp
    val border = (1.6).dp
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .onClick(onClick = onClick),
    ) {
        val vipAccent = TraktTheme.colors.vipAccent
        val accent = TraktTheme.colors.accent

        val borderColor = remember(vip, selected) {
            when {
                selected -> accent
                vip -> vipAccent
                else -> White
            }
        }
        if (userAvatar != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(userAvatar)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                error = painterResource(R.drawable.ic_person_placeholder),
                modifier = Modifier
                    .size(size)
                    .border(border, borderColor, CircleShape)
                    .clip(CircleShape),
            )
        } else {
            Image(
                painter = painterResource(R.drawable.ic_person_placeholder),
                contentDescription = null,
                modifier = Modifier
                    .size(size)
                    .border(border, borderColor, CircleShape)
                    .clip(CircleShape),
            )
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
                            modifier = Modifier
                                .size(18.dp)
                                .onClick {
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
