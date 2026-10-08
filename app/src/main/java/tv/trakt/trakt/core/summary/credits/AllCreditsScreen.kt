package tv.trakt.trakt.core.summary.credits

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.CastPerson
import tv.trakt.trakt.common.model.CrewPerson
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.core.summary.credits.model.CreditsMode
import tv.trakt.trakt.core.summary.credits.model.MediaCredits
import tv.trakt.trakt.core.summary.people.model.PersonCreditsRole
import tv.trakt.trakt.core.summary.ui.views.CastPersonListItem
import tv.trakt.trakt.core.summary.ui.views.CrewPersonListItem
import tv.trakt.trakt.core.summary.ui.views.matchesQuery
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.EmptyListCard
import tv.trakt.trakt.ui.components.EmptyVerticalPanelHeight
import tv.trakt.trakt.ui.components.InputField
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.chips.FilterChip
import tv.trakt.trakt.ui.components.chips.FilterChipGroup
import tv.trakt.trakt.ui.components.chips.FilterChipSkeleton
import tv.trakt.trakt.ui.components.mediacards.skeletons.PanelMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AllCreditsScreen(
    viewModel: AllCreditsViewModel,
    onPersonClick: (Person, PersonCreditsRole?) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AllCreditsContent(
        state = state,
        onModeClick = viewModel::setMode,
        onPersonClick = onPersonClick,
        onBackClick = onNavigateBack,
    )
}

@Composable
private fun AllCreditsContent(
    state: AllCreditsState,
    modifier: Modifier = Modifier,
    onModeClick: (CreditsMode) -> Unit = {},
    onPersonClick: (Person, PersonCreditsRole?) -> Unit = { _, _ -> },
    onBackClick: () -> Unit = {},
) {
    val searchState = rememberTextFieldState()
    val listState = rememberLazyListState()

    val listScrollConnection = rememberSaveable(saver = SimpleScrollConnection.Saver) {
        SimpleScrollConnection()
    }

    val contentPadding = PaddingValues(
        start = TraktTheme.spacing.mainPageHorizontalSpace,
        end = TraktTheme.spacing.mainPageHorizontalSpace,
        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
        bottom = WindowInsets.navigationBars.asPaddingValues()
            .calculateBottomPadding()
            .plus(TraktTheme.size.navigationBarHeight)
            .plus(TraktTheme.spacing.mainPageBottomSpace),
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .nestedScroll(listScrollConnection),
    ) {
        ScrollableBackdropImage(
            imageUrl = state.backgroundUrl,
            translation = listScrollConnection.resultOffset,
        )

        LazyColumn(
            state = listState,
            verticalArrangement = spacedBy(14.dp),
            contentPadding = contentPadding,
            overscrollEffect = null,
        ) {
            val credits = state.credits
            item(key = "header") {
                Column(
                    verticalArrangement = spacedBy(4.dp),
                ) {
                    TitleBar(
                        subtitle = state.mediaTitle,
                        modifier = Modifier.onClick(onClick = onBackClick),
                    )

                    when {
                        state.loading != Done -> {
                            CreditsFiltersSkeleton()
                        }

                        credits != null && credits.modes.isNotEmpty() -> {
                            CreditsFilters(
                                credits = credits,
                                mode = state.mode,
                                searchState = searchState,
                                onModeClick = onModeClick,
                            )
                        }
                    }
                }
            }

            when {
                state.loading != Done -> {
                    items(count = 6) {
                        PanelMediaSkeletonCard(
                            imageWidth = TraktTheme.size.verticalSmallMediaCardSize,
                        )
                    }
                }

                state.error != null -> {
                    item(key = "error") {
                        ErrorView(error = state.error)
                    }
                }

                credits == null || credits.modes.isEmpty() -> {
                    item(key = "empty") {
                        EmptyView()
                    }
                }

                else -> {
                    creditsList(
                        credits = credits,
                        mode = state.mode,
                        query = searchState.text.toString().trim(),
                        onPersonClick = onPersonClick,
                    )
                }
            }
        }
    }
}

