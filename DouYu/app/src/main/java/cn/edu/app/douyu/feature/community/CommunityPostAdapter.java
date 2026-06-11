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
import cn.edu.app.douyu.model.Post;
/**
 * 社区帖子列表适配器：把帖子 Feed 数据渲染成首页卡片并处理点击。
 */

class CommunityPostAdapter extends RecyclerView.Adapter<CommunityPostAdapter.Holder> {
    interface Listener {
        void onPostClick(Post post);
    }

    private static final int MIN_IMAGE_DP = 120;
    private static final int MAX_IMAGE_DP = 260;
    private static final int FALLBACK_IMAGE_DP = 168;

    private final List<Post> posts = new ArrayList<>();
    private final Listener listener;

    CommunityPostAdapter(Listener listener) {
        this.listener = listener;
    }

    void submit(List<Post> nextPosts) {
        posts.clear();
        if (nextPosts != null) {
            posts.addAll(nextPosts);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_community_post, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Post post = posts.get(position);
        int imageHeight = imageHeightDp(post);
        ViewGroup.LayoutParams imageParams = holder.image.getLayoutParams();
        imageParams.height = dp(holder.image, imageHeight);
        holder.image.setLayoutParams(imageParams);

        holder.title.setText(nonBlank(post.title, "未命名作品"));
        String author = post.author == null ? "豆友" : nonBlank(post.author.nickname, "豆友");
        holder.author.setText(author);
        holder.likes.setText(count(post.likeCount) + " 赞");

        if (post.coverImageUrl == null || post.coverImageUrl.isEmpty()) {
            holder.image.setImageResource(R.drawable.bg_image_placeholder);
        } else {
            Glide.with(holder.image)
                    .load(post.coverImageUrl)
                    .placeholder(R.drawable.bg_image_placeholder)
                    .error(R.drawable.bg_image_placeholder)
                    .into(holder.image);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPostClick(post);
            }
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public static int masonryHeightDp(Integer width, Integer height) {
        if (width == null || height == null || width <= 0 || height <= 0) {
            return FALLBACK_IMAGE_DP;
        }
        float ratio = height / (float) width;
        int calculated = Math.round(168f * ratio);
        if (calculated < MIN_IMAGE_DP) {
            return MIN_IMAGE_DP;
        }
        return Math.min(calculated, MAX_IMAGE_DP);
    }

    private static int imageHeightDp(Post post) {
        return masonryHeightDp(post.coverWidth, post.coverHeight);
    }

    private static String nonBlank(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private static int count(Integer value) {
        return value == null ? 0 : value;
    }

    private static int dp(View view, int value) {
        return Math.round(value * view.getResources().getDisplayMetrics().density);
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView title;
        final TextView author;
        final TextView likes;

        Holder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.community_post_image);
            title = itemView.findViewById(R.id.community_post_title);
            author = itemView.findViewById(R.id.community_post_author);
            likes = itemView.findViewById(R.id.community_post_likes);
        }
    }
}
