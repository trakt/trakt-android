import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import tv.trakt.trakt.tv.R
import tv.trakt.trakt.tv.common.model.ExternalRating
import tv.trakt.trakt.tv.helpers.extensions.thousandsFormat
import tv.trakt.trakt.tv.ui.theme.TraktTheme

@Composable
internal fun ExternalRatingsStrip(
    externalRating: ExternalRating?,
    modifier: Modifier = Modifier,
) {
    val grayFilter = remember {
        ColorFilter.colorMatrix(
            ColorMatrix().apply {
                setToSaturation(0F)
            },
        )
    }

    Row(
        horizontalArrangement = spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        // iMDB Rating
        val imdbRating = externalRating?.imdb?.rating ?: 0F
        Row(
            horizontalArrangement = spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_imdb_color),
                contentDescription = null,
                modifier = Modifier.height(14.dp),
                colorFilter = if (imdbRating > 0) null else grayFilter,
            )
            Text(
                text = if (imdbRating > 0) externalRating?.imdb?.ratingString ?: "-" else "-",
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.ratingLabel,
            )

            val imdbVotes = externalRating?.imdb?.votes ?: 0
            if (imdbVotes > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.Top),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        tint = TraktTheme.colors.textSecondary,
                        modifier = Modifier.size(12.5.dp),
                    )
                    Text(
                        text = imdbVotes.thousandsFormat(),
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.ratingLabel.copy(fontSize = 12.sp),
                    )
                }
            }
        }

        // Rotten Tomatoes Rating
        val rottenRating = externalRating?.rotten?.rating?.toInt() ?: 0
        Row(
            horizontalArrangement = spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(externalRating?.rotten?.ratingIcon ?: R.drawable.ic_rotten_tomato),
                contentDescription = null,
                modifier = Modifier.height(16.dp),
                colorFilter = if (rottenRating > 0) null else grayFilter,
            )
            Text(
                text = if (rottenRating > 0) "$rottenRating%" else "-",
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.ratingLabel,
            )
        }

        // Rotten Tomatoes Audience Rating
        val rottenRatingAud = externalRating?.rotten?.userRating ?: 0
        Row(
            horizontalArrangement = spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(
                    externalRating?.rotten?.userRatingIcon ?: R.drawable.ic_rotten_audience_spilled,
                ),
                contentDescription = null,
                modifier = Modifier.height(16.dp),
                colorFilter = if (rottenRatingAud > 0) null else grayFilter,
            )
            Text(
                text = if (rottenRatingAud > 0) "$rottenRatingAud%" else "-",
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.ratingLabel,
            )
        }
    }
}

@Preview(widthDp = 600)
@Composable
private fun ExternalRatingsStripPreview() {
    TraktTheme {
        ExternalRatingsStrip(
            externalRating = ExternalRating(
                tmdb = ExternalRating.TmdbRating(
                    rating = 8.5F,
                    votes = 123456,
                    link = "https://www.themoviedb.org/movie/123456",
                ),
                imdb = ExternalRating.ImdbRating(
                    rating = 7.9F,
                    votes = 1_267_356,
                    link = "https://www.imdb.com/title/tt1234567/",
                ),
                meta = ExternalRating.MetaRating(
                    rating = 85,
                    link = "https://www.metacritic.com/movie/some-movie",
                ),
                rotten = ExternalRating.RottenRating(
                    rating = 23F,
                    state = "Fresh",
                    userRating = 80,
                    userState = "spilled",
                    link = "https://www.rottentomatoes.com/m/some_movie",
                ),
            ),
        )
    }
}
