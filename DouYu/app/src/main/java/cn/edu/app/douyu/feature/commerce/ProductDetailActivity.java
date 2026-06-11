package cn.edu.app.douyu.feature.commerce;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import cn.edu.app.douyu.R;
import cn.edu.app.douyu.auth.AuthGate;
import cn.edu.app.douyu.auth.LoginActivity;
import cn.edu.app.douyu.core.IntentExtras;
import cn.edu.app.douyu.core.MoneyFormatter;
import cn.edu.app.douyu.model.AddressSnapshot;
import cn.edu.app.douyu.model.CartResponse;
import cn.edu.app.douyu.model.Order;
import cn.edu.app.douyu.model.Product;
import cn.edu.app.douyu.model.ProductSku;
import cn.edu.app.douyu.ui.LoadState;
import cn.edu.app.douyu.ui.XmlPageActivity;

public class ProductDetailActivity extends XmlPageActivity {
    private Product currentProduct;
    private ProductSku selectedSku;
    private int quantity;
    private boolean busy;
    private Runnable pendingAfterLogin;

    private View purchaseFormGroup;
    private TextView purchaseSkuSummary;
    private TextView quantityValue;
    private MaterialButton quantityDecrease;
    private MaterialButton quantityIncrease;
    private MaterialButton addToCartButton;
    private MaterialButton buyNowButton;
    private MaterialButton openCartButton;
    private TextView purchaseStatus;
    private EditText recipientInput;
    private EditText phoneInput;
    private EditText regionInput;
    private EditText detailInput;
    private EditText remarkInput;
    private ActivityResultLauncher<Intent> loginLauncher;

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
        bindFields();
        registerLoginLauncher();
        bindActions();
        renderPurchaseControls("正在加载商品...");

