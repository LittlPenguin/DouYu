package cn.edu.app.douyu.core

import cn.edu.app.douyu.feature.message.conversationComposerCanSend
import cn.edu.app.douyu.feature.message.messageHomeTabLabels
import cn.edu.app.douyu.feature.message.nonMutualLimitExceededMessage
import cn.edu.app.douyu.feature.message.shouldBlockConversationAfterSendError
import cn.edu.app.douyu.core.data.ApiException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageUiRuleTest {
    @Test
    fun messageHomeTabsFollowOpenDesignOrder() {
        assertEquals(listOf("私信", "通知"), messageHomeTabLabels())
    }

    @Test
    fun nonMutualLimitErrorBlocksFurtherSending() {
        assertTrue(shouldBlockConversationAfterSendError("NON_MUTUAL_MESSAGE_LIMIT_EXCEEDED"))
        assertTrue(shouldBlockConversationAfterSendError("400 NON_MUTUAL_MESSAGE_LIMIT_EXCEEDED traceId=x"))
        assertFalse(shouldBlockConversationAfterSendError("NETWORK_TIMEOUT"))
    }

    @Test
    fun nonMutualLimitErrorBlocksWhenCodeIsOnlyInApiExceptionCode() {
        val error = ApiException(
            code = "NON_MUTUAL_MESSAGE_LIMIT_EXCEEDED",
            message = "未互关私信已达上限",
            traceId = "trace-message-limit"
        )

        assertTrue(shouldBlockConversationAfterSendError(error))
        assertFalse(shouldBlockConversationAfterSendError(ApiException("NETWORK_TIMEOUT", "网络超时", null)))
    }

    @Test
    fun composerDisablesWhenBackendOrLocalLimitBlocksSending() {
        assertTrue(conversationComposerCanSend(serverCanSend = true, blockedByLimit = false))
        assertFalse(conversationComposerCanSend(serverCanSend = false, blockedByLimit = false))
        assertFalse(conversationComposerCanSend(serverCanSend = true, blockedByLimit = true))
        assertFalse(conversationComposerCanSend(serverCanSend = null, blockedByLimit = true))
    }

    @Test
    fun nonMutualLimitErrorUsesExplicitUserMessage() {
        assertEquals("未互关私信已达 3 条上限，互相关注后可继续聊天。", nonMutualLimitExceededMessage())
    }
}
