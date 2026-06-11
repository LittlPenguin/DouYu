package cn.edu.app.douyu.feature.message;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.edu.app.douyu.R;
import cn.edu.app.douyu.model.NotificationMessage;
/**
 * 通知列表适配器：把后端通知记录渲染成消息列表项。
 */

class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.Holder> {
    interface Listener {
        void onNotificationClick(NotificationMessage notification);
    }

    private final List<NotificationMessage> items = new ArrayList<>();
    private final Listener listener;

    NotificationAdapter(Listener listener) {
        this.listener = listener;
    }

    void submit(List<NotificationMessage> next) {
        items.clear();
        if (next != null) {
            items.addAll(next);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        NotificationMessage notification = items.get(position);
        holder.icon.setImageResource(NotificationTypes.iconRes(notification.type));
        holder.title.setText(safe(notification.title, "通知"));
        holder.content.setText(safe(notification.content, ""));
        holder.meta.setText(NotificationTypes.shortTime(notification.createdAt));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(notification);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private static String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView title;
        final TextView content;
        final TextView meta;

        Holder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.notification_icon);
            title = itemView.findViewById(R.id.notification_title);
            content = itemView.findViewById(R.id.notification_content);
            meta = itemView.findViewById(R.id.notification_meta);
        }
    }
}
