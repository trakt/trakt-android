package tv.trakt.trakt.core.billing.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerifyPurchaseRequest(
    @SerialName("transaction_id")
    val transactionId: String,
    @SerialName("purchase_token")
    val purchaseToken: String,
    @SerialName("transaction_date")
    val date: String,
    @SerialName("product_id")
    val productId: String,
    @SerialName("user_id")
    val userId: Int,
    @SerialName("currency")
    val currencyCode: String?,
    @SerialName("country")
    val countryCode: String?,
    val price: Long?,
    val receipt: String?,
)
