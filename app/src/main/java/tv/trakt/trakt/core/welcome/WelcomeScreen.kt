package tv.trakt.trakt.core.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import tv.trakt.trakt.common.Config.WEB_ABOUT_US_URL
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.ui.theme.colors.Purple500
import tv.trakt.trakt.common.ui.theme.colors.Purple900
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun WelcomeScreen(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
) {
    val uriHandler = LocalUriHandler.current

    val backgroundColor1 = TraktTheme.colors.backgroundPrimary
    val backgroundColor2 = Purple900.copy(alpha = 0.75F)

    val backgroundGradient = remember {
        verticalGradient(
            colors = listOf(
                backgroundColor1,
                backgroundColor2,
            ),
        )
    }

    val contentPadding = PaddingValues(
        top = WindowInsets.statusBars.asPaddingValues()
            .calculateTopPadding()
            .plus(32.dp),
        bottom = WindowInsets.navigationBars.asPaddingValues()
            .calculateBottomPadding(),
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor1),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.75F)
                .background(backgroundGradient),
        )

        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxHeight()
                .padding(contentPadding),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = spacedBy(12.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_trakt),
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                )
                Icon(
                    painter = painterResource(R.drawable.ic_trakt_logo),
                    contentDescription = null,
                    tint = Color.White,
                )
            }

            Column(
                verticalArrangement = spacedBy(42.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 16.dp),
            ) {
                WelcomeItem(
                    title = "discover",
                    subtitle = "shows and movies you\'ll love and where\nyou can watch them right now.",
                    iconPainter = painterResource(R.drawable.ic_discover_on),
                )

                WelcomeItem(
                    title = "track",
                    subtitle = "every movie, episode and season you\'ve seen,\nall in one place.",
                    iconPainter = painterResource(R.drawable.ic_check_double),
                )

                WelcomeItem(
                    title = "share",
                    subtitle = "opinions, ratings and lists that others\nwill love to follow.",
                    iconPainter = painterResource(R.drawable.ic_share),
                )
            }

            val shape = RoundedCornerShape(24.dp)
            Column(
                verticalArrangement = spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 16.dp)
                    .shadow(4.dp, shape)
                    .clip(shape)
                    .background(Purple900)
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 10.dp,
                        bottom = 16.dp,
                    ),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = spacedBy(6.dp),
                ) {
                    Text(
                        text = "15",
                        style = TraktTheme.typography.heading1.copy(
                            fontSize = 48.sp,
                        ),
                        color = Color.White,
                        maxLines = 3,
                        modifier = Modifier.graphicsLayer {
                            translationY = 2.dp.toPx()
                        },
                    )

                    Column {
                        val font = TraktTheme.typography.paragraphSmaller.copy(fontSize = 12.sp)
                        Text(
                            text = "years of watching together",
                            style = font,
                            color = Color.White,
                            maxLines = 1,
                        )
                        Text(
                            text = "mil. shows & movies lovers",
                            style = font,
                            color = Color.White,
                            maxLines = 1,
                        )
                        Text(
                            text = "mil. titles tracked weekly",
                            style = font,
                            color = Color.White,
                            maxLines = 1,
                        )
                    }

                    Spacer(modifier = Modifier.weight(1F))

                    Box(
                        modifier = Modifier.onClick {
                            uriHandler.openUri(WEB_ABOUT_US_URL)
                        },
                    ) {
                        val users = remember {
                            listOf(
                                "https://walter-r2.trakt.tv/images/admins/000/000/001/avatars/thumb/2e6df69058.jpg",
                                "https://walter-r2.trakt.tv/images/admins/000/000/002/avatars/thumb/0f9ac9e4e6.jpg",
                                "https://walter-r2.trakt.tv/images/admins/000/410/291/avatars/thumb/348b7bf208.png",
                            )
                        }
                        users.forEachIndexed { index, model ->
                            AsyncImage(
                                model = model,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                error = painterResource(R.drawable.ic_person_placeholder),
                                modifier = Modifier
                                    .zIndex(10 - index.toFloat())
                                    .padding(start = (index * 20).dp)
                                    .size(42.dp)
                                    .border(2.dp, Red500, CircleShape)
                                    .clip(CircleShape),
                            )
                        }
                    }
                }

                PrimaryButton(
                    text = stringResource(R.string.button_text_join_trakt_free).uppercase(),
                    containerColor = Purple500,
                    contentColor = Color.White,
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun WelcomeItem(
    title: String,
    subtitle: String,
    iconPainter: Painter,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier,
    ) {
        Text(
            text = "  $title  ",
            style = TraktTheme.typography.heading1.copy(
                fontSize = 64.sp,
                letterSpacing = 6.sp,
            ),
            textAlign = TextAlign.Center,
            color = Color.White,
            maxLines = 3,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .blur(8.dp)
                .alpha(0.04F),
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = spacedBy(0.dp),
        ) {
            Icon(
                painter = iconPainter,
                contentDescription = null,
                tint = Purple500,
                modifier = Modifier.size(24.dp),
            )

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = spacedBy(2.dp),
                modifier = Modifier
                    .padding(top = 0.dp, bottom = 4.dp),
            ) {
                Text(
                    text = title,
                    style = TraktTheme.typography.heading1,
                    color = Color.White,
                    maxLines = 3,
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .graphicsLayer {
                            translationY = -10.dp.toPx()
                        }
                        .background(Purple500, CircleShape),
                )
            }
            Text(
                text = subtitle,
                style = TraktTheme.typography.paragraphSmall.copy(
                    fontSize = 14.sp,
                ),
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 3,
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            WelcomeScreen()
        }
    }
}
