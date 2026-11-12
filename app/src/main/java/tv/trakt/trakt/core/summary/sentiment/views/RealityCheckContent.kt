package tv.trakt.trakt.core.summary.sentiment.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight.Companion.W600
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.toMarkdownText
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.realitycheck.RealityCheck
import tv.trakt.trakt.common.model.realitycheck.RealityCheck.CharacterKind
import tv.trakt.trakt.common.model.realitycheck.RealityCheck.ClaimLabel
import tv.trakt.trakt.common.model.realitycheck.RealityTier
import tv.trakt.trakt.core.summary.sentiment.GetVipView
import tv.trakt.trakt.core.summary.sentiment.HighlightsCard
import tv.trakt.trakt.core.summary.ui.views.insights.SegmentMeter
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.spoilerBlur
import tv.trakt.trakt.ui.theme.DefaultCardShape
import tv.trakt.trakt.ui.theme.TraktTheme

private const val MUTED_CHIP_ALPHA = 0.75F
private const val UNCLEAR_BADGE_ALPHA = 0.12F
private val RelaxedLineHeight = 1.6.em

private val MockClaims = persistentListOf(
    RealityCheck.Claim(
        about = null,
        label = ClaimLabel.Invented,
        isMinor = false,
        onScreen = "The hero is betrayed by her closest friend.",
        documented = "The friend never existed. She was invented for the film.",
    ),
    RealityCheck.Claim(
        about = null,
        label = ClaimLabel.Compressed,
        isMinor = true,
        onScreen = "Two court cases play out in a single hearing.",
        documented = "There were two separate cases, settled months apart.",
    ),
    RealityCheck.Claim(
        about = null,
        label = ClaimLabel.Accurate,
        isMinor = false,
        onScreen = "The company freezes its main bank account.",
        documented = "The account was frozen, as shown.",
    ),
)

private enum class RealityTone {
    Accurate,
    Adjusted,
    Altered,
}

private val ClaimLabel.tone: RealityTone
    get() = when (this) {
        ClaimLabel.Accurate -> RealityTone.Accurate
        ClaimLabel.Compressed, ClaimLabel.Composite -> RealityTone.Adjusted
        ClaimLabel.Changed, ClaimLabel.Invented -> RealityTone.Altered
    }

private val CharacterKind.tone: RealityTone
    get() = when (this) {
        CharacterKind.Real -> RealityTone.Accurate
        CharacterKind.Composite -> RealityTone.Adjusted
        CharacterKind.Invented -> RealityTone.Altered
    }

@Composable
internal fun RealityCheckContent(
    realityCheck: RealityCheck,
    vip: Boolean,
    modifier: Modifier = Modifier,
    upsell: @Composable () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(20.dp),
        modifier = modifier.padding(top = 16.dp),
    ) {
        RealityCheckHero(realityCheck)

        RealityCard {
            RealityCheckRows(realityCheck)
        }

        MarkdownText(
            text = realityCheck.summary,
            color = TraktTheme.colors.textPrimary,
        )

        val detail = realityCheck.detail
        if (vip) {
            if (detail != null) {
                RealityCheckDetail(detail)
            }
        } else {
            upsell()
            RealityCard(
                header = stringResource(R.string.header_reality_claims),
                modifier = Modifier.spoilerBlur(),
            ) {
                RealityClaims(MockClaims)
            }
        }
    }
}

@Composable
private fun RealityCheckHero(realityCheck: RealityCheck) {
    Column(
        verticalArrangement = spacedBy(6.dp),
    ) {
        Text(
            text = stringResource(realityCheck.verdict.displayTextRes),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading4,
        )
        Text(
            text = stringResource(R.string.text_reality_check_basis),
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.paragraphSmaller,
        )
        SegmentMeter(
            filled = realityCheck.verdict.segments,
            color = TraktTheme.colors.textPrimary,
            large = true,
            modifier = Modifier.padding(vertical = 4.dp),
        )
        if (realityCheck.hasUnclearTier) {
            Text(
                text = stringResource(R.string.text_reality_early_verdict),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.paragraphSmaller,
            )
        }
    }
}

