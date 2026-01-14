package tv.trakt.trakt.core.settings.features.notifications

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import tv.trakt.trakt.core.notifications.model.DeliveryAdjustment
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AdjustNotificationTimeView(
    current: DeliveryAdjustment,
    onApply: (DeliveryAdjustment) -> Unit = { _ -> },
) {
    AdjustNotificationTimeContent(
        current = current,
        onApply = onApply,
    )
}

@Composable
private fun AdjustNotificationTimeContent(
    current: DeliveryAdjustment,
    onApply: (DeliveryAdjustment) -> Unit = { _ -> },
) {
    var selected by remember { mutableStateOf(current.name) }

    Column(
        verticalArrangement = spacedBy(12.dp),
        modifier = Modifier
            .padding(bottom = 24.dp),
    ) {
        TraktHeader(
            title = stringResource(R.string.text_settings_adjust_delivery),
            subtitle = stringResource(R.string.text_settings_adjust_delivery_description),
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp),
        )

        Column(
            verticalArrangement = spacedBy(TraktTheme.spacing.contextItemsSpace),
            modifier = Modifier
                .padding(horizontal = 16.dp),
        ) {
            for (time in DeliveryAdjustment.entries) {
                GhostButton(
                    text = stringResource(time.displayString),
                    contentColor = when {
                        time.name == selected -> TraktTheme.colors.primaryButtonContent
                        else -> TraktTheme.colors.textSecondary
                    },
                    icon = when {
                        time.name == selected -> painterResource(R.drawable.ic_check)
                        else -> null
                    },
                    iconSize = 18.dp,
                    iconSpace = 12.dp,
                    onClick = {
                        selected = time.name
                    },
                )
            }
        }

        PrimaryButton(
            text = stringResource(R.string.button_text_apply),
            onClick = {
                onApply(DeliveryAdjustment.valueOf(selected))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 20.dp),
        )
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
        AdjustNotificationTimeContent(
            current = DeliveryAdjustment.MINUTES_30,
        )
    }
}
