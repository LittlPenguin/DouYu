package cn.edu.app.douyu.feature.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import cn.edu.app.douyu.R;
/**
 * 个人主页作品适配器：渲染用户发布、点赞或收藏的作品卡片。
 */

public class ProfileAssetAdapter extends RecyclerView.Adapter<ProfileAssetAdapter.Holder> {
    public interface Listener {
        void onCardClick(AssetCard card);
    }

    private static final int OK_LABEL_COLOR = 0xFF2E8F6E;
    private static final int[] HEIGHTS = {176, 120, 242, 150};

    private final List<AssetCard> items = new ArrayList<>();
    private final Listener listener;

    public ProfileAssetAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<AssetCard> next) {
        items.clear();
        if (next != null) {
            items.addAll(next);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile_asset, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        AssetCard card = items.get(position);

        ViewGroup.LayoutParams params = holder.image.getLayoutParams();
        params.height = dp(holder.image, HEIGHTS[position % HEIGHTS.length]);
        holder.image.setLayoutParams(params);
        if (card.imageUrl == null || card.imageUrl.isEmpty()) {
            holder.image.setImageResource(R.drawable.bg_image_placeholder);
        } else {
            Glide.with(holder.image).load(card.imageUrl).placeholder(R.drawable.bg_image_placeholder).into(holder.image);
        }

        holder.label.setText(card.label);
        if (card.labelOk) {
            holder.label.setBackgroundResource(R.drawable.bg_pill_ok);
            holder.label.setTextColor(OK_LABEL_COLOR);
        } else {
            holder.label.setBackgroundResource(R.drawable.bg_chip_plain);
            holder.label.setTextColor(ContextCompat.getColor(holder.label.getContext(), R.color.doyu_text_muted));
        }

        holder.title.setText(card.title == null ? "" : card.title);
        holder.metaLeft.setText(card.metaLeft == null ? "" : card.metaLeft);
        holder.metaRight.setText(card.metaRight == null ? "" : card.metaRight);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCardClick(card);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView label;
        final TextView title;
        final TextView metaLeft;
        final TextView metaRight;

        Holder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.asset_image);
            label = itemView.findViewById(R.id.asset_label);
            title = itemView.findViewById(R.id.asset_title);
            metaLeft = itemView.findViewById(R.id.asset_meta_left);
            metaRight = itemView.findViewById(R.id.asset_meta_right);
        }
    }

    private static int dp(View view, int value) {
        return Math.round(value * view.getResources().getDisplayMetrics().density);
    }
}
