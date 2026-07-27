@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.comments.ui

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
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun CommentDropdown(
    modifier: Modifier = Modifier,
    containerColor: Color = TraktTheme.colors.dialogContainer,
    onReportClick: (() -> Unit)? = null,
) {
    val inspection = LocalInspectionMode.current
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
            expanded = inspection || showMenu,
            onDismissRequest = { showMenu = false },
            containerColor = containerColor,
            shape = RoundedCornerShape(16.dp),
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(R.string.button_text_report_comment),
                        style = TraktTheme.typography.buttonTertiary,
                        color = TraktTheme.colors.textPrimary,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_flag),
                        contentDescription = null,
                        tint = TraktTheme.colors.textPrimary,
                    )
                },
                onClick = {
                    showMenu = false
                    onReportClick?.invoke()
                },
            )
        }
    }
}

@Preview
@Composable
private fun CommentDropdownPreview() {
    TraktTheme {
        CommentDropdown()
    }
}
