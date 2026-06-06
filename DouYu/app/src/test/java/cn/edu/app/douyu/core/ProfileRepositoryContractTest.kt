package cn.edu.app.douyu.core

import cn.edu.app.douyu.core.data.MockData
import cn.edu.app.douyu.core.data.RealProfileRepository
import cn.edu.app.douyu.core.model.BadgeListWrapper
import cn.edu.app.douyu.core.model.CheckinStatus
import cn.edu.app.douyu.core.model.PatternAsset
import cn.edu.app.douyu.core.model.PatternJob
import cn.edu.app.douyu.core.model.Post
import cn.edu.app.douyu.core.model.RewardSummary
import cn.edu.app.douyu.core.model.UpdateProfileRequest
import cn.edu.app.douyu.core.model.UserProfile
import cn.edu.app.douyu.core.network.ApiResponse
import cn.edu.app.douyu.core.network.PageResponse
import cn.edu.app.douyu.core.network.PatternApi
import cn.edu.app.douyu.core.network.RewardApi
import cn.edu.app.douyu.core.network.UserApi
import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileRepositoryContractTest {
    @Test
    fun updateProfileCallsExistingPatchMeContract() {
        val userApi = FakeProfileUserApi()
        val repository = RealProfileRepository(userApi, FakeRewardApi(), FakeProfilePatternApi())

        val updated = repository.updateProfile(
            UpdateProfileRequest(
                nickname = "豆屿岛民",
                avatarFileId = "file_avatar_1",
                bio = "喜欢拼豆"
            )
        )

        assertEquals("豆屿岛民", updated.nickname)
        assertEquals("喜欢拼豆", updated.bio)
        assertEquals(UpdateProfileRequest("豆屿岛民", "file_avatar_1", "喜欢拼豆"), userApi.lastUpdateRequest)
    }

    private class FakeProfileUserApi : UserApi {
        var lastUpdateRequest: UpdateProfileRequest? = null

        override suspend fun me(): ApiResponse<UserProfile> =
            ApiResponse("OK", "success", MockData.user, "trace_me")

        override suspend fun searchUsers(keyword: String, page: Int, size: Int): ApiResponse<PageResponse<UserProfile>> =
            ApiResponse("OK", "success", PageResponse(emptyList(), page, size, 0, false), "trace_search")

        override suspend fun updateMe(request: UpdateProfileRequest): ApiResponse<UserProfile> {
            lastUpdateRequest = request
            return ApiResponse("OK", "success", MockData.user.copy(nickname = request.nickname, bio = request.bio.orEmpty()), "trace_update")
        }

        override suspend fun likedPosts(page: Int, size: Int): ApiResponse<PageResponse<Post>> =
            ApiResponse("OK", "success", PageResponse(emptyList<Post>(), page, size, 0, false), "trace_liked")

        override suspend fun commentedPosts(page: Int, size: Int): ApiResponse<PageResponse<Post>> =
            ApiResponse("OK", "success", PageResponse(emptyList<Post>(), page, size, 0, false), "trace_commented")

        override suspend fun favoritePosts(page: Int, size: Int): ApiResponse<PageResponse<Post>> =
            ApiResponse("OK", "success", PageResponse(emptyList<Post>(), page, size, 0, false), "trace_favorite")

        override suspend fun followedPosts(page: Int, size: Int): ApiResponse<PageResponse<Post>> =
            ApiResponse("OK", "success", PageResponse(emptyList<Post>(), page, size, 0, false), "trace_followed")

        override suspend fun follow(userId: String) =
            ApiResponse("OK", "success", cn.edu.app.douyu.core.model.FollowResult(followed = true, followedByMe = true), "trace_follow")

        override suspend fun unfollow(userId: String) =
            ApiResponse("OK", "success", cn.edu.app.douyu.core.model.FollowResult(followed = false, followedByMe = false), "trace_unfollow")
    }

    private class FakeRewardApi : RewardApi {
        override suspend fun checkin(): ApiResponse<CheckinStatus> =
            ApiResponse("OK", "success", CheckinStatus(), "trace_checkin")

        override suspend fun checkinStatus(): ApiResponse<CheckinStatus> =
            ApiResponse("OK", "success", CheckinStatus(), "trace_checkin_status")

        override suspend fun me(): ApiResponse<RewardSummary> =
            ApiResponse("OK", "success", RewardSummary(), "trace_reward")

        override suspend fun badges(): ApiResponse<BadgeListWrapper> =
            ApiResponse("OK", "success", BadgeListWrapper(), "trace_badges")
    }

    private class FakeProfilePatternApi : PatternApi {
        override suspend fun createJob(request: cn.edu.app.douyu.core.model.CreatePatternJobRequest): ApiResponse<PatternJob> =
            ApiResponse("OK", "success", MockData.jobs.first(), "trace_create_job")

        override suspend fun job(jobId: String): ApiResponse<PatternJob> =
            ApiResponse("OK", "success", MockData.jobs.first().copy(jobId = jobId), "trace_job")

        override suspend fun jobs(page: Int, size: Int): ApiResponse<PageResponse<PatternJob>> =
            ApiResponse("OK", "success", PageResponse(emptyList(), page, size, 0, false), "trace_jobs")

        override suspend fun cancelJob(jobId: String): ApiResponse<PatternJob> =
            ApiResponse("OK", "success", MockData.jobs.first().copy(jobId = jobId), "trace_cancel")

        override suspend fun favoritePattern(patternId: String): ApiResponse<Unit> =
            ApiResponse("OK", "success", Unit, "trace_favorite_pattern")

        override suspend fun favorites(page: Int, size: Int): ApiResponse<PageResponse<PatternAsset>> =
            ApiResponse("OK", "success", PageResponse(emptyList(), page, size, 0, false), "trace_favorites")

        override suspend fun pattern(patternId: String): ApiResponse<PatternAsset> =
            ApiResponse("OK", "success", MockData.patterns.first().copy(patternId = patternId), "trace_pattern")
    }
}
