package tv.trakt.trakt.core.mixtape.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.mixtape.Mixtape.Fit
import tv.trakt.trakt.common.model.mixtape.MixtapeVectors
import kotlin.math.sqrt

internal class ScoreMixtapeFitsUseCase {
    /**
     * Returns null while a pick has no vector yet, so callers can tell "not scorable" from "no fits".
     */
    fun scoreLocalFits(
        picks: List<TraktId>,
        candidates: Collection<TraktId>,
        vectors: MixtapeVectors,
    ): ImmutableList<Fit>? {
        val pickVectors = picks.mapNotNull { vectors.vectors[it] }
        if (pickVectors.isEmpty() || pickVectors.size != picks.size) return null

        return candidates
            .asSequence()
            .distinct()
            .filterNot { it in picks }
            .mapNotNull { id ->
                val vector = vectors.vectors[id] ?: return@mapNotNull null
                val score = pickVectors.minOf { cosine(it, vector) }
                Fit(id = id, score = score, item = null).takeIf { score >= vectors.cutoff }
            }
            .sortedByDescending { it.score }
            .toImmutableList()
    }

    fun mergeFits(
        local: List<Fit>,
        deep: List<Fit>,
    ): ImmutableList<Fit> {
        return (deep + local)
            .distinctBy { it.id }
            .sortedByDescending { it.score }
            .toImmutableList()
    }

    fun appendFits(
        local: List<Fit>,
        deep: List<Fit>,
    ): ImmutableList<Fit> {
        return (local + deep)
            .distinctBy { it.id }
            .toImmutableList()
    }

    private fun cosine(
        a: ByteArray,
        b: ByteArray,
    ): Double {
        var dot = 0L
        var normA = 0L
        var normB = 0L
        a.forEachIndexed { index, value ->
            val other = b.getOrElse(index) { 0 }
            dot += value * other
            normA += value * value
            normB += other * other
        }
        if (normA == 0L || normB == 0L) return 0.0
        return dot / sqrt(normA.toDouble() * normB.toDouble())
    }
}
