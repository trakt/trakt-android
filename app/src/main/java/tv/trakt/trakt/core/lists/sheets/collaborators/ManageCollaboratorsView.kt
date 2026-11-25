package tv.trakt.trakt.core.lists.sheets.collaborators

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType
import tv.trakt.trakt.core.lists.sheets.collaborators.ManageCollaboratorsEvent.Failed
import tv.trakt.trakt.core.lists.sheets.collaborators.ManageCollaboratorsEvent.LimitReached
import tv.trakt.trakt.core.lists.sheets.collaborators.ManageCollaboratorsEvent.ListMoved
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.InputField
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.usertier.UserAvatar
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ManageCollaboratorsView(
    viewModel: ManageCollaboratorsViewModel,
    modifier: Modifier = Modifier,
    onListMoved: (PersonalListType) -> Unit = {},
    onLimitReached: () -> Unit = {},
    onError: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val currentOnListMoved by rememberUpdatedState(onListMoved)
    val currentOnLimitReached by rememberUpdatedState(onLimitReached)
    val currentOnError by rememberUpdatedState(onError)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ListMoved -> currentOnListMoved(event.destination)
                LimitReached -> currentOnLimitReached()
                Failed -> currentOnError()
            }
        }
    }

    ManageCollaboratorsContent(
        state = state,
        onAddClick = viewModel::addCollaborator,
        onRemoveClick = viewModel::removeCollaborator,
        modifier = modifier,
    )
}

@Composable
private fun ManageCollaboratorsContent(
    state: ManageCollaboratorsState,
    onAddClick: (User) -> Unit,
    onRemoveClick: (User) -> Unit,
    modifier: Modifier = Modifier,
) {
    val searchState = rememberTextFieldState()

    val visibleCandidates by remember(state.candidates) {
        derivedStateOf {
            val term = searchState.text.trim().toString()
            when {
                term.isEmpty() -> state.candidates
                else -> state.candidates.filter {
                    "${it.user.displayName} ${it.user.username}".contains(term, ignoreCase = true)
                }
            }
        }
    }

    val bottomPadding = 24.dp + WindowInsets.navigationBars
        .union(WindowInsets.ime)
        .asPaddingValues()
        .calculateBottomPadding()

    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = modifier.padding(horizontal = 24.dp),
    ) {
        TraktHeader(
            title = stringResource(R.string.page_title_manage_collaborators),
            subtitle = stringResource(R.string.description_manage_collaborators_shared),
            maxSubtitleLines = 10,
        )

        InputField(
            state = searchState,
            icon = painterResource(R.drawable.ic_search_off),
            placeholder = stringResource(R.string.input_placeholder_search_collaborators),
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxWidth(),
        )

        when {
            state.loading.isLoading || state.loading == LoadingState.Idle -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 24.dp + bottomPadding),
                ) {
                    FilmProgressIndicator(size = 24.dp)
                }
            }
            visibleCandidates.isEmpty() -> {
                Text(
                    text = stringResource(
                        when {
                            state.candidates.isEmpty() -> R.string.text_no_collaborator_candidates
                            else -> R.string.list_placeholder_empty
                        },
                    ),
                    style = TraktTheme.typography.paragraphSmall,
                    color = TraktTheme.colors.textSecondary,
                    modifier = Modifier.padding(top = 12.dp, bottom = 12.dp + bottomPadding),
                )
            }
            else -> {
                CandidatesList(
                    candidates = visibleCandidates,
                    pendingUserId = state.pendingUserId,
                    contentPadding = PaddingValues(bottom = bottomPadding),
                    onAddClick = onAddClick,
                    onRemoveClick = onRemoveClick,
                )
            }
        }
    }
}

