@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.billing

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.billingclient.api.ProductDetails
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import tv.trakt.trakt.LocalBottomBarVisibility
import tv.trakt.trakt.common.Config
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_BACKGROUND_VIP_IMAGE_URL
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.common.ui.theme.colors.Red400
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.core.billing.model.InternalVersionError
import tv.trakt.trakt.core.billing.model.VipBillingError
import tv.trakt.trakt.core.billing.model.VipBillingOffer.MONTHLY_STANDARD
import tv.trakt.trakt.core.billing.model.VipBillingOffer.MONTHLY_STANDARD_TRIAL
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.components.vip.VipChip
import tv.trakt.trakt.ui.theme.HorizontalImageAspectRatio
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun BillingScreen(
    viewModel: BillingViewModel,
    onNavigateBack: () -> Unit,
) {
    val localActivity = LocalActivity.current
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
        onPurchaseClick = { product ->
            localActivity?.let {
                viewModel.launchPurchaseFlow(
                    activity = it,
                    product = product,
                )
            }
        },
        onErrorClick = viewModel::clearError,
        onBackClick = onNavigateBack,
        modifier = Modifier,
    )
}

@Composable
private fun BillingScreen(
    state: BillingState,
    modifier: Modifier = Modifier,
    onPurchaseClick: (ProductDetails) -> Unit = {},
    onBackClick: () -> Unit = {},
    onErrorClick: () -> Unit = {},
) {
    val inspection = LocalInspectionMode.current
    val configuration = LocalConfiguration.current

    val screenWidth = configuration.screenWidthDp.dp
    val contentPadding = PaddingValues(
        start = TraktTheme.spacing.mainPageHorizontalSpace,
        end = TraktTheme.spacing.mainPageHorizontalSpace,
        top = WindowInsets.statusBars.asPaddingValues()
            .calculateTopPadding()
            .plus(4.dp),
        bottom = WindowInsets.navigationBars.asPaddingValues()
            .calculateBottomPadding(),
    )

    val backgroundColor1 = TraktTheme.colors.backgroundPrimary
    val backgroundGradient = remember {
        verticalGradient(
            colors = listOf(
                Color.Transparent,
                backgroundColor1.copy(alpha = 0.85f),
                backgroundColor1,
            ),
        )
    }

    val listScrollConnection = rememberSaveable(saver = SimpleScrollConnection.Saver) {
        SimpleScrollConnection()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor1)
            .nestedScroll(listScrollConnection),
    ) {
        ScrollableBackdropImage(
            translation = listScrollConnection.resultOffset,
            imageAlpha = 0.85F,
            imageUrl = remember {
                if (inspection) {
                    null
                } else {
                    Firebase.remoteConfig.getString(MOBILE_BACKGROUND_VIP_IMAGE_URL)
                        .ifBlank { null }
                }
            },
        )

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
                    .onClick(
                        enabled = !state.loading.isLoading || state.products == null,
                        onClick = onBackClick,
                    ),
            )

            val spacerHeight = remember(screenWidth) {
                screenWidth / HorizontalImageAspectRatio
            }
                .minus(TraktTheme.size.titleBarHeight)
                .minus(112.dp)

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(spacerHeight),
            )

            VipOfferView(
                modifier = Modifier
                    .padding(bottom = 272.dp),
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .height(192.dp)
                .fillMaxWidth()
                .background(backgroundGradient),
        )

        Column(
            verticalArrangement = spacedBy(16.dp, CenterVertically),
            horizontalAlignment = CenterHorizontally,
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
            if (state.error != null) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = CenterVertically,
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
                            state.error is VipBillingError -> {
                                if (state.error is InternalVersionError) {
                                    "Google Play Billing is not available for internal test versions of the app."
                                } else {
                                    val code = state.error.code
                                    "${stringResource(state.error.displayErrorRes)} ${code?.let { "($it)" } ?: ""}"
                                }
                            }
                            else -> {
                                state.error.message ?: stringResource(R.string.error_text_unexpected_error_short)
                            }
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
                            .size(24.dp)
                            .padding(start = 8.dp)
                            .onClick(onClick = onErrorClick),
                    )
                }
            }

            PaymentDialog(
                user = state.user,
                product = state.products?.firstOrNull(),
                loading = state.loading.isLoading,
                onPurchaseClick = onPurchaseClick,
                onBackClick = onBackClick,
                modifier = Modifier
                    .shadow(4.dp, RoundedCornerShape(28.dp))
                    .fillMaxWidth(),
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

@Composable
private fun VipOfferView(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.text_vip_offer_unlock),
            style = TraktTheme.typography.heading4.copy(
                letterSpacing = 0.em,
            ),
            color = TraktTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(8.dp, CenterHorizontally),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_trakt_logo),
                contentDescription = null,
                modifier = Modifier
                    .height(24.dp),
            )

            VipChip(
                modifier = Modifier
                    .padding(top = 1.5.dp)
                    .shadow(2.dp, RoundedCornerShape(100)),
            )
        }

        Column(
            verticalArrangement = spacedBy(36.dp),
            modifier = Modifier
                .padding(top = 48.dp),
        ) {
            VipOfferItem(
                text = stringResource(R.string.text_vip_offer_unlimited_lists),
                description = stringResource(R.string.text_vip_offer_unlimited_lists_description),
                icon = painterResource(R.drawable.ic_vip_lists),
            )

            VipOfferItem(
                text = stringResource(R.string.text_vip_offer_streaming_sync),
                description = stringResource(R.string.text_vip_offer_streaming_sync_description),
                icon = painterResource(R.drawable.ic_vip_stream_sync),
                iconPadding = 2.dp,
            )

            VipOfferItem(
                text = stringResource(R.string.text_vip_offer_month_review),
                description = stringResource(R.string.text_vip_offer_month_review_description),
                icon = painterResource(R.drawable.ic_vip_month),
                iconPadding = 2.dp,
            )

            VipOfferItem(
                text = stringResource(R.string.text_vip_offer_year_review),
                description = stringResource(R.string.text_vip_offer_year_review_description, nowUtc().year),
                icon = painterResource(R.drawable.ic_vip_stats),
                iconPadding = 2.dp,
            )

            VipOfferItem(
                text = stringResource(R.string.text_vip_offer_alltime_stats),
                description = stringResource(R.string.text_vip_offer_alltime_stats_description),
                icon = painterResource(R.drawable.ic_vip_leaderboard),
                iconPadding = 4.dp,
            )

            VipOfferItem(
                text = stringResource(R.string.text_vip_offer_early_access),
                description = stringResource(R.string.text_vip_offer_early_access_description),
                icon = painterResource(R.drawable.ic_vip_mobile),
                iconPadding = 4.dp,
            )

            VipOfferItem(
                text = stringResource(R.string.text_vip_offer_badges),
                description = stringResource(R.string.text_vip_offer_badges_description),
                icon = painterResource(R.drawable.ic_vip_crown),
                iconPadding = 4.dp,
            )
        }

        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(start = 7.dp, top = 36.dp)
                .fillMaxWidth()
                .onClick {
                    uriHandler.openUri(Config.WEB_VIP_URL)
                },
        ) {
            VipOfferItem(
                text = stringResource(R.string.text_vip_offer_and_more),
                description = stringResource(R.string.text_vip_offer_and_more_description),
                modifier = Modifier
                    .weight(1f, fill = false),
            )

            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                tint = TraktTheme.colors.textPrimary,
                contentDescription = null,
                modifier = Modifier
                    .size(22.dp),
            )
        }

        Column(
            verticalArrangement = spacedBy(5.dp),
            modifier = Modifier.padding(top = 56.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_heart_on),
                tint = Red400,
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .align(CenterHorizontally),
            )

            Text(
                text = stringResource(R.string.text_vip_offer_support_1),
                style = TraktTheme.typography.paragraphSmall.copy(
                    fontSize = 15.sp,
                    lineHeight = 1.3.em,
                ),
                color = TraktTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth(),
            )

            Text(
                text = stringResource(R.string.text_vip_offer_support_2),
                style = TraktTheme.typography.paragraphSmall.copy(
                    lineHeight = 1.3.em,
                ),
                color = TraktTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = TraktTheme.spacing.mainPageHorizontalSpace)
                    .fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun VipOfferItem(
    modifier: Modifier = Modifier,
    text: String,
    description: String,
    icon: Painter? = null,
    iconPadding: Dp = 0.dp,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(16.dp),
        modifier = modifier,
    ) {
        icon?.let {
            Icon(
                painter = it,
                tint = TraktTheme.colors.textPrimary,
                contentDescription = null,
                modifier = Modifier
                    .size(36.dp)
                    .padding(iconPadding),
            )
        }
        Column(
            verticalArrangement = spacedBy(2.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = text,
                style = TraktTheme.typography.heading5,
                color = TraktTheme.colors.textPrimary,
            )
            Text(
                text = description,
                style = TraktTheme.typography.paragraphSmaller,
                color = TraktTheme.colors.textSecondary,
                maxLines = 3,
            )
        }
    }
}

