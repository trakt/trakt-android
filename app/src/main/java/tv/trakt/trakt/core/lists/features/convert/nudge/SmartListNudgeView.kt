package tv.trakt.trakt.core.lists.features.convert.nudge

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.Ids
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.SmartListDraftsSummary
import tv.trakt.trakt.core.lists.features.convert.model.ListOverlap
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.components.buttons.TertiaryButton
import tv.trakt.trakt.ui.theme.DefaultCardShape
import tv.trakt.trakt.ui.theme.TraktTheme

private val TouchTargetSize = 32.dp
private val SmallButtonHeight = 28.dp

@Composable
internal fun SmartListNudgeView(
    modifier: Modifier = Modifier,
    viewModel: SmartListNudgeViewModel = koinViewModel(),
    onConvertClick: (TraktId) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val summary = state.summary ?: return

    SmartListNudgeContent(
        summary = summary,
        onConvertClick = { onConvertClick(it.ids.trakt) },
        onDismissClick = viewModel::dismiss,
        modifier = modifier,
    )
}

@Composable
private fun SmartListNudgeContent(
    summary: SmartListDraftsSummary,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
    onConvertClick: (SmartListDraftsSummary.Entry) -> Unit = {},
    onDismissClick: () -> Unit = {},
) {
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded) }

    val message = when (summary.convertibleCount) {
        1 -> stringResource(R.string.smart_list_nudge_message_short_single, summary.convertibleCount)
        else -> stringResource(R.string.smart_list_nudge_message_short, summary.convertibleCount)
    }

    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(TraktTheme.colors.panelCardContainer, DefaultCardShape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = spacedBy(6.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_smart),
                tint = TraktTheme.colors.accentPalette.shade400,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )

            Text(
                text = message,
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.paragraphSmall.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1F),
            )

            TertiaryButton(
                text = stringResource(
                    when {
                        expanded -> R.string.button_text_hide_lists
                        else -> R.string.button_text_review_lists
                    },
                ),
                height = SmallButtonHeight,
                onClick = { expanded = !expanded },
                modifier = Modifier.padding(start = 8.dp),
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(TouchTargetSize)
                    .onClick(onClick = onDismissClick),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    tint = TraktTheme.colors.textSecondary,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                verticalArrangement = spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            ) {
                summary.lists.forEach { entry ->
                    NudgeRow(
                        entry = entry,
                        onConvertClick = { onConvertClick(entry) },
                    )
                }
            }
        }
    }
}

@Composable
private fun NudgeRow(
    entry: SmartListDraftsSummary.Entry,
    onConvertClick: () -> Unit,
) {
    val overlap = ListOverlap.from(
        count = entry.itemCount,
        confidence = entry.confidence,
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(12.dp),
    ) {
        Column(
            verticalArrangement = spacedBy(2.dp),
            modifier = Modifier.weight(1F),
        ) {
            Text(
                text = entry.name,
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.cardTitle.copy(fontSize = 14.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            overlap?.let {
                Text(
                    text = stringResource(R.string.smart_list_nudge_row_overlap, it.kept, it.total),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.cardSubtitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        PrimaryButton(
            text = stringResource(R.string.button_text_convert),
            height = SmallButtonHeight,
            contentPadding = 12.dp,
            textStyle = TraktTheme.typography.buttonTertiary,
            containerColor = TraktTheme.colors.accent,
            contentColor = Color.White,
            onClick = onConvertClick,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        SmartListNudgeContent(
            initiallyExpanded = true,
            summary = SmartListDraftsSummary(
                convertibleCount = 2,
                bestConfidence = 0.92F,
                lists = persistentListOf(
                    SmartListDraftsSummary.Entry(
                        ids = Ids(trakt = TraktId(1), slug = SlugId("horror")),
                        name = "Horror picks",
                        itemCount = 120,
                        confidence = 0.92F,
                    ),
                    SmartListDraftsSummary.Entry(
                        ids = Ids(trakt = TraktId(2), slug = SlugId("anime")),
                        name = "Anime this season",
                        itemCount = 40,
                        confidence = 0.78F,
                    ),
                ),
            ),
        )
    }
}
