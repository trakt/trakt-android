package tv.trakt.trakt.common.helpers.streamingservices

enum class StreamingServiceApp(
    val packageId: String,
) {
    AMAZON("com.amazon.avod.thirdpartyclient"),
    NETFLIX("com.netflix.mediaclient"),
    APPLE("com.apple.atve.androidtv.appletv"),
    HBO("com.wbd.stream"),
    HULU("com.hulu.plus"),
    DISNEY("com.disney.disneyplus"),
    PEACOCK("com.peacocktv.peacockandroid"),
    PARAMOUNT("com.cbs.app"),
    ESPN("com.espn.score_center"),
    AMC("com.amcplus.amcfullepisodes"),
    RAKUTEN("tv.wuaki"),
    STARZ("com.bydeluxe.d3.android.program.starz"),
    YOUTUBE("com.google.android.apps.youtube.unplugged"),
    SLING("com.sling"),
    DISCOVERY("com.discovery.discoveryplus.mobile"),
    BRITBOX("com.britbox.us"),
    FUBO("tv.fubo.mobile"),
    VIX("com.batanga.vix"),
    CURIOSITYSTREAM("com.curiosity.curiositystream"),
    DAZN("com.dazn"),
    CRUNCHYROLL("com.crunchyroll.crunchyroid"),
    TUBI("com.tubitv"),
    PLUTO("tv.pluto.android"),
    ROKU("com.roku.web.trc"),
    FREEVEE("com.amazon.imdb.tv"),
    SHUDDER("com.dramafever.shudder"),
    ACORN("com.acorn.tv"),
    PHILO("com.philo.philo.google"),
    KANOPY("com.kanopy"),
    VUDU("air.com.vudu.air.DownloaderTablet"),
    HALLMARK("com.feeln.android"),
    FANDANGO("air.com.vudu.air.DownloaderTablet"),
    PLEX("com.plexapp.android"),
    TNT("com.turner.tnt.android.networkapp"),
    ;

    companion object Companion {
        fun findFromSource(source: String): StreamingServiceApp? {
            return entries.firstOrNull {
                source.replace("_", "").contains(it.name, ignoreCase = true) ||
                    source.contains(it.packageId, ignoreCase = true)
            }
        }
    }
}
