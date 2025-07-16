package tv.trakt.app.tv.core.details.lists

import VipChip
import androidx.compose.foundation.BorderStroke
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
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Text
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import tv.trakt.app.tv.R
import tv.trakt.app.tv.common.model.CustomList
import tv.trakt.app.tv.common.model.CustomList.Type
import tv.trakt.app.tv.common.ui.mediacards.VerticalMediaCard
import tv.trakt.app.tv.helpers.preview.PreviewData
import tv.trakt.app.tv.ui.theme.TraktTheme

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
    val containerColor =
        when (list.type) {
            Type.OFFICIAL -> TraktTheme.colors.customOfficialListContainer
            else -> TraktTheme.colors.customListContainer
        }

    Card(
        onClick = onClick,
        modifier = modifier,
        shape =
            CardDefaults.shape(
                shape = RoundedCornerShape(12.dp),
            ),
        border =
            CardDefaults.border(
                focusedBorder =
                    Border(
                        border =
                            BorderStroke(
                                width = (2.75).dp,
                                color = TraktTheme.colors.accent,
                            ),
                        shape = RoundedCornerShape(12.dp),
                    ),
            ),
        colors =
            CardDefaults.colors(
                containerColor = containerColor,
                focusedContainerColor = containerColor,
            ),
        scale =
            CardDefaults.scale(
                focusedScale = 1.02f,
            ),
        content = {
            CustomListContent(
                list = list,
            )
        },
    )
}

@Composable
private fun CustomListContent(list: CustomList) {
    Column(
        verticalArrangement = spacedBy(0.dp, Alignment.CenterVertically),
        modifier =
            Modifier
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
                modifier =
                    Modifier
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
                        modifier =
                            Modifier
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
            if (list.user.images
                    ?.avatar
                    ?.full != null
            ) {
                AsyncImage(
                    model = list.user.images.avatar.full,
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

        Column(verticalArrangement = spacedBy(3.dp)) {
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
                    text = stringResource(R.string.custom_list_by),
                    style = TraktTheme.typography.paragraphSmall,
                    color = TraktTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = list.user.username,
                    style = TraktTheme.typography.paragraphSmall.copy(fontWeight = W700),
                    color = TraktTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (list.user.isAnyVip) {
                    VipChip()
                }
            }
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview
@Composable
fun Preview() {
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
                    modifier =
                        Modifier
                            .height(TraktTheme.size.detailsCustomListSize)
                            .aspectRatio(CardDefaults.HorizontalImageAspectRatio),
                    onClick = {},
                )

                CustomListCardContent(
                    list = PreviewData.customList1.copy(type = Type.ALL),
                    modifier =
                        Modifier
                            .height(TraktTheme.size.detailsCustomListSize)
                            .aspectRatio(CardDefaults.HorizontalImageAspectRatio),
                    onClick = {},
                )
            }
        }
    }
}
