package tv.trakt.trakt.core.streamings.ui

import android.content.Context
import android.icu.util.Currency
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import coil3.request.ImageRequest
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.streamings.model.StreamingServiceRow
import tv.trakt.trakt.common.helpers.extensions.DevicePreview
import tv.trakt.trakt.common.helpers.extensions.countryFlag
import tv.trakt.trakt.common.helpers.extensions.getContrastRatio
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.streamings.StreamingService
import tv.trakt.trakt.common.model.streamings.StreamingType
import tv.trakt.trakt.common.model.streamings.StreamingType.Favorite
import tv.trakt.trakt.common.model.streamings.StreamingType.Free
import tv.trakt.trakt.common.model.streamings.StreamingType.Purchase
import tv.trakt.trakt.common.model.streamings.StreamingType.Rent
import tv.trakt.trakt.common.model.streamings.StreamingType.Subscription
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import java.util.Locale

internal val GroupShape = RoundedCornerShape(16.dp)

private val ChipShape = RoundedCornerShape(12.dp)
private val LogoTileSize = 60.dp
private val InnerSpace = 6.dp
private const val MaxFlagsOnlyCount = 6

@Composable
internal fun AllStreamingsServiceGroup(
    row: StreamingServiceRow,
    type: StreamingType,
    expanded: Boolean,
    modifier: Modifier = Modifier,
    onToggleExpand: () -> Unit = {},
    onServiceClick: (StreamingService) -> Unit = {},
) {
    val source = row.services.firstOrNull() ?: return
    val gridServices = remember(row.services) {
        when {
            // Flags-only header has no tappable first country, so the grid lists all of them.
            row.services.size in 2..MaxFlagsOnlyCount -> row.services
            else -> row.services.drop(1)
        }
    }

    Column(
        verticalArrangement = spacedBy(InnerSpace),
        modifier = modifier
            .fillMaxWidth()
            .clip(GroupShape)
            .background(TraktTheme.colors.panelCardContainer),
    ) {
        GroupHeader(
            services = row.services,
            type = type,
            expanded = expanded,
            onClick = onToggleExpand,
            onCountryClick = { onServiceClick(source) },
            modifier = Modifier
                .padding(InnerSpace),
        )

        if (gridServices.isNotEmpty()) {
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = tween(50), expandFrom = Alignment.Top),
                exit = shrinkVertically(animationSpec = tween(100), shrinkTowards = Alignment.Top),
            ) {
                CountryGrid(
                    services = gridServices,
                    type = type,
                    onServiceClick = onServiceClick,
                    modifier = Modifier
                        .padding(horizontal = InnerSpace)
                        .padding(bottom = InnerSpace),
                )
            }
        }
    }
}

