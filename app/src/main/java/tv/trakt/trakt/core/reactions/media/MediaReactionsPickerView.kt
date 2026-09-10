package tv.trakt.trakt.core.reactions.media

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import tv.trakt.trakt.common.helpers.extensions.rememberPercentFormat
import tv.trakt.trakt.core.reactions.media.data.MediaReaction
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.theme.TraktTheme

private const val MAX_SELECTED_REACTIONS = 3
private const val GRID_COLUMNS = 6

// Unselected reactions dim when the selection cap is reached.
private const val FADED_ALPHA = 0.5F
private val labelSpacing = 3.dp

// Fixed cell height keeps rows even; emoji centers when the label is absent.
private val itemHeight = 58.dp

@Composable
internal fun MediaReactionsPickerView(
    mediaTitle: String,
    reactions: ImmutableList<MediaReaction>,
    selectedReactions: ImmutableList<MediaReactionEmoji>,
    onReactionClick: (MediaReactionEmoji) -> Unit,
    modifier: Modifier = Modifier,
) {
    val percentages = remember(reactions) {
        val totalCount = reactions.sumOf { it.count }
        if (totalCount == 0) {
            emptyMap()
        } else {
            reactions.associate { reaction ->
                reaction.emoji to (reaction.count * 100F / totalCount).toInt()
            }
        }
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
            horizontalArrangement = Arrangement.spacedBy(6.dp),
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
                    percentage = percentages[reaction] ?: 0,
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
    percentage: Int,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val alpha by animateFloatAsState(
        targetValue = if (enabled) 1F else FADED_ALPHA,
        animationSpec = tween(200),
        label = "reactionAlpha",
    )

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

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(labelSpacing, Alignment.CenterVertically),
        modifier = modifier
            .height(itemHeight)
            .alpha(alpha)
            .clip(RoundedCornerShape(20.dp))
            .background(
                color = if (selected) {
                    TraktTheme.colors.chipContent
                } else {
                    TraktTheme.colors.chipContainerOnContent
                },
            )
            .onClick(enabled = enabled, onClick = onClick),
    ) {
        Text(
            text = reaction.emoji,
            fontSize = 22.sp,
            modifier = Modifier
                .graphicsLayer {
                    scaleX = bounce.value
                    scaleY = bounce.value
                },
        )

        if (percentage > 0) {
            Text(
                text = percentage.rememberPercentFormat(),
                style = TraktTheme.typography.meta.copy(fontSize = 10.sp),
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
