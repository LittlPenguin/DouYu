package cn.edu.app.douyu.feature.commerce;

import cn.edu.app.douyu.model.AddressSnapshot;
import cn.edu.app.douyu.model.CartResponse;
import cn.edu.app.douyu.model.Product;
import cn.edu.app.douyu.model.ProductSku;

import java.util.Locale;
/**
 * 购买表单状态：保存商品详情页和购物车下单时的收货信息输入。
 */

final class PurchaseFormState {
    private PurchaseFormState() {
    }

    static ProductSku firstPurchasableSku(Product product) {
        if (product == null
                || !"SELF_OPERATED".equals(product.type)
                || !"ON_SALE".equals(product.status)
                || !"PASS".equals(product.auditStatus)
                || product.skus == null) {
            return null;
        }
        for (ProductSku sku : product.skus) {
            if (sku != null
                    && sku.skuId != null
                    && !sku.skuId.isEmpty()
                    && "ON_SALE".equals(sku.status)
                    && stock(sku) > 0) {
                return sku;
            }
        }
        return null;
    }

    static int clampQuantity(int quantity, int availableStock) {
        if (availableStock <= 0) {
            return 0;
        }
        if (quantity < 1) {
            return 1;
        }
        return Math.min(quantity, availableStock);
    }

    static CartLimit addToCartLimit(ProductSku sku, CartResponse cart, int requestedQuantity) {
        int stock = stock(sku);
        int existingQuantity = existingSkuQuantity(cart, sku == null ? null : sku.skuId);
        int remainingQuantity = Math.max(0, stock - existingQuantity);
        int allowedQuantity = Math.min(Math.max(0, requestedQuantity), remainingQuantity);
        if (stock <= 0) {
            return new CartLimit(false, stock, existingQuantity, remainingQuantity, 0, "商品暂无库存");
        }
        if (requestedQuantity <= 0) {
            return new CartLimit(false, stock, existingQuantity, remainingQuantity, allowedQuantity, "请选择加入数量");
        }
        if (remainingQuantity <= 0) {
            return new CartLimit(false, stock, existingQuantity, remainingQuantity, 0, "已达到库存上限");
        }
        if (requestedQuantity > remainingQuantity) {
            return new CartLimit(false, stock, existingQuantity, remainingQuantity, allowedQuantity,
                    existingQuantity > 0
                            ? "库存仅剩 " + stock + " 件，购物车中已有 " + existingQuantity + " 件，最多还能加入 " + remainingQuantity + " 件"
                            : "库存仅剩 " + stock + " 件，最多还能加入 " + remainingQuantity + " 件");
        }
        return new CartLimit(true, stock, existingQuantity, remainingQuantity, requestedQuantity, "");
    }

    static QuantityLimit cartQuantityLimit(int currentQuantity, int delta, int availableStock) {
        if (availableStock <= 0) {
            return new QuantityLimit(false, 0, "商品暂无库存");
        }
        int current = clampQuantity(currentQuantity, availableStock);
        int next = currentQuantity + delta;
        if (next > availableStock) {
            return new QuantityLimit(false, availableStock, "已达到库存上限");
        }
        if (next < 1) {
            return new QuantityLimit(false, 1, "至少保留 1 件商品");
        }
        if (next == currentQuantity) {
            return new QuantityLimit(false, current, "");
        }
        return new QuantityLimit(true, next, "");
    }

    static String commerceMessage(String message) {
        if (message == null || message.isEmpty()) {
            return "请求失败，请重试。";
        }
        String normalized = message.toLowerCase(Locale.ROOT);
        if (normalized.contains("insufficient stock")
                || normalized.contains("inventory")
                || normalized.contains("http 409")
                || normalized.contains(" conflict")) {
            return "库存不足，请减少数量后重试。";
        }
        if ("conflict".equals(normalized.trim()) || normalized.contains("conflict")) {
            return "请求状态已变化，请刷新后重试。";
        }
        if (message.contains("Address snapshot is required")) {
            return "请填写收货信息后再提交订单。";
        }
        if (message.contains("Address ") && message.contains(" is required")) {
            return "请完整填写收货信息。";
        }
        if (message.contains("Product is unavailable")) {
            return "商品不存在或已下架。";
        }
        if (message.contains("Player products do not support standard")) {
            return "该商品不支持标准购物车或下单流程。";
        }
        if (message.contains("SKU not found")) {
            return "请选择可购买的商品规格。";
        }
        if (message.contains("Cart item id is required")) {
            return "请选择购物车商品。";
        }
        if (message.contains("Cart item not found")) {
            return "购物车商品不存在，请刷新后重试。";
        }
        if (message.contains("Order must contain at least one item")) {
            return "请选择商品后再提交订单。";
        }
        return message;
    }

    static boolean validAddress(AddressSnapshot address) {
        return address != null
                && !isBlank(address.recipient)
                && !isBlank(address.phone)
                && !isBlank(address.region)
                && !isBlank(address.detail);
    }

    static int stock(ProductSku sku) {
        return sku == null || sku.stock == null ? 0 : Math.max(0, sku.stock);
    }

    private static int existingSkuQuantity(CartResponse cart, String skuId) {
        if (cart == null || cart.items == null || skuId == null || skuId.isEmpty()) {
            return 0;
        }
        int quantity = 0;
        for (CartResponse.Item item : cart.items) {
            if (item == null) {
                continue;
            }
            String itemSkuId = item.skuId;
            if ((itemSkuId == null || itemSkuId.isEmpty()) && item.sku != null) {
                itemSkuId = item.sku.skuId;
            }
            if (skuId.equals(itemSkuId)) {
                quantity += Math.max(0, item.quantityValue());
            }
        }
        return quantity;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    static final class CartLimit {
        final boolean allowed;
        final int stock;
        final int existingQuantity;
        final int remainingQuantity;
        final int allowedQuantity;
        final String message;

        CartLimit(boolean allowed, int stock, int existingQuantity, int remainingQuantity, int allowedQuantity, String message) {
            this.allowed = allowed;
            this.stock = stock;
            this.existingQuantity = existingQuantity;
            this.remainingQuantity = remainingQuantity;
            this.allowedQuantity = allowedQuantity;
            this.message = message;
        }
    }

    static final class QuantityLimit {
        final boolean allowed;
        final int quantity;
        final String message;

        QuantityLimit(boolean allowed, int quantity, String message) {
            this.allowed = allowed;
            this.quantity = quantity;
            this.message = message;
        }
    }
}
