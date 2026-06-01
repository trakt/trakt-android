package tv.trakt.trakt.core.summary.sentiment.usecases

import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.Sentiments
import tv.trakt.trakt.common.model.Sentiments.Overall.Positive
import tv.trakt.trakt.common.model.Sentiments.Theme

internal class GetMockSentimentUseCase {
    fun getMockSentiments(): Sentiments {
        return Sentiments(
            overall = Positive,
            analysis = "\"Star Wars\" is a captivating space opera that revitalized classic storytelling tropes " +
                "with groundbreaking special effects. The film masterfully blends elements of adventure, fantasy, " +
                "and science fiction, appealing to a broad audience. The film's overall execution and imaginative " +
                "world-building have cemented its place as a cinematic landmark.",
            highlight = "Features memorable performances and George Lucas's direction " +
                "brought a unique vision to the screen.",
            pros = listOf(
                "Groundbreaking visual effects",
                "Revitalized classic storytelling",
                "Iconic and enhancing score",
                "Remarkable set and creature designs",
                "Universally appealing hero's journey",
            )
                .map { Theme(value = it, confidence = 1F) }
                .toImmutableList(),
            cons = listOf(
                "Simplistic dialogue and plot",
                "Thinly drawn characters",
                "Emotionally exhausting",
            )
                .map { Theme(value = it, confidence = 1F) }
                .toImmutableList(),
        )
    }
}
