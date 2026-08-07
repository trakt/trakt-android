@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.calendar.feature.monthly.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.core.calendar.model.CalendarDayDisplay
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private val MenuShape = RoundedCornerShape(16.dp)

@Composable
internal fun CalendarDisplayDropdown(
    current: CalendarDayDisplay,
    modifier: Modifier = Modifier,
    onDisplayClick: (CalendarDayDisplay) -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Icon(
            painter = painterResource(R.drawable.ic_more_vertical),
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
            modifier = Modifier
                .size(18.dp)
                .onClick { expanded = true },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = TraktTheme.colors.dialogContainer,
            shape = MenuShape,
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(current.toggle.textRes),
                        style = TraktTheme.typography.buttonTertiary,
                        color = TraktTheme.colors.textPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp),
                    )
                },
                onClick = {
                    expanded = false
                    onDisplayClick(current.toggle)
                },
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        CalendarDisplayDropdown(current = CalendarDayDisplay.Posters)
    }
}
