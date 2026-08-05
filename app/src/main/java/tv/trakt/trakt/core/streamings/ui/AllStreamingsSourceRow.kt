package tv.trakt.trakt.core.streamings.ui

import android.content.Context
import android.icu.util.Currency
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import coil3.request.ImageRequest
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.DevicePreview
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.streamings.StreamingService
import tv.trakt.trakt.common.model.streamings.StreamingType
import tv.trakt.trakt.common.model.streamings.StreamingType.FREE
import tv.trakt.trakt.common.model.streamings.StreamingType.PURCHASE
import tv.trakt.trakt.common.model.streamings.StreamingType.RENT
import tv.trakt.trakt.core.streamings.model.StreamingServiceRow
import tv.trakt.trakt.ui.theme.TraktTheme
import java.util.Locale

/**
 * Square tile shared by the source logo and by every country tile, so both columns line up.
 */
internal val TileSize = 96.dp
internal val TileShape = RoundedCornerShape(16.dp)

/**
 * A single streaming source: logo pinned on the left, every country it streams in scrolling
 * horizontally next to it.
 */
@Composable
internal fun AllStreamingsSourceRow(
    row: StreamingServiceRow,
    type: StreamingType,
    modifier: Modifier = Modifier,
    startPadding: Dp = 0.dp,
    endPadding: Dp = 0.dp,
    onServiceClick: (StreamingService) -> Unit = {},
) {
    val source = row.services.firstOrNull() ?: return

    Row(
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        verticalAlignment = CenterVertically,
        modifier = modifier,
    ) {
        SourceLogoTile(
            service = source,
            type = type,
            modifier = Modifier.padding(start = startPadding),
        )

        LazyRow(
            horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
            contentPadding = PaddingValues(end = endPadding),
        ) {
            items(
                items = row.services,
                key = { it.country },
            ) { service ->
                ServiceCountryTile(
                    service = service,
                    type = type,
                    onClick = { onServiceClick(service) },
                )
            }
        }
    }
}

@Composable
private fun SourceLogoTile(
    service: StreamingService,
    type: StreamingType,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val contentColor = TraktTheme.colors.textPrimary

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(TileSize)
            .border(1.dp, TraktTheme.colors.separator, TileShape)
            .padding(horizontal = 12.dp),
    ) {
        if (service.logo.isNullOrBlank()) {
            Text(
                text = service.name,
                color = contentColor,
                style = TraktTheme.typography.buttonPrimary,
                maxLines = 2,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
            )
            return@Box
        }

        val withChannel = remember(service, type) {
            !service.channel.isNullOrBlank() && type != FREE
        }

        Column(
            horizontalAlignment = CenterHorizontally,
            verticalArrangement = spacedBy(0.dp, CenterVertically),
        ) {
            AsyncImage(
                model = remember(service.logo) { context.logoRequest(service.logo) },
                contentDescription = service.name,
                contentScale = ContentScale.FillHeight,
                colorFilter = remember(contentColor) { ColorFilter.tint(contentColor) },
                modifier = Modifier.height(
                    when {
                        withChannel -> 30.dp
                        else -> 38.dp
                    },
                ),
            )

            if (withChannel) {
                AsyncImage(
                    model = remember(service.channel) { context.logoRequest(service.channel) },
                    contentDescription = null,
                    contentScale = ContentScale.FillHeight,
                    colorFilter = remember(contentColor) { ColorFilter.tint(contentColor) },
                    modifier = Modifier
                        .height(18.dp)
                        .graphicsLayer {
                            translationY = -2.dp.toPx()
                        },
                )
            }
        }
    }
}

@Composable
private fun ServiceCountryTile(
    service: StreamingService,
    type: StreamingType,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val flag = remember(service.country) { countryFlag(service.country) }
    val name = remember(service.country) { countryName(service.country) }
    val price = remember(service, type) { service.priceLabel(type) }

    Column(
        horizontalAlignment = CenterHorizontally,
        verticalArrangement = spacedBy(2.dp, CenterVertically),
        modifier = modifier
            .size(TileSize)
            .background(TraktTheme.colors.commentContainer, TileShape)
            .onClick(onClick = onClick)
            .padding(horizontal = 8.dp),
    ) {
        if (flag != null) {
            Text(
                text = flag,
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading3,
            )
        }

        Text(
            text = name,
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.buttonTertiary,
            maxLines = 2,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
        )

        if (!price.isNullOrBlank()) {
            Text(
                text = price,
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.meta,
                maxLines = 1,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun Context.logoRequest(path: String?): ImageRequest {
    val url = "https://$path"
    return ImageRequest.Builder(this)
        .data(url)
        .memoryCacheKey(url)
        .placeholderMemoryCacheKey(url)
        .build()
}

private fun StreamingService.priceLabel(type: StreamingType): String? {
    val amount = when (type) {
        PURCHASE -> purchasePrice
        RENT -> rentPrice
        else -> null
    }

    if (amount.isNullOrBlank()) {
        return null
    }

    val symbol = currency?.symbol
    val space = if (symbol?.count() == 1) "" else " "

    return "$symbol$space$amount".trim()
}

/**
 * Regional indicator pair for an ISO 3166-1 alpha-2 code, ex. "pl" renders the Polish flag.
 */
private fun countryFlag(country: String): String? {
    val code = country.trim().uppercase()
    if (code.length != 2 || code.any { it !in 'A'..'Z' }) {
        return null
    }

    val offset = 0x1F1E6 - 'A'.code
    return code
        .map { String(Character.toChars(offset + it.code)) }
        .joinToString(separator = "")
}

private fun countryName(country: String): String {
    val name = Locale("", country).displayCountry
    return when {
        name.isBlank() -> country.uppercase()
        else -> name
    }
}

// -- Previews --

@OptIn(ExperimentalCoilApi::class)
@DevicePreview
@Composable
private fun Preview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.White.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            AllStreamingsSourceRow(
                row = StreamingServiceRow(
                    source = "apple_tv_plus",
                    services = listOf("pl", "us", "gb", "de")
                        .map { previewService(country = it) }
                        .toImmutableList(),
                ),
                type = StreamingType.SUBSCRIPTION,
                startPadding = 16.dp,
                endPadding = 16.dp,
            )
        }
    }
}

@DevicePreview
@Composable
private fun Preview2() {
    TraktTheme {
        AllStreamingsSourceRow(
            row = StreamingServiceRow(
                source = "canal_plus",
                services = listOf("fr", "cz", "sk")
                    .map { previewService(country = it, logo = null) }
                    .toImmutableList(),
            ),
            type = PURCHASE,
            startPadding = 16.dp,
            endPadding = 16.dp,
        )
    }
}

private fun previewService(
    country: String,
    logo: String? = "logo",
) = StreamingService(
    source = "apple_tv_plus",
    name = "Apple TV+",
    logo = logo,
    channel = null,
    linkDirect = "https://trakt.tv",
    uhd = false,
    color = null,
    country = country,
    currency = Currency.getInstance("USD"),
    purchasePrice = "19.99",
    rentPrice = "4.99",
)
