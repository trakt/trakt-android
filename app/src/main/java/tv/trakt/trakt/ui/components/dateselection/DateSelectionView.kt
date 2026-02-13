package tv.trakt.trakt.ui.components.dateselection

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import tv.trakt.trakt.common.ui.theme.colors.Shade910
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.components.notifications.NotificationsRationaleSheet
import tv.trakt.trakt.ui.theme.TraktTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DateSelectionView(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    nowWatchingEnabled: Boolean = true,
    nowWatchingVisible: Boolean = false,
    onNowWatchingClick: () -> Unit = {},
    onNowClick: () -> Unit = {},
    onReleaseClick: () -> Unit = {},
    onOtherClick: () -> Unit = {},
    onUnknownClick: () -> Unit = {},
    onPermissionGranted: () -> Unit = {},
) {
    val context = LocalContext.current
    val activity = LocalActivity.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                onPermissionGranted()
            } else {
                onNowWatchingClick()
            }
        },
    )

    var rationaleSheet by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                verticalArrangement = spacedBy(1.dp),
            ) {
                Text(
                    text = title.ifBlank {
                        stringResource(R.string.button_text_track)
                    },
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.heading3,
                    maxLines = 1,
                    overflow = Ellipsis,
                    autoSize = TextAutoSize.StepBased(
                        maxFontSize = TraktTheme.typography.heading3.fontSize,
                        minFontSize = 20.sp,
                        stepSize = 2.sp,
                    ),
                )

                Text(
                    text = (subtitle ?: "").ifBlank {
                        stringResource(R.string.button_text_track)
                    },
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.paragraphSmaller,
                    maxLines = 1,
                    overflow = Ellipsis,
                )
            }
        }

        Spacer(
            modifier = Modifier
                .padding(top = 22.dp)
                .background(Shade910)
                .fillMaxWidth()
                .height(1.dp),
        )

        ActionButtons(
            nowWatchingEnabled = nowWatchingEnabled,
            nowWatchingVisible = nowWatchingVisible,
            onNowWatchingClick = {
                checkNotificationPermission(
                    context = context,
                    onPermissionGranted = onNowWatchingClick,
                    onRequestPermission = {
                        activity?.let {
                            when (
                                ActivityCompat.shouldShowRequestPermissionRationale(
                                    activity,
                                    POST_NOTIFICATIONS,
                                )
                            ) {
                                true -> rationaleSheet = true
                                else -> permissionLauncher.launch(POST_NOTIFICATIONS)
                            }
                        } ?: permissionLauncher.launch(POST_NOTIFICATIONS)
                    },
                )
            },
            onNowClick = onNowClick,
            onReleaseClick = onReleaseClick,
            onOtherClick = onOtherClick,
            onUnknownClick = onUnknownClick,
            modifier = Modifier
                .padding(top = 13.dp),
        )
    }

    NotificationsRationaleSheet(
        active = rationaleSheet,
        onOk = {
            rationaleSheet = false
            checkNotificationPermission(
                context = context,
                onPermissionGranted = onNowWatchingClick,
                onRequestPermission = {
                    permissionLauncher.launch(POST_NOTIFICATIONS)
                },
            )
        },
    )
}

@Composable
private fun ActionButtons(
    modifier: Modifier = Modifier,
    nowWatchingEnabled: Boolean = true,
    nowWatchingVisible: Boolean = false,
    onNowWatchingClick: () -> Unit = {},
    onNowClick: () -> Unit = {},
    onReleaseClick: () -> Unit = {},
    onOtherClick: () -> Unit = {},
    onUnknownClick: () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.contextItemsSpace),
        modifier = modifier
            .graphicsLayer {
                translationX = -6.dp.toPx()
            },
    ) {
        if (nowWatchingVisible) {
            GhostButton(
                text = stringResource(R.string.text_now_watching),
                icon = painterResource(R.drawable.ic_eye),
                iconSize = 22.dp,
                iconSpace = 16.dp,
                onClick = onNowWatchingClick,
                enabled = nowWatchingEnabled,
            )
        }
        GhostButton(
            text = stringResource(R.string.button_text_mark_as_watched_now),
            icon = painterResource(R.drawable.ic_check),
            iconSize = 22.dp,
            iconSpace = 16.dp,
            onClick = onNowClick,
        )
        GhostButton(
            text = stringResource(R.string.button_text_mark_as_watched_release_date),
            icon = painterResource(R.drawable.ic_calendar_time_trakt),
            iconSize = 21.dp,
            iconSpace = 17.dp,
            onClick = onReleaseClick,
        )
        GhostButton(
            text = stringResource(R.string.button_text_mark_as_watched_other_date),
            icon = painterResource(R.drawable.ic_edit),
            iconSize = 22.dp,
            iconSpace = 16.dp,
            onClick = onOtherClick,
        )
        GhostButton(
            text = stringResource(R.string.button_text_mark_as_watched_unknown_date),
            icon = painterResource(R.drawable.ic_question),
            iconSize = 22.dp,
            iconSpace = 16.dp,
            onClick = onUnknownClick,
        )
    }
}

private fun checkNotificationPermission(
    context: Context,
    onPermissionGranted: () -> Unit,
    onRequestPermission: () -> Unit,
) {
    val isAtLeastTiramisu = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    val hasPermission = if (isAtLeastTiramisu) {
        ContextCompat.checkSelfPermission(context, POST_NOTIFICATIONS) == PERMISSION_GRANTED
    } else {
        true
    }

    if (isAtLeastTiramisu && !hasPermission) {
        onRequestPermission()
    } else {
        onPermissionGranted()
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        DateSelectionView(
            title = "Lord of the Rings",
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview2() {
    TraktTheme {
        DateSelectionView(
            title = "Lord of the Rings",
            subtitle = "S1 • E1 - The Pilot",
        )
    }
}
