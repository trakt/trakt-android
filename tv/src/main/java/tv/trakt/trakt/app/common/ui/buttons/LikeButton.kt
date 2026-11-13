package tv.trakt.trakt.app.common.ui.buttons

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.resources.R

private val likeShape = RoundedCornerShape(percent = 50)
private val likeIconSize = 20.dp

@Composable
internal fun LikeButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    text: String,
    liked: Boolean = false,
    loading: Boolean = false,
) {
    OutlineTextButton(
        text = text,
        leadingIcon = painterResource(
            when {
                liked -> R.drawable.ic_thumb_up_fill
                else -> R.drawable.ic_thumb_up_like
            },
        ),
        iconSize = likeIconSize,
        shape = likeShape,
        loading = loading,
        onClick = onClick,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun Preview1() {
    TraktTheme {
        Column(
            verticalArrangement = spacedBy(12.dp),
        ) {
            LikeButton(
                text = "12",
            )
            LikeButton(
                text = "13",
                liked = true,
            )
            LikeButton(
                text = "12",
                loading = true,
            )
            LikeButton(
                text = "12.5K",
                liked = true,
            )
        }
    }
}
