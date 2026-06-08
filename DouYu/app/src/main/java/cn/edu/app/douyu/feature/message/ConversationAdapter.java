package cn.edu.app.douyu.feature.message;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.edu.app.douyu.R;
import cn.edu.app.douyu.model.Conversation;

class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.Holder> {
    interface Listener {
        void onConversationClick(Conversation conversation);
    }

    private final List<Conversation> items = new ArrayList<>();
    private final Listener listener;

    ConversationAdapter(Listener listener) {
        this.listener = listener;
    }

    void submit(List<Conversation> next) {
        items.clear();
        if (next != null) {
            items.addAll(next);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Conversation conversation = items.get(position);
        holder.title.setText(peerName(conversation));

        boolean mutual = Boolean.TRUE.equals(conversation.mutualFollow);
        int remaining = conversation.remainingNonMutualMessages == null ? 0 : conversation.remainingNonMutualMessages;
        boolean canSend = mutual || (Boolean.TRUE.equals(conversation.canSend) && remaining > 0) || remaining > 0;
        String lastMessage = conversation.lastMessage == null ? "" : conversation.lastMessage.trim();

        String statusText;
        int pillBg;
        int pillColor;
        if (mutual) {
            holder.subtitle.setText(withLast("互相关注", lastMessage));
            statusText = "互关";
            pillBg = R.drawable.bg_pill_ok;
            pillColor = R.color.doyu_mint_deep;
        } else if (canSend) {
            holder.subtitle.setText(withLast("未互关剩余 " + remaining + " 条", lastMessage));
            statusText = remaining + "/3";
            pillBg = R.drawable.bg_pill_warn;
            pillColor = R.color.doyu_warn;
        } else {
            holder.subtitle.setText("超过 3 条，互相关注后可继续聊天");
            statusText = "禁发";
            pillBg = R.drawable.bg_pill_stop;
            pillColor = R.color.doyu_danger;
        }
        holder.status.setText(statusText);
        holder.status.setBackgroundResource(pillBg);
        holder.status.setTextColor(ContextCompat.getColor(holder.status.getContext(), pillColor));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConversationClick(conversation);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private static String withLast(String status, String lastMessage) {
        return lastMessage.isEmpty() ? status : status + " · " + lastMessage;
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

    static class Holder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView subtitle;
        final TextView status;

        Holder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.conversation_title);
            subtitle = itemView.findViewById(R.id.conversation_subtitle);
            status = itemView.findViewById(R.id.conversation_status);
        }
    }
}
