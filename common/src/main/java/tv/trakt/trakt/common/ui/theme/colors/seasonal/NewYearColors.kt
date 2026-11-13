package tv.trakt.trakt.common.ui.theme.colors.seasonal

import androidx.compose.ui.graphics.Color
import tv.trakt.trakt.common.ui.theme.colors.Amber100
import tv.trakt.trakt.common.ui.theme.colors.Amber200
import tv.trakt.trakt.common.ui.theme.colors.Amber300
import tv.trakt.trakt.common.ui.theme.colors.Amber400
import tv.trakt.trakt.common.ui.theme.colors.Amber500
import tv.trakt.trakt.common.ui.theme.colors.Amber600
import tv.trakt.trakt.common.ui.theme.colors.Amber700
import tv.trakt.trakt.common.ui.theme.colors.Amber800
import tv.trakt.trakt.common.ui.theme.colors.Amber900
import tv.trakt.trakt.common.ui.theme.colors.Amber940
import tv.trakt.trakt.common.ui.theme.colors.AmberPalette
import tv.trakt.trakt.common.ui.theme.colors.DarkColors
import tv.trakt.trakt.common.ui.theme.colors.LightColors
import tv.trakt.trakt.common.ui.theme.colors.TraktColors

private val NewYearBackground = Color(0xFF15161D)

val NewYearDarkColors: TraktColors = DarkColors.copy(
    backgroundPrimary = NewYearBackground,
    accent = Amber500,
    accentPalette = AmberPalette,
    vipAccent = Amber500,
    navigationContentOn = Amber400,
    dropdownContentActive = Amber500,
    customListGradient = Amber900,
    sentimentsContainer = Amber940,
    sentimentsGoodAccent = Amber100,
    triviaContainer = Amber940,
    detailsStatus1 = Amber300,
    detailsStatus2 = Amber100,
    streakLevel1 = Amber900,
    streakLevel2 = Amber700,
    streakLevel3 = Amber500,
    streakLevel4 = Amber300,
    streakAccent = Amber500,
    streakValue = Amber400,
    screenTimeHigh = Amber200,
    screenTimeMedium = Amber500,
    screenTimeLow = Amber900,
    statsCardGradient = Amber700,
    settingsHeader = Amber300,
    settingsLinkedStatus = Amber400,
    rateBannerContainer = Amber600,
    primaryButtonContainer = Amber500,
    switchContainerChecked = Amber800,
    switchThumbChecked = Amber500,
    switchThumbUnchecked = Amber500,
)

val NewYearLightColors: TraktColors = LightColors.copy(
    accent = Amber500,
    accentPalette = AmberPalette,
    vipAccent = Amber500,
    navigationContentOn = Amber600,
    dropdownContainerActive = Amber500,
    customListGradient = Amber200,
    sentimentsContainer = Amber100,
    sentimentsGoodAccent = Amber800,
    triviaContainer = Amber100,
    detailsStatus1 = Amber600,
    detailsStatus2 = Amber800,
    streakLevel1 = Amber200,
    streakLevel2 = Amber400,
    streakLevel3 = Amber600,
    streakLevel4 = Amber800,
    streakAccent = Amber500,
    streakValue = Amber500,
    screenTimeHigh = Amber200,
    screenTimeMedium = Amber500,
    screenTimeLow = Amber900,
    statsCardGradient = Amber700,
    settingsHeader = Amber600,
    settingsLinkedStatus = Amber600,
    rateBannerContainer = Amber600,
    primaryButtonContainer = Amber500,
    switchContainerChecked = Amber400,
    switchThumbChecked = Amber600,
)
