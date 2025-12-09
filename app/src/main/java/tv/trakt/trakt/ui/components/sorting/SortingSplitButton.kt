@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package tv.trakt.trakt.ui.components.sorting

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.model.sorting.SortOrder
import tv.trakt.trakt.common.model.sorting.SortOrder.ASCENDING
import tv.trakt.trakt.common.model.sorting.SortOrder.DESCENDING
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SortingSplitButton(
    text: String,
    order: SortOrder,
    modifier: Modifier = Modifier,
    height: Dp = 32.dp,
    enabled: Boolean = true,
    onLeadingClick: () -> Unit = {},
    onTrailingClick: () -> Unit = {},
) {
    SplitButtonLayout(
        spacing = 3.dp,
        leadingButton = {
            SplitButtonDefaults.LeadingButton(
                enabled = enabled,
                contentPadding = PaddingValues(
                    start = 12.dp,
                    end = 10.dp,
                ),
                colors = buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = TraktTheme.colors.textPrimary,
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = TraktTheme.colors.chipContainer,
                ),
                onClick = onLeadingClick,
            ) {
                Text(
                    text = text,
                    style = TraktTheme.typography.buttonTertiary,
                    color = TraktTheme.colors.textPrimary,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                )
            }
        },
        trailingButton = {
            SplitButtonDefaults.TrailingButton(
                enabled = enabled,
                contentPadding = PaddingValues(
                    end = 3.dp,
                ),
                colors = buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = TraktTheme.colors.textPrimary,
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = TraktTheme.colors.chipContainer,
                ),
                onClick = onTrailingClick,
                modifier = Modifier.widthIn(34.dp),
            ) {
                Icon(
                    painter = painterResource(
                        when (order) {
                            ASCENDING -> R.drawable.ic_sort_asc
                            DESCENDING -> R.drawable.ic_sort_desc
                        },
                    ),
                    contentDescription = null,
                    tint = TraktTheme.colors.textPrimary,
                    modifier = Modifier
                        .size(13.dp),
                )
            }
        },
        modifier = modifier
            .height(height),
    )
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        SortingSplitButton(
            text = "Release Date",
            order = ASCENDING,
        )
    }
}
