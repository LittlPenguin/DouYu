package cn.edu.app.douyu.feature.commerce;

import android.content.Intent;
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
        return "商品详情";
    }

    @Override
    protected void bindViews() {
        String productId = extra(IntentExtras.PRODUCT_ID);
        if (productId.isEmpty()) {
            setText(R.id.product_detail_id, "缺少 productId，无法请求商品详情。");
        } else {
            setText(R.id.product_title, "正在加载商品");
            setText(R.id.product_detail_id, "正在加载商品详情：" + productId);
            loadDetail(
                    repository -> repository.product(productId),
                    this::renderProduct,
                    (state, message) -> setText(R.id.product_detail_id, message)
            );
        }
        findViewById(R.id.product_payment_boundary).setOnClickListener(v -> startActivity(new Intent(this, PaymentBoundaryActivity.class)));
    }

    private void renderProduct(Product product) {
        if (product == null) {
            setText(R.id.product_title, "商品不可用");
            setText(R.id.product_detail_id, "商品不存在或已下架。");
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

        setText(R.id.product_title, first(product.title, product.name, "未命名商品"));
        setText(R.id.product_price, MoneyFormatter.centsToYuan(firstPrice(product)));
        setText(R.id.product_detail_id,
                "商品 ID：" + valueOrFallback(product.productId, "未知") + "\n"
                        + "分类：" + first(product.categoryName, product.type, product.productType, "未分类") + "\n"
                        + "库存：" + stockSummary(product) + "\n"
                        + "状态：" + valueOrFallback(product.status, "未知") + "\n"
                        + "审核：" + valueOrFallback(product.auditStatus, "暂无") + "\n"
                        + "规格：" + skuSummary(product) + "\n"
                        + "说明：" + valueOrFallback(product.description, "暂无商品说明"));
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
            return "暂无库存";
        }
        int total = 0;
        for (ProductSku sku : product.skus) {
            total += sku.stock == null ? 0 : Math.max(0, sku.stock);
        }
        return String.valueOf(total);
    }

    private static String skuSummary(Product product) {
        if (product.skus == null || product.skus.isEmpty()) {
            return "暂无规格";
        }
        StringBuilder builder = new StringBuilder();
        for (ProductSku sku : product.skus) {
            if (builder.length() > 0) {
                builder.append("；");
            }
            builder.append(valueOrFallback(sku.specName, "默认规格"))
                    .append(" ")
                    .append(MoneyFormatter.centsToYuan(sku.priceCent))
                    .append(" 库存 ")
                    .append(sku.stock == null ? 0 : sku.stock);
        }
        return builder.toString();
    }

    private static String tradeBoundary(Product product) {
        String type = first(product.type, product.productType, "");
        if ("SELF_OPERATED".equals(type)) {
            return "自营商品可进入购物和支付联调边界；真实收货地址、正式支付渠道和订单闭环仍以服务端能力为准。";
        }
        if ("PLAYER_SECOND_HAND".equals(type)) {
            return "玩家二手商品只展示信息和直连边界，不进入标准购物车。";
        }
        if ("PLAYER_CUSTOM_SERVICE".equals(type)) {
            return "玩家定制服务只展示咨询边界，不伪造成标准下单或支付成功。";
        }
        return "交易能力按商品类型展示边界，不使用假订单或假支付结果。";
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
