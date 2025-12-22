@file:OptIn(ExperimentalFoundationApi::class)

package tv.trakt.trakt.ui.components.vip

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterEnd
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
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun VipBanner(
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
                    center = Offset(size.width * 1.5F, -size.height * 3.5F),
                    radius = size.width * 1.75F,
                )
            }
        }
    }

    val shape = RoundedCornerShape(24.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .shadow(2.dp, shape)
            .clip(shape)
            .background(containerColor)
            .background(radialGradient),
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 18.dp,
                vertical = 16.dp,
            ),
            verticalArrangement = spacedBy(2.dp),
        ) {
            Text(
                text = stringResource(R.string.text_vip_upsell_default_2).uppercase(),
                style = TraktTheme.typography.heading5,
                color = TraktTheme.colors.textPrimary,
            )

            Text(
                text = stringResource(R.string.text_vip_upsell_default_description_2),
                style = TraktTheme.typography.paragraphSmaller.copy(
                    lineHeight = 1.4.em,
                ),
                color = TraktTheme.colors.textPrimary,
            )
        }

        VipChip(
            text = stringResource(R.string.badge_text_get_vip),
            modifier = Modifier
                .align(CenterEnd)
                .padding(end = 16.dp)
                .shadow(2.dp, RoundedCornerShape(100)),
        )
    }
}

@Preview(widthDp = 400)
@Composable
private fun Preview() {
    TraktTheme {
        VipBanner(
            modifier = Modifier.padding(16.dp),
        )
    }
}
