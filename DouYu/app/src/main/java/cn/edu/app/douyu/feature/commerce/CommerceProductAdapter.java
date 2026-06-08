package cn.edu.app.douyu.feature.commerce;

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
import cn.edu.app.douyu.core.MoneyFormatter;
import cn.edu.app.douyu.model.Product;

class CommerceProductAdapter extends RecyclerView.Adapter<CommerceProductAdapter.Holder> {
    interface Listener {
        void onProductClick(Product product);
    }

    private static final int MIN_IMAGE_DP = 120;
    private static final int MAX_IMAGE_DP = 260;
    private static final int FALLBACK_IMAGE_DP = 168;

    private final List<Product> products = new ArrayList<>();
    private final Listener listener;

    CommerceProductAdapter(Listener listener) {
        this.listener = listener;
    }

    void submit(List<Product> nextProducts) {
        products.clear();
        if (nextProducts != null) {
            products.addAll(nextProducts);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_commerce_product, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Product product = products.get(position);
        ViewGroup.LayoutParams imageParams = holder.image.getLayoutParams();
        imageParams.height = dp(holder.image, masonryHeightDp(product.imageWidth, product.imageHeight));
        holder.image.setLayoutParams(imageParams);

        holder.title.setText(nonBlank(first(product.title, product.name), "未命名商品"));
        holder.price.setText(MoneyFormatter.centsToYuan(firstPrice(product)));
        holder.category.setText(nonBlank(first(product.categoryName, product.type, product.productType), "未分类"));
        holder.stock.setText("库存 " + count(product.stock));

        if (product.imageUrl == null || product.imageUrl.isEmpty()) {
            holder.image.setImageResource(R.drawable.bg_image_placeholder);
        } else {
            Glide.with(holder.image)
                    .load(product.imageUrl)
                    .placeholder(R.drawable.bg_image_placeholder)
                    .error(R.drawable.bg_image_placeholder)
                    .into(holder.image);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
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

    private static Integer firstPrice(Product product) {
        if (product.priceCents != null) {
            return product.priceCents;
        }
        if (product.skus == null || product.skus.isEmpty()) {
            return null;
        }
        return product.skus.get(0).priceCent;
    }

    private static String first(String first, String second) {
        return first(first, second, null);
    }

    private static String first(String first, String second, String third) {
        if (first != null && !first.isEmpty()) {
            return first;
        }
        if (second != null && !second.isEmpty()) {
            return second;
        }
        if (third != null && !third.isEmpty()) {
            return third;
        }
        return null;
    }

    private static String nonBlank(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private static int count(Integer value) {
        return value == null ? 0 : Math.max(0, value);
    }

    private static int dp(View view, int value) {
        return Math.round(value * view.getResources().getDisplayMetrics().density);
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView title;
        final TextView price;
        final TextView category;
        final TextView stock;

        Holder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.commerce_product_image);
            title = itemView.findViewById(R.id.commerce_product_title);
            price = itemView.findViewById(R.id.commerce_product_price);
            category = itemView.findViewById(R.id.commerce_product_category);
            stock = itemView.findViewById(R.id.commerce_product_stock);
        }
    }
}

