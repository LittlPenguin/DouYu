package cn.edu.app.douyu.core

import cn.edu.app.douyu.core.data.MockCommerceRepository
import cn.edu.app.douyu.core.data.MockCommunityRepository
import cn.edu.app.douyu.core.data.MockMessageRepository
import cn.edu.app.douyu.core.data.MockPatternRepository
import cn.edu.app.douyu.core.data.MockProfileRepository
import cn.edu.app.douyu.core.model.PatternJobStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelContractTest {
    @Test
    fun mockCommunityUsesBackendSemanticIdsAndCounters() {
        val post = MockCommunityRepository().feed().items.first()

        assertTrue(post.postId.startsWith("post_"))
        assertTrue(post.authorId.startsWith("user_"))
        assertEquals(post.authorId, post.author.userId)
        assertTrue(post.likeCount >= 0)
        assertTrue(post.favoriteCount >= 0)
        assertTrue(post.commentCount >= 0)
    }

    @Test
    fun mockPatternJobUsesConfirmedFileIdAndBackendStatuses() {
        val job = MockPatternRepository().featuredJob()

        assertTrue(job.jobId.startsWith("job_"))
        assertTrue(job.inputFileId.startsWith("file_"))
        assertNotEquals(PatternJobStatus.SUCCEEDED, job.status)
        assertTrue(PatternJobStatus.entries.map { it.name }.containsAll(listOf("PENDING", "PROCESSING", "SUCCEEDED", "FAILED", "REJECTED", "CANCELED")))
    }

    @Test
    fun mockCommerceUsesProductSkuOrderAndPaymentContractFields() {
        val product = MockCommerceRepository().products().items.first()
        val order = MockCommerceRepository().order()

        assertTrue(product.productId.startsWith("product_"))
        assertTrue(product.skus.isNotEmpty())
        assertTrue(product.skus.first().skuId.startsWith("sku_"))
        assertTrue(product.priceCent > 0)
        assertTrue(product.availableStock > 0)
        assertTrue(order.orderId.startsWith("order_"))
        assertTrue(order.payableAmountCent > 0)
        assertTrue(order.items.isNotEmpty())
    }

    @Test
    fun messagesAndBadgesUsePagedContractData() {
        val messages = MockMessageRepository()
        val profile = MockProfileRepository()

        assertTrue(messages.notifications().items.isNotEmpty())
        assertTrue(messages.conversations().items.isNotEmpty())
        assertTrue(profile.badges().isNotEmpty())
        assertTrue(profile.checkinStatus().userId.startsWith("user_"))
    }
}
