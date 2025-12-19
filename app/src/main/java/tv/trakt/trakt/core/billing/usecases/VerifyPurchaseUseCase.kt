package tv.trakt.trakt.core.billing.usecases

import com.android.billingclient.api.Purchase
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.core.billing.data.remote.BillingRemoteDataSource
import tv.trakt.trakt.core.billing.data.remote.model.VerifyPurchaseRequest
import tv.trakt.trakt.core.billing.model.PendingPurchaseProduct
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
internal class VerifyPurchaseUseCase(
    private val remoteSource: BillingRemoteDataSource,
    private val sessionManager: SessionManager,
) {
    suspend fun verifyPurchase(
        purchase: Purchase,
        pendingPurchaseProduct: PendingPurchaseProduct?,
    ) {
        val user = sessionManager.getProfile()
            ?: throw IllegalStateException("User not authenticated")

        val product = purchase.products.firstOrNull()
            ?: throw IllegalStateException("Purchase has no products")

        pendingPurchaseProduct?.let {
            check(it.skuId == product) {
                "Product ID mismatch: expected=${it.skuId}, actual=$product"
            }
        }

        val verifyRequest = VerifyPurchaseRequest(
            userId = user.ids.trakt.value,
            productId = product,
            transactionId = purchase.orderId ?: "",
            purchaseToken = purchase.purchaseToken,
            date = Instant.fromEpochMilliseconds(purchase.purchaseTime).toString(),
            currencyCode = pendingPurchaseProduct?.currencyCode,
            price = pendingPurchaseProduct?.price,
            countryCode = null,
            receipt = null,
        )

        remoteSource.verifyPurchase(verifyRequest)
    }
}
