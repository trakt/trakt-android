package tv.trakt.trakt.core.billing.model

enum class VipBillingOffer(
    val id: String,
    val tag: String,
) {
    MONTHLY_STANDARD(
        id = "monthly-standard",
        tag = "monthly-base-plan",
    ),
    MONTHLY_STANDARD_TRIAL(
        id = "1-month-free-trial",
        tag = "monthly-trial-offer",
    ),
}
