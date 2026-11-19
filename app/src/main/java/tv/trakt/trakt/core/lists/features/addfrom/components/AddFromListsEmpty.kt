package tv.trakt.trakt.core.lists.features.addfrom.components

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.theme.TraktTheme

internal const val ADD_FROM_LISTS_EMPTY_HEIGHT_FRACTION = 0.85F

@Composable
internal fun AddFromListsEmpty(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.text_empty_list_add_from_lists),
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.paragraph,
            textAlign = TextAlign.Center,
        )

        PrimaryButton(
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
