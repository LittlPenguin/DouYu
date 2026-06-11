package cn.edu.app.douyu.feature.message;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.edu.app.douyu.R;
import cn.edu.app.douyu.model.NotificationMessage;
/**
 * 消息首页适配器：渲染通知分组和通知入口，不承载私信会话。
 */

class MessageHomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    interface Listener {
        void onNotificationClick(NotificationMessage notification);
    }

    private static final int TYPE_SECTION = 0;
    private static final int TYPE_NOTIFICATION = 1;
    private static final int TYPE_EMPTY = 2;

    private final List<Row> rows = new ArrayList<>();
    private final Listener listener;

    MessageHomeAdapter(Listener listener) {
        this.listener = listener;
    }

    void submit(List<NotificationMessage> notifications) {
        rows.clear();
        rows.add(Row.section("通知"));
        if (notifications == null || notifications.isEmpty()) {
            rows.add(Row.empty("暂无通知"));
        } else {
            for (NotificationMessage notification : notifications) {
                rows.add(Row.notification(notification));
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_SECTION) {
            return new SectionHolder(inflater.inflate(R.layout.item_message_section_header, parent, false));
        }
        if (viewType == TYPE_NOTIFICATION) {
            return new NotificationHolder(inflater.inflate(R.layout.item_notification, parent, false));
        }
        return new EmptyHolder(inflater.inflate(R.layout.item_message_section_empty, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Row row = rows.get(position);
        if (holder instanceof SectionHolder) {
            bindSection((SectionHolder) holder, row);
        } else if (holder instanceof NotificationHolder) {
            bindNotification((NotificationHolder) holder, row.notification);
        } else if (holder instanceof EmptyHolder) {
            ((EmptyHolder) holder).text.setText(row.text);
        }
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    private void bindSection(SectionHolder holder, Row row) {
        holder.title.setText(row.text);
        holder.title.setBackgroundResource(R.drawable.bg_pill_ok);
        holder.title.setTextColor(ContextCompat.getColor(holder.title.getContext(), R.color.doyu_mint_deep));
    }

    private void bindNotification(NotificationHolder holder, NotificationMessage notification) {
        holder.icon.setImageResource(NotificationTypes.iconRes(notification.type));
        holder.title.setText(safe(notification.title, "通知"));
        holder.content.setText(safe(notification.content, ""));
        String time = NotificationTypes.shortTime(notification.createdAt);
        holder.meta.setText(time);
        holder.meta.setVisibility(time.isEmpty() ? View.GONE : View.VISIBLE);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(notification);
            }
        });
    }

    private static String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private static final class Row {
        final int type;
        final String text;
        final NotificationMessage notification;

        private Row(int type, String text, NotificationMessage notification) {
            this.type = type;
            this.text = text;
            this.notification = notification;
        }

        static Row section(String text) {
            return new Row(TYPE_SECTION, text, null);
        }

        static Row empty(String text) {
            return new Row(TYPE_EMPTY, text, null);
        }

        static Row notification(NotificationMessage notification) {
            return new Row(TYPE_NOTIFICATION, null, notification);
        }
    }

    static final class SectionHolder extends RecyclerView.ViewHolder {
        final TextView title;

        SectionHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.message_section_title);
        }
    }

    static final class NotificationHolder extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView title;
        final TextView content;
        final TextView meta;

        NotificationHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.notification_icon);
            title = itemView.findViewById(R.id.notification_title);
            content = itemView.findViewById(R.id.notification_content);
            meta = itemView.findViewById(R.id.notification_meta);
        }
    }

    static final class EmptyHolder extends RecyclerView.ViewHolder {
        final TextView text;

        EmptyHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.message_section_empty);
        }
    }
}
