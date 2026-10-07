package tv.trakt.trakt.core.profile.sections.leaderboard.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.rememberThousandsFormat
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.leaderboard.LeaderboardEntry
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

// Decorative podium colours. Light variants are darker to keep contrast on light cards.
private val MedalGold = Color(0xFFF5C451)
private val MedalSilver = Color(0xFFC3CAD3)
private val MedalBronze = Color(0xFFD0905A)
private val MedalGoldLight = Color(0xFFC58F00)
private val MedalSilverLight = Color(0xFF7E8896)
private val MedalBronzeLight = Color(0xFFB0662E)

private const val MEDAL_BACKGROUND_ALPHA = 0.18F
private const val MEDAL_BACKGROUND_ALPHA_LIGHT = 0.16F

private const val LOCKED_CONTAINER_ALPHA = 0.4F

private fun medalColorOf(
    rank: Int?,
    isLight: Boolean,
): Color? {
    return when (rank) {
        1 -> if (isLight) MedalGoldLight else MedalGold
        2 -> if (isLight) MedalSilverLight else MedalSilver
        3 -> if (isLight) MedalBronzeLight else MedalBronze
        else -> null
    }
}

@Composable
internal fun LeaderboardItemView(
    entry: LeaderboardEntry,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val isLockedFollow = entry.isLocked && !entry.isViewer
    val rank = entry.rank.takeUnless { isLockedFollow }
    val shape = RoundedCornerShape(16.dp)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(12.dp),
        modifier = modifier
            .shadow(
                elevation = when {
                    isLockedFollow -> 0.dp
                    else -> TraktTheme.colors.shadowSmall
                },
                shape = shape,
            )
            .background(
                color = when {
                    isLockedFollow -> TraktTheme.colors.panelCardContainer.copy(alpha = LOCKED_CONTAINER_ALPHA)
                    else -> TraktTheme.colors.panelCardContainer
                },
                shape = shape,
            )
            .border(
                width = 1.dp,
                color = when {
                    entry.isViewer -> TraktTheme.colors.vipAccent
                    else -> Color.Transparent
                },
                shape = shape,
            )
            .clip(shape)
            .onClick(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .alpha(if (isLockedFollow) 0.55F else 1F),
    ) {
        RankLabel(rank = rank)

        Avatar(entry = entry)

        Column(
            verticalArrangement = spacedBy(2.dp),
            modifier = Modifier.weight(1F),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = spacedBy(8.dp),
            ) {
                Text(
                    text = entry.user.displayName,
                    style = TraktTheme.typography.cardTitle.copy(
                        fontSize = 14.sp,
                    ),
                    color = when {
                        isLockedFollow -> TraktTheme.colors.textSecondary
                        else -> TraktTheme.colors.textPrimary
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1F, fill = false),
                )
            }

            if (!isLockedFollow) {
                StatsLabel(entry = entry)
            }
        }
    }
}

@Composable
private fun RankLabel(rank: Int?) {
    val isLight = TraktTheme.colors.isLight
    val medalColor = medalColorOf(rank, isLight)
    val medalBackgroundAlpha = if (isLight) MEDAL_BACKGROUND_ALPHA_LIGHT else MEDAL_BACKGROUND_ALPHA

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.widthIn(min = 32.dp),
    ) {
        Text(
            text = rank?.let { "#$it" } ?: "-",
            style = TraktTheme.typography.cardTitle,
            color = medalColor ?: TraktTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier
                .background(
                    color = medalColor?.copy(alpha = medalBackgroundAlpha) ?: Color.Transparent,
                    shape = RoundedCornerShape(100),
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun Avatar(entry: LeaderboardEntry) {
    val size = 44.dp
    val borderColor = when {
        entry.user.isAnyVip -> TraktTheme.colors.vipAccent
        else -> Color.Transparent
    }
    val avatarModifier = Modifier
        .size(size)
        .border(2.dp, borderColor, CircleShape)
        .clip(CircleShape)

    val avatar = entry.user.images?.avatar?.full
    if (avatar != null) {
        AsyncImage(
            model = remember(avatar) {
                when {
                    avatar.startsWith("http") -> avatar
                    else -> "https://$avatar"
                }
                    .replace("/medium/", "/thumb/")
                    .replace("/original/", "/thumb/")
            },
            contentDescription = null,
            contentScale = ContentScale.Crop,
            error = painterResource(R.drawable.ic_person_placeholder),
            modifier = avatarModifier,
        )
    } else {
        Image(
            painter = painterResource(R.drawable.ic_person_placeholder),
            contentDescription = null,
            modifier = avatarModifier,
        )
    }
}

@Composable
private fun StatsLabel(entry: LeaderboardEntry) {
    val playsLabel = stringResource(R.string.stat_label_plays)
    val hoursLabel = stringResource(R.string.stat_label_hours)

    val plays = entry.totalPlays?.let {
        "${rememberThousandsFormat(it)} $playsLabel"
    }
    val hours = entry.totalMinutes?.let {
        "${rememberThousandsFormat(it / 60)} $hoursLabel"
    }

    val text = listOfNotNull(plays, hours).joinToString("  •  ")
    if (text.isEmpty()) return

    Text(
        text = text,
        style = TraktTheme.typography.meta,
        color = TraktTheme.colors.textSecondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Preview(
    backgroundColor = 0xFF131517,
    showBackground = true,
)
@Composable
private fun Preview() {
    TraktThemeLightDark {
        Column(
            verticalArrangement = spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            LeaderboardItemView(
                entry = LeaderboardEntry(
                    user = PreviewData.user1.copy(isVip = true),
                    rank = 1,
                    totalMinutes = 523_400,
                    totalPlays = 12_400,
                    isLocked = false,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            LeaderboardItemView(
                entry = LeaderboardEntry(
                    user = PreviewData.user1.copy(name = "John Dutton", isVip = true),
                    rank = 2,
                    totalMinutes = 301_000,
                    totalPlays = 8_700,
                    isLocked = false,
                    isViewer = true,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            LeaderboardItemView(
                entry = LeaderboardEntry(
                    user = PreviewData.user1.copy(name = "Beth Dutton"),
                    rank = 3,
                    totalMinutes = 42_000,
                    totalPlays = 900,
                    isLocked = false,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            LeaderboardItemView(
                entry = LeaderboardEntry(
                    user = PreviewData.user1.copy(name = "Rip Wheeler"),
                    rank = null,
                    totalMinutes = null,
                    totalPlays = null,
                    isLocked = true,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
