package tv.trakt.trakt.common.ui.theme.colors.seasonal

import androidx.compose.ui.graphics.Color
import tv.trakt.trakt.common.ui.theme.colors.DarkColors
import tv.trakt.trakt.common.ui.theme.colors.LightColors
import tv.trakt.trakt.common.ui.theme.colors.Red100
import tv.trakt.trakt.common.ui.theme.colors.Red200
import tv.trakt.trakt.common.ui.theme.colors.Red300
import tv.trakt.trakt.common.ui.theme.colors.Red400
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.common.ui.theme.colors.Red600
import tv.trakt.trakt.common.ui.theme.colors.Red700
import tv.trakt.trakt.common.ui.theme.colors.Red800
import tv.trakt.trakt.common.ui.theme.colors.Red900
import tv.trakt.trakt.common.ui.theme.colors.Red920
import tv.trakt.trakt.common.ui.theme.colors.Red940
import tv.trakt.trakt.common.ui.theme.colors.RedPalette
import tv.trakt.trakt.common.ui.theme.colors.TraktColors

private val ChristmasBackground = Color(0xFF141719)

val ChristmasDarkColors: TraktColors = DarkColors.copy(
    backgroundPrimary = ChristmasBackground,
    accent = Red500,
    accentPalette = RedPalette,
    vipAccent = Red500,
    navigationContentOn = Red400,
    dropdownContentActive = Red500,
    customListGradient = Red900,
    sentimentsContainer = Red940,
    sentimentsGoodAccent = Red100,
    triviaContainer = Red940,
    detailsStatus1 = Red300,
    detailsStatus2 = Red100,
    streakLevel1 = Red920,
    streakLevel2 = Red700,
    streakLevel3 = Red500,
    streakLevel4 = Red300,
    streakAccent = Red500,
    streakValue = Red400,
    screenTimeHigh = Red200,
    screenTimeMedium = Red500,
    screenTimeLow = Red900,
    statsCardGradient = Red700,
    settingsHeader = Red300,
    settingsLinkedStatus = Red400,
    rateBannerContainer = Red600,
    primaryButtonContainer = Red500,
    switchContainerChecked = Red800,
    switchThumbChecked = Red500,
    switchThumbUnchecked = Red500,
)

val ChristmasLightColors: TraktColors = LightColors.copy(
    accent = Red500,
    accentPalette = RedPalette,
    vipAccent = Red500,
    navigationContentOn = Red600,
    dropdownContainerActive = Red500,
    customListGradient = Red200,
    sentimentsContainer = Red100,
    sentimentsGoodAccent = Red800,
    triviaContainer = Red100,
    detailsStatus1 = Red600,
    detailsStatus2 = Red800,
    streakLevel1 = Red200,
    streakLevel2 = Red400,
    streakLevel3 = Red600,
    streakLevel4 = Red800,
    streakAccent = Red500,
    streakValue = Red500,
    screenTimeHigh = Red200,
    screenTimeMedium = Red500,
    screenTimeLow = Red900,
    statsCardGradient = Red700,
    settingsHeader = Red600,
    settingsLinkedStatus = Red600,
    rateBannerContainer = Red600,
    primaryButtonContainer = Red500,
    switchContainerChecked = Red400,
    switchThumbChecked = Red600,
)
