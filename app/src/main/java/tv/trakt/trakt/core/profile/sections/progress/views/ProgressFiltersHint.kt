package tv.trakt.trakt.core.profile.sections.progress.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private val HintShape = RoundedCornerShape(8.dp)

/**
 * The one-off nudge shown the first time the progress filters enter reorder mode. Reuses the copy
 * the list reorder screen already carries, so it needs no new translations.
 */
@Composable
internal fun ProgressFiltersHint(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(6.dp),
        modifier = modifier
            .background(TraktTheme.colors.tooltipContainer, HintShape)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_drag),
            contentDescription = null,
            tint = TraktTheme.colors.tooltipContent,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = stringResource(R.string.drawer_title_reorder_list),
            style = TraktTheme.typography.meta,
            color = TraktTheme.colors.tooltipContent,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        ProgressFiltersHint()
    }
}
