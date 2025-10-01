package tv.trakt.trakt.core.summary.ui

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.ui.theme.colors.Purple300
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun DetailsHeader(
    title: String,
    genres: List<String>,
    year: Int?,
    images: Images?,
    status: String?,
    trailer: Uri?,
    accentColor: Color?,
    onShareClick: () -> Unit,
    onTrailerClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier
            .fillMaxWidth(),
    ) {
        Box(
            contentAlignment = TopCenter,
            modifier = Modifier.fillMaxWidth(),
        ) {
            val posterSpace = 64.dp
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .width(posterSpace)
                    .padding(top = 8.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_back_arrow),
                    tint = TraktTheme.colors.textPrimary,
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .onClick(onClick = onBackClick),
                )
            }

            DetailsPoster(
                imageUrl = images?.getPosterUrl(Images.Size.MEDIUM),
                color = accentColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = posterSpace),
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = spacedBy(24.dp),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .width(posterSpace)
                    .padding(top = 8.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_share),
                    tint = TraktTheme.colors.textPrimary,
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .onClick(onClick = onShareClick),
                )

                Icon(
                    painter = painterResource(R.drawable.ic_trailer),
                    tint = TraktTheme.colors.textPrimary,
                    contentDescription = null,
                    modifier = Modifier
                        .alpha(if (trailer != null) 1F else 0.25F)
                        .size(21.dp)
                        .onClick(
                            enabled = trailer != null,
                            onClick = onTrailerClick,
                        ),
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 24.dp,
                    start = TraktTheme.spacing.mainPageHorizontalSpace,
                    end = TraktTheme.spacing.mainPageHorizontalSpace,
                ),
        ) {
            val genresText = remember(genres) {
                genres.take(3).joinToString(", ") { genre ->
                    genre.replaceFirstChar {
                        it.uppercaseChar()
                    }
                }
            }

            Text(
                text = title,
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading2,
                maxLines = 1,
                overflow = Ellipsis,
                autoSize = TextAutoSize.StepBased(
                    maxFontSize = TraktTheme.typography.heading2.fontSize,
                    minFontSize = 16.sp,
                    stepSize = 2.sp,
                ),
            )

            Text(
                text = "$year  •  $genresText",
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.paragraphSmaller,
                maxLines = 1,
                overflow = Ellipsis,
                modifier = Modifier
                    .padding(top = 5.dp),
            )

            status?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = spacedBy(4.dp),
                    modifier = Modifier
                        .padding(top = 2.dp),
                ) {
                    Text(
                        text = it.uppercase(),
                        color = when (it.lowercase()) {
                            "canceled", "ended" -> Red500
                            else -> Purple300
                        },
                        style = TraktTheme.typography.meta,
                        modifier = Modifier
                            .padding(top = 1.dp),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun DetailsHeaderPreview() {
    TraktTheme {
        DetailsHeader(
            title = "Movie Title",
            genres = listOf("Action", "Adventure", "Sci-Fi"),
            year = 2023,
            images = null,
            status = "Released",
            trailer = null,
            accentColor = null,
            onShareClick = {},
            onTrailerClick = {},
            onBackClick = {},
        )
    }
}
