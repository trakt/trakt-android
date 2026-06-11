package tv.trakt.trakt.ui.components

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun TraktShareButton(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    onShareLinkClick: () -> Unit = {},
    onShareImageClick: () -> Unit = {},
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Icon(
            painter = painterResource(R.drawable.ic_share),
            tint = TraktTheme.colors.textPrimary,
            contentDescription = null,
            modifier = Modifier
                .size(size)
                .onClick { showMenu = true },
        )
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            containerColor = TraktTheme.colors.dialogContainer,
            shape = RoundedCornerShape(16.dp),
        ) {
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_link),
                        contentDescription = null,
                        tint = TraktTheme.colors.textPrimary,
                        modifier = Modifier.size(22.dp),
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.button_text_media_share_type_link),
                        style = TraktTheme.typography.buttonTertiary,
                        color = TraktTheme.colors.textPrimary,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                onClick = {
                    showMenu = false
                    onShareLinkClick()
                },
            )
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_image),
                        contentDescription = null,
                        tint = TraktTheme.colors.textPrimary,
                        modifier = Modifier.size(22.dp),
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.button_text_media_share_type_image),
                        style = TraktTheme.typography.buttonTertiary,
                        color = TraktTheme.colors.textPrimary,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                onClick = {
                    showMenu = false
                    onShareImageClick()
                },
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF212427)
@Composable
private fun Preview() {
    TraktTheme {
        TraktShareButton(
            onShareLinkClick = {},
            onShareImageClick = {},
        )
    }
}
