package tv.trakt.trakt.core.summary.soundtrack.ui

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.soundtrack.SoundtrackTrack
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private val SearchIconSize = 16.dp

@Composable
internal fun SoundtrackSearchMenu(
    track: SoundtrackTrack,
    mediaTitle: String,
    modifier: Modifier = Modifier,
) {
    val inspection = LocalInspectionMode.current
    val uriHandler = LocalUriHandler.current
    var showMenu by remember { mutableStateOf(false) }
    val query = remember(track, mediaTitle) { soundtrackSearchQuery(mediaTitle, track) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(ActionSize)
            .onClick { showMenu = true },
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_more_vertical),
            contentDescription = null,
            tint = TraktTheme.colors.textSecondary,
            modifier = Modifier.size(SearchIconSize),
        )

        DropdownMenu(
            expanded = inspection || showMenu,
            onDismissRequest = { showMenu = false },
            containerColor = TraktTheme.colors.dialogContainer,
            shape = RoundedCornerShape(16.dp),
        ) {
            SearchMenuItem(
                text = stringResource(R.string.button_text_soundtrack_search_web),
                icon = R.drawable.ic_search_off,
                onClick = {
                    showMenu = false
                    uriHandler.openUri(webSearchUrl(query))
                },
            )
            SearchMenuItem(
                text = stringResource(R.string.button_text_soundtrack_search_youtube),
                icon = R.drawable.ic_trailer,
                onClick = {
                    showMenu = false
                    uriHandler.openUri(youtubeSearchUrl(query))
                },
            )
        }
    }
}

@Composable
private fun SearchMenuItem(
    text: String,
    @DrawableRes icon: Int,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = {
            Text(
                text = text,
                style = TraktTheme.typography.buttonTertiary,
                color = TraktTheme.colors.textPrimary,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier.size(20.dp),
            )
        },
        onClick = onClick,
    )
}

internal fun soundtrackSearchQuery(
    mediaTitle: String,
    track: SoundtrackTrack,
): String {
    val song = listOfNotNull(track.performer, track.title).joinToString(" - ")
    return "$mediaTitle OST: $song"
}

private fun webSearchUrl(query: String) = "https://www.google.com/search?q=${Uri.encode(query)}"

private fun youtubeSearchUrl(query: String) = "https://www.youtube.com/results?search_query=${Uri.encode(query)}"

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        SoundtrackSearchMenu(
            track = previewTrack(spotifyId = null),
            mediaTitle = "The Social Network",
        )
    }
}
