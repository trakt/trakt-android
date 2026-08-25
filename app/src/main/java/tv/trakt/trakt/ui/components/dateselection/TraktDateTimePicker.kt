@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.ui.components.dateselection

import android.os.Build
import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.nowLocal
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.ui.theme.colors.Shade910
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset.UTC
import java.util.Locale

object PastSelectableDates : SelectableDates {
    private val nowUtc = nowUtc()
    private val nowInstantUtc = nowUtcInstant()

    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        return utcTimeMillis <= nowInstantUtc.toEpochMilli()
    }

    override fun isSelectableYear(year: Int): Boolean {
        return year <= nowUtc.year
    }
}

internal class RangeSelectableDates(
    private val minDay: LocalDate,
    private val maxDay: LocalDate,
) : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        val day = Instant.ofEpochMilli(utcTimeMillis).atZone(UTC).toLocalDate()
        return !day.isBefore(minDay) && !day.isAfter(maxDay)
    }

    override fun isSelectableYear(year: Int): Boolean {
        return year in minDay.year..maxDay.year
    }
}

@Composable
internal fun TraktTimePicker(
    active: Boolean,
    selectedDate: Instant?,
    onDateTimeSelected: (dateTime: Instant) -> Unit,
    onDismiss: () -> Unit,
    initialTime: LocalTime? = null,
) {
    val context = LocalContext.current

    val initial = initialTime ?: nowLocal().toLocalTime()
    val timePickerState = rememberTimePickerState(
        is24Hour = DateFormat.is24HourFormat(context),
        initialHour = initial.hour,
        initialMinute = initial.minute,
    )

    if (active) {
        TimePickerDialog(
            onDismissRequest = onDismiss,
            title = {},
            containerColor = TraktTheme.colors.dialogContainer,
            confirmButton = {
                Row(
                    horizontalArrangement = spacedBy(24.dp),
                    verticalAlignment = CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.button_text_cancel),
                        style = TraktTheme.typography.buttonPrimary.copy(
                            fontWeight = W500,
                        ),
                        color = TraktTheme.colors.textSecondary,
                        modifier = Modifier
                            .onClick {
                                onDismiss()
                            },
                    )
                    Text(
                        text = "OK",
                        style = TraktTheme.typography.buttonPrimary.copy(
                            fontWeight = W500,
                        ),
                        color = TraktTheme.colors.textPrimary,
                        modifier = Modifier
                            .onClick {
                                val selectedInstant = selectedDate ?: return@onClick

                                val updatedInstant = selectedInstant
                                    .plusSeconds(timePickerState.hour * 3600L)
                                    .plusSeconds(timePickerState.minute * 60L)

                                onDateTimeSelected(updatedInstant)
                            },
                    )
                }
            },
        ) {
            TimePicker(
                state = timePickerState,
                colors = TimePickerDefaults.colors(
                    containerColor = TraktTheme.colors.dialogContainer,
                    selectorColor = TraktTheme.colors.accent,
                    timeSelectorSelectedContentColor = TraktTheme.colors.textPrimary,
                    timeSelectorUnselectedContentColor = TraktTheme.colors.textPrimary,
                    timeSelectorSelectedContainerColor = TraktTheme.colors.accent,
                    timeSelectorUnselectedContainerColor = TraktTheme.colors.dialogContent,
                    periodSelectorBorderColor = TraktTheme.colors.dialogContent,
                    periodSelectorSelectedContainerColor = TraktTheme.colors.accent,
                    periodSelectorUnselectedContainerColor = TraktTheme.colors.dialogContainer,
                    periodSelectorSelectedContentColor = TraktTheme.colors.textPrimary,
                    periodSelectorUnselectedContentColor = TraktTheme.colors.textPrimary,
                    clockDialColor = TraktTheme.colors.dialogContent,
                    clockDialUnselectedContentColor = TraktTheme.colors.textPrimary,
                    clockDialSelectedContentColor = TraktTheme.colors.textPrimary,
                ),
            )
        }
    }
}

@Composable
internal fun TraktDatePicker(
    active: Boolean,
    onDateSelected: (date: Instant) -> Unit,
    onDismiss: () -> Unit,
    initialDate: LocalDate = nowLocalDay(),
    selectableDates: SelectableDates = PastSelectableDates,
) {
    val pickerLocale = pickerLocale()
    val datePickerState = remember {
        DatePickerState(
            locale = pickerLocale,
            initialSelectedDate = initialDate,
            selectableDates = selectableDates,
        )
    }

    if (active) {
        DatePickerDialog(
            onDismissRequest = onDismiss,
            colors = DatePickerDefaults.colors(
                containerColor = TraktTheme.colors.dialogContainer,
            ),
            confirmButton = {
                Row(
                    horizontalArrangement = spacedBy(24.dp),
                    verticalAlignment = CenterVertically,
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text(
                        text = stringResource(R.string.button_text_cancel),
                        style = TraktTheme.typography.buttonPrimary.copy(
                            fontWeight = W500,
                        ),
                        color = TraktTheme.colors.textSecondary,
                        modifier = Modifier
                            .onClick {
                                onDismiss()
                            },
                    )
                    Text(
                        text = "OK",
                        style = TraktTheme.typography.buttonPrimary.copy(
                            fontWeight = W500,
                        ),
                        color = TraktTheme.colors.textPrimary,
                        modifier = Modifier
                            .onClick {
                                val date = datePickerState.selectedDateMillis ?: return@onClick
                                onDateSelected(
                                    Instant.ofEpochMilli(date),
                                )
                            },
                    )
                }
            },
        ) {
            DatePicker(
                state = datePickerState,
                title = {},
                showModeToggle = false,
                colors = DatePickerDefaults.colors(
                    dividerColor = Shade910,
                    containerColor = TraktTheme.colors.dialogContainer,
                    titleContentColor = TraktTheme.colors.textPrimary,
                    headlineContentColor = TraktTheme.colors.textPrimary,
                    weekdayContentColor = TraktTheme.colors.textPrimary,
                    dayContentColor = TraktTheme.colors.textSecondary,
                    todayContentColor = TraktTheme.colors.textPrimary,
                    yearContentColor = TraktTheme.colors.textSecondary,
                    currentYearContentColor = TraktTheme.colors.accent,
                    selectedYearContentColor = TraktTheme.colors.textPrimary,
                    selectedDayContainerColor = TraktTheme.colors.accent,
                    selectedYearContainerColor = TraktTheme.colors.accent,
                    navigationContentColor = TraktTheme.colors.textSecondary,
                    todayDateBorderColor = TraktTheme.colors.accent,
                    disabledDayContentColor = TraktTheme.colors.textSecondary.copy(
                        alpha = 0.2f,
                    ),
                    disabledYearContentColor = TraktTheme.colors.textSecondary.copy(
                        alpha = 0.2f,
                    ),
                ),
            )
        }
    }
}

@Composable
@ReadOnlyComposable
private fun pickerLocale(): Locale {
    val locale = LocalConfiguration.current.locales[0]
    val builder = Locale.Builder().setLocale(locale)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        builder.setUnicodeLocaleKeyword("fw", "mon")
    } else {
        builder.setRegion("GB")
    }
    return builder.build()
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
    locale = "us",
)
@Composable
private fun DatePickerPreview() {
    TraktTheme {
        TraktDatePicker(
            active = true,
            onDateSelected = {},
            onDismiss = {},
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
    locale = "us",
)
@Composable
private fun TimePickerPreview() {
    TraktTheme {
        TraktTimePicker(
            active = true,
            selectedDate = Instant.EPOCH,
            onDateTimeSelected = {},
            onDismiss = {},
        )
    }
}
