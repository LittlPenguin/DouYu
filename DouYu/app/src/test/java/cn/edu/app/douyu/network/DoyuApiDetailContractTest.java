package cn.edu.app.douyu.network;

import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import cn.edu.app.douyu.model.ConversationDetail;
import cn.edu.app.douyu.model.PatternJob;
import retrofit2.Call;
import retrofit2.http.GET;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DoyuApiDetailContractTest {
    @Test
    public void conversationDetailUsesBackendDetailEnvelope() throws Exception {
        Method method = DoyuApi.class.getMethod("conversation", String.class);

        assertEquals("/api/v1/messages/conversations/{conversationId}", method.getAnnotation(GET.class).value());
        assertCallDataType(method, ConversationDetail.class);
    }

    @Test
    public void patternJobDetailUsesJobEndpoint() throws Exception {
        Method method = DoyuApi.class.getMethod("patternJob", String.class);

        assertEquals("/api/v1/patterns/jobs/{jobId}", method.getAnnotation(GET.class).value());
        assertCallDataType(method, PatternJob.class);
    }

    private static void assertCallDataType(Method method, Class<?> expectedDataType) {
        Type returnType = method.getGenericReturnType();
        assertTrue(returnType instanceof ParameterizedType);
        ParameterizedType callType = (ParameterizedType) returnType;
        assertEquals(Call.class, callType.getRawType());
        ParameterizedType apiResponseType = (ParameterizedType) callType.getActualTypeArguments()[0];
        assertEquals(ApiResponse.class, apiResponseType.getRawType());
        assertEquals(expectedDataType, apiResponseType.getActualTypeArguments()[0]);
    }
}
