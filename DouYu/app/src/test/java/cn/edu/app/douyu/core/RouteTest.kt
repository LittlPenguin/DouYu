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
    fun allAppRoutesAreUnique() {
        val routes = AppRoute.allRoutes
        assertEquals(routes.size, routes.toSet().size)
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
