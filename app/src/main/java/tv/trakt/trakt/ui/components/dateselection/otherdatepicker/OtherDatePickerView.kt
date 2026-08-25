package tv.trakt.trakt.ui.components.dateselection.otherdatepicker

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.DeviceSheetPreview
import tv.trakt.trakt.common.helpers.extensions.longDateTimeFormat
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.helpers.extensions.toLocalDay
import tv.trakt.trakt.common.helpers.extensions.toLocalTime
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Images.Size.THUMB
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem.EpisodeItem
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem.MovieItem
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.components.dateselection.RangeSelectableDates
import tv.trakt.trakt.ui.components.dateselection.TraktDatePicker
import tv.trakt.trakt.ui.components.dateselection.TraktTimePicker
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset.UTC

private const val NEXT_PAGE_OFFSET = 5

private val ButtonOverlayClearance = 96.dp

@Composable
internal fun OtherDatePickerView(
    viewModel: OtherDatePickerViewModel,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onConfirm: (Instant) -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    OtherDatePickerContent(
        state = state,
        title = title,
        subtitle = subtitle,
        contentPadding = contentPadding,
        onLoadMore = viewModel::loadMoreData,
        onConfirm = onConfirm,
        modifier = modifier,
    )
}

@Composable
private fun OtherDatePickerContent(
    state: OtherDatePickerState,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onLoadMore: () -> Unit = {},
    onSlotClick: (before: HomeActivityItem?, after: HomeActivityItem?) -> Unit = { _, _ -> },
    onConfirm: (Instant) -> Unit = {},
) {
    var selectedDate by remember { mutableStateOf<Instant?>(null) }
    var selectedSlotId by remember { mutableStateOf<Long?>(null) }

    // Bounds of the selected slot: manual edits stay between the neighbour items.
    var newerLimit by remember { mutableStateOf<Instant?>(null) }
    var olderLimit by remember { mutableStateOf<Instant?>(null) }

    var editingDate by remember { mutableStateOf(false) }
    var pendingDate by remember { mutableStateOf<Instant?>(null) }

    var wobbleTrigger by remember { mutableStateOf(0) }
    val wobbleOffset = remember { Animatable(0f) }
    val wobbleAmplitude = with(LocalDensity.current) { 6.dp.toPx() }

    LaunchedEffect(wobbleTrigger) {
        if (wobbleTrigger == 0) return@LaunchedEffect
        wobbleOffset.animateTo(
            targetValue = 0f,
            animationSpec = keyframes {
                durationMillis = 450
                0f at 0
                wobbleAmplitude at 75
                -wobbleAmplitude at 150
                wobbleAmplitude * 0.6F at 225
                -wobbleAmplitude * 0.6F at 300
                wobbleAmplitude * 0.3F at 375
                0f at 450
            },
        )
    }

    val gradientColor = TraktTheme.colors.dialogContainer
    val buttonGradient = remember {
        verticalGradient(
            colors = listOf(
                Color.Transparent,
                gradientColor,
                gradientColor,
            ),
        )
    }

    Box(
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = spacedBy(0.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                horizontalArrangement = spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding),
            ) {
                TraktHeader(
                    title = title,
                    subtitle = subtitle,
                    modifier = Modifier.weight(1F),
                )
            }

            val fieldHeight = 64.dp
            Crossfade(
                targetState = selectedDate != null,
                animationSpec = tween(200),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(fieldHeight),
            ) { selected ->
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(fieldHeight)
                        .clip(RoundedCornerShape(12.dp))
                        .padding(contentPadding)
                        .padding(top = 16.dp),
                ) {
                    if (selected) {
                        SelectedDateField(
                            date = selectedDate,
                            onClick = { editingDate = true },
                            modifier = Modifier.height(fieldHeight),
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.text_pick_history_slot, title),
                            color = TraktTheme.colors.textPrimary,
                            style = TraktTheme.typography.paragraphSmall,
                            maxLines = 1,
                            overflow = Ellipsis,
                            modifier = Modifier
                                .graphicsLayer {
                                    translationX = wobbleOffset.value
                                },
                        )
                    }
                }
            }

            HistoryList(
                state = state,
                selectedSlotId = selectedSlotId,
                onLoadMore = onLoadMore,
                onSlotClick = { before, after ->
                    if (after != null) {
                        selectedSlotId = after.id
                        newerLimit = before?.activityAt ?: nowUtcInstant()
                        olderLimit = after.activityAt
                        selectedDate = midpointDate(
                            newer = before?.activityAt ?: nowUtcInstant(),
                            older = after.activityAt,
                        )
                    }
                    onSlotClick(before, after)
                },
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(contentPadding)
                    .padding(top = 2.dp),
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(buttonGradient)
                .padding(contentPadding)
                .padding(
                    top = 48.dp,
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding() + 16.dp,
                ),
        ) {
            Box {
                PrimaryButton(
                    text = stringResource(R.string.button_text_mark_as_watched),
                    enabled = selectedDate != null,
                    onClick = { selectedDate?.let(onConfirm) },
                    modifier = Modifier.fillMaxWidth(),
                )

                // Disabled button swallows no clicks; catch taps to nudge the hint label.
                if (selectedDate == null) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .onClick { wobbleTrigger++ },
                    )
                }
            }
        }
    }

    val older = olderLimit
    val newer = newerLimit
    if (editingDate && older != null && newer != null) {
        TraktDatePicker(
            active = pendingDate == null,
            initialDate = (selectedDate ?: newer).toLocalDay(),
            selectableDates = RangeSelectableDates(
                minDay = older.toLocalDay(),
                maxDay = newer.toLocalDay(),
            ),
            onDateSelected = { pendingDate = it },
            onDismiss = {
                editingDate = false
                pendingDate = null
            },
        )

        TraktTimePicker(
            active = pendingDate != null,
            selectedDate = pendingDate,
            initialTime = selectedDate?.toLocalTime(),
            onDateTimeSelected = { dateTimeUtc ->
                val localDateTime = LocalDateTime.ofInstant(dateTimeUtc, UTC)
                val instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant()
                selectedDate = instant.coerceIn(older, newer)
                editingDate = false
                pendingDate = null
            },
            onDismiss = { pendingDate = null },
        )
    }
}

