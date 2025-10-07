package tv.trakt.trakt.core.home.views

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import tv.trakt.trakt.common.Config.WEB_ABOUT_US_URL
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_EMPTY_IMAGE_4
import tv.trakt.trakt.common.ui.theme.colors.Purple500
import tv.trakt.trakt.common.ui.theme.colors.Shade900
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.theme.TraktTheme

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
internal fun HomeEmptySocialView(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current

    val imageUrl = remember {
        Firebase.remoteConfig.getString(MOBILE_EMPTY_IMAGE_4).ifBlank { null }
    }

    BoxWithConstraints(
        modifier = modifier
            .clip(
                shape = RoundedCornerShape(12.dp),
            ),
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .alpha(0.8F)
                    .matchParentSize(),
            )
        } else {
            Image(
                painter = painterResource(R.drawable.ic_splash_background_2),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .alpha(0.8F)
                    .matchParentSize(),
            )
        }

        Column(
            verticalArrangement = spacedBy(24.dp),
            modifier = Modifier
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
                    text = stringResource(R.string.text_cta_activity_list),
                    style = TraktTheme.typography.paragraphSmall,
                    color = TraktTheme.colors.textPrimary,
                )
            }

            PrimaryButton(
                text = stringResource(R.string.button_text_meet_team),
                icon = painterResource(R.drawable.ic_trakt_icon),
                onClick = {
                    uriHandler.openUri(WEB_ABOUT_US_URL)
                },
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
        HomeEmptySocialView()
    }
}
