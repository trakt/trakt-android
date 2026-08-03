package tv.trakt.trakt.core.lists.sections.personal.ui

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.DevicePreview
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType.Collaborations
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType.Smart
import tv.trakt.trakt.ui.components.chips.FilterChip
import tv.trakt.trakt.ui.components.chips.FilterChipGroup
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ListsFilters(
    modifier: Modifier = Modifier,
    options: ImmutableList<PersonalListType> = PersonalListType.entries.toImmutableList(),
    selected: PersonalListType? = null,
    height: Dp = 28.dp,
    paddingHorizontal: PaddingValues = PaddingValues.Zero,
    paddingVertical: PaddingValues = PaddingValues.Zero,
    onClick: (PersonalListType) -> Unit = { _ -> },
) {
    val initialSelected = remember {
        mutableStateOf(selected)
    }

    LaunchedEffect(selected) {
        if (selected != null) {
            initialSelected.value = selected
        }
    }

    FilterChipGroup(
        paddingHorizontal = paddingHorizontal,
        paddingVertical = paddingVertical,
        modifier = modifier,
    ) {
        for (filter in options) {
            FilterChip(
                selected = selected == filter,
                animated = initialSelected.value != null,
                text = stringResource(filter.displayRes),
                height = height,
                unselectedTextVisible = true,
                leadingContent = {
                    Icon(
                        painter = painterResource(filter.displayIcon),
                        contentDescription = null,
                        tint = TraktTheme.colors.textPrimary,
                        modifier = Modifier.size(
                            when (filter) {
                                Smart -> 18.dp
                                Collaborations -> 15.dp
                                else -> 16.dp
                            },
                        ),
                    )
                },
                onClick = {
                    onClick(filter)
                },
            )
        }
    }
}

@DevicePreview
@Composable
private fun Preview() {
    TraktTheme {
        Column(
            verticalArrangement = spacedBy(12.dp),
        ) {
            ListsFilters(
                selected = null,
            )
            for (type in PersonalListType.entries) {
                ListsFilters(
                    selected = type,
                )
            }
        }
    }
}
