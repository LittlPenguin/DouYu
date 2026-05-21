package cn.edu.app.douyu.core

import cn.edu.app.douyu.core.data.ApiException
import cn.edu.app.douyu.core.data.MockData
import cn.edu.app.douyu.core.data.RealCommunityRepository
import cn.edu.app.douyu.core.model.Comment
import cn.edu.app.douyu.core.model.ContentStatus
import cn.edu.app.douyu.core.model.CreateCommentRequest
import cn.edu.app.douyu.core.model.CreatePostRequest
import cn.edu.app.douyu.core.model.Post
import cn.edu.app.douyu.core.model.PostInteractionResult
import cn.edu.app.douyu.core.network.ApiResponse
import cn.edu.app.douyu.core.network.CommunityApi
import cn.edu.app.douyu.core.network.PageResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CommunityRepositoryContractTest {
    @Test
    fun repositoryExposesCommunityWriteContract() {
        val repository = RealCommunityRepository(FakeCommunityApi())

        val created = repository.createPost(
            CreatePostRequest(
                title = "contract post",
                content = "community content",
                mediaFileIds = emptyList()
            )
        )
        assertEquals(ContentStatus.REVIEWING, created.status)

        assertEquals(true, repository.likePost(created.postId).liked)
        assertEquals(false, repository.unlikePost(created.postId).liked)
        assertEquals(true, repository.favoritePost(created.postId).favorited)
        assertEquals(false, repository.unfavoritePost(created.postId).favorited)

        val comment = repository.createComment(created.postId, CreateCommentRequest("contract comment"))
        assertEquals(ContentStatus.REVIEWING, comment.status)
    }

    @Test
    fun repositoryPreservesBackendErrorCodeAndTraceId() {
        val repository = RealCommunityRepository(FakeCommunityApi(failCreate = true))

        val error = assertThrows(ApiException::class.java) {
            repository.createPost(CreatePostRequest("bad", "bad", emptyList()))
        }

        assertEquals("INVALID_ARGUMENT", error.code)
        assertEquals("trace_contract_error", error.traceId)
    }

    private class FakeCommunityApi(
        private val failCreate: Boolean = false
    ) : CommunityApi {
        private val post = MockData.posts.first().copy(
            postId = "post_contract",
            status = ContentStatus.REVIEWING,
            likeCount = 0,
            favoriteCount = 0,
            commentCount = 0
        )

        override suspend fun feed(page: Int, size: Int): ApiResponse<PageResponse<Post>> =
            ApiResponse("OK", "success", PageResponse(listOf(post), page, size, 1, false), "trace_feed")

        override suspend fun following(page: Int, size: Int): ApiResponse<PageResponse<Post>> =
            ApiResponse("OK", "success", PageResponse(emptyList(), page, size, 0, false), "trace_following")

        override suspend fun createPost(request: CreatePostRequest): ApiResponse<Post> =
            if (failCreate) {
                ApiResponse("INVALID_ARGUMENT", "bad request", null, "trace_contract_error")
            } else {
                ApiResponse("OK", "success", post.copy(title = request.title, content = request.content), "trace_create")
            }

        override suspend fun post(postId: String): ApiResponse<Post> =
            ApiResponse("OK", "success", post.copy(postId = postId), "trace_post")

        override suspend fun likePost(postId: String): ApiResponse<PostInteractionResult> =
            ApiResponse("OK", "success", PostInteractionResult(liked = true), "trace_like")

        override suspend fun unlikePost(postId: String): ApiResponse<PostInteractionResult> =
            ApiResponse("OK", "success", PostInteractionResult(liked = false), "trace_unlike")

        override suspend fun favoritePost(postId: String): ApiResponse<PostInteractionResult> =
            ApiResponse("OK", "success", PostInteractionResult(favorited = true), "trace_favorite")

        override suspend fun unfavoritePost(postId: String): ApiResponse<PostInteractionResult> =
            ApiResponse("OK", "success", PostInteractionResult(favorited = false), "trace_unfavorite")

        override suspend fun comments(postId: String, page: Int, size: Int): ApiResponse<PageResponse<Comment>> =
            ApiResponse("OK", "success", PageResponse(emptyList(), page, size, 0, false), "trace_comments")

        override suspend fun createComment(
            postId: String,
            request: CreateCommentRequest
        ): ApiResponse<Comment> =
            ApiResponse(
                "OK",
                "success",
                Comment(
                    commentId = "comment_contract",
                    postId = postId,
                    authorId = MockData.user.userId,
                    author = MockData.user,
                    content = request.content,
                    status = ContentStatus.REVIEWING
                ),
                "trace_comment"
            )
    }
}
