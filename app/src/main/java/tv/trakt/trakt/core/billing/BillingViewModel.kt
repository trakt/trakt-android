@file:OptIn(FlowPreview::class)
@file:Suppress("UNCHECKED_CAST")

package tv.trakt.trakt.core.billing

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
import com.android.billingclient.api.BillingFlowParams.newBuilder
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.Purchase.PurchaseState
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import tv.trakt.trakt.analytics.Analytics
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.billing.model.CancelledError
import tv.trakt.trakt.core.billing.model.PendingPurchaseProduct
import tv.trakt.trakt.core.billing.model.VipBillingError
import tv.trakt.trakt.core.billing.model.VipBillingOffer.MONTHLY_STANDARD
import tv.trakt.trakt.core.billing.model.VipBillingOffer.MONTHLY_STANDARD_TRIAL
import tv.trakt.trakt.core.billing.model.VipBillingProduct
import tv.trakt.trakt.core.billing.usecases.VerifyPurchaseUseCase
import tv.trakt.trakt.core.user.usecases.LoadUserProfileUseCase

internal class BillingViewModel(
    analytics: Analytics,
    private val appContext: Context,
    private val sessionManager: SessionManager,
    private val verifyPurchaseUseCase: VerifyPurchaseUseCase,
    private val loadUserUseCase: LoadUserProfileUseCase,
) : ViewModel() {
    private val initialState = BillingState()

    private val userState = MutableStateFlow(initialState.user)
    private val productsState = MutableStateFlow(initialState.products)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private lateinit var billingClient: BillingClient
    private var pendingPurchaseProduct: PendingPurchaseProduct? = null

    private val purchasesUpdatedListener = PurchasesUpdatedListener { result, purchases ->
        if (result.responseCode == BillingResponseCode.OK) {
            purchases?.forEach { purchase ->
                handlePurchase(purchase)
            }
        } else {
            pendingPurchaseProduct?.let {
                pendingPurchaseProduct = null
                val error = VipBillingError.fromBillingResponseCode(result.responseCode)
                if (error is CancelledError) {
                    Timber.d("Purchase cancelled by user: ${it.skuId}")
                } else {
                    handleError(error = error)
                }
            }
            loadingState.update { LoadingState.DONE }
        }
    }

    private val billingStateListener = object : BillingClientStateListener {
        override fun onBillingSetupFinished(billingResult: BillingResult) {
            loadingState.update { LoadingState.DONE }
            if (billingResult.responseCode == BillingResponseCode.OK) {
                Timber.d("Billing Client setup finished successfully")
                checkPurchases()
            } else {
                val billingError = VipBillingError.fromBillingResponseCode(billingResult.responseCode)
                handleError(billingError)
            }
        }

        override fun onBillingServiceDisconnected() {
            val billingError = VipBillingError.fromBillingResponseCode(BillingResponseCode.SERVICE_DISCONNECTED)
            handleError(billingError)
        }
    }

    init {
        loadUser()
        observeUser()

        initPlayBilling()
        startPlayBilling()

        analytics.logScreenView(
            screenName = "billing",
        )
    }

    private fun loadUser() {
        viewModelScope.launch {
            userState.update {
                sessionManager.getProfile()
            }
        }
    }

    private fun observeUser() {
        sessionManager.observeProfile()
            .distinctUntilChanged()
            .debounce(200)
            .onEach { user ->
                userState.update { user }
            }
            .launchIn(viewModelScope)
    }

    private fun initPlayBilling() {
        billingClient = BillingClient.newBuilder(appContext)
            .setListener(purchasesUpdatedListener)
            .enableAutoServiceReconnection()
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build(),
            )
            .build()
    }

    private fun startPlayBilling() {
        loadingState.update { LoadingState.LOADING }
        billingClient.startConnection(billingStateListener)
        Timber.d("Starting Billing Client connection")
    }

    private fun checkPurchases() {
        viewModelScope.launch {
            try {
                val purchasesResult = withContext(Dispatchers.IO) {
                    billingClient.queryPurchasesAsync(
                        QueryPurchasesParams.newBuilder()
                            .setProductType(ProductType.SUBS)
                            .build(),
                    )
                }

                val responseCode = purchasesResult.billingResult.responseCode
                if (responseCode != BillingResponseCode.OK) {
                    val billingError = VipBillingError.fromBillingResponseCode(responseCode)
                    handleError(billingError)
                    return@launch
                }

                Timber.d("Purchases loaded: ${purchasesResult.purchasesList.size} purchases found")

                if (purchasesResult.purchasesList.isEmpty()) {
                    Timber.d("No purchases found")
                    loadPurchases()
                    return@launch
                }

                val toAcknowledge = purchasesResult.purchasesList
                    .filter {
                        !it.isAcknowledged && it.purchaseState == PurchaseState.PURCHASED
                    }

                if (toAcknowledge.isEmpty()) {
                    loadPurchases()
                } else {
                    viewModelScope.launch {
                        toAcknowledge.forEach {
                            handlePurchaseAsync(it)
                        }
                    }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    handleError(error)
                }
            }
        }
    }

    private fun loadPurchases() {
        viewModelScope.launch {
            val productList = VipBillingProduct.entries.map {
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(it.sku)
                    .setProductType(ProductType.SUBS)
                    .build()
            }

            val params = QueryProductDetailsParams.newBuilder().apply {
                setProductList(productList)
            }

            try {
                val productDetailsResult = withContext(Dispatchers.IO) {
                    billingClient.queryProductDetails(params.build())
                }

                val responseCode = productDetailsResult.billingResult.responseCode
                if (responseCode == BillingResponseCode.OK) {
                    Timber.d("Products details loaded: ${productDetailsResult.productDetailsList?.size} products found")
                    if (productDetailsResult.productDetailsList.isNullOrEmpty()) {
                        Timber.e("No products details found")
                    } else {
                        productsState.update {
                            productDetailsResult.productDetailsList?.toImmutableList()
                        }
                    }
                } else {
                    val billingError = VipBillingError.fromBillingResponseCode(responseCode)
                    handleError(billingError)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    handleError(error)
                }
            }
        }
    }

    fun launchPurchaseFlow(
        activity: Activity,
        product: ProductDetails,
    ) {
        if (pendingPurchaseProduct != null) {
            Timber.w("Another purchase is pending, cannot start a new one")
            return
        }

        val freeTrialOffer = product.subscriptionOfferDetails
            ?.firstOrNull { it.offerId == MONTHLY_STANDARD_TRIAL.id }

        val standardOffer = product.subscriptionOfferDetails
            ?.firstOrNull { it.offerId == MONTHLY_STANDARD.id }

        val offer = when {
            freeTrialOffer != null -> freeTrialOffer
            standardOffer != null -> standardOffer
            else -> product.subscriptionOfferDetails?.lastOrNull()
        }

        val billingFlowParams = newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    ProductDetailsParams.newBuilder()
                        .setProductDetails(product)
                        .setOfferToken(offer?.offerToken ?: "")
                        .build(),
                ),
            )
            .build()

        loadingState.update { LoadingState.LOADING }
        val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)
        val responseCode = billingResult.responseCode

        if (responseCode != BillingResponseCode.OK) {
            val billingError = VipBillingError.fromBillingResponseCode(responseCode)
            handleError(billingError)
            loadingState.update { LoadingState.DONE }
        } else {
            val price = offer?.pricingPhases?.pricingPhaseList?.firstOrNull()
            pendingPurchaseProduct = PendingPurchaseProduct(
                skuId = product.productId,
                currencyCode = price?.priceCurrencyCode,
                price = price?.priceAmountMicros,
            )
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        viewModelScope.launch {
            handlePurchaseAsync(purchase)
        }
    }

    private suspend fun handlePurchaseAsync(purchase: Purchase) {
        if (purchase.purchaseState == PurchaseState.UNSPECIFIED_STATE) {
            // TODO Handle, inform user?
            Timber.e("Purchase in unspecified state: ${purchase.products}")
            return
        }

        if (purchase.purchaseState == PurchaseState.PENDING) {
            // TODO Handle, inform user?
            Timber.w("Purchase is pending: ${purchase.products}")
            return
        }

        try {
            verifyPurchaseUseCase.verifyPurchase(
                purchase = purchase,
                pendingPurchaseProduct = pendingPurchaseProduct,
            )
            Timber.d("Purchase verified successfully: ${purchase.products.firstOrNull()}")

            delay(500)
            loadUserUseCase.loadUserProfile()
            Timber.d("User profile loaded after purchase")
        } catch (error: Exception) {
            error.rethrowCancellation {
                handleError(error)
            }
        }
    }

    private fun handleError(error: Exception) {
        errorState.update { error }
        Timber.e(error)
    }

    fun clearError() {
        errorState.update { null }
    }

    override fun onCleared() {
        if (::billingClient.isInitialized) {
            billingClient.endConnection()
        }
        super.onCleared()
    }

    val state = combine(
        userState,
        productsState,
        loadingState,
        errorState,
    ) { state ->
        BillingState(
            user = state[0] as User?,
            products = state[1] as ImmutableList<ProductDetails>?,
            loading = state[2] as LoadingState,
            error = state[3] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
