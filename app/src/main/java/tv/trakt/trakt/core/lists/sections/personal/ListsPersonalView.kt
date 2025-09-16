package tv.trakt.trakt.core.lists.sections.personal

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.core.lists.model.PersonalListItem
import tv.trakt.trakt.core.lists.sections.personal.views.ListsPersonalItemView
import tv.trakt.trakt.helpers.preview.PreviewData
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.mediacards.skeletons.VerticalMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ListsPersonalView(
    list: CustomList,
    viewModel: ListsPersonalViewModel,
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ListsPersonalContent(
        state = state,
        list = list,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onMoreClick = onMoreClick,
    )
}

@Composable
internal fun ListsPersonalContent(
    state: ListsPersonalState,
    list: CustomList,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onMoreClick: () -> Unit = {},
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
                verticalArrangement = spacedBy(1.dp),
                modifier = Modifier
                    .weight(1F, fill = false)
                    .fillMaxWidth(0.75F),
            ) {
                Row(
                    horizontalArrangement = spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TraktHeader(
                        title = list.name,
                        modifier = Modifier.weight(1F, fill = false),
                    )

                    Icon(
                        painter = painterResource(R.drawable.ic_more_vertical),
                        contentDescription = "Genres",
                        tint = TraktTheme.colors.textPrimary,
                        modifier = Modifier
                            .onClick(onMoreClick)
                            .size(14.dp),
                    )
                }
                if (!list.description.isNullOrBlank()) {
                    Text(
                        text = list.description ?: "",
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.meta.copy(
                            fontWeight = W400,
                            lineHeight = 1.em,
                        ),
                        maxLines = 2,
                        overflow = Ellipsis,
                    )
                }
            }

            if (!state.items.isNullOrEmpty()) {
                Text(
                    text = stringResource(R.string.button_text_view_all),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.buttonSecondary,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Crossfade(
            targetState = state.loading,
            animationSpec = tween(200),
        ) { loading ->
            when (loading) {
                IDLE, LOADING -> {
                    ContentLoadingList(
                        visible = loading.isLoading,
                        contentPadding = contentPadding,
                    )
                }
                DONE -> {
                    when {
                        state.error != null -> {
                            Text(
                                text =
                                    "${stringResource(R.string.error_text_unexpected_error_short)}\n\n${state.error}",
                                color = TraktTheme.colors.textSecondary,
                                style = TraktTheme.typography.meta,
                                maxLines = 10,
                                modifier = Modifier.padding(contentPadding),
                            )
                        }
                        state.items?.isEmpty() == true -> {
                            ContentEmptyList(
                                contentPadding = contentPadding,
                            )
                        }
                        else -> {
                            ContentList(
                                listItems = (state.items ?: emptyList()).toImmutableList(),
                                contentPadding = contentPadding,
                            )
                        }
                    }
                }
            }
        }
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

@Composable
private fun ContentEmptyList(contentPadding: PaddingValues) {
    LazyRow(
        userScrollEnabled = false,
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
        modifier = Modifier.fillMaxWidth(),
    ) {
        items(count = 5) {
            VerticalMediaSkeletonCard(
                chipRatio = 0.5F,
                shimmer = false,
                containerColor = TraktTheme.colors.skeletonContainer,
            )
        }
    }
}

@Composable
private fun ContentList(
    listItems: ImmutableList<PersonalListItem>,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues,
) {
    val currentList = remember { mutableIntStateOf(listItems.hashCode()) }

    LaunchedEffect(listItems) {
        val hashCode = listItems.hashCode()
        if (currentList.intValue != hashCode) {
            currentList.intValue = hashCode
            listState.animateScrollToItem(0)
        }
    }

    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
    ) {
        items(
            items = listItems,
            key = { it.key },
        ) { item ->
            ListsPersonalItemView(
                item = item,
                showMediaIcon = true,
                modifier = Modifier.animateItem(
                    fadeInSpec = null,
                    fadeOutSpec = null,
                ),
            )
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
            state = ListsPersonalState(loading = IDLE),
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
            state = ListsPersonalState(loading = LOADING),
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
            state = ListsPersonalState(loading = DONE),
        )
    }
}
