package cn.edu.app.douyu.feature.community;

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
import cn.edu.app.douyu.model.SearchResult;

class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.Holder> {
    interface Listener {
        void onSearchResultClick(SearchResult result);
    }

    private final List<SearchResult> results = new ArrayList<>();
    private final Listener listener;

    SearchResultAdapter(Listener listener) {
        this.listener = listener;
    }

    void submit(List<SearchResult> nextResults) {
        results.clear();
        if (nextResults != null) {
            results.addAll(nextResults);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        SearchResult result = results.get(position);
        holder.type.setText(typeLabel(result.resultType, result.meta));
        holder.title.setText(nonBlank(result.title, "未命名结果"));
        holder.subtitle.setText(nonBlank(result.subtitle, ""));
        holder.subtitle.setVisibility(nonBlank(result.subtitle, "").isEmpty() ? View.GONE : View.VISIBLE);

        if (result.imageUrl == null || result.imageUrl.trim().isEmpty()) {
            holder.image.setImageResource(R.drawable.bg_image_placeholder);
        } else {
            Glide.with(holder.image)
                    .load(result.imageUrl)
                    .placeholder(R.drawable.bg_image_placeholder)
                    .error(R.drawable.bg_image_placeholder)
                    .into(holder.image);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSearchResultClick(result);
            }
        });
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    private static String typeLabel(String resultType, String meta) {
        if (meta != null && !meta.trim().isEmpty()) {
            return meta.trim();
        }
        if (SearchResult.TYPE_POST.equals(resultType)) {
            return "作品";
        }
        if (SearchResult.TYPE_PRODUCT.equals(resultType)) {
            return "商品";
        }
        if (SearchResult.TYPE_USER.equals(resultType)) {
            return "用户";
        }
        if (SearchResult.TYPE_TOPIC.equals(resultType)) {
            return "话题";
        }
        return "结果";
    }

    private static String nonBlank(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView type;
        final TextView title;
        final TextView subtitle;

        Holder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.search_result_image);
            type = itemView.findViewById(R.id.search_result_type);
            title = itemView.findViewById(R.id.search_result_title);
            subtitle = itemView.findViewById(R.id.search_result_subtitle);
        }
    }
}
