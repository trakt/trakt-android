package tv.trakt.trakt.ui.components.mediacards.list

import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private const val DISABLED_ALPHA = 0.33F

@Composable
internal fun PersonalListDropdown(
    reorderEnabled: Boolean,
    onEditClick: () -> Unit,
    onReorderClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 16.dp,
    onAddFromListsClick: (() -> Unit)? = null,
    onCollaboratorsClick: (() -> Unit)? = null,
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Icon(
            painter = painterResource(R.drawable.ic_more_vertical),
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
            modifier = Modifier
                .size(iconSize)
                .onClick { showMenu = true },
        )

        DropdownMenu(
            expanded = showMenu,
            containerColor = TraktTheme.colors.dialogContainer,
            shape = RoundedCornerShape(16.dp),
            onDismissRequest = { showMenu = false },
        ) {
            MenuItem(
                text = stringResource(R.string.page_title_edit_list),
                icon = R.drawable.ic_edit,
                iconSize = 22.dp,
                onClick = {
                    showMenu = false
                    onEditClick()
                },
            )

            MenuItem(
                text = stringResource(R.string.button_text_reorder),
                icon = R.drawable.ic_reorder,
                iconSize = 24.dp,
                enabled = reorderEnabled,
                onClick = {
                    showMenu = false
                    onReorderClick()
                },
            )

            if (onAddFromListsClick != null) {
                MenuItem(
                    text = stringResource(R.string.button_text_add_from_lists),
                    icon = R.drawable.ic_plus,
                    iconSize = 22.dp,
                    onClick = {
                        showMenu = false
                        onAddFromListsClick()
                    },
                )
            }

            if (onCollaboratorsClick != null) {
                MenuItem(
                    text = stringResource(R.string.button_text_manage_collaborators),
                    icon = R.drawable.ic_person_double,
                    iconSize = 22.dp,
                    onClick = {
                        showMenu = false
                        onCollaboratorsClick()
                    },
                )
            }

            MenuItem(
                text = stringResource(R.string.button_text_share),
                icon = R.drawable.ic_share,
                iconSize = 22.dp,
                onClick = {
                    showMenu = false
                    onShareClick()
                },
            )
        }
    }
}

@Composable
internal fun CollaborationListDropdown(
    onLeaveClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 16.dp,
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Icon(
            painter = painterResource(R.drawable.ic_more_vertical),
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
            modifier = Modifier
                .size(iconSize)
                .onClick { showMenu = true },
        )

        DropdownMenu(
            expanded = showMenu,
            containerColor = TraktTheme.colors.dialogContainer,
            shape = RoundedCornerShape(16.dp),
            onDismissRequest = { showMenu = false },
        ) {
            MenuItem(
                text = stringResource(R.string.button_text_leave_collaboration),
                icon = R.drawable.ic_logout,
                iconSize = 22.dp,
                onClick = {
                    showMenu = false
                    onLeaveClick()
                },
            )
        }
    }
}

@Composable
private fun MenuItem(
    text: String,
    icon: Int,
    iconSize: Dp,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    val contentColor = TraktTheme.colors.textPrimary.copy(
        alpha = if (enabled) 1F else DISABLED_ALPHA,
    )

    DropdownMenuItem(
        enabled = enabled,
        text = {
            Text(
                text = text,
                style = TraktTheme.typography.buttonTertiary,
                color = contentColor,
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(iconSize),
            )
        },
        onClick = onClick,
    )
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        PersonalListDropdown(
            reorderEnabled = true,
            onEditClick = {},
            onReorderClick = {},
            onShareClick = {},
            onAddFromListsClick = {},
        )
    }
}
