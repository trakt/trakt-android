package tv.trakt.trakt.core.settings.features.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.rememberThousandsFormat
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.common.ui.theme.colors.Red400
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.core.settings.features.history.model.CleanUpProgress
import tv.trakt.trakt.core.settings.features.history.model.CleanUpTarget
import tv.trakt.trakt.core.settings.features.history.model.PlaysSummary
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.TertiaryButton
import tv.trakt.trakt.ui.theme.TraktTheme

private val CARD_SHAPE = RoundedCornerShape(16.dp)
private val CARD_PADDING = 20.dp
private val CARD_ITEM_SPACING = 8.dp
private val BREAKDOWN_SPACING = 16.dp
private val BREAKDOWN_SEPARATOR_HEIGHT = 24.dp

@Composable
internal fun HistoryAnalysisCard(
    label: String,
    summary: PlaysSummary,
    loading: Boolean,
    cleanUp: CleanUpProgress?,
    cleanUpEnabled: Boolean,
    modifier: Modifier = Modifier,
    onCleanUpClick: () -> Unit = {},
) {
    val hasDuplicates = summary.duplicates > 0
    val badAccent = when (TraktTheme.colors.isLight) {
        true -> Red500
        false -> Red400
    }

    Column(
        verticalArrangement = spacedBy(CARD_ITEM_SPACING),
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = TraktTheme.colors.shadowDynamicSmall,
                shape = CARD_SHAPE,
            )
            .background(
                color = TraktTheme.colors.panelCardContainer,
                shape = CARD_SHAPE,
            )
            .border(
                width = 1.dp,
                color = if (hasDuplicates) badAccent.copy(alpha = 0.25F) else Color.Transparent,
                shape = CARD_SHAPE,
            )
            .padding(CARD_PADDING),
    ) {
        Text(
            text = label.uppercase(),
            style = TraktTheme.typography.meta,
            color = TraktTheme.colors.textSecondary,
        )

        if (loading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
            ) {
                FilmProgressIndicator(
                    size = 36.dp,
                    strokeWidth = 4.dp,
                )
            }
            return@Column
        }

        Column {
            Text(
                text = rememberThousandsFormat(summary.duplicates),
                style = TraktTheme.typography.heading2,
                color = if (hasDuplicates) badAccent else TraktTheme.colors.textPrimary,
            )
            Text(
                text = stringResource(R.string.text_duplicate_plays),
                style = TraktTheme.typography.paragraphSmall,
                color = TraktTheme.colors.textSecondary,
            )
        }

        Box(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth()
                .height(1.dp)
                .background(TraktTheme.colors.separator),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = spacedBy(BREAKDOWN_SPACING),
            modifier = Modifier.fillMaxWidth(),
        ) {
            BreakdownItem(
                value = rememberThousandsFormat(summary.unique),
                label = stringResource(R.string.text_unique_items),
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(BREAKDOWN_SEPARATOR_HEIGHT)
                    .background(TraktTheme.colors.separator),
            )
            BreakdownItem(
                value = rememberThousandsFormat(summary.total),
                label = stringResource(R.string.text_total_plays),
                modifier = Modifier.weight(1F),
            )
            TertiaryButton(
                text = stringResource(R.string.button_text_clean_up),
                enabled = cleanUpEnabled && hasDuplicates,
                loading = cleanUp?.isRunning == true,
                containerColor = Red500,
                contentColor = Color.White,
                onClick = onCleanUpClick,
                modifier = Modifier.widthIn(min = 86.dp),
            )
        }

        if (cleanUp?.isRunning == true) {
            Column(
                verticalArrangement = spacedBy(CARD_ITEM_SPACING),
                modifier = Modifier.padding(top = CARD_ITEM_SPACING),
            ) {
                LinearProgressIndicator(
                    progress = {
                        if (cleanUp.total > 0) cleanUp.processed.toFloat() / cleanUp.total else 0F
                    },
                    color = badAccent,
                    trackColor = TraktTheme.colors.separator,
                    drawStopIndicator = { },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = stringResource(
                        R.string.cleanup_status_removing,
                        cleanUp.processed,
                        cleanUp.total,
                    ),
                    style = TraktTheme.typography.paragraphSmaller,
                    color = TraktTheme.colors.textSecondary,
                )
            }
        }

        cleanUp?.removed?.let { removed ->
            Text(
                text = stringResource(R.string.cleanup_status_done, removed),
                style = TraktTheme.typography.paragraphSmaller,
                color = TraktTheme.colors.textPrimary,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

@Composable
private fun BreakdownItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = value,
            style = TraktTheme.typography.heading5,
            color = TraktTheme.colors.textPrimary,
        )
        Text(
            text = label,
            style = TraktTheme.typography.paragraphSmaller,
            color = TraktTheme.colors.textSecondary,
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        Column(verticalArrangement = spacedBy(BREAKDOWN_SPACING)) {
            HistoryAnalysisCard(
                label = "Movies",
                summary = PlaysSummary(
                    unique = 1240,
                    total = 1312,
                    duplicateIds = persistentListOf(1L, 2L, 3L),
                ),
                loading = false,
                cleanUp = CleanUpProgress(
                    target = CleanUpTarget.Movies,
                    processed = 1,
                    total = 3,
                ),
                cleanUpEnabled = false,
            )
            HistoryAnalysisCard(
                label = "Episodes",
                summary = PlaysSummary(unique = 8400, total = 8400),
                loading = false,
                cleanUp = CleanUpProgress(
                    target = CleanUpTarget.Episodes,
                    total = 12,
                    removed = 12,
                ),
                cleanUpEnabled = true,
            )
            HistoryAnalysisCard(
                label = "Movies",
                summary = PlaysSummary(),
                loading = true,
                cleanUp = null,
                cleanUpEnabled = true,
            )
        }
    }
}
