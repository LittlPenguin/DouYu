package cn.edu.app.douyu.feature.message;

import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import cn.edu.app.douyu.R;
import cn.edu.app.douyu.core.IntentExtras;
import cn.edu.app.douyu.model.ChatMessage;
import cn.edu.app.douyu.model.Conversation;
import cn.edu.app.douyu.model.ConversationDetail;
import cn.edu.app.douyu.ui.XmlPageActivity;

/**
 * Private conversation detail with real send.
 * Send ability is decided by the backend conversation state (mutual follow / remaining quota):
 * mutual or remaining&gt;0 enables the input; once blocked the input is disabled and no fake
 * success is shown. Send failures keep the draft and offer retry/cancel.
 */
public class ConversationActivity extends XmlPageActivity {
    private final ChatAdapter chatAdapter = new ChatAdapter();

    private View content;
    private ProgressBar loading;
    private View errorBox;
    private TextView errorText;
    private TextView heroPill;
    private TextView heroTitle;
    private TextView heroDesc;
    private RecyclerView messages;
    private TextView emptyView;
    private TextView warning;
    private View retryRow;
    private EditText input;
    private TextView status;
    private MaterialButton send;
    private MaterialButton follow;

    private String conversationId;
    private boolean mutualFollow;
    private boolean canSend;
    private int remaining;
    private boolean sending;

    @Override
    protected int layoutRes() {
        return R.layout.activity_conversation;
    }

    @Override
    protected String title() {
        return valueOrFallback(extra(IntentExtras.PEER_NAME), "私信");
    }

    @Override
    protected void bindViews() {
        content = findViewById(R.id.conversation_content);
        loading = findViewById(R.id.conversation_loading);
        errorBox = findViewById(R.id.conversation_error);
        errorText = findViewById(R.id.conversation_error_text);
        heroPill = findViewById(R.id.conversation_hero_pill);
        heroTitle = findViewById(R.id.conversation_hero_title);
        heroDesc = findViewById(R.id.conversation_hero_desc);
        messages = findViewById(R.id.conversation_messages);
        emptyView = findViewById(R.id.conversation_empty);
        warning = findViewById(R.id.conversation_warning);
        retryRow = findViewById(R.id.conversation_retry_row);
        input = findViewById(R.id.conversation_input);
        status = findViewById(R.id.conversation_status);
        send = findViewById(R.id.conversation_send);
        follow = findViewById(R.id.conversation_follow);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messages.setLayoutManager(layoutManager);
        messages.setAdapter(chatAdapter);

        send.setOnClickListener(v -> doSend());
        findViewById(R.id.conversation_retry_load).setOnClickListener(v -> loadConversation());
        findViewById(R.id.conversation_send_retry).setOnClickListener(v -> doSend());
        findViewById(R.id.conversation_send_cancel).setOnClickListener(v -> clearFailure());
        follow.setOnClickListener(v ->
                Toast.makeText(this, "请到对方主页关注 ta，互相关注后可继续聊天。", Toast.LENGTH_SHORT).show());

        conversationId = extra(IntentExtras.CONVERSATION_ID);
        if (conversationId.isEmpty()) {
            renderLoadError("缺少 conversationId，无法打开会话。请从消息列表进入。");
            return;
        }
        loadConversation();
    }

    private void loadConversation() {
        showLoading();
        loadDetail(
                repository -> repository.conversation(conversationId),
                this::onConversationLoaded,
                (state, message) -> renderLoadError(message)
        );
    }

    private void onConversationLoaded(ConversationDetail detail) {
        if (detail == null || detail.conversation == null) {
            renderLoadError("会话不存在或无权访问。");
            return;
        }
        updateConversationState(detail.conversation);
        chatAdapter.submit(detail.messages);
        emptyView.setVisibility(detail.messages == null || detail.messages.isEmpty() ? View.VISIBLE : View.GONE);
        if (chatAdapter.size() > 0) {
            messages.scrollToPosition(chatAdapter.size() - 1);
        }
        configureInput();
        showContent();
    }

    private void updateConversationState(Conversation conversation) {
        mutualFollow = Boolean.TRUE.equals(conversation.mutualFollow);
        remaining = conversation.remainingNonMutualMessages == null ? 0 : conversation.remainingNonMutualMessages;
        canSend = mutualFollow || (Boolean.TRUE.equals(conversation.canSend) && remaining > 0) || remaining > 0;
        String peerName = peerName(conversation);
        setText(R.id.page_title, peerName);
        renderHero();
    }