@Composable
private fun GroupHeader(
    services: List<StreamingService>,
    type: StreamingType,
    expanded: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onCountryClick: () -> Unit = {},
) {
    val source = services.first()
    val othersCount = services.size - 1
    val flagsOnly = services.size in 2..MaxFlagsOnlyCount

    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180F else 0F,
        label = "chevronRotation",
    )

    val flag = remember(source.country) { countryFlag(source.country) }
    val name = remember(source.country) { countryName(source.country) }
    val price = remember(source, type) { source.priceLabel(type) }

    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .onClick(onClick = onClick),
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(12.dp),
            modifier = Modifier
                .weight(1F)
                .padding(end = 24.dp),
        ) {
            SourceLogoTile(
                service = source,
                type = type,
                modifier = Modifier
                    .onClick(onClick = onCountryClick),
            )

            Column(
                verticalArrangement = spacedBy(4.dp),
                modifier = Modifier
                    .onClick(onClick = onCountryClick),
            ) {
                Text(
                    text = source.name,
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.heading6,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Row(
                    verticalAlignment = CenterVertically,
                    horizontalArrangement = spacedBy(5.dp),
                ) {
                    when {
                        flagsOnly -> {
                            services.forEach { service ->
                                Text(
                                    text = remember(service.country) {
                                        countryFlag(service.country) ?: service.country.uppercase()
                                    },
                                    style = TraktTheme.typography.cardSubtitle,
                                    maxLines = 1,
                                )
                            }
                        }
                        else -> {
                            if (flag != null) {
                                Text(
                                    text = flag,
                                    style = TraktTheme.typography.cardSubtitle,
                                )
                            }

                            Text(
                                text = when {
                                    price.isNullOrBlank() -> name
                                    else -> "$name • $price"
                                },
                                color = TraktTheme.colors.textPrimary,
                                style = TraktTheme.typography.cardSubtitle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }

        if (othersCount > 0) {
            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = spacedBy(4.dp),
                modifier = Modifier.padding(end = 2.dp),
            ) {
                if (!flagsOnly) {
                    Text(
                        text = "+$othersCount",
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.meta,
                    )
                }

                Icon(
                    painter = painterResource(R.drawable.ic_chevron_down_small),
                    contentDescription = null,
                    tint = TraktTheme.colors.textSecondary,
                    modifier = Modifier
                        .size(12.dp)
                        .graphicsLayer {
                            rotationZ = chevronRotation
                        },
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
    val containerColor = TraktTheme.colors.placeholderContainer

    val logoFilter = remember(service.color, containerColor) {
        ColorFilter.tint(
            when {
                service.color != null && getContrastRatio(service.color!!, containerColor) >= 3f -> {
                    service.color!!
                }
                else -> {
                    contentColor
                }
            },
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(LogoTileSize)
            .aspectRatio(5 / 4F)
            .background(containerColor, ChipShape)
            .padding(horizontal = 4.dp),
    ) {
        if (service.logo.isNullOrBlank()) {
            Text(
                text = service.name,
                color = contentColor,
                style = TraktTheme.typography.cardTitle,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            return@Box
        }

        val withChannel = remember(service, type) {
            !service.channel.isNullOrBlank() && type != Free
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = spacedBy(0.dp, CenterVertically),
        ) {
            AsyncImage(
                model = remember(service.logo) { context.logoRequest(service.logo) },
                contentDescription = service.name,
                contentScale = ContentScale.Fit,
                colorFilter = logoFilter,
                modifier = Modifier.height(
                    when {
                        withChannel -> 22.dp
                        else -> 28.dp
                    },
                ),
            )

            if (withChannel) {
                AsyncImage(
                    model = remember(service.channel) { context.logoRequest(service.channel) },
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    colorFilter = remember(contentColor) { ColorFilter.tint(contentColor) },
                    modifier = Modifier
                        .height(14.dp)
                        .graphicsLayer {
                            translationY = -1.dp.toPx()
                        },
                )
            }
        }
    }
}

@Composable
private fun CountryGrid(
    services: List<StreamingService>,
    type: StreamingType,
    modifier: Modifier = Modifier,
    onServiceClick: (StreamingService) -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(InnerSpace),
        modifier = modifier.fillMaxWidth(),
    ) {
        services.chunked(2).forEach { pair ->
            Row(
                horizontalArrangement = spacedBy(InnerSpace),
                modifier = Modifier.fillMaxWidth(),
            ) {
                pair.forEach { service ->
                    CountryChip(
                        service = service,
                        type = type,
                        onClick = { onServiceClick(service) },
                        modifier = Modifier.weight(1F),
                    )
                }

                if (pair.size == 1) {
                    Box(modifier = Modifier.weight(1F))
                }
            }
        }
    }
}

@Composable
private fun CountryChip(
    service: StreamingService,
    type: StreamingType,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val flag = remember(service.country) { countryFlag(service.country) }
    val name = remember(service.country) { countryName(service.country) }
    val price = remember(service, type) { service.priceLabel(type) }

    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(InnerSpace),
        modifier = modifier
            .clip(ChipShape)
            .background(TraktTheme.colors.placeholderContainer)
            .onClick(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(
            text = flag ?: service.country.uppercase(),
            style = TraktTheme.typography.cardTitle,
        )

        Column(
            verticalArrangement = spacedBy(2.dp),
            modifier = Modifier.weight(1F),
        ) {
            Text(
                text = name,
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.cardTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!price.isNullOrBlank()) {
                Text(
                    text = price,
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.cardSubtitle.copy(
                        fontSize = TraktTheme.typography.meta.fontSize,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
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
        Purchase -> purchasePrice
        Rent -> rentPrice
        Subscription, Free, Favorite -> null
    }

    if (amount.isNullOrBlank()) {
        return null
    }

    val symbol = currency?.symbol
    val space = if (symbol?.count() == 1) "" else " "

    return "$symbol$space$amount".trim()
}

internal fun countryName(country: String): String {
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
            AllStreamingsServiceGroup(
                row = StreamingServiceRow(
                    source = "apple_tv_plus",
                    services = listOf("pl", "us", "gb", "de")
                        .map { previewService(country = it) }
                        .toImmutableList(),
                ),
                type = Subscription,
                expanded = false,
            )
        }
    }
}

@DevicePreview
@Composable
private fun Preview2() {
    TraktTheme {
        AllStreamingsServiceGroup(
            row = StreamingServiceRow(
                source = "canal_plus",
                services = listOf("fr", "cz", "sk", "de", "us")
                    .map { previewService(country = it, logo = null) }
                    .toImmutableList(),
            ),
            type = Purchase,
            expanded = true,
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
