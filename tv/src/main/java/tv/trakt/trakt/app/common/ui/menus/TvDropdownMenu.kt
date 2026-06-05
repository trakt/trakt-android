package tv.trakt.trakt.app.common.ui.menus

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.app.ui.theme.TraktTheme

@Composable
internal fun TvDropdownMenu(
    visible: Boolean,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    DropdownMenu(
        containerColor = TraktTheme.colors.dialogContainer,
        shape = RoundedCornerShape(20.dp),
        expanded = visible,
        onDismissRequest = { onDismiss() },
        content = content,
        modifier = modifier,
    )
}
