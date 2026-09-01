package tv.trakt.trakt.core.home.sections.recommended.whythis

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.extensions.capitalize
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.core.home.sections.recommended.model.RecommendedItem
import tv.trakt.trakt.core.home.sections.recommended.model.RecommendedSource
import tv.trakt.trakt.core.home.sections.recommended.model.RecommendedSource.Companion.mergedSubgenres
import tv.trakt.trakt.core.home.sections.recommended.model.RecommendedSource.Type.Activity
import tv.trakt.trakt.core.home.sections.recommended.model.RecommendedSource.Type.Favorite
import tv.trakt.trakt.core.home.sections.recommended.model.RecommendedSource.Type.Subgenre
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.chips.InfoChip
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

private const val GRID_COLUMNS = 4

@Composable
internal fun WhyThisView(
    item: RecommendedItem,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = spacedBy(24.dp),
        modifier = modifier
            .verticalScroll(
                state = rememberScrollState(),
                overscrollEffect = null,
            ),
    ) {
        TraktHeader(
            title = item.title,
            subtitle = stringResource(R.string.drawer_title_recommendation_sources),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        )

        val watched = item.sources.filter { it.type == Activity }
        val loved = item.sources.filter { it.type == Favorite }
        val liked = item.sources.filter { it.type == Subgenre }

        if (watched.isNotEmpty()) {
            Column(
                verticalArrangement = spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            ) {
                SectionHeader(
                    icon = R.drawable.ic_check_double,
                    title = stringResource(R.string.list_title_recommendation_sources_activity),
                )
                SourcePosters(sources = watched)
            }
        }

        if (loved.isNotEmpty()) {
            Column(
                verticalArrangement = spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            ) {
                SectionHeader(
                    icon = R.drawable.ic_heart_off,
                    title = stringResource(R.string.list_title_recommendation_sources_favorite),
                )
                SourcePosters(sources = loved)
            }
        }

        if (liked.isNotEmpty()) {
            Column(
                verticalArrangement = spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                SectionHeader(
                    icon = R.drawable.ic_discover_off,
                    title = stringResource(R.string.list_title_recommendation_sources_subgenre),
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
                SubgenreGroup(
                    sources = liked.distinctBy {
                        it.movie?.ids?.trakt ?: it.show?.ids?.trakt
                    },
                )
            }
        }

        Spacer(modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun SectionHeader(
    icon: Int,
    title: String,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(8.dp),
        modifier = modifier,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
            modifier = Modifier.size(20.dp),
        )

        TraktHeader(
            title = title,
            modifier = Modifier.weight(1F),
        )
    }
}

@Composable
private fun SubgenreGroup(sources: List<RecommendedSource>) {
    Column(
        verticalArrangement = spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Subgenres(subgenres = sources.mergedSubgenres())
        SourcePosters(
            sources = sources,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
    }
}

@Composable
private fun Subgenres(subgenres: ImmutableList<RecommendedSource.Subgenre>) {
    LazyRow(
        horizontalArrangement = spacedBy(6.dp),
        contentPadding = PaddingValues(horizontal = 24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        items(subgenres.size) { index ->
            InfoChip(
                text = subgenres[index].name.capitalize(),
            )
        }
    }
}

@Composable
private fun SourcePosters(
    sources: List<RecommendedSource>,
    modifier: Modifier = Modifier,
) {
    val gridSpacing = TraktTheme.spacing.mainGridHorizontalSpace
    Column(
        verticalArrangement = spacedBy(gridSpacing),
        modifier = modifier.fillMaxWidth(),
    ) {
        sources.chunked(GRID_COLUMNS).forEach { row ->
            Row(
                horizontalArrangement = spacedBy(gridSpacing),
                modifier = Modifier.fillMaxWidth(),
            ) {
                row.forEach { source ->
                    VerticalMediaCard(
                        title = source.movie?.title ?: source.show?.title.orEmpty(),
                        imageUrl = (source.movie?.images ?: source.show?.images)?.getPosterUrl(),
                        more = false,
                        corner = 13.dp,
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = TraktTheme.colors.shadowSmall,
                            pressedElevation = TraktTheme.colors.shadowSmall,
                            disabledElevation = TraktTheme.colors.shadowSmall,
                        ),
                        modifier = Modifier.weight(1F),
                    )
                }

                repeat(GRID_COLUMNS - row.size) {
                    Spacer(modifier = Modifier.weight(1F))
                }
            }
        }
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
    heightDp = 1500,
)
@Composable
private fun Preview() {
    TraktTheme {
        WhyThisView(
            item = RecommendedItem.MovieItem(
                movie = PreviewData.movie1,
                sources = persistentListOf(
                    RecommendedSource(
                        type = Activity,
                        movie = PreviewData.movie1,
                    ),
                    RecommendedSource(
                        type = Favorite,
                        movie = PreviewData.movie2,
                    ),
                    RecommendedSource(
                        type = Subgenre,
                        movie = PreviewData.movie1,
                        subgenres = persistentListOf(
                            RecommendedSource.Subgenre(id = 1, name = "magic", slug = "magic"),
                            RecommendedSource.Subgenre(id = 2, name = "3d animation", slug = "3d-animation"),
                        ),
                    ),
                    RecommendedSource(
                        type = Subgenre,
                        movie = PreviewData.movie2,
                        subgenres = persistentListOf(
                            RecommendedSource.Subgenre(id = 1, name = "magic", slug = "magic"),
                            RecommendedSource.Subgenre(id = 2, name = "3d animation", slug = "3d-animation"),
                        ),
                    ),
                    RecommendedSource(
                        type = Subgenre,
                        movie = PreviewData.movie1,
                        subgenres = persistentListOf(
                            RecommendedSource.Subgenre(id = 3, name = "villain", slug = "villain"),
                        ),
                    ),
                ),
            ),
        )
    }
}
