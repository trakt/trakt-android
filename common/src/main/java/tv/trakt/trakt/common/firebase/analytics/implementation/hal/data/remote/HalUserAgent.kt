package tv.trakt.trakt.common.firebase.analytics.implementation.hal.data.remote

internal enum class HalDeviceKind {
    Phone,
    Tablet,
    Television,
}

internal fun halUserAgent(
    appVersion: String,
    androidRelease: String,
    deviceKind: HalDeviceKind,
): String {
    val deviceToken = when (deviceKind) {
        HalDeviceKind.Phone -> "; Mobile"
        HalDeviceKind.Tablet -> ""
        HalDeviceKind.Television -> "; TV"
    }
    return "Trakt Android/$appVersion (Linux; Android $androidRelease$deviceToken)"
}
