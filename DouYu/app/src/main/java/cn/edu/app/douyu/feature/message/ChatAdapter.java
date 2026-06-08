package cn.edu.app.douyu.feature.message;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.edu.app.douyu.R;
import cn.edu.app.douyu.model.ChatMessage;

/**
 * Chat bubbles for a private conversation. Own messages align right with the petal bubble;
 * peer messages align left with an avatar and the surface bubble.
 */
class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.Holder> {
    private final List<ChatMessage> items = new ArrayList<>();

    void submit(List<ChatMessage> next) {
        items.clear();
        if (next != null) {
            items.addAll(next);
        }
        notifyDataSetChanged();
    }

    void append(ChatMessage message) {
        if (message == null) {
            return;
        }
        items.add(message);
        notifyItemInserted(items.size() - 1);
    }

    int size() {
        return items.size();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        ChatMessage message = items.get(position);
        boolean mine = Boolean.TRUE.equals(message.mine);
        holder.bubble.setText(message.content == null ? "" : message.content);
        holder.bubble.setMaxWidth(Math.round(holder.itemView.getResources().getDisplayMetrics().widthPixels * 0.78f));
        holder.bubble.setBackgroundResource(mine ? R.drawable.bg_bubble_mine : R.drawable.bg_bubble_peer);
        holder.avatar.setVisibility(mine ? View.GONE : View.VISIBLE);
        holder.row.setGravity(mine ? Gravity.END : Gravity.START);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final LinearLayout row;
        final View avatar;
        final TextView bubble;

        Holder(@NonNull View itemView) {
            super(itemView);
            row = itemView.findViewById(R.id.chat_row);
            avatar = itemView.findViewById(R.id.chat_avatar);
            bubble = itemView.findViewById(R.id.chat_bubble);
        }
    }
}
