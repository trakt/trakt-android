package tv.trakt.trakt.core.reactions.media.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.model.reactions.MediaReaction
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private const val GRID_COLUMNS = 6
private val CellSize = 48.dp
private val EmojiFontSize = 28.sp

// Emoji glyphs are narrower than their cell, so the header is inset to start where the glyphs do.
private val HeaderInset = 10.dp

/**
 * Full media reactions picker: the quick reactions first, then every reaction.
 */
@Composable
internal fun MediaReactionsPickerView(
    userReactions: ImmutableList<MediaReaction>,
    isLimitReached: Boolean,
    onReactionClick: (MediaReaction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = spacedBy(12.dp),
        modifier = modifier.verticalScroll(rememberScrollState()),
    ) {
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
                for (reaction in row) {
                    MediaReactionEmoji(
                        reaction = reaction,
                        selected = reaction in userReactions,
                        enabled = !isLimitReached,
                        size = CellSize,
                        fontSize = EmojiFontSize,
                        onClick = { onReactionClick(reaction) },
                    )
                }
            }
        }
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
