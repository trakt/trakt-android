@file:OptIn(ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.profile.sections.thismonth

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.common.ui.theme.colors.Shade920
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.VipChip
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ThisMonthVipCard(
    modifier: Modifier = Modifier,
    containerColor: Color = Shade920,
) {
    val radialGradient = remember {
        object : ShaderBrush() {
            override fun createShader(size: Size): Shader {
                return RadialGradientShader(
                    colors = listOf(
                        Red500,
                        containerColor,
                    ),
                    center = Offset(size.width * 1.5F, -size.height / 1.5F),
                    radius = size.width * 1.5F,
                )
            }
        }
    }

    val shape = RoundedCornerShape(24.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .shadow(4.dp, shape)
            .clip(shape)
            .background(containerColor)
            .background(radialGradient),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.text_vip_upsell_default).uppercase(),
                style = TraktTheme.typography.heading5,
                color = TraktTheme.colors.textPrimary,
                modifier = Modifier
                    .fillMaxWidth(),
            )

            Text(
                text = stringResource(R.string.text_vip_upsell_default_description),
                style = TraktTheme.typography.paragraphSmaller.copy(
                    lineHeight = 1.4.em,
                ),
                color = TraktTheme.colors.textPrimary,
                modifier = Modifier
                    .fillMaxWidth(),
            )

            VipChip(
                text = stringResource(R.string.badge_text_get_vip),
                modifier = Modifier
                    .widthIn(min = 92.dp)
                    .padding(top = 4.dp),
            )
        }
    }
}

@Preview(widthDp = 400)
@Composable
private fun Preview() {
    TraktTheme {
        ThisMonthVipCard(
            modifier = Modifier.padding(16.dp),
        )
    }
}
