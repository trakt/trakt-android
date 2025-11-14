package tv.trakt.trakt.core.search

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.search.model.SearchInput
import tv.trakt.trakt.core.search.model.SearchItem
import tv.trakt.trakt.core.user.UserCollectionState

@Immutable
internal data class SearchState(
    val input: SearchInput = SearchInput(),
    val state: State = State.IDLE,
    val popularResults: SearchResult? = null,
    val searchResult: SearchResult? = null,
    val navigateShow: Show? = null,
    val navigateMovie: Movie? = null,
    val navigatePerson: Person? = null,
    val collection: UserCollectionState = UserCollectionState.Default,
    val user: UserState = UserState(),
    val searching: Boolean = false,
    val error: Exception? = null,
) {
    data class UserState(
        val user: User? = null,
        val loading: LoadingState = LoadingState.IDLE,
    ) {
        val isAuthenticated: Boolean
            get() = user != null
    }

    data class SearchResult(
        val items: ImmutableList<SearchItem>? = null,
    )

    enum class State {
        IDLE,
        SEARCH_RESULTS,
        ERROR,
    }
}
