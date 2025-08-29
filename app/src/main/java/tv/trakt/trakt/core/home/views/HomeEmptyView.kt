package tv.trakt.trakt.core.home.views

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import coil3.request.ImageRequest
import coil3.request.crossfade
import tv.trakt.trakt.common.ui.theme.colors.Purple500
import tv.trakt.trakt.common.ui.theme.colors.Shade900
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.theme.TraktTheme

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
internal fun HomeEmptyView(
    modifier: Modifier = Modifier,
    height: Dp? = null,
    text: String,
    icon: Int,
    backgroundImage: Int? = null,
    backgroundImageUrl: String? = null,
    buttonText: String,
    buttonIcon: Int = R.drawable.ic_search,
    onClick: () -> Unit = {},
) {
    BoxWithConstraints(
        modifier = modifier
            .clip(
                shape = RoundedCornerShape(12.dp),
            ),
    ) {
        backgroundImage?.let {
            Image(
                painter = painterResource(it),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .alpha(0.8F)
                    .matchParentSize(),
            )
        }

        backgroundImageUrl?.let {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(it)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .alpha(0.8F)
                    .matchParentSize(),
            )
        }

        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .heightIn(min = height ?: Dp.Unspecified)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Shade900,
                            Purple500.copy(alpha = 0.33F),
                        ),
                    ),
                )
                .padding(16.dp),
        ) {
            Row(
                horizontalArrangement = spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = text,
                    style = TraktTheme.typography.paragraphSmall
                        .copy(fontWeight = W400),
                    color = TraktTheme.colors.textPrimary,
                    modifier = Modifier.weight(1f),
                )

                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = TraktTheme.colors.textPrimary,
                    modifier = Modifier
                        .size(72.dp),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(
                text = buttonText,
                icon = painterResource(buttonIcon),
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .dropShadow(
                        shape = RoundedCornerShape(12.dp),
                        shadow = Shadow(
                            radius = 4.dp,
                            color = Color.Black,
                            spread = 2.dp,
                            alpha = 0.12f,
                        ),
                    ),
            )
        }
    }
}

@Preview(widthDp = 350)
@Composable
private fun Preview() {
    TraktTheme {
        HomeEmptyView(
            text = "Continue watching your favorite shows & pick up where you left off." +
                "\n\nWhich shows are you currently following?",
            icon = R.drawable.ic_empty_upnext,
            buttonText = "Browse Shows",
            backgroundImage = R.drawable.ic_splash_background_2,
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
                height = 200.dp,
                text = "Add movies to your watchlist and start building a list of movies you want to watch.",
                icon = R.drawable.ic_empty_watchlist,
                buttonText = "Browse Movies",
                backgroundImage = R.drawable.ic_splash_background,
            )
        }
    }
}
