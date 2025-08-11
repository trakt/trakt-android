package tv.trakt.trakt.app.core.search.views

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.tv.material3.Text
import tv.trakt.trakt.app.common.ui.PositionFocusLazyRow
import tv.trakt.trakt.app.common.ui.mediacards.HorizontalMediaSkeletonCard
import tv.trakt.trakt.app.helpers.extensions.requestSafeFocus
import tv.trakt.trakt.app.ui.theme.TraktTheme

@Composable
internal fun SearchLoadingView(
    header: String,
    focusRequesters: Map<String, FocusRequester>,
) {
    val contentPadding = PaddingValues(
        start = TraktTheme.spacing.mainContentStartSpace,
        end = TraktTheme.spacing.mainContentEndSpace,
    )

    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = Modifier
            .focusProperties {
                onEnter = {
                    focusRequesters.getValue("shows").requestSafeFocus()
                }
            }
            .focusGroup()
            .focusable(),
    ) {
        Text(
            text = header,
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading5,
            modifier = Modifier.padding(contentPadding),
        )

        ContentList(
            contentPadding = contentPadding,
        )
    }
}

@Composable
private fun ContentList(contentPadding: PaddingValues) {
    PositionFocusLazyRow(
        contentPadding = contentPadding,
    ) {
        items(count = 10) {
            HorizontalMediaSkeletonCard()
        }
    }
}
