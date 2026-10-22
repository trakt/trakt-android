package tv.trakt.trakt.core.auth.model

internal enum class LoginSource(
    val value: String,
) {
    OnboardingLogin("landing-login"),
    OnboardingJoin("landing-get-started"),
    HeaderBar("navbar"),
    Gated("redirect"),
}