private fun LazyListScope.creditsList(
    credits: MediaCredits,
    mode: CreditsMode,
    query: String,
    onPersonClick: (Person, PersonCreditsRole?) -> Unit,
) {
    when (mode) {
        CreditsMode.Main -> castList(
            cast = credits.main.filter { it.matchesQuery(query) },
            mode = mode,
            onPersonClick = onPersonClick,
        )
        CreditsMode.Supporting -> castList(
            cast = credits.supporting.filter { it.matchesQuery(query) },
            mode = mode,
            onPersonClick = onPersonClick,
        )
        CreditsMode.Crew -> crewList(
            crew = credits.crew.filter { it.matchesQuery(query) },
            mode = mode,
            onPersonClick = onPersonClick,
        )
    }
}

private fun LazyListScope.castList(
    cast: List<CastPerson>,
    mode: CreditsMode,
    onPersonClick: (Person, PersonCreditsRole?) -> Unit,
) {
    if (cast.isEmpty()) {
        item(key = "empty") { EmptyView() }
        return
    }

    items(
        items = cast,
        key = { "${mode}_${it.person.ids.trakt.value}" },
    ) { item ->
        CastPersonListItem(
            person = item,
            onClick = { onPersonClick(it.person, PersonCreditsRole.Acting) },
            modifier = Modifier.animateItem(
                fadeInSpec = null,
                fadeOutSpec = null,
            ),
        )
    }
}

private fun LazyListScope.crewList(
    crew: List<CrewPerson>,
    mode: CreditsMode,
    onPersonClick: (Person, PersonCreditsRole?) -> Unit,
) {
    if (crew.isEmpty()) {
        item(key = "empty") { EmptyView() }
        return
    }

    items(
        items = crew,
        key = { "${mode}_${it.person.ids.trakt.value}" },
    ) { item ->
        CrewPersonListItem(
            person = item,
            onClick = { onPersonClick(it.person, null) },
            modifier = Modifier.animateItem(
                fadeInSpec = null,
                fadeOutSpec = null,
            ),
        )
    }
}

@Composable
private fun TitleBar(
    subtitle: String?,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(TraktTheme.size.titleBarHeight)
            .graphicsLayer { translationX = -2.dp.toPx() },
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_back_arrow),
            tint = TraktTheme.colors.textPrimary,
            contentDescription = null,
        )
        TraktHeader(
            title = stringResource(R.string.drawer_title_people),
            subtitle = subtitle,
        )
    }
}

