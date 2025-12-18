@file:OptIn(FlowPreview::class)
@file:Suppress("UNCHECKED_CAST")

package tv.trakt.trakt.core.billing

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.queryProductDetails
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
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
import tv.trakt.trakt.core.billing.model.VipBillingError
import tv.trakt.trakt.core.billing.model.VipBillingProduct

internal class BillingViewModel(
    private val appContext: Context,
    private val sessionManager: SessionManager,
    analytics: Analytics,
) : ViewModel() {
    private val initialState = BillingState()

    private val userState = MutableStateFlow(initialState.user)
    private val productsState = MutableStateFlow(initialState.products)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private lateinit var billingClient: BillingClient

    private val purchasesUpdatedListener = PurchasesUpdatedListener { result, purchases ->
        // To be implemented in a later section.
    }

    private val billingStateListener = object : BillingClientStateListener {
        override fun onBillingSetupFinished(billingResult: BillingResult) {
            loadingState.update { LoadingState.DONE }
            if (billingResult.responseCode == BillingResponseCode.OK) {
                Timber.d("Billing Client setup finished successfully")
                loadPurchases()
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

    private fun loadPurchases() {
        viewModelScope.launch {
            val productList = VipBillingProduct.entries.map {
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(it.sku)
                    .setProductType(BillingClient.ProductType.SUBS)
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
