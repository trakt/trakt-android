package tv.trakt.trakt.core.settings.features.avatar

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.ui.theme.colors.Red400
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AvatarMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onUploadClick: () -> Unit,
    onRemoveClick: () -> Unit,
) {
    val removeColor = when (TraktTheme.colors.isLight) {
        true -> Red500
        false -> Red400
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        containerColor = TraktTheme.colors.dialogContainer,
        shape = RoundedCornerShape(16.dp),
    ) {
        AvatarMenuItem(
            text = stringResource(R.string.button_text_upload_photo),
            icon = R.drawable.ic_image,
            color = TraktTheme.colors.textPrimary,
            onClick = {
                onDismiss()
                onUploadClick()
            },
        )
        AvatarMenuItem(
            text = stringResource(R.string.button_text_remove_photo),
            icon = R.drawable.ic_trash,
            color = removeColor,
            onClick = {
                onDismiss()
                onRemoveClick()
            },
        )
    }
}

@Composable
private fun AvatarMenuItem(
    text: String,
    @DrawableRes icon: Int,
    color: Color,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = {
            Text(
                text = text,
                style = TraktTheme.typography.buttonTertiary,
                color = color,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = color,
            )
        },
        onClick = onClick,
    )
}
