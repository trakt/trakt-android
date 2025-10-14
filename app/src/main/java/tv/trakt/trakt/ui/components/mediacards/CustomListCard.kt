package tv.trakt.trakt.ui.components.mediacards

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.CustomList.Type
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.HorizontalImageAspectRatio
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun CustomListCard(
    list: CustomList,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    CustomListCardContent(
        list = list,
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
private fun CustomListCardContent(
    list: CustomList,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = cardColors(
            containerColor = TraktTheme.colors.customListContainer,
        ),
        content = {
            CustomListContent(
                list = list,
                onClick = onClick,
            )
        },
    )
}

@Composable
private fun CustomListContent(
    list: CustomList,
    onClick: () -> Unit,
) {
    Column(
        verticalArrangement = spacedBy(0.dp, Alignment.CenterVertically),
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
    ) {
        CustomListHeader(
            list = list,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        val images =
            remember(list.images?.posters?.size) {
                list.images?.getPostersUrl()?.take(8)
            }

        images?.let {
            Box(
                modifier = Modifier
                    .onClick(onClick = onClick)
                    .padding(
                        top = 16.dp,
                        start = 16.dp,
                        end = 16.dp,
                    ),
            ) {
                it.forEachIndexed { index, url ->
                    VerticalMediaCard(
                        title = "",
                        imageUrl = url,
                        width = 70.dp,
                        corner = 8.dp,
                        enabled = false,
                        modifier = Modifier
                            .padding(start = (32 * index).dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1F))
    }
}

@Composable
private fun CustomListHeader(
    list: CustomList,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(8.dp),
        modifier = modifier,
    ) {
        Box(
            modifier =
                Modifier
                    .size(36.dp),
        ) {
            val avatarBorder = if (list.user.isAnyVip) Color.Red else Color.Transparent
            val avatar = list.user.images?.avatar?.full
            if (avatar != null) {
                AsyncImage(
                    model = avatar,
                    contentDescription = "User avatar",
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.ic_person_placeholder),
                    modifier =
                        Modifier
                            .border(2.dp, avatarBorder, CircleShape)
                            .clip(CircleShape),
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.ic_person_placeholder),
                    contentDescription = null,
                    modifier =
                        Modifier
                            .border(2.dp, avatarBorder, CircleShape)
                            .clip(CircleShape),
                )
            }
        }

        Column(verticalArrangement = spacedBy(2.dp)) {
            Text(
                text = list.name,
                style = TraktTheme.typography.paragraph,
                color = TraktTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                horizontalArrangement = spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.text_by),
                    style = TraktTheme.typography.paragraphSmall,
                    color = TraktTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = list.user.displayName,
                    style = TraktTheme.typography.paragraphSmall.copy(fontWeight = W700),
                    color = TraktTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview
@Composable
private fun Preview() {
    TraktTheme {
        val previewHandler =
            AsyncImagePreviewHandler {
                ColorImage(Color.LightGray.toArgb())
            }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            Column(
                verticalArrangement = spacedBy(16.dp),
            ) {
                CustomListCardContent(
                    list = PreviewData.customList1,
                    modifier = Modifier
                        .height(TraktTheme.size.customListCardSize)
                        .aspectRatio(HorizontalImageAspectRatio),
                    onClick = {},
                )

                CustomListCardContent(
                    list = PreviewData.customList1.copy(type = Type.ALL),
                    modifier = Modifier
                        .height(TraktTheme.size.customListCardSize)
                        .aspectRatio(HorizontalImageAspectRatio),
                    onClick = {},
                )
            }
        }
    }
}