@Composable
private fun CandidatesList(
    candidates: List<CollaboratorCandidate>,
    pendingUserId: TraktId?,
    contentPadding: PaddingValues,
    onAddClick: (User) -> Unit,
    onRemoveClick: (User) -> Unit,
) {
    val collaborators = remember(candidates) { candidates.filter { it.isCollaborator } }
    val followers = remember(candidates) { candidates.filterNot { it.isCollaborator } }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = contentPadding,
        overscrollEffect = null,
    ) {
        if (collaborators.isNotEmpty()) {
            item(key = "collaborators_title") {
                SectionTitle(text = stringResource(R.string.list_title_collaborators))
            }
            items(
                items = collaborators,
                key = { "collaborator_${it.user.ids.trakt.value}" },
            ) { candidate ->
                CollaboratorRow(
                    candidate = candidate,
                    pending = pendingUserId == candidate.user.ids.trakt,
                    onClick = { onRemoveClick(candidate.user) },
                )
            }
        }

        if (followers.isNotEmpty()) {
            item(key = "followers_title") {
                SectionTitle(
                    text = stringResource(R.string.button_text_followers),
                    modifier = Modifier.padding(top = if (collaborators.isEmpty()) 0.dp else 8.dp),
                )
            }
            items(
                items = followers,
                key = { "follower_${it.user.ids.trakt.value}" },
            ) { candidate ->
                CollaboratorRow(
                    candidate = candidate,
                    pending = pendingUserId == candidate.user.ids.trakt,
                    onClick = { onAddClick(candidate.user) },
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text.uppercase(),
        style = TraktTheme.typography.meta,
        color = TraktTheme.colors.textSecondary,
        modifier = modifier,
    )
}

@Composable
private fun CollaboratorRow(
    candidate: CollaboratorCandidate,
    pending: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val user = candidate.user

    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .onClick(enabled = !pending, onClick = onClick),
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(12.dp),
            modifier = Modifier.weight(1F),
        ) {
            UserAvatar(
                user = user,
                borderWidth = 0.dp,
                modifier = Modifier.size(42.dp),
            )

            Column(
                verticalArrangement = spacedBy(2.dp),
            ) {
                Text(
                    text = user.displayName,
                    style = TraktTheme.typography.paragraphSmall,
                    color = TraktTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "@${user.username}",
                    style = TraktTheme.typography.paragraphSmaller,
                    color = TraktTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(start = 12.dp)
                .size(24.dp),
        ) {
            if (pending) {
                FilmProgressIndicator(size = 20.dp)
            } else {
                Checkbox(
                    checked = candidate.isCollaborator,
                    onCheckedChange = null,
                    enabled = false,
                    colors = CheckboxDefaults.colors().copy(
                        disabledUncheckedBoxColor = Color.Transparent,
                        disabledUncheckedBorderColor = TraktTheme.colors.textPrimary,
                        disabledCheckedBoxColor = TraktTheme.colors.textPrimary,
                        disabledBorderColor = TraktTheme.colors.textPrimary,
                        checkedCheckmarkColor = TraktTheme.colors.dialogContainer,
                    ),
                )
            }
        }
    }
}

private val previewCandidates = persistentListOf(
    CollaboratorCandidate(PreviewData.user1, isCollaborator = true, isMutual = true),
    CollaboratorCandidate(
        user = PreviewData.user1.copy(
            ids = PreviewData.user1.ids.copy(trakt = TraktId(2)),
            username = "other",
        ),
        isCollaborator = false,
        isMutual = false,
    ),
)

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        ManageCollaboratorsContent(
            state = ManageCollaboratorsState(
                loading = LoadingState.Done,
                candidates = previewCandidates,
            ),
            onAddClick = {},
            onRemoveClick = {},
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun PreviewLoading() {
    TraktThemePreview {
        ManageCollaboratorsContent(
            state = ManageCollaboratorsState(
                loading = LoadingState.Loading,
            ),
            onAddClick = {},
            onRemoveClick = {},
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun PreviewPending() {
    TraktThemePreview {
        ManageCollaboratorsContent(
            state = ManageCollaboratorsState(
                loading = LoadingState.Done,
                candidates = previewCandidates,
                pendingUserId = TraktId(2),
            ),
            onAddClick = {},
            onRemoveClick = {},
        )
    }
}
