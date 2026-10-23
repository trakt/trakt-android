@file:OptIn(FlowPreview::class)
@file:Suppress("UNCHECKED_CAST")

package tv.trakt.trakt.core.billing

import android.app.Activity
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
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
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.billing.model.CancelledError
import tv.trakt.trakt.core.billing.model.InternalVersionError
import tv.trakt.trakt.core.billing.model.PendingPurchaseProduct
import tv.trakt.trakt.core.billing.model.VipBillingError
import tv.trakt.trakt.core.billing.model.VipBillingOffer.MONTHLY_STANDARD
import tv.trakt.trakt.core.billing.model.VipBillingOffer.MONTHLY_STANDARD_TRIAL
import tv.trakt.trakt.core.billing.model.VipBillingProduct
import tv.trakt.trakt.core.billing.model.VipUsageLimit
import tv.trakt.trakt.core.billing.model.toVipFailureCode
import tv.trakt.trakt.core.billing.navigation.BillingDestination
import tv.trakt.trakt.core.billing.usecases.GetVipUsageLimitsUseCase
import tv.trakt.trakt.core.billing.usecases.VerifyPurchaseUseCase
import tv.trakt.trakt.core.user.usecases.LoadUserProfileUseCase
import kotlin.time.Duration.Companion.milliseconds

private const val VIP_FLOW = "new"
private const val KEY_OPEN_LOGGED = "vip_open_logged"
private const val KEY_PENDING_PLAN = "vip_pending_plan"

private val AUTO_OPEN_SOURCES = setOf("onboarding", "vip-limit")

