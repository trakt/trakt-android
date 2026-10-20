package tv.trakt.trakt.core.billing.model

import com.android.billingclient.api.BillingClient.BillingResponseCode
import tv.trakt.trakt.common.helpers.extensions.getHttpCode
import java.io.IOException

/**
 * Short analytics code for a failed VIP purchase or verification. Never carries a message.
 */
internal fun Exception.toVipFailureCode(): String {
    val httpCode = getHttpCode()
    return when {
        this is FeatureNotSupportedError -> "feature_not_supported"
        this is VipBillingError -> code?.let(::toBillingCode) ?: "billing_error"
        httpCode != null -> "http_$httpCode"
        this is IOException -> "network"
        else -> "unknown"
    }
}

private fun toBillingCode(code: Int): String {
    return when (code) {
        BillingResponseCode.FEATURE_NOT_SUPPORTED -> "feature_not_supported"
        BillingResponseCode.SERVICE_DISCONNECTED -> "service_disconnected"
        BillingResponseCode.SERVICE_UNAVAILABLE -> "service_unavailable"
        BillingResponseCode.BILLING_UNAVAILABLE -> "billing_unavailable"
        BillingResponseCode.ITEM_UNAVAILABLE -> "item_unavailable"
        BillingResponseCode.DEVELOPER_ERROR -> "developer_error"
        BillingResponseCode.ITEM_ALREADY_OWNED -> "item_already_owned"
        BillingResponseCode.ITEM_NOT_OWNED -> "item_not_owned"
        BillingResponseCode.NETWORK_ERROR -> "network"
        else -> "billing_error"
    }
}
