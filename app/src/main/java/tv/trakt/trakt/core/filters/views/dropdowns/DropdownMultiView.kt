package tv.trakt.trakt.core.filters.views.dropdowns

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun <T> DropdownMultiView(
    header: String,
    values: ImmutableList<DropdownOption<T>>,
    active: Boolean,
    options: ImmutableList<DropdownOption<T>>,
    onOptionsSelected: (List<DropdownOption<T>>) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var showMenu by remember { mutableStateOf(false) }

    val selected = values.any { it.raw != null }

    Box(
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = spacedBy(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = when (active) {
                        true -> TraktTheme.colors.dropdownContainerActive
                        false -> TraktTheme.colors.dropdownContainer
                    },
                    shape = RoundedCornerShape(15.dp),
                )
                .padding(
                    horizontal = 13.dp,
                    vertical = 13.dp,
                )
                .onClick(enabled = enabled) {
                    showMenu = true
                },
        ) {
            Text(
                text = when {
                    selected -> values.joinToString(", ") { it.displayString }
                    else -> header
                },
                style = TraktTheme.typography.buttonTertiary,
                color = when (active) {
                    true -> TraktTheme.colors.dropdownContentActive
                    false -> TraktTheme.colors.dropdownContent
                },
                maxLines = 1,
                overflow = Ellipsis,
            )
        }

        DropdownMenu(
            expanded = showMenu,
            containerColor = TraktTheme.colors.dropdownMenuContainer,
            shape = RoundedCornerShape(16.dp),
            onDismissRequest = {
                showMenu = false
            },
            offset = DpOffset(0.dp, (-4).dp),
            modifier = Modifier
                .align(Alignment.BottomEnd),
        ) {
            for (option in options) {
                DropdownMenuItem(
                    text = {
                        Row(
                            horizontalArrangement = spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (option in values) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_check),
                                    contentDescription = null,
                                    tint = TraktTheme.colors.textPrimary,
                                    modifier = Modifier.size(12.dp),
                                )
                            }
                            Text(
                                text = option.displayString,
                                style = TraktTheme.typography.buttonTertiary,
                                color = when (option in values) {
                                    true -> TraktTheme.colors.textPrimary
                                    false -> TraktTheme.colors.textSecondary
                                },
                            )
                        }
                    },
                    onClick = {
                        when {
                            option.raw == null -> {
                                onOptionsSelected.invoke(emptyList())
                                return@DropdownMenuItem
                            }
                            option in values -> {
                                onOptionsSelected.invoke(
                                    values.filterNot { it == option },
                                )
                            }
                            else -> {
                                onOptionsSelected.invoke(values + option)
                            }
                        }
                    },
                )
            }
        }
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun PreviewDropdownView() {
    TraktTheme {
        DropdownMultiView(
            header = "Genre",
            active = true,
            values = persistentListOf<DropdownOption<String?>>(
                DropdownOption(raw = "Action", displayString = "Action"),
                DropdownOption(raw = "Comedy", displayString = "Comedy"),
            ),
            options = persistentListOf(
                DropdownOption(raw = null, displayString = "All"),
                DropdownOption(raw = "Action", displayString = "Action"),
                DropdownOption(raw = "Comedy", displayString = "Comedy"),
            ),
            onOptionsSelected = {},
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun PreviewDropdownViewInactive() {
    TraktTheme {
        DropdownMultiView(
            header = "Genre",
            active = false,
            values = persistentListOf<DropdownOption<String?>>(
                DropdownOption(raw = null, displayString = "All"),
            ),
            options = persistentListOf(
                DropdownOption(raw = null, displayString = "All"),
                DropdownOption(raw = "Action", displayString = "Action"),
                DropdownOption(raw = "Comedy", displayString = "Comedy"),
            ),
            onOptionsSelected = {},
        )
    }
}
