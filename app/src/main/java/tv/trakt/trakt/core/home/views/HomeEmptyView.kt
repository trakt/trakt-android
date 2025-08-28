package tv.trakt.trakt.core.home.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import tv.trakt.trakt.common.ui.theme.colors.Purple500
import tv.trakt.trakt.common.ui.theme.colors.Shade900
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun HomeEmptyView(
    modifier: Modifier = Modifier,
    text: String,
    icon: Int,
    buttonText: String,
    buttonIcon: Int = R.drawable.ic_search,
    imageUrl: String? = null,
) {
    Box(
        modifier = modifier
            .clip(
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        if (!imageUrl.isNullOrBlank()) {
//            AsyncImage(
//                model = ImageRequest.Builder(LocalContext.current)
//                    .data(imageUrl)
//                    .crossfade(true)
//                    .build(),
//                contentDescription = null,
//                contentScale = ContentScale.Crop,
//                modifier = Modifier
//                    .fillMaxHeight()
//            )
        }

        Column(
            verticalArrangement = spacedBy(16.dp),
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Shade900,
                            Purple500.copy(alpha = 0.8F),
                        ),
                    ),
                )
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = text,
                    style = TraktTheme.typography.paragraphSmall
                        .copy(fontWeight = W400),
                    color = TraktTheme.colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = TraktTheme.colors.textPrimary,
                    modifier = Modifier
                        .size(72.dp)
                )
            }

            PrimaryButton(
                text = buttonText,
                icon = painterResource(buttonIcon),
                modifier = Modifier
                    .fillMaxWidth()
                    .dropShadow(
                        shape = RoundedCornerShape(12.dp),
                        shadow = Shadow(
                            radius = 4.dp,
                            color = Color.Black,
                            spread = 2.dp,
                            alpha = 0.1f
                        )
                    )
            )
        }
    }
}

@Preview(widthDp = 350)
@Composable
private fun Preview() {
    TraktTheme {
        HomeEmptyView(
            text = "Continue watching your favorite shows & pick up where you left off.\n\nWhich shows are you currently following?",
            icon = R.drawable.ic_empty_upnext,
            buttonText = "Browse Shows",
        )
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(widthDp = 350)
@Composable
private fun Preview2() {
    val previewHandler = AsyncImagePreviewHandler {
        ColorImage(Color.Blue.toArgb())
    }
    TraktTheme {
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            HomeEmptyView(
                text = "Add movies to your watchlist and start building a list of movies you want to watch.",
                icon = R.drawable.ic_empty_watchlist,
                buttonText = "Browse Movies",
                imageUrl = "https://walter.trakt.tv/images/movies/000/003/003/posters/thumb/8f6b1e6a6d.jpg",
            )
        }
    }
}
