package tv.trakt.trakt.app.common.ui.chips

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.ui.theme.colors.Green600
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.resources.R

@Composable
internal fun FinaleChip(
    modifier: Modifier = Modifier,
    contentTextStyle: TextStyle = TraktTheme.typography.meta,
    containerColor: Color = TraktTheme.colors.chipContainerOnContent,
) {
    StatusChip(
        text = stringResource(R.string.tag_text_finale),
        dotColor = Red500,
        contentTextStyle = contentTextStyle,
        containerColor = containerColor,
        modifier = modifier,
    )
}

@Composable
internal fun PremiereChip(
    modifier: Modifier = Modifier,
    contentTextStyle: TextStyle = TraktTheme.typography.meta,
    containerColor: Color = TraktTheme.colors.chipContainerOnContent,
) {
    StatusChip(
        text = stringResource(R.string.tag_text_premiere),
        dotColor = Green600,
        contentTextStyle = contentTextStyle,
        containerColor = containerColor,
        modifier = modifier,
    )
}

@Composable
private fun StatusChip(
    text: String,
    modifier: Modifier = Modifier,
    dotColor: Color = TraktTheme.colors.chipContent,
    containerColor: Color = TraktTheme.colors.chipContainerOnContent,
    contentTextStyle: TextStyle = TraktTheme.typography.meta,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Absolute.spacedBy(3.5.dp),
        modifier = modifier
            .background(
                shape = RoundedCornerShape(100),
                color = containerColor,
            )
            .padding(start = 6.dp, end = 6.dp)
            .padding(
                vertical = 4.dp,
            ),
    ) {
        Box(
            modifier = Modifier
                .size(7.5.dp)
                .background(
                    color = dotColor,
                    shape = RoundedCornerShape(100),
                ),
        )
        Text(
            text = text,
            style = contentTextStyle,
            color = TraktTheme.colors.chipContent,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        Column(
            verticalArrangement = spacedBy(8.dp),
        ) {
            FinaleChip(
                modifier = Modifier.height(20.dp),
            )
            PremiereChip(
                modifier = Modifier.height(20.dp),
            )
        }
    }
}
