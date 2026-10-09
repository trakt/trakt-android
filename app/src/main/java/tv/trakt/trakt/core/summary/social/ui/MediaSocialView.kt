package tv.trakt.trakt.core.summary.social.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.core.summary.social.model.MediaSocialActivity
import tv.trakt.trakt.core.summary.social.recommendedby.RecommendedByViewModel
import tv.trakt.trakt.core.summary.social.recommendedby.recommendedByViewModelKey
import tv.trakt.trakt.core.summary.ui.header.social.DetailsSocialChip
import tv.trakt.trakt.core.summary.ui.header.social.SocialChipShare
import tv.trakt.trakt.core.summary.ui.header.social.socialChipUsers

/**
 * @param recommendedByPath Relative web path of the item, e.g. `/shows/<slug>/seasons/1/episodes/1`.
 */
@Composable
internal fun MediaSocialView(
    modifier: Modifier = Modifier,
    visible: Boolean,
    activity: ImmutableList<MediaSocialActivity>?,
    recommendedByPath: String?,
    onActivityClick: () -> Unit,
) {
    val recommendedByViewModel: RecommendedByViewModel? = when {
        LocalInspectionMode.current || recommendedByPath == null -> null
        else -> koinViewModel(
            key = recommendedByViewModelKey(recommendedByPath),
            parameters = { parametersOf(recommendedByPath) },
        )
    }
    val recommendedByState = recommendedByViewModel?.state?.collectAsStateWithLifecycle()
    val recommendedBy = recommendedByState?.value?.recommendedBy
    val isLoaded = activity != null &&
        recommendedByState?.value?.loading?.isDone != false

    val share = SocialChipShare.of(recommendedBy, activity)

    Box(
        contentAlignment = Center,
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(200, delayMillis = 250),
            ),
    ) {
        val users = remember(activity?.size, share, recommendedBy) {
            socialChipUsers(share, activity, recommendedBy)
        }
        AnimatedVisibility(
            visible = isLoaded && (visible || share != SocialChipShare.None),
            enter = fadeIn(tween(200, delayMillis = 350)),
            exit = fadeOut(tween(200, delayMillis = 350)),
            modifier = Modifier
                .padding(top = 20.dp)
                .onClick(onClick = onActivityClick),
        ) {
            DetailsSocialChip(
                users = users ?: EmptyImmutableList,
                share = share,
            )
        }
    }
}
