package tv.trakt.trakt.core.mixtape.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.mixtape.MixtapeItem
import tv.trakt.trakt.core.mixtape.MAX_MIXTAPE_PICKS
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.components.buttons.TertiaryButton
import tv.trakt.trakt.ui.theme.TraktTheme

private val SlotWidth = 44.dp
private val SlotHeight = 66.dp
private val SlotShape = RoundedCornerShape(8.dp)
private val DockShape = RoundedCornerShape(24.dp)
private val RemoveSize = 18.dp

@Composable
internal fun MixtapeDock(
    picks: ImmutableList<MixtapeItem>,
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit = {},
    onRemoveClick: (MixtapeItem) -> Unit = {},
    onClearClick: () -> Unit = {},
    onMixClick: () -> Unit = {},
) {
    val hasPicks = picks.isNotEmpty()
    val backgroundColor = TraktTheme.colors.backgroundPrimary
    val gradient = remember(backgroundColor) {
        verticalGradient(
            colors = listOf(
                Color.Transparent,
                backgroundColor.copy(alpha = 0.95F),
                backgroundColor,
            ),
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(gradient)
            .padding(
                start = TraktTheme.spacing.mainPageHorizontalSpace,
                end = TraktTheme.spacing.mainPageHorizontalSpace,
                top = 32.dp,
                bottom = WindowInsets.navigationBars.asPaddingValues()
                    .calculateBottomPadding()
                    .plus(12.dp),
            ),
    ) {
        DockCard(
            picks = picks,
            hasPicks = hasPicks,
            onAddClick = onAddClick,
            onRemoveClick = onRemoveClick,
            onClearClick = onClearClick,
            onMixClick = onMixClick,
        )
    }
}

@Composable
private fun DockCard(
    picks: ImmutableList<MixtapeItem>,
    hasPicks: Boolean,
    onAddClick: () -> Unit,
    onRemoveClick: (MixtapeItem) -> Unit,
    onClearClick: () -> Unit,
    onMixClick: () -> Unit,
) {
    val shadow = Shadow(
        radius = 8.dp,
        color = Color.Black,
        spread = 2.dp,
        alpha = if (TraktTheme.colors.isLight) 0.1F else 0.25F,
    )

    Column(
        verticalArrangement = spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .dropShadow(shape = DockShape, shadow = shadow)
            .background(TraktTheme.colors.navigationContainer, DockShape)
            .padding(16.dp),
    ) {
        Row(
            horizontalArrangement = spacedBy(8.dp),
            verticalAlignment = CenterVertically,
        ) {
            picks.forEach { item ->
                PickSlot(
                    item = item,
                    onRemoveClick = { onRemoveClick(item) },
                )
            }

            if (picks.size < MAX_MIXTAPE_PICKS) {
                AddSlot(
                    first = !hasPicks,
                    onClick = onAddClick,
                )
            }
        }

        Row(
            horizontalArrangement = spacedBy(8.dp),
            verticalAlignment = CenterVertically,
        ) {
            Text(
                text = when {
                    hasPicks -> stringResource(R.string.text_mixtape_pick_count, picks.size.toString())
                    else -> stringResource(R.string.text_mixtape_pick_prompt)
                },
                style = TraktTheme.typography.paragraphSmall,
                color = TraktTheme.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1F),
            )

            if (hasPicks) {
                GhostButton(
                    text = stringResource(R.string.button_text_mixtape_clear),
                    fillWidth = false,
                    onClick = onClearClick,
                )
            }

            TertiaryButton(
                text = stringResource(R.string.button_text_mix_it),
                enabled = hasPicks,
                onClick = onMixClick,
            )
        }
    }
}

@Composable
private fun PickSlot(
    item: MixtapeItem,
    onRemoveClick: () -> Unit,
) {
    Box(
        modifier = Modifier.size(width = SlotWidth, height = SlotHeight),
    ) {
        AsyncImage(
            model = item.images?.getPosterUrl(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(width = SlotWidth, height = SlotHeight)
                .clip(SlotShape)
                .background(TraktTheme.colors.placeholderContainer),
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(2.dp)
                .size(RemoveSize)
                .clip(CircleShape)
                .background(TraktTheme.colors.backgroundPrimary)
                .onClick(onClick = onRemoveClick),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier.size(10.dp),
            )
        }
    }
}

@Composable
private fun AddSlot(
    first: Boolean,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(4.dp, Alignment.CenterVertically),
        modifier = Modifier
            .size(width = SlotWidth, height = SlotHeight)
            .border(1.dp, TraktTheme.colors.chipContainer, SlotShape)
            .onClick(onClick = onClick),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_plus),
            contentDescription = null,
            tint = TraktTheme.colors.textSecondary,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = stringResource(
                if (first) R.string.text_mixtape_slot_first else R.string.text_mixtape_slot_next,
            ),
            style = TraktTheme.typography.meta,
            color = TraktTheme.colors.textSecondary,
            maxLines = 1,
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        MixtapeDock(picks = persistentListOf())
    }
}
