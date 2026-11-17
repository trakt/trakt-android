package tv.trakt.trakt.core.summary.ui.views.insights

import android.icu.text.ListFormatter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import tv.trakt.trakt.common.model.realitycheck.DocumentaryCheck
import tv.trakt.trakt.resources.R

@Composable
internal fun documentaryMakersText(documentaryCheck: DocumentaryCheck): String? {
    val locale = LocalConfiguration.current.locales[0]
    val formatter = remember(locale) { ListFormatter.getInstance(locale) }

    val madeBy = documentaryCheck.madeBy
        .takeIf { it.isNotEmpty() }
        ?.let { stringResource(R.string.text_documentary_made_by, formatter.format(it)) }
    val fundedBy = documentaryCheck.fundedBy
        .takeIf { it.isNotEmpty() }
        ?.let { stringResource(R.string.text_documentary_funded_by, formatter.format(it)) }

    return listOfNotNull(madeBy, fundedBy)
        .joinToString(" ")
        .ifBlank { null }
}
