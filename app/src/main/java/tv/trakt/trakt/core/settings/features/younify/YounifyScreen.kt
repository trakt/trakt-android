@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.settings.features.younify

import android.content.Intent
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.text.font.FontWeight.Companion.W600
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import timber.log.Timber
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.Config
import tv.trakt.trakt.common.helpers.DynamicStringResource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.common.ui.theme.colors.Purple400
import tv.trakt.trakt.common.ui.theme.colors.Red400
import tv.trakt.trakt.core.settings.features.younify.model.LinkStatus
import tv.trakt.trakt.core.settings.features.younify.model.linkStatus
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.buttons.TertiaryButton
import tv.trakt.trakt.ui.components.confirmation.ConfirmationSheet
import tv.trakt.trakt.ui.theme.TraktTheme
import tv.younify.sdk.connect.StreamingService

@Composable
internal fun YounifyScreen(
    viewModel: YounifyViewModel,
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val snack = LocalSnackbarState.current
    val registry = LocalActivityResultRegistryOwner.current

    val state by viewModel.state.collectAsStateWithLifecycle()

    var confirmUnlinkSheet by remember { mutableStateOf<StreamingService?>(null) }

    YounifyScreenContent(
        state = state,
        onServiceActionClick = {
            viewModel.onServiceAction(
                service = it,
                context = context,
                registry = registry?.activityResultRegistry,
            )
        },
        onServiceEditClick = {
            viewModel.onServiceEdit(
                service = it,
                context = context,
                registry = registry?.activityResultRegistry,
            )
        },
        onServiceUnlinkClick = {
            confirmUnlinkSheet = it
        },
        onSendLogsClick = {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = "mailto:".toUri()
                putExtra(Intent.EXTRA_EMAIL, arrayOf(Config.WEB_SUPPORT_MAIL))
                putExtra(Intent.EXTRA_SUBJECT, "Automatic Tracking - Logs")
                putExtra(Intent.EXTRA_TEXT, state.logs.joinToString("\n"))
            }

            try {
                context.startActivity(intent)
            } catch (error: Exception) {
                // No email client installed
                Timber.w(error, "Unable to start email client")
            }
        },
        onAllSettingsClick = {
            uriHandler.openUri(Config.WEB_SETTINGS_SCROBBLING_URL)
        },
        onBackClick = onNavigateBack,
    )

    ConfirmationSheet(
        active = !state.syncDataPrompt.isNullOrEmpty(),
        title = stringResource(R.string.dialog_title_younify_data_sync),
        message = stringResource(R.string.dialog_prompt_younify_data_sync),
        yesText = stringResource(R.string.button_text_data_sync_yes),
        noText = stringResource(R.string.button_text_data_sync_ignore),
        onYes = {
            state.syncDataPrompt?.let { serviceId ->
                viewModel.notifyYounifyRefresh(
                    serviceId = serviceId,
                    syncData = true,
                    info = DynamicStringResource(R.string.text_info_younify_service_linked),
                )
            }
        },
        onNo = {
            state.syncDataPrompt?.let { serviceId ->
                viewModel.notifyYounifyRefresh(
                    serviceId = serviceId,
                    syncData = false,
                    info = DynamicStringResource(R.string.text_info_younify_service_linked),
                )
            }
        },
    )

    ConfirmationSheet(
        active = confirmUnlinkSheet != null,
        title = stringResource(R.string.button_text_younify_unlink),
        message = stringResource(
            R.string.warning_prompt_younify_unlink,
            confirmUnlinkSheet?.name ?: "",
        ),
        onYes = {
            confirmUnlinkSheet?.let {
                viewModel.onServiceUnlink(it)
                confirmUnlinkSheet = null
            }
        },
        yesColor = Red400,
        onNo = {
            confirmUnlinkSheet = null
        },
    )

    LaunchedEffect(state.info) {
        if (state.info == null) {
            return@LaunchedEffect
        }

        state.info?.get(context)?.let {
            snack.showSnackbar(it)
        }

        viewModel.clearInfo()
    }
}

@Composable
private fun YounifyScreenContent(
    state: YounifyState,
    modifier: Modifier = Modifier,
    onServiceActionClick: (StreamingService) -> Unit = { },
    onServiceUnlinkClick: (StreamingService) -> Unit = { },
    onServiceEditClick: (StreamingService) -> Unit = { },
    onSendLogsClick: () -> Unit = { },
    onAllSettingsClick: () -> Unit = { },
    onBackClick: () -> Unit = { },
) {
    val context = LocalContext.current

    val contentPadding = PaddingValues(
        start = TraktTheme.spacing.mainPageHorizontalSpace,
        end = TraktTheme.spacing.mainPageHorizontalSpace,
        top = WindowInsets.statusBars.asPaddingValues()
            .calculateTopPadding()
            .plus(4.dp),
        bottom = WindowInsets.navigationBars.asPaddingValues()
            .calculateBottomPadding()
            .plus(TraktTheme.size.navigationBarHeight * 2),
    )

    val scrollConnection = rememberSaveable(saver = SimpleScrollConnection.Saver) {
        SimpleScrollConnection()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .nestedScroll(scrollConnection),
    ) {
        ScrollableBackdropImage(
            translation = scrollConnection.resultOffset,
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
                onSendLogsClick = onSendLogsClick,
                onAllSettingsClick = onAllSettingsClick,
                modifier = Modifier
                    .onClick {
                        onBackClick()
                    },
            )

            state.error?.let { error ->
                Text(
                    text = error.get(context),
                    style = TraktTheme.typography.paragraphSmaller,
                    color = Red400,
                    modifier = Modifier
                        .padding(
                            top = 8.dp,
                            bottom = 16.dp,
                        ),
                )
            }

            if (state.loading.isDone && !state.younifyServices.isNullOrEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    for (service in state.younifyServices) {
                        YounifyServiceView(
                            service = service,
                            onActionClick = {
                                onServiceActionClick(service)
                            },
                            onEditClick = {
                                onServiceEditClick(service)
                            },
                            onUnlinkClick = {
                                onServiceUnlinkClick(service)
                            },
                            modifier = Modifier
                                .fillMaxWidth(),
                        )
                    }
                }
            }
        }

        if (state.loading == LoadingState.LOADING) {
            FilmProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(contentPadding),
            )
        }
    }
}

