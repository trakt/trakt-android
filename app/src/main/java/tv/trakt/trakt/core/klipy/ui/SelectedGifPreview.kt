package tv.trakt.trakt.core.klipy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.core.klipy.model.KlipyGif
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

// Preview sits beside the text area; wide enough to read, narrow enough to keep the input usable.
internal val SelectedGifWidth = 112.dp

@Composable
internal fun SelectedGifPreview(
    gif: KlipyGif,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onRemoveClick: () -> Unit = {},
) {
    Box(modifier = modifier) {
        GifCard(
            gif = gif,
            preview = false,
            shape = RoundedCornerShape(16.dp),
        )

        Icon(
            painter = painterResource(R.drawable.ic_close),
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .background(
                    color = TraktTheme.colors.dialogContainer.copy(alpha = 0.8F),
                    shape = CircleShape,
                )
                .size(24.dp)
                .padding(5.dp)
                .onClick(enabled = enabled, onClick = onRemoveClick),
        )
    }
}
