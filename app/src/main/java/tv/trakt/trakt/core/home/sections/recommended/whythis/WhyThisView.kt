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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.capitalize
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.recommended.RecommendedPerson
import tv.trakt.trakt.common.model.recommended.RecommendedSource
import tv.trakt.trakt.common.model.recommended.RecommendedSource.Companion.mergedSubgenres
import tv.trakt.trakt.common.model.recommended.RecommendedSource.Type.Activity
import tv.trakt.trakt.common.model.recommended.RecommendedSource.Type.Favorite
import tv.trakt.trakt.common.model.recommended.RecommendedSource.Type.Subgenre
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.chips.InfoChip
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun WhyThisView(
    title: String,
    state: WhyThisState,
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
            title = title,
            subtitle = stringResource(R.string.drawer_title_recommendation_sources),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        )

        if (state.loading.isLoading) {
            FilmProgressIndicator(
                size = 32.dp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 24.dp),
            )
        } else if (state.sources.isEmpty() && state.people.isEmpty()) {
            Text(
                text = stringResource(R.string.list_placeholder_empty),
                style = TraktTheme.typography.paragraphSmall,
                color = TraktTheme.colors.textSecondary,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
        }

        if (state.people.isNotEmpty()) {
            Column(
                verticalArrangement = spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            ) {
                SectionHeader(
                    icon = R.drawable.ic_person,
                    title = stringResource(R.string.list_title_recommendation_sources_people),
                )
                state.people.forEach {
                    WhyThisPersonView(reason = it)
                }
            }
        }

        val watched = state.sources.filter { it.type == Activity }
        val loved = state.sources.filter { it.type == Favorite }
        val liked = state.sources.filter { it.type == Subgenre }

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
    WhyThisPosterGrid(
        cells = sources.map {
            WhyThisGridCell.Poster(
                title = it.movie?.title ?: it.show?.title.orEmpty(),
                imageUrl = (it.movie?.images ?: it.show?.images)?.getPosterUrl(),
            )
        },
        modifier = modifier,
    )
}

@PreviewTraktLightDark
@Composable
private fun LoadingPreview() {
    TraktThemePreview {
        WhyThisView(
            title = PreviewData.movie1.title,
            state = WhyThisState(loading = Loading),
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun EmptyPreview() {
    TraktThemePreview {
        WhyThisView(
            title = PreviewData.movie1.title,
            state = WhyThisState(loading = Done),
        )
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
    TraktThemePreview {
        WhyThisView(
            title = PreviewData.movie1.title,
            state = WhyThisState(
                people = persistentListOf(
                    RecommendedPerson(
                        person = PreviewData.person1,
                        watchedRoles = persistentListOf(RecommendedPerson.Role.Directing),
                        watchedCount = 6,
                        titles = persistentListOf(
                            RecommendedPerson.Title(movie = PreviewData.movie1),
                            RecommendedPerson.Title(movie = PreviewData.movie2),
                        ),
                    ),
                ),
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
