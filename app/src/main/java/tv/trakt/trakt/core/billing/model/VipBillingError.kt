package tv.trakt.trakt.core.billing.model

import com.android.billingclient.api.BillingClient.BillingResponseCode
import tv.trakt.trakt.resources.R

/*
 *         BillingResponseCode
 *
 *         @Deprecated
 *         int SERVICE_TIMEOUT = -3;
 *         int FEATURE_NOT_SUPPORTED = -2;
 *         int SERVICE_DISCONNECTED = -1;
 *         int OK = 0;
 *         int USER_CANCELED = 1;
 *         int SERVICE_UNAVAILABLE = 2;
 *         int BILLING_UNAVAILABLE = 3;
 *         int ITEM_UNAVAILABLE = 4;
 *         int DEVELOPER_ERROR = 5;
 *         int ERROR = 6;
 *         int ITEM_ALREADY_OWNED = 7;
 *         int ITEM_NOT_OWNED = 8;
 *         int NETWORK_ERROR = 12;
 *     }
 */

sealed class VipBillingError : Exception() {
    abstract val code: Int?
    abstract val displayErrorRes: Int

    companion object {
        fun fromBillingResponseCode(code: Int): VipBillingError {
            return when (code) {
                BillingResponseCode.USER_CANCELED -> CancelledError(code)
                BillingResponseCode.FEATURE_NOT_SUPPORTED -> FeatureNotSupportedError()
                BillingResponseCode.ITEM_ALREADY_OWNED -> AlreadyOwnedError(code)
                BillingResponseCode.ITEM_UNAVAILABLE -> ItemUnavailableError(code)
                else -> OtherBillingError(code)
            }
        }
    }
}

class PendingPaymentError : VipBillingError() {
    override val code = null
    override val displayErrorRes = R.string.error_text_payment_pending
}

class FeatureNotSupportedError : VipBillingError() {
    override val code = null
    override val displayErrorRes = R.string.error_text_payment_feature_not_supported
}

class AlreadyOwnedError(
    override val code: Int?,
) : VipBillingError() {
    override val displayErrorRes = R.string.error_text_payment_already_owned
}

class ItemUnavailableError(
    override val code: Int?,
) : VipBillingError() {
    override val displayErrorRes = R.string.error_text_payment_item_unavailable
}

class CancelledError(
    override val code: Int?,
) : VipBillingError() {
    override val displayErrorRes = R.string.error_text_payment_canceled
}

class OtherBillingError(
    override val code: Int?,
) : VipBillingError() {
    override val displayErrorRes = R.string.error_text_payment_other
}
