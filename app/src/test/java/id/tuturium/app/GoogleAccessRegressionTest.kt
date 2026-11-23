package id.tuturium.app

import org.junit.Assert.*
import org.junit.Test

class GoogleAccessRegressionTest {
    @Test fun reauthIsDistinguishedFromOrdinaryCancellation() {
        assertTrue(isAccountReauthFailure("[16] Account reauth failed."))
        assertFalse(isAccountReauthFailure("[16] Cancelled by user"))
        assertFalse(isAccountReauthFailure(null))
    }

    @Test fun onlyVerifiedDriveIdentityCanReceiveTheLibrary() {
        assertTrue(isMatchingDriveAccount("owner@example.com", "OWNER@example.com"))
        assertFalse(isMatchingDriveAccount("owner@example.com", "other@example.com"))
        assertFalse(isMatchingDriveAccount("owner@example.com", null))
        assertFalse(isMatchingDriveAccount("", "owner@example.com"))
    }

    @Test fun resumableUploadUsesOnlyTheServerConfirmedOffset() {
        assertEquals(0L, nextDriveUploadOffset(null, 0, 511))
        assertEquals(256L, nextDriveUploadOffset("bytes=0-255", 0, 511))
        assertEquals(512L, nextDriveUploadOffset("bytes=0-511", 256, 511))
        assertThrows(CloudAccessException::class.java) { nextDriveUploadOffset("bytes=20-511", 256, 511) }
        assertThrows(CloudAccessException::class.java) { nextDriveUploadOffset("bytes=0-512", 256, 511) }
    }
}
