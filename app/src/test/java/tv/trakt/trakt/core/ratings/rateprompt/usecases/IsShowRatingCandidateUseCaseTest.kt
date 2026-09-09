package tv.trakt.trakt.core.ratings.rateprompt.usecases

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import tv.trakt.trakt.common.model.EpisodeType.MID_SEASON_FINALE
import tv.trakt.trakt.common.model.EpisodeType.MID_SEASON_PREMIERE
import tv.trakt.trakt.common.model.EpisodeType.SEASON_FINALE
import tv.trakt.trakt.common.model.EpisodeType.SEASON_PREMIERE
import tv.trakt.trakt.common.model.EpisodeType.SERIES_FINALE
import tv.trakt.trakt.common.model.EpisodeType.SERIES_PREMIERE

class IsShowRatingCandidateUseCaseTest {
    private val useCase = IsShowRatingCandidateUseCase()

    @Test
    fun `series finale is a candidate`() {
        assertTrue(useCase(episodeType = SERIES_FINALE, showPlaysInBingeWindow = 0))
    }

    @Test
    fun `season finale is a candidate`() {
        assertTrue(useCase(episodeType = SEASON_FINALE, showPlaysInBingeWindow = 0))
    }

    @Test
    fun `mid season finale is a candidate`() {
        assertTrue(useCase(episodeType = MID_SEASON_FINALE, showPlaysInBingeWindow = 0))
    }

    @Test
    fun `premieres are not candidates on their own`() {
        assertFalse(useCase(episodeType = SERIES_PREMIERE, showPlaysInBingeWindow = 0))
        assertFalse(useCase(episodeType = SEASON_PREMIERE, showPlaysInBingeWindow = 0))
        assertFalse(useCase(episodeType = MID_SEASON_PREMIERE, showPlaysInBingeWindow = 0))
    }

    @Test
    fun `unknown episode type is not a candidate on its own`() {
        assertFalse(useCase(episodeType = null, showPlaysInBingeWindow = 0))
    }

    @Test
    fun `binge threshold makes a non finale a candidate`() {
        assertTrue(
            useCase(
                episodeType = null,
                showPlaysInBingeWindow = SHOW_BINGE_EPISODE_THRESHOLD,
            ),
        )
    }

    @Test
    fun `one play below the binge threshold is not a candidate`() {
        assertFalse(
            useCase(
                episodeType = null,
                showPlaysInBingeWindow = SHOW_BINGE_EPISODE_THRESHOLD - 1,
            ),
        )
    }

    @Test
    fun `plays above the binge threshold stay candidates`() {
        assertTrue(
            useCase(
                episodeType = SERIES_PREMIERE,
                showPlaysInBingeWindow = SHOW_BINGE_EPISODE_THRESHOLD + 5,
            ),
        )
    }
}