@Composable
private fun YounifyServiceView(
    service: StreamingService,
    modifier: Modifier = Modifier,
    onActionClick: () -> Unit = { },
    onEditClick: () -> Unit = { },
    onUnlinkClick: () -> Unit = { },
) {
    val context = LocalContext.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .background(
                color = TraktTheme.colors.panelCardContainer,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(
                top = 5.dp,
                bottom = 5.dp,
                start = 5.dp,
                end = 16.dp,
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1F, fill = false),
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(service.smallThumbnailUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp)),
            )

            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Text(
                    text = service.name,
                    style = TraktTheme.typography.cardTitle.copy(fontSize = 15.sp),
                    color = TraktTheme.colors.textPrimary,
                )

                Text(
                    text = stringResource(service.linkStatus.displayTextRes),
                    style = TraktTheme.typography.meta.copy(
                        fontWeight = when (service.linkStatus) {
                            LinkStatus.LINKED, LinkStatus.BROKEN -> W600
                            else -> W400
                        },
                    ),
                    color = when (service.linkStatus) {
                        LinkStatus.LINKED -> Purple400
                        LinkStatus.BROKEN -> Red400
                        else -> TraktTheme.colors.textSecondary
                    },
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    service.link?.username?.let { username ->
                        Text(
                            text = username,
                            style = TraktTheme.typography.meta.copy(
                                fontWeight = W400,
                            ),
                            color = TraktTheme.colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    service.link?.profileName?.let { profile ->
                        Text(
                            text = "($profile)",
                            style = TraktTheme.typography.meta.copy(
                                fontWeight = W400,
                            ),
                            color = TraktTheme.colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }

        if (service.linkStatus != LinkStatus.LINKED) {
            TertiaryButton(
                text = when (service.linkStatus) {
                    LinkStatus.UNLINKED -> stringResource(R.string.button_text_login)
                    LinkStatus.BROKEN -> stringResource(R.string.button_text_younify_fix)
                    LinkStatus.LINKED -> ""
                },
                containerColor = when (service.linkStatus) {
                    LinkStatus.UNLINKED -> TraktTheme.colors.primaryButtonContainer
                    LinkStatus.BROKEN -> Red400
                    LinkStatus.LINKED -> TraktTheme.colors.primaryButtonContainer
                },
                onClick = onActionClick,
            )
        } else if (service.linkStatus == LinkStatus.LINKED) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(start = 12.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_edit),
                    contentDescription = null,
                    tint = TraktTheme.colors.textPrimary,
                    modifier = Modifier
                        .size(18.dp)
                        .onClick(onClick = onEditClick),
                )

                Icon(
                    painter = painterResource(R.drawable.ic_trash),
                    contentDescription = null,
                    tint = TraktTheme.colors.textPrimary,
                    modifier = Modifier
                        .size(21.dp)
                        .onClick(onClick = onUnlinkClick),
                )
            }
        }
    }
}

@Composable
private fun TitleBar(
    modifier: Modifier = Modifier,
    onAllSettingsClick: () -> Unit = { },
    onSendLogsClick: () -> Unit = { },
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
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
            TraktHeader(
                title = stringResource(R.string.text_automatic_tracking),
            )
        }

        Box {
            var showMenu by remember { mutableStateOf(false) }

            Icon(
                painter = painterResource(R.drawable.ic_more_vertical),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier
                    .size(18.dp)
                    .onClick {
                        showMenu = true
                    },
            )

            DropdownMenu(
                expanded = showMenu,
                containerColor = TraktTheme.colors.dialogContainer,
                shape = RoundedCornerShape(16.dp),
                onDismissRequest = {
                    showMenu = false
                },
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.text_all_settings),
                            style = TraktTheme.typography.buttonTertiary,
                            color = TraktTheme.colors.textPrimary,
                            modifier = Modifier.onClick(
                                onClick = onAllSettingsClick,
                            ),
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_external),
                            contentDescription = null,
                            tint = TraktTheme.colors.textPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    onClick = {
                        showMenu = false
                    },
                )

                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.button_text_younify_send_logs),
                            style = TraktTheme.typography.buttonTertiary,
                            color = TraktTheme.colors.textPrimary,
                            modifier = Modifier.onClick(
                                onClick = onSendLogsClick,
                            ),
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_share),
                            contentDescription = null,
                            tint = TraktTheme.colors.textPrimary,
                            modifier = Modifier.size(22.dp),
                        )
                    },
                    onClick = {
                        showMenu = false
                    },
                )
            }
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
        YounifyScreenContent(
            state = YounifyState(),
        )
    }
}
