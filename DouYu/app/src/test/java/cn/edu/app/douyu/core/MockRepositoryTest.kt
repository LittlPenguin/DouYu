package cn.edu.app.douyu.core

import cn.edu.app.douyu.core.data.MockCommerceRepository
import cn.edu.app.douyu.core.data.MockCommunityRepository
import cn.edu.app.douyu.core.data.MockPatternRepository
import cn.edu.app.douyu.core.model.ContentStatus
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
    fun communityFeedOnlyExposesVisibleContent() {
        assertTrue(MockCommunityRepository().feed().items.all { it.status == ContentStatus.VISIBLE })
    }
}
