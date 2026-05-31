package cn.edu.app.douyu.feature.ai

import cn.edu.app.douyu.core.navigation.AppRoute
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiUploadNavigationTest {
    @Test
    fun uploadRequiresLoginBeforeStartingNetworkRequest() {
        assertFalse(canStartAiUpload(isLoggedIn = false, hasSelectedImage = true))
        assertFalse(canStartAiUpload(isLoggedIn = true, hasSelectedImage = false))
        assertTrue(canStartAiUpload(isLoggedIn = true, hasSelectedImage = true))
    }

    @Test
    fun unauthenticatedUploadReturnsToImageSelectAfterLogin() {
        assertEquals(AppRoute.login(AppRoute.IMAGE_SELECT), aiUploadLoginRoute())
    }

    @Test
    fun uploadedFileIdBuildsParamsRoute() {
        assertEquals("ai_params/file_123", aiParamsRouteOrNull("file_123"))
        assertNull(aiParamsRouteOrNull(null))
    }
}
