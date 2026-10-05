package tv.trakt.trakt.core.summary.ui.views.info

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentMapOf
import tv.trakt.trakt.common.model.parentalguide.ParentalGuide
import tv.trakt.trakt.common.model.parentalguide.ParentalGuideCategory
import tv.trakt.trakt.common.model.parentalguide.ParentalGuideSeverity
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TextLineLoadingIndicator
import tv.trakt.trakt.ui.theme.TraktTheme

private val RowMinHeight = 48.dp
private val SeverityWidth = 112.dp
private val BarThickness = 4.dp
private const val TRACK_ALPHA = 0.22F

@Composable
internal fun ParentalGuideView(
    guide: ParentalGuide?,
    loading: Boolean,
    error: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!loading && !error && guide?.isEmpty != false) return

    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = modifier,
    ) {
        HorizontalDivider(
            thickness = 1.dp,
            color = TraktTheme.colors.separator,
        )

        Text(
            text = stringResource(R.string.option_text_certification_parental_guidance).uppercase(),
            style = TraktTheme.typography.meta,
            color = TraktTheme.colors.textSecondary,
            maxLines = 1,
            overflow = Ellipsis,
            modifier = Modifier.padding(top = 16.dp),
        )

        if (error) {
            Text(
                text = stringResource(R.string.error_text_parental_guide_load_failed),
                style = TraktTheme.typography.paragraphSmaller,
                color = TraktTheme.colors.parentalGuideSevere,
                modifier = Modifier
                    .heightIn(min = RowMinHeight)
                    .padding(vertical = 8.dp),
            )
            return@Column
        }

        Column(
            modifier = Modifier
                .padding(vertical = 8.dp),
        ) {
            for (category in ParentalGuideCategory.entries) {
                ParentalGuideRow(
                    category = category,
                    severity = guide?.severities?.get(category),
                    loading = loading,
                )
            }
        }
    }
}

@Composable
private fun ParentalGuideRow(
    category: ParentalGuideCategory,
    severity: ParentalGuideSeverity?,
    loading: Boolean,
) {
    val categoryLabel = stringResource(category.displayTextRes)
    val severityLabel = stringResource(severity?.displayTextRes ?: R.string.text_unknown)
    val severityColor = if (loading) TraktTheme.colors.textSecondary else severity.color()

    Column(
        verticalArrangement = spacedBy(5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = RowMinHeight)
            .clearAndSetSemantics {
                if (!loading) {
                    contentDescription = "$categoryLabel: $severityLabel"
                }
            },
    ) {
        Row(
            horizontalArrangement = spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = categoryLabel,
                style = TraktTheme.typography.paragraphSmaller,
                color = TraktTheme.colors.textPrimary,
                maxLines = 1,
                overflow = Ellipsis,
                modifier = Modifier.weight(1F),
            )

            Box(
                contentAlignment = Alignment.CenterEnd,
            ) {
                if (loading) {
                    TextLineLoadingIndicator(
                        style = TraktTheme.typography.paragraphSmaller,
                        color = TraktTheme.colors.textSecondary,
                        size = 14.dp,
                    )
                } else {
                    Text(
                        text = severityLabel,
                        style = TraktTheme.typography.paragraphSmaller,
                        color = severityColor,
                        textAlign = TextAlign.End,
                        maxLines = 2,
                        overflow = Ellipsis,
                    )
                }
            }
        }

        SeverityBar(
            progress = if (loading) 0F else severity.progress,
            color = severityColor,
        )
    }
}

@Composable
private fun SeverityBar(
    progress: Float,
    color: Color,
) {
    val shape = RoundedCornerShape(BarThickness / 2)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(BarThickness)
            .clip(shape)
            .background(TraktTheme.colors.textSecondary.copy(alpha = TRACK_ALPHA)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .clip(shape)
                .background(color),
        )
    }
}

private val ParentalGuideSeverity?.progress: Float
    get() = when (this) {
        null -> 0F
        ParentalGuideSeverity.None -> 0.1F
        ParentalGuideSeverity.Mild -> 0.3F
        ParentalGuideSeverity.Moderate -> 0.6F
        ParentalGuideSeverity.Severe -> 1F
    }

@Composable
private fun ParentalGuideSeverity?.color(): Color {
    return when (this) {
        null -> TraktTheme.colors.textSecondary
        ParentalGuideSeverity.None -> TraktTheme.colors.parentalGuideNone
        ParentalGuideSeverity.Mild -> TraktTheme.colors.parentalGuideMild
        ParentalGuideSeverity.Moderate -> TraktTheme.colors.parentalGuideModerate
        ParentalGuideSeverity.Severe -> TraktTheme.colors.parentalGuideSevere
    }
}

@Preview(
    widthDp = 400,
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun Preview() {
    TraktTheme {
        ParentalGuideView(
            guide = ParentalGuide(
                severities = persistentMapOf(
                    ParentalGuideCategory.Nudity to ParentalGuideSeverity.None,
                    ParentalGuideCategory.Violence to ParentalGuideSeverity.Severe,
                    ParentalGuideCategory.Profanity to ParentalGuideSeverity.Moderate,
                    ParentalGuideCategory.Alcohol to ParentalGuideSeverity.Mild,
                ),
            ),
            loading = false,
            error = false,
            modifier = Modifier.padding(24.dp),
        )
    }
}

@Preview(
    widthDp = 400,
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun PreviewLoading() {
    TraktTheme {
        ParentalGuideView(
            guide = null,
            loading = true,
            error = false,
            modifier = Modifier.padding(24.dp),
        )
    }
}

@Preview(
    widthDp = 400,
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun PreviewError() {
    TraktTheme {
        ParentalGuideView(
            guide = null,
            loading = false,
            error = true,
            modifier = Modifier.padding(24.dp),
        )
    }
}
