package tv.trakt.trakt.core.mixtape.results.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.mixtape.model.MixtapeTarget
import tv.trakt.trakt.core.mixtape.model.MixtapeTarget.PersonalList
import tv.trakt.trakt.core.mixtape.model.MixtapeTarget.Watchlist
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.InputField
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.theme.TraktTheme

private val OptionShape = RoundedCornerShape(12.dp)
private val OptionMinHeight = 52.dp
private val PreviewHeight = 600.dp

@Composable
internal fun MixtapeMoveView(
    current: MixtapeTarget,
    lists: ImmutableList<PersonalList>,
    suggestedName: String,
    moving: Boolean,
    modifier: Modifier = Modifier,
    onStay: () -> Unit = {},
    onMove: (MixtapeTarget) -> Unit = {},
    onMoveNew: (String) -> Unit = {},
) {
    val watchlistName = stringResource(R.string.page_title_watchlist)
    val nameState = rememberTextFieldState(suggestedName)
    var selected by remember { mutableStateOf<MixtapeTarget?>(current) }

    val isNewList = selected == null
    val newName = nameState.text.toString().trim()
    val targetName = when (val target = selected) {
        null -> newName
        Watchlist -> watchlistName
        is PersonalList -> target.name
    }
    val currentName = current.displayName(watchlistName)

    Column(
        verticalArrangement = spacedBy(16.dp),
        modifier = modifier,
    ) {
        TraktHeader(title = stringResource(R.string.header_mixtape_move))

        Column(
            verticalArrangement = spacedBy(8.dp),
            modifier = Modifier
                .weight(1F)
                .verticalScroll(rememberScrollState(), overscrollEffect = null),
        ) {
            (listOf(Watchlist) + lists).forEach { target ->
                MoveOption(
                    name = target.displayName(watchlistName),
                    current = target == current,
                    selected = target == selected,
                    onClick = { selected = target },
                )
            }
            NewListOption(
                selected = isNewList,
                nameState = nameState,
                enabled = !moving,
                onClick = { selected = null },
            )
        }

        PrimaryButton(
            text = when {
                selected == current -> stringResource(R.string.button_text_mixtape_stay, currentName)
                else -> stringResource(R.string.button_text_mixtape_move, targetName)
            },
            enabled = !moving && targetName.isNotBlank(),
            loading = moving,
            onClick = {
                when (val target = selected) {
                    current -> onStay()
                    null -> onMoveNew(newName)
                    else -> onMove(target)
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun MoveOption(
    name: String,
    current: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
) {
    OptionContainer(
        selected = selected,
        onClick = onClick,
    ) {
        Column(
            verticalArrangement = spacedBy(1.dp),
            modifier = Modifier.weight(1F),
        ) {
            Text(
                text = name,
                style = TraktTheme.typography.paragraph,
                color = TraktTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (current) {
                Text(
                    text = stringResource(R.string.text_mixtape_move_current),
                    style = TraktTheme.typography.meta,
                    color = TraktTheme.colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun NewListOption(
    selected: Boolean,
    nameState: TextFieldState,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Column(verticalArrangement = spacedBy(8.dp)) {
        OptionContainer(
            selected = selected,
            onClick = onClick,
        ) {
            Text(
                text = stringResource(R.string.button_text_mixtape_new_list),
                style = TraktTheme.typography.paragraph,
                color = TraktTheme.colors.textPrimary,
                modifier = Modifier.weight(1F),
            )
        }
        if (selected) {
            InputField(
                state = nameState,
                enabled = enabled,
                placeholder = stringResource(R.string.input_label_mixtape_list_name),
                containerColor = Color.Transparent,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun OptionContainer(
    selected: Boolean,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = OptionMinHeight)
            .shadow(elevation = TraktTheme.colors.shadowDynamicSmall, shape = OptionShape)
            .clip(OptionShape)
            .background(TraktTheme.colors.dialogOnContainer)
            .border(
                width = 1.dp,
                color = if (selected) TraktTheme.colors.accent else Color.Transparent,
                shape = OptionShape,
            )
            .onClick(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        content()
        if (selected) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

internal fun MixtapeTarget.displayName(watchlistName: String): String {
    return when (this) {
        Watchlist -> watchlistName
        is PersonalList -> name
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        MixtapeMoveView(
            current = Watchlist,
            lists = persistentListOf(
                PersonalList(id = TraktId(1), ownerId = TraktId(1), name = "Weekend picks"),
                PersonalList(id = TraktId(2), ownerId = TraktId(1), name = "Date night"),
            ),
            suggestedName = "Mixtape: The Pitt + Severance",
            moving = false,
            modifier = Modifier
                .height(PreviewHeight)
                .background(TraktTheme.colors.dialogContainer)
                .padding(24.dp),
        )
    }
}
