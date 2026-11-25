package tv.trakt.trakt.core.vipveteran.usecases

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.auth.session.helpers.distinctUntilSessionChanged
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.featureflags.model.FeatureFlag
import tv.trakt.trakt.core.featureflags.usecases.FeatureFlagsUseCase
import tv.trakt.trakt.core.userprofile.usecases.GetUserProfileDetailsUseCase
import tv.trakt.trakt.core.vipveteran.model.UserTierState

internal class ObserveUserTierStateUseCase(
    private val featureFlagsUseCase: FeatureFlagsUseCase,
    private val sessionManager: SessionManager,
    private val getUserProfileDetailsUseCase: GetUserProfileDetailsUseCase,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observe(): Flow<UserTierState> {
        return featureFlagsUseCase.observeEnabled(FeatureFlag.VipVeteran)
            .distinctUntilChanged()
            .flatMapLatest { enabled ->
                when {
                    enabled -> observeOwnVeteran()
                    else -> flowOf(UserTierState())
                }
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeOwnVeteran(): Flow<UserTierState> {
        return sessionManager.observeProfile()
            .distinctUntilSessionChanged()
            .flatMapLatest { user ->
                flow {
                    val base = UserTierState(
                        enabled = true,
                        ownId = user?.ids?.trakt,
                    )
                    emit(base)

                    if (user == null || !user.isAnyVip) return@flow

                    try {
                        val veteran = getUserProfileDetailsUseCase
                            .getUserProfileDetails(user.ids.trakt)
                            .veteran
                        emit(base.copy(ownVeteran = veteran))
                    } catch (error: Exception) {
                        error.rethrowCancellation {
                            Timber.w(error, "Own VIP veteran load failed")
                        }
                    }
                }
            }
    }
}
