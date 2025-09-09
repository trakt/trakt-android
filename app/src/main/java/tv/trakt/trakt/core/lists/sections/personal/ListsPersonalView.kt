package tv.trakt.trakt.core.lists.sections.personal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.helpers.preview.PreviewData
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.mediacards.skeletons.VerticalMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ListsPersonalView(
    list: CustomList,
    modifier: Modifier = Modifier,
//    viewModel: ListsWatchlistViewModel = koinViewModel(),
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
) {
//    val state by viewModel.state.collectAsStateWithLifecycle()

    ListsPersonalContent(
//        state = state,
        list = list,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
    )
}

@Composable
internal fun ListsPersonalContent(
//    state: ListsWatchlistState,
    list: CustomList,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(headerPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1F, fill = false)
                    .padding(end = 48.dp),
            ) {
                Text(
                    text = list.name,
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.heading5,
                )
                if (!list.description.isNullOrBlank()) {
                    Text(
                        text = list.description ?: "",
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.meta.copy(fontWeight = W400),
                        maxLines = 2,
                        overflow = Ellipsis,
                    )
                }
            }
//            if (!state.items.isNullOrEmpty() || state.loading != DONE) {
            Text(
                text = stringResource(R.string.button_text_view_all),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.buttonSecondary,
                modifier = Modifier,
            )
//            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ContentLoadingList(
            visible = true,
            contentPadding = contentPadding,
        )
    }
}

@Composable
private fun ContentLoadingList(
    visible: Boolean = true,
    contentPadding: PaddingValues,
) {
    LazyRow(
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (visible) 1F else 0F),
    ) {
        items(count = 6) {
            VerticalMediaSkeletonCard(chipRatio = 0.5F)
        }
    }
}

// Previews

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        ListsPersonalContent(
            list = PreviewData.customList1,
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview2() {
    TraktTheme {
        ListsPersonalContent(
            list = PreviewData.customList1,
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview3() {
    TraktTheme {
        ListsPersonalContent(
            list = PreviewData.customList1,
        )
    }
}
