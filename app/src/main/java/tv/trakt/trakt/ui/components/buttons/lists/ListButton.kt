package tv.trakt.trakt.ui.components.buttons.lists

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ListButton(
    text: String,
    checked: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(16.dp),
        modifier = modifier
            .alpha(if (enabled) 1F else 0.2F),
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            enabled = false,
            colors = CheckboxDefaults.colors().copy(
                uncheckedBoxColor = Color.Transparent,
                uncheckedBorderColor = TraktTheme.colors.primaryButtonContent,
                checkedBoxColor = TraktTheme.colors.primaryButtonContent,
                checkedBorderColor = TraktTheme.colors.primaryButtonContent,
                checkedCheckmarkColor = TraktTheme.colors.dialogContainer,
                disabledUncheckedBoxColor = Color.Transparent,
                disabledUncheckedBorderColor = TraktTheme.colors.primaryButtonContent,
                disabledCheckedBoxColor = TraktTheme.colors.primaryButtonContent,
                disabledBorderColor = TraktTheme.colors.primaryButtonContent,
            ),
        )

        Text(
            text = text.uppercase(),
            color = TraktTheme.colors.primaryButtonContent,
            style = TraktTheme.typography.buttonSecondary,
            maxLines = 1,
            overflow = Ellipsis,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .weight(1F, false)
                .padding(vertical = 10.dp),
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun PreviewChecked() {
    TraktTheme {
        ListButton(
            text = "Favorites",
            checked = true,
            enabled = true,
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun PreviewUnchecked() {
    TraktTheme {
        ListButton(
            text = "Favorites",
            checked = false,
            enabled = false,
        )
    }
}
