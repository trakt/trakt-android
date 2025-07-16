package tv.trakt.app.tv.common.ui.mediacards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Text
import tv.trakt.app.tv.R
import tv.trakt.app.tv.ui.theme.TraktTheme
import tv.trakt.app.tv.ui.theme.colors.Purple300
import tv.trakt.app.tv.ui.theme.colors.Purple400

@Composable
internal fun HorizontalViewAllCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(9.dp),
        modifier = modifier,
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier
                .align(Alignment.Start)
                .width(TraktTheme.size.horizontalMediaCardSize)
                .aspectRatio(CardDefaults.HorizontalImageAspectRatio)
                .requiredWidth(120.dp)
                .graphicsLayer {
                    translationX = -20.dp.toPx()
                },
            shape = CardDefaults.shape(
                shape = RoundedCornerShape(12.dp),
            ),
            border = CardDefaults.border(
                focusedBorder = Border(
                    border = BorderStroke(width = (2.75).dp, color = TraktTheme.colors.accent),
                    shape = RoundedCornerShape(12.dp),
                ),
            ),
            colors = CardDefaults.colors(
                containerColor = Purple400,
                focusedContainerColor = Purple400,
                pressedContainerColor = Purple400,
            ),
            scale = CardDefaults.scale(
                focusedScale = 1.06f,
            ),
            content = {
                Image(
                    painter = painterResource(R.drawable.ic_view_all_horizontal),
                    contentDescription = "View All",
                    alignment = Alignment.CenterStart,
                    contentScale = ContentScale.FillHeight,
                    colorFilter = ColorFilter.tint(Purple300),
                    modifier = Modifier
                        .padding(
                            start = 8.dp,
                            top = 8.dp,
                            bottom = 8.dp,
                        ),
                )
            },
        )

        Text(
            text = "View All".uppercase(),
            style = TraktTheme.typography.buttonPrimary,
            color = TraktTheme.colors.textPrimary,
            maxLines = 1,
        )
    }
}

@Preview(widthDp = 160)
@Composable
private fun Preview() {
    TraktTheme {
        HorizontalViewAllCard()
    }
}
