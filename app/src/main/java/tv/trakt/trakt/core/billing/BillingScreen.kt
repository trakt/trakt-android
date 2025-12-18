@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.billing

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.Top
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.billingclient.api.ProductDetails
import tv.trakt.trakt.LocalBottomBarVisibility
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.core.billing.model.VipBillingError
import tv.trakt.trakt.core.billing.model.VipBillingOffer.MONTHLY_STANDARD
import tv.trakt.trakt.core.billing.model.VipBillingOffer.MONTHLY_STANDARD_TRIAL
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun BillingScreen(
    viewModel: BillingViewModel,
    onNavigateBack: () -> Unit,
) {
    val localBottomBarVisibility = LocalBottomBarVisibility.current

    val state by viewModel.state.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        localBottomBarVisibility.value = false

        onDispose {
            localBottomBarVisibility.value = true
        }
    }

    BillingScreen(
        state = state,
        onErrorClick = viewModel::clearError,
        onBackClick = onNavigateBack,
    )
}

@Composable
private fun BillingScreen(
    state: BillingState,
    onBackClick: () -> Unit = {},
    onErrorClick: () -> Unit = {},
) {
    val inspection = LocalInspectionMode.current
    val contentPadding = PaddingValues(
        start = TraktTheme.spacing.mainPageHorizontalSpace,
        end = TraktTheme.spacing.mainPageHorizontalSpace,
        top = WindowInsets.statusBars.asPaddingValues()
            .calculateTopPadding()
            .plus(4.dp),
        bottom = WindowInsets.navigationBars.asPaddingValues()
            .calculateBottomPadding(),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary),
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(
                    state = rememberScrollState(),
                    overscrollEffect = null,
                )
                .padding(contentPadding),
        ) {
            TitleBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .onClick(onClick = onBackClick),
            )
        }

        Column(
            verticalArrangement = spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(
                    start = TraktTheme.spacing.mainPageHorizontalSpace,
                    end = TraktTheme.spacing.mainPageHorizontalSpace,
                    bottom = contentPadding
                        .calculateBottomPadding()
                        .plus(16.dp),
                ),
        ) {
            if (state.error != null || inspection) {
                Row(
                    verticalAlignment = Top,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .background(
                            Red500,
                            RoundedCornerShape(14.dp),
                        )
                        .padding(
                            horizontal = 12.dp,
                            vertical = 12.dp,
                        ),
                ) {
                    Text(
                        text = when {
                            state.error is VipBillingError -> stringResource(state.error.displayErrorRes)
                            else -> state.error?.message ?: stringResource(R.string.error_text_unexpected_error_short)
                        },
                        style = TraktTheme.typography.paragraphSmaller,
                        color = TraktTheme.colors.textPrimary,
                        maxLines = 10,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f, fill = false),
                    )

                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        tint = TraktTheme.colors.textPrimary,
                        contentDescription = null,
                        modifier = Modifier
                            .size(28.dp)
                            .padding(start = 8.dp)
                            .onClick(onClick = onErrorClick),
                    )
                }
            }

            PaymentDialog(
                product = state.products?.firstOrNull(),
                modifier = Modifier
                    .shadow(4.dp, RoundedCornerShape(28.dp))
                    .fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PaymentDialog(
    product: ProductDetails?,
    modifier: Modifier = Modifier,
) {
    val inspection = LocalInspectionMode.current
    val monthPeriodText = stringResource(R.string.text_billing_period_month)

    Column(
        modifier = modifier
            .animateContentSize()
            .background(
                TraktTheme.colors.dialogContainer,
                RoundedCornerShape(28.dp),
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (inspection || product != null) {
            val freeTrialOffer = remember(product) {
                product?.subscriptionOfferDetails
                    ?.firstOrNull { it.offerId == MONTHLY_STANDARD_TRIAL.id }
            }

            val standardOffer = remember(product) {
                product?.subscriptionOfferDetails
                    ?.firstOrNull { it.offerId == MONTHLY_STANDARD.id }
            }

            if (freeTrialOffer != null) {
                Text(
                    text = stringResource(R.string.text_billing_month_for_free),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.paragraphSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(bottom = 12.dp),
                )
            }

            Text(
                text = when {
                    freeTrialOffer != null -> {
                        val pricing = freeTrialOffer.pricingPhases.pricingPhaseList
                            .lastOrNull { it.priceAmountMicros > 0 }
                        "${pricing?.formattedPrice} / $monthPeriodText"
                    }

                    standardOffer != null -> {
                        val pricing = standardOffer.pricingPhases.pricingPhaseList
                            .lastOrNull { it.priceAmountMicros > 0 }
                        "${pricing?.formattedPrice} / $monthPeriodText"
                    }

                    inspection -> {
                        "£4.99 / month"
                    }

                    else -> {
                        ""
                    }
                },
                style = TraktTheme.typography.heading3.copy(
                    letterSpacing = 0.em,
                ),
                color = TraktTheme.colors.textPrimary,
            )

            val buttonShape = RoundedCornerShape(18.dp)
            Column(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
                    .shadow(2.dp, buttonShape)
                    .background(Red500, buttonShape)
                    .padding(14.dp),
                verticalArrangement = spacedBy(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = when {
                        freeTrialOffer != null -> stringResource(R.string.text_billing_try_for_free)
                        else -> stringResource(R.string.text_billing_subscribe_now)
                    }.uppercase(),
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.buttonPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )

                if (freeTrialOffer != null) {
                    Text(
                        text = stringResource(R.string.text_billing_no_payment_required),
                        color = TraktTheme.colors.textPrimary,
                        style = TraktTheme.typography.meta.copy(
                            fontSize = 10.sp,
                            fontWeight = W400,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Text(
                text = stringResource(R.string.text_billing_disclaimer, monthPeriodText),
                style = TraktTheme.typography.meta.copy(
                    fontSize = 10.sp,
                    fontWeight = W400,
                    lineHeight = 1.2.em,
                ),
                textAlign = TextAlign.Center,
                color = TraktTheme.colors.textSecondary,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
            )
        } else {
            FilmProgressIndicator(
                size = 42.dp,
                modifier = Modifier
                    .padding(vertical = 16.dp),
            )
        }
    }
}

@Composable
private fun TitleBar(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(12.dp),
            modifier = Modifier
                .height(TraktTheme.size.titleBarHeight)
                .graphicsLayer {
                    translationX = -2.dp.toPx()
                },
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back_arrow),
                tint = TraktTheme.colors.textPrimary,
                contentDescription = null,
            )
        }
    }
}

// Previews

@Preview(
    device = "id:pixel_6",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        BillingScreen(
            state = BillingState(
                user = PreviewData.user1,
                loading = LoadingState.IDLE,
            ),
        )
    }
}
