package tv.trakt.trakt.core.billing

import androidx.compose.runtime.Immutable
import com.android.billingclient.api.ProductDetails
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.billing.model.VipUsageLimit

@Immutable
internal data class BillingState(
    val user: User? = null,
    val products: ImmutableList<ProductDetails>? = null,
    val limits: ImmutableList<VipUsageLimit>? = null,
    val loading: LoadingState = LoadingState.Idle,
    val error: Exception? = null,
)