@Composable
private fun RealityCheckRows(realityCheck: RealityCheck) {
    Column(
        verticalArrangement = spacedBy(12.dp),
    ) {
        RealityRow(
            name = stringResource(R.string.label_reality_row_story),
            caption = null,
            tier = realityCheck.story,
        )
        val protagonistCaption = stringResource(R.string.label_reality_row_protagonist)
        realityCheck.protagonists.forEach {
            RealityRow(
                name = it.name,
                caption = protagonistCaption,
                tier = it.tier,
            )
        }
    }
}

@Composable
private fun RealityRow(
    name: String,
    caption: String?,
    tier: RealityTier,
) {
    Row(
        horizontalArrangement = spacedBy(8.dp),
        verticalAlignment = CenterVertically,
        modifier = Modifier.heightIn(min = 32.dp),
    ) {
        Column(
            modifier = Modifier.weight(1F),
        ) {
            MarkdownText(
                text = name,
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.paragraphSmall.copy(fontWeight = W600),
                maxLines = 1,
            )
            caption?.let {
                Text(
                    text = it,
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.paragraphSmaller,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.width(40.dp),
        ) {
            if (tier == RealityTier.Unclear) {
                UnclearBadge()
            } else {
                SegmentMeter(
                    filled = tier.segments,
                    color = TraktTheme.colors.textPrimary,
                )
            }
        }

        Text(
            text = stringResource(tier.displayTextRes),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.paragraphSmaller.copy(fontWeight = W600),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(96.dp),
        )
    }
}

@Composable
private fun UnclearBadge() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(16.dp)
            .background(
                color = TraktTheme.colors.textPrimary.copy(alpha = UNCLEAR_BADGE_ALPHA),
                shape = CircleShape,
            ),
    ) {
        Text(
            text = "?",
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.meta,
        )
    }
}

