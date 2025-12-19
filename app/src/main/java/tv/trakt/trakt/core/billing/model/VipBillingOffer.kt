package tv.trakt.trakt.core.billing.model

enum class VipBillingOffer(
    val id: String,
) {
    MONTHLY_STANDARD(
        id = "monthly-standard",
    ),
    MONTHLY_STANDARD_TRIAL(
        id = "1-month-free-trial",
    ),
}
