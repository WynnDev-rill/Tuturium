package id.tuturium.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GoogleUpdateRegressionTest {
    @Test fun driveConsentUsesTheSelectedAccountAndOnlyAppData() {
        val request = driveAuthorization("owner@example.com")
        assertEquals("owner@example.com", request.account?.name)
        assertEquals("com.google", request.account?.type)
        assertEquals(listOf(DRIVE_APPDATA_SCOPE), request.requestedScopes.map { it.scopeUri })
    }

    @Test fun previouslyPublishedReleaseIsOlderThanInstalled135() {
        assertNull(Updates.parseLatestRelease(JSONObject().put("tag_name", "v1.0.0"), "1.3.5"))
    }

    @Test fun validNewerApkIsPresentedTo135ButNotTo136() {
        val release = release()
        assertEquals("1.3.6", Updates.parseLatestRelease(release, "1.3.5")?.version)
        assertNull(Updates.parseLatestRelease(release, "1.3.6"))
        release.put("prerelease", true)
        assertNull(Updates.parseLatestRelease(release, "1.3.5"))
    }

    @Test fun newerReleaseWithoutTrustedApkIsAnError() {
        assertThrows(UpdateCheckException::class.java) {
            Updates.parseLatestRelease(release(url = "https://example.com/Tuturium.apk"), "1.3.5")
        }
        assertThrows(UpdateCheckException::class.java) {
            Updates.parseLatestRelease(JSONObject().put("tag_name", "v1.3.6"), "1.3.5")
        }
    }

    private fun release(url: String = "https://github.com/WynnDev-rill/Tuturium/releases/download/v1.3.6/Tuturium.apk") =
        JSONObject().put("tag_name", "v1.3.6").put("assets", JSONArray().put(
            JSONObject().put("name", "Tuturium.apk").put("state", "uploaded")
                .put("browser_download_url", url).put("digest", "sha256:" + "a".repeat(64))
                .put("size", 10_000)
        ))
}
