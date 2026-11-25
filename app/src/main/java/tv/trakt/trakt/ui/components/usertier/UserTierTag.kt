package tv.trakt.trakt.ui.components.usertier

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.model.UserTier
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private val TagMinWidth = 36.dp
private val TagShape = RoundedCornerShape(100)

@Composable
internal fun UserTierTag(
    tier: UserTier,
    isDirector: Boolean,
    maxWidth: Dp,
    modifier: Modifier = Modifier,
) {
    val isFree = tier == UserTier.Free
    val labelRes = when {
        isDirector -> R.string.translated_value_job_director
        else -> tier.labelRes() ?: return
    }
    val label = stringResource(labelRes)

    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(2.dp, Alignment.CenterHorizontally),
        modifier = modifier
            .widthIn(min = TagMinWidth, max = maxWidth)
            .graphicsLayer {
                translationY = size.height / 8
            }
            .then(
                if (isFree) {
                    Modifier.border(1.dp, userTierFreeRing(TraktTheme.colors.isLight), TagShape)
                } else {
                    Modifier
                        .shadow(elevation = TraktTheme.colors.shadowDynamicSmall, shape = TagShape)
                        .background(tier.tagBrush(), TagShape)
                },
            )
            .padding(horizontal = 6.dp, vertical = 3.dp),
    ) {
        Text(
            text = label.uppercase(),
            color = if (isFree) TraktTheme.colors.textSecondary else tier.tagInk(),
            style = TraktTheme.typography.meta.copy(
                fontSize = 12.sp,
                letterSpacing = 0.03.sp,
            ),
            maxLines = 1,
            overflow = Ellipsis,
            autoSize = TextAutoSize.StepBased(
                maxFontSize = 12.sp,
                minFontSize = 8.sp,
                stepSize = 0.5.sp,
            ),
            modifier = Modifier.weight(1F, false),
        )
    }
}

private fun UserTier.labelRes(): Int? {
    return when (this) {
        UserTier.Free -> null
        UserTier.Vip -> R.string.tag_text_vip
        UserTier.Pro -> R.string.tag_text_user_tier_pro
        UserTier.Veteran -> R.string.tag_text_user_tier_veteran
        UserTier.Elite -> R.string.tag_text_user_tier_elite
        UserTier.Legend -> R.string.tag_text_user_tier_legend
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        Column(
            verticalArrangement = spacedBy(12.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            UserTier.entries.forEach { tier ->
                UserTierTag(tier = tier, isDirector = false, maxWidth = 120.dp)
            }
            UserTierTag(tier = UserTier.Free, isDirector = true, maxWidth = 120.dp)
        }
    }
}
