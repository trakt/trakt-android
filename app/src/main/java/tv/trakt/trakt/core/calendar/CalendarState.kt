package tv.trakt.trakt.core.calendar

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.home.sections.upcoming.model.HomeUpcomingItem
import tv.trakt.trakt.core.main.model.MediaMode
import java.time.Instant

@Immutable
internal data class CalendarState(
    val items: ImmutableMap<Instant, ImmutableList<HomeUpcomingItem>?>? = null,
    val user: User? = null,
    val filter: MediaMode? = null,
    val navigateShow: TraktId? = null,
    val navigateEpisode: Pair<TraktId, Episode>? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
)
