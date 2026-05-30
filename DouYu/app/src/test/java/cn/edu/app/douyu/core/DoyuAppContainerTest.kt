package cn.edu.app.douyu.core

import cn.edu.app.douyu.core.data.DoyuAppContainer
import cn.edu.app.douyu.core.network.InMemoryTokenStore
import org.junit.Assert.assertFalse
import org.junit.Test

class DoyuAppContainerTest {
    @Test
    fun containerDoesNotUseDirectInMemoryTokenStore() {
        val field = DoyuAppContainer::class.java.getDeclaredField("tokenStore")
        field.isAccessible = true

        assertFalse(field.get(DoyuAppContainer) is InMemoryTokenStore)
    }
}
