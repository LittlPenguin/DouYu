package cn.edu.app.douyu.feature.profile;

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
import cn.edu.app.douyu.model.UserProfile;

public class ProfileUserAdapter extends RecyclerView.Adapter<ProfileUserAdapter.Holder> {
    private final List<UserProfile> items = new ArrayList<>();

    public void submit(List<UserProfile> next) {
        items.clear();
        if (next != null) {
            items.addAll(next);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile_user, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        UserProfile user = items.get(position);
        holder.name.setText(safe(user.nickname, "豆友"));
        holder.bio.setText(safe(user.bio, "拼豆爱好者"));
        holder.counts.setText("关注 " + count(user.followingCount) + " · 粉丝 " + count(user.followerCount));
        if (user.avatarUrl != null && user.avatarUrl.startsWith("http")) {
            Glide.with(holder.avatar)
                    .load(user.avatarUrl)
                    .circleCrop()
                    .placeholder(R.drawable.bg_avatar_circle)
                    .error(R.drawable.bg_avatar_circle)
                    .into(holder.avatar);
        } else {
            holder.avatar.setImageDrawable(null);
            holder.avatar.setBackgroundResource(R.drawable.bg_avatar_circle);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ImageView avatar;
        final TextView name;
        final TextView bio;
        final TextView counts;

        Holder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.profile_user_avatar);
            name = itemView.findViewById(R.id.profile_user_name);
            bio = itemView.findViewById(R.id.profile_user_bio);
            counts = itemView.findViewById(R.id.profile_user_counts);
        }
    }

    private static String safe(String value, String fallback) {
        return value == null || value.isEmpty() ? fallback : value;
    }

    private static int count(Integer value) {
        return value == null ? 0 : value;
    }
}
