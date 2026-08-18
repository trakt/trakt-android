package tv.trakt.trakt.core.ratings.allratings

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.DeviceSheetPreview
import tv.trakt.trakt.common.helpers.extensions.capitalize
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.rememberThousandsFormat
import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.Ids
import tv.trakt.trakt.common.model.Rating
import tv.trakt.trakt.common.model.Season
import tv.trakt.trakt.common.model.toSlugId
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.ratings.allratings.ui.QualityOverTimeCard
import tv.trakt.trakt.core.ratings.allratings.ui.QualityOverTimeSkeletonCard
import tv.trakt.trakt.core.ratings.allratings.ui.TraktRatingCard
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.theme.TraktTheme

private const val TILES_PER_ROW = 3

@Composable
internal fun AllRatingsView(
    viewModel: AllRatingsViewModel,
    ratings: ExternalRating,
    modifier: Modifier = Modifier,
    malEnabled: Boolean = false,
    onImdbClick: () -> Unit = {},
    onRottenClick: (link: String) -> Unit = {},
    onMalClick: (link: String) -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AllRatingsContent(
        ratings = ratings,
        seasons = state.seasons,
        seasonsLoading = state.loading.isLoading,
        malEnabled = malEnabled,
        onImdbClick = onImdbClick,
        onRottenClick = onRottenClick,
        onMalClick = onMalClick,
        modifier = modifier,
    )
}

