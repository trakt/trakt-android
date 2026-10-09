package tv.trakt.trakt.core.summary.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.reactions.MediaReactionsTarget
import tv.trakt.trakt.core.reactions.media.MediaReactionsView
import tv.trakt.trakt.core.reactions.media.MediaReactionsViewModel
import tv.trakt.trakt.core.summary.social.model.MediaSocialActivity
import tv.trakt.trakt.core.summary.social.recommendedby.RecommendedByViewModel
import tv.trakt.trakt.core.summary.social.recommendedby.model.RecommendedBy
import tv.trakt.trakt.core.summary.social.recommendedby.recommendedByViewModelKey
import tv.trakt.trakt.core.summary.ui.header.social.DetailsSocialChip
import tv.trakt.trakt.core.summary.ui.header.social.SocialChipShare
import tv.trakt.trakt.core.summary.ui.header.social.socialChipUsers
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark

private val RowTopSpace = 20.dp
private val RowItemsSpace = 24.dp

private const val ENTER_DELAY_MS = 300

/**
 * @param recommendedByPath Relative web path of the item, e.g. `/movies/<slug>`.
 */
@Composable
internal fun DetailsSocialRow(
    target: MediaReactionsTarget,
    recommendedByPath: String,
    socials: ImmutableList<MediaSocialActivity>?,
    onActivityClick: () -> Unit,
    modifier: Modifier = Modifier,
    previewRecommendedBy: RecommendedBy? = null,
) {
    val isInspection = LocalInspectionMode.current
    val reactionsViewModel: MediaReactionsViewModel? = when {
        isInspection -> null
        else -> koinViewModel(parameters = { parametersOf(target) })
    }
    val reactionsState = reactionsViewModel?.state?.collectAsStateWithLifecycle()

    val recommendedByViewModel: RecommendedByViewModel? = when {
        isInspection -> null
        else -> koinViewModel(
            key = recommendedByViewModelKey(recommendedByPath),
            parameters = { parametersOf(recommendedByPath) },
        )
    }
    val recommendedByState = recommendedByViewModel?.state?.collectAsStateWithLifecycle()
    val recommendedBy = recommendedByState?.value?.recommendedBy ?: previewRecommendedBy

    val isLoaded = socials != null &&
        reactionsState?.value?.loading?.isDone != false &&
        recommendedByState?.value?.loading?.isDone != false

    val share = SocialChipShare.of(recommendedBy, socials)
    val users = remember(socials?.size, share, recommendedBy) {
        socialChipUsers(share, socials, recommendedBy)
    }

    Box(
        contentAlignment = Center,
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(200, delayMillis = 250),
            ),
    ) {
        Row(
            horizontalArrangement = spacedBy(RowItemsSpace),
            verticalAlignment = CenterVertically,
        ) {
            reactionsViewModel?.let {
                MediaReactionsView(
                    viewModel = it,
                    visible = isLoaded,
                    modifier = Modifier.padding(top = RowTopSpace),
                )
            }

            AnimatedVisibility(
                visible = isLoaded && (!socials.isEmpty() || share != SocialChipShare.None),
                enter = fadeIn(tween(200, delayMillis = ENTER_DELAY_MS)),
                exit = fadeOut(tween(200, delayMillis = ENTER_DELAY_MS)),
                modifier = Modifier
                    .padding(top = RowTopSpace)
                    .onClick(onClick = onActivityClick),
            ) {
                DetailsSocialChip(
                    users = users ?: EmptyImmutableList,
                    share = share,
                )
            }
        }
    }
}

@Preview(widthDp = 360)
@Composable
private fun Preview() {
    TraktThemeLightDark {
        DetailsSocialRow(
            target = PreviewTarget,
            recommendedByPath = "/movies/preview",
            socials = persistentListOf(PreviewActivity),
            onActivityClick = {},
        )
    }
}

@Preview(widthDp = 360)
@Composable
private fun SharedPreview() {
    TraktThemeLightDark {
        DetailsSocialRow(
            target = PreviewTarget,
            recommendedByPath = "/movies/preview",
            socials = persistentListOf(PreviewActivity),
            onActivityClick = {},
            previewRecommendedBy = PreviewRecommendedBy,
        )
    }
}

@Preview(widthDp = 360)
@Composable
private fun SharedOnlyPreview() {
    TraktThemeLightDark {
        DetailsSocialRow(
            target = PreviewTarget,
            recommendedByPath = "/movies/preview",
            socials = EmptyImmutableList,
            onActivityClick = {},
            previewRecommendedBy = PreviewRecommendedBy,
        )
    }
}

private val PreviewTarget = MediaReactionsTarget(
    type = MediaType.Movie,
    id = TraktId(1),
)

private val PreviewActivity = MediaSocialActivity(
    type = MediaType.Movie,
    user = PreviewData.user1,
    watched = null,
    watchlist = null,
)

private val PreviewRecommendedBy = RecommendedBy(
    users = persistentListOf(PreviewData.user1),
    otherCount = 2,
)