@Composable
private fun SelectedDateField(
    date: Instant?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = TraktTheme.colors.chipContainer,
                shape = RoundedCornerShape(12.dp),
            )
            .onClick { onClick() }
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = date?.toLocal()?.format(longDateTimeFormat())
                ?: stringResource(R.string.date_time_label_watched),
            color = when (date) {
                null -> TraktTheme.colors.textSecondary
                else -> TraktTheme.colors.textPrimary
            },
            style = TraktTheme.typography.paragraph,
            modifier = Modifier.weight(1F),
        )

        if (date != null) {
            Icon(
                painter = painterResource(R.drawable.ic_edit),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier.size(19.dp),
            )
        }
    }
}

@Composable
private fun HistoryList(
    state: OtherDatePickerState,
    modifier: Modifier = Modifier,
    selectedSlotId: Long? = null,
    onLoadMore: () -> Unit = {},
    onSlotClick: (before: HomeActivityItem?, after: HomeActivityItem?) -> Unit = { _, _ -> },
) {
    val listState = rememberLazyListState()
    val items = state.items

    val isScrolledToBottom by remember(items?.size) {
        derivedStateOf {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            info.totalItemsCount > 0 && lastVisible >= info.totalItemsCount - NEXT_PAGE_OFFSET
        }
    }

    LaunchedEffect(isScrolledToBottom) {
        if (isScrolledToBottom) {
            onLoadMore()
        }
    }

    if (state.loading.isLoading) {
        OtherDatePickerSkeleton(
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
        )
        return
    }

    LazyColumn(
        state = listState,
        verticalArrangement = spacedBy(0.dp),
        overscrollEffect = null,
        contentPadding = PaddingValues(
            top = 14.dp,
            bottom = WindowInsets.navigationBars.asPaddingValues()
                .calculateBottomPadding() + ButtonOverlayClearance,
        ),
        modifier = modifier,
    ) {
        itemsIndexed(
            items = items ?: persistentListOf(),
            key = { _, item -> item.id },
        ) { index, item ->
            Column {
                InsertSlot(
                    selected = selectedSlotId == item.id,
                    onClick = {
                        onSlotClick(
                            items?.getOrNull(index - 1),
                            item,
                        )
                    },
                )
                HistoryItemRow(
                    item = item,
                )
            }
        }

        if (state.loadingMore.isLoading) {
            item(key = "loading-more") {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                ) {
                    FilmProgressIndicator(
                        size = 24.dp,
                    )
                }
            }
        }
    }
}

