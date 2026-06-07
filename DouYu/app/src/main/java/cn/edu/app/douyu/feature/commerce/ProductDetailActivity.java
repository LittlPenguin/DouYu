package cn.edu.app.douyu.feature.commerce;

import android.content.Intent;

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
            setText(R.id.product_detail_id, "商品不存在或已下架。");
            return;
        }
        setText(R.id.product_detail_id,
                "商品 ID：" + valueOrFallback(product.productId, "未知") + "\n"
                        + "名称：" + first(product.title, product.name, "未命名商品") + "\n"
                        + "类型：" + first(product.categoryName, product.type, product.productType, "未分类") + "\n"
                        + "价格：" + MoneyFormatter.centsToYuan(firstPrice(product)) + "\n"
                        + "状态：" + valueOrFallback(product.status, "未知") + "\n"
                        + "审核：" + valueOrFallback(product.auditStatus, "暂无") + "\n"
                        + "SKU：" + skuSummary(product) + "\n"
                        + "说明：" + valueOrFallback(product.description, "暂无商品说明"));
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
}
