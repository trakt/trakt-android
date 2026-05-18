package tv.trakt.trakt.core.summary.ui.views.info

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun MetaViewItemView(
    title: String,
    subtitle: String,
    icon: @Composable (() -> Unit),
    modifier: Modifier = Modifier,
    loading: Boolean = false,
) {
    val cornerShape = RoundedCornerShape(16.dp)

    Column(
        verticalArrangement = Arrangement.spacedBy(3.dp),
        modifier = modifier
            .background(
                color = TraktTheme.colors.dialogOnContainer,
                shape = cornerShape,
            )
            .padding(
                horizontal = 12.dp,
                vertical = 12.dp,
            ),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier.size(14.dp),
            ) {
                icon()
            }
            Text(
                text = subtitle,
                style = TraktTheme.typography.paragraphSmaller.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W500,
                ),
                color = TraktTheme.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (loading) {
            FilmProgressIndicator(
                color = TraktTheme.colors.textSecondary,
                modifier = Modifier
                    .padding(top = 1.dp)
                    .size(14.dp)
                    .align(Alignment.Start),
            )
        } else {
            Text(
                text = title,
                style = TraktTheme.typography.paragraphSmaller,
                color = TraktTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun Preview() {
    TraktTheme {
        MetaViewItemView(
            title = "12,345",
            subtitle = "Plays",
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_play),
                    contentDescription = null,
                    tint = TraktTheme.colors.textSecondary,
                    modifier = Modifier.size(14.dp),
                )
            },
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun PreviewLoading() {
    TraktTheme {
        MetaViewItemView(
            title = "",
            subtitle = "Plays",
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_play),
                    contentDescription = null,
                    tint = TraktTheme.colors.textSecondary,
                    modifier = Modifier.size(14.dp),
                )
            },
            loading = true,
        )
    }
}
