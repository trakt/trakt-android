package tv.trakt.trakt.core.summary.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.onClickCombined
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.common.ui.theme.colors.Shade400
import tv.trakt.trakt.common.ui.theme.colors.Shade920
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun DetailsActions(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    primaryEnabled: Boolean = true,
    secondaryVisible: Boolean = true,
    loading: Boolean = false,
    inLists: Boolean? = false,
    trailer: Boolean = false,
    primaryIcon: Int = R.drawable.ic_check,
    onPrimaryClick: (() -> Unit)? = null,
    onSecondaryClick: (() -> Unit)? = null,
    onSecondaryLongClick: (() -> Unit)? = null,
    onTrailerClick: (() -> Unit)? = null,
    onMoreClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .shadow(
                elevation = 2.dp,
                shape = shape,
                ambientColor = DefaultShadowColor.copy(alpha = 0.33F),
                spotColor = DefaultShadowColor.copy(alpha = 0.33F),
            )
            .background(
                color = Shade920,
                shape = shape,
            )
            .padding(
                top = 8.dp,
                bottom = 8.dp,
                start = 8.dp,
                end = when {
                    secondaryVisible -> 20.dp
                    else -> 32.dp
                },
            ),
    ) {
        Row(
            horizontalArrangement = spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                modifier = Modifier
                    .weight(1F)
                    .height(40.dp),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                ),
                shape = RoundedCornerShape(12.dp),
                colors = buttonColors(
                    containerColor = TraktTheme.colors.primaryButtonContainer,
                    contentColor = TraktTheme.colors.primaryButtonContent,
                    disabledContainerColor = TraktTheme.colors.primaryButtonContainerDisabled,
                    disabledContentColor = TraktTheme.colors.primaryButtonContentDisabled,
                ),
                enabled = enabled && primaryEnabled,
                onClick = onPrimaryClick ?: {},
            ) {
                if (!loading) {
                    Icon(
                        painter = painterResource(primaryIcon),
                        tint = when {
                            enabled && primaryEnabled -> TraktTheme.colors.primaryButtonContent
                            else -> Shade400
                        },
                        contentDescription = null,
                        modifier = Modifier.size(23.dp),
                    )
                } else {
                    FilmProgressIndicator(
                        size = 20.dp,
                        color = Shade400,
                    )
                }
            }

            if (secondaryVisible) {
                Icon(
                    painter = when {
                        inLists == true -> painterResource(R.drawable.ic_bookmark_on)
                        else -> painterResource(R.drawable.ic_bookmark_off)
                    },
                    tint = when {
                        enabled -> TraktTheme.colors.primaryButtonContent
                        else -> TraktTheme.colors.primaryButtonContainerDisabled
                    },
                    contentDescription = null,
                    modifier = Modifier
                        .onClickCombined(
                            onClick = onSecondaryClick,
                            onLongClick = onSecondaryLongClick,
                        )
                        .size(19.dp),
                )

                Icon(
                    painter = painterResource(R.drawable.ic_trailer),
                    tint = when {
                        enabled && trailer -> TraktTheme.colors.primaryButtonContent
                        else -> TraktTheme.colors.primaryButtonContainerDisabled
                    },
                    contentDescription = null,
                    modifier = Modifier
                        .onClick(
                            enabled = enabled && trailer,
                            onClick = onTrailerClick ?: {},
                        )
                        .size(21.dp),
                )
            }

            Icon(
                painter = painterResource(R.drawable.ic_more_vertical),
                tint = when {
                    enabled -> TraktTheme.colors.primaryButtonContent
                    else -> TraktTheme.colors.primaryButtonContainerDisabled
                },
                contentDescription = null,
                modifier = Modifier
                    .onClick(
                        enabled = enabled,
                        onClick = onMoreClick ?: {},
                    )
                    .rotate(90F)
                    .size(21.dp),
            )
        }
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    widthDp = 400,
)
@Composable
private fun Preview() {
    TraktTheme {
        Column(
            verticalArrangement = spacedBy(16.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            DetailsActions()

            DetailsActions(
                inLists = true,
            )

            DetailsActions(
                enabled = false,
                secondaryVisible = false,
            )

            DetailsActions(
                enabled = false,
                loading = true,
            )
        }
    }
}
