package tv.trakt.trakt.ui.components.whatsnew

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import tv.trakt.trakt.BuildConfig
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.WhatsNew
import tv.trakt.trakt.common.ui.theme.colors.Purple400
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun WhatsNewView(
    data: WhatsNew,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
) {
    val context = LocalContext.current

    Column(
        verticalArrangement = spacedBy(32.dp),
        modifier = modifier,
    ) {
        Column {
            Text(
                text = stringResource(R.string.header_whats_new).uppercase(),
                style = TraktTheme.typography.heading3.copy(
                    letterSpacing = 0.em,
                ),
                color = TraktTheme.colors.textPrimary,
            )
            Text(
                text = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                style = TraktTheme.typography.meta,
                color = TraktTheme.colors.textSecondary,
            )
        }

        Column(
            verticalArrangement = spacedBy(32.dp),
        ) {
            data.notes.forEach { note ->
                Row(
                    horizontalArrangement = spacedBy(8.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_discover_off),
                        contentDescription = null,
                        tint = Purple400,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(14.dp),
                    )
                    Column(
                        verticalArrangement = spacedBy(2.dp),
                    ) {
                        Text(
                            text = note.title,
                            style = TraktTheme.typography.heading5,
                            color = TraktTheme.colors.textPrimary,
                        )
                        Text(
                            text = note.text,
                            style = TraktTheme.typography.paragraphSmaller,
                            color = TraktTheme.colors.textSecondary,
                        )
                    }
                }
            }
        }

        Column(
            verticalArrangement = spacedBy(42.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
        ) {
            Column(
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = spacedBy(4.dp),
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .onClick { openPlayStore(context) },
            ) {
                Text(
                    text = stringResource(R.string.header_rate_us_1),
                    style = TraktTheme.typography.paragraphSmaller.copy(
                        lineHeight = 20.sp,
                    ),
                    color = TraktTheme.colors.textPrimary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )

                Text(
                    text = stringResource(R.string.header_rate_us_2),
                    style = TraktTheme.typography.paragraphSmaller.copy(
                        lineHeight = 20.sp,
                    ),
                    color = TraktTheme.colors.textSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }

            PrimaryButton(
                text = "OK",
                onClick = onDismiss,
                containerColor = TraktTheme.colors.primaryButtonContainer,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

fun openPlayStore(context: Context) {
    val packageName = context.packageName
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri()))
    } catch (_: ActivityNotFoundException) {
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    "https://play.google.com/store/apps/details?id=$packageName".toUri(),
                ),
            )
        } catch (_: Exception) {
            // Ignore if no browser available
        }
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
    locale = "us",
)
@Composable
private fun Preview() {
    TraktTheme {
        WhatsNewView(
            data = WhatsNew(
                id = 1,
                versionName = "1.0.0",
                versionCode = 1,
                notes = listOf(
                    WhatsNew.WhatsNewNote(
                        title = "New Feature",
                        text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                    ),
                    WhatsNew.WhatsNewNote(
                        title = "Improvement",
                        text = "Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                    ),
                    WhatsNew.WhatsNewNote(
                        title = "Bug Fix",
                        text = "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi.",
                    ),
                ),
            ),
        )
    }
}
