package tv.trakt.trakt.core.reactions.media.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
private val DividerSlotWidth = 8.dp
private const val PICK_DRIFT_FRACTION = 12

@Composable
internal fun MediaReactionsBadge(
    summary: MediaReactionsSummary,
    userReactions: ImmutableList<MediaReaction>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val topReactions = remember(summary) { summary.top(TOP_REACTIONS_LIMIT) }
    val hasRoom = summary.reactionsCount > 0
    val shownIndex = rememberRotatingIndex(userReactions)

    Row(
        horizontalArrangement = spacedBy(4.dp),
        verticalAlignment = CenterVertically,
        modifier = modifier,
    ) {
        if (enabled) {
            UserPick(reaction = userReactions.getOrNull(shownIndex) ?: userReactions.firstOrNull())
        }

        // Dots and separator share one fixed slot, so switching between them never shifts the row.
        if (enabled && (hasRoom || userReactions.size > 1)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.width(DividerSlotWidth),
            ) {
                when {
                    userReactions.size > 1 -> PickDots(
                        count = userReactions.size,
                        shownIndex = shownIndex,
                    )
                    else -> Box(
                        modifier = Modifier
                            .size(width = 1.dp, height = 14.dp)
                            .background(TraktTheme.colors.separator),
                    )
                }
            }
        }

        if (hasRoom) {
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
                // Tabular digits keep the count's width steady as it changes.
                style = TraktTheme.typography.meta.copy(fontSize = 12.sp, fontFeatureSettings = "tnum"),
                color = TraktTheme.colors.textPrimary,
                modifier = Modifier
                    .graphicsLayer {
                        translationX = -1.5.dp.toPx()
                    },
            )
        }
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
private fun UserPick(reaction: MediaReaction?) {
    // The add icon takes the emoji cell's size, so the first pick does not shift the row.
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(BadgeEmojiSize),
    ) {
        if (reaction == null) {
            Icon(
                painter = painterResource(R.drawable.ic_reaction_add),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier.size(22.dp),
            )
            return@Box
        }

        AnimatedContent(
            targetState = reaction,
            transitionSpec = {
                (fadeIn(tween(500)) + slideInVertically(tween(500)) { it / PICK_DRIFT_FRACTION })
                    .togetherWith(fadeOut(tween(500)) + slideOutVertically(tween(500)) { -it / PICK_DRIFT_FRACTION })
                    .using(SizeTransform(clip = false))
            },
            label = "user_pick",
        ) { shown ->
            MediaReactionEmoji(
                reaction = shown,
                size = BadgeEmojiSize,
                fontSize = BadgeEmojiFontSize,
            )
        }
    }
}

@Composable
private fun PickDots(
    count: Int,
    shownIndex: Int,
) {
    Column(verticalArrangement = spacedBy(2.dp)) {
        repeat(count) { index ->
            Box(
                modifier = Modifier
                    .size(3.25.dp)
                    .background(
                        color = when (index) {
                            shownIndex -> TraktTheme.colors.textPrimary
                            else -> TraktTheme.colors.textSecondary.copy(alpha = 0.4F)
                        },
                        shape = CircleShape,
                    ),
            )
        }
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
            userReactions = persistentListOf(),
        )
    }
}

@Preview(name = "Has reactions")
@Composable
private fun PreviewHasReactions() {
    TraktThemeLightDark {
        MediaReactionsBadge(
            summary = PreviewSummary,
            userReactions = persistentListOf(),
        )
    }
}

@Preview(name = "User picks")
@Composable
private fun PreviewUserPicks() {
    TraktThemeLightDark {
        MediaReactionsBadge(
            summary = PreviewSummary,
            userReactions = persistentListOf(MediaReaction.Fire, MediaReaction.Skull),
        )
    }
}

@Preview(name = "Signed out")
@Composable
private fun PreviewSignedOut() {
    TraktThemeLightDark {
        MediaReactionsBadge(
            summary = PreviewSummary,
            userReactions = persistentListOf(),
            enabled = false,
        )
    }
}
