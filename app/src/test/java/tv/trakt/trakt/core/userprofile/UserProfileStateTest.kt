package tv.trakt.trakt.core.userprofile

import org.junit.Assert.assertEquals
import org.junit.Test
import tv.trakt.trakt.common.helpers.preview.PreviewData

class UserProfileStateTest {
    @Test
    fun `public profile grants access while following status is loading`() {
        val state = UserProfileState(
            user = PreviewData.user1.copy(isPrivate = false),
            userFollowing = UserProfileState.FollowingState(
                following = false,
                loading = true,
            ),
        )

        assertEquals(UserProfileState.Access.Granted, state.access)
    }

    @Test
    fun `private profile checks access while following status is loading`() {
        val state = UserProfileState(
            user = PreviewData.user1.copy(isPrivate = true),
            userFollowing = UserProfileState.FollowingState(
                following = false,
                loading = true,
            ),
        )

        assertEquals(UserProfileState.Access.Checking, state.access)
    }

    @Test
    fun `private profile grants access to approved follower`() {
        val state = UserProfileState(
            user = PreviewData.user1.copy(isPrivate = true),
            accessChecking = false,
            userFollowing = UserProfileState.FollowingState(
                following = true,
                loading = false,
            ),
        )

        assertEquals(UserProfileState.Access.Granted, state.access)
    }

    @Test
    fun `private profile grants access to profile owner`() {
        val state = UserProfileState(
            user = PreviewData.user1.copy(isPrivate = true),
            isCurrentUser = true,
            userFollowing = UserProfileState.FollowingState(
                following = false,
                loading = true,
            ),
        )

        assertEquals(UserProfileState.Access.Granted, state.access)
    }

    @Test
    fun `private profile denies access to non follower`() {
        val state = UserProfileState(
            user = PreviewData.user1.copy(isPrivate = true),
            accessChecking = false,
            userFollowing = UserProfileState.FollowingState(
                following = false,
                loading = false,
            ),
        )

        assertEquals(UserProfileState.Access.Denied, state.access)
    }

    @Test
    fun `private profile stays denied while follow request is loading`() {
        val state = UserProfileState(
            user = PreviewData.user1.copy(isPrivate = true),
            accessChecking = false,
            userFollowing = UserProfileState.FollowingState(
                following = false,
                loading = true,
            ),
        )

        assertEquals(UserProfileState.Access.Denied, state.access)
    }
}
