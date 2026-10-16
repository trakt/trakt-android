package tv.trakt.trakt.common.core.billing.data.remote

import tv.trakt.trakt.common.core.billing.data.remote.model.VerifyPurchaseRequest

interface BillingRemoteDataSource {
    suspend fun verifyPurchase(request: VerifyPurchaseRequest)

    suspend fun clear()
}
