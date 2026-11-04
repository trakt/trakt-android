package tv.trakt.trakt.ui.components.vip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.ui.theme.colors.Shade920
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.DefaultCardShape
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun VipBanner(
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.text_vip_upsell_default_2).uppercase(),
    description: String = stringResource(R.string.text_vip_features_tagline),
    containerColor: Color = Shade920,
    containerShape: RoundedCornerShape = DefaultCardShape,
    onClick: () -> Unit = {},
) {
    val gradientAccent = TraktTheme.colors.accentPalette.shade600
    val radialGradient = remember(gradientAccent) {
        object : ShaderBrush() {
            override fun createShader(size: Size): Shader {
                return RadialGradientShader(
                    colors = listOf(
                        gradientAccent,
                        containerColor,
                    ),
                    center = Offset(size.width * 1.5F, -size.height * 4F),
                    radius = size.width * 1.75F,
                )
            }
        }
    }

    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, containerShape)
            .clip(containerShape)
            .background(containerColor)
            .background(radialGradient)
            .onClick(onClick = onClick)
            .padding(
                start = 18.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 16.dp,
            ),
    ) {
        Column(
            verticalArrangement = spacedBy(2.dp),
            modifier = Modifier.weight(1F),
        ) {
            Text(
                text = title,
                style = TraktTheme.typography.heading5,
                color = TraktTheme.colors.textPrimaryOnAccent,
            )

            Text(
                text = description,
                style = TraktTheme.typography.paragraphSmaller.copy(
                    lineHeight = 1.4.em,
                ),
                color = TraktTheme.colors.textPrimaryOnAccent,
            )
        }

        VipChip(
            text = stringResource(R.string.badge_text_get_vip),
            onClick = onClick,
            modifier = Modifier.shadow(2.dp, RoundedCornerShape(100)),
        )
    }
}

@Preview(widthDp = 400)
@Composable
private fun Preview() {
    TraktThemeLightDark {
        VipBanner(
            modifier = Modifier.padding(16.dp),
        )
        VipBanner(
            description = stringResource(R.string.text_leaderboard_upsell_footer),
            modifier = Modifier.padding(16.dp),
        )
    }
}
