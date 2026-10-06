package tv.trakt.trakt.core.reactions.media.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.delay
import tv.trakt.trakt.common.helpers.extensions.rememberThousandsFormat
import tv.trakt.trakt.common.model.reactions.MediaReaction
import tv.trakt.trakt.common.model.reactions.MediaReactionsSummary
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.helpers.extensions.rememberAnimationsDisabled
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import kotlin.time.Duration.Companion.seconds

private val RotationInterval = 5.seconds
private const val TOP_REACTIONS_LIMIT = 3

private val BadgeEmojiSize = 24.dp
private val BadgeEmojiFontSize = 18.sp
private val BadgeAddIconSize = 22.dp
private const val PICK_DRIFT_FRACTION = 12

@Composable
internal fun MediaReactionsBadge(
    summary: MediaReactionsSummary,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val topReactions = remember(summary) { summary.top(TOP_REACTIONS_LIMIT) }
    val hasReactions = summary.reactionsCount > 0

    Row(
        horizontalArrangement = spacedBy(4.dp),
        verticalAlignment = CenterVertically,
        modifier = modifier,
    ) {
        if (!hasReactions) {
            if (enabled) {
                AddReactionIcon()
            }
            return@Row
        }

        Row(
            horizontalArrangement = spacedBy((-2).dp),
            verticalAlignment = CenterVertically,
        ) {
            for (reaction in topReactions) {
                MediaReactionEmoji(
                    reaction = reaction,
                    size = BadgeEmojiSize,
                    fontSize = BadgeEmojiFontSize,
                )
            }
        }

        Text(
            text = rememberThousandsFormat(summary.reactionsCount),
            style = TraktTheme.typography.meta.copy(fontSize = 12.sp, fontFeatureSettings = "tnum"),
            color = TraktTheme.colors.textPrimary,
            modifier = Modifier
                .graphicsLayer {
                    translationX = -1.5.dp.toPx()
                },
        )
    }
}

@Composable
internal fun MediaReactionsUserPick(
    userReactions: ImmutableList<MediaReaction>,
    modifier: Modifier = Modifier,
    emojiSize: Dp = BadgeEmojiSize,
    emojiFontSize: TextUnit = BadgeEmojiFontSize,
    addIconSize: Dp = BadgeAddIconSize,
) {
    val shownIndex = rememberRotatingIndex(userReactions)
    val reaction = userReactions.getOrNull(shownIndex) ?: userReactions.firstOrNull()

    if (reaction == null) {
        AddReactionIcon(
            cellSize = emojiSize,
            iconSize = addIconSize,
            modifier = modifier,
        )
        return
    }

    AnimatedContent(
        targetState = reaction,
        transitionSpec = {
            (fadeIn(tween(500)) + slideInVertically(tween(500)) { it / PICK_DRIFT_FRACTION })
                .togetherWith(fadeOut(tween(500)) + slideOutVertically(tween(500)) { -it / PICK_DRIFT_FRACTION })
                .using(SizeTransform(clip = false))
        },
        label = "user_pick",
        modifier = modifier,
    ) { shown ->
        MediaReactionEmoji(
            reaction = shown,
            size = emojiSize,
            fontSize = emojiFontSize,
        )
    }
}

@Composable
private fun rememberRotatingIndex(userReactions: ImmutableList<MediaReaction>): Int {
    val animationsDisabled = rememberAnimationsDisabled()
    var shownIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(userReactions, animationsDisabled) {
        shownIndex = userReactions.lastIndex.coerceAtLeast(0)
        if (userReactions.size < 2 || animationsDisabled) return@LaunchedEffect

        while (true) {
            delay(RotationInterval)
            shownIndex = (shownIndex + 1) % userReactions.size
        }
    }

    return shownIndex
}

@Composable
private fun AddReactionIcon(
    modifier: Modifier = Modifier,
    cellSize: Dp = BadgeEmojiSize,
    iconSize: Dp = BadgeAddIconSize,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(cellSize),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_reaction_add),
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
            modifier = Modifier.size(iconSize),
        )
    }
}

private val PreviewSummary = MediaReactionsSummary(
    reactionsCount = 1240,
    usersCount = 800,
    distribution = persistentMapOf(
        MediaReaction.Popcorn to 600,
        MediaReaction.Fire to 400,
        MediaReaction.MindBlown to 240,
    ),
)

@Preview(name = "Empty")
@Composable
private fun PreviewEmpty() {
    TraktThemeLightDark {
        MediaReactionsBadge(
            summary = MediaReactionsSummary(),
        )
    }
}

@Preview(name = "Has reactions")
@Composable
private fun PreviewHasReactions() {
    TraktThemeLightDark {
        MediaReactionsBadge(
            summary = PreviewSummary,
        )
    }
}

@Preview(name = "User picks")
@Composable
private fun PreviewUserPicks() {
    TraktThemeLightDark {
        MediaReactionsUserPick(
            userReactions = persistentListOf(MediaReaction.Fire, MediaReaction.Skull),
        )
    }
}

@Preview(name = "Signed out")
@Composable
private fun PreviewSignedOut() {
    TraktThemeLightDark {
        MediaReactionsBadge(
            summary = MediaReactionsSummary(),
            enabled = false,
        )
    }
}
