package tv.trakt.trakt.core.userprofile.sections.match.ui

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.rememberPercentFormat
import tv.trakt.trakt.common.model.UserMatch
import tv.trakt.trakt.core.userprofile.sections.match.model.MatchBand
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private val PillShape = RoundedCornerShape(100)
private val PillHeight = 28.dp
private val PillTouchHeight = 48.dp

@Composable
internal fun UserMatchPill(
    match: UserMatch?,
    loading: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Box(
        contentAlignment = CenterStart,
        modifier = modifier
            .heightIn(min = PillTouchHeight)
            .then(if (match != null) Modifier.onClick(onClick = onClick) else Modifier),
    ) {
        when {
            match != null -> MatchPillContent(score = match.score)
            loading -> MatchPillSkeleton()
        }
    }
}

@Composable
private fun MatchPillContent(
    score: Int,
    modifier: Modifier = Modifier,
) {
    val accent = TraktTheme.colors.textPrimary
    val fill = score / 100F

    val background = remember(accent, fill) {
        val filled = accent.copy(alpha = 0.22F)
        val empty = accent.copy(alpha = 0.05F)
        Brush.horizontalGradient(
            0F to filled,
            fill to filled,
            fill to empty,
            1F to empty,
        )
    }

    val borderColor = remember(accent, score) {
        when (MatchBand.of(score)) {
            MatchBand.High -> accent.copy(alpha = 0.6F)
            MatchBand.Mid -> accent.copy(alpha = 0.3F)
            MatchBand.Low -> accent.copy(alpha = 0.15F)
        }
    }

    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(4.dp),
        modifier = modifier
            .height(PillHeight)
            .clip(PillShape)
            .background(background, PillShape)
            .border(1.dp, borderColor, PillShape)
            .padding(horizontal = 12.dp),
    ) {
        Text(
            text = stringResource(R.string.match_pill_anchor),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.paragraphSmaller,
            maxLines = 1,
            modifier = Modifier.alpha(0.85F),
        )

        Text(
            text = score.rememberPercentFormat(),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.paragraphSmaller.copy(fontWeight = W700),
            maxLines = 1,
        )

        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
            modifier = Modifier
                .padding(start = 2.dp)
                .size(9.dp)
                .alpha(0.55F),
        )
    }
}

@Composable
private fun MatchPillSkeleton(modifier: Modifier = Modifier) {
    val containerColor = TraktTheme.colors.skeletonContainer
    val shimmerColor = TraktTheme.colors.skeletonShimmer

    val infiniteTransition = rememberInfiniteTransition(label = "matchPillTransition")
    val shimmer by infiniteTransition.animateColor(
        initialValue = containerColor,
        targetValue = shimmerColor,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "matchPillShimmer",
    )

    Box(
        modifier = modifier
            .height(PillHeight)
            .width(108.dp)
            .background(shimmer, PillShape),
    )
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        Column(
            verticalArrangement = spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            listOf(92, 55, 12).forEach { score ->
                UserMatchPill(
                    match = UserMatchPreviewData.match.copy(score = score),
                    loading = false,
                )
            }
            UserMatchPill(
                match = null,
                loading = true,
            )
        }
    }
}
