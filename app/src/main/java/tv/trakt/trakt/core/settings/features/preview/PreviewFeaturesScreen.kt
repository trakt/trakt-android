package tv.trakt.trakt.core.settings.features.preview

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.core.featureflags.model.FeatureFlag
import tv.trakt.trakt.core.featureflags.model.FeatureFlagAudience
import tv.trakt.trakt.core.featureflags.model.FeatureFlagPreview
import tv.trakt.trakt.core.settings.ui.SettingsSegmentedGroup
import tv.trakt.trakt.core.settings.ui.SettingsSubscreen
import tv.trakt.trakt.core.settings.ui.SettingsSwitchField
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.vip.VipBanner
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun PreviewFeaturesScreen(
    viewModel: PreviewFeaturesViewModel,
    onNavigateVip: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    PreviewFeaturesContent(
        state = state,
        onFeatureClick = viewModel::setFeatureEnabled,
        onVipClick = onNavigateVip,
        onBackClick = onNavigateBack,
    )
}

@Composable
private fun PreviewFeaturesContent(
    state: PreviewFeaturesState,
    modifier: Modifier = Modifier,
    onFeatureClick: (FeatureFlag, Boolean) -> Unit = { _, _ -> },
    onVipClick: () -> Unit = { },
    onBackClick: () -> Unit = { },
) {
    SettingsSubscreen(
        title = stringResource(R.string.header_preview_features),
        onBackClick = onBackClick,
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.description_preview_features),
            style = TraktTheme.typography.paragraph.copy(
                fontSize = 14.sp,
            ),
            color = TraktTheme.colors.textSecondary,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        when {
            state.features == null -> {
            }

            state.features.isEmpty() -> {
                Text(
                    text = stringResource(R.string.text_placeholder_preview_features),
                    style = TraktTheme.typography.paragraphSmall,
                    color = TraktTheme.colors.textPrimary,
                )
            }

            else -> {
                if (state.features.any { it.locked }) {
                    VipBanner(
                        description = stringResource(R.string.text_vip_upsell_preview_features),
                        onClick = onVipClick,
                    )
                }

                PreviewFeaturesGroup(
                    title = stringResource(R.string.tag_text_vip),
                    features = state.features.filter { it.audience == FeatureFlagAudience.Vip },
                    onFeatureClick = onFeatureClick,
                )
                PreviewFeaturesGroup(
                    title = stringResource(R.string.header_director),
                    features = state.features.filter { it.audience == FeatureFlagAudience.Director },
                    onFeatureClick = onFeatureClick,
                )
            }
        }
    }
}

@Composable
private fun PreviewFeaturesGroup(
    title: String,
    features: List<FeatureFlagPreview>,
    onFeatureClick: (FeatureFlag, Boolean) -> Unit,
) {
    if (features.isEmpty()) return

    SettingsSegmentedGroup(
        title = title,
    ) {
        features.forEach { feature ->
            item {
                SettingsSwitchField(
                    text = stringResource(feature.flag.title),
                    description = stringResource(feature.flag.description),
                    descriptionMaxLines = Int.MAX_VALUE,
                    checked = feature.enabled,
                    enabled = !feature.locked,
                    onClick = {
                        onFeatureClick(feature.flag, !feature.enabled)
                    },
                )
            }
        }
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        PreviewFeaturesContent(
            state = PreviewFeaturesState(
                features = persistentListOf(
                    FeatureFlagPreview(
                        flag = FeatureFlag.CharacterPortraits,
                        audience = FeatureFlagAudience.Vip,
                        enabled = true,
                        locked = false,
                    ),
                    FeatureFlagPreview(
                        flag = FeatureFlag.CharacterPortraits,
                        audience = FeatureFlagAudience.Director,
                        enabled = false,
                        locked = false,
                    ),
                ),
            ),
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun PreviewLocked() {
    TraktThemePreview {
        PreviewFeaturesContent(
            state = PreviewFeaturesState(
                features = persistentListOf(
                    FeatureFlagPreview(
                        flag = FeatureFlag.CharacterPortraits,
                        audience = FeatureFlagAudience.Vip,
                        enabled = false,
                        locked = true,
                    ),
                ),
            ),
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun PreviewEmpty() {
    TraktThemePreview {
        PreviewFeaturesContent(
            state = PreviewFeaturesState(
                features = persistentListOf(),
            ),
        )
    }
}
