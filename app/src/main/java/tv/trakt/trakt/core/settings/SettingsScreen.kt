package tv.trakt.trakt.core.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration.Short
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W600
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tv.trakt.trakt.BuildConfig
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.Config
import tv.trakt.trakt.common.helpers.extensions.DevicePreview
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.ui.theme.colors.LightColors
import tv.trakt.trakt.core.settings.features.avatar.AvatarCropSheet
import tv.trakt.trakt.core.settings.model.SettingsCategory
import tv.trakt.trakt.core.settings.ui.SettingsCategoryField
import tv.trakt.trakt.core.settings.ui.SettingsScaffold
import tv.trakt.trakt.core.settings.ui.SettingsSegmentedGroup
import tv.trakt.trakt.core.settings.ui.SettingsUserPanel
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.whatsnew.openPlayStore
import tv.trakt.trakt.ui.theme.DefaultCardShape
import tv.trakt.trakt.ui.theme.TraktTheme

internal const val SECTION_SPACING_DP = 12
internal const val SECTION_ITEM_HEIGHT_DP = 32

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateCategory: (SettingsCategory) -> Unit,
    onNavigateUsage: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val snackbar = LocalSnackbarState.current
    val context = LocalContext.current

    val state by viewModel.state.collectAsStateWithLifecycle()

    var avatarImage by rememberSaveable { mutableStateOf<Uri?>(null) }
    val avatarPicker = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        avatarImage = uri
    }

    LaunchedEffect(Unit) {
        viewModel.info.collect { info ->
            snackbar.showSnackbar(
                message = info.get(context),
                duration = Short,
            )
        }
    }

    SettingsScreenContent(
        state = state,
        onCategoryClick = onNavigateCategory,
        onUserClick = onNavigateUsage,
        onUploadAvatar = {
            avatarPicker.launch(PickVisualMediaRequest(ImageOnly))
        },
        onRemoveAvatar = viewModel::removeAvatar,
        onGithubClick = {
            uriHandler.openUri(Config.WEB_SOCIAL_GITHUB_URL)
        },
        onInstagramClick = {
            uriHandler.openUri(Config.WEB_SOCIAL_INSTAGRAM_URL)
        },
        onTwitterClick = {
            uriHandler.openUri(Config.WEB_SOCIAL_X_URL)
        },
        onRedditClick = {
            viewModel.logRedditLink()
            uriHandler.openUri(Config.WEB_SOCIAL_REDDIT_URL)
        },
        onBackClick = onNavigateBack,
    )

    AvatarCropSheet(
        image = avatarImage,
        onConfirm = viewModel::uploadAvatar,
        onDismiss = {
            avatarImage = null
        },
    )
}

@Composable
private fun SettingsScreenContent(
    state: SettingsState,
    modifier: Modifier = Modifier,
    onCategoryClick: (SettingsCategory) -> Unit = { },
    onUserClick: () -> Unit = { },
    onUploadAvatar: () -> Unit = { },
    onRemoveAvatar: () -> Unit = { },
    onGithubClick: () -> Unit = { },
    onInstagramClick: () -> Unit = { },
    onTwitterClick: () -> Unit = { },
    onRedditClick: () -> Unit = { },
    onBackClick: () -> Unit = { },
) {
    SettingsScaffold(
        modifier = modifier,
        verticalArrangement = spacedBy(24.dp),
        titleBar = {
            TitleBar(
                onRedditClick = onRedditClick,
                onGithubClick = onGithubClick,
                onInstagramClick = onInstagramClick,
                onTwitterClick = onTwitterClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .onClick {
                        onBackClick()
                    },
            )
        },
    ) {
        state.user?.let { user ->
            // The avatar URL does not change with the image, so a new key forces a fresh image request.
            key(state.avatarVersion) {
                SettingsUserPanel(
                    user = user,
                    avatarLoading = state.avatarLoading.isLoading,
                    onClick = onUserClick,
                    onUploadAvatar = onUploadAvatar,
                    onRemoveAvatar = onRemoveAvatar,
                )
            }
        }

        SettingsSegmentedGroup {
            SettingsCategory.entries.forEach { category ->
                item {
                    SettingsCategoryField(
                        category = category,
                        vip = state.user?.isAnyVip == true,
                        onClick = {
                            onCategoryClick(category)
                        },
                    )
                }
            }
        }

        SettingsFooter()
    }
}

