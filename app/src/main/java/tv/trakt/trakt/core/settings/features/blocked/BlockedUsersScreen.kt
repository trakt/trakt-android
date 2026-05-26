package tv.trakt.trakt.core.settings.features.blocked

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration.Short
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.buttons.TertiaryButton
import tv.trakt.trakt.ui.theme.TraktTheme

private val ROW_ITEM_SPACING = 12.dp
private val LIST_ITEM_SPACING = 24.dp

@Composable
internal fun BlockedUsersScreen(
    viewModel: BlockedUsersViewModel,
    onNavigateBack: () -> Unit,
) {
    val snack = LocalSnackbarState.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.info) {
        state.info?.let { info ->
            scope.launch {
                snack.showSnackbar(
                    message = info.get(context),
                    duration = Short,
                )
            }
            viewModel.clearInfo()
        }
    }

    BlockedUsersContent(
        state = state,
        onUnblock = viewModel::unblockUser,
        onBackClick = onNavigateBack,
    )
}

@Composable
private fun BlockedUsersContent(
    state: BlockedUsersState,
    modifier: Modifier = Modifier,
    onUnblock: (User) -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    val contentPadding = PaddingValues(
        start = TraktTheme.spacing.mainPageHorizontalSpace,
        end = TraktTheme.spacing.mainPageHorizontalSpace,
        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding().plus(4.dp),
        bottom = WindowInsets.navigationBars.asPaddingValues()
            .calculateBottomPadding()
            .plus(TraktTheme.size.navigationBarHeight)
            .plus(TraktTheme.spacing.mainPageBottomSpace),
    )

    val scrollConnection = rememberSaveable(saver = SimpleScrollConnection.Saver) {
        SimpleScrollConnection()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .nestedScroll(scrollConnection),
    ) {
        ScrollableBackdropImage(
            translation = scrollConnection.resultOffset,
        )

        LazyColumn(
            contentPadding = contentPadding,
            overscrollEffect = null,
        ) {
            item {
                Row(
                    verticalAlignment = CenterVertically,
                    horizontalArrangement = spacedBy(ROW_ITEM_SPACING),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(TraktTheme.size.titleBarHeight)
                        .graphicsLayer { translationX = -2.dp.toPx() }
                        .onClick { onBackClick() },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back_arrow),
                        tint = TraktTheme.colors.textPrimary,
                        contentDescription = null,
                    )
                    TraktHeader(
                        title = stringResource(R.string.heading_blocked_users),
                    )
                }
            }

            if (state.loading.isLoading) {
                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                    ) {
                        FilmProgressIndicator()
                    }
                }
            } else if (state.users.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.text_no_blocked_users),
                        style = TraktTheme.typography.paragraph,
                        color = TraktTheme.colors.textSecondary,
                    )
                }
            } else {
                items(
                    items = state.users,
                    key = { it.ids.trakt.value },
                ) { user ->
                    BlockedUserRow(
                        user = user,
                        loading = state.loading.isLoading,
                        onUnblock = { onUnblock(user) },
                        modifier = Modifier
                            .padding(bottom = LIST_ITEM_SPACING),
                    )
                }
            }
        }
    }
}

@Composable
private fun BlockedUserRow(
    user: User,
    loading: Boolean,
    onUnblock: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(ROW_ITEM_SPACING),
            modifier = Modifier.weight(1f),
        ) {
            AsyncImage(
                model = user.images?.avatar?.full,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                error = painterResource(R.drawable.ic_person_placeholder),
                placeholder = painterResource(R.drawable.ic_person_placeholder),
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
            )

            Column(
                verticalArrangement = spacedBy(2.dp),
            ) {
                if (user.displayName.isNotBlank()) {
                    Text(
                        text = user.displayName,
                        style = TraktTheme.typography.paragraphSmall,
                        color = TraktTheme.colors.textPrimary,
                        maxLines = 1,
                    )
                }
                Text(
                    text = "@${user.username}",
                    style = TraktTheme.typography.paragraphSmaller,
                    color = TraktTheme.colors.textSecondary,
                    maxLines = 1,
                )
            }
        }

        TertiaryButton(
            text = stringResource(R.string.button_text_unblock),
            enabled = !loading,
            onClick = onUnblock,
            modifier = Modifier.padding(start = ROW_ITEM_SPACING),
        )
    }
}

@Preview(
    device = "id:pixel_6",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        BlockedUsersContent(
            state = BlockedUsersState(
                loading = LoadingState.Done,
                users = persistentListOf(),
            ),
        )
    }
}
