package tv.trakt.trakt.common.ui.theme.colors.seasonal

import androidx.compose.ui.graphics.Color
import tv.trakt.trakt.common.ui.theme.colors.DarkColors
import tv.trakt.trakt.common.ui.theme.colors.Emerald100
import tv.trakt.trakt.common.ui.theme.colors.Emerald200
import tv.trakt.trakt.common.ui.theme.colors.Emerald300
import tv.trakt.trakt.common.ui.theme.colors.Emerald400
import tv.trakt.trakt.common.ui.theme.colors.Emerald500
import tv.trakt.trakt.common.ui.theme.colors.Emerald600
import tv.trakt.trakt.common.ui.theme.colors.Emerald700
import tv.trakt.trakt.common.ui.theme.colors.Emerald800
import tv.trakt.trakt.common.ui.theme.colors.Emerald900
import tv.trakt.trakt.common.ui.theme.colors.Emerald920
import tv.trakt.trakt.common.ui.theme.colors.Emerald940
import tv.trakt.trakt.common.ui.theme.colors.EmeraldPalette
import tv.trakt.trakt.common.ui.theme.colors.LightColors
import tv.trakt.trakt.common.ui.theme.colors.TraktColors

private val DoomsdayBackground = Color(0xFF0C0D0C)

val DoomsdayDarkColors: TraktColors = DarkColors.copy(
    backgroundPrimary = DoomsdayBackground,
    accent = Emerald500,
    accentPalette = EmeraldPalette,
    vipAccent = Emerald500,
    navigationContentOn = Emerald400,
    dropdownContentActive = Emerald500,
    customListGradient = Emerald900,
    sentimentsContainer = Emerald940,
    sentimentsGoodAccent = Emerald100,
    triviaContainer = Emerald940,
    detailsStatus1 = Emerald300,
    detailsStatus2 = Emerald100,
    streakLevel1 = Emerald920,
    streakLevel2 = Emerald700,
    streakLevel3 = Emerald500,
    streakLevel4 = Emerald300,
    streakAccent = Emerald500,
    streakValue = Emerald400,
    screenTimeHigh = Emerald200,
    screenTimeMedium = Emerald500,
    screenTimeLow = Emerald900,
    statsCardGradient = Emerald700,
    settingsHeader = Emerald300,
    settingsLinkedStatus = Emerald400,
    rateBannerContainer = Emerald600,
    primaryButtonContainer = Emerald500,
    switchContainerChecked = Emerald800,
    switchThumbChecked = Emerald500,
    switchThumbUnchecked = Emerald500,
)

val DoomsdayLightColors: TraktColors = LightColors.copy(
    accent = Emerald500,
    accentPalette = EmeraldPalette,
    vipAccent = Emerald500,
    navigationContentOn = Emerald600,
    dropdownContainerActive = Emerald500,
    customListGradient = Emerald200,
    sentimentsContainer = Emerald100,
    sentimentsGoodAccent = Emerald800,
    triviaContainer = Emerald100,
    detailsStatus1 = Emerald600,
    detailsStatus2 = Emerald800,
    streakLevel1 = Emerald200,
    streakLevel2 = Emerald400,
    streakLevel3 = Emerald600,
    streakLevel4 = Emerald800,
    streakAccent = Emerald500,
    streakValue = Emerald500,
    screenTimeHigh = Emerald200,
    screenTimeMedium = Emerald500,
    screenTimeLow = Emerald900,
    statsCardGradient = Emerald700,
    settingsHeader = Emerald600,
    settingsLinkedStatus = Emerald600,
    rateBannerContainer = Emerald600,
    primaryButtonContainer = Emerald500,
    switchContainerChecked = Emerald400,
    switchThumbChecked = Emerald600,
)
