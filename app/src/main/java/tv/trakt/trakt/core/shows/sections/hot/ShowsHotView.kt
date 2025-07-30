package tv.trakt.trakt.core.shows.sections.hot

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.R
import tv.trakt.trakt.core.shows.model.WatchersShow
import tv.trakt.trakt.tv.helpers.extensions.thousandsFormat
import tv.trakt.trakt.ui.components.InfoChip
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.components.mediacards.skeletons.VerticalMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ShowsHotView(
    modifier: Modifier = Modifier,
    viewModel: ShowsHotViewModel = koinViewModel(),
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ShowsHotContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
    )
}

@Composable
internal fun ShowsHotContent(
    state: ShowsHotState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(headerPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.header_hot_month),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading5,
            )
            Text(
                text = stringResource(R.string.view_all).uppercase(),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.buttonTertiary,
            )
        }

        Crossfade(
            targetState = state.items?.size,
            animationSpec = tween(250),
        ) { items ->
            when (items == null) {
                true -> {
                    ContentLoadingList(
                        contentPadding = contentPadding,
                    )
                }
                false -> {
                    ContentList(
                        listItems = { state.items ?: emptyList<WatchersShow>().toImmutableList() },
                        contentPadding = contentPadding,
                    )
                }
            }
        }
    }
}

@Composable
private fun ContentLoadingList(contentPadding: PaddingValues) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
    ) {
        items(count = 6) {
            VerticalMediaSkeletonCard()
        }
    }
}

@Composable
private fun ContentList(
    listItems: () -> ImmutableList<WatchersShow>,
    contentPadding: PaddingValues,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
    ) {
        items(
            items = listItems(),
            key = { it.show.ids.trakt.value },
        ) { item ->
            ContentListItem(
                item = item,
            )
        }
    }
}

@Composable
private fun ContentListItem(
    item: WatchersShow,
    onClick: () -> Unit = {},
) {
    VerticalMediaCard(
        title = item.show.title,
        imageUrl = item.show.images?.getPosterUrl(),
        onClick = onClick,
        chipContent = {
            InfoChip(
                text = stringResource(R.string.people_eager, item.watchers.thousandsFormat()),
            )
        },
    )
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        ShowsHotContent(
            state = ShowsHotState(),
        )
    }
}
