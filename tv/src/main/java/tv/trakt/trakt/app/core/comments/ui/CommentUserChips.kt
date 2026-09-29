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
import tv.trakt.trakt.common.model.CommentUserProgress
import tv.trakt.trakt.resources.R

@Composable
internal fun CommentUserChips(
    comment: Comment,
    progress: CommentUserProgress?,
    modifier: Modifier = Modifier,
) {
    val rating = comment.user5Rating
    if (progress == null && rating == null) return

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(6.dp),
        modifier = modifier,
    ) {
        if (progress != null) {
            InfoChip(
                text = progress.percent,
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
            progress = PreviewData.comment1.userStats.progress(totalEpisodes = 10),
        )
    }
}

@Preview
@Composable
private fun PreviewRatingOnly() {
    TraktTheme {
        CommentUserChips(
            comment = PreviewData.comment1.copy(userRating = 8),
            progress = null,
        )
    }
}
