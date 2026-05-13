package cn.edu.app.douyu.core

import cn.edu.app.douyu.core.network.ApiResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiResponseTest {
    @Test
    fun okResponseCarriesTraceAndData() {
        val response = ApiResponse(
            code = "OK",
            message = "success",
            data = "pong",
            traceId = "trace_001"
        )

        assertTrue(response.isOk)
        assertEquals("pong", response.data)
        assertEquals("trace_001", response.traceId)
    }
}