@Composable
private fun CreditsFilters(
    credits: MediaCredits,
    mode: CreditsMode,
    searchState: TextFieldState,
    onModeClick: (CreditsMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = spacedBy(14.dp),
        modifier = modifier,
    ) {
        FilterChipGroup(
            paddingVertical = PaddingValues(bottom = 1.dp),
        ) {
            for (option in credits.modes) {
                FilterChip(
                    selected = mode == option,
                    text = stringResource(
                        when {
                            option == CreditsMode.Main && !credits.isSplit -> R.string.drawer_meta_info_cast
                            else -> option.displayRes
                        },
                    ),
                    height = 32.dp,
                    leadingContent = {
                        Icon(
                            painter = painterResource(option.iconRes),
                            contentDescription = null,
                            tint = TraktTheme.colors.textPrimaryOnAccent,
                            modifier = Modifier.size(FilterChipDefaults.IconSize),
                        )
                    },
                    onClick = { onModeClick(option) },
                )
            }
        }

        InputField(
            state = searchState,
            border = 1.dp,
            containerColor = Color.Transparent,
            icon = painterResource(R.drawable.ic_search_off),
            placeholder = stringResource(R.string.input_placeholder_search_credit_members),
            endSlot = {
                if (searchState.text.isNotBlank()) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = null,
                        tint = TraktTheme.colors.textSecondary,
                        modifier = Modifier
                            .size(18.dp)
                            .onClick { searchState.clearText() },
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun CreditsFiltersSkeleton(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransition")
    val shimmerTransition by infiniteTransition
        .animateColor(
            initialValue = TraktTheme.colors.skeletonContainer,
            targetValue = TraktTheme.colors.skeletonShimmer,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "shimmerTransition",
        )

    Column(
        verticalArrangement = spacedBy(14.dp),
        modifier = modifier,
    ) {
        FilterChipGroup(
            paddingVertical = PaddingValues(bottom = 1.dp),
        ) {
            repeat(3) {
                FilterChipSkeleton(height = 32.dp)
            }
        }

        // Matches the default InputField height and corner radius.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(
                    color = shimmerTransition,
                    shape = RoundedCornerShape(16.dp),
                ),
        )
    }
}

@Composable
private fun ErrorView(
    error: Exception,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "${stringResource(R.string.error_text_unexpected_error_short)}\n\n$error",
        color = TraktTheme.colors.textSecondary,
        style = TraktTheme.typography.meta,
        maxLines = 10,
        modifier = modifier,
    )
}

@Composable
private fun EmptyView(modifier: Modifier = Modifier) {
    EmptyListCard(
        modifier = modifier.height(EmptyVerticalPanelHeight),
    )
}

private val previewCredits = MediaCredits(
    main = persistentListOf(
        CastPerson(
            person = PreviewData.person1,
            characters = listOf("Jackson Lamb"),
            episodesCount = 6,
        ),
    ),
    supporting = persistentListOf(
        CastPerson(
            person = PreviewData.person2,
            characters = listOf("River Cartwright"),
            episodesCount = 2,
        ),
    ),
    crew = persistentListOf(
        CrewPerson(
            person = PreviewData.person1,
            jobs = persistentListOf("Director", "Writer"),
            episodesCount = 3,
        ),
    ),
)

@OptIn(ExperimentalCoilApi::class)
@Composable
private fun PreviewContainer(state: AllCreditsState) {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            Box {
                AllCreditsContent(state = state)
            }
        }
    }
}

@Preview(device = "id:pixel_5")
@Composable
private fun PreviewMain() {
    PreviewContainer(
        state = AllCreditsState(
            mediaTitle = "Slow Horses",
            credits = previewCredits,
            mode = CreditsMode.Main,
            loading = Done,
        ),
    )
}

@Preview(device = "id:pixel_5")
@Composable
private fun PreviewCrew() {
    PreviewContainer(
        state = AllCreditsState(
            mediaTitle = "Slow Horses",
            credits = previewCredits,
            mode = CreditsMode.Crew,
            loading = Done,
        ),
    )
}

@Preview(device = "id:pixel_5")
@Composable
private fun PreviewNotSplit() {
    PreviewContainer(
        state = AllCreditsState(
            mediaTitle = "Tinker Tailor Soldier Spy",
            credits = previewCredits.copy(supporting = persistentListOf()),
            mode = CreditsMode.Main,
            loading = Done,
        ),
    )
}

@Preview(device = "id:pixel_5")
@Composable
private fun PreviewLoading() {
    PreviewContainer(
        state = AllCreditsState(
            mediaTitle = "Slow Horses",
            loading = Loading,
        ),
    )
}

@Preview(device = "id:pixel_5")
@Composable
private fun PreviewError() {
    PreviewContainer(
        state = AllCreditsState(
            mediaTitle = "Slow Horses",
            loading = Done,
            error = IllegalStateException("Preview error"),
        ),
    )
}

@Preview(device = "id:pixel_5")
@Composable
private fun PreviewEmpty() {
    PreviewContainer(
        state = AllCreditsState(
            mediaTitle = "Slow Horses",
            credits = MediaCredits(),
            loading = Done,
        ),
    )
}
