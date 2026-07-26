package tv.trakt.trakt.app.core.lists.filters

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.tv.material3.Text
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.resources.R

@Composable
internal fun TvListEmptyState(
    filter: GlobalFilter,
    modifier: Modifier = Modifier,
    @StringRes defaultMessageRes: Int = R.string.list_placeholder_empty,
) {
    Text(
        text = stringResource(
            when (filter.isActive) {
                true -> R.string.list_placeholder_no_filter_results
                false -> defaultMessageRes
            },
        ),
        color = TraktTheme.colors.textSecondary,
        style = TraktTheme.typography.paragraphLarge,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth(),
    )
}
