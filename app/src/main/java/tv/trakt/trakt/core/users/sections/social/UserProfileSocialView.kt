@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.users.sections.social

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.profile.sections.social.model.SocialFilter
import tv.trakt.trakt.core.profile.sections.social.ui.SocialUserView
import tv.trakt.trakt.core.profile.sections.social.ui.SocialUserViewSkeleton
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktSectionHeader
import tv.trakt.trakt.ui.components.chips.FilterChip
import tv.trakt.trakt.ui.components.chips.FilterChipGroup
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun UserProfileSocialView(
    modifier: Modifier = Modifier,
    viewModel: UserProfileSocialViewModel,
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
    onUserClick: ((User) -> Unit)? = null,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    UserProfileSocialContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onCollapse = viewModel::setCollapsed,
        onFilterClick = viewModel::setFilter,
        onUserClick = { onUserClick?.invoke(it) },
    )
}

@Composable
internal fun UserProfileSocialContent(
    state: UserProfileSocialState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onCollapse: (Boolean) -> Unit = {},
    onFilterClick: (SocialFilter) -> Unit = {},
    onUserClick: (User) -> Unit = {},
) {
    var animateCollapse by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .animateContentSize(
                animationSpec = if (animateCollapse) spring() else snap(),
            ),
    ) {
        TraktSectionHeader(
            title = stringResource(R.string.list_title_social),
            chevron = false,
            collapsed = state.collapsed ?: false,
            onCollapseClick = {
                animateCollapse = true
                val current = (state.collapsed ?: false)
                onCollapse(!current)
            },
            modifier = Modifier
                .padding(headerPadding),
        )

        if (state.collapsed != true) {
            ContentFilters(
                state = state,
                headerPadding = headerPadding,
                onFilterClick = onFilterClick,
            )

            Crossfade(
                targetState = state.loading,
                animationSpec = tween(200),
            ) { loading ->
                when (loading) {
                    Idle, Loading -> {
                        ContentLoadingList(
                            visible = loading.isLoading,
                            contentPadding = contentPadding,
                        )
                    }

                    Done -> {
                        when {
                            state.error != null -> {
                                Text(
                                    text = "${stringResource(R.string.error_text_unexpected_error_short)}\n\n${state.error}",
                                    color = TraktTheme.colors.textSecondary,
                                    style = TraktTheme.typography.meta,
                                    maxLines = 10,
                                    modifier = Modifier.padding(contentPadding),
                                )
                            }

                            state.items?.isEmpty() == true -> {
                                Text(
                                    text = stringResource(R.string.list_placeholder_empty),
                                    color = TraktTheme.colors.textSecondary,
                                    style = TraktTheme.typography.heading6,
                                    modifier = Modifier.padding(contentPadding),
                                )
                            }

                            else -> {
                                ContentList(
                                    listItems = (state.items ?: emptyList()).toImmutableList(),
                                    contentPadding = contentPadding,
                                    onUserClick = onUserClick,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContentFilters(
    headerPadding: PaddingValues,
    state: UserProfileSocialState,
    onFilterClick: (SocialFilter) -> Unit,
) {
    val usersCount = state.items?.size ?: 0
    FilterChipGroup(
        paddingHorizontal = headerPadding,
        paddingVertical = PaddingValues(top = 13.dp, bottom = 16.dp),
    ) {
        for (filter in SocialFilter.entries) {
            FilterChip(
                selected = state.filter == filter,
                text = stringResource(filter.displayRes),
                leadingContent = {
                    Icon(
                        painter = painterResource(filter.iconRes),
                        contentDescription = null,
                        tint = TraktTheme.colors.textPrimary,
                        modifier = Modifier
                            .size(17.dp)
                            .padding(end = 2.dp),
                    )
                },
                endContent = {
                    if (state.loading == Done && state.filter == filter) {
                        Text(
                            text = " • $usersCount",
                            style = TraktTheme.typography.buttonTertiary,
                            color = TraktTheme.colors.textPrimary,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                        )
                    } else {
                        null
                    }
                },
                onClick = { onFilterClick(filter) },
            )
        }
    }
}

@Composable
private fun ContentLoadingList(
    visible: Boolean = true,
    contentPadding: PaddingValues,
) {
    LazyRow(
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
        userScrollEnabled = false,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (visible) 1F else 0F),
    ) {
        items(count = 10) {
            SocialUserViewSkeleton()
        }
    }
}

@Composable
private fun ContentList(
    listItems: ImmutableList<User>,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues,
    onUserClick: (User) -> Unit = {},
) {
    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
    ) {
        items(
            items = listItems,
            key = { it.ids.trakt.value },
        ) { user ->
            SocialUserView(user = user, onUserClick = { onUserClick(user) })
        }
    }
}

// Previews

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        UserProfileSocialContent(
            state = UserProfileSocialState(
                loading = Idle,
            ),
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview2() {
    TraktTheme {
        UserProfileSocialContent(
            state = UserProfileSocialState(
                loading = Loading,
            ),
        )
    }
}
