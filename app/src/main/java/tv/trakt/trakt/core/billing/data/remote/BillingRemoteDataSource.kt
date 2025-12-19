package tv.trakt.trakt.core.billing.data.remote

import tv.trakt.trakt.core.billing.data.remote.model.VerifyPurchaseRequest

interface BillingRemoteDataSource {
    suspend fun verifyPurchase(request: VerifyPurchaseRequest)
}
