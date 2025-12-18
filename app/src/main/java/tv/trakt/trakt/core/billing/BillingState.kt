package tv.trakt.trakt.core.billing

import androidx.compose.runtime.Immutable
import com.android.billingclient.api.ProductDetails
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.User

@Immutable
internal data class BillingState(
    val user: User? = null,
    val products: ImmutableList<ProductDetails>? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
)
