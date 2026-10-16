package tv.trakt.trakt.core.billing.model

enum class VipBillingProduct(
    val sku: String,
    val plan: String,
) {
    MONTHLY_STANDARD(sku = "monthly_standard", plan = "monthly"),
}
