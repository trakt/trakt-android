package tv.trakt.trakt.core.lists.sheets.collaborators

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.InputField
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.usertier.UserAvatar
import tv.trakt.trakt.ui.theme.TraktTheme

private val ListMaxHeight = 420.dp

@Composable
internal fun ManageCollaboratorsView(
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

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
    ) {
        TraktHeader(
            title = stringResource(R.string.page_title_manage_collaborators),
        )

        Text(
            text = stringResource(R.string.description_manage_collaborators_shared),
            style = TraktTheme.typography.paragraphSmall,
            color = TraktTheme.colors.textSecondary,
        )

        InputField(
            state = searchState,
            icon = painterResource(R.drawable.ic_search_on),
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
                        .padding(vertical = 24.dp),
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
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            }
            else -> {
                CandidatesList(
                    candidates = visibleCandidates,
                    pendingUserId = state.pendingUserId,
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
    onAddClick: (User) -> Unit,
    onRemoveClick: (User) -> Unit,
) {
    val collaborators = remember(candidates) { candidates.filter { it.isCollaborator } }
    val followers = remember(candidates) { candidates.filterNot { it.isCollaborator } }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.heightIn(max = ListMaxHeight),
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
    val label = stringResource(
        when {
            candidate.isCollaborator -> R.string.button_label_remove_collaborator
            else -> R.string.button_label_add_collaborator
        },
        user.displayName,
    )

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
                modifier = Modifier.size(40.dp),
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
                Icon(
                    painter = painterResource(
                        when {
                            candidate.isCollaborator -> R.drawable.ic_check_2
                            else -> R.drawable.ic_plus
                        },
                    ),
                    contentDescription = label,
                    tint = TraktTheme.colors.textPrimary,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        ManageCollaboratorsView(
            state = ManageCollaboratorsState(
                loading = LoadingState.Done,
                candidates = persistentListOf(
                    CollaboratorCandidate(PreviewData.user1, isCollaborator = true, isMutual = true),
                    CollaboratorCandidate(
                        user = PreviewData.user1.copy(
                            ids = PreviewData.user1.ids.copy(trakt = TraktId(2)),
                            username = "other",
                        ),
                        isCollaborator = false,
                        isMutual = false,
                    ),
                ),
            ),
            onAddClick = {},
            onRemoveClick = {},
        )
    }
}
