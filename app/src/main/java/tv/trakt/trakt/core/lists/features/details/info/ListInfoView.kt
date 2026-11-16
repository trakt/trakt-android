package tv.trakt.trakt.core.lists.features.details.info

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W600
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.toMarkdownText
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Images.Size.THUMB
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.InputField
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard
import tv.trakt.trakt.ui.components.mediacards.skeletons.PanelMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

private const val DESCRIPTION_MAX_LINES = 5
private const val SKELETON_COUNT = 4

private val CardShape = RoundedCornerShape(16.dp)

@Composable
internal fun ListInfoView(
    list: CustomList,
    viewModel: ListInfoViewModel,
    modifier: Modifier = Modifier,
    onPersonClick: (Person) -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ListInfoContent(
        list = list,
        state = state,
        onPersonClick = onPersonClick,
        modifier = modifier,
    )
}

@Composable
private fun ListInfoContent(
    list: CustomList,
    state: ListInfoState,
    modifier: Modifier = Modifier,
    onPersonClick: (Person) -> Unit = {},
) {
    val searchState = rememberTextFieldState()
    val query = searchState.text.toString().trim()
    val people = state.people

    val visiblePeople = remember(people, query) {
        people.orEmpty().filter { it.name.contains(query, ignoreCase = true) }
    }

    LazyColumn(
        verticalArrangement = spacedBy(TraktTheme.spacing.contextItemsSpace),
        modifier = modifier,
    ) {
        item(key = "header") {
            TraktHeader(
                title = stringResource(R.string.header_details),
                subtitle = list.name,
            )
        }

        item(key = "description") {
            DescriptionSection(
                description = list.description?.trim(),
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        item(key = "featured-title") {
            SectionTitle(
                text = stringResource(R.string.list_title_featured_people),
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        when {
            people == null || (state.loading.isLoading && people.isEmpty()) -> {
                items(count = SKELETON_COUNT) {
                    PanelMediaSkeletonCard(
                        imageWidth = TraktTheme.size.verticalSmallMediaCardSize,
                    )
                }
            }

            people.isEmpty() -> {
                item(key = "featured-empty") {
                    FeaturedPeopleEmpty()
                }
            }

            else -> {
                item(key = "featured-search") {
                    InputField(
                        state = searchState,
                        border = 1.dp,
                        containerColor = Color.Transparent,
                        icon = painterResource(R.drawable.ic_search_off),
                        placeholder = stringResource(R.string.input_placeholder_search_credit_members),
                        endSlot = {
                            if (searchState.text.isNotBlank()) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_close),
                                    contentDescription = null,
                                    tint = TraktTheme.colors.textSecondary,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .onClick { searchState.clearText() },
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                if (visiblePeople.isEmpty()) {
                    item(key = "featured-no-results") {
                        Text(
                            text = stringResource(R.string.list_placeholder_no_filter_results),
                            color = TraktTheme.colors.textSecondary,
                            style = TraktTheme.typography.paragraphSmaller,
                        )
                    }
                }

                items(
                    items = visiblePeople,
                    key = { it.ids.trakt.value },
                ) { person ->
                    FeaturedPersonItem(
                        person = person,
                        onClick = { onPersonClick(person) },
                        modifier = Modifier.animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = TraktTheme.typography.paragraphSmaller.copy(fontWeight = W600),
        color = TraktTheme.colors.textSecondary,
        modifier = modifier,
    )
}

@Composable
private fun DescriptionSection(
    description: String?,
    modifier: Modifier = Modifier,
) {
    var collapsed by remember { mutableStateOf(true) }

    val linkColor = TraktTheme.colors.textPrimary
    val markdownText = remember(description, linkColor) {
        (description ?: "").toMarkdownText(linkColor)
    }

    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.contextItemsSpace),
        modifier = modifier,
    ) {
        SectionTitle(text = stringResource(R.string.header_list_description))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(TraktTheme.colors.panelCardContainer, CardShape)
                .padding(16.dp),
        ) {
            if (description.isNullOrBlank()) {
                Text(
                    text = stringResource(R.string.text_list_no_description),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.paragraphSmaller,
                )
            } else {
                Text(
                    text = markdownText,
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.paragraphSmaller,
                    maxLines = if (collapsed) DESCRIPTION_MAX_LINES else Int.MAX_VALUE,
                    overflow = Ellipsis,
                    modifier = Modifier.onClick { collapsed = !collapsed },
                )
            }
        }
    }
}

@Composable
private fun FeaturedPeopleEmpty(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(TraktTheme.spacing.contextItemsSpace),
        modifier = modifier
            .fillMaxWidth()
            .background(TraktTheme.colors.panelCardContainer, CardShape)
            .padding(16.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_person_double),
            contentDescription = null,
            tint = TraktTheme.colors.textSecondary,
            modifier = Modifier
                .background(TraktTheme.colors.textSecondary.copy(alpha = 0.1F), CircleShape)
                .padding(10.dp)
                .size(20.dp),
        )

        Column(
            verticalArrangement = spacedBy(2.dp),
        ) {
            Text(
                text = stringResource(R.string.text_no_featured_people),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.paragraphSmaller.copy(fontWeight = W600),
            )
            Text(
                text = stringResource(R.string.text_featured_people_empty_hint),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.meta,
            )
        }
    }
}

@Composable
private fun FeaturedPersonItem(
    person: Person,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PanelMediaCard(
        title = person.name,
        titleOriginal = null,
        subtitle = person.knownForDepartment
            ?.replaceFirstChar { it.uppercaseChar() }
            ?: "",
        contentImageUrl = person.images?.getHeadshotUrl(THUMB),
        containerImageUrl = null,
        imageWidth = TraktTheme.size.verticalSmallMediaCardSize,
        more = false,
        onClick = onClick,
        onImageClick = onClick,
        modifier = modifier,
    )
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        ListInfoContent(
            list = PreviewData.customList1,
            state = ListInfoState(
                people = persistentListOf(PreviewData.person1, PreviewData.person2),
                loading = LoadingState.Done,
            ),
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun PreviewEmpty() {
    TraktThemePreview {
        ListInfoContent(
            list = PreviewData.customList1.copy(description = null),
            state = ListInfoState(
                people = persistentListOf(),
                loading = LoadingState.Done,
            ),
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun PreviewLoading() {
    TraktThemePreview {
        ListInfoContent(
            list = PreviewData.customList1,
            state = ListInfoState(
                people = null,
                loading = LoadingState.Loading,
            ),
        )
    }
}
