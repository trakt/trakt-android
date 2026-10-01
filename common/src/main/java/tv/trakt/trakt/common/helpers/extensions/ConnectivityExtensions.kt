package tv.trakt.trakt.common.helpers.extensions

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED

/**
 * Checks if the device has a network connection with validated internet access.
 */
fun Context.isOnline(): Boolean {
    val connectivityManager = getSystemService(ConnectivityManager::class.java) ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(
        connectivityManager.activeNetwork,
    ) ?: return false

    return capabilities.hasCapability(NET_CAPABILITY_INTERNET) &&
        capabilities.hasCapability(NET_CAPABILITY_VALIDATED)
}
