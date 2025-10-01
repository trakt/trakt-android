package tv.trakt.trakt.core.summary.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.ui.theme.colors.Shade920
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun DetailsActions(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                color = Shade920,
                shape = RoundedCornerShape(18.dp),
            )
            .padding(
                top = 8.dp,
                bottom = 8.dp,
                start = 8.dp,
                end = 20.dp,
            ),
    ) {
        Row(
            horizontalArrangement = spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Button(
                modifier = Modifier
                    .weight(1F)
                    .height(40.dp),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                ),
                shape = RoundedCornerShape(12.dp),
                colors = buttonColors(
                    containerColor = TraktTheme.colors.primaryButtonContainer,
                    contentColor = TraktTheme.colors.primaryButtonContent,
                    disabledContainerColor = TraktTheme.colors.primaryButtonContainerDisabled,
                    disabledContentColor = TraktTheme.colors.primaryButtonContentDisabled,
                ),
                onClick = {},
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_check_double),
                    tint = TraktTheme.colors.primaryButtonContent,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
            }

            Icon(
                painter = painterResource(R.drawable.ic_plus_round),
                tint = TraktTheme.colors.primaryButtonContent,
                contentDescription = null,
                modifier = Modifier
                    .size(22.dp)
                    .graphicsLayer {
                        translationX = -1.dp.toPx()
                    },
            )

            Icon(
                painter = painterResource(R.drawable.ic_more_vertical),
                tint = TraktTheme.colors.primaryButtonContent,
                contentDescription = null,
                modifier = Modifier
                    .rotate(90F)
                    .size(21.dp),
            )
        }
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
    widthDp = 400,
)
@Composable
private fun Preview() {
    TraktTheme {
        Column(
            verticalArrangement = spacedBy(16.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            DetailsActions()

            DetailsActions()
        }
    }
}
