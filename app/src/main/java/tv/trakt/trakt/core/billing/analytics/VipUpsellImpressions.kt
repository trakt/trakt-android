package tv.trakt.trakt.core.billing.analytics

import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.core.billing.model.VipUpsellSource
import java.util.concurrent.atomic.AtomicBoolean

internal class VipUpsellImpressions(
    private val analytics: Analytics,
) {
    private val navbarLogged = AtomicBoolean(false)

    fun log(source: VipUpsellSource) {
        // The navbar pill is the same upsell on every main tab, so it counts once per app session.
        if (source == VipUpsellSource.NavbarMobile && !navbarLogged.compareAndSet(false, true)) return

        analytics.vip.logUpsellImpression(source.value)
    }
}
