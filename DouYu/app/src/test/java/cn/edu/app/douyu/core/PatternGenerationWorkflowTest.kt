package cn.edu.app.douyu.core

import cn.edu.app.douyu.core.data.MockData
import cn.edu.app.douyu.core.model.*
import cn.edu.app.douyu.core.network.ApiResponse
import cn.edu.app.douyu.core.network.PatternApi
import cn.edu.app.douyu.core.network.PatternGenerationWorkflow
import cn.edu.app.douyu.core.network.UploadApi
import cn.edu.app.douyu.core.network.UploadTransport
import cn.edu.app.douyu.core.network.PageResponse
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class PatternGenerationWorkflowTest {
    @Test
    fun createsPatternJobOnlyAfterPresignUploadAndConfirm() = runBlocking {
        val calls = mutableListOf<String>()
        val uploadApi = FakeUploadApi(calls)
        val patternApi = FakePatternApi(calls)
        val transport = UploadTransport { presign, _ ->
            calls += "put:${presign.fileKey}"
        }

        val job = PatternGenerationWorkflow(uploadApi, patternApi, transport).createJobFromLocalImage(
            localName = "cat.jpg",
            mimeType = "image/jpeg",
            bytes = ByteArray(8),
            imageWidth = 320,
            imageHeight = 320,
            params = CreatePatternJobRequest(
                inputFileId = "will_be_replaced",
                beadSize = BeadSize.MM_2_6,
                targetSize = "SMALL_CHARM",
                difficulty = PatternDifficulty.BEGINNER,
                paletteId = "palette_doyu_48",
                style = PatternStyle.CUTE
            )
        )

        assertEquals(listOf("presign", "put:ai-input/cat.jpg", "confirm", "createJob:file_confirmed_001"), calls)
        assertEquals("job_workflow_001", job.jobId)
    }

    private class FakeUploadApi(private val calls: MutableList<String>) : UploadApi {
        override suspend fun presign(request: UploadPresignRequest): ApiResponse<UploadPresign> {
            calls += "presign"
            return ApiResponse(
                "OK",
                "success",
                UploadPresign("https://upload.example.test/stub", "ai-input/cat.jpg", emptyMap(), 300),
                "trace_presign"
            )
        }

        override suspend fun confirm(request: UploadConfirmRequest): ApiResponse<FileAsset> {
            calls += "confirm"
            return ApiResponse(
                "OK",
                "success",
                MockData.confirmedAiInput.copy(fileId = "file_confirmed_001", storageKey = request.fileKey),
                "trace_confirm"
            )
        }
    }

    private class FakePatternApi(private val calls: MutableList<String>) : PatternApi {
        override suspend fun createJob(request: CreatePatternJobRequest): ApiResponse<PatternJob> {
            calls += "createJob:${request.inputFileId}"
            return ApiResponse("OK", "success", MockData.jobs.first().copy(jobId = "job_workflow_001", inputFileId = request.inputFileId), "trace_job")
        }

        override suspend fun job(jobId: String): ApiResponse<PatternJob> =
            ApiResponse("OK", "success", MockData.jobs.first().copy(jobId = jobId), "trace_job")

        override suspend fun jobs(page: Int, size: Int): ApiResponse<PageResponse<PatternJob>> =
            ApiResponse("OK", "success", PageResponse(MockData.jobs, page, size, MockData.jobs.size, false), "trace_jobs")

        override suspend fun cancelJob(jobId: String): ApiResponse<PatternJob> =
            ApiResponse("OK", "success", MockData.jobs.first().copy(jobId = jobId, status = PatternJobStatus.CANCELED), "trace_cancel")

        override suspend fun favoritePattern(patternId: String): ApiResponse<Unit> =
            ApiResponse("OK", "success", Unit, "trace_favorite")

        override suspend fun pattern(patternId: String): ApiResponse<PatternAsset> =
            ApiResponse("OK", "success", MockData.patterns.first().copy(patternId = patternId), "trace_pattern")
    }
}
