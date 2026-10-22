@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.profile.sections.library.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.ui.theme.colors.LightColors
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.chips.InfoChip
import tv.trakt.trakt.ui.components.vip.VipChip
import tv.trakt.trakt.ui.theme.TraktTheme

private const val TAG_CONTAINER_ALPHA = 0.16F

private val tooltipShape = RoundedCornerShape(8.dp)
private val tagTouchPadding = PaddingValues(vertical = 8.dp)

@Composable
internal fun LibraryCollectionLimitTag(
    servers: ImmutableList<String?>,
    modifier: Modifier = Modifier,
    onVipClick: () -> Unit = {},
    onShow: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val tooltipState = rememberTooltipState(isPersistent = true)

    val fallbackName = stringResource(R.string.label_plex_server)
    val serverNames = servers.joinToString(", ") { it ?: fallbackName }

    TooltipBox(
        state = tooltipState,
        positionProvider = rememberTooltipPositionProvider(
            positioning = TooltipAnchorPosition.Above,
        ),
        tooltip = {
            Column(
                verticalArrangement = spacedBy(10.dp),
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .background(TraktTheme.colors.tooltipContainer, tooltipShape)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Text(
                    text = stringResource(R.string.description_library_collection_limit, serverNames),
                    style = TraktTheme.typography.meta,
                    color = TraktTheme.colors.tooltipContent,
                )
                VipChip(
                    text = stringResource(R.string.badge_text_get_vip),
                    color = TraktTheme.colors.accent,
                    onClick = {
                        tooltipState.dismiss()
                        onVipClick()
                    },
                )
            }
        },
        enableUserInput = false,
        modifier = modifier,
    ) {
        InfoChip(
            iconPainter = painterResource(R.drawable.ic_info),
            iconPadding = 2.dp,
            text = stringResource(R.string.label_library_collection_limit),
            containerColor = TraktTheme.colors.warningAccent.copy(alpha = TAG_CONTAINER_ALPHA),
            contentColor = TraktTheme.colors.warningAccent,
            modifier = Modifier
                .onClick {
                    onShow()
                    scope.launch {
                        tooltipState.show()
                    }
                }
                .padding(tagTouchPadding),
        )
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        LibraryCollectionLimitTag(
            servers = persistentListOf("Living Room", null),
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFF,
)
@Composable
private fun PreviewLight() {
    TraktTheme(
        colors = LightColors,
    ) {
        LibraryCollectionLimitTag(
            servers = persistentListOf("Living Room", null),
        )
    }
}
