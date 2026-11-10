package tv.trakt.trakt.core.settings.features.usage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.core.billing.model.VipUsageGroup
import tv.trakt.trakt.core.billing.model.VipUsageLimit
import tv.trakt.trakt.core.billing.ui.VipUsageLimitItem
import tv.trakt.trakt.core.billing.ui.VipUsageLimitSkeleton
import tv.trakt.trakt.core.settings.ui.SettingsSegmentedGroup
import tv.trakt.trakt.core.settings.ui.SettingsSubscreen
import tv.trakt.trakt.core.settings.ui.SettingsUserPanel
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R

private val PlaceholderGroups = persistentListOf(
    VipUsageGroup(
        title = R.string.usage_title_lists,
        limits = persistentListOf(
            VipUsageLimit(R.string.limit_title_smart_lists, current = 0, free = 0, vip = 0),
            VipUsageLimit(R.string.limit_title_personal_lists, current = 0, free = 0, vip = 0),
            VipUsageLimit(R.string.limit_title_total_list_items, current = 0, free = 0, vip = 0),
            VipUsageLimit(R.string.limit_title_watchlist_items, current = 0, free = 0, vip = 0),
        ),
    ),
    VipUsageGroup(
        title = R.string.usage_title_library,
        limits = persistentListOf(
            VipUsageLimit(R.string.limit_title_digital_library, current = 0, free = 0, vip = 0),
        ),
    ),
)

@Composable
internal fun UsageScreen(
    viewModel: UsageViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    UsageContent(
        state = state,
        onBackClick = onNavigateBack,
    )
}

@Composable
private fun UsageContent(
    state: UsageState,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = { },
) {
    val vip = state.user?.isAnyVip == true
    val loading = !state.loading.isDone

    SettingsSubscreen(
        title = stringResource(R.string.button_text_usage),
        onBackClick = onBackClick,
        modifier = modifier,
    ) {
        state.user?.let { user ->
            SettingsUserPanel(user = user)
        }

        val groups = if (loading) PlaceholderGroups else state.groups
        groups?.forEach { group ->
            UsageGroup(
                group = group,
                vip = vip,
                loading = loading,
            )
        }
    }
}

@Composable
private fun UsageGroup(
    group: VipUsageGroup,
    vip: Boolean,
    loading: Boolean,
) {
    SettingsSegmentedGroup(
        title = stringResource(group.title),
    ) {
        group.limits.forEach { limit ->
            item {
                if (loading) {
                    VipUsageLimitSkeleton(
                        title = limit.title,
                        onPanel = true,
                    )
                } else {
                    VipUsageLimitItem(
                        limit = limit,
                        vip = vip,
                    )
                }
            }
        }
    }
}

private val PreviewGroups: ImmutableList<VipUsageGroup> = persistentListOf(
    VipUsageGroup(
        title = R.string.usage_title_lists,
        limits = persistentListOf(
            VipUsageLimit(R.string.limit_title_smart_lists, current = 1, free = 2, vip = 20),
            VipUsageLimit(R.string.limit_title_personal_lists, current = 3, free = 2, vip = 100),
            VipUsageLimit(R.string.limit_title_total_list_items, current = 64, free = 100, vip = 5000),
            VipUsageLimit(R.string.limit_title_watchlist_items, current = 120, free = 100, vip = 5000),
        ),
    ),
    VipUsageGroup(
        title = R.string.usage_title_library,
        limits = persistentListOf(
            VipUsageLimit(R.string.limit_title_digital_library, current = 12, free = 100, vip = 5000),
        ),
    ),
)

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        UsageContent(
            state = UsageState(
                user = PreviewData.user1,
                groups = PreviewGroups,
                loading = LoadingState.Done,
            ),
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun LoadingPreview() {
    TraktThemePreview {
        UsageContent(
            state = UsageState(
                user = PreviewData.user1,
                loading = LoadingState.Loading,
            ),
        )
    }
}
