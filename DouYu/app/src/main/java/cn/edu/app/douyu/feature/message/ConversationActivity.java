package cn.edu.app.douyu.feature.message;

import cn.edu.app.douyu.R;
import cn.edu.app.douyu.core.IntentExtras;
import cn.edu.app.douyu.model.ChatMessage;
import cn.edu.app.douyu.model.Conversation;
import cn.edu.app.douyu.model.ConversationDetail;
import cn.edu.app.douyu.ui.XmlPageActivity;

public class ConversationActivity extends XmlPageActivity {
    @Override
    protected int layoutRes() {
        return R.layout.activity_conversation;
    }

    @Override
    protected String title() {
        return "私信详情";
    }

    @Override
    protected void bindViews() {
        String conversationId = extra(IntentExtras.CONVERSATION_ID);
        if (conversationId.isEmpty()) {
            setText(R.id.conversation_detail_id, "缺少 conversationId，无法请求会话详情。");
        } else {
            setText(R.id.conversation_detail_id, "正在加载会话详情：" + conversationId);
            loadDetail(
                    repository -> repository.conversation(conversationId),
                    this::renderConversation,
                    (state, message) -> setText(R.id.conversation_detail_id, message)
            );
        }
    }

    private void renderConversation(ConversationDetail detail) {
        if (detail == null || detail.conversation == null) {
            setText(R.id.conversation_detail_id, "会话不存在或无权访问。");
            return;
        }
        Conversation conversation = detail.conversation;
        StringBuilder builder = new StringBuilder()
                .append("会话 ID：").append(valueOrFallback(conversation.conversationId, "未知")).append('\n')
                .append("对方：").append(peerName(conversation)).append('\n')
                .append("互相关注：").append(Boolean.TRUE.equals(conversation.mutualFollow) ? "是" : "否").append('\n')
                .append("可发送：").append(Boolean.TRUE.equals(conversation.canSend) ? "是" : "否").append('\n')
                .append("剩余额度：").append(conversation.remainingNonMutualMessages == null ? "未知" : conversation.remainingNonMutualMessages).append('\n')
                .append("最近消息：").append(valueOrFallback(conversation.lastMessage, "暂无")).append('\n')
                .append("消息：");
        if (detail.messages == null || detail.messages.isEmpty()) {
            builder.append("暂无消息");
        } else {
            for (ChatMessage message : detail.messages) {
                builder.append('\n')
                        .append(Boolean.TRUE.equals(message.mine) ? "我" : valueOrFallback(message.senderName, "对方"))
                        .append("：")
                        .append(valueOrFallback(message.content, ""));
            }
        }
        setText(R.id.conversation_detail_id, builder.toString());
    }

    private static String peerName(Conversation conversation) {
        if (conversation.peer != null && conversation.peer.nickname != null && !conversation.peer.nickname.isEmpty()) {
            return conversation.peer.nickname;
        }
        return valueOrFallback(conversation.peerName, "未知用户");
    }
}
