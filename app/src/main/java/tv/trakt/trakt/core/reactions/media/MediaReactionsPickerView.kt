package tv.trakt.trakt.core.reactions.media

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.extensions.DevicePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.theme.TraktTheme

private const val MAX_SELECTED_REACTIONS = 3
private const val GRID_COLUMNS = 6
private const val FADED_ALPHA = 0.4F
private val itemSize = 40.dp
private val gridHeight = 260.dp

@Composable
internal fun MediaReactionsPickerView(
    mediaTitle: String,
    selectedReactions: ImmutableList<MediaReactionEmoji>,
    onReactionClick: (MediaReactionEmoji) -> Unit,
    modifier: Modifier = Modifier,
) {
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
                    selected = selected,
                    selectable = selectable,
                    onClick = { if (selectable) onReactionClick(reaction) },
                )
            }
        }
    }
}

@Composable
private fun ReactionItem(
    reaction: MediaReactionEmoji,
    selected: Boolean,
    selectable: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val alpha by animateFloatAsState(
        targetValue = if (selectable) 1F else FADED_ALPHA,
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

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(itemSize)
            .alpha(alpha)
            .clip(CircleShape)
            .background(
                color = if (selected) {
                    TraktTheme.colors.chipContent
                } else {
                    TraktTheme.colors.chipContainerOnContent
                },
            )
            .clickable(onClick = onClick),
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
    }
}

@DevicePreview
@Composable
private fun Preview() {
    TraktTheme {
        MediaReactionsPickerView(
            mediaTitle = "The Matrix",
            selectedReactions = persistentListOf(
                MediaReactionEmoji.Popcorn,
                MediaReactionEmoji.Fire,
            ),
            onReactionClick = {},
        )
    }
}
