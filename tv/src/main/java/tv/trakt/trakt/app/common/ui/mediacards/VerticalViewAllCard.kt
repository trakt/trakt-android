package tv.trakt.trakt.app.common.ui.mediacards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.FixedScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Text
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.ui.theme.colors.Purple300
import tv.trakt.trakt.common.ui.theme.colors.Purple400
import tv.trakt.trakt.resources.R

@Composable
internal fun VerticalViewAllCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier
                .width(TraktTheme.size.verticalMediaCardSize)
                .aspectRatio(CardDefaults.VerticalImageAspectRatio),
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
                    contentScale = FixedScale(1.2F),
                    colorFilter = ColorFilter.tint(Purple300),
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(start = 12.dp),
                )
            },
        )

        Text(
            text = stringResource(R.string.button_text_view_all),
            style = TraktTheme.typography.cardTitle,
            color = TraktTheme.colors.textPrimary,
            maxLines = 1,
        )
    }
}

@Preview(widthDp = 160)
@Composable
private fun Preview() {
    TraktTheme {
        VerticalViewAllCard()
    }
}
