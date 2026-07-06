@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.home.sections.welcome.banner

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.core.home.sections.welcome.sheet.WelcomeSheet
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.theme.TraktTheme

private val viewShape = RoundedCornerShape(20.dp)

@Composable
internal fun HomeWelcomeView(
    modifier: Modifier = Modifier,
    onDiscoverClick: () -> Unit = {},
    onImportClick: () -> Unit = {},
    onDismissClick: () -> Unit = {},
) {
    var welcomeSheet by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, viewShape)
            .background(TraktTheme.colors.dialogContainer, viewShape)
            .padding(18.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_trakt_icon),
                    contentDescription = null,
                    modifier = Modifier
                        .height(22.dp),
                )
                Text(
                    text = stringResource(R.string.welcome_banner_heading),
                    style = TraktTheme.typography.heading5,
                    color = TraktTheme.colors.textPrimary,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = true),
                )

                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = null,
                    tint = TraktTheme.colors.textPrimary,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(18.dp)
                        .onClick(onClick = onDismissClick),
                )
            }

            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .onClick {
                        welcomeSheet = true
                    },
            ) {
                Text(
                    text = stringResource(R.string.welcome_banner_description),
                    style = TraktTheme.typography.paragraphSmaller.copy(
                        lineHeight = 1.3.em,
                    ),
                    color = TraktTheme.colors.textSecondary,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f, fill = true),
                )

                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = TraktTheme.colors.textSecondary,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(18.dp),
                )
            }
        }

        PrimaryButton(
            text = stringResource(R.string.welcome_banner_action),
            onClick = onImportClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
        )
    }

    WelcomeSheet(
        visible = welcomeSheet,
        onGetStartedClick = onDiscoverClick,
        onDismiss = { welcomeSheet = false },
    )
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
)
@Composable
private fun Preview() {
    TraktTheme {
        HomeWelcomeView(
            modifier = Modifier
                .width(450.dp)
                .padding(32.dp),
        )
    }
}