    private void renderHero() {
        if (mutualFollow) {
            stylePill(heroPill, "互相关注", R.drawable.bg_pill_ok, R.color.doyu_mint_deep);
            heroTitle.setText("可以继续聊天");
            heroDesc.setText("输入框启用，发送失败时保留草稿并展示重试。");
        } else if (canSend) {
            stylePill(heroPill, "未互关剩余 " + remaining + " 条", R.drawable.bg_pill_warn, R.color.doyu_warn);
            heroTitle.setText("未互关对话");
            heroDesc.setText("你还可以发送 " + remaining + " 条消息，互相关注后不再受限。");
        } else {
            stylePill(heroPill, "已达上限", R.drawable.bg_pill_stop, R.color.doyu_danger);
            heroTitle.setText("超过 3 条");
            heroDesc.setText("互相关注后可继续聊天。");
        }
    }

    private void configureInput() {
        clearFailure();
        if (canSend) {
            input.setEnabled(true);
            input.setFocusable(true);
            input.setFocusableInTouchMode(true);
            send.setVisibility(View.VISIBLE);
            send.setEnabled(!sending);
            follow.setVisibility(View.GONE);
            warning.setVisibility(View.GONE);
            if (mutualFollow) {
                stylePill(status, "互关可发送", R.drawable.bg_pill_ok, R.color.doyu_mint_deep);
            } else {
                stylePill(status, "未互关剩余 " + remaining + " 条", R.drawable.bg_pill_warn, R.color.doyu_warn);
            }
        } else {
            input.setEnabled(false);
            input.setFocusable(false);
            send.setVisibility(View.GONE);
            follow.setVisibility(View.VISIBLE);
            warning.setVisibility(View.VISIBLE);
            warning.setText("互相关注后可继续聊天");
            stylePill(status, "已达上限", R.drawable.bg_pill_stop, R.color.doyu_danger);
        }
    }

    private void doSend() {
        if (sending || !canSend) {
            return;
        }
        String text = input.getText() == null ? "" : input.getText().toString().trim();
        if (text.isEmpty()) {
            return;
        }
        sending = true;
        send.setEnabled(false);
        send.setText("发送中");
        retryRow.setVisibility(View.GONE);
        warning.setVisibility(View.GONE);
        loadDetail(
                repository -> repository.sendMessage(conversationId, text),
                this::onSendSuccess,
                (state, message) -> onSendError()
        );
    }

    private void onSendSuccess(ChatMessage message) {
        sending = false;
        send.setText("发送");
        send.setEnabled(true);
        input.setText("");
        if (message != null) {
            chatAdapter.append(message);
            emptyView.setVisibility(View.GONE);
            messages.scrollToPosition(chatAdapter.size() - 1);
        }
        clearFailure();
        // Refresh authoritative remaining/canSend without disturbing the message list.
        loadDetail(
                repository -> repository.conversation(conversationId),
                detail -> {
                    if (detail != null && detail.conversation != null) {
                        updateConversationState(detail.conversation);
                        configureInput();
                    }
                },
                (state, message2) -> { /* keep current UI; next manual action will refresh */ }
        );
    }

    private void onSendError() {
        sending = false;
        send.setText("发送");
        send.setEnabled(true);
        // Re-check authoritative state: a real limit hit must show the blocked boundary, not a network error.
        loadDetail(
                repository -> repository.conversation(conversationId),
                detail -> {
                    if (detail != null && detail.conversation != null) {
                        updateConversationState(detail.conversation);
                        if (!canSend) {
                            configureInput();
                        } else {
                            showFailure();
                        }
                    } else {
                        showFailure();
                    }
                },
                (state, message) -> showFailure()
        );
    }

    private void showFailure() {
        warning.setVisibility(View.VISIBLE);
        warning.setText("网络异常，消息未发送，草稿已保留在输入框。");
        retryRow.setVisibility(View.VISIBLE);
    }

    private void clearFailure() {
        retryRow.setVisibility(View.GONE);
        if (canSend) {
            warning.setVisibility(View.GONE);
        }
    }

    private void stylePill(TextView pill, String text, int bgRes, int colorRes) {
        pill.setText(text);
        pill.setBackgroundResource(bgRes);
        pill.setTextColor(ContextCompat.getColor(this, colorRes));
    }

    private void showLoading() {
        loading.setVisibility(View.VISIBLE);
        content.setVisibility(View.GONE);
        errorBox.setVisibility(View.GONE);
    }

    private void showContent() {
        loading.setVisibility(View.GONE);
        content.setVisibility(View.VISIBLE);
        errorBox.setVisibility(View.GONE);
    }

    private void renderLoadError(String message) {
        loading.setVisibility(View.GONE);
        content.setVisibility(View.GONE);
        errorBox.setVisibility(View.VISIBLE);
        errorText.setText(valueOrFallback(message, "会话加载失败，请重试。"));
    }

    private static String peerName(Conversation conversation) {
        if (conversation.peer != null && conversation.peer.nickname != null && !conversation.peer.nickname.trim().isEmpty()) {
            return conversation.peer.nickname;
        }
        if (conversation.peerName != null && !conversation.peerName.trim().isEmpty()) {
            return conversation.peerName;
        }
        return "对方";
    }
}
