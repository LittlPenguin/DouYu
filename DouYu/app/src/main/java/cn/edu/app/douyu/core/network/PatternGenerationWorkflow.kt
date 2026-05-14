package cn.edu.app.douyu.core.network

import cn.edu.app.douyu.core.model.CreatePatternJobRequest
import cn.edu.app.douyu.core.model.PatternJob
import cn.edu.app.douyu.core.model.UploadConfirmRequest
import cn.edu.app.douyu.core.model.UploadPresign
import cn.edu.app.douyu.core.model.UploadPresignRequest
import cn.edu.app.douyu.core.model.UploadUsage

fun interface UploadTransport {
    suspend fun upload(presign: UploadPresign, bytes: ByteArray)
}

class PatternGenerationWorkflow(
    private val uploadApi: UploadApi,
    private val patternApi: PatternApi,
    private val uploadTransport: UploadTransport
) {
    suspend fun createJobFromLocalImage(
        localName: String,
        mimeType: String,
        bytes: ByteArray,
        imageWidth: Int?,
        imageHeight: Int?,
        params: CreatePatternJobRequest
    ): PatternJob {
        val presign = requireSuccess(
            uploadApi.presign(
                UploadPresignRequest(
                    usage = UploadUsage.AI_INPUT,
                    fileName = localName,
                    mimeType = mimeType,
                    sizeBytes = bytes.size.toLong()
                )
            )
        )

        uploadTransport.upload(presign, bytes)

        val file = requireSuccess(
            uploadApi.confirm(
                UploadConfirmRequest(
                    fileKey = presign.fileKey,
                    usage = UploadUsage.AI_INPUT,
                    mimeType = mimeType,
                    sizeBytes = bytes.size.toLong(),
                    width = imageWidth,
                    height = imageHeight
                )
            )
        )

        return requireSuccess(
            patternApi.createJob(params.copy(inputFileId = file.fileId))
        )
    }

    private fun <T> requireSuccess(response: ApiResponse<T>): T {
        val data = response.data
        if (!response.isOk || data == null) {
            throw IllegalStateException("API error ${response.code}: ${response.message}")
        }
        return data
    }
}
