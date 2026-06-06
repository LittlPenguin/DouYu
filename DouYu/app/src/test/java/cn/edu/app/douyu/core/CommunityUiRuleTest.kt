package cn.edu.app.douyu.core

import cn.edu.app.douyu.core.model.Comment
import cn.edu.app.douyu.core.model.ContentStatus
import cn.edu.app.douyu.core.model.UserProfile
import cn.edu.app.douyu.feature.community.SelectedMention
import cn.edu.app.douyu.feature.community.SelectedTopic
import cn.edu.app.douyu.feature.community.commentImageFailedState
import cn.edu.app.douyu.feature.community.commentImageLimitLabel
import cn.edu.app.douyu.feature.community.commentImageRetryActionLabel
import cn.edu.app.douyu.feature.community.commentFailedImagesRetryActionLabel
import cn.edu.app.douyu.feature.community.commentImageUploadedState
import cn.edu.app.douyu.feature.community.commentCountAfterReviewingSubmit
import cn.edu.app.douyu.feature.community.postCarouselItems
import cn.edu.app.douyu.feature.community.hasMeaningfulCommentText
import cn.edu.app.douyu.feature.community.publicCommentEmptyStateCopy
import cn.edu.app.douyu.feature.community.publicCommentListItems
import cn.edu.app.douyu.feature.community.shouldOfferCommentImageRetry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CommunityUiRuleTest {
    @Test
    fun commentTextRequiresMoreThanMentionAndTopicTokens() {
        val mentions = listOf(SelectedMention("user_1", "小岛手作"))
        val topics = listOf(SelectedTopic("topic_1", "新手教程"))

        assertFalse(hasMeaningfulCommentText("@小岛手作 #新手教程", mentions, topics))
        assertFalse(hasMeaningfulCommentText(" @小岛手作  ", mentions, emptyList()))
        assertFalse(hasMeaningfulCommentText("#新手教程", emptyList(), topics))
        assertTrue(hasMeaningfulCommentText("@小岛手作 这个配色很适合新手 #新手教程", mentions, topics))
    }

    @Test
    fun postCarouselItemsPreferCoverAndDeduplicateMediaIds() {
        assertEquals(
            listOf("https://cdn.example/post.jpg", "file_1", "file_2"),
            postCarouselItems(
                coverImageUrl = "https://cdn.example/post.jpg",
                mediaFileIds = listOf("file_1", "file_2", "file_1")
            )
        )
        assertEquals(listOf("file_1"), postCarouselItems(coverImageUrl = "", mediaFileIds = listOf("file_1")))
        assertEquals(listOf("fallback"), postCarouselItems(coverImageUrl = null, mediaFileIds = emptyList()))
    }

    @Test
    fun failedCommentImageOnlyOffersRetryWhenNotPosting() {
        assertTrue(shouldOfferCommentImageRetry(failed = true, posting = false))
        assertFalse(shouldOfferCommentImageRetry(failed = true, posting = true))
        assertFalse(shouldOfferCommentImageRetry(failed = false, posting = false))
    }

    @Test
    fun uploadedCommentImageStateKeepsFileIdBeforeWholeBatchFinishes() {
        val uploaded = commentImageUploadedState("file_comment_001")

        assertEquals("file_comment_001", uploaded.fileId)
        assertEquals(1f, uploaded.progress, 0.001f)
        assertFalse(uploaded.failed)
    }

    @Test
    fun failedCommentImageStateDoesNotPretendUploadSucceeded() {
        val failed = commentImageFailedState()

        assertEquals(null, failed.fileId)
        assertEquals(0f, failed.progress, 0.001f)
        assertTrue(failed.failed)
    }

    @Test
    fun reviewingCommentSubmitDoesNotIncreasePublicCommentCount() {
        assertEquals(9, commentCountAfterReviewingSubmit(9))
    }

    @Test
    fun commentImageLimitLabelIsVisibleAtNineImages() {
        assertEquals(null, commentImageLimitLabel(8))
        assertEquals("9/9 已达上限", commentImageLimitLabel(9))
    }

    @Test
    fun failedCommentImageRetryActionHasStableLabel() {
        assertEquals("重试上传", commentImageRetryActionLabel(failed = true, posting = false))
        assertEquals(null, commentImageRetryActionLabel(failed = true, posting = true))
        assertEquals(null, commentImageRetryActionLabel(failed = false, posting = false))
    }

    @Test
    fun failedCommentImagesExposeStableRetryButtonLabel() {
        assertEquals("重试上传失败图片", commentFailedImagesRetryActionLabel(failedImageCount = 1, posting = false))
        assertEquals("重试上传失败图片", commentFailedImagesRetryActionLabel(failedImageCount = 9, posting = false))
        assertEquals(null, commentFailedImagesRetryActionLabel(failedImageCount = 1, posting = true))
        assertEquals(null, commentFailedImagesRetryActionLabel(failedImageCount = 0, posting = false))
    }

    @Test
    fun publicCommentListOnlyShowsVisibleComments() {
        val author = UserProfile(userId = "user_1", nickname = "tester")
        val comments = ContentStatus.entries.mapIndexed { index, status ->
            Comment(
                commentId = "comment_$index",
                postId = "post_1",
                authorId = author.userId,
                author = author,
                content = status.name,
                status = status
            )
        }

        assertEquals(
            listOf("VISIBLE"),
            publicCommentListItems(comments).map { it.content }
        )
    }

    @Test
    fun publicCommentEmptyStateExplainsReviewingCommentsWithoutRetry() {
        val copy = publicCommentEmptyStateCopy(rawCommentCount = 3, fallbackCount = 3)

        assertEquals("暂无公开评论", copy.title)
        assertEquals("评论提交后会等待审核，通过后才会公开展示。", copy.message)
        assertFalse(copy.showRetry)
    }
}
