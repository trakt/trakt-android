@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.ui.components.dateselection

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import timber.log.Timber
import tv.trakt.trakt.common.helpers.extensions.nowLocal
import tv.trakt.trakt.common.model.CustomDate
import tv.trakt.trakt.common.model.DateSelectionResult
import tv.trakt.trakt.common.model.Now
import tv.trakt.trakt.common.model.ReleaseDate
import tv.trakt.trakt.common.model.UnknownDate
import tv.trakt.trakt.core.checkin.data.CheckInManager
import tv.trakt.trakt.core.notifications.data.work.ScheduleNotificationsWorker
import tv.trakt.trakt.core.notifications.usecases.EnableNotificationsUseCase
import tv.trakt.trakt.ui.components.TraktBottomSheet
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.Instant

@Composable
internal fun DateSelectionSheet(
    state: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    ),
    active: Boolean = false,
    title: String,
    subtitle: String? = null,
    nowWatchingVisible: Boolean = false,
    onCheckIn: () -> Unit = {},
    onResult: (result: DateSelectionResult) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val checkInManager = koinInject<CheckInManager>()
    val notificationsUseCase = koinInject<EnableNotificationsUseCase>()

    var datePicker by remember { mutableStateOf(false) }
    var timePicker by remember { mutableStateOf<Instant?>(null) }
    val hasActiveCheckIn = remember(checkInManager.isActive()) {
        checkInManager.isActive()
    }

    if (active) {
        TraktBottomSheet(
            sheetState = state,
            onDismiss = onDismiss,
        ) {
            DateSelectionView(
                title = title,
                subtitle = subtitle,
                nowWatchingVisible = nowWatchingVisible,
                nowWatchingEnabled = !hasActiveCheckIn,
                onNowWatchingClick = {
                    scope.dismissWithAction(
                        sheet = state,
                        action = { onCheckIn() },
                        onDismiss = onDismiss,
                    )
                },
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
                onPermissionGranted = {
                    scope.launch {
                        state.hide()
                        notificationsUseCase.enableNotifications(true)
                        ScheduleNotificationsWorker.schedule(context.applicationContext)
                    }.invokeOnCompletion {
                        if (!state.isVisible) {
                            onCheckIn()
                        }
                    }
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
            onCheckIn = { },
            onDismiss = { },
        )
    }
}
