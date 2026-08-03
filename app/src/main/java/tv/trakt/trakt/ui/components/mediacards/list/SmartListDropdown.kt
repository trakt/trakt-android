@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.ui.components.mediacards.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SmartListDropdown(
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = TraktTheme.colors.dialogContainer,
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Icon(
            painter = painterResource(R.drawable.ic_more_vertical),
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
            modifier = Modifier
                .size(16.dp)
                .onClick { showMenu = true },
        )
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            containerColor = containerColor,
            shape = RoundedCornerShape(16.dp),
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(R.string.button_text_delete_list),
                        style = TraktTheme.typography.buttonTertiary,
                        color = TraktTheme.colors.textPrimary,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_trash),
                        contentDescription = null,
                        tint = TraktTheme.colors.textPrimary,
                    )
                },
                onClick = {
                    showMenu = false
                    onDeleteClick()
                },
            )
        }
    }
}

@Preview
@Composable
private fun SmartListDropdownPreview() {
    TraktTheme {
        SmartListDropdown(onDeleteClick = {})
    }
}
