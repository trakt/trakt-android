package tv.trakt.trakt.core.settings.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import timber.log.Timber
import tv.trakt.trakt.resources.R

private fun openLinkHandlingSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS,
        "package:${context.packageName}".toUri(),
    )

    try {
        context.startActivity(intent)
    } catch (error: Exception) {
        Timber.w(error, "Unable to open link handling settings")
    }
}

@Composable
internal fun SettingsImdbLinksField(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val context = LocalContext.current

    SettingsTextField(
        text = stringResource(R.string.text_settings_open_imdb_links),
        description = stringResource(R.string.text_settings_open_imdb_links_description),
        enabled = enabled,
        onClick = { openLinkHandlingSettings(context) },
        modifier = modifier,
    )
}
