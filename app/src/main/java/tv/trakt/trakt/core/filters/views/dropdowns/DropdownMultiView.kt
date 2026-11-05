package tv.trakt.trakt.core.filters.views.dropdowns

import androidx.annotation.DrawableRes
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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.common.ui.theme.colors.Shade10
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun <T> DropdownMultiView(
    header: String,
    values: ImmutableList<DropdownOption<T>>,
    active: Boolean,
    options: ImmutableList<DropdownOption<T>>,
    modifier: Modifier = Modifier,
    onOptionsSelected: (List<DropdownOption<T>>) -> Unit = {},
    enabled: Boolean = true,
    exclusion: DropdownExclusion<T>? = null,
) {
    var showMenu by remember { mutableStateOf(false) }

    val included = values.filter { it.raw != null }
    val excluded = exclusion?.values.orEmpty()
    val selected = included.isNotEmpty() || excluded.isNotEmpty()

    val excludedLabels = excluded.map {
        stringResource(R.string.option_text_excluded, it.displayString)
    }

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
                    selected -> (included.map { it.displayString } + excludedLabels).joinToString(", ")
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
                if (exclusion != null && option.raw != null) {
                    ExcludableMenuItem(
                        option = option,
                        state = when (option) {
                            in included -> DropdownOptionState.Included
                            in excluded -> DropdownOptionState.Excluded
                            else -> null
                        },
                        onStateChange = { state ->
                            exclusion.onSelectionChange(
                                included.filterNot { it == option } +
                                    listOfNotNull(option.takeIf { state == DropdownOptionState.Included }),
                                excluded.filterNot { it == option } +
                                    listOfNotNull(option.takeIf { state == DropdownOptionState.Excluded }),
                            )
                        },
                    )
                    continue
                }

                val checked = option in values && (option.raw != null || excluded.isEmpty())

                DropdownMenuItem(
                    text = {
                        Row(
                            horizontalArrangement = spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (checked) {
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
                                color = when (checked) {
                                    true -> TraktTheme.colors.textPrimary
                                    false -> TraktTheme.colors.textSecondary
                                },
                            )
                        }
                    },
                    onClick = {
                        when {
                            option.raw == null -> {
                                when (exclusion) {
                                    null -> onOptionsSelected.invoke(emptyList())
                                    else -> exclusion.onSelectionChange(emptyList(), emptyList())
                                }
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

@Immutable
internal class DropdownExclusion<T>(
    val values: ImmutableList<DropdownOption<T>>,
    val onSelectionChange: (included: List<DropdownOption<T>>, excluded: List<DropdownOption<T>>) -> Unit,
)

private enum class DropdownOptionState {
    Included,
    Excluded,
}

@Composable
private fun <T> ExcludableMenuItem(
    option: DropdownOption<T>,
    state: DropdownOptionState?,
    onStateChange: (DropdownOptionState?) -> Unit,
) {
    DropdownMenuItem(
        leadingIcon = {
            ExclusionToggle(
                icon = R.drawable.ic_plus,
                selected = state == DropdownOptionState.Included,
                selectedColor = TraktTheme.colors.accent,
                onClick = {
                    onStateChange(DropdownOptionState.Included.takeIf { state != it })
                },
            )
        },
        text = {
            Text(
                text = option.displayString,
                style = TraktTheme.typography.buttonTertiary,
                color = when (state) {
                    DropdownOptionState.Included -> TraktTheme.colors.accent
                    DropdownOptionState.Excluded -> Red500
                    null -> TraktTheme.colors.textSecondary
                },
                textDecoration = TextDecoration.LineThrough.takeIf {
                    state == DropdownOptionState.Excluded
                },
            )
        },
        trailingIcon = {
            ExclusionToggle(
                icon = R.drawable.ic_minus,
                selected = state == DropdownOptionState.Excluded,
                selectedColor = Red500,
                onClick = {
                    onStateChange(DropdownOptionState.Excluded.takeIf { state != it })
                },
            )
        },
        onClick = {
            onStateChange(DropdownOptionState.Included.takeIf { state == null })
        },
    )
}

@Composable
private fun ExclusionToggle(
    @DrawableRes icon: Int,
    selected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(24.dp)
            .background(
                color = if (selected) selectedColor else Color.Transparent,
                shape = RoundedCornerShape(6.dp),
            )
            .onClick(throttle = false, onClick = onClick),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = when {
                selected -> Shade10
                else -> TraktTheme.colors.textSecondary.copy(alpha = 0.35F)
            },
            modifier = Modifier.size(12.dp),
        )
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

@PreviewTraktLightDark
@Composable
private fun PreviewDropdownViewExcluded() {
    TraktThemePreview {
        DropdownMultiView(
            header = "Genre",
            active = true,
            values = persistentListOf<DropdownOption<String?>>(
                DropdownOption(raw = "Action", displayString = "Action"),
            ),
            options = persistentListOf(
                DropdownOption(raw = null, displayString = "All"),
                DropdownOption(raw = "Action", displayString = "Action"),
                DropdownOption(raw = "Comedy", displayString = "Comedy"),
            ),
            exclusion = DropdownExclusion(
                values = persistentListOf(
                    DropdownOption(raw = "Comedy", displayString = "Comedy"),
                ),
                onSelectionChange = { _, _ -> },
            ),
        )
    }
}
