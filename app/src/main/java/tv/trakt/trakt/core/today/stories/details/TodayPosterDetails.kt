package tv.trakt.trakt.core.today.stories.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.MediaGenre
import tv.trakt.trakt.common.model.Sentiments
import tv.trakt.trakt.common.model.Sentiments.Overall
import tv.trakt.trakt.core.summary.ui.DetailsRatings
import tv.trakt.trakt.core.today.model.TodayMedia
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private const val MAX_GENRES = 3
private const val OVERVIEW_LINES = 4
private const val HIGHLIGHT_LINES = 4
private const val ASPECTS_LIMIT = 4
private const val HEADER_RATIO = 2F

private val AspectIconSize = 14.dp
private val VerdictShape = RoundedCornerShape(50)

@Composable
internal fun TodayPosterDetails(
    media: TodayMedia,
    viewModel: TodayPosterDetailsViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    TodayPosterDetailsContent(
        media = media,
        state = state,
        modifier = modifier,
    )
}

@Composable
private fun TodayPosterDetailsContent(
    media: TodayMedia,
    state: TodayPosterDetailsState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(TraktTheme.colors.commentContainer)
            .verticalScroll(rememberScrollState()),
    ) {
        TodayDetailsHeader(media = media)

        Column(
            verticalArrangement = spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            DetailsRatings(
                traktRatings = media.rating.ratingPercent.takeIf { it > 0 },
                externalRatings = state.ratings,
                chevron = false,
                malEnabled = MediaGenre.Anime in media.genres,
                animated = false,
            )

            media.overview?.let {
                Text(
                    text = it,
                    style = TraktTheme.typography.paragraphSmaller,
                    color = TraktTheme.colors.textPrimary,
                    maxLines = OVERVIEW_LINES,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            state.sentiment?.let {
                TodaySentiment(
                    sentiment = it,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun TodayDetailsHeader(media: TodayMedia) {
    val cardBackground = TraktTheme.colors.commentContainer

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(HEADER_RATIO),
    ) {
        AsyncImage(
            model = media.images?.getFanartUrl(Images.Size.MEDIUM),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.3F to Color.Transparent,
                        1F to cardBackground,
                    ),
                ),
        )
        Column(
            verticalArrangement = spacedBy(2.dp),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 16.dp, vertical = 4.dp),
        ) {
            Text(
                text = media.title,
                style = TraktTheme.typography.heading5,
                color = TraktTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = mediaMeta(media),
                style = TraktTheme.typography.cardSubtitle,
                color = TraktTheme.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun TodaySentiment(
    sentiment: Sentiments,
    modifier: Modifier = Modifier,
) {
    val pros = when (sentiment.overall) {
        Overall.Positive -> sentiment.pros.take(ASPECTS_LIMIT)
        Overall.Mixed -> sentiment.pros.take(ASPECTS_LIMIT / 2)
        Overall.Negative -> emptyList()
    }
    val cons = when (sentiment.overall) {
        Overall.Positive -> emptyList()
        Overall.Mixed -> sentiment.cons.take(ASPECTS_LIMIT / 2)
        Overall.Negative -> sentiment.cons.take(ASPECTS_LIMIT)
    }

    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.header_community_sentiment).uppercase(),
                style = TraktTheme.typography.meta,
                color = TraktTheme.colors.accentPalette.shade300,
                maxLines = 1,
                modifier = Modifier.weight(1F),
            )
            TodaySentimentVerdict(overall = sentiment.overall)
        }

        if (sentiment.highlight.isNotBlank()) {
            Text(
                text = sentiment.highlight,
                style = TraktTheme.typography.paragraphSmaller,
                color = TraktTheme.colors.textPrimary,
                maxLines = HIGHLIGHT_LINES,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(vertical = 6.dp),
            )
        }

        TodaySentimentAspects(
            aspects = pros,
            iconRes = R.drawable.ic_thumb_up2_fill,
            color = TraktTheme.colors.sentimentsAccent,
        )

        TodaySentimentAspects(
            aspects = cons,
            iconRes = R.drawable.ic_thumb_down2_fill,
            color = TraktTheme.colors.sentimentsBadAccent,
        )
    }
}

@Composable
private fun TodaySentimentVerdict(overall: Overall) {
    val color = overall.color()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(4.dp),
        modifier = Modifier
            .background(color.copy(alpha = 0.16F), VerdictShape)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Icon(
            painter = painterResource(overall.displayIconRes),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(AspectIconSize),
        )
        Text(
            text = stringResource(overall.displayTextRes).uppercase(),
            style = TraktTheme.typography.meta,
            color = color,
            maxLines = 1,
        )
    }
}

@Composable
private fun TodaySentimentAspects(
    aspects: List<Sentiments.Theme>,
    iconRes: Int,
    color: Color,
) {
    if (aspects.isEmpty()) return

    Row(
        horizontalArrangement = spacedBy(10.dp),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = color,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(AspectIconSize),
        )
        Column(
            verticalArrangement = spacedBy(3.dp),
        ) {
            aspects.forEach {
                Text(
                    text = it.value.replaceFirstChar(Char::titlecase),
                    style = TraktTheme.typography.paragraphSmaller,
                    color = color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun Overall.color(): Color {
    return when (this) {
        Overall.Positive, Overall.Mixed -> TraktTheme.colors.sentimentsAccent
        Overall.Negative -> TraktTheme.colors.sentimentsBadAccent
    }
}

@Composable
private fun mediaMeta(media: TodayMedia): String {
    val genres = media.genres
        .take(MAX_GENRES)
        .map { stringResource(it.displayStringRes) }
        .joinToString(", ")

    return listOfNotNull(
        media.year?.toString(),
        genres.ifBlank { null },
    ).joinToString(" • ")
}

private val previewSentiment = Sentiments(
    overall = Overall.Mixed,
    highlight = "Viewers praise the cast and the final episodes, but many find the middle of the season slow.",
    pros = persistentListOf(
        Sentiments.Theme("strong performances", 0.9F),
        Sentiments.Theme("satisfying finale", 0.8F),
    ),
    cons = persistentListOf(
        Sentiments.Theme("slow pacing", 0.7F),
        Sentiments.Theme("predictable twists", 0.6F),
    ),
)

private val previewRatings = ExternalRating(
    trakt = null,
    imdb = ExternalRating.ImdbRating(rating = 8.1F, votes = 1200, link = null),
    tmdb = null,
    meta = ExternalRating.MetaRating(rating = 76, link = null),
    rotten = ExternalRating.RottenRating(
        rating = 91F,
        state = "certified-fresh",
        userRating = 88,
        userState = "upright",
        link = null,
    ),
    mal = null,
    letterboxd = null,
)

private val previewShow = TodayMedia.ShowMedia(
    PreviewData.show1.copy(
        title = "The Long Night of the Northern Lights",
        genres = persistentListOf(MediaGenre.Drama, MediaGenre.Mystery),
    ),
)

private val previewAnime = TodayMedia.MovieMedia(
    PreviewData.movie1.copy(
        genres = persistentListOf(MediaGenre.Anime, MediaGenre.Action),
    ),
)

@Composable
private fun PreviewCard(
    state: TodayPosterDetailsState,
    media: TodayMedia = previewShow,
) {
    TraktTheme {
        TodayPosterDetailsContent(
            media = media,
            state = state,
            modifier = Modifier
                .padding(16.dp)
                .width(350.dp)
                .aspectRatio(2F / 3F)
                .clip(RoundedCornerShape(16.dp)),
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    PreviewCard(
        state = TodayPosterDetailsState(
            ratings = previewRatings,
            sentiment = previewSentiment,
        ),
    )
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun LoadingPreview() {
    PreviewCard(
        state = TodayPosterDetailsState(loading = LoadingState.Loading),
    )
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun PositivePreview() {
    PreviewCard(
        state = TodayPosterDetailsState(
            ratings = previewRatings,
            sentiment = previewSentiment.copy(
                overall = Overall.Positive,
                pros = persistentListOf(
                    Sentiments.Theme("strong performances", 0.9F),
                    Sentiments.Theme("satisfying finale", 0.8F),
                    Sentiments.Theme("beautiful cinematography", 0.7F),
                    Sentiments.Theme("memorable score", 0.6F),
                ),
            ),
        ),
    )
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun NegativePreview() {
    PreviewCard(
        state = TodayPosterDetailsState(
            ratings = previewRatings,
            sentiment = previewSentiment.copy(overall = Overall.Negative),
        ),
    )
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun NoSentimentPreview() {
    PreviewCard(
        state = TodayPosterDetailsState(
            ratings = previewRatings,
            loading = LoadingState.Done,
        ),
    )
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun AnimeMoviePreview() {
    PreviewCard(
        media = previewAnime,
        state = TodayPosterDetailsState(
            ratings = previewRatings.copy(
                mal = ExternalRating.MalRating(rating = 8.4F, votes = 5400, link = null),
            ),
            sentiment = previewSentiment,
        ),
    )
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun SparsePreview() {
    PreviewCard(
        media = TodayMedia.ShowMedia(
            PreviewData.show1.copy(
                overview = null,
                images = null,
                year = null,
            ),
        ),
        state = TodayPosterDetailsState(
            ratings = previewRatings.copy(
                imdb = null,
                meta = null,
                rotten = null,
            ),
            sentiment = previewSentiment.copy(highlight = ""),
        ),
    )
}
