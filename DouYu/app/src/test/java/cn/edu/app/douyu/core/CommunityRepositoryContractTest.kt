package cn.edu.app.douyu.core

import cn.edu.app.douyu.core.data.ApiException
import cn.edu.app.douyu.core.data.MockData
import cn.edu.app.douyu.core.data.RealCommunityRepository
import cn.edu.app.douyu.core.model.Comment
import cn.edu.app.douyu.core.model.ContentStatus
import cn.edu.app.douyu.core.model.CreateCommentRequest
import cn.edu.app.douyu.core.model.CreatePostRequest
import cn.edu.app.douyu.core.model.CommentMediaAsset
import cn.edu.app.douyu.core.model.FollowResult
import cn.edu.app.douyu.core.model.Post
import cn.edu.app.douyu.core.model.PostInteractionResult
import cn.edu.app.douyu.core.model.AuditStatus
import cn.edu.app.douyu.core.model.UpdateProfileRequest
import cn.edu.app.douyu.core.model.UserProfile
import cn.edu.app.douyu.core.network.ApiResponse
import cn.edu.app.douyu.core.network.CommunityApi
import cn.edu.app.douyu.core.network.PageResponse
import cn.edu.app.douyu.core.network.UserApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CommunityRepositoryContractTest {
    @Test
    fun repositoryExposesCommunityWriteContract() {
        val repository = RealCommunityRepository(FakeCommunityApi(), FakeUserApi())

        val created = repository.createPost(
            CreatePostRequest(
                title = "contract post",
                content = "community content",
                mediaFileIds = emptyList()
            )
        )
        assertEquals(ContentStatus.REVIEWING, created.status)

        val liked = repository.likePost(created.postId)
        assertEquals(true, liked.liked)
        assertEquals(true, liked.likedByMe)
        assertEquals(41, liked.likeCount)

        val unliked = repository.unlikePost(created.postId)
        assertEquals(false, unliked.liked)
        assertEquals(false, unliked.likedByMe)
        assertEquals(40, unliked.likeCount)

        val favorited = repository.favoritePost(created.postId)
        assertEquals(true, favorited.favorited)
        assertEquals(true, favorited.favoritedByMe)
        assertEquals(8, favorited.favoriteCount)

        val unfavorited = repository.unfavoritePost(created.postId)
        assertEquals(false, unfavorited.favorited)
        assertEquals(false, unfavorited.favoritedByMe)
        assertEquals(7, unfavorited.favoriteCount)
        assertEquals(true, repository.followUser(MockData.user.userId).followedByMe)
        assertEquals(false, repository.unfollowUser(MockData.user.userId).followedByMe)

        val comment = repository.createComment(
            created.postId,
            CreateCommentRequest(
                content = "",
                mediaFileIds = listOf("file_comment_001"),
                mentionUserIds = listOf("user_mention"),
                topicIds = listOf("topic_beginner"),
                stickerIds = listOf("sticker_like")
            )
        )
        assertEquals(ContentStatus.REVIEWING, comment.status)
        assertEquals(listOf("file_comment_001"), comment.mediaFileIds)
        assertEquals("http://127.0.0.1/comment-image.png", comment.mediaAssets.first().publicUrl)
        assertEquals("user_mention", comment.mentions.first().userId)
        assertEquals("topic_beginner", comment.topics.first().topicId)
        assertEquals("sticker_like", comment.stickers.first().stickerId)
        assertEquals("topic_beginner", repository.topics("beginner").items.first().topicId)
        assertEquals("sticker_like", repository.stickerPacks().items.first().stickers.first().stickerId)
        assertEquals(created.postId, repository.likedPosts().items.first().postId)
        assertEquals(created.postId, repository.commentedPosts().items.first().postId)
        assertEquals(created.postId, repository.favoritePosts().items.first().postId)
        assertEquals(created.postId, repository.followedPosts().items.first().postId)
    }

    @Test
    fun repositoryPreservesBackendErrorCodeAndTraceId() {
        val repository = RealCommunityRepository(FakeCommunityApi(failCreate = true), FakeUserApi())

        val error = assertThrows(ApiException::class.java) {
            repository.createPost(CreatePostRequest("bad", "bad", emptyList()))
        }

        assertEquals("INVALID_ARGUMENT", error.code)
        assertEquals("trace_contract_error", error.traceId)
    }

    private class FakeUserApi : UserApi {
        override suspend fun me(): ApiResponse<UserProfile> =
            ApiResponse("OK", "success", MockData.user, "trace_me")

        override suspend fun searchUsers(keyword: String, page: Int, size: Int): ApiResponse<PageResponse<UserProfile>> =
            ApiResponse("OK", "success", PageResponse(listOf(MockData.user), page, size, 1, false), "trace_search_users")

        override suspend fun updateMe(request: UpdateProfileRequest): ApiResponse<UserProfile> =
            ApiResponse("OK", "success", MockData.user.copy(nickname = request.nickname, bio = request.bio.orEmpty()), "trace_update_me")

        override suspend fun likedPosts(page: Int, size: Int): ApiResponse<PageResponse<Post>> =
            ApiResponse("OK", "success", PageResponse(listOf(MockData.posts.first().copy(postId = "post_contract")), page, size, 1, false), "trace_liked_posts")

        override suspend fun commentedPosts(page: Int, size: Int): ApiResponse<PageResponse<Post>> =
            likedPosts(page, size)

        override suspend fun favoritePosts(page: Int, size: Int): ApiResponse<PageResponse<Post>> =
            likedPosts(page, size)

        override suspend fun followedPosts(page: Int, size: Int): ApiResponse<PageResponse<Post>> =
            likedPosts(page, size)

        override suspend fun follow(userId: String): ApiResponse<FollowResult> =
            ApiResponse("OK", "success", FollowResult(followed = true, followedByMe = true), "trace_follow")

        override suspend fun unfollow(userId: String): ApiResponse<FollowResult> =
            ApiResponse("OK", "success", FollowResult(followed = false, followedByMe = false), "trace_unfollow")
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
            ApiResponse("OK", "success", PostInteractionResult(liked = true, likedByMe = true, likeCount = 41, favoriteCount = 7, favoritedByMe = false), "trace_like")

        override suspend fun unlikePost(postId: String): ApiResponse<PostInteractionResult> =
            ApiResponse("OK", "success", PostInteractionResult(liked = false, likedByMe = false, likeCount = 40, favoriteCount = 7, favoritedByMe = false), "trace_unlike")

        override suspend fun favoritePost(postId: String): ApiResponse<PostInteractionResult> =
            ApiResponse("OK", "success", PostInteractionResult(favorited = true, favoritedByMe = true, favoriteCount = 8, likeCount = 40, likedByMe = false), "trace_favorite")

        override suspend fun unfavoritePost(postId: String): ApiResponse<PostInteractionResult> =
            ApiResponse("OK", "success", PostInteractionResult(favorited = false, favoritedByMe = false, favoriteCount = 7, likeCount = 40, likedByMe = false), "trace_unfavorite")

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
                    mediaFileIds = request.mediaFileIds,
                    mediaAssets = request.mediaFileIds.map {
                        CommentMediaAsset(
                            fileId = it,
                            publicUrl = "http://127.0.0.1/comment-image.png",
                            mimeType = "image/png",
                            width = 64,
                            height = 64,
                            auditStatus = AuditStatus.NEED_MANUAL_REVIEW
                        )
                    },
                    mentions = request.mentionUserIds.map { cn.edu.app.douyu.core.model.CommentMention(it, "mentioned") },
                    topics = request.topicIds.map { cn.edu.app.douyu.core.model.CommentTopic(it, "topic") },
                    stickers = request.stickerIds.map { cn.edu.app.douyu.core.model.Sticker(it, "pack_doyu_basic", "喜欢", emojiText = "喜欢") },
                    status = ContentStatus.REVIEWING
                ),
                "trace_comment"
            )

        override suspend fun topics(keyword: String, page: Int, size: Int): ApiResponse<PageResponse<cn.edu.app.douyu.core.model.Topic>> =
            ApiResponse("OK", "success", PageResponse(listOf(cn.edu.app.douyu.core.model.Topic("topic_beginner", "新手教程")), page, size, 1, false), "trace_topics")

        override suspend fun topicPosts(topicId: String, page: Int, size: Int): ApiResponse<PageResponse<Post>> =
            ApiResponse("OK", "success", PageResponse(listOf(post.copy(topicIds = listOf(topicId))), page, size, 1, false), "trace_topic_posts")

        override suspend fun stickerPacks(): ApiResponse<PageResponse<cn.edu.app.douyu.core.model.StickerPack>> =
            ApiResponse(
                "OK",
                "success",
                PageResponse(
                    listOf(cn.edu.app.douyu.core.model.StickerPack("pack_doyu_basic", "豆屿基础", listOf(cn.edu.app.douyu.core.model.Sticker("sticker_like", "pack_doyu_basic", "喜欢", emojiText = "喜欢")))),
                    1,
                    20,
                    1,
                    false
                ),
                "trace_stickers"
            )
    }
}