@Composable
private fun TitleBar(
    modifier: Modifier = Modifier,
    onRedditClick: () -> Unit = {},
    onGithubClick: () -> Unit = {},
    onInstagramClick: () -> Unit = {},
    onTwitterClick: () -> Unit = {},
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(12.dp),
            modifier = Modifier
                .height(TraktTheme.size.titleBarHeight)
                .graphicsLayer {
                    translationX = -2.dp.toPx()
                },
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back_arrow),
                tint = TraktTheme.colors.textPrimary,
                contentDescription = null,
            )
            TraktHeader(
                title = stringResource(R.string.page_title_settings),
                subtitle = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            )
        }

        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(14.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_reddit),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(24.dp)
                    .onClick(onClick = onRedditClick),
            )

            Icon(
                painter = painterResource(R.drawable.ic_github),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(22.dp)
                    .onClick(onClick = onGithubClick),
            )

            Icon(
                painter = painterResource(R.drawable.ic_instagram),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier
                    .size(28.dp)
                    .onClick(onClick = onInstagramClick),
            )

            Icon(
                painter = painterResource(R.drawable.ic_x_twitter),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier
                    .size(28.dp)
                    .onClick(onClick = onTwitterClick),
            )
        }
    }
}

@Composable
private fun SettingsFooter(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current

    Column(
        verticalArrangement = spacedBy(24.dp),
        modifier = modifier,
    ) {
        RateTraktView(
            modifier = Modifier
                .fillMaxWidth(),
        )

        Row(
            horizontalArrangement = spacedBy(8.dp),
            verticalAlignment = CenterVertically,
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
        ) {
            Text(
                text = stringResource(R.string.link_text_terms),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.paragraph.copy(
                    fontSize = 14.sp,
                ),
                modifier = Modifier.onClick {
                    uriHandler.openUri(Config.WEB_TERMS_URL)
                },
            )

            Text(
                text = "•",
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.paragraph.copy(
                    fontSize = 14.sp,
                ),
            )

            Text(
                text = stringResource(R.string.link_text_policy),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.paragraph.copy(
                    fontSize = 14.sp,
                ),
                modifier = Modifier.onClick {
                    uriHandler.openUri(Config.WEB_PRIVACY_URL)
                },
            )
        }
    }
}

@Composable
private fun RateTraktView(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(14.dp),
        modifier = modifier
            .onClick {
                openPlayStore(context)
            }
            .shadow(
                elevation = 2.dp,
                shape = DefaultCardShape,
                clip = false,
            )
            .background(
                color = TraktTheme.colors.rateBannerContainer,
                shape = DefaultCardShape,
            )
            .padding(horizontal = 16.dp, vertical = 18.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_mood_face),
            contentDescription = null,
            tint = TraktTheme.colors.textPrimaryOnAccent,
            modifier = Modifier.size(26.dp),
        )

        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.header_rate_us_1),
                style = TraktTheme.typography.paragraphSmall.copy(
                    fontWeight = W600,
                ),
                color = TraktTheme.colors.textPrimaryOnAccent,
                textAlign = TextAlign.Start,
            )

            Text(
                text = stringResource(R.string.header_rate_us_2),
                style = TraktTheme.typography.paragraphSmaller,
                color = TraktTheme.colors.textPrimaryOnAccent,
                textAlign = TextAlign.Start,
            )
        }
    }
}

// Previews

@DevicePreview
@Composable
private fun Preview() {
    TraktTheme {
        SettingsScreenContent(
            state = SettingsState(
                user = PreviewData.user1,
            ),
        )
    }
}

@DevicePreview
@Composable
private fun PreviewVip() {
    TraktTheme {
        SettingsScreenContent(
            state = SettingsState(
                user = PreviewData.user1.copy(
                    location = "Cracow, Poland",
                    isVip = true,
                    isDirector = false,
                ),
            ),
        )
    }
}

@DevicePreview
@Composable
private fun PreviewDirector() {
    TraktTheme {
        SettingsScreenContent(
            state = SettingsState(
                user = PreviewData.user1.copy(
                    location = "Cracow, Poland",
                    isVip = true,
                    isDirector = true,
                ),
            ),
        )
    }
}

@DevicePreview
@Composable
private fun PreviewLight() {
    TraktTheme(
        colors = LightColors,
    ) {
        SettingsScreenContent(
            state = SettingsState(
                user = PreviewData.user1,
            ),
        )
    }
}
