package tv.trakt.trakt.core.profile.sections.leaderboard.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.rememberThousandsFormat
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.leaderboard.LeaderboardEntry
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.helpers.extensions.rememberAnimationsDisabled
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import kotlin.time.Duration.Companion.milliseconds

internal const val PODIUM_SIZE = 3

private val AvatarSize = 64.dp
private val LeaderAvatarSize = 84.dp
private val EntryOffset = 4.dp

private const val ENTRY_STAGGER_MS = 100L
private const val ENTRY_DURATION_MS = 500

private val SlotOrder = listOf(1, 0, 2)

internal fun LeaderboardEntry.isPodiumEntry(): Boolean {
    val rank = rank ?: return false
    if (isLocked && !isViewer) return false
    return rank <= PODIUM_SIZE
}

@Composable
internal fun LeaderboardPodiumView(
    entries: ImmutableList<LeaderboardEntry>,
    modifier: Modifier = Modifier,
    onEntryClick: (LeaderboardEntry) -> Unit = {},
) {
    val animationsDisabled = rememberAnimationsDisabled()
    var entered by rememberSaveable { mutableStateOf(animationsDisabled) }
    val progress = remember {
        List(PODIUM_SIZE) { Animatable(if (entered) 1F else 0F) }
    }

    LaunchedEffect(Unit) {
        if (entered) return@LaunchedEffect
        progress
            .mapIndexed { index, animatable ->
                launch {
                    if (index > 0) delay(ENTRY_STAGGER_MS.milliseconds)
                    animatable.animateTo(
                        targetValue = 1F,
                        animationSpec = tween(
                            durationMillis = ENTRY_DURATION_MS,
                            easing = FastOutSlowInEasing,
                        ),
                    )
                }
            }
            .joinAll()
        entered = true
    }

    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = spacedBy(4.dp),
        modifier = modifier,
    ) {
        SlotOrder.forEach { entryIndex ->
            val entry = entries.getOrNull(entryIndex)
            Box(
                contentAlignment = Alignment.BottomCenter,
                modifier = Modifier.weight(1F),
            ) {
                if (entry != null) {
                    PodiumSpot(
                        entry = entry,
                        onClick = { onEntryClick(entry) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                val value = progress[entryIndex].value
                                alpha = value
                                translationY = (1F - value) * EntryOffset.toPx()
                            },
                    )
                }
            }
        }
    }
}

@Composable
internal fun LeaderboardPodiumSkeleton(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier,
    ) {
        PodiumSpot(
            entry = null,
            modifier = Modifier.alpha(0F),
        )
        FilmProgressIndicator(size = 32.dp)
    }
}

// A null entry lays out the leader spot with blank content, so the skeleton reserves
// exactly the height of a loaded podium.
@Composable
private fun PodiumSpot(
    entry: LeaderboardEntry?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val rank = entry?.rank ?: 1
    val avatarSize = if (rank == 1) LeaderAvatarSize else AvatarSize
    val avatarModifier = Modifier.padding(bottom = 6.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(4.dp),
        modifier = modifier.onClick(
            enabled = entry != null,
            onClick = onClick,
        ),
    ) {
        if (entry != null) {
            LeaderboardAvatar(
                entry = entry,
                size = avatarSize,
                border = 3.dp,
                modifier = avatarModifier,
            )
        } else {
            Spacer(avatarModifier.size(avatarSize))
        }

        LeaderboardRankLabel(rank = rank)

        Text(
            text = entry?.user?.displayName ?: " ",
            style = TraktTheme.typography.cardTitle.copy(fontSize = 14.sp),
            color = TraktTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp),
        )

        PodiumStats(
            hours = entry?.totalMinutes?.let { rememberThousandsFormat(it / 60) },
            plays = entry?.totalPlays?.let { rememberThousandsFormat(it) },
            placeholder = entry == null,
        )
    }
}

@Composable
private fun PodiumStats(
    hours: String?,
    plays: String?,
    placeholder: Boolean,
) {
    val hoursLabel = stringResource(R.string.stat_label_hours)
    val playsLabel = stringResource(R.string.stat_label_plays)

    if (hours != null || placeholder) {
        val valueStyle = TraktTheme.typography.heading4
            .toSpanStyle()
            .copy(color = TraktTheme.colors.textPrimary)

        Text(
            text = buildAnnotatedString {
                withStyle(valueStyle) { append(hours ?: " ") }
                append(" ")
                withStyle(SpanStyle(color = TraktTheme.colors.textSecondary)) { append(hoursLabel) }
            },
            style = TraktTheme.typography.cardTitle,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }

    if (plays != null || placeholder) {
        Text(
            text = "${plays ?: " "} $playsLabel",
            style = TraktTheme.typography.cardTitle,
            color = TraktTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        LeaderboardPodiumView(
            entries = persistentListOf(
                LeaderboardEntry(
                    user = PreviewData.user1.copy(isVip = true),
                    rank = 1,
                    totalMinutes = 2_904_000,
                    totalPlays = 78_900,
                    isLocked = false,
                    isViewer = true,
                ),
                LeaderboardEntry(
                    user = PreviewData.user1.copy(
                        ids = PreviewData.user1.ids.copy(trakt = TraktId(2)),
                        name = "John Dutton",
                    ),
                    rank = 2,
                    totalMinutes = 2_424_000,
                    totalPlays = 68_800,
                    isLocked = false,
                ),
                LeaderboardEntry(
                    user = PreviewData.user1.copy(
                        ids = PreviewData.user1.ids.copy(trakt = TraktId(3)),
                        name = "Beth Dutton",
                    ),
                    rank = 3,
                    totalMinutes = 1_080_000,
                    totalPlays = 23_800,
                    isLocked = false,
                ),
            ),
            modifier = Modifier
                .fillMaxWidth(),
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun SkeletonPreview() {
    TraktThemePreview {
        LeaderboardPodiumSkeleton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )
    }
}
