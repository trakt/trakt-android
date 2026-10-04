@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.reactions.media.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.model.reactions.MediaReaction
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.InputField
import tv.trakt.trakt.ui.theme.TraktTheme

private const val GRID_COLUMNS = 6
private val CellSize = 44.dp
private val EmojiFontSize = 28.sp
private val HeaderInset = 10.dp

@Composable
internal fun MediaReactionsPickerView(
    userReactions: ImmutableList<MediaReaction>,
    isLimitReached: Boolean,
    onReactionClick: (MediaReaction) -> Unit,
    modifier: Modifier = Modifier,
    searchState: TextFieldState = rememberTextFieldState(),
) {
    val query by remember(searchState) {
        derivedStateOf { searchState.text.toString().trim() }
    }
    val searchResults = remember(query) {
        MediaReaction.entries.filter { it.matches(query) }
    }

    val density = LocalDensity.current
    var tallestContentPx by remember { mutableIntStateOf(0) }

    Column(
        verticalArrangement = spacedBy(16.dp),
        modifier = modifier,
    ) {
        InputField(
            state = searchState,
            placeholder = stringResource(R.string.page_title_search),
            icon = painterResource(R.drawable.ic_search_off),
            imeAction = ImeAction.Search,
            modifier = Modifier.fillMaxWidth(),
        )

        Column(
            verticalArrangement = spacedBy(12.dp),
            modifier = Modifier
                // Keeps the tallest height seen, so the sheet does not shrink while searching.
                .heightIn(min = with(density) { tallestContentPx.toDp() })
                .onSizeChanged { tallestContentPx = maxOf(tallestContentPx, it.height) }
                .verticalScroll(rememberScrollState()),
        ) {
            if (query.isNotEmpty()) {
                ReactionsGrid(
                    reactions = searchResults,
                    userReactions = userReactions,
                    isLimitReached = isLimitReached,
                    onReactionClick = onReactionClick,
                )
                return@Column
            }

            ReactionsGrid(
                reactions = QuickMediaReactions,
                userReactions = userReactions,
                isLimitReached = isLimitReached,
                onReactionClick = onReactionClick,
            )

            Text(
                text = stringResource(R.string.option_text_all),
                style = TraktTheme.typography.heading6,
                color = TraktTheme.colors.textSecondary,
                modifier = Modifier.padding(start = HeaderInset, top = 4.dp),
            )

            ReactionsGrid(
                reactions = MediaReaction.entries,
                userReactions = userReactions,
                isLimitReached = isLimitReached,
                onReactionClick = onReactionClick,
            )
        }
    }
}

private fun MediaReaction.matches(query: String): Boolean {
    return displayName.contains(query, ignoreCase = true) || emoji == query
}

@Composable
private fun ReactionsGrid(
    reactions: List<MediaReaction>,
    userReactions: ImmutableList<MediaReaction>,
    isLimitReached: Boolean,
    onReactionClick: (MediaReaction) -> Unit,
) {
    Column(verticalArrangement = spacedBy(4.dp)) {
        reactions.chunked(GRID_COLUMNS).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Partial rows keep their slots, so search results stay on the grid.
                for (index in 0 until GRID_COLUMNS) {
                    val reaction = row.getOrNull(index)
                    if (reaction == null) {
                        Spacer(Modifier.size(CellSize))
                        continue
                    }

                    NamedReactionEmoji(
                        reaction = reaction,
                        selected = reaction in userReactions,
                        enabled = !isLimitReached,
                        onClick = { onReactionClick(reaction) },
                    )
                }
            }
        }
    }
}

@Composable
private fun NamedReactionEmoji(
    reaction: MediaReaction,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val tooltipState = rememberTooltipState()
    val scope = rememberCoroutineScope()

    TooltipBox(
        state = tooltipState,
        positionProvider = rememberTooltipPositionProvider(
            positioning = TooltipAnchorPosition.Above,
        ),
        enableUserInput = false,
        tooltip = {
            PlainTooltip(
                containerColor = TraktTheme.colors.tooltipContainer,
                contentColor = TraktTheme.colors.tooltipContent,
            ) {
                Text(
                    text = reaction.displayName,
                    style = TraktTheme.typography.paragraphSmall,
                )
            }
        },
    ) {
        MediaReactionEmoji(
            reaction = reaction,
            selected = selected,
            enabled = enabled,
            size = CellSize,
            fontSize = EmojiFontSize,
            onClick = onClick,
            onLongClick = { scope.launch { tooltipState.show() } },
        )
    }
}

@Preview(name = "Empty", heightDp = 640)
@Composable
private fun PreviewEmpty() {
    TraktThemeLightDark {
        MediaReactionsPickerView(
            userReactions = persistentListOf(),
            isLimitReached = false,
            onReactionClick = {},
        )
    }
}

@Preview(name = "User picks", heightDp = 640)
@Composable
private fun PreviewUserPicks() {
    TraktThemeLightDark {
        MediaReactionsPickerView(
            userReactions = persistentListOf(MediaReaction.HeartEyes, MediaReaction.Popcorn),
            isLimitReached = false,
            onReactionClick = {},
        )
    }
}

@Preview(name = "Searching", heightDp = 320)
@Composable
private fun PreviewSearching() {
    TraktThemeLightDark {
        MediaReactionsPickerView(
            userReactions = persistentListOf(),
            isLimitReached = false,
            onReactionClick = {},
            searchState = rememberTextFieldState("heart"),
        )
    }
}

@Preview(name = "Limit reached", heightDp = 640)
@Composable
private fun PreviewLimitReached() {
    TraktThemeLightDark {
        MediaReactionsPickerView(
            userReactions = persistentListOf(MediaReaction.Popcorn, MediaReaction.Fire, MediaReaction.Nerd),
            isLimitReached = true,
            onReactionClick = {},
        )
    }
}
