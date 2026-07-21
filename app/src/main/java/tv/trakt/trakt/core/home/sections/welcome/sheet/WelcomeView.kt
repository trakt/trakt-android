package tv.trakt.trakt.core.home.sections.welcome.sheet

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import tv.trakt.trakt.common.Config
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_BACKGROUND_VIP_IMAGE_URL
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.ui.theme.colors.Purple500
import tv.trakt.trakt.common.ui.theme.colors.Purple600
import tv.trakt.trakt.common.ui.theme.colors.Shade200
import tv.trakt.trakt.common.ui.theme.colors.Shade910
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.PARALLAX_RATIO
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.components.chips.InfoChip
import tv.trakt.trakt.ui.components.vip.VipChip
import tv.trakt.trakt.ui.theme.DefaultCardShape
import tv.trakt.trakt.ui.theme.HorizontalImageAspectRatio
import tv.trakt.trakt.ui.theme.TraktTheme

private const val IMPORT_SOURCE_IMDB = "IMDb"
private const val IMPORT_SOURCE_LETTERBOXD = "Letterboxd"

private const val TV_TIME_LIBERATOR_URL: String =
    "https://chromewebstore.google.com/detail/tv-time-liberator-extensi/pohobkcjhigehafgnhehkanhjakajhpm?pli=1"

private const val TV_TIME_GDPR_URL: String =
    "https://gdpr.tvtime.com/gdpr/self-service"

@Composable
internal fun WelcomeView(
    modifier: Modifier = Modifier,
    name: String? = null,
    isVip: Boolean = false,
    onVipClick: () -> Unit = {},
    onStartExploringClick: () -> Unit = {},
) {
    val preview = LocalInspectionMode.current
    val uriHandler = LocalUriHandler.current

    val listScrollConnection = rememberSaveable(saver = SimpleScrollConnection.Saver) {
        SimpleScrollConnection()
    }

    val backgroundColor1 = TraktTheme.colors.dialogContainer
    val backgroundGradient = remember {
        verticalGradient(
            colors = listOf(
                Color.Transparent,
                backgroundColor1,
            ),
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .nestedScroll(listScrollConnection),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .graphicsLayer {
                    translationY = listScrollConnection.resultOffset * PARALLAX_RATIO
                },
        ) {
            ScrollableBackdropImage(
                translation = 0F,
                imageAlpha = 0.65F,
                imageUrl = remember {
                    if (preview) {
                        null
                    } else {
                        Firebase.remoteConfig.getString(MOBILE_BACKGROUND_VIP_IMAGE_URL)
                            .ifBlank { null }
                    }
                },
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(HorizontalImageAspectRatio)
                    .background(backgroundGradient),
            )
        }

        Column(
            verticalArrangement = spacedBy(0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = spacedBy(8.dp, CenterHorizontally),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 100.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_trakt_icon),
                    contentDescription = null,
                    modifier = Modifier
                        .align(CenterVertically)
                        .size(28.dp),
                )
                Text(
                    text = when {
                        !name.isNullOrBlank() -> stringResource(R.string.welcome_greeting, name)
                        else -> stringResource(R.string.welcome_greeting_generic)
                    },
                    style = TraktTheme.typography.heading3,
                    color = TraktTheme.colors.textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier,
                )
            }

            Text(
                text = stringResource(R.string.welcome_intro),
                style = TraktTheme.typography.paragraphSmall,
                color = TraktTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
            )

            PrimaryButton(
                text = stringResource(R.string.welcome_get_started),
                icon = painterResource(R.drawable.ic_discover_on),
                onClick = onStartExploringClick,
                modifier = Modifier
                    .align(CenterHorizontally)
                    .padding(top = 30.dp),
            )

            Text(
                text = stringResource(R.string.welcome_import_heading),
                style = TraktTheme.typography.heading4,
                color = TraktTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp),
            )

            Text(
                text = stringResource(R.string.welcome_import_description),
                style = TraktTheme.typography.paragraphSmall,
                color = TraktTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            )

            ImportTvTimeCard(
                onImportClick = {
                    uriHandler.openUri(Config.webDataImportUrl("tvtime"))
                },
                onLiberatorClick = {
                    uriHandler.openUri(TV_TIME_LIBERATOR_URL)
                },
                onGdprClick = {
                    uriHandler.openUri(TV_TIME_GDPR_URL)
                },
                modifier = Modifier.padding(top = 30.dp),
            )

            ImportSourceCard(
                title = IMPORT_SOURCE_IMDB,
                description = stringResource(R.string.welcome_import_imdb_description),
                onImportClick = {
                    uriHandler.openUri(Config.webDataImportUrl("imdb"))
                },
                modifier = Modifier.padding(top = 16.dp),
            )

            ImportSourceCard(
                title = IMPORT_SOURCE_LETTERBOXD,
                description = stringResource(R.string.welcome_import_letterboxd_description),
                onImportClick = {
                    uriHandler.openUri(Config.webDataImportUrl("letterboxd"))
                },
                modifier = Modifier
                    .padding(top = 16.dp),
            )

            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = spacedBy(4.dp),
                modifier = Modifier
                    .align(CenterHorizontally)
                    .padding(top = 26.dp, bottom = 24.dp)
                    .onClick {
                        uriHandler.openUri(Config.webDataImportUrl(null))
                    },
            ) {
                Text(
                    text = stringResource(R.string.welcome_import_browse_all),
                    style = TraktTheme.typography.paragraphSmall,
                    color = TraktTheme.colors.textPrimary,
                    modifier = Modifier
                        .graphicsLayer {
                            translationY = -0.5.dp.toPx()
                        },
                )
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = TraktTheme.colors.textPrimary,
                    modifier = Modifier.size(16.dp),
                )
            }

            if (!isVip) {
                HorizontalDivider(
                    color = TraktTheme.colors.chipContainer,
                    modifier = Modifier
                        .padding(horizontal = 1.dp)
                        .padding(bottom = 24.dp),
                )

                Text(
                    text = stringResource(R.string.welcome_outro_heading),
                    style = TraktTheme.typography.heading3,
                    color = TraktTheme.colors.textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                )

                BecomeVipCard(
                    modifier = Modifier.onClick(onClick = onVipClick),
                )
            }
        }
    }
}

