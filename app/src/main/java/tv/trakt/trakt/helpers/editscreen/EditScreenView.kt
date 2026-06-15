@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.helpers.editscreen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.toImmutableMap
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.helpers.editscreen.data.model.EditScreenKey
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun EditScreenView(
    viewModel: EditScreenViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    EditScreenView(
        state = state,
        onToggle = viewModel::toggle,
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
internal fun EditScreenView(
    state: EditScreenState,
    modifier: Modifier = Modifier,
    onToggle: (EditScreenKey) -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(16.dp),
        modifier = modifier,
    ) {
        TraktHeader(
            title = stringResource(R.string.text_edit_screen),
            subtitle = stringResource(R.string.text_edit_screen_description),
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Column(
            verticalArrangement = spacedBy(10.dp),
        ) {
            state.values?.let {
                it.forEach { (key, visible) ->
                    SectionToggleRow(
                        title = stringResource(key.displayStringRes),
                        visible = visible,
                        onToggle = { onToggle(key) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionToggleRow(
    title: String,
    visible: Boolean,
    onToggle: () -> Unit,
) {
    val animatedAlpha: Float by animateFloatAsState(
        targetValue = if (visible) 1f else 0.25F,
        animationSpec = tween(100),
        label = "alpha",
    )

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .alpha(animatedAlpha)
            .height(42.dp)
            .fillMaxWidth()
            .background(
                color = TraktTheme.colors.dialogOnContainer,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(horizontal = 16.dp)
            .onClick(
                throttle = false,
                onClick = onToggle,
            ),
    ) {
        Text(
            text = title,
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.buttonSecondary,
            maxLines = 1,
            overflow = Ellipsis,
            modifier = Modifier
                .weight(1F)
                .padding(end = 16.dp),
        )

        Icon(
            painter = painterResource(
                when (visible) {
                    true -> R.drawable.ic_eye
                    false -> R.drawable.ic_eye_off
                },
            ),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = TraktTheme.colors.textPrimary,
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun Preview() {
    TraktTheme {
        EditScreenView(
            state = EditScreenState(
                values = mapOf(
                    EditScreenKey.DiscoverPopular to true,
                    EditScreenKey.DiscoverTrending to true,
                ).toImmutableMap(),
            ),
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun PreviewPartial() {
    TraktTheme {
        EditScreenView(
            state = EditScreenState(
                values = mapOf(
                    EditScreenKey.DiscoverTrending to false,
                    EditScreenKey.DiscoverAnticipated to true,
                ).toImmutableMap(),
            ),
        )
    }
}
