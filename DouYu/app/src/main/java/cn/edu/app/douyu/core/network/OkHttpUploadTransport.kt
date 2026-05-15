package cn.edu.app.douyu.core.network

import cn.edu.app.douyu.core.model.UploadPresign
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import okio.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class OkHttpUploadTransport(
    client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
) : UploadTransport {

    private val httpClient = client

    override suspend fun upload(
        presign: UploadPresign,
        bytes: ByteArray,
        onProgress: ((Float) -> Unit)?
    ) {
        val body = ProgressRequestBody(bytes, presign.headers["Content-Type"], onProgress)
        val builder = Request.Builder()
            .url(presign.uploadUrl)
            .put(body)
        presign.headers.forEach { (key, value) ->
            if (key != "Content-Type") builder.header(key, value)
        }
        val request = builder.build()

        withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { cont ->
                val call = httpClient.newCall(request)
                cont.invokeOnCancellation { call.cancel() }
                call.enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        if (cont.isActive) cont.resumeWithException(e)
                    }

                    override fun onResponse(call: Call, response: okhttp3.Response) {
                        response.use {
                            if (cont.isActive) {
                                if (response.isSuccessful) {
                                    cont.resume(Unit)
                                } else {
                                    cont.resumeWithException(
                                        IOException("Upload failed: ${response.code} ${response.message}")
                                    )
                                }
                            }
                        }
                    }
                })
            }
        }
    }

    private class ProgressRequestBody(
        private val bytes: ByteArray,
        private val contentType: String?,
        private val onProgress: ((Float) -> Unit)?
    ) : RequestBody() {
        override fun contentType() = contentType?.toMediaType()
        override fun contentLength() = bytes.size.toLong()

        override fun writeTo(sink: BufferedSink) {
            val total = bytes.size
            var uploaded = 0
            val chunkSize = 8 * 1024
            while (uploaded < total) {
                val end = minOf(uploaded + chunkSize, total)
                sink.write(bytes, uploaded, end - uploaded)
                uploaded = end
                onProgress?.invoke(uploaded.toFloat() / total)
            }
        }
    }
}
