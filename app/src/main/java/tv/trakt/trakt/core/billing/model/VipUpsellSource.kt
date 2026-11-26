package tv.trakt.trakt.core.billing.model

internal enum class VipUpsellSource(
    val value: String,
) {
    NavbarMobile("navbar-mobile"),
    Welcome("welcome"),
    WelcomeBanner("welcome-banner"),
    AllTimeStats("all-time-stats"),
    LibraryCollectionLimit("library-collection-limit"),
    LeaderboardViewer("leaderboard-viewer"),
    Soundtrack("soundtrack"),
    Trivia("trivia"),
    Sentiment("sentiment"),
    RealityCheck("reality-check"),
    ConnectedApps("connected-apps"),
    Onboarding("onboarding"),
    VipLimit("vip-limit"),
    PreviewFeatures("preview-features"),
    HomeNudge("home-nudge"),
}
