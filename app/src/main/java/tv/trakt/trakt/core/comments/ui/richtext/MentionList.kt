package tv.trakt.trakt.core.comments.ui.richtext

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight.Companion.W600
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.core.comments.model.CommentMention
import tv.trakt.trakt.ui.theme.TraktTheme

private val MentionListMaxHeight = 160.dp

@Composable
internal fun MentionList(
    mentions: List<CommentMention>,
    onPick: (CommentMention) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = MentionListMaxHeight)
            .verticalScroll(rememberScrollState()),
    ) {
        mentions.forEach { mention ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .onClick(throttle = false, indication = true) { onPick(mention) }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            ) {
                Text(
                    text = mention.name,
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.paragraphSmall.copy(fontWeight = W600),
                    maxLines = 1,
                    overflow = Ellipsis,
                    modifier = Modifier.weight(1F, fill = false),
                )

                mention.detail?.let {
                    Text(
                        text = it,
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.meta,
                        maxLines = 1,
                        overflow = Ellipsis,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF212427)
@Composable
private fun Preview() {
    TraktTheme {
        MentionList(
            mentions = listOf(
                CommentMention(name = "Steve Carell", href = "", detail = "Michael Scott"),
                CommentMention(name = "Rainn Wilson", href = ""),
            ),
            onPick = {},
        )
    }
}
