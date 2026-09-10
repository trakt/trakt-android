package tv.trakt.trakt.core.reactions.media

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.extensions.DevicePreview
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.rememberThousandsFormat
import tv.trakt.trakt.core.reactions.media.data.MediaReaction
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.theme.TraktTheme

private const val MAX_SELECTED_REACTIONS = 3
private const val GRID_COLUMNS = 6
private val itemSize = 40.dp
private val countSpacing = 2.dp
private val itemPadding = 6.dp

@Composable
internal fun MediaReactionsPickerView(
    mediaTitle: String,
    reactions: ImmutableList<MediaReaction>,
    selectedReactions: ImmutableList<MediaReactionEmoji>,
    onReactionClick: (MediaReactionEmoji) -> Unit,
    modifier: Modifier = Modifier,
) {
    val counts = remember(reactions) {
        reactions.associate { it.emoji to it.count }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier,
    ) {
        TraktHeader(
            title = stringResource(R.string.header_reactions),
            subtitle = mediaTitle,
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(GRID_COLUMNS),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            overscrollEffect = null,
        ) {
            items(
                items = MediaReactionEmoji.entries,
                key = { it.slug },
            ) { reaction ->
                val selected = reaction in selectedReactions
                val selectable = selected || selectedReactions.size < MAX_SELECTED_REACTIONS

                ReactionItem(
                    reaction = reaction,
                    count = counts[reaction] ?: 0,
                    selected = selected,
                    enabled = selectable,
                    onClick = { if (selectable) onReactionClick(reaction) },
                )
            }
        }
    }
}

@Composable
private fun ReactionItem(
    reaction: MediaReactionEmoji,
    count: Int,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bounce = remember { Animatable(1F) }
    var wasSelected by remember { mutableStateOf(selected) }

    LaunchedEffect(selected) {
        if (selected && !wasSelected) {
            bounce.snapTo(1F)
            bounce.animateTo(
                targetValue = 1F,
                animationSpec = keyframes {
                    durationMillis = 350
                    1.25F at 100
                    0.9F at 250
                    1F at 350
                },
            )
        }
        wasSelected = selected
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(countSpacing, Alignment.CenterHorizontally),
        modifier = modifier
            .height(itemSize)
            .clip(CircleShape)
            .background(
                color = if (selected) {
                    TraktTheme.colors.chipContent
                } else {
                    TraktTheme.colors.chipContainerOnContent
                },
            )
            .onClick(enabled = enabled, onClick = onClick)
            .padding(horizontal = itemPadding),
    ) {
        Text(
            text = reaction.emoji,
            fontSize = 20.sp,
            modifier = Modifier
                .graphicsLayer {
                    scaleX = bounce.value
                    scaleY = bounce.value
                },
        )

        if (count > 0) {
            Text(
                text = rememberThousandsFormat(count),
                style = TraktTheme.typography.meta.copy(fontSize = 12.sp),
                color = if (selected) {
                    if (TraktTheme.colors.isLight) {
                        TraktTheme.colors.textPrimaryOnAccent
                    } else {
                        Color.Black
                    }
                } else {
                    TraktTheme.colors.textSecondary
                },
                maxLines = 1,
            )
        }
    }
}

@DevicePreview
@Composable
private fun Preview() {
    TraktThemeLightDark {
        MediaReactionsPickerView(
            mediaTitle = "The Matrix",
            reactions = persistentListOf(
                MediaReaction(id = 1, count = 1234, emoji = MediaReactionEmoji.Popcorn),
                MediaReaction(id = 2, count = 87, emoji = MediaReactionEmoji.Fire),
                MediaReaction(id = 3, count = 5, emoji = MediaReactionEmoji.Skull),
            ),
            selectedReactions = persistentListOf(
                MediaReactionEmoji.Popcorn,
                MediaReactionEmoji.Fire,
            ),
            onReactionClick = {},
        )
    }
}
