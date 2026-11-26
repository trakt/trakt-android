package tv.trakt.trakt.core.mixtape.results.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.model.mixtape.MixtapeItem

private const val UNKNOWN_DURATION = "N/A"

@Composable
internal fun rememberMixtapeMeta(item: MixtapeItem): String {
    val runtime = rememberDurationFormat(item.runtime?.inWholeMinutes)
    val genre = item.genre?.let { stringResource(it.displayStringRes) }

    return listOfNotNull(
        item.year?.toString(),
        runtime.takeIf { it != UNKNOWN_DURATION },
        genre,
    ).joinToString(" · ")
}
