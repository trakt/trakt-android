package tv.trakt.trakt.core.summary.sentiment.views

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.realitycheck.DocumentaryCheck
import tv.trakt.trakt.common.model.realitycheck.DocumentaryCheck.DisputeSource
import tv.trakt.trakt.core.summary.sentiment.GetVipView
import tv.trakt.trakt.core.summary.sentiment.model.RealityCheckSection
import tv.trakt.trakt.core.summary.ui.views.insights.SegmentMeter
import tv.trakt.trakt.core.summary.ui.views.insights.documentaryMakersText
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.spoilerBlur
import tv.trakt.trakt.ui.theme.TraktTheme

private val MockDisputes = persistentListOf(
    DocumentaryCheck.Dispute(
        claim = "The film claims the river has lost most of its fish.",
        by = "A university research group",
        source = DisputeSource.Expert,
        year = 2021,
        reply = null,
        isMinor = false,
    ),
    DocumentaryCheck.Dispute(
        claim = "The film claims the plant dumps waste every night.",
        by = "A regional newspaper",
        source = DisputeSource.Press,
        year = 2021,
        reply = null,
        isMinor = false,
    ),
    DocumentaryCheck.Dispute(
        claim = "The film's account of the industry's own safety record.",
        by = "An industry group",
        source = DisputeSource.Interested,
        year = 2021,
        reply = "The filmmakers say the film stands by its sources.",
        isMinor = true,
    ),
)

@Composable
internal fun DocumentaryCheckContent(
    documentaryCheck: DocumentaryCheck,
    vip: Boolean,
    modifier: Modifier = Modifier,
    collapsedSections: ImmutableSet<RealityCheckSection> = persistentSetOf(),
    onSectionCollapse: (RealityCheckSection, Boolean) -> Unit = { _, _ -> },
    upsell: @Composable () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(28.dp),
        modifier = modifier.padding(top = 16.dp),
    ) {
        DocumentaryCheckHero(documentaryCheck)

        Column(
            verticalArrangement = spacedBy(8.dp),
        ) {
            MarkdownText(
                text = documentaryCheck.summary,
                color = TraktTheme.colors.textPrimary,
            )
            documentaryMakersText(documentaryCheck)?.let {
                Text(
                    text = it,
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.paragraphSmaller,
                )
            }
        }

        if (vip) {
            if (documentaryCheck.disputes.isNotEmpty()) {
                val collapsed = RealityCheckSection.Disputes in collapsedSections
                DocumentaryDisputes(
                    disputes = documentaryCheck.disputes,
                    collapsed = collapsed,
                    onCollapseClick = { onSectionCollapse(RealityCheckSection.Disputes, !collapsed) },
                )
            }
        } else {
            upsell()
            DocumentaryDisputes(
                disputes = MockDisputes,
                modifier = Modifier.spoilerBlur(),
            )
        }
    }
}

@Composable
private fun DocumentaryCheckHero(documentaryCheck: DocumentaryCheck) {
    Column(
        verticalArrangement = spacedBy(6.dp),
    ) {
        Text(
            text = stringResource(documentaryCheck.tier.displayTextRes),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading4,
        )
        SegmentMeter(
            filled = documentaryCheck.tier.segments,
            color = when {
                TraktTheme.colors.isLight -> TraktTheme.colors.accent
                else -> TraktTheme.colors.accentPalette.shade400
            },
            large = true,
            modifier = Modifier.padding(
                top = 6.dp,
                bottom = 4.dp,
            ),
        )
    }
}

@Composable
private fun DocumentaryDisputes(
    disputes: ImmutableList<DocumentaryCheck.Dispute>,
    modifier: Modifier = Modifier,
    collapsed: Boolean? = null,
    onCollapseClick: () -> Unit = {},
) {
    InsightsSegments(
        header = stringResource(R.string.header_documentary_disputed_claims),
        count = disputes.size,
        modifier = modifier,
        collapsed = collapsed,
        onCollapseClick = onCollapseClick,
    ) { index ->
        DocumentaryDispute(disputes[index])
    }
}

@Composable
private fun DocumentaryDispute(dispute: DocumentaryCheck.Dispute) {
    Column(
        verticalArrangement = spacedBy(16.dp),
    ) {
        Row(
            horizontalArrangement = spacedBy(8.dp),
            verticalAlignment = CenterVertically,
        ) {
            RealityChip(
                text = stringResource(dispute.source.displayTextRes),
                tone = RealityTone.Neutral,
            )
            if (dispute.isMinor) {
                Text(
                    text = "(${stringResource(R.string.text_reality_claim_minor)})",
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.paragraphSmaller,
                    maxLines = 1,
                )
            }
            Text(
                text = dispute.year?.toString().orEmpty(),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.paragraphSmaller,
                textAlign = TextAlign.End,
                maxLines = 1,
                modifier = Modifier.weight(1F),
            )
        }

        ClaimText(
            label = stringResource(R.string.label_reality_claim_on_screen),
            text = dispute.claim,
            color = TraktTheme.colors.textSecondary,
        )
        ClaimText(
            label = stringResource(R.string.label_documentary_claim_disputed_by),
            text = dispute.by,
            color = TraktTheme.colors.textPrimary,
        )
        dispute.reply?.let {
            ClaimText(
                label = stringResource(R.string.label_documentary_claim_reply),
                text = it,
                color = TraktTheme.colors.textSecondary,
            )
        }
    }
}

@PreviewTraktLightDark
@Composable
private fun PreviewVip() {
    TraktThemePreview {
        DocumentaryCheckContent(
            documentaryCheck = PreviewData.documentaryCheck,
            vip = true,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun PreviewFree() {
    TraktThemePreview {
        DocumentaryCheckContent(
            documentaryCheck = PreviewData.documentaryCheck,
            vip = false,
            modifier = Modifier.padding(16.dp),
            upsell = { GetVipView() },
        )
    }
}
