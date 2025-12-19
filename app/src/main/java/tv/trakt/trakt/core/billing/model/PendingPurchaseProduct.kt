package tv.trakt.trakt.core.billing.model

data class PendingPurchaseProduct(
    val skuId: String,
    val currencyCode: String?,
    val price: Long?,
)
