package cn.edu.app.douyu.core

import cn.edu.app.douyu.core.navigation.AppRoute
import cn.edu.app.douyu.core.navigation.BottomTab
import org.junit.Assert.assertEquals
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
}