@Composable
private fun PaymentDialog(
    user: User?,
    product: ProductDetails?,
    loading: Boolean,
    modifier: Modifier = Modifier,
    onPurchaseClick: (ProductDetails) -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    val inspection = LocalInspectionMode.current

    Column(
        modifier = modifier
            .animateContentSize(
                animationSpec = tween(300),
            )
            .background(
                TraktTheme.colors.dialogContainer,
                RoundedCornerShape(28.dp),
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = CenterHorizontally,
    ) {
        if (user?.isVip == true) {
            PaymentAlreadyVipContent(
                onBackClick = onBackClick,
            )
        } else if ((inspection || product != null) && !loading) {
            PaymentDialogContent(
                product = product,
                onPurchaseClick = onPurchaseClick,
            )
        } else {
            FilmProgressIndicator(
                size = 42.dp,
                modifier = Modifier
                    .padding(vertical = 32.dp),
            )
        }
    }
}

@Composable
private fun PaymentAlreadyVipContent(onBackClick: () -> Unit) {
    Column(
        verticalArrangement = spacedBy(16.dp),
        horizontalAlignment = CenterHorizontally,
        modifier = Modifier.padding(top = 8.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_check_double),
            tint = TraktTheme.colors.textPrimary,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp),
        )

        Text(
            text = stringResource(R.string.text_billing_already_vip),
            style = TraktTheme.typography.paragraph,
            color = TraktTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )

        PrimaryButton(
            text = "OK",
            onClick = onBackClick,
            modifier = Modifier
                .padding(top = 8.dp)
                .widthIn(164.dp),
        )
    }
}

