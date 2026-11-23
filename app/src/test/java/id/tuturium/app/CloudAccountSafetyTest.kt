package id.tuturium.app

import org.junit.Assert.*
import org.junit.Test

class CloudAccountSafetyTest {
    @Test fun firstAccountIsAllowed() {
        assertTrue(isCompatibleCloudAccount(null, "user@example.com"))
    }
    @Test fun sameAccountIsAllowedRegardlessOfCase() {
        assertTrue(isCompatibleCloudAccount("USER@example.com", "user@example.com"))
    }
    @Test fun differentAccountMustNotReceiveOldLocalBooks() {
        assertFalse(isCompatibleCloudAccount("first@example.com", "second@example.com"))
    }
    @Test fun emptyAccountIsNeverAllowed() {
        assertFalse(isCompatibleCloudAccount(null, " "))
    }
}
