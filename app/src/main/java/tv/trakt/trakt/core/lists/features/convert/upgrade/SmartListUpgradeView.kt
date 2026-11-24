package tv.trakt.trakt.core.lists.features.convert.upgrade

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.common.model.lists.SmartList
import tv.trakt.trakt.common.model.lists.SmartListDraft
import tv.trakt.trakt.core.lists.features.convert.model.ListOverlap
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.theme.DefaultCardShape
import tv.trakt.trakt.ui.theme.TraktTheme

private val TouchTargetSize = 32.dp

private val CardShadow = Shadow(
    radius = 1.dp,
    color = Color.Black,
    spread = 0.75.dp,
    alpha = 0.1F,
    offset = DpOffset(x = 0.dp, y = 0.75.dp),
)

@Composable
internal fun SmartListUpgradeView(
    list: CustomList,
    modifier: Modifier = Modifier,
    viewModel: SmartListUpgradeViewModel = koinViewModel(
        key = list.ids.trakt.value.toString(),
        parameters = { parametersOf(list) },
    ),
    onConvertClick: (CustomList) -> Unit,
    onCreateSmartListClick: () -> Unit,
    onSmartListRedirect: (SmartList) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.navigateSmartList) {
        state.navigateSmartList?.let {
            onSmartListRedirect(it)
            viewModel.clearNavigation()
        }
    }

    AnimatedVisibility(
        visible = state.visible && !state.loading.isLoading,
        enter = fadeIn() + slideInVertically { it / 20 },
        exit = fadeOut(),
        modifier = modifier,
    ) {
        SmartListUpgradeCard(
            list = list,
            draft = state.draft,
            onConvertClick = onConvertClick,
            onCreateSmartListClick = onCreateSmartListClick,
            onDismissClick = viewModel::dismiss,
        )
    }
}

@Composable
private fun SmartListUpgradeCard(
    list: CustomList,
    draft: SmartListDraft?,
    onConvertClick: (CustomList) -> Unit,
    onCreateSmartListClick: () -> Unit,
    onDismissClick: () -> Unit,
) {
    val origin = list.staleOrigin?.displayName.orEmpty()

    if (draft != null) {
        val overlap = ListOverlap.from(
            count = list.itemCount ?: 0,
            confidence = draft.confidence,
        )
        SmartListUpgradeContent(
            title = stringResource(R.string.smart_list_upgrade_draft_title),
            summary = overlap?.let {
                stringResource(
                    R.string.smart_list_upgrade_overlap,
                    it.kept.toString(),
                    it.total.toString(),
                )
            },
            detail = draft.rememberSummary(),
            origin = origin,
            actionText = stringResource(R.string.button_text_smart_list_convert),
            onActionClick = { onConvertClick(list) },
            onDismissClick = onDismissClick,
        )
    } else {
        SmartListUpgradeContent(
            title = stringResource(R.string.smart_list_upgrade_plain_title),
            summary = null,
            detail = stringResource(R.string.smart_list_upgrade_plain_description),
            origin = origin,
            actionText = stringResource(R.string.button_text_create_smart_list),
            onActionClick = onCreateSmartListClick,
            onDismissClick = onDismissClick,
        )
    }
}

@Composable
private fun SmartListUpgradeContent(
    title: String,
    summary: String?,
    detail: String,
    origin: String,
    actionText: String,
    modifier: Modifier = Modifier,
    onActionClick: () -> Unit = {},
    onDismissClick: () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .dropShadow(shape = DefaultCardShape, shadow = CardShadow)
            .background(TraktTheme.colors.panelCardContainer, DefaultCardShape)
            .padding(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = spacedBy(16.dp),
        ) {
            Column(
                verticalArrangement = spacedBy(6.dp),
                modifier = Modifier.weight(1F),
            ) {
                Text(
                    text = title,
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.paragraphSmall.copy(fontWeight = FontWeight.Bold),
                )
                summary?.let {
                    Text(
                        text = it,
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.paragraphSmall,
                    )
                }
                Text(
                    text = detail,
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.meta.copy(
                        lineHeight = 1.2.em,
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(TouchTargetSize)
                    .padding(start = 4.dp)
                    .onClick(onClick = onDismissClick),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    tint = TraktTheme.colors.textSecondary,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        PrimaryButton(
            text = actionText,
            icon = painterResource(R.drawable.ic_smart),
            iconSpace = 4.dp,
            height = 40.dp,
            containerColor = TraktTheme.colors.accent,
            contentColor = Color.White,
            onClick = onActionClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        )

        if (origin.isNotBlank()) {
            Text(
                text = stringResource(R.string.smart_list_upgrade_origin_note, origin),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.meta,
            )
        }
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        SmartListUpgradeContent(
            title = "Make this list update itself",
            summary = "Brings back 84 of your 100 titles, plus new releases",
            detail = "Trending · Movies · Horror · 2020-2026",
            origin = "MDBList",
            actionText = "Turn into a smart list",
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun PlainPreview() {
    TraktThemePreview {
        SmartListUpgradeContent(
            title = "Want this list to update itself?",
            summary = null,
            detail = "Smart lists add new titles on their own, based on filters you pick.",
            origin = "MDBList",
            actionText = "Create a smart list",
        )
    }
}
