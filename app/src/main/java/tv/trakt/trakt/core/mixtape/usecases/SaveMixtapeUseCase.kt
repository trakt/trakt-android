package tv.trakt.trakt.core.mixtape.usecases

import tv.trakt.trakt.common.core.lists.usecases.AddPersonalListItemUseCase
import tv.trakt.trakt.common.core.lists.usecases.RemovePersonalListItemUseCase
import tv.trakt.trakt.common.core.sync.changes.DataChange.Kind
import tv.trakt.trakt.common.core.sync.changes.DataChanges
import tv.trakt.trakt.common.core.user.usecases.lists.LoadUserListsUseCase
import tv.trakt.trakt.common.model.mixtape.MixtapeItem
import tv.trakt.trakt.common.model.mixtape.MixtapeItem.MovieItem
import tv.trakt.trakt.common.model.mixtape.MixtapeItem.ShowItem
import tv.trakt.trakt.core.mixtape.model.MixtapeTarget
import tv.trakt.trakt.core.mixtape.model.MixtapeTarget.PersonalList
import tv.trakt.trakt.core.mixtape.model.MixtapeTarget.Watchlist
import tv.trakt.trakt.core.sync.usecases.UpdateMovieWatchlistUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateShowWatchlistUseCase

internal class SaveMixtapeUseCase(
    private val updateMovieWatchlistUseCase: UpdateMovieWatchlistUseCase,
    private val updateShowWatchlistUseCase: UpdateShowWatchlistUseCase,
    private val addListItemUseCase: AddPersonalListItemUseCase,
    private val removeListItemUseCase: RemovePersonalListItemUseCase,
    private val loadListsUseCase: LoadUserListsUseCase,
    private val dataChanges: DataChanges,
) {
    suspend fun keep(
        target: MixtapeTarget,
        item: MixtapeItem,
    ) {
        add(target, listOf(item))
    }

    /**
     * @return the items that were not in [to] before, so an undo removes only those.
     */
    suspend fun move(
        from: MixtapeTarget,
        to: MixtapeTarget,
        items: List<MixtapeItem>,
    ): List<MixtapeItem> {
        val added = notYetIn(to, items)
        add(to, items)
        remove(from, items)
        return added
    }

    suspend fun undoMove(
        from: MixtapeTarget,
        to: MixtapeTarget,
        items: List<MixtapeItem>,
        added: List<MixtapeItem>,
    ) {
        add(from, items)
        remove(to, added)
    }

    private suspend fun add(
        target: MixtapeTarget,
        items: List<MixtapeItem>,
    ) {
        when (target) {
            Watchlist -> {
                items.forEach {
                    when (it) {
                        is MovieItem -> updateMovieWatchlistUseCase.addToWatchlist(it.movie)
                        is ShowItem -> updateShowWatchlistUseCase.addToWatchlist(it.show)
                    }
                }
            }
            is PersonalList -> {
                addListItemUseCase.addItems(
                    listId = target.id,
                    ownerId = target.ownerId,
                    movieIds = items.filterIsInstance<MovieItem>().map { it.id },
                    showIds = items.filterIsInstance<ShowItem>().map { it.id },
                )
                dataChanges.notify(Kind.Lists)
            }
        }
    }

    private suspend fun remove(
        target: MixtapeTarget,
        items: List<MixtapeItem>,
    ) {
        if (items.isEmpty()) return

        when (target) {
            Watchlist -> {
                items.forEach {
                    when (it) {
                        is MovieItem -> updateMovieWatchlistUseCase.removeFromWatchlist(it.id)
                        is ShowItem -> updateShowWatchlistUseCase.removeFromWatchlist(it.id)
                    }
                }
            }
            is PersonalList -> {
                items.forEach {
                    when (it) {
                        is MovieItem -> removeListItemUseCase.removeMovie(target.id, target.ownerId, it.id)
                        is ShowItem -> removeListItemUseCase.removeShow(target.id, target.ownerId, it.id)
                    }
                }
                dataChanges.notify(Kind.Lists)
            }
        }
    }

    private suspend fun notYetIn(
        target: MixtapeTarget,
        items: List<MixtapeItem>,
    ): List<MixtapeItem> {
        return when (target) {
            Watchlist -> items
            is PersonalList -> items.filterNot {
                val lists = when (it) {
                    is MovieItem -> loadListsUseCase.loadMovieLists(it.id)
                    is ShowItem -> loadListsUseCase.loadShowLists(it.id)
                }
                target.id in lists
            }
        }
    }
}