@Composable
private fun PaymentDialogContent(
    product: ProductDetails?,
    onPurchaseClick: (ProductDetails) -> Unit,
) {
    val inspection = LocalInspectionMode.current
    val monthPeriodText = stringResource(R.string.text_billing_period_month)

    val freeTrialOffer = remember(product) {
        product?.subscriptionOfferDetails
            ?.firstOrNull {
                it.offerId == MONTHLY_STANDARD_TRIAL.id ||
                    it.basePlanId == MONTHLY_STANDARD_TRIAL.id
            }
    }

    val standardOffer = remember(product) {
        product?.subscriptionOfferDetails
            ?.firstOrNull {
                it.offerId == MONTHLY_STANDARD.id ||
                    it.basePlanId == MONTHLY_STANDARD.id
            }
    }

    if (freeTrialOffer != null) {
        Text(
            text = stringResource(R.string.text_billing_month_for_free),
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.paragraphSmaller,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp),
        )
    } else if (standardOffer != null) {
        Text(
            text = stringResource(R.string.button_text_join_trakt),
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.paragraphSmaller,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp),
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
            .background(
                color = Red500,
                shape = buttonShape,
            )
            .padding(14.dp)
            .onClick(indication = true) {
                product?.let {
                    onPurchaseClick(it)
                }
            },
        verticalArrangement = spacedBy(2.dp),
        horizontalAlignment = CenterHorizontally,
    ) {
        Text(
            text = when {
                freeTrialOffer != null -> stringResource(R.string.text_billing_try_for_free)
                else -> stringResource(R.string.button_text_upgrade_to_vip)
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
