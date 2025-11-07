package tv.trakt.trakt.common.helpers.extensions

import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

/**
 * This function launches a new coroutine for each element in the iterable to apply the transformation.
 * It then waits for all transformations to complete before returning the resulting list.
 *
 * @param A The type of elements in the original iterable.
 * @param B The type of elements in the resulting list.
 * @param f The suspending transformation function to apply to each element.
 * @return A [List] containing the results of applying the transformation function to each element
 *         of the original iterable.
 */
suspend fun <A, B> Iterable<A>.asyncMap(f: suspend (A) -> B): List<B> {
    return withContext(Dispatchers.Default) {
        map { async { f(it) } }.awaitAll()
    }
}

fun <T> Iterable<Iterable<T>>.interleave(): List<T> {
    val result = ArrayList<T>()
    val iterators = this.map { it.iterator() }

    do {
        var hasMore = false
        for (i in iterators) {
            if (i.hasNext()) {
                result.add(i.next())
                hasMore = true
            }
        }
    } while (hasMore)

    return result
}

val EmptyImmutableList = emptyList<Nothing>().toImmutableList()