@Composable
private fun RealityCheckDetail(detail: RealityCheck.Detail) {
    Column(
        verticalArrangement = spacedBy(20.dp),
    ) {
        if (detail.breakdown.isNotBlank()) {
            HighlightsCard(
                header = stringResource(R.string.header_reality_real_story),
                text = detail.breakdown,
            )
        }

        if (detail.claims.isNotEmpty()) {
            RealityCard(header = stringResource(R.string.header_reality_claims)) {
                RealityClaims(detail.claims)
            }
        }

        if (detail.characters.isNotEmpty()) {
            RealityCard(header = stringResource(R.string.header_reality_people)) {
                detail.characters.forEach { character ->
                    RealityCharacter(character)
                }
            }
        }

        if (detail.timeline.isNotBlank()) {
            RealityCard(header = stringResource(R.string.header_reality_timeline)) {
                MarkdownText(
                    text = detail.timeline,
                    color = TraktTheme.colors.textSecondary,
                )
            }
        }

        if (detail.leftOut.isNotEmpty()) {
            RealityCard(header = stringResource(R.string.header_reality_left_out)) {
                detail.leftOut.forEach { item ->
                    Row(
                        horizontalArrangement = spacedBy(6.dp),
                    ) {
                        Text(
                            text = "•",
                            color = TraktTheme.colors.textSecondary,
                            style = TraktTheme.typography.paragraphSmall,
                        )
                        MarkdownText(
                            text = item,
                            color = TraktTheme.colors.textSecondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RealityClaims(claims: ImmutableList<RealityCheck.Claim>) {
    Column(
        verticalArrangement = spacedBy(16.dp),
    ) {
        claims.forEachIndexed { index, claim ->
            if (index > 0) {
                HorizontalDivider(color = TraktTheme.colors.separator)
            }
            RealityClaim(claim)
        }
    }
}

@Composable
private fun RealityClaim(claim: RealityCheck.Claim) {
    Column(
        verticalArrangement = spacedBy(8.dp),
    ) {
        Row(
            horizontalArrangement = spacedBy(8.dp),
            verticalAlignment = CenterVertically,
        ) {
            RealityChip(
                text = stringResource(claim.label.displayTextRes),
                tone = claim.label.tone,
                muted = claim.isMinor,
            )
            if (claim.isMinor) {
                Text(
                    text = stringResource(R.string.text_reality_claim_minor),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.paragraphSmaller,
                )
            }
            claim.about?.let {
                MarkdownText(
                    text = it,
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.paragraphSmaller,
                    maxLines = 1,
                )
            }
        }

        ClaimText(
            label = stringResource(R.string.label_reality_claim_on_screen),
            text = claim.onScreen,
            color = TraktTheme.colors.textSecondary,
        )
        ClaimText(
            label = stringResource(R.string.label_reality_claim_reality),
            text = claim.documented,
            color = TraktTheme.colors.textPrimary,
        )
    }
}

@Composable
private fun ClaimText(
    label: String,
    text: String,
    color: Color,
) {
    Row(
        horizontalArrangement = spacedBy(10.dp),
    ) {
        Text(
            text = label.uppercase(),
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.meta,
            modifier = Modifier
                .widthIn(min = 72.dp)
                .padding(top = 4.dp),
        )
        MarkdownText(
            text = text,
            color = color,
        )
    }
}

@Composable
private fun RealityCharacter(character: RealityCheck.Character) {
    Column(
        verticalArrangement = spacedBy(4.dp),
    ) {
        Row(
            horizontalArrangement = spacedBy(8.dp),
            verticalAlignment = CenterVertically,
        ) {
            MarkdownText(
                text = character.name,
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.paragraphSmall.copy(fontWeight = W600),
                maxLines = 1,
                modifier = Modifier.weight(1F, fill = false),
            )
            RealityChip(
                text = stringResource(character.kind.displayTextRes),
                tone = character.kind.tone,
            )
        }
        if (character.note.isNotBlank()) {
            MarkdownText(
                text = character.note,
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.paragraphSmaller.copy(lineHeight = RelaxedLineHeight),
            )
        }
    }
}

@Composable
private fun RealityChip(
    text: String,
    tone: RealityTone,
    muted: Boolean = false,
) {
    val (containerColor, contentColor) = when (tone) {
        RealityTone.Accurate -> TraktTheme.colors.realityAccurateContainer to TraktTheme.colors.realityAccurateContent
        RealityTone.Adjusted -> TraktTheme.colors.realityAdjustedContainer to TraktTheme.colors.realityAdjustedContent
        RealityTone.Altered -> TraktTheme.colors.realityAlteredContainer to TraktTheme.colors.realityAlteredContent
    }

    Text(
        text = text.uppercase(),
        color = contentColor,
        style = TraktTheme.typography.meta,
        maxLines = 1,
        modifier = Modifier
            .alpha(if (muted) MUTED_CHIP_ALPHA else 1F)
            .background(
                color = containerColor,
                shape = CircleShape,
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@Composable
private fun RealityCard(
    modifier: Modifier = Modifier,
    header: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = DefaultCardShape,
        elevation = CardDefaults.cardElevation(
            defaultElevation = TraktTheme.colors.shadowDynamicDefault,
            pressedElevation = TraktTheme.colors.shadowDynamicDefault,
            disabledElevation = TraktTheme.colors.shadowDynamicDefault,
        ),
        colors = cardColors(
            containerColor = TraktTheme.colors.commentContainer,
        ),
    ) {
        Column(
            verticalArrangement = spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            header?.let {
                Text(
                    text = it,
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.heading5,
                )
            }
            content()
        }
    }
}

@Composable
private fun MarkdownText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    style: TextStyle = TraktTheme.typography.paragraphSmall.copy(
        lineHeight = RelaxedLineHeight,
    ),
    maxLines: Int = Int.MAX_VALUE,
) {
    val linkColor = TraktTheme.colors.textPrimary
    Text(
        text = remember(text, linkColor) { text.toMarkdownText(linkColor) },
        color = color,
        style = style,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

@PreviewTraktLightDark
@Composable
private fun PreviewVip() {
    TraktThemePreview {
        RealityCheckContent(
            realityCheck = PreviewData.realityCheck,
            vip = true,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun PreviewFree() {
    TraktThemePreview {
        RealityCheckContent(
            realityCheck = PreviewData.realityCheck.copy(detail = null),
            vip = false,
            modifier = Modifier.padding(16.dp),
            upsell = { GetVipView() },
        )
    }
}
