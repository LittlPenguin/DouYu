package cn.edu.app.douyu.core

import cn.edu.app.douyu.core.data.MockCommerceRepository
import cn.edu.app.douyu.core.data.MockCommunityRepository
import cn.edu.app.douyu.core.data.MockPatternRepository
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MockRepositoryTest {
    @Test
    fun mockRepositoriesExposeCoreMvpData() {
        assertFalse(MockCommunityRepository().feed().items.isEmpty())
        assertFalse(MockPatternRepository().history().items.isEmpty())
        assertFalse(MockCommerceRepository().products().items.isEmpty())
    }

    @Test
    fun communityFeedContainsReviewingContent() {
        assertTrue(MockCommunityRepository().feed().items.any { it.status.name == "REVIEWING" })
    }
}
