package tv.trakt.trakt.tv.core.main.ui.drawer

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Start
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.tv.material3.DrawerValue
import androidx.tv.material3.DrawerValue.Closed
import androidx.tv.material3.DrawerValue.Open
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.ui.theme.colors.Purple50
import tv.trakt.trakt.tv.R
import tv.trakt.trakt.tv.core.auth.navigation.AuthDestination
import tv.trakt.trakt.tv.core.home.navigation.HomeDestination
import tv.trakt.trakt.tv.core.lists.navigation.ListsDestination
import tv.trakt.trakt.tv.core.main.model.TraktDrawerItem
import tv.trakt.trakt.tv.core.movies.navigation.MoviesDestination
import tv.trakt.trakt.tv.core.profile.navigation.ProfileDestination
import tv.trakt.trakt.tv.core.shows.navigation.ShowsDestination
import tv.trakt.trakt.tv.helpers.extensions.onClick
import tv.trakt.trakt.tv.helpers.preview.PreviewData
import tv.trakt.trakt.tv.ui.theme.TraktTheme
import kotlin.math.max
import tv.trakt.trakt.common.R as RCommon

@Composable
internal fun NavigationDrawerContent(
    drawerValue: DrawerValue,
    currentDestination: NavDestination?,
    profile: User?,
    modifier: Modifier = Modifier,
    onProfileSelected: () -> Unit = {},
    onSelected: (TraktDrawerItem) -> Unit = {},
    onReselected: () -> Unit = {},
) {
    val focusRequesters = remember {
        List(drawerItems.size) { FocusRequester() }
    }

    Column(
        verticalArrangement = spacedBy(0.dp, Alignment.CenterVertically),
        modifier = modifier
            .fillMaxHeight()
            .background(
                color = TraktTheme.colors.navigationBackground,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(top = 12.dp, bottom = 4.dp)
            .animateContentSize(
                animationSpec = tween(200),
            )
            .focusProperties {
                onEnter = {
                    val index = drawerItems.indexOfFirst {
                        currentDestination?.hasRoute(it.destination::class) == true
                    }
                    focusRequesters[max(0, index)].requestFocus()
                }
            }
            .focusGroup(),
    ) {
        val isOpen = (drawerValue == Open)
        Image(
            painter = painterResource(
                if (isOpen) R.drawable.ic_trakt_banner else R.drawable.ic_trakt,
            ),
            contentDescription = null,
            modifier = Modifier
                .padding(start = 11.5.dp)
                .align(Start)
                .height(26.dp),
        )

        Spacer(modifier = Modifier.weight(1f))

        NavigationDrawerItems(
            currentDestination = currentDestination,
            focusRequesters = focusRequesters,
            onReselected = onReselected,
            onSelected = onSelected,
            isOpen = isOpen,
        )

        Spacer(modifier = Modifier.weight(1f))

        NavigationProfileItem(
            profile = profile,
            isOpen = isOpen,
            onProfileSelected = {
                if (currentDestination?.hasRoute<AuthDestination>() == true) {
                    onReselected()
                } else {
                    onProfileSelected()
                }
            },
        )
    }
}

@Composable
private fun NavigationDrawerItems(
    currentDestination: NavDestination?,
    focusRequesters: List<FocusRequester>,
    onReselected: () -> Unit,
    onSelected: (TraktDrawerItem) -> Unit,
    isOpen: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = spacedBy(10.dp),
        modifier = modifier,
    ) {
        drawerItems.forEachIndexed { index, drawerItem ->
            val isSelected = currentDestination
                ?.hierarchy
                ?.any { it.hasRoute(drawerItem.destination::class) } == true

            var hasFocus by remember { mutableStateOf(false) }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = spacedBy(10.dp),
                modifier = Modifier
                    .padding(horizontal = 5.dp)
                    .background(
                        color = if (isSelected && isOpen) {
                            Purple50
                        } else {
                            Color.Transparent
                        },
                        shape = RoundedCornerShape(12.dp),
                    )
                    .border(
                        width = 2.dp,
                        color = if (hasFocus && isOpen) {
                            TraktTheme.colors.accent
                        } else {
                            Color.Transparent
                        },
                        shape = RoundedCornerShape(12.dp),
                    )
                    .padding(horizontal = 8.dp, vertical = 7.dp)
                    .focusRequester(
                        focusRequesters.getOrElse(index) {
                            FocusRequester.Default
                        },
                    )
                    .onFocusChanged { hasFocus = it.isFocused }
                    .onClick {
                        val destination = drawerItem.destination
                        if (currentDestination?.hasRoute(destination::class) == true) {
                            onReselected()
                            return@onClick
                        }

                        if (currentDestination?.hasRoute<AuthDestination>() == true &&
                            destination in listOf(HomeDestination, ProfileDestination)
                        ) {
                            onReselected()
                            return@onClick
                        }

                        onSelected(drawerItem)
                    },
            ) {
                Icon(
                    painter = painterResource(if (isSelected) drawerItem.iconOn else drawerItem.iconOff),
                    contentDescription = null,
                    tint = if (isSelected) {
                        TraktTheme.colors.navigationItemOn
                    } else {
                        TraktTheme.colors.navigationItemOff
                    },
                    modifier = Modifier
                        .requiredSize(24.dp)
                        .padding(3.dp),
                )

                if (isOpen) {
                    Text(
                        text = stringResource(drawerItem.label).uppercase(),
                        style = TraktTheme.typography.navigationLabel,
                        color = if (isSelected) {
                            TraktTheme.colors.navigationItemOn
                        } else {
                            TraktTheme.colors.navigationItemOff
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun NavigationProfileItem(
    profile: User?,
    isOpen: Boolean,
    onProfileSelected: () -> Unit,
) {
    var isFocused by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(10.dp),
        modifier = Modifier
            .padding(horizontal = 3.dp)
            .border(
                width = 2.dp,
                color = if (isFocused) {
                    TraktTheme.colors.accent
                } else {
                    Color.Transparent
                },
                shape = RoundedCornerShape(14.dp),
            )
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .onFocusChanged { isFocused = it.isFocused }
            .onClick {
                onProfileSelected()
            },
    ) {
        Box(
            modifier = Modifier.size(28.dp),
        ) {
            if (profile?.hasImage == true) {
                AsyncImage(
                    model = profile.images?.avatar?.full,
                    contentDescription = "User avatar",
                    contentScale = ContentScale.Crop,
                    error = painterResource(RCommon.drawable.ic_person_placeholder),
                    modifier = Modifier
                        .border(2.dp, Color.White, CircleShape)
                        .clip(CircleShape),
                )
            } else {
                Image(
                    painter = painterResource(RCommon.drawable.ic_person_placeholder),
                    contentDescription = null,
                    modifier = Modifier
                        .border(2.dp, Color.White, CircleShape)
                        .clip(CircleShape),
                )
            }

            if (profile?.isAnyVip == true) {
                Icon(
                    painter = painterResource(R.drawable.ic_crown),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .graphicsLayer {
                            val offset = 4.dp
                            translationX = offset.toPx()
                            translationY = -offset.toPx()
                        }
                        .shadow(
                            elevation = 1.dp,
                            shape = CircleShape,
                        )
                        .background(Color.Red, shape = CircleShape)
                        .size(16.dp)
                        .padding(bottom = (3.5).dp, top = 3.dp),
                )
            }
        }

        if (isOpen) {
            val profileText = if (profile == null) {
                stringResource(R.string.log_in)
            } else {
                stringResource(R.string.view_profile)
            }

            val profileTextSize = if (profile == null) {
                TraktTheme.typography.navigationLabel
            } else {
                TraktTheme.typography.navigationLabel.copy(fontSize = 10.sp)
            }

            Column(
                verticalArrangement = spacedBy(1.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (profile?.username != null) {
                    Text(
                        text = profile.username,
                        style = TraktTheme.typography.navigationLabel.copy(
                            fontSize = 11.sp,
                            letterSpacing = TextUnit.Unspecified,
                        ),
                        color = TraktTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Start,
                    )
                }
                Text(
                    text = profileText.uppercase(),
                    style = profileTextSize,
                    color = TraktTheme.colors.navigationItemOff,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start,
                )
            }
        }
    }
}

@Preview(device = "id:tv_4k")
@Composable
private fun Preview() {
    TraktTheme {
        Row(
            horizontalArrangement = spacedBy(32.dp),
        ) {
            NavigationDrawerContent(
                drawerValue = Closed,
                currentDestination = null,
                profile = PreviewData.user1,
                onSelected = {},
                onReselected = {},
            )
            NavigationDrawerContent(
                drawerValue = Closed,
                currentDestination = null,
                profile = PreviewData.user1.copy(isVip = true),
                onSelected = {},
                onReselected = {},
            )
            NavigationDrawerContent(
                drawerValue = Open,
                currentDestination = null,
                profile = PreviewData.user1,
                onSelected = {},
                onReselected = {},
                modifier = Modifier.width(TraktTheme.size.navigationDrawerSize),
            )
        }
    }
}

private val drawerItems = listOf(
    TraktDrawerItem(
        destination = HomeDestination,
        label = RCommon.string.home,
        iconOn = RCommon.drawable.ic_home_on,
        iconOff = RCommon.drawable.ic_home_off,
    ),
    TraktDrawerItem(
        destination = ShowsDestination,
        label = RCommon.string.shows,
        iconOn = RCommon.drawable.ic_shows_on,
        iconOff = RCommon.drawable.ic_shows_off,
    ),
    TraktDrawerItem(
        destination = MoviesDestination,
        label = RCommon.string.movies,
        iconOn = RCommon.drawable.ic_movies_on,
        iconOff = RCommon.drawable.ic_movies_off,
    ),
    TraktDrawerItem(
        destination = ListsDestination,
        label = RCommon.string.lists,
        iconOn = RCommon.drawable.ic_lists_on,
        iconOff = RCommon.drawable.ic_lists_off,
    ),
)
