package cn.edu.app.douyu.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import cn.edu.app.douyu.R;

public class SummaryAdapter extends RecyclerView.Adapter<SummaryAdapter.Holder> {
    public interface Listener {
        void onItemClick(SummaryItem item);
    }

    private final List<SummaryItem> items = new ArrayList<>();
    private final Listener listener;
    private boolean masonry;

    public SummaryAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<SummaryItem> nextItems) {
        items.clear();
        if (nextItems != null) {
            items.addAll(nextItems);
        }
        notifyDataSetChanged();
    }

    public void setMasonry(boolean masonry) {
        this.masonry = masonry;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_summary, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        SummaryItem item = items.get(position);
        ViewGroup.LayoutParams imageParams = holder.image.getLayoutParams();
        imageParams.height = dp(holder.image, masonryHeight(position));
        holder.image.setLayoutParams(imageParams);
        holder.title.setText(item.title);
        holder.subtitle.setText(item.subtitle);
        if (item.imageUrl == null || item.imageUrl.isEmpty()) {
            holder.image.setImageResource(R.drawable.bg_image_placeholder);
        } else {
            Glide.with(holder.image).load(item.imageUrl).placeholder(R.drawable.bg_image_placeholder).into(holder.image);
        }
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView title;
        final TextView subtitle;

        Holder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.summary_image);
            title = itemView.findViewById(R.id.summary_title);
            subtitle = itemView.findViewById(R.id.summary_subtitle);
        }
    }

    private int masonryHeight(int position) {
        if (!masonry) {
            return 56;
        }
        int mod = position % 4;
        if (mod == 1) {
            return 242;
        }
        if (mod == 2) {
            return 120;
        }
        if (mod == 3) {
            return 176;
        }
        return 150;
    }

    private static int dp(View view, int value) {
        return Math.round(value * view.getResources().getDisplayMetrics().density);
    }
}
