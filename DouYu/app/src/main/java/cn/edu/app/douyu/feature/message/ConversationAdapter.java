package cn.edu.app.douyu.feature.message;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
        holder.subtitle.setText(lastMessage(conversation));
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

    private static String lastMessage(Conversation conversation) {
        if (conversation.lastMessage != null && !conversation.lastMessage.trim().isEmpty()) {
            return conversation.lastMessage;
        }
        return "暂无消息";
    }

    static String peerName(Conversation conversation) {
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

        Holder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.conversation_title);
            subtitle = itemView.findViewById(R.id.conversation_subtitle);
        }
    }
}
