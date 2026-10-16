package tv.trakt.trakt.common.firebase.analytics.implementation

import org.junit.Assert.assertEquals
import org.junit.Test

class HalUserAgentTest {
    @Test
    fun `carries the device class tokens`() {
        val phone = halUserAgent("3.24.0", "14", HalDeviceKind.Phone)
        val tablet = halUserAgent("3.24.0", "14", HalDeviceKind.Tablet)
        val television = halUserAgent("3.24.0", "14", HalDeviceKind.Television)

        assertEquals("Trakt Android/3.24.0 (Linux; Android 14; Mobile)", phone)
        assertEquals("Trakt Android/3.24.0 (Linux; Android 14)", tablet)
        assertEquals("Trakt Android/3.24.0 (Linux; Android 14; TV)", television)
    }
}
