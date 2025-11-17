@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.ui.components.dateselection

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.helpers.extensions.nowLocal
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.ui.theme.colors.Shade910
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktBottomSheet
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.Instant

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

@Composable
internal fun DateSelectionSheet(
    state: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    ),
    active: Boolean = false,
    title: String,
    subtitle: String? = null,
    onResult: (result: DateSelectionResult) -> Unit,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    var datePicker by remember { mutableStateOf(false) }
    var timePicker by remember { mutableStateOf<Instant?>(null) }

    if (active) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            DateSelectionView(
                title = title,
                subtitle = subtitle,
                onNowClick = {
                    scope.dismissWithAction(
                        sheet = state,
                        action = { onResult(Now) },
                        onDismiss = onDismiss,
                    )
                },
                onReleaseClick = {
                    scope.dismissWithAction(
                        sheet = state,
                        action = { onResult(ReleaseDate) },
                        onDismiss = onDismiss,
                    )
                },
                onOtherClick = {
                    datePicker = true
                    timePicker = null
                },
                onUnknownClick = {
                    scope.dismissWithAction(
                        sheet = state,
                        action = { onResult(UnknownDate) },
                        onDismiss = onDismiss,
                    )
                },
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .padding(horizontal = 24.dp),
            )
        }
    }

    TraktDatePicker(
        active = datePicker,
        onDateSelected = {
            timePicker = it
        },
        onDismiss = {
            datePicker = false
            timePicker = null
        },
    )

    TraktTimePicker(
        active = timePicker != null,
        selectedDate = timePicker,
        onDateTimeSelected = { dateTimeUtc ->
            timePicker = null
            datePicker = false
            scope.dismissWithAction(
                sheet = state,
                action = {
                    val localOffset = nowLocal().offset.totalSeconds
                    val localDateTime = dateTimeUtc.plusSeconds(-localOffset.toLong())
                    onResult(CustomDate(localDateTime))

                    Timber.d("Selected date time: UTC=$dateTimeUtc, Local=$localDateTime")
                },
                onDismiss = onDismiss,
            )
        },
        onDismiss = {
            timePicker = null
        },
    )
}

@Composable
private fun TraktTimePicker(
    active: Boolean,
    selectedDate: Instant?,
    onDateTimeSelected: (dateTime: Instant) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    val timePickerState = rememberTimePickerState(
        is24Hour = DateFormat.is24HourFormat(context),
        initialHour = 20,
        initialMinute = 0,
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
private fun TraktDatePicker(
    active: Boolean,
    onDateSelected: (date: Instant) -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDate = nowLocalDay(),
        selectableDates = PastSelectableDates,
    )

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

private fun CoroutineScope.dismissWithAction(
    sheet: SheetState,
    action: () -> Unit = {},
    onDismiss: () -> Unit,
) {
    launch {
        sheet.hide()
    }.invokeOnCompletion {
        if (!sheet.isVisible) {
            action()
            onDismiss()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    TraktTheme {
        DateSelectionSheet(
            state = rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
            ),
            title = "The Matrix",
            onResult = { },
            onDismiss = { },
        )
    }
}
