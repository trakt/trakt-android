package tv.trakt.trakt.core.mixtape.results.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.model.mixtape.MixtapeItem

private const val UNKNOWN_DURATION = "N/A"
private const val MAX_GENRES = 2

@Composable
internal fun rememberMixtapeMeta(item: MixtapeItem): String {
    val runtime = rememberDurationFormat(item.runtime?.inWholeMinutes)
    val genres = item.genres
        .take(MAX_GENRES)
        .map { stringResource(it.displayStringRes) }
        .joinToString(", ")

    return listOfNotNull(
        item.year?.toString(),
        runtime.takeIf { it != UNKNOWN_DURATION },
        genres.ifEmpty { null },
    ).joinToString("  •  ")
}