@Composable
private fun InsertSlot(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: () -> Unit = {},
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxWidth(),
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (selected) 2.dp else 1.dp)
                .background(
                    when {
                        selected -> TraktTheme.colors.accent
                        else -> TraktTheme.colors.chipContainer
                    },
                ),
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(28.dp)
                .background(
                    color = when {
                        selected -> TraktTheme.colors.accent
                        else -> TraktTheme.colors.dialogContainer
                    },
                    shape = RoundedCornerShape(8.dp),
                )
                .border(
                    width = 1.dp,
                    color = when {
                        selected -> TraktTheme.colors.accent
                        else -> TraktTheme.colors.chipContainer
                    },
                    shape = RoundedCornerShape(8.dp),
                )
                .onClick { onClick() },
        ) {
            Icon(
                painter = painterResource(
                    when {
                        selected -> R.drawable.ic_check
                        else -> R.drawable.ic_plus
                    },
                ),
                contentDescription = null,
                tint = when {
                    selected -> TraktTheme.colors.textPrimaryOnAccent
                    else -> TraktTheme.colors.textPrimary
                },
                modifier = Modifier.size(
                    when {
                        selected -> 14.dp
                        else -> 16.dp
                    },
                ),
            )
        }
    }
}

@Composable
private fun HistoryItemRow(
    item: HomeActivityItem,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
    ) {
        VerticalMediaCard(
            title = "",
            width = 40.dp,
            corner = 8.dp,
            more = false,
            imageUrl = item.images?.getPosterUrl(THUMB),
        )

        Column(
            verticalArrangement = spacedBy(1.dp),
        ) {
            Text(
                text = when (item) {
                    is MovieItem -> {
                        item.movie.title
                    }
                    is EpisodeItem -> {
                        val episodeTitle = stringResource(
                            R.string.episode_footer_season_episode,
                            item.episode.season,
                            item.episode.number,
                        )
                        "$episodeTitle - ${item.show.title}"
                    }
                },
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.cardTitle.copy(fontSize = 16.sp),
                maxLines = 1,
                overflow = Ellipsis,
            )

            Text(
                text = item.activityAt.toLocal().format(longDateTimeFormat()),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.cardSubtitle.copy(fontSize = 12.sp),
                maxLines = 1,
                overflow = Ellipsis,
            )
        }
    }
}

private fun midpointDate(
    newer: Instant,
    older: Instant,
): Instant {
    return Instant.ofEpochSecond((newer.epochSecond + older.epochSecond) / 2)
}

@DeviceSheetPreview
@Composable
private fun Preview() {
    TraktThemeLightDark {
        OtherDatePickerContent(
            title = "Dark",
            subtitle = "Season 3 • Episode 2 - Some Title",
            state = OtherDatePickerState(
                items = persistentListOf(
                    EpisodeItem(
                        id = 1,
                        user = null,
                        userRating = null,
                        activity = "watch",
                        activityAt = nowUtcInstant(),
                        episode = PreviewData.episode1,
                        show = PreviewData.show1,
                    ),
                    MovieItem(
                        id = 2,
                        user = null,
                        userRating = null,
                        activity = "watch",
                        activityAt = nowUtcInstant(),
                        movie = PreviewData.movie1,
                    ),
                ),
            ),
        )
    }
}

@DeviceSheetPreview
@Composable
private fun PreviewLoading() {
    TraktTheme {
        OtherDatePickerContent(
            title = "The Survivors",
            subtitle = "Dark • Season 3 • Episode 2",
            state = OtherDatePickerState(
                loading = LoadingState.Loading,
            ),
        )
    }
}
