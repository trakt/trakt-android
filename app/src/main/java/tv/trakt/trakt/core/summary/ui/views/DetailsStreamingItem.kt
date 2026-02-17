package tv.trakt.trakt.core.summary.ui.views

import android.icu.util.Currency
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import coil3.request.ImageRequest
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.streamings.StreamingService
import tv.trakt.trakt.common.model.streamings.StreamingType
import tv.trakt.trakt.common.model.streamings.StreamingType.PURCHASE
import tv.trakt.trakt.common.model.streamings.StreamingType.RENT
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun DetailsStreamingItem(
    service: StreamingService,
    type: StreamingType,
    onClick: ((StreamingService) -> Unit)?,
) {
    val itemHeight = 86.dp
    val contentHeight = 52.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(4.dp, CenterVertically),
        modifier = Modifier
            .height(itemHeight)
            .background(TraktTheme.colors.commentContainer, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .onClick(
                onClick = { onClick?.invoke(service) },
            ),
    ) {
        if (service.logo.isNullOrBlank()) {
            Text(
                text = service.name,
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.buttonPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .height(contentHeight)
                    .wrapContentHeight(align = CenterVertically),
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = spacedBy(0.dp, CenterVertically),
                modifier = Modifier
                    .height(contentHeight),
            ) {
                val withChannel = remember(service, type) {
                    !service.channel.isNullOrBlank() && type != StreamingType.FREE
                }
                val logoUrl = "https://${service.logo}"
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(logoUrl)
                        .memoryCacheKey(logoUrl)
                        .placeholderMemoryCacheKey(logoUrl)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.FillHeight,
                    colorFilter = remember(service.color) {
                        ColorFilter.tint(
                            when {
                                service.color == Color.Black -> Color.White
                                service.color != null -> service.color!!
                                else -> Color.White
                            },
                        )
                    },
                    modifier = Modifier.height(
                        when {
                            withChannel -> 34.dp
                            else -> 40.dp
                        },
                    ),
                )

                if (withChannel) {
                    val logoUrl = "https://${service.channel}"
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(logoUrl)
                            .memoryCacheKey(logoUrl)
                            .placeholderMemoryCacheKey(logoUrl)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.FillHeight,
                        colorFilter = ColorFilter.tint(Color.White),
                        modifier = Modifier
                            .height(20.dp)
                            .graphicsLayer {
                                translationY = -3.dp.toPx()
                            },
                    )
                }
            }
        }

        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(2.dp),
        ) {
            Text(
                text = stringResource(type.labelRes).uppercase(),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.meta,
            )

            val price = remember(service.purchasePrice, service.rentPrice) {
                val currencySymbol = service.currency?.symbol
                val currencySpace = if (currencySymbol?.count() == 1) "" else " "

                when (type) {
                    PURCHASE -> "$currencySymbol$currencySpace${service.purchasePrice}".trim()
                    RENT -> "$currencySymbol$currencySpace${service.rentPrice}".trim()
                    else -> null
                }
            }

            if (!price.isNullOrBlank()) {
                Text(
                    text = "(${price.uppercase()})",
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.meta,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
    locale = "en",
    widthDp = 200,
)
@Composable
private fun Preview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            DetailsStreamingItem(
                service = StreamingService(
                    source = "Hello",
                    name = "Hello",
                    logo = "test",
                    channel = "Hello",
                    linkDirect = "Hello",
                    uhd = false,
                    color = null,
                    country = "pl",
                    currency = Currency.getInstance("PLN"),
                    purchasePrice = "19.99",
                    rentPrice = "19.99",
                ),
                type = StreamingType.PURCHASE,
                onClick = {},
            )
        }
    }
}
