package cn.edu.app.douyu.feature.commerce;

import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import cn.edu.app.douyu.R;
import cn.edu.app.douyu.core.IntentExtras;
import cn.edu.app.douyu.core.MoneyFormatter;
import cn.edu.app.douyu.model.Product;
import cn.edu.app.douyu.model.ProductSku;
import cn.edu.app.douyu.ui.XmlPageActivity;

public class ProductDetailActivity extends XmlPageActivity {
    @Override
    protected int layoutRes() {
        return R.layout.activity_product_detail;
    }

    @Override
    protected String title() {
        return "Product detail";
    }

    @Override
    protected void bindViews() {
        String productId = extra(IntentExtras.PRODUCT_ID);
        if (productId.isEmpty()) {
            setText(R.id.product_detail_id, "Missing productId; product detail cannot be loaded.");
            return;
        }
        setText(R.id.product_title, "Loading product");
        setText(R.id.product_detail_id, "Loading product detail: " + productId);
        loadDetail(
                repository -> repository.product(productId),
                this::renderProduct,
                (state, message) -> setText(R.id.product_detail_id, message)
        );
    }

    private void renderProduct(Product product) {
        if (product == null) {
            setText(R.id.product_title, "Product unavailable");
            setText(R.id.product_detail_id, "The product does not exist or is offline.");
            return;
        }

        ImageView image = findViewById(R.id.product_image);
        ViewGroup.LayoutParams params = image.getLayoutParams();
        params.height = dp(CommerceProductAdapter.masonryHeightDp(product.imageWidth, product.imageHeight) + 60);
        image.setLayoutParams(params);
        if (product.imageUrl == null || product.imageUrl.isEmpty()) {
            image.setImageResource(R.drawable.bg_image_placeholder);
        } else {
            Glide.with(image)
                    .load(product.imageUrl)
                    .placeholder(R.drawable.bg_image_placeholder)
                    .error(R.drawable.bg_image_placeholder)
                    .into(image);
        }

        setText(R.id.product_title, first(product.title, product.name, "Untitled product"));
        setText(R.id.product_price, MoneyFormatter.centsToYuan(firstPrice(product)));
        setText(R.id.product_detail_id,
                "Product ID: " + valueOrFallback(product.productId, "unknown") + "\n"
                        + "Category: " + first(product.categoryName, product.type, product.productType, "uncategorized") + "\n"
                        + "Stock: " + stockSummary(product) + "\n"
                        + "Status: " + valueOrFallback(product.status, "unknown") + "\n"
                        + "Audit: " + valueOrFallback(product.auditStatus, "none") + "\n"
                        + "SKU: " + skuSummary(product) + "\n"
                        + "Description: " + valueOrFallback(product.description, "No product description"));
        setText(R.id.product_trade_boundary, tradeBoundary(product));
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

    private static String stockSummary(Product product) {
        if (product.stock != null) {
            return String.valueOf(Math.max(0, product.stock));
        }
        if (product.skus == null || product.skus.isEmpty()) {
            return "no stock";
        }
        int total = 0;
        for (ProductSku sku : product.skus) {
            total += sku.stock == null ? 0 : Math.max(0, sku.stock);
        }
        return String.valueOf(total);
    }

    private static String skuSummary(Product product) {
        if (product.skus == null || product.skus.isEmpty()) {
            return "no SKU";
        }
        StringBuilder builder = new StringBuilder();
        for (ProductSku sku : product.skus) {
            if (builder.length() > 0) {
                builder.append("; ");
            }
            builder.append(valueOrFallback(sku.specName, "default"))
                    .append(" ")
                    .append(MoneyFormatter.centsToYuan(sku.priceCent))
                    .append(" stock ")
                    .append(sku.stock == null ? 0 : sku.stock);
        }
        return builder.toString();
    }

    private static String tradeBoundary(Product product) {
        String type = first(product.type, product.productType, "");
        if ("SELF_OPERATED".equals(type)) {
            return "Self-operated products show real price, stock, and address requirements. Order records depend on backend data.";
        }
        if ("PLAYER_SECOND_HAND".equals(type)) {
            return "Player second-hand products are display and direct-contact only; they do not enter the standard cart.";
        }
        if ("PLAYER_CUSTOM_SERVICE".equals(type)) {
            return "Player custom services are inquiry-only and do not create standard orders.";
        }
        return "Commerce boundaries follow product type and never use fake order results.";
    }

    private static String first(String first, String second, String fallback) {
        return first(first, second, null, fallback);
    }

    private static String first(String first, String second, String third, String fallback) {
        if (first != null && !first.isEmpty()) {
            return first;
        }
        if (second != null && !second.isEmpty()) {
            return second;
        }
        if (third != null && !third.isEmpty()) {
            return third;
        }
        return fallback;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
