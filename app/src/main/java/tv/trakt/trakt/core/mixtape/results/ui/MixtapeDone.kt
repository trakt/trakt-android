package tv.trakt.trakt.core.mixtape.results.ui

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.core.mixtape.results.MixtapeResultsState
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun MixtapeDone(
    state: MixtapeResultsState,
    modifier: Modifier = Modifier,
    onFindMoreClick: () -> Unit = {},
    onChangePicksClick: () -> Unit = {},
    onCloseClick: () -> Unit = {},
) {
    val kept = state.rounds.flatten()

    Column(
        verticalArrangement = spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding().plus(12.dp),
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding().plus(20.dp),
                start = TraktTheme.spacing.mainPageHorizontalSpace,
                end = TraktTheme.spacing.mainPageHorizontalSpace,
            ),
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.header_mixtape_done),
                style = TraktTheme.typography.heading4,
                color = TraktTheme.colors.textPrimary,
                modifier = Modifier.weight(1F),
            )
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier
                    .size(24.dp)
                    .onClick(onClick = onCloseClick),
            )
        }

        Text(
            text = stringResource(
                R.string.text_mixtape_based_on,
                state.pickTitles.values.joinToString(", "),
            ),
            style = TraktTheme.typography.paragraphSmall,
            color = TraktTheme.colors.textSecondary,
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(TraktTheme.size.mainGridColumns),
            horizontalArrangement = spacedBy(TraktTheme.spacing.mainGridHorizontalSpace),
            verticalArrangement = spacedBy(TraktTheme.spacing.mainGridVerticalSpace),
            overscrollEffect = null,
            modifier = Modifier.weight(1F),
        ) {
            items(
                count = kept.size,
                key = { kept[it].key },
            ) { index ->
                val item = kept[index]
                VerticalMediaCard(
                    title = item.title,
                    imageUrl = item.images?.getPosterUrl(),
                    more = false,
                    enabled = false,
                )
            }
        }

        Text(
            text = stringResource(R.string.text_mixtape_saved_as_you_go),
            style = TraktTheme.typography.paragraphSmall,
            color = TraktTheme.colors.textSecondary,
        )

        if (state.slots.isNotEmpty()) {
            PrimaryButton(
                text = stringResource(R.string.button_text_mixtape_show_more),
                onClick = onFindMoreClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Row(horizontalArrangement = spacedBy(12.dp)) {
            GhostButton(
                text = stringResource(R.string.button_text_mixtape_change_picks),
                onClick = onChangePicksClick,
                modifier = Modifier.weight(1F),
            )
            GhostButton(
                text = stringResource(R.string.button_text_mixtape_finish),
                onClick = onCloseClick,
                modifier = Modifier.weight(1F),
            )
        }
    }
}
