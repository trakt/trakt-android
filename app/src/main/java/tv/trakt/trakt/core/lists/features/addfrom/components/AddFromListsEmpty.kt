package tv.trakt.trakt.core.lists.features.addfrom.components

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.TertiaryButton
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AddFromListsEmpty(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = spacedBy(16.dp),
        horizontalAlignment = Alignment.Start,
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.text_empty_list_add_from_lists),
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.heading6,
        )

        TertiaryButton(
            text = stringResource(R.string.button_text_add_from_lists),
            icon = painterResource(R.drawable.ic_plus),
            onClick = onClick,
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        AddFromListsEmpty(onClick = {})
    }
}