@Composable
private fun AllRatingsContent(
    ratings: ExternalRating,
    seasons: ImmutableList<Season>?,
    modifier: Modifier = Modifier,
    seasonsLoading: Boolean,
    malEnabled: Boolean,
    onImdbClick: () -> Unit = {},
    onRottenClick: (link: String) -> Unit = {},
    onMalClick: (link: String) -> Unit = {},
) {
    val tiles = buildRatingTiles(
        ratings = ratings,
        malEnabled = malEnabled,
        onImdbClick = onImdbClick,
        onRottenClick = onRottenClick,
        onMalClick = onMalClick,
    )

    Column(
        modifier = modifier
            .verticalScroll(
                state = rememberScrollState(),
                overscrollEffect = null,
            ),
    ) {
        TraktHeader(
            title = stringResource(R.string.page_title_ratings),
        )

        ratings.trakt?.takeIf { it.votes > 0 }?.let { trakt ->
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
            ) {
                Text(
                    text = stringResource(R.string.header_ratings_trakt),
                    style = TraktTheme.typography.paragraphSmaller.copy(fontWeight = FontWeight.W600),
                    color = TraktTheme.colors.textSecondary,
                )

                TraktRatingCard(
                    rating = trakt,
                    modifier = Modifier
                        .fillMaxWidth(),
                )
            }
        }

        val seasonRatings = seasons?.takeIf { it.size >= 2 }
        if (seasonsLoading || seasonRatings != null) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
            ) {
                Text(
                    text = stringResource(R.string.header_ratings_quality_over_time),
                    style = TraktTheme.typography.paragraphSmaller.copy(fontWeight = FontWeight.W600),
                    color = TraktTheme.colors.textSecondary,
                )

                Crossfade(
                    targetState = seasonRatings,
                    label = "QualityOverTimeCard",
                ) { seasonRatings ->
                    when {
                        !seasonRatings.isNullOrEmpty() -> QualityOverTimeCard(
                            seasons = seasonRatings,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        else -> QualityOverTimeSkeletonCard(
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }

        if (tiles.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
                    .padding(bottom = TraktTheme.spacing.shadowClipSpace),
            ) {
                Text(
                    text = stringResource(R.string.header_ratings_official),
                    style = TraktTheme.typography.paragraphSmaller.copy(fontWeight = FontWeight.W600),
                    color = TraktTheme.colors.textSecondary,
                )
                tiles
                    .chunked(TILES_PER_ROW)
                    .forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            row.forEach { tile ->
                                RatingTile(
                                    tile = tile,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            repeat(TILES_PER_ROW - row.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
            }
        }
    }
}

private fun buildRatingTiles(
    ratings: ExternalRating,
    malEnabled: Boolean,
    onImdbClick: () -> Unit,
    onRottenClick: (link: String) -> Unit,
    onMalClick: (link: String) -> Unit,
): ImmutableList<RatingTileData> =
    buildList {
        ratings.imdb?.takeIf { it.rating > 0 }?.let { imdb ->
            add(
                RatingTileData(
                    iconRes = R.drawable.ic_imdb_color,
                    value = imdb.ratingString,
                    votes = imdb.votes,
                    onClick = onImdbClick,
                ),
            )
        }

        if (malEnabled) {
            ratings.mal?.takeIf { it.rating > 0 }?.let { mal ->
                add(
                    RatingTileData(
                        iconRes = R.drawable.ic_mal,
                        value = mal.ratingString,
                        votes = mal.votes,
                        roundedIcon = true,
                        onClick = mal.link?.let { link -> { onMalClick(link) } },
                    ),
                )
            }
        }

        ratings.rotten?.let { rotten ->
            val onClick = rotten.link?.let { link -> { onRottenClick(link) } }

            if (rotten.rating > 0) {
                add(
                    RatingTileData(
                        iconRes = rotten.ratingIcon,
                        value = "${rotten.rating.toInt()}%",
                        label = rotten.state?.capitalize(),
                        onClick = onClick,
                    ),
                )
            }

            rotten.userRating?.takeIf { it > 0 }?.let { userRating ->
                add(
                    RatingTileData(
                        iconRes = rotten.userRatingIcon,
                        value = "$userRating%",
                        label = rotten.userState?.capitalize(),
                        onClick = onClick,
                    ),
                )
            }
        }

        ratings.tmdb?.takeIf { it.rating > 0 }?.let { tmdb ->
            add(
                RatingTileData(
                    iconRes = R.drawable.ic_tmdb,
                    value = tmdb.ratingString,
                    votes = tmdb.votes,
                ),
            )
        }
    }.toImmutableList()

@Composable
private fun RatingTile(
    tile: RatingTileData,
    modifier: Modifier = Modifier,
) {
    val tileShape = RoundedCornerShape(16.dp)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp),
        modifier = modifier
            .shadow(
                elevation = TraktTheme.colors.shadowDynamicDefault,
                shape = tileShape,
            )
            .background(
                color = TraktTheme.colors.dialogOnContainer,
                shape = tileShape,
            )
            .onClick(enabled = tile.onClick != null) {
                tile.onClick?.invoke()
            }
            .padding(horizontal = 12.dp, vertical = 16.dp),
    ) {
        Image(
            painter = painterResource(tile.iconRes),
            contentDescription = null,
            modifier = when {
                tile.roundedIcon -> {
                    Modifier
                        .height(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                }
                else -> {
                    Modifier.height(24.dp)
                }
            },
        )

        Text(
            text = tile.value,
            style = TraktTheme.typography.heading3.copy(
                fontSize = 22.sp,
                letterSpacing = 0.02.sp,
            ),
            color = TraktTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        when {
            tile.votes != null -> Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_person_double),
                    tint = TraktTheme.colors.textSecondary,
                    contentDescription = null,
                    modifier = Modifier.size(11.dp),
                )
                Text(
                    text = rememberThousandsFormat(tile.votes),
                    style = TraktTheme.typography.meta,
                    color = TraktTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            tile.label != null -> Text(
                text = tile.label,
                style = TraktTheme.typography.meta,
                color = TraktTheme.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private data class RatingTileData(
    val iconRes: Int,
    val value: String,
    val votes: Int? = null,
    val label: String? = null,
    val roundedIcon: Boolean = false,
    val onClick: (() -> Unit)? = null,
)

@DeviceSheetPreview
@Composable
private fun Preview() {
    TraktTheme {
        AllRatingsContent(
            malEnabled = true,
            seasonsLoading = false,
            seasons = listOf(85, 88, 84, 90, 96, 33, 44, 55)
                .mapIndexed { index, percent ->
                    Season(
                        ids = Ids(
                            trakt = index.toTraktId(),
                            slug = "".toSlugId(),
                        ),
                        number = index + 1,
                        rating = Rating(
                            rating = percent / 10F,
                            votes = 1000,
                        ),
                        episodeCount = 10,
                        images = null,
                        overview = null,
                        firstAired = null,
                        updatedAt = null,
                    )
                }
                .toImmutableList(),
            ratings = ExternalRating(
                imdb = ExternalRating.ImdbRating(
                    rating = 8.3F,
                    votes = 56300,
                    link = "https://www.imdb.com/title/tt1234567/",
                ),
                meta = ExternalRating.MetaRating(
                    rating = 75,
                    link = "https://www.metacritic.com/movie/example",
                ),
                rotten = ExternalRating.RottenRating(
                    rating = 100F,
                    state = "fresh",
                    userRating = 92,
                    userState = "upright",
                    link = "https://www.rottentomatoes.com/m/example",
                ),
                tmdb = ExternalRating.TmdbRating(
                    rating = 8.5F,
                    votes = 910,
                    link = "https://www.themoviedb.org/movie/12345",
                ),
                mal = ExternalRating.MalRating(
                    rating = 8.4F,
                    votes = 596800,
                    link = "https://myanimelist.net/anime/12345",
                ),
                trakt = ExternalRating.TraktRating(
                    rating = 8.5F,
                    votes = 5100,
                    distribution = persistentMapOf(
                        1 to 40F,
                        2 to 60F,
                        3 to 80F,
                        4 to 120F,
                        5 to 200F,
                        6 to 300F,
                        7 to 500F,
                        8 to 900F,
                        9 to 1400F,
                        10 to 1500F,
                    ),
                ),
            ),
        )
    }
}