        String productId = extra(IntentExtras.PRODUCT_ID);
        if (productId.isEmpty()) {
            setText(R.id.product_detail_id, "缺少商品 ID，无法加载商品详情。");
            renderUnavailable("缺少商品 ID。");
            return;
        }
        setText(R.id.product_title, "正在加载商品");
        setText(R.id.product_detail_id, "正在加载商品详情：" + productId);
        loadDetail(
                repository -> repository.product(productId),
                this::renderProduct,
                (state, message) -> {
                    setText(R.id.product_detail_id, message);
                    renderUnavailable(message);
                }
        );
    }

    private void bindFields() {
        purchaseFormGroup = findViewById(R.id.purchase_form_group);
        purchaseSkuSummary = findViewById(R.id.purchase_sku_summary);
        quantityValue = findViewById(R.id.purchase_quantity_value);
        quantityDecrease = findViewById(R.id.purchase_quantity_decrease);
        quantityIncrease = findViewById(R.id.purchase_quantity_increase);
        addToCartButton = findViewById(R.id.add_to_cart_button);
        buyNowButton = findViewById(R.id.buy_now_button);
        openCartButton = findViewById(R.id.open_cart_button);
        purchaseStatus = findViewById(R.id.purchase_status);
        recipientInput = findViewById(R.id.address_recipient_input);
        phoneInput = findViewById(R.id.address_phone_input);
        regionInput = findViewById(R.id.address_region_input);
        detailInput = findViewById(R.id.address_detail_input);
        remarkInput = findViewById(R.id.order_remark_input);
    }

    private void registerLoginLauncher() {
        loginLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() != RESULT_OK) {
                return;
            }
            String action = result.getData() == null
                    ? ""
                    : result.getData().getStringExtra(LoginActivity.EXTRA_RETURN_ACTION);
            Runnable pending = pendingAfterLogin;
            pendingAfterLogin = null;
            if (pending != null && AuthGate.RETURN_ACTION_PURCHASE.equals(action)) {
                pending.run();
            } else if (AuthGate.RETURN_ACTION_CART.equals(action)) {
                openCart();
            } else {
                showStatus("已登录，请继续完成本商品购买。", false);
            }
        });
    }

    private void bindActions() {
        quantityDecrease.setOnClickListener(v -> changeQuantity(-1));
        quantityIncrease.setOnClickListener(v -> changeQuantity(1));
        addToCartButton.setOnClickListener(v -> addToCart());
        buyNowButton.setOnClickListener(v -> buyNow());
        openCartButton.setOnClickListener(v -> requireLoggedIn(AuthGate.RETURN_ACTION_CART, this::openCart));
    }

    private void renderProduct(Product product) {
        currentProduct = product;
        if (product == null) {
            setText(R.id.product_title, "商品不可购买");
            setText(R.id.product_detail_id, "商品不存在或已下架。");
            renderUnavailable("商品不存在或已下架。");
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

        selectedSku = PurchaseFormState.firstPurchasableSku(product);
        if (selectedSku != null) {
            quantity = PurchaseFormState.clampQuantity(quantity <= 0 ? 1 : quantity, PurchaseFormState.stock(selectedSku));
        } else {
            quantity = 0;
        }

        setText(R.id.product_title, first(product.title, product.name, "未命名商品"));
        setText(R.id.product_price, MoneyFormatter.centsToYuan(firstPrice(product)));
        setText(R.id.product_detail_id,
                "商品 ID：" + valueOrFallback(product.productId, "未知") + "\n"
                        + "分类：" + first(product.categoryName, product.type, product.productType, "未分类") + "\n"
                        + "库存：" + stockSummary(product) + "\n"
                        + "状态：" + productStatus(product.status) + "\n"
                        + "审核：" + auditStatus(product.auditStatus) + "\n"
                        + "规格：" + skuSummary(product) + "\n"
                        + "描述：" + valueOrFallback(product.description, "暂无商品描述"));
        setText(R.id.product_trade_boundary, tradeBoundary(product));
        renderPurchaseControls(null);
    }

    private void renderUnavailable(String message) {
        selectedSku = null;
        quantity = 0;
        renderPurchaseControls(message == null || message.isEmpty() ? "该商品暂不可下单。" : message);
    }

    private void renderPurchaseControls(String message) {
        boolean purchasable = selectedSku != null && quantity > 0;
        if (purchaseFormGroup != null) {
            purchaseFormGroup.setVisibility(purchasable ? View.VISIBLE : View.GONE);
        }
        if (purchaseSkuSummary != null) {
            purchaseSkuSummary.setText(purchasable
                    ? valueOrFallback(selectedSku.specName, "默认规格") + " · 库存 " + PurchaseFormState.stock(selectedSku)
                    : "登录后可查看购物车。当前商品暂不可下单。");
        }
        quantityValue.setText(String.valueOf(Math.max(0, quantity)));
        quantityDecrease.setEnabled(!busy && purchasable && quantity > 1);
        quantityIncrease.setEnabled(!busy && purchasable && quantity < PurchaseFormState.stock(selectedSku));
        addToCartButton.setEnabled(!busy && purchasable);
        buyNowButton.setEnabled(!busy && purchasable);
        openCartButton.setEnabled(!busy);
        addToCartButton.setAlpha(addToCartButton.isEnabled() ? 1f : 0.52f);
        buyNowButton.setAlpha(buyNowButton.isEnabled() ? 1f : 0.52f);
        quantityDecrease.setAlpha(quantityDecrease.isEnabled() ? 1f : 0.46f);
        quantityIncrease.setAlpha(quantityIncrease.isEnabled() ? 1f : 0.46f);
        if (message != null && !message.isEmpty()) {
            showStatus(message, true);
        } else if (purchasable && (purchaseStatus == null || purchaseStatus.getText().toString().trim().isEmpty())) {
            showStatus("请选择数量并填写真实收货信息后提交订单。", false);
        }
    }

    private void changeQuantity(int delta) {
        if (selectedSku == null || busy) {
            return;
        }
        quantity = PurchaseFormState.clampQuantity(quantity + delta, PurchaseFormState.stock(selectedSku));
        renderPurchaseControls(null);
    }

    private void addToCart() {
        if (selectedSku == null || busy) {
            renderPurchaseControls("该商品暂不能加入购物车。");
            return;
        }
        requireLoggedIn(AuthGate.RETURN_ACTION_PURCHASE, this::addToCartAfterLogin);
    }

    private void addToCartAfterLogin() {
        if (selectedSku == null || busy) {
            return;
        }
        setBusy(true, "正在加入购物车...");
        loadDetail(
                repository -> {
                    CartResponse cart = repository.cart();
                    PurchaseFormState.CartLimit limit = PurchaseFormState.addToCartLimit(selectedSku, cart, quantity);
                    if (!limit.allowed) {
                        return AddToCartResult.blocked(limit.message);
                    }
                    return AddToCartResult.added(repository.addCartItem(selectedSku.skuId, quantity));
                },
                this::renderAddToCartResult,
                (state, message) -> {
                    setBusy(false, null);
                    showStatus(userFacingCommerceError(state, message), true);
                }
        );
    }

    private void renderAddToCartResult(AddToCartResult result) {
        if (result != null && !result.allowed) {
            setBusy(false, null);
            showStatus(result.message, true);
            return;
        }
        renderAddedToCart(result == null ? null : result.item);
    }

    private void renderAddedToCart(CartResponse.Item item) {
        setBusy(false, null);
        String count = item == null || item.quantity == null ? String.valueOf(quantity) : String.valueOf(item.quantity);
        showStatus("已加入购物车，购物车数量：" + count + "。", false);
    }

    private void buyNow() {
        if (selectedSku == null || busy) {
            renderPurchaseControls("该商品暂不可下单。");
            return;
        }
        AddressSnapshot address = currentAddress();
        if (!PurchaseFormState.validAddress(address)) {
            showStatus("请填写收货人、手机号、所在地区和收货地址后再提交订单。", true);
            return;
        }
        requireLoggedIn(AuthGate.RETURN_ACTION_PURCHASE, this::buyNowAfterLogin);
    }

    private void buyNowAfterLogin() {
        if (selectedSku == null || busy) {
            return;
        }
        AddressSnapshot address = currentAddress();
        if (!PurchaseFormState.validAddress(address)) {
            showStatus("请填写收货人、手机号、所在地区和收货地址后再提交订单。", true);
            return;
        }
        String remark = text(remarkInput);
        setBusy(true, "正在创建订单...");
        loadDetail(
                repository -> repository.createImmediateOrder(selectedSku.skuId, quantity, address, remark),
                this::renderCreatedOrder,
                (state, message) -> {
                    setBusy(false, null);
                    showStatus(userFacingCommerceError(state, message), true);
                }
        );
    }

    private void renderCreatedOrder(Order order) {
        setBusy(false, null);
        if (order == null) {
            showStatus("创建订单成功，请刷新订单记录查看详情。", false);
            return;
        }
        showStatus("创建订单成功：" + valueOrFallback(order.orderId, "未知")
                + "\n状态：" + orderStatus(order.status)
                + "\n订单金额：" + MoneyFormatter.centsToYuan(order.payableAmountCent != null
                ? order.payableAmountCent : order.totalAmountCent), false);
    }

    private void openCart() {
        startActivity(new Intent(this, CartActivity.class));
    }

    private boolean requireLoggedIn(String returnAction, Runnable action) {
        pendingAfterLogin = action;
        return AuthGate.runOrRequestLogin(this, loginLauncher, returnAction, () -> {
            pendingAfterLogin = null;
            action.run();
        });
    }

    private AddressSnapshot currentAddress() {
        return new AddressSnapshot(
                text(recipientInput),
                text(phoneInput),
                text(regionInput),
                text(detailInput));
    }

    private void setBusy(boolean nextBusy, String message) {
        busy = nextBusy;
        if (message != null && !message.isEmpty()) {
            showStatus(message, false);
        }
        renderPurchaseControls(null);
    }

    private void showStatus(String message, boolean warning) {
        if (purchaseStatus == null) {
            return;
        }
        purchaseStatus.setVisibility(message == null || message.isEmpty() ? View.GONE : View.VISIBLE);
        purchaseStatus.setText(message == null ? "" : message);
        purchaseStatus.setTextColor(getColor(warning ? R.color.doyu_warn : R.color.doyu_text_muted));
    }

    private static String userFacingCommerceError(LoadState state, String message) {
        if (state == LoadState.LOGIN_REQUIRED) {
            return "请先登录，再操作购物车或创建订单。";
        }
        if (message == null || message.isEmpty()) {
            return "请求失败，请重试。";
        }
        return PurchaseFormState.commerceMessage(message);
    }

    private static String text(EditText input) {
        return input == null ? "" : input.getText().toString().trim();
    }

    private static Integer firstPrice(Product product) {
        ProductSku sku = PurchaseFormState.firstPurchasableSku(product);
        if (sku != null && sku.priceCent != null) {
            return sku.priceCent;
        }
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
            total += sku == null || sku.stock == null ? 0 : Math.max(0, sku.stock);
        }
        return String.valueOf(total);
    }

    private static String skuSummary(Product product) {
        if (product.skus == null || product.skus.isEmpty()) {
            return "暂无 SKU";
        }
        StringBuilder builder = new StringBuilder();
        for (ProductSku sku : product.skus) {
            if (sku == null) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append("; ");
            }
            builder.append(valueOrFallback(sku.specName, "默认规格"))
                    .append(" ")
                    .append(MoneyFormatter.centsToYuan(sku.priceCent))
                    .append(" 库存 ")
                    .append(sku.stock == null ? 0 : sku.stock);
        }
        return builder.length() == 0 ? "暂无 SKU" : builder.toString();
    }

    private static String tradeBoundary(Product product) {
        String type = first(product.type, product.productType, "");
        if ("SELF_OPERATED".equals(type)) {
            return "自营商品展示真实价格、库存、购物车和收货信息要求，订单记录以后端数据为准。";
        }
        if ("PLAYER_SECOND_HAND".equals(type)) {
            return "玩家二手商品仅展示和直接联系，不进入标准购物车。";
        }
        if ("PLAYER_CUSTOM_SERVICE".equals(type)) {
            return "玩家定制服务仅支持咨询，不创建标准订单。";
        }
        return "交易边界按商品类型执行，不展示虚假订单结果。";
    }

    private static String productStatus(String status) {
        if ("ON_SALE".equals(status)) {
            return "在售";
        }
        if ("DRAFT".equals(status)) {
            return "草稿";
        }
        if ("OFF_SALE".equals(status)) {
            return "已下架";
        }
        return valueOrFallback(status, "未知");
    }

    private static String auditStatus(String status) {
        if ("PASS".equals(status)) {
            return "已通过";
        }
        if ("NEED_MANUAL_REVIEW".equals(status)) {
            return "待人工审核";
        }
        if ("REJECTED".equals(status)) {
            return "未通过";
        }
        return valueOrFallback(status, "无");
    }

    private static String orderStatus(String status) {
        if ("CREATED".equals(status)) {
            return "已创建";
        }
        if ("CANCELED".equals(status)) {
            return "已取消";
        }
        return valueOrFallback(status, "已创建");
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

    private static final class AddToCartResult {
        final boolean allowed;
        final String message;
        final CartResponse.Item item;

        private AddToCartResult(boolean allowed, String message, CartResponse.Item item) {
            this.allowed = allowed;
            this.message = message;
            this.item = item;
        }

        static AddToCartResult blocked(String message) {
            return new AddToCartResult(false, message, null);
        }

        static AddToCartResult added(CartResponse.Item item) {
            return new AddToCartResult(true, "", item);
        }
    }
}
