import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun HeaderBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(16.dp),
    ) {
        Column(
            verticalArrangement = spacedBy(1.dp),
        ) {
            Text(
                text = "Hello there!",
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.paragraphSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "November 30th, 2025",
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.meta,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.weight(1F))

        Icon(
            painter = painterResource(R.drawable.ic_filter_off),
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
            modifier = Modifier.size(24.dp),
        )

        Image(
            painter = painterResource(R.drawable.ic_person_placeholder),
            contentDescription = null,
            modifier = Modifier
                .size(30.dp)
                .border(2.dp, Color.White, CircleShape)
                .clip(CircleShape),
        )
    }
}

@Preview(widthDp = 320)
@Composable
private fun Preview() {
    TraktTheme {
        HeaderBar()
    }
}
