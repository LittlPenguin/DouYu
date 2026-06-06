package cn.edu.app.douyu.core

import cn.edu.app.douyu.core.navigation.AppRoute
import cn.edu.app.douyu.core.navigation.BottomTab
import cn.edu.app.douyu.feature.auth.loginNavigationSpec
import cn.edu.app.douyu.feature.auth.loginSuccessRoute
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteTest {
    @Test
    fun bottomTabRoutesAreUnique() {
        val routes = BottomTab.entries.map { it.route }
        assertEquals(routes.size, routes.toSet().size)
    }

    @Test
    fun bottomTabLabelsFollowOpenDesignCopy() {
        assertEquals(
            listOf("社区", "商城", "AI", "消息", "我的"),
            BottomTab.entries.map { it.label }
        )
    }

    @Test
    fun bottomTabIconSemanticsFollowOpenDesignDestinations() {
        assertEquals(
            listOf("community", "shop", "ai", "message", "profile"),
            BottomTab.entries.map { it.iconSemantic }
        )
    }

    @Test
    fun allAppRoutesAreUnique() {
        val routes = AppRoute.allRoutes
        assertEquals(routes.size, routes.toSet().size)
    }

    @Test
    fun stageThreeRoutesExposeSearchSettingsAndProfileEditTargets() {
        assertEquals("search", AppRoute.SEARCH)
        assertEquals("profile_edit", AppRoute.PROFILE_EDIT)
        assertTrue(AppRoute.allRoutes.contains(AppRoute.SEARCH))
        assertTrue(AppRoute.allRoutes.contains(AppRoute.PROFILE_EDIT))
    }

    @Test
    fun aiCameraEntryKeepsImageSelectAsPreviewOwner() {
        assertEquals("image_select", AppRoute.imageSelect())
        assertEquals("image_select?openCamera=true", AppRoute.imageSelect(openCamera = true))
        assertTrue(AppRoute.allRoutes.contains(AppRoute.IMAGE_SELECT_ROUTE))
    }

    @Test
    fun stageClosureRoutesExposeNotificationAndSettingsSubpages() {
        assertEquals("notification_detail/{notificationId}?title={title}&content={content}&type={type}&createdAt={createdAt}", AppRoute.NOTIFICATION_DETAIL)
        assertEquals("settings_section/{section}", AppRoute.SETTINGS_SECTION)
        assertEquals("notification_detail/notice_1?title=%E8%AE%A2%E5%8D%95&content=%E5%B7%B2%E6%9B%B4%E6%96%B0&type=ORDER&createdAt=2026-06-04T16%3A30%3A00", AppRoute.notificationDetail("notice_1", "订单", "已更新", "ORDER", "2026-06-04T16:30:00"))
        assertEquals("settings_section/privacy", AppRoute.settingsSection("privacy"))
        assertTrue(AppRoute.allRoutes.contains(AppRoute.NOTIFICATION_DETAIL))
        assertTrue(AppRoute.allRoutes.contains(AppRoute.SETTINGS_SECTION))
    }

    @Test
    fun profileInteractionPostAssetRoutesKeepSmokeContract() {
        assertEquals(
            listOf(
                AppRoute.LIKED_POSTS,
                AppRoute.COMMENTED_POSTS,
                AppRoute.FAVORITE_POSTS,
                AppRoute.FOLLOWED_POSTS
            ),
            AppRoute.profileInteractionPostAssetRoutes
        )
    }

    @Test
    fun loginRouteCanCarryBottomTabReturnTarget() {
        assertEquals("login_return?returnTo=message", AppRoute.login(BottomTab.MESSAGE.route))
    }

    @Test
    fun loginSuccessUsesReturnTargetWhenProvided() {
        assertEquals(BottomTab.MESSAGE.route, loginSuccessRoute(BottomTab.MESSAGE.route))
    }

    @Test
    fun loginSuccessDefaultsToProfileWithoutReturnTarget() {
        assertEquals(BottomTab.PROFILE.route, loginSuccessRoute(null))
    }

    @Test
    fun loginNavigationForMessageReturnUsesBottomTabStackReset() {
        val spec = loginNavigationSpec(BottomTab.MESSAGE.route)

        assertEquals(BottomTab.MESSAGE.route, spec.targetRoute)
        assertTrue(spec.resetBottomTabStack)
        assertNull(spec.popUpToRoute)
    }

    @Test
    fun loginNavigationDefaultsToProfileAsBottomTab() {
        val spec = loginNavigationSpec(null)

        assertEquals(BottomTab.PROFILE.route, spec.targetRoute)
        assertTrue(spec.resetBottomTabStack)
        assertNull(spec.popUpToRoute)
    }

    @Test
    fun loginNavigationForNonTabReturnRemovesLoginRoute() {
        val spec = loginNavigationSpec(AppRoute.POST_CREATE)

        assertEquals(AppRoute.POST_CREATE, spec.targetRoute)
        assertFalse(spec.resetBottomTabStack)
        assertEquals(AppRoute.LOGIN_ROUTE, spec.popUpToRoute)
    }
}
