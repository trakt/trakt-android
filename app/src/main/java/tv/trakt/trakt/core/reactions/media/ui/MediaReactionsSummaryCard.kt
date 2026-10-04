package tv.trakt.trakt.core.reactions.media.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.EmojiSupportMatch
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableMap
import tv.trakt.trakt.common.helpers.extensions.rememberPercentFormat
import tv.trakt.trakt.common.model.reactions.MediaReaction
import tv.trakt.trakt.common.model.reactions.MediaReactionsSummary
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import kotlin.math.roundToInt

private const val PAGE_SIZE = 8
private const val ROW_SIZE = 4

@Composable
internal fun MediaReactionsSummaryCard(
    summary: MediaReactionsSummary,
    userReactions: ImmutableList<MediaReaction>,
    modifier: Modifier = Modifier,
) {
    val pages = remember(summary) {
        summary.top(MediaReaction.entries.size).chunked(PAGE_SIZE)
    }
    // Every page reserves two rows once there is more than one, so swiping never changes the height.
    val rowsPerPage = if (pages.size > 1 || pages.firstOrNull().orEmpty().size > ROW_SIZE) 2 else 1
    val pagerState = rememberPagerState { pages.size }

    Column(
        verticalArrangement = spacedBy(12.dp),
        modifier = modifier
            .background(
                color = TraktTheme.colors.reactionsSummaryContainer,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 20.dp,
                    bottomEnd = 20.dp,
                ),
            )
            .padding(horizontal = 8.dp, vertical = 12.dp),
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_reaction_dot),
                contentDescription = null,
                tint = TraktTheme.colors.textSecondary,
                modifier = Modifier.size(18.dp),
            )

            Text(
                text = stringResource(R.string.button_label_popup_reactions).uppercase(),
                style = TraktTheme.typography.paragraphSmall.copy(fontWeight = W700, fontSize = 12.sp),
                color = TraktTheme.colors.textSecondary,
                modifier = Modifier.weight(1F),
            )

            if (pages.size > 1) {
                PageIndicator(
                    pageCount = pages.size,
                    currentPage = pagerState.currentPage,
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            Column(verticalArrangement = spacedBy(8.dp)) {
                repeat(rowsPerPage) { rowIndex ->
                    val row = pages[page].drop(rowIndex * ROW_SIZE).take(ROW_SIZE)
                    Row(modifier = Modifier.fillMaxWidth()) {
                        repeat(ROW_SIZE) { slot ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.weight(1F),
                            ) {
                                row.getOrNull(slot)?.let { reaction ->
                                    SummaryItem(
                                        reaction = reaction,
                                        share = shareOf(
                                            count = summary.distribution[reaction] ?: 0,
                                            total = summary.reactionsCount,
                                        ),
                                        highlight = reaction in userReactions,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
) {
    Row(horizontalArrangement = spacedBy(3.dp)) {
        repeat(pageCount) { page ->
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(
                        color = when (page) {
                            currentPage -> TraktTheme.colors.textPrimary
                            else -> TraktTheme.colors.textSecondary.copy(alpha = 0.4F)
                        },
                        shape = CircleShape,
                    ),
            )
        }
    }
}

private fun shareOf(
    count: Int,
    total: Int,
): Int {
    if (total <= 0) return 0
    return (count * 100f / total).roundToInt()
}

@Composable
private fun SummaryItem(
    reaction: MediaReaction,
    share: Int,
    highlight: Boolean,
) {
    Row(
        horizontalArrangement = spacedBy(3.dp),
        verticalAlignment = CenterVertically,
        modifier = Modifier
            .background(
                color = if (highlight) TraktTheme.colors.reactionsSummaryHighlight else Color.Transparent,
                shape = RoundedCornerShape(12.dp),
            )
            .padding(horizontal = 6.dp, vertical = 4.dp),
    ) {
        Text(
            text = reaction.emoji,
            fontSize = 14.sp,
            style = TextStyle(
                platformStyle = PlatformTextStyle(
                    emojiSupportMatch = EmojiSupportMatch.Default,
                ),
            ),
        )

        Text(
            // Shares too small to round up still read as present.
            text = when (share) {
                0 -> "<${1.rememberPercentFormat()}"
                else -> share.rememberPercentFormat()
            },
            style = TraktTheme.typography.meta,
            color = TraktTheme.colors.textPrimary,
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Preview(name = "One row", widthDp = 300)
@Composable
private fun PreviewOneRow() {
    TraktThemeLightDark {
        MediaReactionsSummaryCard(
            summary = MediaReactionsSummary(
                reactionsCount = 14,
                usersCount = 10,
                distribution = mapOf(
                    MediaReaction.Popcorn to 8,
                    MediaReaction.Fire to 4,
                    MediaReaction.Skull to 2,
                ).toImmutableMap(),
            ),
            userReactions = persistentListOf(MediaReaction.Fire),
        )
    }
}

@Preview(name = "Several pages", widthDp = 300)
@Composable
private fun PreviewPages() {
    TraktThemeLightDark {
        MediaReactionsSummaryCard(
            summary = MediaReactionsSummary(
                reactionsCount = 5_400,
                usersCount = 3_000,
                distribution = MediaReaction.entries
                    .take(12)
                    .mapIndexed { index, reaction -> reaction to (1_200 - index * 90) }
                    .toMap()
                    .toImmutableMap(),
            ),
            userReactions = persistentListOf(MediaReaction.HeartEyes),
        )
    }
}