@Composable
private fun ImportTvTimeCard(
    modifier: Modifier = Modifier,
    onImportClick: () -> Unit = {},
    onLiberatorClick: () -> Unit = {},
    onGdprClick: () -> Unit = {},
) {
    val radialGradient = remember {
        object : ShaderBrush() {
            override fun createShader(size: Size): Shader {
                return RadialGradientShader(
                    colors = listOf(
                        Purple600,
                        Shade910,
                    ),
                    center = Offset(size.width * 1.5F, -size.height / 1.5F),
                    radius = size.width * 1.5F,
                )
            }
        }
    }

    Column(
        verticalArrangement = spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = DefaultCardShape)
            .border(width = 1.dp, color = TraktTheme.colors.accent, shape = DefaultCardShape)
            .background(radialGradient, DefaultCardShape)
            .padding(20.dp),
    ) {
        InfoChip(
            text = stringResource(R.string.welcome_tvtime_badge),
            containerColor = TraktTheme.colors.primaryButtonContainer,
            modifier = Modifier
                .shadow(elevation = 1.dp, shape = RoundedCornerShape(100)),
        )

        Text(
            text = stringResource(R.string.welcome_tvtime_heading),
            style = TraktTheme.typography.heading4,
            color = TraktTheme.colors.textPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
        )

        Text(
            text = stringResource(R.string.welcome_tvtime_body),
            style = TraktTheme.typography.paragraphSmall,
            color = TraktTheme.colors.textPrimary,
        )

        Column(
            verticalArrangement = spacedBy(4.dp),
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            PrimaryButton(
                text = stringResource(R.string.welcome_tvtime_import_cta),
                onClick = onImportClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun BecomeVipCard(modifier: Modifier = Modifier) {
    val radialGradient = remember {
        object : ShaderBrush() {
            override fun createShader(size: Size): Shader {
                return RadialGradientShader(
                    colors = listOf(
                        Purple600,
                        Shade910,
                    ),
                    center = Offset(size.width * 1.5F, -size.height / 1.5F),
                    radius = size.width * 1.5F,
                )
            }
        }
    }

    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = DefaultCardShape)
            .border(width = 1.dp, color = TraktTheme.colors.accent, shape = DefaultCardShape)
            .background(radialGradient, DefaultCardShape)
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Column(
            verticalArrangement = spacedBy(4.dp),
            modifier = Modifier
                .weight(1F)
                .align(CenterVertically),
        ) {
            Text(
                text = stringResource(R.string.welcome_vip_upsell_heading),
                style = TraktTheme.typography.heading4,
                color = TraktTheme.colors.textPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
            )

            Text(
                text = stringResource(R.string.welcome_vip_upsell_description),
                style = TraktTheme.typography.paragraphSmall,
                color = Shade200,
            )
        }

        VipChip(
            text = stringResource(R.string.badge_text_get_vip),
            icon = painterResource(R.drawable.ic_stars),
            color = Purple500,
            modifier = Modifier.shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(100),
            ),
        )
    }
}

@Composable
private fun ImportSourceCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    onImportClick: () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = DefaultCardShape)
            .background(TraktTheme.colors.dialogOnContainer, DefaultCardShape)
            .padding(20.dp),
    ) {
        Text(
            text = title,
            style = TraktTheme.typography.heading4,
            color = TraktTheme.colors.textPrimary,
        )

        Text(
            text = description,
            style = TraktTheme.typography.paragraphSmall,
            color = TraktTheme.colors.textSecondary,
        )

        PrimaryButton(
            text = stringResource(R.string.welcome_import_action, title),
            onClick = onImportClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        )
    }
}

@Preview(heightDp = 2000)
@Composable
private fun Preview() {
    TraktTheme {
        WelcomeView(name = "Mike")
    }
}
