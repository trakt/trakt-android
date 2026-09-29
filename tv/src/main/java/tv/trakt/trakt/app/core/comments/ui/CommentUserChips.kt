package tv.trakt.trakt.app.core.comments.ui

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.app.common.ui.chips.InfoChip
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.resources.R

@Composable
internal fun CommentUserChips(
    comment: Comment,
    progressVisible: Boolean,
    modifier: Modifier = Modifier,
) {
    val rating = comment.user5Rating
    if (!progressVisible && rating == null) return

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(6.dp),
        modifier = modifier,
    ) {
        if (progressVisible) {
            InfoChip(
                text = comment.userStats.completedPercent,
                iconPainter = painterResource(R.drawable.ic_eye),
            )
        }

        if (rating != null) {
            InfoChip(
                text = rating,
                iconPainter = painterResource(R.drawable.ic_star_trakt_on),
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        CommentUserChips(
            comment = PreviewData.comment1.copy(userRating = 8),
            progressVisible = true,
        )
    }
}

@Preview
@Composable
private fun PreviewRatingOnly() {
    TraktTheme {
        CommentUserChips(
            comment = PreviewData.comment1.copy(userRating = 8),
            progressVisible = false,
        )
    }
}
