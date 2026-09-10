@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.reactions.media

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import tv.trakt.trakt.common.helpers.extensions.DevicePreview
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

// Placeholder shown until the media has any real top reactions.
private val defaultReactions = persistentListOf(
    MediaReactionEmoji.ThumbsUp,
    MediaReactionEmoji.Popcorn,
    MediaReactionEmoji.Heart,
)

@Composable
internal fun MediaReactionsChip(
    mediaTitle: String,
    topReactions: ImmutableList<MediaReactionEmoji>,
    onReactionsSelected: (ImmutableList<MediaReactionEmoji>) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPicker by remember { mutableStateOf(false) }
    var selection by remember(topReactions) { mutableStateOf(topReactions.toPersistentList()) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .onClick { showPicker = true },
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_reaction_add),
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
            modifier = Modifier
                .size(22.dp)
                .graphicsLayer {
                    translationY = 0.5.dp.toPx()
                },
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            topReactions.ifEmpty { defaultReactions }.forEach { reaction ->
                Text(
                    text = reaction.emoji,
                    fontSize = 16.sp,
                )
            }
        }
    }

    MediaReactionsSheet(
        visible = showPicker,
        mediaTitle = mediaTitle,
        selectedReactions = selection,
        onReactionClick = { reaction ->
            selection = when (reaction) {
                in selection -> selection.remove(reaction)
                else -> selection.add(reaction)
            }
        },
        onDismiss = {
            showPicker = false
            onReactionsSelected(selection)
        },
    )
}

@DevicePreview
@Composable
private fun Preview() {
    TraktTheme {
        MediaReactionsChip(
            mediaTitle = "The Matrix",
            topReactions = persistentListOf(
                MediaReactionEmoji.SmilingFaceWithTear,
                MediaReactionEmoji.Popcorn,
                MediaReactionEmoji.ColdFace,
            ),
            onReactionsSelected = {},
        )
    }
}

@DevicePreview
@Composable
private fun PreviewEmpty() {
    TraktTheme {
        MediaReactionsChip(
            mediaTitle = "The Matrix",
            topReactions = persistentListOf(),
            onReactionsSelected = {},
        )
    }
}
