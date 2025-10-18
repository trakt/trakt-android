@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.summary.people

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tv.trakt.trakt.common.Config.WEB_V3_BASE_URL
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.summary.ui.DetailsBackground
import tv.trakt.trakt.core.summary.ui.DetailsHeader
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun PersonDetailsScreen(
    modifier: Modifier = Modifier,
    viewModel: PersonDetailsViewModel,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    PersonDetailsContent(
        state = state,
        modifier = modifier,
        onNavigateToShow = onNavigateToShow,
        onNavigateToMovie = onNavigateToMovie,
        onBackClick = onNavigateBack,
    )
}

@Composable
internal fun PersonDetailsContent(
    state: PersonDetailsState,
    modifier: Modifier = Modifier,
    onNavigateToShow: ((TraktId) -> Unit)? = null,
    onNavigateToMovie: ((TraktId) -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val previewMode = LocalInspectionMode.current

    val listState = rememberLazyListState(
        cacheWindow = LazyLayoutCacheWindow(
            aheadFraction = 1F,
            behindFraction = 1F,
        ),
    )

    val listScrollConnection = rememberSaveable(
        saver = SimpleScrollConnection.Saver,
    ) {
        SimpleScrollConnection()
    }

    Box(
        contentAlignment = TopCenter,
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .nestedScroll(listScrollConnection),
    ) {
        val contentPadding = PaddingValues(
            top = WindowInsets.statusBars.asPaddingValues()
                .calculateTopPadding()
                .plus(16.dp),
            bottom = WindowInsets.navigationBars.asPaddingValues()
                .calculateBottomPadding()
                .plus(TraktTheme.size.navigationBarHeight * 2),
        )

        state.personDetails?.let { person ->
            DetailsBackground(
                imageUrl = state.personBackdropUrl,
                translation = listScrollConnection.resultOffset,
                color = TraktTheme.colors.backgroundPrimary,
            )

            LazyColumn(
                state = listState,
                verticalArrangement = spacedBy(0.dp),
                contentPadding = contentPadding,
                overscrollEffect = null,
            ) {
                item {
                    DetailsHeader(
                        person = person,
                        loading = state.loading.isLoading,
                        onShareClick = { sharePerson(person, context) },
                        onBackClick = onBackClick ?: {},
                    )
                }

                item {
                    AnimatedVisibility(
                        visible = person.biography != null,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        DetailsOverview(
                            overview = person.biography,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = TraktTheme.spacing.mainPageHorizontalSpace),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailsOverview(
    modifier: Modifier = Modifier,
    overview: String? = null,
) {
    var isCollapsed by remember { mutableStateOf(true) }
    Text(
        text = overview ?: stringResource(R.string.text_overview_placeholder),
        style = TraktTheme.typography.paragraphSmall,
        color = TraktTheme.colors.textSecondary,
        maxLines = if (isCollapsed) 6 else Int.MAX_VALUE,
        textAlign = TextAlign.Start,
        overflow = Ellipsis,
        modifier = modifier
            .onClick {
                isCollapsed = !isCollapsed
            },
    )
}

private fun sharePerson(
    person: Person,
    context: Context,
) {
    val shareText = "${WEB_V3_BASE_URL}people/${person.ids.slug.value}"
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
    }

    context.startActivity(Intent.createChooser(intent, person.name))
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        PersonDetailsContent(
            state = PersonDetailsState(
                personDetails = PreviewData.person1,
            ),
        )
    }
}
