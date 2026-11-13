package tv.trakt.trakt.app.common.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Text
import tv.trakt.trakt.app.common.ui.mediacards.EpisodeSkeletonCard
import tv.trakt.trakt.app.common.ui.mediacards.VerticalMediaSkeletonCard
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.resources.R

private val CardShape = RoundedCornerShape(12.dp)
private val CardTextPadding = 16.dp

@Composable
internal fun EmptyListCard(
    modifier: Modifier = Modifier,
    text: String = stringResource(R.string.list_placeholder_empty),
    onFocused: () -> Unit = {},
    sizeReference: @Composable () -> Unit,
) {
    val containerColor = TraktTheme.colors.dialogContainer
    val cardWidth = TraktTheme.size.horizontalMediaCardSize * 2 + TraktTheme.spacing.mainRowSpace

    Card(
        onClick = {},
        modifier = modifier
            .width(cardWidth)
            .onFocusChanged {
                if (it.isFocused) onFocused()
            },
        shape = CardDefaults.shape(shape = CardShape),
        border = CardDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(width = (2.75).dp, color = TraktTheme.colors.accent),
                shape = CardShape,
            ),
        ),
        colors = CardDefaults.colors(
            containerColor = containerColor,
            focusedContainerColor = containerColor,
            pressedContainerColor = containerColor,
        ),
        scale = CardDefaults.scale(focusedScale = 1F),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Laid out invisibly so the card takes the exact height of the row it stands in for.
            Box(
                modifier = Modifier
                    .alpha(0F)
                    .focusProperties { canFocus = false },
            ) {
                sizeReference()
            }

            Text(
                text = text,
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.heading6,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(CardTextPadding),
            )
        }
    }
}

@Preview(
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        Column(
            verticalArrangement = spacedBy(TraktTheme.spacing.mainRowVerticalSpace),
            modifier = Modifier.padding(TraktTheme.spacing.mainContentEndSpace),
        ) {
            EmptyListCard {
                EpisodeSkeletonCard()
            }
            EmptyListCard {
                VerticalMediaSkeletonCard(
                    width = TraktTheme.size.verticalMediaBigCardSize,
                    footer2 = true,
                )
            }
            EmptyListCard {
                Spacer(modifier = Modifier.height(TraktTheme.size.detailsCustomListSize))
            }
        }
    }
}