internal class BillingViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val analytics: Analytics,
    private val appContext: Context,
    private val sessionManager: SessionManager,
    private val verifyPurchaseUseCase: VerifyPurchaseUseCase,
    private val loadUserUseCase: LoadUserProfileUseCase,
    private val getUsageLimitsUseCase: GetVipUsageLimitsUseCase,
) : ViewModel() {
    private val initialState = BillingState()

    private val userState = MutableStateFlow(initialState.user)
    private val productsState = MutableStateFlow(initialState.products)
    private val limitsState = MutableStateFlow(initialState.limits)
    private val loadingLimitsState = MutableStateFlow(initialState.loadingLimits)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private lateinit var billingClient: BillingClient
    private var pendingPurchaseProduct: PendingPurchaseProduct? = null

    private val source = savedStateHandle.toRoute<BillingDestination>().source

    private val purchasesUpdatedListener = PurchasesUpdatedListener { result, purchases ->
        if (result.responseCode == BillingResponseCode.OK) {
            purchases?.forEach { purchase ->
                handlePurchase(purchase)
            }
        } else {
            pendingPurchaseProduct?.let {
                pendingPurchaseProduct = null
                val error = VipBillingError.fromBillingResponseCode(result.responseCode)
                logCheckoutOutcome(error)
                if (error is CancelledError) {
                    Timber.d("Purchase cancelled by user: ${it.skuId}")
                } else {
                    handleError(error = error)
                }
            }
            loadingState.update { LoadingState.Done }
        }
    }

    private val billingStateListener = object : BillingClientStateListener {
        override fun onBillingSetupFinished(billingResult: BillingResult) {
            loadingState.update { LoadingState.Done }
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
        loadLimits()
        observeUser()

        initPlayBilling()
        startPlayBilling()

        analytics.logScreenView(
            screenName = "billing",
        )
        logPaywallOpen()
    }

    private fun logPaywallOpen() {
        if (savedStateHandle.get<Boolean>(KEY_OPEN_LOGGED) == true) return
        savedStateHandle[KEY_OPEN_LOGGED] = true

        analytics.vip.logView(source = source, flow = VIP_FLOW)
        if (source !in AUTO_OPEN_SOURCES) {
            analytics.logVipUpsell(source)
        }
    }

    private fun loadUser() {
        viewModelScope.launch {
            userState.update {
                sessionManager.getProfile()
            }
        }
    }

    private fun loadLimits() {
        viewModelScope.launch {
            loadingLimitsState.update { LoadingState.Loading }
            try {
                limitsState.update {
                    getUsageLimitsUseCase.getLimits()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
            loadingLimitsState.update { LoadingState.Done }
        }
    }

    private fun observeUser() {
        sessionManager.observeProfile()
            .distinctUntilChanged()
            .debounce(200.milliseconds)
            .onEach { user ->
                userState.update { user }
            }
            .launchIn(viewModelScope)
    }

    private fun initPlayBilling() {
        if (appContext.packageName != "tv.trakt.trakt") {
            handleError(InternalVersionError())
            return
        }

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
        if (appContext.packageName != "tv.trakt.trakt") {
            handleError(InternalVersionError())
            return
        }

        loadingState.update { LoadingState.Loading }
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
                        val error = VipBillingError.fromBillingResponseCode(BillingResponseCode.ITEM_UNAVAILABLE)
                        handleError(error)
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

        loadingState.update { LoadingState.Loading }
        val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)
        val responseCode = billingResult.responseCode

        val plan = VipBillingProduct.entries.find { it.sku == product.productId }?.plan
        savedStateHandle[KEY_PENDING_PLAN] = plan

        if (responseCode != BillingResponseCode.OK) {
            val billingError = VipBillingError.fromBillingResponseCode(responseCode)
            logCheckoutOutcome(billingError)
            handleError(billingError)
            loadingState.update { LoadingState.Done }
        } else {
            val price = offer?.pricingPhases?.pricingPhaseList?.firstOrNull()
            pendingPurchaseProduct = PendingPurchaseProduct(
                skuId = product.productId,
                currencyCode = price?.priceCurrencyCode,
                price = price?.priceAmountMicros,
            )

            plan?.let { analytics.logVipUpgrade(plan = it, source = source, flow = VIP_FLOW) }
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

        val plan = savedStateHandle.get<String>(KEY_PENDING_PLAN)

        try {
            verifyPurchaseUseCase.verifyPurchase(
                purchase = purchase,
                pendingPurchaseProduct = pendingPurchaseProduct,
            )
            Timber.d("Purchase verified successfully: ${purchase.products.firstOrNull()}")
        } catch (error: Exception) {
            error.rethrowCancellation {
                savedStateHandle.remove<String>(KEY_PENDING_PLAN)
                plan?.let {
                    analytics.vip.logConfirmFailed(
                        plan = it,
                        source = source,
                        flow = VIP_FLOW,
                        error = error.toVipFailureCode(),
                    )
                }
                handleError(error)
                Timber.recordError(error)
            }
            return
        }

        savedStateHandle.remove<String>(KEY_PENDING_PLAN)
        plan?.let { analytics.vip.logConfirmed(plan = it, source = source, flow = VIP_FLOW) }
        refreshProfileAfterPurchase()
    }

    private suspend fun refreshProfileAfterPurchase() {
        try {
            delay(500)
            loadUserUseCase.loadUserProfile()
            Timber.d("User profile loaded after purchase")
        } catch (error: Exception) {
            error.rethrowCancellation {
                handleError(error)
                Timber.recordError(error)
            }
        }
    }

    private fun logCheckoutOutcome(error: VipBillingError) {
        val plan = savedStateHandle.remove<String>(KEY_PENDING_PLAN) ?: return
        if (error is CancelledError) {
            analytics.vip.logCheckoutCanceled(plan = plan, source = source, flow = VIP_FLOW)
            return
        }
        analytics.vip.logCheckoutFailed(
            plan = plan,
            source = source,
            flow = VIP_FLOW,
            error = error.toVipFailureCode(),
        )
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
        limitsState,
        loadingLimitsState,
    ) { state ->
        BillingState(
            user = state[0] as User?,
            products = state[1] as ImmutableList<ProductDetails>?,
            loading = state[2] as LoadingState,
            error = state[3] as Exception?,
            limits = state[4] as ImmutableList<VipUsageLimit>?,
            loadingLimits = state[5] as LoadingState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
