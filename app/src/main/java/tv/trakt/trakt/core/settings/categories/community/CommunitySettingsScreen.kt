package tv.trakt.trakt.core.settings.categories.community

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tv.trakt.trakt.common.Config
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.core.settings.ui.SettingsSegmentedGroup
import tv.trakt.trakt.core.settings.ui.SettingsSubscreen
import tv.trakt.trakt.core.settings.ui.SettingsTextField
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R

@Composable
internal fun CommunitySettingsScreen(
    viewModel: CommunitySettingsViewModel,
    onNavigateBack: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    CommunitySettingsContent(
        state = state,
        onRedditClick = {
            viewModel.logRedditLink()
            uriHandler.openUri(Config.WEB_SOCIAL_REDDIT_URL)
        },
        onSupportClick = {
            uriHandler.openUri(Config.WEB_SUPPORT_URL)
        },
        onFeedbackClick = {
            uriHandler.openUri(Config.WEB_ROADMAP_URL)
        },
        onBackClick = onNavigateBack,
    )
}

@Composable
private fun CommunitySettingsContent(
    state: CommunitySettingsState,
    modifier: Modifier = Modifier,
    onRedditClick: () -> Unit = { },
    onSupportClick: () -> Unit = { },
    onFeedbackClick: () -> Unit = { },
    onBackClick: () -> Unit = { },
) {
    SettingsSubscreen(
        title = stringResource(R.string.text_footer_category_community),
        onBackClick = onBackClick,
        modifier = modifier,
    ) {
        SettingsSegmentedGroup {
            item {
                SettingsTextField(
                    text = stringResource(R.string.link_label_reddit),
                    onClick = onRedditClick,
                )
            }

            if (state.user?.isAnyVip == true) {
                item {
                    SettingsTextField(
                        text = stringResource(R.string.link_text_support),
                        onClick = onSupportClick,
                    )
                }

                item {
                    SettingsTextField(
                        text = stringResource(R.string.link_text_feedback),
                        icon = R.drawable.ic_feedback,
                        iconSize = 17.dp,
                        onClick = onFeedbackClick,
                    )
                }
            }
        }
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        CommunitySettingsContent(
            state = CommunitySettingsState(
                user = PreviewData.user1,
            ),
        )
    }
}
