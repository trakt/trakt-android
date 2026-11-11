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
    ShowDetails("show-details"),
    MovieDetails("movie-details"),
    EpisodeDetails("episode-details"),
    Soundtrack("soundtrack"),
    Trivia("trivia"),
    Sentiment("sentiment"),
    RealityCheck("reality-check"),
    ConnectedApps("connected-apps"),
    Onboarding("onboarding"),
    VipLimit("vip-limit"),
}
