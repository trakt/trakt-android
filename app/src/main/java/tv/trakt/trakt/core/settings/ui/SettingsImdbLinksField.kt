package tv.trakt.trakt.core.settings.ui

import android.content.Context
import android.content.Intent
import android.content.pm.verify.domain.DomainVerificationManager
import android.content.pm.verify.domain.DomainVerificationUserState.DOMAIN_STATE_NONE
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle.Event.ON_RESUME
import androidx.lifecycle.compose.LifecycleEventEffect
import timber.log.Timber
import tv.trakt.trakt.resources.R

private const val IMDB_HOST = "www.imdb.com"

@RequiresApi(Build.VERSION_CODES.S)
private fun isImdbLinkHandlingEnabled(context: Context): Boolean {
    val manager = context.getSystemService(DomainVerificationManager::class.java) ?: return false
    val userState = try {
        manager.getDomainVerificationUserState(context.packageName)
    } catch (error: Exception) {
        Timber.w(error, "Unable to read domain verification state")
        null
    }
    val hostState = userState?.hostToStateMap?.get(IMDB_HOST) ?: DOMAIN_STATE_NONE
    return hostState != DOMAIN_STATE_NONE
}

@RequiresApi(Build.VERSION_CODES.S)
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
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        return
    }

    val context = LocalContext.current
    var checked by remember { mutableStateOf(isImdbLinkHandlingEnabled(context)) }

    LifecycleEventEffect(ON_RESUME) {
        checked = isImdbLinkHandlingEnabled(context)
    }

    SettingsSwitchField(
        text = stringResource(R.string.text_settings_open_imdb_links),
        description = stringResource(R.string.text_settings_open_imdb_links_description),
        checked = checked,
        enabled = enabled,
        onClick = { openLinkHandlingSettings(context) },
        modifier = modifier,
    )
}
